package org.cakelab.litwrl.setup;

import org.cakelab.litwrl.setup.litwr.LitWRLConfig;
import org.cakelab.litwrl.setup.litwr.tasks.LitwrCfgAddOptional;
import org.cakelab.litwrl.setup.litwr.tasks.LitwrCfgRemoveOptional;
import org.cakelab.omcl.repository.PackageDescriptor;
import org.cakelab.omcl.repository.Repository;
import org.cakelab.omcl.setup.ModSetupServiceBase;
import org.cakelab.omcl.setup.SetupParameters;
import org.cakelab.omcl.taskman.TaskManager;

public abstract class OptionalModSetupServiceBase extends ModSetupServiceBase {

	private boolean requestedInstall;
	private LitWRLConfig litwrlcfg;

	protected OptionalModSetupServiceBase(SetupParameters setupParams,
			PackageDescriptor pd, Repository repository) {
		super(setupParams, pd, repository);
		try {
			litwrlcfg = LitWRLConfig.loadFromGameDir(setupParams.gamedir);
		} catch (Throwable e) {
			litwrlcfg = null;
		}
		
	}

	
	@Override
	public void init() throws Throwable {
		super.init();
		requestedInstall = setupParams.containsOptionalAddon(descriptor.getID());
	}

	
	
	@Override
	public boolean hasModifications() {
		boolean hasModification = (isBaseInstalled() != requestedInstall);
		return hasModification;
	}

	@Override
	public void scheduleModifications(TaskManager taskman, boolean force) throws Throwable {
		if (requestedInstall) {
			scheduleDownloads(taskman, force);
			scheduleInstalls(taskman, force);
		} else {
			scheduleRemove(taskman);
		}
	}


	@Override
	public void scheduleInstalls(TaskManager taskman, boolean force)
			throws Throwable {
		super.scheduleInstalls(taskman, force);
		taskman.addSingleTask(new LitwrCfgAddOptional("installing mod", descriptor.getID(), setupParams.gamedir.getAbsolutePath()));
	}


	@Override
	public void scheduleRemove(TaskManager taskman) throws Throwable {
		super.scheduleRemove(taskman);
		taskman.addSingleTask(new LitwrCfgRemoveOptional("deleting mod", descriptor.getID(), setupParams.gamedir.getAbsolutePath()));
	}

	@Override
	public boolean isBaseInstalled() {
		return super.isBaseInstalled() && litwrlcfg != null && litwrlcfg.isOptionalAddonInstalled(descriptor.getID());
	}

	
}
