package de.bund.bfr.knime.pmm.fskx.ui;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import org.fife.ui.rtextarea.RTextScrollPane;

import de.bund.bfr.knime.pmm.fskx.rsnippet.RSnippet;

/** JPanel with an R script */
public class ScriptPanel extends JPanel {

	private static final long serialVersionUID = -4493061426461816058L;
	private RSnippetTextArea textArea;

	public ScriptPanel(final String title, final String script, final boolean editable) {
		super(new BorderLayout());
		setName(title);

		textArea = new RSnippetTextArea(new RSnippet());
		textArea.setLineWrap(true);
		textArea.setText(script);
		textArea.setEditable(editable);
		add(new RTextScrollPane(textArea));
	}

	public RSnippetTextArea getTextArea() {
		return textArea;
	}
}