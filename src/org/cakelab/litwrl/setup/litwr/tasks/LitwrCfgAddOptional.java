package org.cakelab.litwrl.setup.litwr.tasks;

import java.io.File;
import java.io.IOException;

import org.cakelab.json.codec.JSONCodecException;
import org.cakelab.litwrl.setup.litwr.LitWRLConfig;
import org.cakelab.omcl.taskman.RunnableTask;

public class LitwrCfgAddOptional extends RunnableTask {

	private String gamedir;
	private String id;

	public LitwrCfgAddOptional(String userInfo, String id, String gamedir) {
		super("modifying litwrl.cfg: marking optional mod '" + id + "' as installed", userInfo + "'" + id + "'");
		this.id = id;
		this.gamedir = gamedir;
	}

	@Override
	public void run() {
		LitWRLConfig cfg;
		try {
			cfg = LitWRLConfig.loadFromGameDir(new File(gamedir));
			if (cfg != null) {
				cfg.setOptionalAddonInstalled(id, true);
				cfg.save();
			}
		} catch (IOException | JSONCodecException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getDetailedErrorMessage() {
		return getDefaultErrorMessage();
	}

}
