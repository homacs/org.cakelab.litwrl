package org.cakelab.litwrl.gui.tabs.config;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

import org.cakelab.json.JSONException;
import org.cakelab.json.codec.JSONCodecException;
import org.cakelab.litwrl.Launcher;
import org.cakelab.litwrl.config.Config;
import org.cakelab.litwrl.config.Variants;
import org.cakelab.litwrl.gui.MainWindow;
import org.cakelab.litwrl.gui.footer.VariantSelector;
import org.cakelab.litwrl.gui.utils.FileEdit;
import org.cakelab.litwrl.gui.utils.notification.JTextAreaChangeNotificationService;
import org.cakelab.litwrl.setup.LitWRSetupParams;
import org.cakelab.litwrl.setup.litwr.LitWRLConfig;
import org.cakelab.litwrl.setup.shaders.Shaders;
import org.cakelab.omcl.config.GameConfig;
import org.cakelab.omcl.config.GameTypes;
import org.cakelab.omcl.repository.Versions;
import org.cakelab.omcl.setup.minecraft.LauncherProfiles;
import org.cakelab.omcl.setup.minecraft.MinecraftClient;
import org.cakelab.omcl.utils.FileSystem;
import org.cakelab.omcl.utils.OS;
import org.cakelab.omcl.utils.Regex;
import org.cakelab.omcl.utils.log.Log;

@SuppressWarnings("serial")
public class ConfigPane extends ConfigPaneUIElements {
	
	private Config config;
	
	/** user selected game type (always client) */
	private static final GameTypes selectedGameType = GameTypes.CLIENT;

	/** user selected game variant */
	private Variants selectedVariant;

	/** litwr game config based on type and variant. */
	private GameConfig selectedGameConfig;
	
	/** launcher profiles based on workdir. Contains profile selected based on gameconfig.*/
	private LauncherProfiles launcherProfiles;
	private boolean profileExists;
	
	/** litwr installation config based on gamedir */
	private LitWRLConfig litwrlcfg;
	
	
	private boolean validContent = false;
	private boolean modified = false;
	/** enables/disables processing of action notifications and controls execution 
	 * of post update tasks such as launcher status update and saving of modifications.
	 * 0 == enabled, not null == disabled */
	private int updating = 0;

	private MainWindow window;

	public ConfigPane() {
		// all basic UI elements initialisation is performed in our base class.
	}

	public void init(MainWindow window, Config config, VariantSelector variantSelector) {
		this.window = window;
		this.config = config;
		this.variantSelector = variantSelector;
		
		beginUpdateSection();
		variantSelector.addActionListener(this);
		
		// TODO: Support different versions for different variants.
		Versions versions = Launcher.INSTANCE.getLitWRVersions(GameTypes.CLIENT, variantSelector.getSelectedVariant());
		
		this.version.init(config, versions.getAvailableVersionStrings());
		
		String workDir = config.getWorkDir();
		if (workDir == null) {
			workDir = Launcher.INSTANCE.getDefaultWorkDir();
			config.setWorkDir(workDir);
		}
		workingDir.init(workDir, this, false);
		
		loadProfiles();
		
		updatedVariant(config.getSelectedVariant());
		
		validContent = validateContent();

		JTextAreaChangeNotificationService notificationService = new JTextAreaChangeNotificationService(this, javaArgs, 500);
		notificationService.setEnabled(true);
		endUpdateSection();
	}

	private void updatedVariant(Variants selectedVariant) {
		beginUpdateSection();
		this.selectedVariant = selectedVariant;
		config.setSelectedVariant(selectedVariant);
		variant.setText(selectedVariant.toString());
		loadGameConfig(selectedVariant);

		endUpdateSection();
	}
	
	private void loadGameConfig(Variants selectedVariant) {
		beginUpdateSection();
		selectedGameConfig = config.getGameConfig(selectedGameType, selectedVariant);
		if (selectedGameConfig == null) {
			selectedGameConfig = config.addGameConfig(selectedGameType, selectedVariant);
		}

		profile.setText(selectedGameConfig.getProfileName());

		updatedProfile();
		endUpdateSection();
	}


