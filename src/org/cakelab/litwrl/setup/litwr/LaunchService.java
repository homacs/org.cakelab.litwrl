package org.cakelab.litwrl.setup.litwr;

public interface LaunchService {
	public void launch() throws LaunchException;

	public void init() throws Throwable;
}
