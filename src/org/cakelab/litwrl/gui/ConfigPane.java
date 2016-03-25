package org.cakelab.litwrl.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.cakelab.json.JSONException;
import org.cakelab.json.codec.JSONCodecException;
import org.cakelab.litwrl.Launcher;
import org.cakelab.litwrl.config.Config;
import org.cakelab.litwrl.config.Variants;
import org.cakelab.litwrl.gui.FileEdit.FileVerifier;
import org.cakelab.litwrl.gui.footer.VariantSelector;
import org.cakelab.litwrl.gui.notification.JTextAreaChangeNotificationService;
import org.cakelab.litwrl.setup.LitWRSetupParams;
import org.cakelab.litwrl.setup.litwr.LitWRLConfig;
import org.cakelab.litwrl.setup.shaders.Shaders;
import org.cakelab.litwrl.setup.shadersmod.OptionsShaders;
import org.cakelab.omcl.config.GameConfig;
import org.cakelab.omcl.config.GameTypes;
import org.cakelab.omcl.setup.VersionStd;
import org.cakelab.omcl.setup.minecraft.LauncherProfiles;
import org.cakelab.omcl.setup.minecraft.MinecraftClient;
import org.cakelab.omcl.utils.FileSystem;
import org.cakelab.omcl.utils.OS;
import org.cakelab.omcl.utils.Regex;
import org.cakelab.omcl.utils.log.Log;

public class ConfigPane extends JPanel implements ActionListener, FileVerifier, org.cakelab.litwrl.gui.notification.DelayedNotificationReceiver {
	static final boolean DETAILS_FEATURE = false;

	private static final long serialVersionUID = 1L;

	private JTextField variant;
	private JTextField version;
	private JTextField profile;
	private FileEdit gamedir;
	private VariantSelector variantSelector;
	private JTextArea javaArgs;
	
	private Config config;
	private GameTypes selectedGameType = GameTypes.CLIENT;
	private GameConfig selectedGameConfig;
	private FileEdit workingDir;
	private LauncherProfiles launcherProfiles;
	private boolean profileExists;

	private LitWRLConfig litwrlcfg;
	private Variants selectedVariant;
	private GroupLayout layout;
	private ParallelGroup labelsColumn;
	private ParallelGroup valuesColumn;
	private SequentialGroup rows;
	private MainWindow window;
	private JComboBox<String> shader;
	private Shaders selectedShaderSet;
	private String selectedShader;
	
	private boolean validContent = false;
	private boolean modified = false;
	private boolean processActions;
	private JButton resetButton;
	private JPanel spacer;
	private JPanel configArea;
	private ConfigDetailsPanel detailsPanel;


	
	public static ConfigPane create() {
		ConfigPane pane = new ConfigPane();
		return pane;
	}

	public void init(MainWindow window, Config config, VariantSelector variantSelector) {
		this.window = window;
		this.config = config;
		this.variantSelector = variantSelector;
		variantSelector.addActionListener(this);

		String workDir = config.getWorkDir();
		if (workDir == null) {
			workDir = Launcher.INSTANCE.getDefaultWorkDir();
			config.setWorkDir(workDir);
		}
		workingDir.init(workDir, this, false);
		
		setSelectedVariant(config.getSelectedVariant());
		
		validContent = validateContent();

		new JTextAreaChangeNotificationService(this, javaArgs, 500);
	}

	
	private ConfigPane() {
		configArea = new JPanel();
		
		layout = new GroupLayout(configArea);
		configArea.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		labelsColumn = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
		valuesColumn = layout.createParallelGroup(GroupLayout.Alignment.LEADING);

		rows = layout.createSequentialGroup();
		

		JLabel label;
		
		label = new JLabel("Working directory:");
		workingDir = FileEdit.create("Select a Minecraft Working Directory");
		workingDir.setEditable(false);
		addRow(label, workingDir, "This is the minecraft working directory.\n"
				+ "You can choose your existing minecraft\n"
				+ "installation or a separate one.\n\n"
				+ "<em>We recommend to use a separate one.</em>");

		label = new JLabel("Variant:");
		variant = new JTextField(1);
		variant.setEditable(false);
		addRow(label, variant, "This is the variant of Life in the Woods Renaissance\nyou selected in the lower right corner of the window.");

		label = new JLabel("Version:");
		version = new JTextField(1);
		version.setEditable(false);
		addRow(label, version, "This is the current version of\nLife in the Woods Renaissance.");
		
		label = new JLabel("Profile:");
		profile = new JTextField(1);
		profile.setEditable(false);
		addRow(label, profile, "This is the name of the Minecraft launcher\n"
				+ "profile to be used for the selected variant.");
		
		label = new JLabel("Game directory:");
		gamedir = FileEdit.create("Select a Game Directory");
		gamedir.setEditable(false);
		addRow(label, gamedir, "This is the game directory for the selected\n"
				+ "Life in the Woods Renaissance variant.");

		label = new JLabel("JVM arguments:");
		javaArgs = new JTextArea(2,0);
		javaArgs.setLineWrap(true);
		javaArgs.setWrapStyleWord(true);
		javaArgs.setBorder(BorderFactory.createLineBorder(Color.gray));
		javaArgs.setEditable(false);
		addRow(label, javaArgs, "These are the command line arguments for\nthe Java VM running the Minecraft client.");
		
		
		addShadersSection();

		addDetailsSection();
		
		addResetSection();
		
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGroup(labelsColumn)
				.addGroup(valuesColumn));

