package org.cakelab.litwrl.setup.litwr;

import java.io.File;
import java.io.IOException;

import org.cakelab.json.JSONException;
import org.cakelab.json.codec.JSONCodecException;
import org.cakelab.litwrl.repository.LitWRLRepository;
import org.cakelab.litwrl.setup.LitWRSetupParams;
import org.cakelab.litwrl.setup.dynamiclights.DynamicLights;
import org.cakelab.litwrl.setup.litwr.tasks.FinishLitWRLSetup;
import org.cakelab.litwrl.setup.litwr.tasks.SetOptions;
import org.cakelab.litwrl.setup.multiplayer.MultiplayerMeta;
import org.cakelab.litwrl.setup.optifine.OptiFine;
import org.cakelab.litwrl.setup.shaders.ShaderSetup;
import org.cakelab.litwrl.setup.shaders.Shaders;
import org.cakelab.litwrl.setup.shadersmod.ShadersMod;
import org.cakelab.omcl.gui.GUI;
import org.cakelab.omcl.plugins.PluginServices;
import org.cakelab.omcl.plugins.StubException;
import org.cakelab.omcl.plugins.minecraft.LauncherServicesStub;
import org.cakelab.omcl.repository.PackageDescriptor;
import org.cakelab.omcl.setup.SetupService;
import org.cakelab.omcl.setup.VersionStd;
import org.cakelab.omcl.setup.forge.Forge;
import org.cakelab.omcl.setup.minecraft.LauncherProfiles;
import org.cakelab.omcl.setup.minecraft.MinecraftBootstrap;
import org.cakelab.omcl.setup.minecraft.MinecraftClient;
import org.cakelab.omcl.setup.minecraft.tasks.RunMinecraftBootstrap;
import org.cakelab.omcl.setup.tasks.Delete;
import org.cakelab.omcl.setup.tasks.Unzip;
import org.cakelab.omcl.taskman.TaskManager;
import org.cakelab.omcl.utils.OS;
import org.cakelab.omcl.utils.log.Log;

public abstract class LifeInTheWoodsClient extends SetupService implements LaunchService {


	private static final String DEFAULT_BOOSTRAP_LOCATION = "thirdparty/minecraft/bootstrap/05";
	protected MinecraftBootstrap minecraftBootstrap;
	protected MinecraftClient minecraftClient;
	protected SetupService forge;
	protected File configDir;
	protected File configFile;
	protected File modsDir;
	private boolean hideLauncher = true;
	private ShaderSetup shader;
	private SetupService shadersMod;
	private SetupService optifine;
	private SetupService dynamicLights;
	private boolean shadersEnabled;
	private LitWRLConfig litwrlcfg;
	private SetupService multiplayer;
	private LitWRSetupParams litwrlParams;
	
	
	public LifeInTheWoodsClient(LitWRSetupParams params, PackageDescriptor descriptor, LitWRLRepository repository) {
		super(params, descriptor, repository);
		this.litwrlParams = params;

	}

