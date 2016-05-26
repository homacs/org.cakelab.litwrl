package org.cakelab.litwrl.gui.tabs.config;

import javax.swing.JComboBox;

import org.cakelab.omcl.utils.log.Log;




@SuppressWarnings("serial")
public class VersionSelector extends JComboBox<String> {
	public VersionSelector() {
		setEditable(false);
	}

	public void init(String[] versions) {
		removeAllItems();
		for (String v : versions) {
			addItem(v);
		}
	}

	public void setVersion(String version) {
		setSelectedItem(version);
	}

	public String getVersion() {
		return (String) getSelectedItem();
	}

	@Override
	public void setSelectedItem(Object version) {
		String v = getVersion();
		boolean different = (v != version) && (v != null && !v.equals(version)) || (version != null && !version.equals(v));
		if (different) {
			super.setSelectedItem(version);
			Log.info(v + " != " + version);
		}
	}

}
