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

import org.cakelab.litwrl.config.Config;

@SuppressWarnings("serial")
public class VersionSelectorField extends JPanel implements UIConfigField, ActionListener {
	private SequentialGroup horizontalGroup;
	private ParallelGroup verticalGroup;
	private VersionSelector version;
	private JRadioButton keepButton;
	private String latest;
	private String installed;
	private boolean processActions;
	
	private UIConfigFieldService updateService;
	
	
	public VersionSelectorField(ConfigPaneUIElements configPaneUIElements) {
		updateService = new UIConfigFieldService(this);
		addConfigUpdateListener(configPaneUIElements);
		setOpaque(true);
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

	public void init(Config config, String[] versions) {
		processActions = false;
		keepButton.setSelected(false);
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
			} else {
				if (installed != null) version.setVersion(latest);
			}
			
			sendUpdateNotification();
		} else if (e.getSource() == version) {
			String v = version.getVersion();
			if (isKeepVersion()) {
				
			} else if (!v.equals(latest)) {
				processActions = false;
				keepButton.setSelected(true);
				processActions = true;
			}
			
			sendUpdateNotification();
		}
	}

	private void sendUpdateNotification() {
		if (!processActions) return;
		updateService.forwardConfigUpdate();
	}

	public void setLatestVersion(String latest) {
		this.latest = latest;
	}

	public void setInstalledVersion(String installed) {
		this.installed = installed;
		int i;
		for (i = 0; i < version.getItemCount(); i++) {
			if (version.getItemAt(i).equals(installed)) break;
		}
		if (i == version.getItemCount()) {
			version.addItem(installed);
		}
	}
	
	public void setKeepVersion(boolean keep) {
		keepButton.setSelected(keep);
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
		setConfigurable(keepButton.isEnabled());
		processActions = true;
	}

	public boolean isVersionUpgrade() {
		return !version.getVersion().equals(installed) ;
	}

	@Override
	public void setConfigurable(boolean configurable) {
		keepButton.setEnabled(configurable);
		version.setEnabled(installed == null && configurable);
	}

	@Override
	public boolean isConfigurable() {
		return keepButton.isEnabled();
	}

	@Override
	public void addConfigUpdateListener(ConfigUpdateListener listener) {
		updateService.addConfigUpdateListener(listener);
	}

	@Override
	public void removeConfigUpdateListener(ConfigUpdateListener listener) {
		updateService.removeConfigUpdateListener(listener);
	}

}