	@Override
	public void init() throws Throwable {

		// TODO: implement actual global dependency management (requires scheduling overhaul)
		
		PackageDescriptor mcBootstrapDescriptor = repository.getLocalPackageDescriptorFromLocation(DEFAULT_BOOSTRAP_LOCATION);
		minecraftBootstrap = MinecraftBootstrap.getSetupService(setupParams, mcBootstrapDescriptor, repository);
		
		String dependency = descriptor.findRequiredDependency("thirdparty/minecraft/client");
		PackageDescriptor mcClientDescriptor = repository.getLocalPackageDescriptorFromLocation(dependency);
		minecraftClient = MinecraftClient.getSetupService(setupParams, mcClientDescriptor, repository, minecraftBootstrap);

		dependency = descriptor.findRequiredDependency("thirdparty/forge");
		PackageDescriptor forgeDescriptor = repository.getLocalPackageDescriptorFromLocation(dependency);
		forge = Forge.getSetupService(setupParams, forgeDescriptor, repository, minecraftClient);

		
		configDir = new File(setupParams.gamedir, MinecraftClient.SUBDIR_CONFIG);
		modsDir = new File(setupParams.gamedir, MinecraftClient.SUBDIR_MODS);
		configFile = new File(configDir, LitWRLConfig.CONFIG_FILE);
		if (configFile.exists()) {
			litwrlcfg = LitWRLConfig.load(configFile);
		} else {
			litwrlcfg = null;
		}
		
		
		
		minecraftBootstrap.init();
		minecraftClient.init();
		forge.init();

		
		if (!setupParams.shader.equals(Shaders.SHADER_NONE) && !setupParams.shader.equals(Shaders.SHADER_INTERNAL)) {
			try {
				String location = descriptor.findOptionalDependency("meta/shaders");
				PackageDescriptor metaShaders = repository.getLocalPackageDescriptorFromLocation(location);
	
				location = metaShaders.findRequiredDependency("thirdparty/shadersmod");
				PackageDescriptor pd = repository.getLocalPackageDescriptorFromLocation(location);
				shadersMod = ShadersMod.getSetupService(setupParams, pd, repository);
				shadersMod.init();
	
				location = metaShaders.findRequiredDependency("thirdparty/optifine");
				pd = repository.getLocalPackageDescriptorFromLocation(location);
				optifine = OptiFine.getSetupService(setupParams, pd, repository);
				optifine.init();
				
				Shaders<String> shaders = new Shaders<String>(metaShaders.location, metaShaders, repository);
				pd = shaders.getPackageDescriptor(setupParams.shader);
				if (pd == null) pd = shaders.migrateUnknownShader(setupParams.gamedir, setupParams.shader);
				shader = shaders.getSetupService(setupParams, pd, repository);
				shader.init();
				
	
				location = descriptor.findOptionalDependency("thirdparty/dynamiclights");
				pd = repository.getLocalPackageDescriptorFromLocation(location);
				dynamicLights = DynamicLights.getSetupService(setupParams, pd, repository);
				dynamicLights.init();
				shadersEnabled = true;
			} catch (IllegalArgumentException e) {
				shadersEnabled = false;
			}
		}
		
		//
		// multiplayer server lists
		//
		try {
			String location = descriptor.findOptionalDependency("meta/multiplayer/litwr");
			if (location != null) {
				PackageDescriptor pd = repository.getLocalPackageDescriptorFromLocation(location);
				multiplayer = MultiplayerMeta.getSetupService(setupParams, pd, repository);
				multiplayer.init();
			}
		} catch (IllegalArgumentException e) {
			// no server list available
		}
	}

	@Override
	public boolean isDownloaded() {
		boolean downloaded = minecraftBootstrap.isDownloaded() 
				&& minecraftClient.isDownloaded() 
				&& forge.isDownloaded() 
				&& isLocalPackageAvailable();
		if (shadersEnabled && Shaders.isNonStandardShader(setupParams.shader)) {
			downloaded = downloaded 
					&& shadersMod.isDownloaded() 
					&& optifine.isDownloaded() 
					&& shader.isDownloaded()
					&& dynamicLights.isDownloaded();
		}
		
		if (multiplayer != null) {
			downloaded = downloaded && multiplayer.isDownloaded();
		}
		return downloaded;
	}

	@Override
	public boolean isInstalled() {
		boolean installed = minecraftBootstrap.isInstalled() 
				&& minecraftClient.isInstalled() 
				&& forge.isInstalled() 
				&& isModsInstalled();
		if (shadersEnabled && Shaders.isNonStandardShader(setupParams.shader)) {
			installed = installed 
					&& shadersMod.isInstalled() 
					&& optifine.isInstalled() 
					&& shader.isInstalled()
					&& dynamicLights.isInstalled();
		}
		if (multiplayer != null) {
			installed = installed && multiplayer.isInstalled();
		}
		return installed;
	}

	private boolean isModsInstalled() {
		return litwrlcfg != null && litwrlcfg.getVersion().equals(descriptor.version);
	}
	
	public boolean hasUpgrade() {
		// TODO: has to ask every related setup service!
		if (litwrlcfg != null) {
			try {
				VersionStd current = VersionStd.decode(litwrlcfg.getVersion());
				VersionStd setup = VersionStd.decode(setupParams.version);
				return setup.isGreaterThan(current);
			} catch (Throwable e) {
				// so, there is a config file, but it is corrupted or its 
				// format is out-dated.
				//
				// We decide to fix this by claiming that we have an update.
				// That way the malicious installation gets removed and
				// we can have a fresh or updated install of the new version.
				return true;
			}
		} else {
			// This package is not installed, 
			// thus it is not considered as an upgrade.
			return false;
		}
	}
	