	public void loadProfiles() {
		beginUpdateSection();
		try {
			launcherProfiles = LauncherProfiles.load(new File(workingDir.getSelectedFile(), LauncherProfiles.PROFILES_FILE));
		} catch (IOException | JSONException | JSONCodecException e) {
			launcherProfiles = null;
		}

		userSelector.init(launcherProfiles);
		endUpdateSection();
	}

	public void updatedProfile() {
		beginUpdateSection();
		profileExists = false;
		if (launcherProfiles != null) {
			profileExists = launcherProfiles.exists(profile.getText());
			if (profileExists) {
				launcherProfiles.setSelectProfile(profile.getText());
			}
		}
		
		if (profileExists) {
			String dir = launcherProfiles.getGameDir(profile.getText());
			gamedir.init(dir, this, false);
		} else {
			gamedir.init(createDefaultGamePath(), this, false);
		}

		if (profileExists) {
			String args = launcherProfiles.getJavaArgs(profile.getText());
			javaArgs.setText(trimJavaArgs(args));
		} else {
			javaArgs.setText(trimJavaArgs(getOptimizedJavaArgs()));
		}
		
		updatedGameDir();
		endUpdateSection();
	}
	
	public void updatedWorkDir() {
		beginUpdateSection();
		loadProfiles();
		updatedProfile();
		endUpdateSection();
	}
	
	void updatedGameDir() {
		beginUpdateSection();
		litwrlcfg = null;
		try {
			litwrlcfg = LitWRLConfig.loadFromGameDir(gamedir.getSelectedFile());
		} catch (IOException | JSONCodecException e) {
			// nevermind .. we'll fix it below
		}

		
		optionalAddons.updatedGameDir(litwrlcfg, gamedir);

		
		String latestLitWRVersion = Launcher.INSTANCE.getLatestLitWRVersion(selectedGameType, selectedVariant);
		if (latestLitWRVersion == null) latestLitWRVersion = "0.0.0";
		version.setLatestVersion(latestLitWRVersion);
		version.setInstalledVersion( (litwrlcfg != null) ? litwrlcfg.getVersion() : null);
		version.setKeepVersion((litwrlcfg != null) ? litwrlcfg.isKeepVersion() : false);
		version.update();
		updatedVersion();

		endUpdateSection();
	}



	private void updatedVersion() {
		beginUpdateSection();
		boolean willUpgrade = version.isVersionUpgrade();
		
		if (!willUpgrade && litwrlcfg != null) {
			litwrlcfg.setKeepVersion(version.isKeepVersion());
		}
		
		
		Shaders<String> selectedShaderSet = null;
		//
		// refresh set of shaders for the given version
		//
		try {
			selectedShaderSet = Launcher.INSTANCE.fetchLitWRShaders(selectedGameType, selectedVariant, version.getVersion());
		} catch (Exception e) {
			// inconsistent repository
		}
		
		optionalAddons.updatedShaderSet(selectedShaderSet, willUpgrade);

		endUpdateSection();
	}

	private String getOptimizedJavaArgs() {
		long totalMB = OS.getTotalAvailabMemorySize()/1024/1024;
		if (totalMB < 0) {
			return "-Xmx1G -XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -XX:-UseAdaptiveSizePolicy -Xmn128M";
		} else {
			// try to get 4G, 2G is good, 1G is minimum
			int heapMemory = Math.min((int)(((float)totalMB)*3.0/8.0), 2048);
			if (heapMemory < 1024) {
				// We assume that this is an error of the JMX implementation
				// and just set it to the minimum values.
				// If the RAM was really so tiny, then the game won't run anyway
				// and the user would have never tried to install LitWR.
				heapMemory = 1024;
			}
			
			//
			// Its getting extremely slow once youngGen gets larger then the 2nd 
			// level data cache size. So, I guess it should be below.
			// 
			// 
			int youngGenMem = (int)(((float)heapMemory)/1024*128);
			String args = "-Xmx" + heapMemory + "M -Xmn" + youngGenMem + "M ";
			
			// GC specific
			args += " -XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -XX:-UseAdaptiveSizePolicy";
			return args;
		}
	}


