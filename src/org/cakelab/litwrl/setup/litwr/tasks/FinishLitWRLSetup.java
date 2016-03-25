package org.cakelab.litwrl.setup.litwr.tasks;

import java.io.File;
import java.io.IOException;

import org.cakelab.json.codec.JSONCodecException;
import org.cakelab.litwrl.setup.litwr.LitWRLConfig;
import org.cakelab.litwrl.setup.shaders.Shaders;
import org.cakelab.omcl.taskman.RunnableTask;

public class FinishLitWRLSetup extends RunnableTask {

	private LitWRLConfig litWRLConfig;
	private String configFile;

	public FinishLitWRLSetup(LitWRLConfig litWRLConfig, File configFile) {
		super("creation of LitWRL config at '" + configFile.getPath() + "'", "installing modpack");
		this.litWRLConfig = litWRLConfig;
		this.configFile = configFile.getAbsolutePath();
	}

	@Override
	public void run() {
		try {
			File cfgFile = new File(configFile);
			Shaders.setCapRenderDistance(cfgFile.getParentFile(), 16);
			this.litWRLConfig.save(cfgFile);
		} catch (IOException | JSONCodecException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getDetailedErrorMessage() {
		return getDefaultErrorMessage();
	}

}
