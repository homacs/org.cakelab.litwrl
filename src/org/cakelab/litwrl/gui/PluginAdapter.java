package org.cakelab.litwrl.gui;

import org.cakelab.omcl.gui.GUI;
import org.cakelab.omcl.plugins.interfaces.ServicesListener;
import org.cakelab.omcl.utils.log.Log;

public abstract class PluginAdapter extends GUI implements ServicesListener {

	
	protected static PluginAdapter INSTANCE = null;

	
	public PluginAdapter() {
	}


	public static PluginAdapter getInstance() {
		return INSTANCE;
	}

	
	
	@Override
	public void fatal(String msg, Throwable e) {
		if (e != null) Log.fatal(msg, e);
		else Log.fatal(msg);
	}

	@Override
	public void error(String msg, Throwable e) {
		if (e != null) Log.error(msg, e);
		else Log.error(msg);
	}

	@Override
	public void warn(String msg, Throwable e) {
		if (e != null) Log.warn(msg, e);
		else Log.warn(msg);
	}

	@Override
	public void info(String msg, Throwable e) {
		Log.info(msg);
	}

}
