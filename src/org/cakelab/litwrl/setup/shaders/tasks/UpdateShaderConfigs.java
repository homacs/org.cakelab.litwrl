package org.cakelab.litwrl.setup.shaders.tasks;

import java.io.File;
import java.io.IOException;

import org.cakelab.litwrl.setup.shaders.Shaders;
import org.cakelab.omcl.taskman.RunnableTask;

public class UpdateShaderConfigs extends RunnableTask {

	private String configFile;
	private String shaderFilename;

	public UpdateShaderConfigs(String shaderFilename, File file) {
		this("installing shaders", shaderFilename, file);
	}

	public UpdateShaderConfigs(String userInfo, String shaderFilename, File file) {
		super("updating shader config at '" + file.getPath() + "' for shader '" + shaderFilename + "'", userInfo);
		this.shaderFilename = shaderFilename;
		this.configFile = file.getAbsolutePath();
	}

	@Override
	public void run() {
		try {
			
			File osFile = new File(configFile);
			if (!Shaders.isNonStandardShader(shaderFilename) && !osFile.exists()) {
				// ignore this case, because it happens only during removal
				return;
			}
			Shaders.setShaderOptions(shaderFilename, osFile.getParentFile());
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getDetailedErrorMessage() {
		return getDefaultErrorMessage();
	}

}
