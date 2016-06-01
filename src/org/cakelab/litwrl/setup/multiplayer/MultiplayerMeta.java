package org.cakelab.litwrl.setup.multiplayer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.cakelab.litwrl.setup.multiplayer.tasks.AddServers;
import org.cakelab.litwrl.setup.multiplayer.tasks.RemoveServers;
import org.cakelab.omcl.repository.PackageDescriptor;
import org.cakelab.omcl.repository.Repository;
import org.cakelab.omcl.setup.SetupParameters;
import org.cakelab.omcl.setup.SetupService;
import org.cakelab.omcl.setup.minecraft.ServersDat;
import org.cakelab.omcl.taskman.TaskManager;

public class MultiplayerMeta extends SetupService {

	List<ServerInfo> serverList = new ArrayList<ServerInfo>();
	
	
	protected MultiplayerMeta(SetupParameters setupParams,
			PackageDescriptor pd, Repository repository) {
		super(setupParams, pd, repository);
	}

	@Override
	public void init() throws Throwable {
		for (String location : descriptor.optional) {
			PackageDescriptor pd = repository.getLocalPackageDescriptorFromLocation(location);
			serverList.add(new ServerInfo(pd));
		}
	}

	@Override
	public boolean isDownloaded() {
		return true;
	}

	@Override
	public boolean isBaseInstalled() {
		try {
			ServersDat serversDat = ServersDat.loadFromGamedir(setupParams.gamedir);
			for (ServerInfo server : serverList) {
				if (!serversDat.containsServer(server.getIp())) {
					return false;
				}
			}
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	@Override
	public boolean hasUpgrade() {
		// upgrades are handled by the litwr setup service, not here
		return false;
	}

	@Override
	public void scheduleDownloads(TaskManager taskman, boolean forced) throws Throwable {
		// no files associated
	}

	@Override
	public void scheduleInstalls(TaskManager taskman, boolean force)
			throws Throwable {
		taskman.addSingleTask(new AddServers("install mod-pack", setupParams.gamedir, serverList));
	}

	@Override
	public void scheduleUpgrades(TaskManager taskman,
			SetupService formerVersionSetupService) throws Throwable {
		// upgrades are handled by the litwr setup service, not here
	}

	@Override
	public void scheduleRemove(TaskManager taskman) throws Throwable {
		taskman.addSingleTask(new RemoveServers("install mod-pack", setupParams.gamedir, serverList));
	}

	public static SetupService getSetupService(SetupParameters setupParams,
			PackageDescriptor pd, Repository repository) {
		return new MultiplayerMeta(setupParams, pd, repository);
	}

	@Override
	public boolean hasModifications() {
		// TODO not defined for multiplayer package
		return false;
	}

	@Override
	public void scheduleModifications(TaskManager taskman, boolean force) {
		// TODO not defined for multiplayer package
	}

}
