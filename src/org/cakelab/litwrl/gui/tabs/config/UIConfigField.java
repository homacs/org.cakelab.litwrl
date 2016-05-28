package org.cakelab.litwrl.gui.tabs.config;

public interface UIConfigField {
	void setConfigurable(boolean configurable);
	boolean isConfigurable();
	void addConfigUpdateListener(ConfigUpdateListener listener);
	void removeConfigUpdateListener(ConfigUpdateListener listener);
}
