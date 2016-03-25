package org.cakelab.litwrl.setup.launcher.tasks;

import java.io.File;
import java.io.IOException;

import org.cakelab.json.codec.JSONCodecException;
import org.cakelab.omcl.repository.PackageDescriptor;
import org.cakelab.omcl.repository.Versions;
import org.cakelab.omcl.taskman.RunnableTask;
import org.cakelab.omcl.utils.log.Log;

public class FinishLauncherUpdate extends RunnableTask {

	private File file;
	private PackageDescriptor descriptor;
	private File versionsFile;

	public FinishLauncherUpdate(String logInfo, String userInfo, PackageDescriptor descriptor, File file, File versionsFile) {
		super(logInfo, userInfo);
		this.descriptor = descriptor;
		this.file = file;
		this.versionsFile = versionsFile;
	}

	@Override
	public void run() {
		try {
			descriptor.save(file);
			Versions versions;
			if (!versionsFile.exists()) {
				versions = new Versions();
			} else {
				try {
					versions = Versions.load(versionsFile);
				} catch (Throwable e) {
					Log.warn("Failed to read local versions file: " + versionsFile.getAbsolutePath());
					versions = new Versions();
				}
			}
			int latest = versions.addAvailable(descriptor.location);
			versions.setLatest(latest);
			versions.save(versionsFile);
		} catch (IOException | JSONCodecException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getDetailedErrorMessage() {
		return getDefaultErrorMessage();
	}

}
