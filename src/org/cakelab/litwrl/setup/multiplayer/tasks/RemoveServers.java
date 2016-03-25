package org.cakelab.litwrl.setup.multiplayer.tasks;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.cakelab.litwrl.setup.multiplayer.ServerInfo;
import org.cakelab.omcl.setup.minecraft.ServersDat;
import org.cakelab.omcl.taskman.RunnableTask;

public class RemoveServers extends RunnableTask {

	private String gamedir;
	private ServerInfo[] serverList;

	public RemoveServers(String userInfo, File gamedir, List<ServerInfo> serverList) {
		super("multiplayer server list update", userInfo);
		this.gamedir = gamedir.getAbsolutePath();
		
		this.serverList = serverList.toArray(new ServerInfo[0]);
	}

	@Override
	public void run() {
		try {
			ServersDat serversDat = ServersDat.loadFromGamedir(new File(gamedir));

			for (ServerInfo server : serverList) {
				serversDat.removeServer(server.getIp());
			}
			
			if (serversDat.isModified()) serversDat.save();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getDetailedErrorMessage() {
		return super.getDefaultErrorMessage();
	}

}