	@Override
	public String getInstalledVersion() throws Throwable {
		LitWRLConfig litwrlcfg;
		try {
			litwrlcfg = LitWRLConfig.load(configFile);
			return litwrlcfg.getVersion();
		} catch (Throwable e) {
			throw new UnsupportedOperationException(descriptor.name + " is not installed");
		}
	}

	@Override
	public void scheduleDownloads(TaskManager taskman, boolean forced) throws Throwable {
		minecraftBootstrap.scheduleDownloads(taskman, forced);
		minecraftClient.scheduleDownloads(taskman, forced);
		forge.scheduleDownloads(taskman, forced);
		if (shadersEnabled && Shaders.isNonStandardShader(setupParams.shader)) {
			shadersMod.scheduleDownloads(taskman, forced);
			optifine.scheduleDownloads(taskman, forced);
			shader.scheduleDownloads(taskman, forced);
			dynamicLights.scheduleDownloads(taskman, forced);
		}
		
		if (multiplayer != null) {
			multiplayer.scheduleDownloads(taskman, forced);
		}
		
		if (forced || (!isModsInstalled() && !isLocalPackageAvailable())) schedulePackageDownload(taskman);
	}

	private void schedulePrerequisiteInstalls(TaskManager taskman, boolean force) throws Throwable {
		// Those that are not affected by an upgrade will not be forced to reinstall
		minecraftBootstrap.scheduleInstalls(taskman, false);
		minecraftClient.scheduleInstalls(taskman, false);
		forge.scheduleInstalls(taskman, false);
		if (shadersEnabled && Shaders.isNonStandardShader(setupParams.shader)) {
			shadersMod.scheduleInstalls(taskman, force);
			optifine.scheduleInstalls(taskman, force);
			shader.scheduleInstalls(taskman, force);
			dynamicLights.scheduleInstalls(taskman, force);
		}
		
		if (multiplayer != null) {
			multiplayer.scheduleInstalls(taskman, force);
		}
	}
	
	@Override
	public void scheduleInstalls(TaskManager taskman, boolean force) throws Throwable {
		schedulePrerequisiteInstalls(taskman, force);
		if (!isModsInstalled() || force) {
			taskman.addSingleTask(new SetOptions("installing mod-pack", setupParams.gamedir.getAbsolutePath(), setupParams.gameConfig));
			
			taskman.addSingleTask(new Unzip("installing mod-pack", repository.getLocalFileLocation(descriptor, descriptor.filename).getAbsolutePath(), setupParams.gamedir.getAbsolutePath()));
			if (OS.isMac()) {
				taskman.addSingleTask(new Delete("installing mod-pack", new File(setupParams.gamedir, MinecraftClient.SUBDIR_MODS + File.separator + "Waila-1.5.10_1.7.10.jar").getAbsolutePath()));
			}
			taskman.addSingleTask(new FinishLitWRLSetup(new LitWRLConfig(setupParams.version, setupParams.type, litwrlParams.variant, litwrlParams.keepVersion), configFile));
		}

	}
	
