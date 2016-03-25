package org.cakelab.litwrl;

import org.cakelab.omcl.ContextClassLoader;



public class Boot {

	public static void main(String[] args) throws Throwable
	{
		try {
			/*
			 * Here we replace the system class loader with a
			 * context class loader to support plugins.
			 */
			ContextClassLoader.bootstrap("org.cakelab.litwrl.Main", new String[]{"org.cakelab.litwrl.plugins"}, args);
		} catch (Throwable e) {
			System.err.println("launcher bootstrap failed.");
			e.printStackTrace(System.err);
			System.exit(1);
		}
	}

}
