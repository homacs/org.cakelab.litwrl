package org.cakelab.litwrl.setup.shadersmod.tasks;

import java.io.File;
import java.io.IOException;

import org.cakelab.litwrl.setup.shadersmod.OptionsShaders;
import org.cakelab.omcl.taskman.RunnableTask;

public class CreateShaderOptions extends RunnableTask {

	private String configFile;

	public CreateShaderOptions(File file) {
		this("installing shaders", file);
	}

	public CreateShaderOptions(String userInfo, File file) {
		super("creation of shader config at '" + file.getPath() + "'", userInfo);
		this.configFile = file.getAbsolutePath();
	}

	@Override
	public void run() {
		try {
			OptionsShaders options = OptionsShaders.createDefault();
			options.save(new File(configFile));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getDetailedErrorMessage() {
		return getDefaultErrorMessage();
	}

}
