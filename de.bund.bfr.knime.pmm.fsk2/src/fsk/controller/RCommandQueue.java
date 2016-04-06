/**
 * TODO: file docstring
 */
package fsk.controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.workflow.NodeContext;
import org.rosuda.REngine.REXP;

import fsk.controller.IRController.RException;
import fsk.controller.RConsoleController.ExecutionMonitorFactory;

/**
 * A {@link LinkedBlockingQueue} which contains {@link RCommand} and as means of starting a thread
 * which will sequentially execute the RCommands in the queue.
 *
 * @author Jonathan Hale
 * @author Heiko Hofer
 */
public class RCommandQueue extends LinkedBlockingQueue<RCommand> {

    /**
     * Generated serialVersionUID
     */
    private static final long serialVersionUID = 7356174307842402380L;

    private final NodeLogger LOGGER = NodeLogger.getLogger(RController.class);

    private RCommandConsumer m_thread = null;

    private final RController m_controller;

    /**
     * Constructor
     *
     * @param controller {@link RController} for evaluating R code in the execution thread.
     */
    public RCommandQueue(final RController controller) {
        m_controller = controller;
    }

    /**
     * Inserts the specified script at the tail of this queue, waiting if necessary for space to
     * become available.
     *
     * @param showInConsole If the command should be copied into the R console.
     * @return {@link RCommand} which wraps the added <code>rScript</code>. Use {@link
     * RCommand#get()} to wait for execution and fetch the result of evaluation if any.
     */
    public RCommand putRScript(final String rScript, final boolean showInConsole) {
        RCommand rCommand = new RCommand(rScript.trim(), showInConsole);
        add(rCommand); // we did not limit capacity, this should always work.
        return rCommand;
    }

    /**
     * Interface for classes listening to the execution of commands in a {@link RCommandQueue}.
     *
     * @author Jonathan Hale.
     */
    interface RCommandExecutionListener {

        /**
         * Called before execution of a command is started.
         *
         * @param command the command to be executed.
         */
        void onCommandExecutionStart(RCommand command);

        /**
         * Called after a command has been completed, even if an error occured and {@link
         * #onCommandExecutionError(RException)} was called before.
         *
         * @param command command which has been completed
         */
        void onCommandExecutionEnd(RCommand command, String stoud, String stderr);

        /**
         * Called when an error occurs during execution of a command.
         *
         * @param e The exception that occurred.
         */
        void onCommandExecutionError(RException e);

        /**
         * Called when a command is cancelled.
         */
        void onCommandExecutionCanceled();
    }

    private final Set<RCommandExecutionListener> m_listeners = new HashSet<>();

    /**
     * Add a {@link RCommandExecutionListener} to listen to this RCommandQueue.
     *
     * @param l the Listener
     */
    void addRCommandExecutionListener(final RCommandExecutionListener l) {
        m_listeners.add(l);
    }

    /**
     * Remove a {@link RCommandExecutionListener} from this RCommandQueue.
     *
     * @param l the Listener
     */
    public void removeRCommandExecutionListener(final RCommandExecutionListener l) {
        m_listeners.remove(l);
    }

    /**
     * Thread which executes RCommands from the command queue.
     *
     * @author Jonathan Hale
     */
    private class RCommandConsumer extends Thread {

        private ExecutionMonitorFactory m_execMonitorFactory;
        private final NodeContext m_context;

        RCommandConsumer(final ExecutionMonitorFactory execMonitorFactory,
                         final boolean withContext) {
            super("RCommandQueue Executor");
            m_execMonitorFactory = execMonitorFactory;

            m_context = (withContext) ? NodeContext.getContext() : null;
        }

