package org.cakelab.litwrl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Arrays;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;

import org.cakelab.json.JSONException;
import org.cakelab.json.codec.JSONCodecException;
import org.cakelab.litwrl.config.Config;
import org.cakelab.litwrl.config.Variants;
import org.cakelab.litwrl.gui.LitWRLGUI;
import org.cakelab.litwrl.gui.utils.OptionalPane;
import org.cakelab.litwrl.repository.LitWRLRepository;
import org.cakelab.litwrl.setup.LitWRSetupParams;
import org.cakelab.litwrl.setup.SetupControl;
import org.cakelab.litwrl.setup.launcher.LauncherUpdate;
import org.cakelab.litwrl.setup.litwr.LaunchException;
import org.cakelab.litwrl.setup.litwr.LaunchService;
import org.cakelab.litwrl.setup.litwr.LifeInTheWoods;
import org.cakelab.litwrl.setup.shaders.Shaders;
import org.cakelab.omcl.config.GameConfig;
import org.cakelab.omcl.config.GameTypes;
import org.cakelab.omcl.gui.ExternalBrowser;
import org.cakelab.omcl.repository.PackageDescriptor;
import org.cakelab.omcl.repository.Versions;
import org.cakelab.omcl.setup.SetupStatus;
import org.cakelab.omcl.setup.VersionStd;
import org.cakelab.omcl.setup.minecraft.LauncherProfiles;
import org.cakelab.omcl.setup.minecraft.MinecraftBootstrap;
import org.cakelab.omcl.taskman.TaskManager;
import org.cakelab.omcl.update.ServerLockedException;
import org.cakelab.omcl.update.TransportException;
import org.cakelab.omcl.update.URLPath;
import org.cakelab.omcl.update.UpdateServer;
import org.cakelab.omcl.update.UpdateServerPool;
import org.cakelab.omcl.utils.CharacterEncoding;
import org.cakelab.omcl.utils.Classpath;
import org.cakelab.omcl.utils.FileSystem;
import org.cakelab.omcl.utils.OS;
import org.cakelab.omcl.utils.UrlConnectionUtils;
import org.cakelab.omcl.utils.log.Log;
import org.cakelab.omcl.utils.log.LogFileListener;



public class Launcher {

	/** Version of the running launcher */
	public static final String LAUNCHER_VERSION = "1.3.6";
	/** maximum litwr version we can install with this launcher */
	private static final String MAX_LITWR_VERSION = "1.2.0";

	/** URL displayed to the user in case we can't upgrade. */
	private static final String LAUNCHER_UPDATE_URL = "http://lifeinthewoods.ca/litwr/downloads/LifeInTheWoodsRenaissanceLauncher.zip";

	public static final String APPLICATION_NAME = "Life in the Woods Renaissance Launcher v" + LAUNCHER_VERSION;
	private static final String LAUNCHER_NAME = "Life in the Woods Renaissance Launcher";
	public static final String STANDARD_PROFILE_PREFIX = "LitWR";

	
	// -----------   FILES AND FOLDERS -------------
	public static final String CONFIG_DIR_NAME = ".litwrl";

	public static final String LOGFILE = "litwrl.log";
	public static final String TASK_DB = "task.db";
	private static final String LAUNCHER_LOCK_FILE = "lock";
	private static final String LAUNCHER_PID_FILE = "pid";
	
	public static final String GAMES_SUBDIRECTORY = "games";
	public static final String REPOSITORY_FOLDER ="repository";
	static final String LAUNCHER_JAR_FILE = "litwrl.jar";
	static final String LAUNCHER_REPOSITORY_LOCATION = "launcher/" + LAUNCHER_VERSION;

	// -----------   RETURN VALUES OF isUniqueInstance() -------------
	private static final int IS_UNIQUE_SUCCESS = 2;
	private static final int IS_UNIQUE_ERROR = 1;
	private static final int IS_UNIQUE_NOT = 0;
	private static final int IS_UNIQUE_NOT_PID_UNKNOWN = IS_UNIQUE_NOT;

	/** singleton instance */
	public static final Launcher INSTANCE = new Launcher();

