package org.cakelab.litwrl.gui.footer;

import java.awt.Component;
import java.awt.Font;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.cakelab.litwrl.config.Variants;
import org.cakelab.litwrl.config.Config;

public class VariantSelector extends JComboBox<Variants> {
	private static final long serialVersionUID = 1L;
	
	private Variants selected;

	public VariantSelector() {
		setEditable(false);
		setRenderer(new ComboBoxRenderer());
	}

	public void init(Variants[] variants, Config config) {
		setFont(getFont().deriveFont(Font.BOLD));
		for (Variants variant : variants) {
			addItem(variant);
		}

		this.selected = config.getSelectedVariant();

		setSelectedItem(selected);
		setFocusable(false);
	}

	public Variants getSelectedVariant() {
		return (Variants) getSelectedItem();
	}

	class ComboBoxRenderer extends JLabel implements ListCellRenderer<Variants> {
		private static final long serialVersionUID = 1L;

		public ComboBoxRenderer() {
			setOpaque(true);
			setHorizontalAlignment(CENTER);
			setVerticalAlignment(CENTER);
		}

		/*
		 * This method finds the image and text corresponding to the selected
		 * value and returns the label, set up to display the text and image.
		 */
		@Override
		public Component getListCellRendererComponent(
				JList<? extends Variants> list, Variants value, int index,
				boolean isSelected, boolean cellHasFocus) {
			// Get the selected index. (The index param isn't
			// always valid, so just use the value.)

			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}

			if (value != null) {
				setText(value.toString());
//				setFont(list.getFont());
			}

			return this;
		}

	}

}