		layout.setVerticalGroup(rows);

		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		add(configArea);
		
		spacer = new JPanel();
		Dimension dim = new Dimension(0,3000);
		spacer.setPreferredSize(dim);
		add(spacer);
	}
	
	private void addDetailsSection() {
		if (DETAILS_FEATURE) {
			JLabel label = new JLabel("Details");
			detailsPanel = new ConfigDetailsPanel();
			addRow(label, detailsPanel, "Allows to configure the details of visual enhancements.");
		}
	}



	private void addResetSection() {
		JLabel label = new JLabel();
		JPanel resetPanel = new JPanel();
		resetButton = new JButton("Reset");
		resetButton.setToolTipText("<html>This button resets all settings<br/>to their default values.</html>");
		Dimension buttonSize = new Dimension(100, 30);
		resetButton.setPreferredSize(buttonSize);
		resetButton.addActionListener(this);
		FlowLayout resetLayout = new FlowLayout(FlowLayout.RIGHT, 0, 10);
		resetPanel.setLayout(resetLayout);
		resetPanel.add(new JLabel());
		resetPanel.add(resetButton);
		addRow(label, resetPanel, null);
	}



	private void addShadersSection() {
		JLabel label = new JLabel("Shader:");
		
		shader = new JComboBox<String>();
		selectedShader = Shaders.SHADER_NONE;
		shader.addItem(selectedShader);
		shader.setEditable(false);
		shader.setEnabled(false);
		shader.addActionListener(this);
		
		addRow(label, shader, 
				"This option allows you to use shader packs.\n"
				+ "You can select a shader pack to be installed\n"
				+ "and used when you start the game.\n"
				+ "\n"
				+ "Please note, that this requires you to download\n"
				+ "certain files manually. But don't worry, we will\n"
				+ "guide you to the right pages to do so.");
	}

	

	private void addRow(JComponent label, JComponent value, String tooltip) {
		if (tooltip != null) {
			tooltip = GUIUtils.createMultilineTooltip(tooltip);
			label.setToolTipText(tooltip);
			value.setToolTipText(tooltip);
		}
		labelsColumn.addComponent(label);
		valuesColumn.addComponent(value, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
		rows.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(label)
				.addComponent(value));
	}

	private void setSelectedVariant(Variants selectedVariant) {
		this.selectedVariant = selectedVariant;
		config.setSelectedVariant(selectedVariant);
		variant.setText(selectedVariant.toString());
		loadWorkDir();
		checkModified();
	}
	
	private void setSelectedShader(String selectedItem) {
		if (selectedShader != selectedItem) {
			selectedShader = selectedItem;
			setProcessActions(false);
			shader.setSelectedItem(selectedItem);
			setProcessActions(true);
			
			try {
				String filename;
				try {
					if (selectedShaderSet != null) {
						filename = selectedShaderSet.getFilenameOf(selectedItem);
					} else {
						filename = Shaders.getUnknownShaderFileName(selectedItem);
					}
				} catch (IllegalArgumentException e) {
					filename = Shaders.getUnknownShaderFileName(selectedItem);
				}
				Shaders.setShaderOptions(filename, gamedir.getSelectedFile());
			} catch (IOException e) {
				// nevermind
			}
			
			modified = true;
		}
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				checkModified();
			}
			
		});
	}

	private void setDefaults() {
		
		int answer = JOptionPane.showConfirmDialog(this, "This will reset all settings to their default values.", Launcher.APPLICATION_NAME + " - Question", JOptionPane.OK_CANCEL_OPTION);
		
		if (answer == JOptionPane.CLOSED_OPTION || answer == JOptionPane.CANCEL_OPTION) {
			return;
		}
		Log.info("resetting to default config");
		String workDir = Launcher.INSTANCE.getDefaultWorkDir();
		if (!workDir.equals(workingDir.getSelectedFile().getAbsolutePath())) {
			config.setWorkDir(workDir);
			loadWorkDir();
		}
		
		if (profileExists) {
			gamedir.init(createDefaultGamePath(), this, false);
		}
		
		javaArgs.setText(trimJavaArgs(getOptimizedJavaArgs()));
		
		setSelectedShader(Shaders.SHADER_NONE);
		checkModified();
	}

	
	
	
	protected void loadWorkDir() {
		setProcessActions(false);
		try {
			launcherProfiles = LauncherProfiles.load(new File(workingDir.getSelectedFile(), LauncherProfiles.PROFILES_FILE));
			this.profileExists = true;
		} catch (IOException | JSONException | JSONCodecException e) {
			this.profileExists = false;
		}

		selectedGameConfig = config.getGameConfig(selectedGameType, selectedVariant);
		if (selectedGameConfig == null) {
			selectedGameConfig = config.addGameConfig(selectedGameType, selectedVariant);
		}

		String profileName = selectedGameConfig.getProfileName();
		profile.setText(profileName);

		if (profileExists) {
			profileExists = launcherProfiles.exists(profileName);
			if (profileExists) {
				launcherProfiles.setSelectProfile(profileName);
			}
		}
		
		
		if (profileExists) {
			String dir = launcherProfiles.getGameDir(profileName);
			gamedir.init(dir, this, false);
		} else {
			gamedir.init(createDefaultGamePath(), this, false);
		}
		
		
		litwrlcfg = null;
		try {
			litwrlcfg = LitWRLConfig.loadFromGameDir(gamedir.getSelectedFile());
		} catch (IOException | JSONCodecException e) {
			// nevermind .. we'll fix it below
		}
		
		
		String latestVersion = Launcher.INSTANCE.getLatestLitWRVersion(selectedGameType, selectedVariant);
		if (latestVersion == null) latestVersion = "0.0.0";
		if (litwrlcfg == null) {
			version.setText(latestVersion);
		} else {
			VersionStd latest = VersionStd.decode(latestVersion);
			VersionStd current = VersionStd.decode(litwrlcfg.getVersion());
			if (latest.isGreaterThan(current)) {
				version.setText(latest.toString());
			} else {
				version.setText(current.toString());
			}
		}


		if (profileExists) {
			String args = launcherProfiles.getJavaArgs(selectedGameConfig.getProfileName());
			javaArgs.setText(trimJavaArgs(args));
		} else {
			javaArgs.setText(trimJavaArgs(getOptimizedJavaArgs()));
		}


		//
		// shaders
		//
		shader.removeAllItems();
		try {
			selectedShaderSet = Launcher.INSTANCE.getLitWRShaders(selectedGameType, selectedVariant, version.getText());
		} catch (Throwable e) {
			// repository inconsistent
			selectedShaderSet = null;
		}
		if (selectedShaderSet == null) {
			// shaders not supported or local repository inconsistent
			// Get into a safe state:
			shader.addItem(Shaders.SHADER_NONE);
			try {
				OptionsShaders optionsShaders = OptionsShaders.loadFromGamedir(gamedir.getSelectedFile());
				String shaderPackFileName = optionsShaders.getShaderPack();
				String shaderName = Shaders.getUnknownShaderName(shaderPackFileName);
				shader.addItem(shaderName);
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
				try { 
					setSelectedShader(selectedShaderSet.getNameOf(shaderPackFileName));
				} catch (IllegalArgumentException e) {
					String shaderName = Shaders.getUnknownShaderName(shaderPackFileName);
					shader.addItem(shaderName);
					setSelectedShader(shaderName);
				}
			} catch (IOException e) {
				setSelectedShader(Shaders.SHADER_NONE);
			}
		}
		setProcessActions(true);
	}



	private String getOptimizedJavaArgs() {
		long totalMB = OS.getTotalAvailabMemorySize()/1024/1024;
		if (totalMB < 0) {
			return "-Xmx1G -XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -XX:-UseAdaptiveSizePolicy -Xmn128M";
		} else {
			// try to get 4G, 2G is good, 1G is minimum
			int heapMemory = Math.min((int)(((float)totalMB)*3.0/4.0), 4096);
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
		}		
		
		if (needsStatusUpdate || modified) {
			window.updateSetupStatus();
			modified = false;
		}
		
	}






	private boolean validateContent() {
		return validateWorkDir(workingDir.getSelectedFile());
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
		if (!isProcessActions()) return;
		
		if (e.getSource().equals(variantSelector)) {
			if (e.getActionCommand().equals("comboBoxChanged")) {
				setSelectedVariant(variantSelector.getSelectedVariant());
			}
		} else if (e.getSource().equals(shader)) {
			setSelectedShader((String) shader.getSelectedItem());
		} else if (e.getSource().equals(resetButton)) {
			setDefaults();
		}
	}



	private boolean validateWorkDir(File wd) {
		boolean valid = true;
		
		if (FileSystem.isChildOf(wd, gamedir.getSelectedFile())) {
			workingDir.setErrorMessage("The working directory cannot be located inside the game directory.");
			valid = false;
		}
		
		if (valid && !FileSystem.hasAccessibleParent(wd)) {
			valid = false;
			workingDir.setErrorMessage("Selected working directory cannot be created\nor does not allow to store files in it.");
		}
		
		try {
			if (valid && wd.exists() && Files.isSameFile(wd.toPath(), FileSystem.getUserHome().toPath())) {
				valid = false;
				workingDir.setErrorMessage("Don't do this!\nYou probably meant '" + new File(FileSystem.getUserHome(), ".minecraft").toString() + "'");
			}
		} catch (IOException e) {
			e.printStackTrace();
			valid = false;
		}
		
		if (valid && wd.exists() && new File(wd, MinecraftClient.SUBDIR_MODS).exists()) {
			valid = false;
			workingDir.setErrorMessage("This minecraft installation already contains mods.\nYou need to choose another directory.");
		}
		
		
		return valid;
	}
	

	@Override
	public boolean verify(FileEdit folderEdit) {
		if (folderEdit.equals(workingDir)) {
			File wd = workingDir.getSelectedFile();
			boolean isValid = validateWorkDir(wd);
			
			if (isValid) {
				config.setWorkDir(wd.toString());
				checkModified();
				loadWorkDir();
			} else {
				setValidContent(false);
				return false;
			}
		}
		return true;
	}


	public LitWRSetupParams getSetupParams() {
		LitWRSetupParams setup = new LitWRSetupParams(selectedGameConfig, 
				new File(config.getWorkDir()), 
				gamedir.getSelectedFile(), 
				version.getText(), 
				selectedGameType, 
				config.getSelectedVariant(), 
				trimJavaArgs(javaArgs.getText()), 
				selectedShader);
		return setup;
	}



	public void setConfigurable(boolean configurable) {
		if (configurable) {
			loadWorkDir();
		}
		workingDir.setEditable(configurable);
		javaArgs.setEditable(configurable);
		shader.setEnabled(configurable);
	}



	@Override
	public void delayedNotification(JComponent component) {
		if (component.equals(javaArgs)) {
			if (profileExists) {
				String args = trimJavaArgs(javaArgs.getText());
				launcherProfiles.setJavaArgs(this.selectedGameConfig.getProfileName(), args);
				checkModified();
			}
		}
	}

	private boolean isProcessActions() {
		return processActions;
	}

	private void setProcessActions(boolean processActions) {
		this.processActions = processActions;

		if (DETAILS_FEATURE) detailsPanel.setProcessActions(processActions);
	}

}
