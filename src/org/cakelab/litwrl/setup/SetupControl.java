package org.cakelab.litwrl.setup;

import org.cakelab.litwrl.repository.LitWRLRepository;
import org.cakelab.litwrl.setup.litwr.LifeInTheWoods;
import org.cakelab.omcl.config.GameTypes;
import org.cakelab.omcl.repository.PackageDescriptor;
import org.cakelab.omcl.setup.SetupService;
import org.cakelab.omcl.setup.SetupStatus;
import org.cakelab.omcl.taskman.TaskManager;
import org.cakelab.omcl.utils.log.Log;


public class SetupControl {

	private LitWRSetupParams setupParams;
	private LitWRLRepository repository;
	private TaskManager taskman;
	private SetupService service;

	public SetupControl(LitWRSetupParams setupParams, LitWRLRepository repository, TaskManager taskman) {
		this.setupParams = setupParams;
		this.repository = repository;
		this.taskman = taskman;
	}

	
	public void scheduleSetupTasks() throws Throwable {

		if (!setupParams.type.equals(GameTypes.CLIENT)) {
			Log.error("game type mismatch. This launcher cannot setup game type "+ setupParams.type);
			return;
		}
		
		
		SetupService litwr = getSetupService(setupParams.version);
		if (litwr.hasUpgrade()) {
			// force downloads
			litwr.scheduleDownloads(taskman, true);
			
			String currentVersion = litwr.getInstalledVersion();
			SetupService formerLitWR = getSetupService(currentVersion);
			
			litwr.scheduleUpgrades(taskman, formerLitWR);
		} else {
			litwr.scheduleDownloads(taskman, false);
			litwr.scheduleInstalls(taskman, false);
		}
		
	}

	private SetupService getSetupService(String version) throws Throwable {
		if (service == null) {
			PackageDescriptor descriptor = repository.fetchLitWRDependencies(setupParams.type, setupParams.variant, version);
			service = LifeInTheWoods.getSetupService(descriptor, setupParams, repository);
			service.init();
		}
		return service;
	}

	
	public SetupStatus getSetupStatus() throws Throwable {
		SetupService service = getSetupService(setupParams.version);
		SetupStatus result = new SetupStatus(setupParams, service.isInstalled(), service.hasUpgrade());
		return result;
	}


	public boolean needsDownloads() throws Throwable {
		SetupService service = getSetupService(setupParams.version);
		return ((service.hasUpgrade() || !service.isInstalled()) && !service.isDownloaded());
	}


	public boolean hasUpgrade() throws Throwable {
		SetupService service = getSetupService(setupParams.version);
		return service.hasUpgrade();
	}


}