	private File tmpDir;
	private File configDir;
	private FileLock systemWideFileLock;
	

	private Config config;
	private SetupControl setupControl;
	private TaskManager taskman;
	private LitWRLRepository repository;
	private LitWRLGUI gui;
	private UpdateServer updateServer;
	private UpdateServer primaryUpdateServer;
	private UpdateServer secondaryUpdateServer;
	
	
	private boolean needRestartShown = false;
	private boolean canInstall = true;

	public static Launcher create() {
		return INSTANCE;
	}
	
	public void init() {
		UrlConnectionUtils.initSystemProperties();

		configDir = Main.getConfigDir();

		//
		// Force unique process instance or die trying.
		//
		int pid = 0;
    	try {
    		pid = OS.getpid();
    	} catch (UnsupportedOperationException e) {
    		e.printStackTrace();
    	}
		tryUniqueInstance(pid);
		
		initLog();
		
		String launcherLocation = null;
		try {
			File location = Classpath.determineClasspathEntry(Main.class).getDirectory();
			launcherLocation = FileSystem.getStandardisedPath(location);
		} catch (Throwable t) {
			// This is just for debugging purposes, so we don't care about errors here
			launcherLocation = ".. on earth I guess";
		}
		Log.info("running launcher (" + pid + ") located at: " + launcherLocation);

		//
		// Temporary local directory for downloads
		//
		tmpDir = new File(configDir, "tmp");
		if (tmpDir.exists()) {
			FileSystem.delete(tmpDir);
		}
		tmpDir.mkdirs();
		
		//
		// create and show gui
		//
		gui = LitWRLGUI.create(this);

		config = Config.load(configDir);

		Log.info("Running launcher version " + LAUNCHER_VERSION + " (pid:"+pid+")");
		Log.info("Config directory: " + configDir);

		taskman = new TaskManager(new File(configDir, TASK_DB));
		tryFinishingInterruptedTasks();

		File repositoryFolder = new File(configDir, REPOSITORY_FOLDER);
		int revision;
		try {
			revision = LitWRLRepository.getRevision(repositoryFolder);
		} catch (IOException e) {
			Log.warn("local repository read error");
			revision = 0;
			FileSystem.delete(repositoryFolder);
		}
		tryConnectUpdateServer(config, revision);

		repository = new LitWRLRepository(updateServer, repositoryFolder);
		
		tryLauncherUpdate(config);

		checkPostUpdateTasks(config);
		
		tryInitRepository();
		
		if (config.isModified()) config.save();

		// if the launcher requires an update it will 
		// immediately perform a restart and not return to this
		// method here.
		Log.info("initialisation successful.");
		if (!canInstall) Log.info("installation feature turned off.");
		
		gui.init(Variants.values(), config);
		
	}

