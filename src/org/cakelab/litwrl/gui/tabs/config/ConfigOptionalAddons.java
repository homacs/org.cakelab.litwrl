package org.cakelab.litwrl.gui.tabs.config;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import org.cakelab.litwrl.gui.utils.FileEdit;
import org.cakelab.litwrl.gui.utils.GUIUtils;
import org.cakelab.litwrl.setup.dynamiclights.DynamicLights;
import org.cakelab.litwrl.setup.litwr.LitWRLConfig;
import org.cakelab.litwrl.setup.optifine.OptiFine;
import org.cakelab.litwrl.setup.shaders.Shaders;
import org.cakelab.litwrl.setup.shadersmod.OptionsShaders;
import org.cakelab.litwrl.setup.shadersmod.ShadersMod;
import org.cakelab.omcl.utils.log.Log;


/** This class is for future purposes (not yet active).
 * 
 */
@SuppressWarnings("serial")
public class ConfigOptionalAddons extends JPanel implements UIConfigField, ActionListener {
	private GroupLayout layout;
	private ParallelGroup labelsColumn;
	private ParallelGroup valuesColumn;
	private SequentialGroup verticalGroup;
	private JPanel configArea;
	private int updating = 0;

	private JComboBox<String> shader;
	private JCheckBox shadersMod;
	private JCheckBox optifine;
	private JCheckBox dynamicLights;
	
	private UIConfigFieldService updateService;
	private FileEdit gamedir;
	private Shaders<String> selectedShaderSet;
	private String selectedShader;
	
	/** indicates whether the user has manually removed or added optional mods */
	private boolean userCustomized;

	
	public ConfigOptionalAddons(ConfigUpdateListener listener) {
		
		updateService = new UIConfigFieldService(this);
		addConfigUpdateListener(listener);

		
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		this.setBorder(new TitledBorder("Optional Features"));
		
		addConfigArea();
		
	}

	private void addRow(JComponent label, JComponent value, String tooltip) {
		if (tooltip != null) {
			tooltip = GUIUtils.createMultilineTooltip(tooltip);
			label.setToolTipText(tooltip);
			value.setToolTipText(tooltip);
		}
		labelsColumn.addComponent(label, 0, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE);
		valuesColumn.addComponent(value, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
		verticalGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addComponent(label, 0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(value, 0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE));
	}

	private void addConfigArea() {
		configArea = new JPanel();
		configArea.setVisible(true);
		add(configArea);

		layout = new GroupLayout(configArea);
		configArea.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		labelsColumn = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
		valuesColumn = layout.createParallelGroup(GroupLayout.Alignment.LEADING);

		verticalGroup = layout.createSequentialGroup();
		

		JLabel label = new JLabel("Shader:");
		shader = new JComboBox<String>();
		shader.setEditable(false);
		shader.setEnabled(false);
		shader.addActionListener(this);
		
		addRow(label, shader, 
				"This option allows you to use supported shader packs.\n"
				+ "You can select a shader pack to be installed\n"
				+ "and used when you start the game.");

		label = new JLabel("Shaders Mod");
		shadersMod = new JCheckBox();
		shadersMod.setEnabled(false);
		shadersMod.addActionListener(this);
		addRow(label, shadersMod, "Required by shaders.");
		
		label = new JLabel("OptiFine");
		optifine = new JCheckBox();
		optifine.setEnabled(false);
		optifine.addActionListener(this);
		addRow(label, optifine, "Optimizes overall performance.\nEspecially recommended for shaders.");
		
		label = new JLabel("Dynamic Lights");
		dynamicLights = new JCheckBox();
		dynamicLights.setEnabled(false);
		dynamicLights.addActionListener(this);
		addRow(label, dynamicLights, "Adds light effects to light emitting items such as\ntorches when equipped or on the ground.");
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGroup(labelsColumn)
				.addGroup(valuesColumn));

		layout.setVerticalGroup(verticalGroup);

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		if (isUpdating()) return;

		if (e.getSource().equals(shader)) {
			setSelectedShader((String) shader.getSelectedItem());
		} else if (e.getSource().equals(shadersMod)) {
			checkUserCustomization();
			updateService.forwardConfigUpdate();
		} else if (e.getSource().equals(optifine)) {
			checkUserCustomization();
			updateService.forwardConfigUpdate();
		} else if (e.getSource().equals(dynamicLights)) {
			checkUserCustomization();
			updateService.forwardConfigUpdate();
		}
	}

	boolean isUpdating() {
		return (updating != 0);
	}
	
	void beginUpdateSection() {
		this.updating += 1;
	}

