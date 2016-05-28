package org.cakelab.litwrl.gui.tabs.config;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.cakelab.litwrl.gui.footer.VariantSelector;
import org.cakelab.litwrl.gui.utils.FileEdit;
import org.cakelab.litwrl.gui.utils.FileEdit.FileVerifier;
import org.cakelab.litwrl.gui.utils.GUIUtils;
import org.cakelab.litwrl.gui.utils.notification.DelayedNotificationReceiver;

public abstract class ConfigPaneUIElements extends JPanel implements ActionListener, FileVerifier, DelayedNotificationReceiver, ConfigUpdateListener {
	// TODO: remove when done
	static final boolean DETAILS_FEATURE = false;

	private static final long serialVersionUID = 1L;

	protected VariantSelector variantSelector;

	protected UserSelector userSelector;
	protected FileEdit workingDir;
	protected JTextField variant;
	protected VersionSelectorField version;
	protected JTextField profile;
	protected FileEdit gamedir;
	protected JTextArea javaArgs;
	protected JComboBox<String> shader;
	protected JButton resetButton;
	protected ConfigOptionalPanel detailsPanel;

	private GroupLayout layout;
	private ParallelGroup labelsColumn;
	private ParallelGroup valuesColumn;
	private SequentialGroup rows;
	private JPanel spacer;
	private JPanel panel;

	
	public ConfigPaneUIElements() {
		panel = new JPanel();
		layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		labelsColumn = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
		valuesColumn = layout.createParallelGroup(GroupLayout.Alignment.LEADING);

		rows = layout.createSequentialGroup();
		

		JLabel label;
		
		label = new JLabel("User:");
		userSelector = new UserSelector();
		addRow(label, userSelector,   "Here you can switch between your\n"
									+ "Minecraft accounts. Select\n"
									+ "`login on next start`\n"
									+ "if you want to add another account.");
		
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
		version = new VersionSelectorField(this);
		version.setEnabled(false);
		addRow(label, version, "This shows the version of the Life in the Woods\n"
							 + "Renaissance mod-pack which is installed or will\n"
							 + "be installed. You can choose to keep an old \n"
							 + "version or select one of the available versions\n"
							 + "for a fresh install.");
		
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
		add(panel);
		
		spacer = new JPanel();
		Dimension dim = new Dimension(0,3000);
		spacer.setPreferredSize(dim);
		add(spacer);
	}
	
	private void addDetailsSection() {
		if (DETAILS_FEATURE) {
			JLabel label = new JLabel("Details");
			detailsPanel = new ConfigOptionalPanel();
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

}