	private void checkPostUpdateTasks(Config config) {
		// TODO: those should be stored in task db as well
		VersionStd zero = VersionStd.decode("0.0.0");
		VersionStd last = VersionStd.decode(config.getLastVersion());
		if (last.equals(zero)) {
			config.setLastVersion(LAUNCHER_VERSION);
			config.save();
			return;
		}
		if (last.getMajor() <= 1) {
			if (last.getMinor() <= 2) {
				if (last.getBuild() <= 2) {
					if (config.getWorkDir() != null) {
						FileSystem.delete(new File(new File(config.getWorkDir()),MinecraftBootstrap.LAUNCHER_JAR));
					}
				} 
				if (last.getBuild() <= 5) {
					//
					// Add versions file pointing to the latest version
					//
					PackageDescriptor descriptor = Launcher.getPackageDescriptor();
					File versionsFile = repository.getLocalVersionsFile(descriptor);
					Versions versions;
					if (!versionsFile.exists()) {
						versions = new Versions();
					} else {
						try {
							versions = Versions.load(versionsFile);
						} catch (Throwable e) {
							Log.warn("Failed to read local versions file: " + versionsFile.getAbsolutePath(), e);
							versions = new Versions();
						}
					}
					int latest = versions.addAvailable(descriptor.location);
					versions.setLatest(latest);
					try {
						File dir = versionsFile.getParentFile();
						if (!dir.exists()) dir.mkdirs();
						versions.save(versionsFile);
					} catch (Throwable e) {
						Log.error("failed to update versions file: " + versionsFile.getAbsolutePath(), e);
					}
					
					//
					// Fix corrupted profile with Umlaute
					//
					String userhome = System.getProperty("user.home");
					if (OS.isWindows() && !CharacterEncoding.isPureAscii(userhome)) {
						if (config.getWorkDir() != null) {

							File profilesFile = new File(new File(config.getWorkDir()), LauncherProfiles.PROFILES_FILE);
							if (profilesFile.exists()) {
								try {
									LauncherProfiles profiles = LauncherProfiles.load(profilesFile);
									profiles.removeProfile(Launcher.STANDARD_PROFILE_PREFIX + ".Basic");
									profiles.removeProfile(Launcher.STANDARD_PROFILE_PREFIX + ".Hungry");
									profiles.save();
								} catch (IOException | JSONException | JSONCodecException e) {
									// delete corrupted profiles file
									Log.error("corrupted profiles file. Creating backup", e);
									try {
										FileSystem.mv(profilesFile, new File(profilesFile.getAbsolutePath() + ".backup"), false);
									} catch (IOException e1) {
										Log.error("failed to move corrupted profiles file. Deleting.", e1);
									}
								}
							}
						}
					}
				}
			}
			if (last.getMinor() <= 3) {
				if (last.getBuild() <= 3) {
					config.setDownShowUpgradeWarning(false);
				}
			}
		}
		config.setLastVersion(LAUNCHER_VERSION);
		config.save();
	}

	private void tryUniqueInstance(int mypid) {
		int pid = isUniqueInstance(mypid);
		if (pid == IS_UNIQUE_ERROR) {
			// Can't say. We just let the user play. Hopefully there 
			// is no other instance running.
			return;
		}
		while (pid <= IS_UNIQUE_NOT) {
			boolean killAndRetry = false;
			String message = "There is another launcher already running." + "\n\n" + "Please use that instead.";
			if (pid == IS_UNIQUE_NOT_PID_UNKNOWN || !OS.isKillSupported()) {
				OptionalPane.showMessageDialog(null, message, Launcher.APPLICATION_NAME, JOptionPane.INFORMATION_MESSAGE);
			} else {
				JRadioButton killIt = new JRadioButton("I can't because it's crashed and I want you to kill the other launcher!");
				int isClosed = OptionalPane.showOptionDialog(null, message, Launcher.APPLICATION_NAME, 
						JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, new JComponent[]{killIt});
				if (isClosed != JOptionPane.CLOSED_OPTION && killIt.isSelected()) {
					int result = JOptionPane.showConfirmDialog(null, "Are you sure?!" + "\n\n" + "Killing a running process can cause data loss!\nWe will also kill any Minecraft client started\nby the launcher!", Launcher.APPLICATION_NAME, JOptionPane.YES_NO_OPTION);
					if (result == JOptionPane.YES_OPTION) { 
						killAndRetry  = true; 
					}
				}
			}
			
			if (killAndRetry) {
				try {
					OS.kill(-pid, true);
					// some systems need a second to release system resources
					Thread.sleep(1000);
					pid = isUniqueInstance(mypid);
					if (pid == IS_UNIQUE_ERROR) {
						// Can't say. We just let the user play. Hopefully there 
						// is no other instance running.
						return;
					}
				} catch (IOException e) {
					System.err.println("Getting a unique process instance failed");
					e.printStackTrace();
					JOptionPane.showMessageDialog(null, 
									"Can't kill the running process (pid: " + pid + ")!" 
									+ "\n\n" + "Probably we have no support for your system, sorry!"
									+ "\n\n" + "Shutting down.",
							Launcher.APPLICATION_NAME, JOptionPane.ERROR_MESSAGE);
					killAndRetry = false;
				} catch (InterruptedException e) {
					System.err.println("Interrupted while trying to get a unique process instance");
					e.printStackTrace();
					killAndRetry = false;
				}
			}
			
			if (!killAndRetry) {
				// that's it, I'm outa here!
				exit(-1);
			}
		}
	}

