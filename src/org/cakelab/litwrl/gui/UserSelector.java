package org.cakelab.litwrl.gui;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import org.cakelab.json.JSONException;
import org.cakelab.json.codec.JSONCodecException;
import org.cakelab.omcl.setup.minecraft.AuthDB;
import org.cakelab.omcl.setup.minecraft.AuthDB.Entry;
import org.cakelab.omcl.setup.minecraft.LauncherProfiles;
import org.cakelab.omcl.utils.log.Log;

@SuppressWarnings("serial")
public class UserSelector extends JPanel implements ActionListener {

	public static class ComboBoxRenderer extends JLabel implements ListCellRenderer<AuthDB.Entry> {

		private Font standardFont;
		private Font italicFont;

		public ComboBoxRenderer(Font standardFont, Font italicFont) {
			this.standardFont = standardFont;
			this.italicFont = italicFont;
			setOpaque(true);
		}

		@Override
		public Component getListCellRendererComponent(
				JList<? extends Entry> list, Entry value, int index,
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
				String display = value.getDisplayName();
				if (display == null || display.length() == 0) {
					display = value.getUsername();
				}
				
				if (value.isDemoAccount()) {
					display = "Demo User: " + display;
				}
				
				if (display == null) {
					setFont(this.italicFont);
					display = "login on next start";
				} else {
					setFont(this.standardFont);
				}
				setText(display);
			}

			return this;
		}

	}

	private static final Entry EMPTY_ENTRY = new AuthDB.Entry("", null);

	private JComboBox<AuthDB.Entry> userList;
	private SequentialGroup horizontalGroup;
	private ParallelGroup verticalGroup;
	
	private Font standardFont;
	private Font italicFont;

	private LauncherProfiles profiles;

	public UserSelector() {
		GroupLayout layout = new GroupLayout(this);
		layout.setAutoCreateGaps(true);
		
		horizontalGroup = layout.createSequentialGroup();
		verticalGroup = layout.createParallelGroup(Alignment.CENTER);
		
		setLayout(layout);
		
		userList = new JComboBox<AuthDB.Entry>();
		standardFont = userList.getFont();
		italicFont = standardFont.deriveFont(Font.ITALIC);
		userList.setEditable(false);
		userList.setFocusable(false);
		userList.setRenderer(new ComboBoxRenderer(standardFont, italicFont));
		addRow(userList);
		
		userList.addActionListener(this);

		layout.setHorizontalGroup(horizontalGroup);
		layout.setVerticalGroup(verticalGroup);
	}
	
	private void addRow(JComponent component) {
		horizontalGroup.addComponent(component);
		verticalGroup.addComponent(component);
	}

	public void init(LauncherProfiles profiles) {
		this.profiles = profiles;
		AuthDB.Entry selectedEntry = null;
		
		userList.removeActionListener(this);
		userList.removeAllItems();
		userList.addItem(EMPTY_ENTRY);
		if (profiles != null) {
			AuthDB db = profiles.getAuthenticationDatabase();
			for (AuthDB.Entry user : db) {
				userList.addItem(user);
			}
	
			String selected = profiles.getSelectedUser();
			selectedEntry = db.get(selected);
		}
		userList.addActionListener(this);
		
		if (selectedEntry == null) selectedEntry = EMPTY_ENTRY;
		userList.setSelectedItem(selectedEntry);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == userList) {
			if (userList.getFont() != standardFont) {
				userList.setFont(standardFont);
			}
			
			Entry selectedItem = (Entry) userList.getSelectedItem();
			if (selectedItem != null) {
				if (selectedItem.equals(EMPTY_ENTRY)) {
					userList.setFont(italicFont);
				}
				
				switchUser(selectedItem);
			}
		}
	}

	private void switchUser(Entry entry) {
		if (profiles == null) return;
		
		if (entry.equals(EMPTY_ENTRY)) {
			profiles.setSelectedUser("");
		} else {
			profiles.setSelectedUser(entry.getID());
		}
		try {
			profiles.save();
		} catch (IOException | JSONCodecException | JSONException e) {
			Log.error("switching user failed: ", e);
		}
	}

	@Override
	public void setToolTipText(String text) {
		userList.setToolTipText(text);
		super.setToolTipText(text);
	}

	

}
