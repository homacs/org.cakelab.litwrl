package org.cakelab.litwrl.gui.tabs.log;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.cakelab.litwrl.gui.utils.TextScrollPane;
import org.cakelab.omcl.utils.log.Log;
import org.cakelab.omcl.utils.log.LogListener;

public class LogPane extends TextScrollPane implements LogListener {
	private static final long serialVersionUID = 1L;

	public static LogPane create() {
		LogPane logpane = new LogPane();
		
		return logpane;
	}



	private LogPane() {
		super();
	}
	
	
	public void init() {
		Log.addLogListener(this);
	}


	@Override
	public void fatal(String msg, Throwable e) {
		appendLater("fatal", msg, e);
	}


	@Override
	public void fatal(String msg) {
		fatal(msg, null);
	}


	@Override
	public void error(String msg, Throwable e) {
		appendLater("error", msg, e);
	}


	@Override
	public void error(String msg) {
		error(msg, null);
	}


	@Override
	public void warn(String msg, Throwable e) {
		appendLater("warn", msg, e);
	}


	@Override
	public void warn(String msg) {
		warn(msg, null);
	}


	@Override
	public void info(String msg) {
		appendLater("info", msg, null);
	}


	private void appendLater(String channel, String msg, Throwable ex) {
		String timeStamp = new SimpleDateFormat().format(Calendar.getInstance().getTime());
		String line = timeStamp + " [" + channel + "] " + msg + getExceptionAsString(ex) + '\n';
		appendLater(line);
	}


	private String getExceptionAsString(Throwable ex) {
		if (ex != null) {
			return " " + ex.getMessage();
		}
		else 
		{
			return "";
		}
	}

}
