package org.cakelab.litwrl.setup.launcher.tasks;

import java.io.File;
import java.util.ArrayList;

import org.cakelab.litwrl.Launcher;
import org.cakelab.omcl.taskman.RunnableTask;
import org.cakelab.omcl.utils.OS;

public class RestartLauncher extends RunnableTask {


	private String[] javaArgs;
	private String jarFile;
	private String workDir;

	public RestartLauncher(String[] javaArgs, File jarFile, File workDir) {
		super("execution of jar '" + jarFile + "'", "restarting launcher");
		this.javaArgs = javaArgs;
		this.jarFile = jarFile.getAbsolutePath();
		this.workDir = workDir.getAbsolutePath();
	}

	@Override
	public void run() {
		try {
			File javaCmd = OS.getJavaExecutable();
			
			
			ProcessBuilder pb = new ProcessBuilder();
			pb.inheritIO();
			ArrayList<String> commandline = new ArrayList<String>();
			commandline.add(javaCmd.toString());
			commandline.add("-jar");
			commandline.add(this.jarFile);
			for (String arg : javaArgs) {
				commandline.add(arg);
			}
			pb.directory(new File(this.workDir));
			pb.command(commandline);
			
			// mark this task as finished before we restart
			// so the next instance will not try to do another restart.
			finished();
			Launcher.INSTANCE.prepareRestart();
			pb.start();
			Launcher.INSTANCE.exit(0);
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
		

	}

	@Override
	public String getDetailedErrorMessage() {
		return getDefaultErrorMessage();
	}

}
