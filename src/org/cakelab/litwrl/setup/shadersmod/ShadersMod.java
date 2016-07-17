package org.cakelab.litwrl.setup.shadersmod;

import java.io.File;

import org.cakelab.litwrl.setup.OptionalModSetupServiceBase;
import org.cakelab.litwrl.setup.shadersmod.tasks.CreateShaderOptions;
import org.cakelab.omcl.repository.PackageDescriptor;
import org.cakelab.omcl.repository.Repository;
import org.cakelab.omcl.setup.SetupParameters;
import org.cakelab.omcl.setup.SetupService;
import org.cakelab.omcl.setup.tasks.Delete;
import org.cakelab.omcl.taskman.TaskManager;

public class ShadersMod extends OptionalModSetupServiceBase {


	private File optionsfile;

	protected ShadersMod(SetupParameters setupParams, PackageDescriptor pd,
			Repository repository) {
		super(setupParams, pd, repository);
	}
	
	@Override
	public void init() throws Throwable {
		optionsfile = new File(setupParams.gamedir + File.separator + OptionsShaders.FILENAME);
		super.init();
	}

	@Override
	public boolean isBaseInstalled() {
		return super.isBaseInstalled() && optionsfile.exists();
	}

	@Override
	public void scheduleInstalls(TaskManager taskman, boolean force) throws Throwable {
		if ((requestedInstall && !isBaseInstalled()) || force) {
			super.scheduleInstalls(taskman, force);
			taskman.addSingleTask(new CreateShaderOptions("installing shaders", optionsfile));
		}
	}

	@Override
	public void scheduleRemove(TaskManager taskman) throws Throwable {
		if (isBaseInstalled()) {
			taskman.addSingleTask(new Delete("upgrading mod-pack", optionsfile.getAbsolutePath()));
		}
		super.scheduleRemove(taskman);
	}

	public static SetupService getSetupService(SetupParameters setupParams,
			PackageDescriptor pd, Repository repository) {
		return new ShadersMod(setupParams, pd, repository);
	}

	public static String getID() {
		return "thirdparty/shadersmod";
	}

}