	private void tryInitRepository() {
		Throwable e = null;
		String reason = "Reason is unknown.";
		try {
			repository.init();
			/* 
			 * prefetch data here, so the GUI doesn't need to later on.
			 */
			if (!updateServer.isOffline()) {
				String version = getLatestLitWRVersion(config.getSelectedType(), config.getSelectedVariant());
				repository.fetchLitWRDependencies(config.getSelectedType(), config.getSelectedVariant(), version);
			}
		} catch (JSONCodecException e1) {
			e = e1;
			reason = "Inconsistencies on server side.\nPlease report the issues so it gets fixed.\nRefer to http://homac.cakelab.org/projects/litwrl/support/report.html";
		} catch (ServerLockedException e1) {
			e = e1;
			reason = "Server is currently getting updated.\nPlease retry later.";
		} catch (TransportException e1) {
			e = e1;
			reason = "Server not available or we have lost connection.\nPlease retry later.";
		} catch (IOException e1) {
			e = e1;
			reason = "Issues with file access or inconsistencies in local repository.\nRefer to http://homac.cakelab.org/projects/litwrl/support/ to get help.";
		} catch (Throwable e1) {
			e = e1;
			reason = "Internal error '"+ e.getClass().getSimpleName() + "'\nRefer to http://homac.cakelab.org/projects/litwrl/support/ to get help.";
		}
		
		if (e != null) {
			String title = "Local repository initialisation failed!";
			Log.warn(title + "\n" + reason, e);
			gui.showWarning(title, 
					"\n\n" + reason + "\n\n"
					+ "You won't be able to install or update.");
			canInstall = false;
		}
	}

	private void tryConnectUpdateServer(final Config config, final int revision) {
		
		if (config.isOffline()) {
			updateServer = new UpdateServer();
			return;
		}

		final UpdateServerPool pool = new UpdateServerPool(config.getUpdateURL(), config.getServerPool());
		
		Thread connectPrimary = new Thread("connectPrimary"){
			public void run() {
				try {
					primaryUpdateServer = pool.connect(revision);
				} catch (Throwable t) {
					Log.warn("can't connect to any update server in pool.", t);
				}
			}
		};
		connectPrimary.start();
		
		
		
		Thread connectSecondary = new Thread("connectSecondary"){
			public void run() {
				try {
					secondaryUpdateServer = new UpdateServer(new URL(config.getSecondaryUpdateURL()));
				} catch (Throwable t) {
					Log.warn("can't connect to secondary update server.", t);
				}
			}
		};
		connectSecondary.start();
		try {connectPrimary.join();} catch (InterruptedException e) {}
		try {connectSecondary.join();} catch (InterruptedException e) {}
		
		if (primaryUpdateServer != null) {
			updateServer = primaryUpdateServer;
			Log.info("using update server " + updateServer.getUrl());
		} else if (secondaryUpdateServer != null && secondaryUpdateServer.getMinRevision() >= revision) {
			updateServer = secondaryUpdateServer;
			Log.info("using update server " + updateServer.getUrl());
		} else {
			Log.warn("failed to connect to an update server. Fallback to offline mode.");
			gui.showWarning("No update server available.", 
					"Either update servers are currently down or you are currently offline.\n\n"
					+ "You can retry later or proceed in offline mode.");
			updateServer = new UpdateServer();
			canInstall = false;
		}
		
	}

	private void tryFinishingInterruptedTasks() {
		try {
			taskman.loadDB();
			if (taskman.hasPendingTasks()) {
				Log.info("There are unfinished tasks. Going to finish them now.");
				gui.showInfo("Going to finish interrupted tasks", 
						"The launcher was interrupted during installation\n"
						+ "or update last time. We are going to finish the\n"
						+ "remaining tasks now to get back into a stable\n"
						+ "state.");
				if (!taskman.runToCompletion(true, gui)) {
					taskman.resetDB();
				}
			}
		} catch (Throwable e) {
			Log.error("Finishing unfinished tasks failed. Discarding all remaining tasks.", e);
			try {
				taskman.resetDB();
			} catch (IOException | JSONCodecException e1) {
				Log.error("Can't access task db.", e);
			}
		}
	}

