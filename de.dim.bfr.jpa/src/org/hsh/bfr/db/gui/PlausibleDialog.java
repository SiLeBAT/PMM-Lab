/*
 * Created by JFormDesigner on Wed Dec 14 16:43:06 CET 2011
 */

package org.hsh.bfr.db.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

/**
 * @author Armin Weiser
 */
public class PlausibleDialog extends JDialog {
	public boolean okPressed = false;
	public PlausibleDialog(Frame owner) {
		super(owner);
		okPressed = false;
		initComponents();
	}

	public PlausibleDialog(Dialog owner) {
		super(owner);
		okPressed = false;
		initComponents();
	}
	
	private void okButtonActionPerformed(ActionEvent e) {
		okPressed = true;
		dispose();
	}

	private void cancelButtonActionPerformed(ActionEvent e) {
		okPressed = false;
		dispose();
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		dialogPane = new JPanel();
		contentPanel = new JPanel();
		radioButton1 = new JRadioButton();
		passwordField1 = new JPasswordField();
		radioButton2 = new JRadioButton();
		radioButton3 = new JRadioButton();
		textField1 = new JTextField();
		radioButton4 = new JRadioButton();
		checkBox1 = new JCheckBox();
		buttonBar = new JPanel();
		okButton = new JButton();
		cancelButton = new JButton();

		//======== this ========
		setTitle("Welche Datens\u00e4tze sollen einer Plausibilit\u00e4tspr\u00fcfung unterzogen werden?");
		setModal(true);
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		//======== dialogPane ========
		{
			dialogPane.setBorder(Borders.DIALOG_BORDER);
			dialogPane.setLayout(new BorderLayout());

			//======== contentPanel ========
			{
				contentPanel.setLayout(new FormLayout(
					"default, $lcgap, default:grow",
					"4*(default, $lgap), default"));

				//---- radioButton1 ----
				radioButton1.setText("Alle");
				radioButton1.setSelected(true);
				contentPanel.add(radioButton1, CC.xy(1, 1));
				contentPanel.add(passwordField1, CC.xy(3, 1));

				//---- radioButton2 ----
				radioButton2.setText("nur sichtbare Tabelle");
				contentPanel.add(radioButton2, CC.xy(1, 3));

				//---- radioButton3 ----
				radioButton3.setText("nur folgende IDs der sichtbaren Tabelle:");
				contentPanel.add(radioButton3, CC.xy(1, 5));

				//---- textField1 ----
				textField1.setToolTipText("z.B. 23-28");
				contentPanel.add(textField1, CC.xy(3, 5));

				//---- radioButton4 ----
				radioButton4.setText("nur selektierer Eintrag in der sichtbaren Tabelle");
				contentPanel.add(radioButton4, CC.xy(1, 7));

				//---- checkBox1 ----
				checkBox1.setText("nur Datens\u00e4tze des angemeldeten Benutzers anzeigen");
				checkBox1.setSelected(true);
				contentPanel.add(checkBox1, CC.xywh(1, 9, 3, 1));
			}
			dialogPane.add(contentPanel, BorderLayout.CENTER);

			//======== buttonBar ========
			{
				buttonBar.setBorder(Borders.BUTTON_BAR_GAP_BORDER);
				buttonBar.setLayout(new FormLayout(
					"$glue, $button, $rgap, $button",
					"pref"));

				//---- okButton ----
				okButton.setText("OK");
				okButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						okButtonActionPerformed(e);
					}
				});
				buttonBar.add(okButton, CC.xy(2, 1));

				//---- cancelButton ----
				cancelButton.setText("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						cancelButtonActionPerformed(e);
					}
				});
				buttonBar.add(cancelButton, CC.xy(4, 1));
			}
			dialogPane.add(buttonBar, BorderLayout.SOUTH);
		}
		contentPane.add(dialogPane, BorderLayout.CENTER);
		setSize(465, 210);
		setLocationRelativeTo(getOwner());

		//---- buttonGroup1 ----
		ButtonGroup buttonGroup1 = new ButtonGroup();
		buttonGroup1.add(radioButton1);
		buttonGroup1.add(radioButton2);
		buttonGroup1.add(radioButton3);
		buttonGroup1.add(radioButton4);
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JPanel dialogPane;
	private JPanel contentPanel;
	public JRadioButton radioButton1;
	private JPasswordField passwordField1;
	public JRadioButton radioButton2;
	public JRadioButton radioButton3;
	public JTextField textField1;
	public JRadioButton radioButton4;
	public JCheckBox checkBox1;
	private JPanel buttonBar;
	private JButton okButton;
	private JButton cancelButton;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
