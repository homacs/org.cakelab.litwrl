package org.cakelab.litwrl.gui.tabs.config;

import java.util.ArrayList;

public class UIConfigFieldService {
	private ArrayList<ConfigUpdateListener> listeners = new ArrayList<ConfigUpdateListener>();
	private UIConfigField owner;

	public UIConfigFieldService(UIConfigField owner) {
		this.owner = owner;
	}
	
	void forwardConfigUpdate() {
		for (ConfigUpdateListener l : listeners) {
			l.updatedUIConfigField(owner);
		}
	}
	
	void addConfigUpdateListener(ConfigUpdateListener listener) {
		listeners.add(listener);
	}
	
	void removeConfigUpdateListener(ConfigUpdateListener listener) {
		listeners.remove(listener);
	}
	
}
