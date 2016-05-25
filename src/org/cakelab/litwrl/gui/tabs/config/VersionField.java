package org.cakelab.litwrl.gui.tabs.config;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.cakelab.litwrl.config.Config;

@SuppressWarnings("serial")
public class VersionField extends JPanel implements ActionListener {
	private SequentialGroup horizontalGroup;
	private ParallelGroup verticalGroup;
	private JTextField version;
	private JRadioButton keepButton;
	private Config config;
	private ConfigPane configPane;
	public VersionField(ConfigPane configPane) {
		this.configPane = configPane;
		GroupLayout layout = new GroupLayout(this);
		layout.setAutoCreateGaps(true);
		
		horizontalGroup = layout.createSequentialGroup();
		verticalGroup = layout.createParallelGroup(Alignment.CENTER);
		
		setLayout(layout);

		
		version = new JTextField(1);
		version.setEditable(false);
		addRow(version);

		keepButton = new JRadioButton("Keep");
		keepButton.setEnabled(false);
		keepButton.addActionListener(this);
		addRow(keepButton);
		
		layout.setHorizontalGroup(horizontalGroup);
		layout.setVerticalGroup(verticalGroup);

	}
	
	private void addRow(JComponent component) {
		horizontalGroup.addComponent(component);
		verticalGroup.addComponent(component);
	}

	@Override
	public void setToolTipText(String text) {
		version.setToolTipText(text);
		keepButton.setToolTipText("<html>If this tick is set, the launcher<br/>will not update the mod-pack.</html>");
		super.setToolTipText(text);
	}

	public void setEnabled(boolean editable) {
		keepButton.setEnabled(editable);
	}

	public void init(Config config) {
		this.config = config;
		if (isKeepVersion()) {
			keepButton.setSelected(true);
		} else {
			keepButton.setSelected(false);
		}
	}

	public void setVersion(String latestVersion) {
		version.setText(latestVersion);
	}

	public boolean isKeepVersion() {
		String keepLitwrVer = config.getKeepLitWRVersion();
		return keepLitwrVer != null && keepLitwrVer.length() > 0;
	}

	public String getVersion() {
		return version.getText();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == keepButton) {
			if (keepButton.isSelected()) {
				config.setKeepLitWRVersion(version.getText());
			} else {
				config.setKeepLitWRVersion(null);
			}
			config.save();
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					configPane.updatedVersionField();
				}
				
			});
		}
	}


}
