package org.cakelab.litwrl.setup.multiplayer.tasks;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.cakelab.litwrl.setup.multiplayer.ServerInfo;
import org.cakelab.omcl.setup.minecraft.ServersDat;
import org.cakelab.omcl.taskman.RunnableTask;

public class AddServers extends RunnableTask {

	private String gamedir;
	private ServerInfo[] serverList;

	public AddServers(String userInfo, File gamedir, List<ServerInfo> serverList) {
		super("multiplayer server list update", userInfo);
		this.gamedir = gamedir.getAbsolutePath();
		
		this.serverList = serverList.toArray(new ServerInfo[0]);
	}

	@Override
	public void run() {
		try {
			File dir = new File(gamedir);
			if (!dir.exists()) dir.mkdirs();
			ServersDat serversDat = ServersDat.loadFromGamedir(dir);

			for (ServerInfo server : serverList) {
				if (!serversDat.containsServer(server.getIp())) {
					serversDat.addServer(server.getIp(), server.getName());
				}
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
