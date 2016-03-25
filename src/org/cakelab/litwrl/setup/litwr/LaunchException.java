package org.cakelab.litwrl.setup.litwr;

public class LaunchException extends Exception {
	private static final long serialVersionUID = 1L;

	public LaunchException() {
		super();
	}

	public LaunchException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public LaunchException(String message, Throwable cause) {
		super(message, cause);
	}

	public LaunchException(String message) {
		super(message);
	}

	public LaunchException(Throwable cause) {
		super(cause);
	}

}