	private void tryLauncherUpdate(Config config) {
		if (config.isOffline() || updateServer.isOffline()) return;
		try {
			Log.info("looking for launcher update");
			PackageDescriptor descriptor = getPackageDescriptor();
			URLPath basePath = new URLPath(descriptor.location).getParent();
			Versions versions = updateServer.getVersions(basePath);
			VersionStd recommendedVersion = VersionStd.decode(versions.getLatestVersionString());
			VersionStd currentVersion = VersionStd.decode(descriptor.version);
			if (null != recommendedVersion && recommendedVersion.isGreaterThan(currentVersion)) {
				Log.info("applying launcher update to v" + recommendedVersion);
				PackageDescriptor newDescriptor = fetchPackageDescriptorForVersion(descriptor, recommendedVersion.toString());
				LauncherUpdate setupService = new LauncherUpdate(this, newDescriptor, repository);
				
				setupService.scheduleDownloads(taskman, false);
				setupService.scheduleInstalls(taskman, false);
				
				if (!taskman.runToCompletion(true, gui)) {
					throw new Throwable("error executing launcher update tasks. See log for details.");
				}
			} else {
				Log.info("launcher up-to-date");
			}
		} catch (Throwable e) {
			Log.error("Error during launcher update:", e);
			canInstall = false;
			if (e instanceof SocketTimeoutException 
					|| e instanceof TransportException 
					|| e instanceof ServerLockedException) 
			{
				gui.showError("Update server not available!", 
						"Please retry later.\n");
			} else {
				gui.showError("Launcher update failed for mysterious reasons!",
						"Please download the new launcher from \n"
						+ Launcher.LAUNCHER_UPDATE_URL
						+ "to be able to update to newer mod-pack versions.");
			}
		}
	}

	/**
	 * This method fetches a new package descriptor from the Update 
	 * Server and adds it to the local repository.
	 * 
	 * 
	 * The local repository will not be updated and the revision number
	 * will not change. 
	 * 
	 * @param descriptor
	 * @param version
	 * @return
	 * @throws ServerLockedException
	 * @throws TransportException
	 * @throws IOException
	 * @throws JSONCodecException
	 */
	public PackageDescriptor fetchPackageDescriptorForVersion(
			PackageDescriptor descriptor, String version) throws ServerLockedException, TransportException, IOException, JSONCodecException {
		String newLocation = new URLPath(descriptor.location).getParent().append(version).toString();
		PackageDescriptor newDescriptor = null;
		if (!updateServer.isOffline()) {
			try {
				newDescriptor = updateServer.getPackageDescriptorForLocation(newLocation);
			} catch (TransportException e) {
				if (e.getCause() != null && e.getCause() instanceof FileNotFoundException) {
					Log.warn("inconsistent repository on server side - package not found at " + descriptor.location);
				}
			}
		}
		return newDescriptor;
	}

	static PackageDescriptor getPackageDescriptor() {
		return new PackageDescriptor(LAUNCHER_NAME, LAUNCHER_VERSION, LAUNCHER_JAR_FILE, LAUNCHER_REPOSITORY_LOCATION, null);
	}

	private void initLog() {
		File logfile = new File(configDir, LOGFILE);
		try {
			Log.addLogListener(new LogFileListener(logfile));
		} catch (IOException e) {
			Log.warn("can't access launcher log file ' " + logfile.getAbsolutePath() + "'");
		}
	}


	public void run() {
		init();
	}

	public void exit(int status) {
		System.exit(status);
	}

	public String createStandardProfileName(String gameConfigName) {
		return STANDARD_PROFILE_PREFIX + "." + gameConfigName;
	}

	public Versions getLitWRVersions(GameTypes type,
			Variants variant) {
		return repository.getLocalLitWRVersions(type, variant);
	}