        @Override
        public void run() {
            ExecutionMonitor progress = null;
            try {
                if (m_context != null) {
                    NodeContext.pushContext(m_context);
                }

                while (!isInterrupted()) {
                    // interrupted flag is checked every 100 milliseconds while
                    // command queue is empty.
                    RCommand nextCommand = poll(50, TimeUnit.MILLISECONDS);

                    if (nextCommand == null) {
            /* queue was empty */
                        continue;
                    }

                    // we fetch the entire contents of the queue to be able to
                    // show progress for all commands currently in queue. This
                    // is necessary to prevent flashing of the progress bar in
                    // RSnippetNodePanel, which would happen because "invisible"
                    // commands are executed before and after user commands are
                    // executed.
                    final ArrayList<RCommand> commands = new ArrayList<>();
                    do {
                        commands.add(nextCommand);
                    } while ((nextCommand = poll(10, TimeUnit.MILLISECONDS)) != null);

                    progress = m_execMonitorFactory.create();
                    progress.setMessage("Executing commands...");
                    final int numCommands = commands.size();
                    final double progressIncrement = 1 / numCommands;

                    REXP ret = null;
                    for (RCommand rCmd : commands) {
                        m_listeners.stream().forEach((l) -> l.onCommandExecutionStart(rCmd));

                        ExecutionMonitor sub = progress.createSubProgress(progressIncrement);
                        sub.setProgress(0.0);
                        if (rCmd.isShowInConsole()) {
                            final ConsoleLikeRExecutor exec = new ConsoleLikeRExecutor(m_controller);
                            try {
                                exec.setupOutputCapturing(sub.createSubProgress(0.2));
                            } catch (RException e) {
                                m_listeners.stream().forEach((l) -> l.onCommandExecutionError(
                                        new RException("Could not capture output of command.", e)));
                            }

                            try {
                                ret = exec.execute(rCmd.getCommand(), sub.createSubProgress(0.5));
                            } catch (RException e) {
                                m_listeners.stream().forEach((l) -> l.onCommandExecutionError(e));
                            }
                            try {
                                exec.finishOutputCapturing(sub.createSubProgress(0.2));
                            } catch (RException e) {
                                m_listeners.stream().forEach((l) -> l.onCommandExecutionError(
                                        new RException("Could not capture output of command.", e)));
                            }

                            try {
                                exec.cleanup(sub.createSubProgress(0.1));
                            } catch (RException e) {
                                m_listeners.stream().forEach((l) -> l.onCommandExecutionError(
                                        new RException("Could not cleanup after command execution.", e)));
                            }

                            // complete Future to notify all threads waiting on
                            // it
                            rCmd.complete(ret);

                            m_listeners.stream().forEach(
                                    (l) -> l.onCommandExecutionEnd(rCmd, exec.getStdOut(), exec.getStdErr()));
                        } else {
                            // simple execution without error checks or output
                            // capturing for user-invisible commands issued for
                            // dialog functionality
                            try {
                                ret = m_controller.monitoredEval(rCmd.getCommand(), sub.createSubProgress(0.9));
                            } catch (RException e) {
                                m_listeners.stream().forEach((l) -> l.onCommandExecutionError(
                                        new RException("Could not execute internal command.", e)));
                            }

                            // complete Future to notify all threads waiting on
                            // it
                            rCmd.complete(ret);

                            m_listeners.stream().forEach((l) -> l.onCommandExecutionEnd(rCmd, "", ""));
                        }

                        sub.setProgress(1.0);
                    }
                    progress.setProgress(1.0, "Done!");
                    progress = null;
                }
            } catch (InterruptedException | CanceledExecutionException e) {
                try {
                    if (progress != null && progress.getProgressMonitor().getProgress() != 1.0) {
                        m_controller.terminateAndRelaunch();
                    }
                } catch (final Exception e1) {
                    // Could not terminate Rserve properly. Politely ask user to
                    // do it manually. Should basically never happen.
                    LOGGER.error(
                            "Could not properly terminate Rserve process. It may still be running in the background. Please try to terminate it manually.");
                }

                if (progress != null) {
                    progress.setProgress(1.0);
                }

                m_listeners.stream().forEach(RCommandExecutionListener::onCommandExecutionCanceled);
            } catch (IllegalAccessError err) {
                err.printStackTrace();
            } finally {
                if (m_context != null) {
                    NodeContext.removeLastContext();
                }
            }
        }
    }

    /**
     * Start this queues execution thread (Thread which executes the queues {@link RCommand}s).
     *
     * @param controller Controller to use for execution of R code
     * @param factory    Factory creating {@link ExecutionMonitor}s
     * @see #startExecutionThread(RController, ExecutionMonitorFactory)
     * @see #stopExecutionThread()
     * @see #isExecutionThreadRunning()
     */
    public void startExecutionThread() {
        startExecutionThread(ExecutionMonitor::new, false);
    }

    /**
     * @return <code>true</code> if the execution thread is currently running.
     * @see #startExecutionThread()
     * @see #startExecutionThread(RController, ExecutionMonitorFactory)
     * @see #stopExecutionThread()
     */
    public boolean isExecutionThreadRunning() {
        return m_thread != null && m_thread.isAlive();
    }

    /**
     * Start this queues execution thread (Thread which executes the queues {@link RCommand}s).
     *
     * @param controller Controller to use for execution of R code
     * @param factory    Factory creating {@link ExecutionMonitor}s
     * @see #startExecutionThread(RController)
     * @see #stopExecutionThread()
     * @see #isExecutionThreadRunning()
     */
    public void startExecutionThread(final ExecutionMonitorFactory factory,
                                     final boolean withNodeContext) {
        if (m_thread != null && m_thread.isAlive()) {
            throw new IllegalStateException("Can only launch one R execution thread on a RCommandQueue.");
        }

        m_thread = new RCommandConsumer(factory, withNodeContext);
        m_thread.start();
    }

    /**
     * Stop the queues execution thread. Does nothing if the queue is already stopped.
     *
     * @see #startExecutionThread(RController)
     * @see #startExecutionThread(RController, ExecutionMonitorFactory)
     * @see #isExecutionThreadRunning()
     */
    public void stopExecutionThread() {
        if (m_thread != null && m_thread.isAlive()) {
            m_thread.interrupt();
        }
    }
}
