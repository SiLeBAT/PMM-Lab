package de.bund.bfr.knime.pmm.common.chart;

import java.awt.BorderLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

public class ChartAllPanel extends JPanel implements ComponentListener {

	private static final long serialVersionUID = 1L;

	private JSplitPane verticalSplitPane;
	private JSplitPane horizontalSplitPane;

	private ChartSelectionPanel selectionPanel;
	private ChartConfigPanel configPanel;
	private ChartSamplePanel samplePanel;

	private boolean verticalPaneAdjusted;
	private boolean horizontalPaneAdjusted;

	public ChartAllPanel(ChartCreator chartCreator,
			ChartSelectionPanel selectionPanel, ChartConfigPanel configPanel) {
		verticalPaneAdjusted = false;
		horizontalPaneAdjusted = false;

		this.selectionPanel = selectionPanel;
		this.configPanel = configPanel;
		JPanel upperPanel = new JPanel();

		upperPanel.setLayout(new BorderLayout());
		upperPanel.add(chartCreator, BorderLayout.CENTER);
		upperPanel.add(new JScrollPane(configPanel,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.EAST);

		verticalSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				upperPanel, selectionPanel);
		verticalSplitPane.addComponentListener(this);

		setLayout(new BorderLayout());
		add(verticalSplitPane, BorderLayout.CENTER);
	}

	public ChartAllPanel(ChartCreator chartCreator,
			ChartSelectionPanel selectionPanel, ChartConfigPanel configPanel,
			ChartSamplePanel samplePanel) {
		verticalPaneAdjusted = false;
		horizontalPaneAdjusted = false;

		this.selectionPanel = selectionPanel;
		this.configPanel = configPanel;
		this.samplePanel = samplePanel;
		JPanel upperPanel = new JPanel();

		upperPanel.setLayout(new BorderLayout());
		upperPanel.add(chartCreator, BorderLayout.CENTER);
		upperPanel.add(new JScrollPane(configPanel,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.EAST);

		horizontalSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				selectionPanel, samplePanel);
		horizontalSplitPane.addComponentListener(this);

		verticalSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				upperPanel, horizontalSplitPane);
		verticalSplitPane.addComponentListener(this);

		setLayout(new BorderLayout());
		add(verticalSplitPane, BorderLayout.CENTER);
	}

	public ChartSelectionPanel getSelectionPanel() {
		return selectionPanel;
	}

	public ChartConfigPanel getConfigPanel() {
		return configPanel;
	}

	public ChartSamplePanel getSamplePanel() {
		return samplePanel;
	}

	public int getVerticalDividerLocation() {
		return verticalSplitPane.getDividerLocation();
	}

	public void setVerticalDividerLocation(int location) {
		verticalSplitPane.setDividerLocation(location);
	}

	public int getHorizontalDividerLocation() {
		return horizontalSplitPane.getDividerLocation();
	}

	public void setHorizontalDividerLocation(int location) {
		horizontalSplitPane.setDividerLocation(location);
	}

	@Override
	public void componentHidden(ComponentEvent e) {
	}

	@Override
	public void componentMoved(ComponentEvent e) {
	}

	@Override
	public void componentResized(ComponentEvent e) {
		if (e.getComponent() == verticalSplitPane && !verticalPaneAdjusted
				&& verticalSplitPane.getWidth() > 0
				&& verticalSplitPane.getHeight() > 0) {
			verticalSplitPane.setDividerLocation(0.5);
			verticalPaneAdjusted = true;
		}

		if (e.getComponent() == horizontalSplitPane && !horizontalPaneAdjusted
				&& horizontalSplitPane.getWidth() > 0
				&& horizontalSplitPane.getHeight() > 0) {
			horizontalSplitPane.setDividerLocation(0.5);
			horizontalPaneAdjusted = true;
		}
	}

	@Override
	public void componentShown(ComponentEvent e) {
	}
}
