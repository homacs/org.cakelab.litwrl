package org.cakelab.litwrl.setup.litwr.tasks;

import java.io.File;
import java.io.IOException;

import org.cakelab.omcl.config.GameConfig;
import org.cakelab.omcl.setup.minecraft.Options;
import org.cakelab.omcl.taskman.RunnableTask;
import org.cakelab.omcl.utils.log.Log;

public class SetOptions extends RunnableTask {

	private GameConfig gameConfig;
	private String gamedir;

	public SetOptions(String userInfo, String gamedir, GameConfig gameConfig) {
		super("init game directory", userInfo);
		this.gamedir = gamedir;
		this.gameConfig = gameConfig;
	}

	@Override
	public void run() {
		try {
			File dir = new File(gamedir);
			if (!dir.exists()) dir.mkdirs();
			Options options = Options.loadFromGamedir(dir);
			options.setFullscreen(gameConfig.getFullscreen());
			options.setGuiScale(Options.GUI_SCALE_NORMAL);
			// this is an optifine option and it will be overridden by optifine
			// but we do it anyway since optifine could in future decided to consider
			// presets.
			options.setZoomKey(Options.KEY_Z);
			options.save();
		} catch (IOException e) {
			// nevermind, not so important
			Log.warn("couldn't set default options", e);
			return;
		}
	}

	@Override
	public String getDetailedErrorMessage() {
		return super.getDefaultErrorMessage();
	}

}