	private void setDefaults() {
		beginUpdateSection();
		int answer = JOptionPane.showConfirmDialog(this, "This will reset all settings to their default values.", Launcher.APPLICATION_NAME + " - Question", JOptionPane.OK_CANCEL_OPTION);
		
		if (answer == JOptionPane.CLOSED_OPTION || answer == JOptionPane.CANCEL_OPTION) {
			return;
		}
		
		setConfigurable(false);

		Log.info("resetting to default config");
		String workDir = Launcher.INSTANCE.getDefaultWorkDir();
		if (!workDir.equals(workingDir.getSelectedFile().getAbsolutePath())) {
			workingDir.setSelectedFile(workDir);
			config.setWorkDir(workDir);
			modified = true;
		}
		javaArgs.setText(trimJavaArgs(getOptimizedJavaArgs()));
		String defaultGameDir = createDefaultGamePath();
		if (!defaultGameDir.equals(gamedir.getSelectedFile().getAbsolutePath())) {
			gamedir.setSelectedFile(defaultGameDir);
			modified = true;
		}

		optionalAddons.setDefaults();
		
		version.setKeepVersion(false);
		version.update();
		updatedVersion();

		setConfigurable(true);
		endUpdateSection();
	}

	
	private String trimJavaArgs(String args) {
		return args != null ? args.replaceAll("" + Regex.utf_whitespace_class + Regex.utf_whitespace_class + "*", " ").trim() : "";
	}

	private String createDefaultGamePath() {
		return new File(Launcher.INSTANCE.getConfigDir(), Launcher.INSTANCE.getDefaultGameSubDir(selectedGameConfig)).toString();
	}




	private void checkModified() {
		boolean needsStatusUpdate = false;
		
		
		boolean isValid = validateContent();
		needsStatusUpdate = needsStatusUpdate || (isValid != validContent);
		validContent = isValid;

		if (validContent) {
			if (config.isModified()) {
				config.save();
				needsStatusUpdate = true;
			}
			if (profileExists && launcherProfiles != null && launcherProfiles.isModified()) {
				try {
					launcherProfiles.save();
					needsStatusUpdate = true;
				} catch (IOException | JSONCodecException | JSONException e) {
					Log.error("saving profile failed", e);
				}
			}
			if (litwrlcfg != null && litwrlcfg.isModified()) {
				try {
					litwrlcfg.save();
				} catch (IOException | JSONCodecException e) {
					Log.error("saving litwrl config");
				}
			}
		}
		
		if (needsStatusUpdate || modified) {
			window.updateSetupStatus();
			modified = false;
		}
		
	}

	private boolean validateContent() {
		return workingDir.getInputVerifier().verify(workingDir);
	}

	public boolean hasValidContent() {
		return validContent;
	}


	private void setValidContent(boolean isValid) {
		if (validContent != isValid) {
			validContent = isValid;
			window.updateSetupStatus();
		}
	}

	

	@Override
	public void actionPerformed(ActionEvent e) {
		if (isUpdating()) return;
		
		if (e.getSource().equals(variantSelector)) {
			updatedVariant(variantSelector.getSelectedVariant());
		} else if (e.getSource().equals(resetButton)) {
			setDefaults();
		}
	}


	@Override
	public void updatedUIConfigField(UIConfigField field) {
		if (field == version) {
			updatedVersion();
		} else if (field == optionalAddons) {
			beginUpdateSection();
			modified = true;
			endUpdateSection();
		}
	}


	@Override
	public void delayedNotification(JComponent component) {
		beginUpdateSection();
		if (component.equals(javaArgs)) {
			if (profileExists) {
				String args = trimJavaArgs(javaArgs.getText());
				launcherProfiles.setJavaArgs(this.selectedGameConfig.getProfileName(), args);
			}
		}
		endUpdateSection();
	}