	public void endUpdateSection() {
		this.updating -= 1;
	}

	public void setConfigurable(boolean configurable) {
		shader.setEnabled(configurable);
		shadersMod.setEnabled(configurable && !Shaders.isNonStandardShader((String)shader.getSelectedItem()));
		optifine.setEnabled(configurable);
		dynamicLights.setEnabled(configurable);
	}

	@Override
	public boolean isConfigurable() {
		return shader.isEnabled();
	}

	@Override
	public void addConfigUpdateListener(ConfigUpdateListener listener) {
		updateService.addConfigUpdateListener(listener);
	}

	@Override
	public void removeConfigUpdateListener(ConfigUpdateListener listener) {
		updateService.removeConfigUpdateListener(listener);
	}

	
	public void updatedGameDir(LitWRLConfig litwrlcfg, FileEdit gamedir) {
		this.gamedir = gamedir;
		
		beginUpdateSection();
		
		shadersMod.setSelected(false);
		optifine.setSelected(false);
		dynamicLights.setSelected(false);
		if (litwrlcfg != null) {
			if (litwrlcfg.isOptionalAddonInstalled(ShadersMod.getID())) shadersMod.setSelected(true);
			if (litwrlcfg.isOptionalAddonInstalled(OptiFine.getID())) optifine.setSelected(true);
			if (litwrlcfg.isOptionalAddonInstalled(DynamicLights.getID())) dynamicLights.setSelected(true);
		}
		checkUserCustomization();
		
		endUpdateSection();
	}
	
	private void checkUserCustomization() {
		if (shadersMod.isSelected() == optifine.isSelected() && shadersMod.isSelected() == dynamicLights.isSelected()) {
			userCustomized = false;
		} else {
			userCustomized = true;
		}
	}

	void updatedShaderSet(Shaders<String> shaderSet, boolean willUpgrade) {

		this.selectedShader = null;

		this.selectedShaderSet = shaderSet;
		try {
			beginUpdateSection();
			
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
						shaderName = selectedShaderSet.getNameOfEquivalent(shaderPackFileName);
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
		} finally {
			endUpdateSection();
		}
		
	}

	void setSelectedShader(String shaderPack) {
		beginUpdateSection();
		if (selectedShader == shaderPack) {
			Log.info("shader update refused (old == new)");
			return;
		} else if (selectedShader == null || !selectedShader.equals(shaderPack)) {
			
			boolean isUnknown = false;
			try {
				String filename;
				try {
					if (selectedShaderSet != null) {
						filename = selectedShaderSet.getFilenameOf(shaderPack);
					} else {
						filename = Shaders.getUnknownShaderFileName(shaderPack);
						isUnknown = true;
					}
				} catch (IllegalArgumentException e) {
					filename = Shaders.getUnknownShaderFileName(shaderPack);
					isUnknown = true;
				}
				
				if (isUnknown && !Shaders.isInstalled(filename, gamedir.getSelectedFile())) {
					shaderPack = Shaders.SHADER_NONE;
				}
				
				Shaders.setShaderOptions(filename, gamedir.getSelectedFile());
				
			} catch (IOException e) {
				// nevermind
			}

			selectedShader = shaderPack;

			shader.setSelectedItem(shaderPack);
			
			
			if (Shaders.isNonStandardShader(selectedShader)) {
				shadersMod.setSelected(true);
				shadersMod.setEnabled(false);
				if (!userCustomized) {
					optifine.setSelected(true);
					dynamicLights.setSelected(true);
				}
			} else {
				shadersMod.setEnabled(true);
			}
			
			updateService.forwardConfigUpdate();
		} else {
			Log.info("shader update refused (old equals new)");
		}
		
		endUpdateSection();
	}

	public String getSelectedShader() {
		String selected = (String)shader.getSelectedItem();
		return selected;
	}

	public String[] getOptionalAddons() {
		ArrayList<String> optionals = new ArrayList<String>();
		if (optifine.isSelected()) optionals.add(OptiFine.getID());
		if (shadersMod.isSelected()) optionals.add(ShadersMod.getID());
		if (dynamicLights.isSelected()) optionals.add(DynamicLights.getID());
		if (Shaders.isNonStandardShader(selectedShader)) optionals.add(selectedShaderSet.getPackageDescriptor(selectedShader).getID());
		String[] a = new String[optionals.size()];
		return optionals.toArray(a);
	}

	public void setDefaults() {
		beginUpdateSection();
		setSelectedShader(Shaders.SHADER_NONE);
		optifine.setSelected(false);
		shadersMod.setSelected(false);
		dynamicLights.setSelected(false);
		endUpdateSection();
	}

}