	public String getLatestLitWRVersion(GameTypes type, Variants variant) {
		Versions versions = repository.getLocalLitWRVersions(type, variant);
		if (versions == null) {
			return null;
		}
		String latest = versions.getLatestVersionString();
		VersionStd latestVersion = VersionStd.decode(latest);
		VersionStd supportedVersion = VersionStd.decode(Launcher.MAX_LITWR_VERSION);
		if (supportedVersion.isLessEqual(latestVersion)) {
			if (!needRestartShown) {
				gui.showInfo("Launcher restart required.", 
						  "It must have been just a second ago that version " + latest + " of Life in the Woods\n"
						+ "Renaissance came out. Unfortunately, the launcher you are currently\n"
						+ "running supports installation of versions below " + Launcher.MAX_LITWR_VERSION + " only.\n"
						+ "Just restart the launcher to receive the new launcher to be able to update\n"
						+ "to version" + latest + ".");
				needRestartShown = true;
			}
			latest = Launcher.MAX_LITWR_VERSION;
		}
		return latest;
	}

	public String getDefaultGameSubDir(GameConfig selectedGameConfig) {
		return GAMES_SUBDIRECTORY + File.separator + selectedGameConfig.getProfileName();
	}

	public String getDefaultWorkDir() {
		return new File(configDir, "minecraft").getPath();
	}

	public boolean setupLitWR(LitWRSetupParams params) {
		
		if (!canInstall) {
			gui.showError("Installation feature not available.", 
					"You can restart the launcher to try again.");
			return false;
		}
		
		Log.info("attempting setup with the following parameters:");
		
		logSetupParams(params);
		try {
			setupControl = new SetupControl(params, repository, taskman);

			if (updateServer.isOffline() && setupControl.needsDownloads()) {
				gui.showError("Can't install due to missing downloads.", "Network or update server is currently not available.\nTry again later.");
				return false;
			}

			if (setupControl.hasUpgrade()) {
				if (!gui.showUpgradeWillResetWarning()) return false;
			}
			
			if (config.isModified()) config.save();
			
			setupControl.scheduleSetupTasks();
			
			if (!taskman.runToCompletion(true, gui)) {
				Log.warn("Installation failed or cancelled");
				taskman.resetDB();
				return false;
			}
			if (!Shaders.isNonStandardShader(params.shader) && !config.isDontShowShaderHint()) {
				config.setDontShowShaderHint(gui.showShaderHint());
			} else {
				config.setDontShowShaderHint(true);
			}
			return true;
		} catch (Throwable e) {
			Log.error("Installation failed: ", e);
			if (e instanceof TransportException) {
				gui.showError("Installation failed.", e.getCause().getMessage());
				canInstall = false;
			} else {
				gui.showError("Installation failed.", e.getMessage());
			}
			
			try {
				taskman.resetDB();
			} catch (IOException | JSONCodecException e1) {
				Log.warn("Reset of taskmanager failed.", e1);
			}
			
		}
		return false;
	}
	
	
	public Shaders<String> getLitWRShaders(GameTypes type,
			Variants variant, String version) throws Exception {
		try {
			PackageDescriptor descriptor = repository.fetchLitWRDependencies(type, variant, version);
			if (descriptor == null) return null;
			String location = descriptor.findOptionalDependency("meta/shaders");
			PackageDescriptor metaShaders = repository.getLocalPackageDescriptorFromLocation(location);
			
			Shaders<String> shaders = new Shaders<String>(metaShaders.location, metaShaders, repository);
			return shaders;

		} catch (IllegalArgumentException e) {
			// no shaders available
			throw e;
		}
	}

	
	public boolean upgradeLitWR(LitWRSetupParams params) {
		return setupLitWR(params);
	}
	
	public boolean modifyLitWR(LitWRSetupParams setup) {
		return setupLitWR(setup);
	}