	private boolean validateWorkDir(File wd) {
		
		if (FileSystem.isChildOf(wd, gamedir.getSelectedFile())) {
			workingDir.setErrorMessage("The working directory cannot be located inside the game directory.");
			return false;
		} else if (!FileSystem.hasAccessibleParent(wd)) {
			workingDir.setErrorMessage("Selected working directory cannot be created\nor does not allow to store files in it.");
			return false;
		} else if (wd.exists() && new File(wd, MinecraftClient.SUBDIR_MODS).exists()) {
			workingDir.setErrorMessage("This minecraft installation already contains mods.\nYou need to choose another directory.");
			return false;
		} else {
			try {
				if (wd.exists() && Files.isSameFile(wd.toPath(), FileSystem.getUserHome().toPath())) {
					workingDir.setErrorMessage("Don't do this!\nYou probably meant '" + new File(FileSystem.getUserHome(), ".minecraft").toString() + "'");
				}
			} catch (AccessDeniedException e) {
				workingDir.setErrorMessage("Can't use chosen directory: access denied");
				return false;
			} catch (IOException e) {
				workingDir.setErrorMessage("Can't use chosen directory: '" + e.getLocalizedMessage() + "'");
				return false;
			}
		}
		return true;
	}
	
	private boolean validateGameDir(File gd) {
		if (FileSystem.isChildOf(gd, workingDir.getSelectedFile())) {
			gamedir.setErrorMessage("Game directory should not be located inside the Minecraft folder.");
			return false;
		} else if (!FileSystem.hasAccessibleParent(gd)) {
			workingDir.setErrorMessage("Selected working directory cannot be created\nor does not allow to store files in it.");
			return false;
		} else {
			try {
				if (gd.exists() && Files.isSameFile(gd.toPath(), FileSystem.getUserHome().toPath())) {
					workingDir.setErrorMessage("Don't do this. You probably meant '" + new File(FileSystem.getUserHome(), "games").toString() + "'");
				}
			} catch (AccessDeniedException e) {
				workingDir.setErrorMessage("Can't use chosen directory: access denied");
				return false;
			} catch (IOException e) {
				workingDir.setErrorMessage("Can't use chosen directory: '" + e.getLocalizedMessage() + "'");
				return false;
			}
		}
		return true;
	}

	

	@Override
	public boolean verify(FileEdit folderEdit) {
		if (folderEdit.equals(workingDir)) {
			File wd = workingDir.getSelectedFile();
			boolean isValid = validateWorkDir(wd);
			if (isValid) {
				config.setWorkDir(wd.toString());
				updatedWorkDir();
			} else {
				userSelector.init(null);
				setValidContent(false);
				return false;
			}
		} else if (folderEdit.equals(gamedir)) {
			File gd = gamedir.getSelectedFile();
			boolean isValid = validateGameDir(gd);
			if (isValid) {
				if (profileExists) {
					launcherProfiles.setGameDir(profile.getText(), gd);
				}
				updatedGameDir();
			} else {
				setValidContent(false);
				return false;
			}
		}
		return true;
	}



	public LitWRSetupParams getSetupParams() {
		LitWRSetupParams setup;
		setup = new LitWRSetupParams(selectedGameConfig, 
				new File(config.getWorkDir()), 
				gamedir.getSelectedFile(), 
				version.getVersion(), 
				version.isKeepVersion(),
				selectedGameType, 
				config.getSelectedVariant(), 
				trimJavaArgs(javaArgs.getText()), 
				optionalAddons.getSelectedShader(),
				optionalAddons.getOptionalAddons());
		return setup;
	}



	public void setConfigurable(boolean configurable) {
		workingDir.setEditable(configurable);
		gamedir.setEditable(configurable);
		javaArgs.setEditable(configurable);
		resetButton.setEnabled(configurable);
		userSelector.setEnabled(configurable);
		version.setConfigurable(configurable);
		optionalAddons.setConfigurable(configurable);
	}

	private boolean isUpdating() {
		return (updating != 0);
	}

	void beginUpdateSection() {
		this.updating += 1;
		optionalAddons.beginUpdateSection();
	}
	
	void endUpdateSection() {
		this.updating -= 1;
		assert(this.updating >= 0);
		optionalAddons.endUpdateSection();
		if (!isUpdating()) {
			checkModified();
		}
	}

}
