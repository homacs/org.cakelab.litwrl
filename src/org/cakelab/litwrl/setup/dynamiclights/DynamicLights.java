package org.cakelab.litwrl.setup.dynamiclights;

import java.io.File;

import org.cakelab.litwrl.Launcher;
import org.cakelab.litwrl.setup.OptionalModSetupServiceBase;
import org.cakelab.litwrl.setup.litwr.tasks.LitwrCfgAddOptional;
import org.cakelab.omcl.repository.PackageDescriptor;
import org.cakelab.omcl.repository.Repository;
import org.cakelab.omcl.setup.SetupParameters;
import org.cakelab.omcl.setup.SetupService;
import org.cakelab.omcl.setup.minecraft.MinecraftClient;
import org.cakelab.omcl.setup.tasks.Copy;
import org.cakelab.omcl.setup.tasks.Delete;
import org.cakelab.omcl.setup.tasks.Unzip;
import org.cakelab.omcl.taskman.TaskManager;

public class DynamicLights extends OptionalModSetupServiceBase {

	private String jarfile;

	protected DynamicLights(SetupParameters setupParams, PackageDescriptor descriptor,
			Repository repository) {
		super(setupParams, descriptor, repository);
	}

	public static SetupService getSetupService(SetupParameters setupParams,
			PackageDescriptor pd, Repository repository) {
		return new DynamicLights(setupParams, pd, repository);
	}

	@Override
	public void init() throws Throwable {
		super.init();
		jarfile = descriptor.filename.replace(".zip", ".jar");
		modfile = new File(setupParams.gamedir,MinecraftClient.SUBDIR_MODS + File.separator + jarfile);
	}

	@Override
	public void scheduleInstalls(TaskManager taskman, boolean force) throws Throwable {
		if ((requestedInstall && !isBaseInstalled()) || force) {
			File tmpdir = new File(Launcher.INSTANCE.getTempDir(), descriptor.filename);
			taskman.addSingleTask(new Unzip("installing mod", getPackageRepositoryFile().getPath(), tmpdir.getPath()));
			taskman.addSingleTask(new Copy("installing mod", new File(tmpdir, "mods/" + jarfile).getPath(), modfile.getPath()));
			taskman.addSingleTask(new Delete("installing mod", tmpdir.getPath()));
			taskman.addSingleTask(new LitwrCfgAddOptional("installing mod", descriptor.getID(), setupParams.gamedir.getAbsolutePath()));
		}
	}

	public static String getID() {
		return "thirdparty/dynamiclights";
	}

	
	
}
