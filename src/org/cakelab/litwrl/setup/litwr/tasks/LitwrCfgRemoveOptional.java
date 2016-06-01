package org.cakelab.litwrl.setup.litwr.tasks;

import java.io.File;
import java.io.IOException;

import org.cakelab.json.codec.JSONCodecException;
import org.cakelab.litwrl.setup.litwr.LitWRLConfig;
import org.cakelab.omcl.taskman.RunnableTask;

public class LitwrCfgRemoveOptional extends RunnableTask {

	private String gamedir;
	private String id;

	public LitwrCfgRemoveOptional(String userInfo, String id, String gamedir) {
		super("modifying litwrl.cfg: marking optional mod '" + id + "' as not installed", userInfo + "'" + id + "'");
		this.id = id;
		this.gamedir = gamedir;
	}

	@Override
	public void run() {
		LitWRLConfig cfg;
		try {
			cfg = LitWRLConfig.loadFromGameDir(new File(gamedir));
			if (cfg != null) {
				cfg.setOptionalAddonInstalled(id, false);
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
