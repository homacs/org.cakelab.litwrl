package org.cakelab.litwrl.setup.shaders;

import java.io.File;
import java.io.IOException;

import org.cakelab.litwrl.setup.shaders.tasks.UpdateShaderConfigs;
import org.cakelab.litwrl.setup.shadersmod.OptionsShaders;
import org.cakelab.omcl.repository.PackageDescriptor;
import org.cakelab.omcl.repository.Repository;
import org.cakelab.omcl.setup.SetupParameters;
import org.cakelab.omcl.setup.SetupService;
import org.cakelab.omcl.setup.minecraft.MinecraftClient;
import org.cakelab.omcl.setup.tasks.Copy;
import org.cakelab.omcl.setup.tasks.Delete;
import org.cakelab.omcl.taskman.TaskManager;
import org.cakelab.omcl.utils.log.Log;

public class ShaderSetup extends SetupService {

	private File shaderFile;
	private File optionsfile;
	private File shaderBackupFile;

	protected ShaderSetup(SetupParameters setupParams, PackageDescriptor pd,
			Repository repository) {
		super(setupParams, pd, repository);
	}

	@Override
	public void init() throws Throwable {
		optionsfile = new File(setupParams.gamedir + File.separator + OptionsShaders.FILENAME);
		shaderFile = new File(setupParams.gamedir,MinecraftClient.SUBDIR_SHADERPACKS + File.separator + descriptor.filename);
		shaderBackupFile = new File(setupParams.gamedir, 
				MinecraftClient.SUBDIR_MODS + 
				File.separator + "resources" + 
				File.separator + "litwr" + 
				File.separator + "shaderpatch" + 
				File.separator + "backup" + 
				File.separator + descriptor.filename);
	}

	@Override
	public boolean isDownloaded() {
		return super.isLocalPackageAvailable();
	}

	@Override
	public boolean isBaseInstalled() {
		if (!shaderFile.exists()) {
			return false;
		}
		try {
			if (!OptionsShaders.existsIn(setupParams.gamedir)) {
				return false;
			} else {
				OptionsShaders options = OptionsShaders.loadFromGamedir(setupParams.gamedir);
				if (!options.getShaderPack().equals(shaderFile.getName())) {
					return false;
				}
			}
		} catch (IOException e) {
			Log.error("can't access optionsshaders.txt file", e);
			return false;
		}
		return true;
	}

	@Override
	public boolean hasUpgrade() {
		return false;
	}

	@Override
	public void scheduleDownloads(TaskManager taskman, boolean forced) throws Throwable {
		// TODO: To allow forcing a download, we need to determine if the shader is 
		// a user deployed shader
		if (!shaderFile.exists()) super.schedulePackageDownload(taskman);
	}

	@Override
	public void scheduleInstalls(TaskManager taskman, boolean force) throws Throwable {
		if (!isBaseInstalled() || force) {
			if (!shaderFile.exists() || force) {
				taskman.addSingleTask(new Copy("installing shader", getPackageRepositoryFile().getPath(), shaderFile.getPath()));
			}
			taskman.addSingleTask(new UpdateShaderConfigs(shaderFile.getName(), optionsfile));
		}
	}

	@Override
	public void scheduleUpgrades(TaskManager taskman, SetupService formerVersionSetup) throws Throwable {
		formerVersionSetup.scheduleRemove(taskman);
		scheduleInstalls(taskman, true);
	}

	@Override
	public void scheduleRemove(TaskManager taskman) {

		if (isBaseInstalled()) {
			taskman.addSingleTask(new UpdateShaderConfigs(Shaders.SHADER_NONE, optionsfile));
			if (!isLocalPackageAvailable()) {
				taskman.addSingleTask(new Copy("saving shader", shaderFile.getPath(), getPackageRepositoryFile().getPath()));
			}
			if (shaderFile.exists()) taskman.addSingleTask(new Delete("upgrading mod-pack", shaderFile.getAbsolutePath()));
			if (shaderBackupFile.exists()) taskman.addSingleTask(new Delete("upgrading mod-pack", shaderBackupFile.getAbsolutePath()));
		}
	}

	@Override
	public boolean hasModifications() {
		return !isBaseInstalled();
	}

	@Override
	public void scheduleModifications(TaskManager taskman, boolean force) throws Throwable {
		scheduleDownloads(taskman, force);
		scheduleInstalls(taskman, true);
	}

}
