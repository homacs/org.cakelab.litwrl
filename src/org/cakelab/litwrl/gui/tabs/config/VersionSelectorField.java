package org.cakelab.litwrl.gui.tabs.config;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;

import org.cakelab.litwrl.config.Config;
import org.cakelab.omcl.utils.log.Log;

@SuppressWarnings("serial")
public class VersionSelectorField extends JPanel implements ActionListener {
	private SequentialGroup horizontalGroup;
	private ParallelGroup verticalGroup;
	private VersionSelector version;
	private JRadioButton keepButton;
	private Config config;
	private ConfigPane configPane;
	private String latest;
	private String installed;
	private boolean processActions;
	public VersionSelectorField(ConfigPane configPane) {
		setOpaque(true);
		this.configPane = configPane;
		GroupLayout layout = new GroupLayout(this);
		layout.setAutoCreateGaps(true);
		
		horizontalGroup = layout.createSequentialGroup();
		verticalGroup = layout.createParallelGroup(Alignment.CENTER);
		
		setLayout(layout);

		
		version = new VersionSelector();
		version.setEditable(false);
		addRow(version);
		version.addActionListener(this);

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

	public void setEnabled(boolean configurable) {
		keepButton.setEnabled(configurable);
		version.setEnabled(installed == null && configurable);
	}

	public void init(Config config, String[] versions) {
		processActions = false;
		this.config = config;
		String keepLitwrVer = config.getKeepLitWRVersion();
		keepButton.setSelected(keepLitwrVer != null && keepLitwrVer.length() > 0);
		version.init(versions);
		processActions = true;
	}

	public void setVersion(String version) {
		processActions = false;
		this.version.setVersion(version);
		processActions = true;
	}

	public boolean isKeepVersion() {
		return keepButton.isSelected();
	}

	public String getVersion() {
		return version.getVersion();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (!processActions) return;
		
		if (e.getSource() == keepButton) {
			if (isKeepVersion()) {
				if (installed != null) version.setVersion(installed);
				config.setKeepLitWRVersion(version.getVersion());
			} else {
				if (installed != null) version.setVersion(latest);
				config.setKeepLitWRVersion(null);
			}
			config.save();
			
			sendUpdateNotification();
		} else if (e.getSource() == version) {
			String v = version.getVersion();
			if (isKeepVersion()) config.setKeepLitWRVersion(v);
			else if (!v.equals(latest)) {
				processActions = false;
				keepButton.setSelected(true);
				processActions = true;
				config.setKeepLitWRVersion(v);
			}
			config.save();
			
			sendUpdateNotification();
		}
	}

	private void sendUpdateNotification() {
		if (!processActions) return;
		
		Log.warn("send update ..", new Exception("test"));
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				configPane.updatedVersionField();
			}
			
		});
	}

	public void setLatestVersion(String latest) {
		this.latest = latest;
	}

	public void setInstalledVersion(String installed) {
		this.installed = installed;
	}

	public void update() {
		processActions = false;
		if (this.isKeepVersion()) {
			if (installed != null) {
				version.setVersion(installed);
			}
		} else {
			version.setVersion(latest);
		}
		setEnabled(keepButton.isEnabled());
		processActions = true;
	}

	public boolean isVersionUpgrade() {
		return !version.getVersion().equals(installed) ;
	}

	

}
