package org.cakelab.litwrl.gui.tabs.config;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

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
import org.cakelab.litwrl.setup.shadersmod.OptionsShaders;
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
	
	/** Selected set of shaders based on litwr game config and version */
	private Shaders<String> selectedShaderSet;
	private String selectedShader;
	
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

		updatedWorkDir();
		endUpdateSection();
	}

	private void setSelectedShader(String selectedItem) {
		beginUpdateSection();
		if (OPTIONAL_ADDONS_FEATURE) {
			optionalAddons.setSelectedShader(selectedItem);
		}
		if (selectedShader != selectedItem) {
			boolean isUnknown = false;
			try {
				String filename;
				try {
					if (selectedShaderSet != null) {
						filename = selectedShaderSet.getFilenameOf(selectedItem);
					} else {
						filename = Shaders.getUnknownShaderFileName(selectedItem);
						isUnknown = true;
					}
				} catch (IllegalArgumentException e) {
					filename = Shaders.getUnknownShaderFileName(selectedItem);
					isUnknown = true;
				}
				
				if (isUnknown && !Shaders.isInstalled(filename, gamedir.getSelectedFile())) {
					selectedItem = Shaders.SHADER_NONE;
				}
				
				Shaders.setShaderOptions(filename, gamedir.getSelectedFile());
			} catch (IOException e) {
				// nevermind
			}

			selectedShader = selectedItem;
			shader.setSelectedItem(selectedItem);

			modified = true;
		}
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				checkModified();
			}
			
		});
		endUpdateSection();
	}

	private void setDefaults() {
		beginUpdateSection();
		int answer = JOptionPane.showConfirmDialog(this, "This will reset all settings to their default values.", Launcher.APPLICATION_NAME + " - Question", JOptionPane.OK_CANCEL_OPTION);
		
		if (answer == JOptionPane.CLOSED_OPTION || answer == JOptionPane.CANCEL_OPTION) {
			return;
		}
		Log.info("resetting to default config");
		String workDir = Launcher.INSTANCE.getDefaultWorkDir();
		if (!workDir.equals(workingDir.getSelectedFile().getAbsolutePath())) {
			config.setWorkDir(workDir);
			updatedWorkDir();
		}
		
		javaArgs.setText(trimJavaArgs(getOptimizedJavaArgs()));
		
		setSelectedShader(Shaders.SHADER_NONE);
		endUpdateSection();
	}

	public void updatedWorkDir() {
		beginUpdateSection();
		try {
			launcherProfiles = LauncherProfiles.load(new File(workingDir.getSelectedFile(), LauncherProfiles.PROFILES_FILE));
		} catch (IOException | JSONException | JSONCodecException e) {
			launcherProfiles = null;
		}

		userSelector.init(launcherProfiles);

		
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
			String args = launcherProfiles.getJavaArgs(selectedGameConfig.getProfileName());
			javaArgs.setText(trimJavaArgs(args));
		} else {
			javaArgs.setText(trimJavaArgs(getOptimizedJavaArgs()));
		}
		
		updatedGameDir();
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
		
		
		
		
		//
		// refresh set of shaders for the given version
		//
		try {
			selectedShaderSet = Launcher.INSTANCE.getLitWRShaders(selectedGameType, selectedVariant, version.getVersion());
		} catch (Throwable e) {
			// repository inconsistent
			selectedShaderSet = null;
		}
		
		
		if (OPTIONAL_ADDONS_FEATURE) {
			optionalAddons.updatedGameDir(litwrlcfg, selectedShaderSet, gamedir, willUpgrade);
		} else {
			shader.removeAllItems();
			if (selectedShaderSet == null) {
				// shaders not supported or local repository inconsistent
				// Get into a safe state:
				shader.addItem(Shaders.SHADER_NONE);
				try {
					OptionsShaders optionsShaders = OptionsShaders.loadFromGamedir(gamedir.getSelectedFile());
					String shaderPackFileName = optionsShaders.getShaderPack();
					String shaderName = Shaders.getUnknownShaderName(shaderPackFileName);
					if (Shaders.isInstalled(shaderPackFileName, gamedir.getSelectedFile())) {
						shader.addItem(shaderName);
					} else {
						shaderName = Shaders.SHADER_NONE;
					}
					setSelectedShader(shaderName);
				} catch (IOException e) {
					setSelectedShader(Shaders.SHADER_NONE);
				}
			} else {
				for (String s : selectedShaderSet.getAvailableShaders()) {
					shader.addItem(s);
				}
				try {
					OptionsShaders optionsShaders = OptionsShaders.loadFromGamedir(gamedir.getSelectedFile());
					String shaderPackFileName = optionsShaders.getShaderPack();
					String shaderName;
					try {
						if (willUpgrade) {
							shaderName = selectedShaderSet.getNameOfUpgrade(shaderPackFileName);
						} else {
							shaderName = selectedShaderSet.getNameOf(shaderPackFileName);
						}
					} catch (IllegalArgumentException e) {
						shaderName = Shaders.getUnknownShaderName(shaderPackFileName);
						if (Shaders.isInstalled(shaderPackFileName, gamedir.getSelectedFile())) {
							shader.addItem(shaderName);
						} else {
							shaderName = Shaders.SHADER_NONE;
						}
					}
					setSelectedShader(shaderName);
				} catch (IOException e) {
					setSelectedShader(Shaders.SHADER_NONE);
				}
			}
		}
		endUpdateSection();
	}

	private String getOptimizedJavaArgs() {
		long totalMB = OS.getTotalAvailabMemorySize()/1024/1024;
		if (totalMB < 0) {
			return "-Xmx1G -XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -XX:-UseAdaptiveSizePolicy -Xmn128M";
		} else {
			// try to get 4G, 2G is good, 1G is minimum
			int heapMemory = Math.min((int)(((float)totalMB)*3.0/4.0), 2048);
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
			if (e.getActionCommand().equals("comboBoxChanged")) {
				updatedVariant(variantSelector.getSelectedVariant());
			}
		} else if (e.getSource().equals(shader)) {
			setSelectedShader((String) shader.getSelectedItem());
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
		if (OPTIONAL_ADDONS_FEATURE) {
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
		} else {
			setup = new LitWRSetupParams(selectedGameConfig, 
					new File(config.getWorkDir()), 
					gamedir.getSelectedFile(), 
					version.getVersion(), 
					version.isKeepVersion(),
					selectedGameType, 
					config.getSelectedVariant(), 
					trimJavaArgs(javaArgs.getText()), 
					selectedShader, null);
		}
		return setup;
	}



	public void setConfigurable(boolean configurable) {
		if (configurable) {
			updatedWorkDir();
		}
		workingDir.setEditable(configurable);
		gamedir.setEditable(configurable);
		javaArgs.setEditable(configurable);
		resetButton.setEnabled(configurable);
		userSelector.setEnabled(configurable);
		version.setConfigurable(configurable);
		if (OPTIONAL_ADDONS_FEATURE) {
			optionalAddons.setConfigurable(configurable);
		} else {
			shader.setEnabled(configurable);
		}
	}

	private boolean isUpdating() {
		return (updating != 0);
	}

	void beginUpdateSection() {
		this.updating += 1;
		if (OPTIONAL_ADDONS_FEATURE) optionalAddons.beginUpdateSection();
	}
	
	void endUpdateSection() {
		this.updating -= 1;
		assert(this.updating >= 0);
		if (OPTIONAL_ADDONS_FEATURE) optionalAddons.endUpdateSection();
		if (!isUpdating()) {
			checkModified();
		}
	}

}
