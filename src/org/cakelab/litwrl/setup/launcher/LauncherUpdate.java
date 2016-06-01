package org.cakelab.litwrl.setup.launcher;

import java.io.File;

import org.cakelab.litwrl.Launcher;
import org.cakelab.litwrl.repository.LitWRLRepository;
import org.cakelab.litwrl.setup.launcher.tasks.FinishLauncherUpdate;
import org.cakelab.litwrl.setup.launcher.tasks.RestartLauncher;
import org.cakelab.omcl.repository.PackageDescriptor;
import org.cakelab.omcl.setup.SetupService;
import org.cakelab.omcl.taskman.TaskManager;

public class LauncherUpdate extends SetupService {

	private Launcher launcher;



	public LauncherUpdate(Launcher launcher, PackageDescriptor pd, LitWRLRepository repository) {
		super(null, pd, repository);
		this.launcher = launcher;
	}

	@Override
	public void init() throws Throwable {
	}


	@Override
	public boolean isBaseInstalled() {
		// we would run the new version if it was installed.
		// So, the only time this setup service exists, is when it is not installed.
		return false;
	}

	@Override
	public boolean hasUpgrade() {
		// same here .. we would not call the setup service for the launcher unless there
		// was no new version available.
		
		// always true
		return true;
	}
	
	@Override
	public boolean hasModifications() {
		// launcher is a standalone package with no optional addons
		return true;
	}

	@Override
	public void scheduleDownloads(TaskManager taskman, boolean forced) throws Throwable {
		if (forced || !isDownloaded()) schedulePackageDownload(taskman, "updating launcher");
	}



	@Override
	public void scheduleInstalls(TaskManager taskman, boolean force) throws Throwable {
		// just restart
		File targetDir = repository.getLocalLocation(descriptor);
		File jarFile = new File(targetDir, descriptor.filename);
		String[] javaArgs = new String[0];
		File descriptorFile = new File(targetDir, PackageDescriptor.FILENAME);
		
		File versionsFile = repository.getLocalVersionsFile(descriptor);
		
		taskman.addSingleTask(new FinishLauncherUpdate("saving new descriptor", "launcher update", descriptor, descriptorFile, versionsFile));
		taskman.addSingleTask(new RestartLauncher(javaArgs, jarFile, launcher.getConfigDir()));
	}



	@Override
	public boolean isDownloaded() {
		return isLocalPackageAvailable();
	}



	@Override
	public void scheduleUpgrades(TaskManager taskman, SetupService formerVersionSetup) throws Throwable {
		// for the time being, same tasks as in installation
		// TODO: consider cleanups
		scheduleInstalls(taskman, false);
	}

	@Override
	public void scheduleRemove(TaskManager taskman) {
		// TODO support cleanup
		
	}

	@Override
	public void scheduleModifications(TaskManager taskman, boolean force) {
		// launcher has no optional addons
		
	}




}
