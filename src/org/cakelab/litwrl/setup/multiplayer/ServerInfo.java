package org.cakelab.litwrl.setup.multiplayer;

import org.cakelab.omcl.repository.PackageDescriptor;

public class ServerInfo {

	String name;
	String ip;
	

	public ServerInfo(PackageDescriptor pd) {
		this.name = pd.name;
		// TODO: needs patch mechanic instead of misusing downloadUrl entry
		this.ip = pd.getDownloadUrl().trim();
	}

	public String getIp() {
		return ip;
	}

	public String getName() {
		return name;
	}

}
