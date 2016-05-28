package org.cakelab.litwrl.gui.tabs.config;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import org.cakelab.litwrl.gui.utils.GUIUtils;


/** This class is for future purposes (not yet active).
 * 
 */
@SuppressWarnings("serial")
public class ConfigDetailsPanel extends JPanel implements ActionListener {
	private GroupLayout layout;
	private ParallelGroup labelsColumn;
	private ParallelGroup valuesColumn;
	private SequentialGroup rows;
	private JPanel configArea;
	private JButton showHideButton;
	private JCheckBox shadersModButton;
	private JCheckBox optifineButton;
	private JCheckBox dynamicLights;
	private boolean processActions;

	public ConfigDetailsPanel () {
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		this.setBorder(new TitledBorder("Video Options"));
		showHideButton = new JButton("Show");
		Dimension buttonSize = new Dimension(100, 30);
		showHideButton.setPreferredSize(buttonSize);
		showHideButton.addActionListener(this);
		add(showHideButton);
		
		
		addConfigArea();
		

		JPanel spacer = new JPanel();
		Dimension dim = new Dimension(0,3000);
		spacer.setPreferredSize(dim);
		add(spacer);
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


	private void addConfigArea() {
		configArea = new JPanel();
		configArea.setVisible(false);
		add(configArea);

		layout = new GroupLayout(configArea);
		configArea.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		labelsColumn = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
		valuesColumn = layout.createParallelGroup(GroupLayout.Alignment.LEADING);

		rows = layout.createSequentialGroup();
		

		JLabel label = new JLabel();

		
		shadersModButton = new JCheckBox("Shaders Mod");
		addRow(label, shadersModButton, "Required for shaders.");
		
		optifineButton = new JCheckBox("OptiFine");
		addRow(label, optifineButton, "Optimizes performance especially recommended for shaders.");
		
		dynamicLights = new JCheckBox("Dynamic Lights Mod");
		addRow(label, dynamicLights, "Adds light effects to torches.");
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGroup(labelsColumn)
				.addGroup(valuesColumn));

		layout.setVerticalGroup(rows);

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (!processActions) return;
		
		if (e.getSource().equals(showHideButton)) {
			showHideButton.setText(configArea.isVisible()?"Show":"Hide");
			configArea.setVisible(!configArea.isVisible());
		}
		
	}

	public void setProcessActions(boolean processActions) {
		this.processActions = processActions;
	}
}
