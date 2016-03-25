package org.cakelab.litwrl.setup.optifine;

import java.io.File;

import org.cakelab.omcl.repository.PackageDescriptor;
import org.cakelab.omcl.repository.Repository;
import org.cakelab.omcl.setup.ModSetupServiceBase;
import org.cakelab.omcl.setup.SetupParameters;
import org.cakelab.omcl.setup.SetupService;
import org.cakelab.omcl.setup.tasks.Delete;
import org.cakelab.omcl.taskman.TaskManager;


public class OptiFine extends ModSetupServiceBase {

	private static final String OFOPTIONS_FILENAME = "ofoptions.txt";
	private File optionsfile;



	protected OptiFine(SetupParameters setupParams, PackageDescriptor pd,
			Repository repository) {
		super(setupParams, pd, repository);
	}

	@Override
	public void init() throws Throwable {
		super.init();
		optionsfile = new File(setupParams.gamedir, OFOPTIONS_FILENAME);
	}

	
	@Override
	public void scheduleRemove(TaskManager taskman) throws Throwable {
		if (isInstalled()) {
			taskman.addSingleTask(new Delete("upgrading mod-pack", optionsfile.getAbsolutePath()));
		}
		super.scheduleRemove(taskman);
	}

	public static SetupService getSetupService(SetupParameters setupParams,
			PackageDescriptor pd, Repository repository) {
		return new OptiFine(setupParams, pd, repository);
	}

}