	public SetupStatus getSetupStatus(LitWRSetupParams params) {
		try {
			setupControl = new SetupControl(params, repository, taskman);
			SetupStatus status = setupControl.getSetupStatus();
			if (!canInstall && (!status.isInstalled() || status.hasUpgrade() || status.hasModifications())) {
				return null;
			} else {
				return status;
			}
		} catch (Throwable e) {
			Log.error("error requesting setup status.", e);
			return null;
		}
	}
	
	
	private void logSetupParams(LitWRSetupParams params) {
		Log.info("   version:  " + params.version);
		Log.info("   workdir:  " + params.workdir);
		Log.info("   variant:  " + params.variant.toString());
		Log.info("   type:     " + params.type.toString());
		Log.info("   profile:  " + params.gameConfig.getProfileName());
		Log.info("   gamedir:  " + params.gamedir);
		Log.info("   javaArgs: " + params.javaArgs);
		Log.info("   addons:   " + Arrays.toString(params.optionals));
	}

	public File getTempDir() {
		return tmpDir;
	}

	public File getConfigDir() {
		return configDir;
	}

	public boolean launchLitWR(LitWRSetupParams params) {
		try {
			Log.info("attempting to launch with the following parameters:");
			logSetupParams(params);
			
			String packageLocation = repository.getLocationForLitWRParams(params.type, params.variant, params.version);
			PackageDescriptor pd = repository.getLocalPackageDescriptorFromLocation(packageLocation);
			LaunchService service = LifeInTheWoods.getLaunchService(pd, params, repository);
			service.init();
			service.launch();
			return true;
		} catch (LaunchException e) {
			Log.error("Launch of minecraft client failed.", e);
			return false;
		} catch (Throwable e) {
			Log.error("Initialisation of launch failed.", e);
			return false;
		}
	}

	/**
	 * Tries to determine whether there is another process of the 
	 * launcher running.
	 * 
	 * ERRORS:
	 * If another process is found it returns -pid.
	 * If another process is found, but we can't get its pid, it returns 0.
	 * If an error occurred or we don't have support for the system, it returns 1.
	 * If it succeeds (it is unique) then it returns 2.
	 * 
	 * @param mypid pid of our own process or 0 if not available.
	 * @return
	 */
	public int isUniqueInstance(int mypid) {
		int isUnique = IS_UNIQUE_SUCCESS; // assume success
		try {
			File lockFile = new File(getConfigDir(), LAUNCHER_LOCK_FILE);
			File pidFile = new File(getConfigDir(), LAUNCHER_PID_FILE);
			
			// create if non existent
			lockFile.createNewFile();
	
			// open and leave open until process exit
			@SuppressWarnings("resource")
			RandomAccessFile raf = new RandomAccessFile(lockFile, "rw");
			RandomAccessFile pidRW = new RandomAccessFile(pidFile, "rw");
			FileChannel fc = raf.getChannel();
			systemWideFileLock = fc.tryLock();
		    if (systemWideFileLock == null) {
		    	// 0 or other process id
		    	isUnique = -pidRW.readInt();
		    } else {
		    	pidRW.writeInt(mypid);
		    	fc.force(true); // aka. flush()
		    	isUnique = IS_UNIQUE_SUCCESS; // success
		    }
		    pidRW.close();
		} catch (IOException e) {
			// lot's of impossible exceptions (prevented earlier), but anyway
			e.printStackTrace();
			isUnique = IS_UNIQUE_ERROR; // error, unsure about the situation
		}
		return isUnique;
	}

	/**
	 * Called from the launcher restart task to inform the launcher 
	 * that a restart is about to happen and it should prepare to be 
	 * killed after this method call.
	 */

	public void prepareRestart() {
		try {
			// file locks are handled by the operating system
			// so, they will be released as soon as we die, but
			// anyway, we do a regular release here.
			systemWideFileLock.release();
		} catch (IOException e) {
			// nothing we could do about it here - we are going down now.
			e.printStackTrace();
		}
	}

	public File getDownloadFolder() {
		if (config.getDownloadFolder() == null) {
			config.setDownloadFolder(ExternalBrowser.getDefaultDownloadFolder().getAbsolutePath());
			config.save();
		}
		return new File(config.getDownloadFolder());
	}

	public void updateDownloadFolder(String parent) {
		config.setDownloadFolder(parent);
		if (config.isModified())config.save();
	}

	public Config getConfig() {
		return config;
	}




}