	@Override
	public void scheduleUpgrades(TaskManager taskman, SetupService formerVersionSetup) throws Throwable {
		
		LifeInTheWoodsClient formerSetup = (LifeInTheWoodsClient)formerVersionSetup;
		
		
		//
		// for a start we take a very aggressive move here.
		// We will remove all mods and its configs first.
		//
		formerSetup.scheduleRemove(taskman);

		// 
		// now we make sure all prerequisites are still installed (just in case)
		// and we force mods, we had to remove for the upgrade, to reinstall.
		//
		schedulePrerequisiteInstalls(taskman, true);
		
		//
		// and finally we schedule installation of the new package
		//
		taskman.addSingleTask(new Unzip("upgrading mod-pack", repository.getLocalFileLocation(descriptor, descriptor.filename).getAbsolutePath(), setupParams.gamedir.getAbsolutePath()));
		if (OS.isMac()) {
			taskman.addSingleTask(new Delete("upgrading mod-pack", new File(setupParams.gamedir, MinecraftClient.SUBDIR_MODS + File.separator + "Waila-1.5.10_1.7.10.jar").getAbsolutePath()));
		}
		taskman.addSingleTask(new FinishLitWRLSetup(new LitWRLConfig(setupParams.version, setupParams.type, litwrlParams.variant, litwrlParams.keepVersion), configFile));
	}


	
	@Override
	public void scheduleRemove(TaskManager taskman) throws Throwable {
		if (shadersEnabled) {
			dynamicLights.scheduleRemove(taskman);
			shader.scheduleRemove(taskman);
			optifine.scheduleRemove(taskman);
			shadersMod.scheduleRemove(taskman);
		}
		
		if (multiplayer != null) {
			multiplayer.scheduleRemove(taskman);
		}
		
		
		taskman.addSingleTask(new Delete("upgrading mod-pack", new File(setupParams.gamedir.getAbsolutePath(), MinecraftClient.SUBDIR_MODS).getAbsolutePath()));
		taskman.addSingleTask(new Delete("upgrading mod-pack", new File(setupParams.gamedir.getAbsolutePath(), MinecraftClient.SUBDIR_CONFIG).getAbsolutePath()));
	}

	@Override
	public void launch() throws LaunchException {
		Log.info("selecting profile: " + setupParams.gameConfig.getProfileName());
		File launcher_profiles_json = new File(setupParams.workdir, LauncherProfiles.PROFILES_FILE);
		try {
			LauncherProfiles profiles = LauncherProfiles.load(launcher_profiles_json);
			profiles.setSelectProfile(setupParams.gameConfig.getProfileName());
			profiles.save(launcher_profiles_json);
		} catch (IOException | JSONException | JSONCodecException e1) {
			Log.error("launch failed due to inconsistent file '"+ launcher_profiles_json + "'", e1);
			throw new LaunchException(e1);
		} catch (IllegalArgumentException e1) {
			Log.error("launch failed due to inconsistent file '"+ launcher_profiles_json + "'", e1);
			throw new LaunchException(e1);
		}
		
		
		//
		// Cap render distance at 16 on each start if heap size is below 2GB
		//
		if (OS.getTotalPhysicalMemorySize() < 2*1024*1024*1024) {
			Shaders.setCapRenderDistance(setupParams.gamedir, 16);
		}

		if (hideLauncher) {
			File launcherJar = new File(setupParams.workdir, "launcher.jar");
			if (launcherJar.exists()) {
				try {
					LauncherServicesStub stub = LauncherServicesStub.create(launcherJar, PluginServices.getListener());
					stub.launchSelectedProfile(setupParams.workdir);
				} catch (IOException | StubException e) {
					Log.warn("launching through launcher.jar failed. Using fallback.", e);
					hideLauncher = false;
				}
			} else {
				hideLauncher = false;
			}
		} 

		if (!hideLauncher) {
			//
			// If something went wrong during launch through minecraft launch plugin
			// then we try to launch through the official minecraft launcher instead.
			//
			GUI.getInstance().showInfo(
					  "           Launching through Minecraft launcher!", 
					  "                      DON'T PANIC!\n"
					+ "\n"
					+ "Mojang updated it's Minecraft launcher and until we have\n"
					+ "updated ours too, we will start the Minecraft launcher\n"
					+ "to let you play the game.\n"
					+ "\n"
					+ "\n"
					+ "The following will happen now:\n"
					+ "\n"
					+ "1. We will select the LitW/R game profile for you,\n"
					+ "2. we start the Minecraft launcher,\n"
					+ "3. and YOU just need to press Play.\n"
					+ "\n"
					+ "\n"
					+ "                   Have fun!\n"
					+ "\n"
					+ "Oh, and don't forget to select Biomes O' Plenty\n"
					+ "when you create a new world!\n"
					+ "\n"
					+ "              Press OK when you are ready!");

			
			
			RunMinecraftBootstrap runnable = new RunMinecraftBootstrap( minecraftBootstrap.getJar(), setupParams.workdir.getAbsolutePath(), descriptor.version);
			try {
				runnable.run();
			} catch (Throwable t) {
				Log.error("Launch failed.", t);
				throw new LaunchException(t);
			}
		}
	}


}
