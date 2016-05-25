package org.cakelab.litwrl.gui.utils;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicOptionPaneUI;

public class OptionalPane extends JDialog implements ActionListener {
	private static final long serialVersionUID = 1L;
	/**
	 * Another option type, which creates a dialog with no 
	 * standard buttons.
	 */
	public static final int BARE_OPTION = -2;
	static Dimension DEFAULT_BUTTON_SIZE = new Dimension(100,30);
	private ParallelGroup column;
	private SequentialGroup rows;
	private JButton cancelButton;
	private JButton okButton;
	private int result;
	private Icon icon;

	private static class OptionPaneUtils extends BasicOptionPaneUI {
		public OptionPaneUtils() {
			this.optionPane = new JOptionPane();
		}
		public Icon getIcon(int messageType) {
			return super.getIconForType(messageType);
		}
	}
	
	static OptionPaneUtils utils = new OptionPaneUtils();

	public OptionalPane(JFrame parent, String message, String title,
			int optionType, int messageType, 
			JComponent[] additionalComponents) {
		super(parent);
		result = JOptionPane.CLOSED_OPTION;
		
		icon = utils.getIcon(messageType);
		
		int verticalComponentGap = 30;

		setTitle(title);
		
		JPanel panel = new JPanel();
		
		GroupLayout layout = new GroupLayout(panel);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		panel.setLayout(layout);
		
		column = layout.createParallelGroup(GroupLayout.Alignment.CENTER);
		rows = layout.createSequentialGroup();
		
		rows.addGap(verticalComponentGap);
		
		//
		// add icon and text
		//
		JPanel messagePanel = new JPanel();
		messagePanel.setLayout(new BorderLayout());
		JLabel text = new JLabel();
		text.setText(formatText(message));
		messagePanel.add(text, BorderLayout.CENTER);

        if (icon != null) {
            JLabel            iconLabel = new JLabel(icon);
            iconLabel.setVerticalAlignment(SwingConstants.TOP);
            messagePanel.add(iconLabel, BorderLayout.BEFORE_LINE_BEGINS);
        }

		
		addRow(messagePanel);
		rows.addGap(verticalComponentGap);
		
		
		
		if (additionalComponents != null) {
			for (JComponent c : additionalComponents) {
				addRow(c);
				rows.addGap(verticalComponentGap);
			}
		}

		
		switch (optionType) {
		case JOptionPane.DEFAULT_OPTION:
		case JOptionPane.OK_OPTION:
			okButton = createButton("OK");
			okButton.addActionListener(this);
			addRow(okButton);
			break;
		case JOptionPane.OK_CANCEL_OPTION:
			JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout(new FlowLayout());
			cancelButton = createButton("Cancel");
			cancelButton.addActionListener(this);
			buttonPanel.add(cancelButton);
			okButton = createButton("OK");
			okButton.addActionListener(this);
			buttonPanel.add(okButton);
			addRow(buttonPanel);
			break;
		case BARE_OPTION:
			break;
		default:
			throw new IllegalArgumentException("Given OPTION not supported for now");
		}

		rows.addGap(verticalComponentGap);
		
		SequentialGroup horizontal = layout.createSequentialGroup().addGap(30).addGroup(column).addGap(30);
		
		layout.setHorizontalGroup(horizontal);
		layout.setVerticalGroup(rows);

		
		JScrollPane scroll = new JScrollPane(panel);
		scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		add(scroll);
		pack();
		
		// center window
		Dimension dim = getSize();
		GUIUtils.adjustToDesktopSize(dim);
		setSize(dim);
		GUIUtils.centerWindow(this);
		
		setAutoRequestFocus(true);
		setModal(true);

	}

	


	private String formatText(String message) {
		
		if (message.contains("\n")) {
			message = message.replace("\n", "<br/>").trim();
			if (!message.startsWith("<html>")) {
				message = "<html>" + message + "</html>";
			}
		}
		return message;
	}




	private void addRow(JComponent component) {
		column.addComponent(component);
		rows.addComponent(component);
	}

	public int getResult() {
		return result;
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(okButton)) {
			result = JOptionPane.OK_OPTION;
			setVisible(false);
		} else if (e.getSource().equals(cancelButton)) {
			result = JOptionPane.CANCEL_OPTION;
			setVisible(false);
		}
	}
	

	public void setResult(int option) {
		result = option;
	}

	public static int showOptionDialog(JFrame parent, String message,
			String title, int optionType, int messageType, JComponent[] additionalComponents) {
		OptionalPane pane = new OptionalPane(parent, message, title, optionType, messageType, additionalComponents);
		
		pane.setVisible(true);
		return pane.getResult();
	}

	public static JButton createButton(String text) {
		JButton button = new JButton(text);
		button.setPreferredSize(OptionalPane.DEFAULT_BUTTON_SIZE);
		button.setMinimumSize(OptionalPane.DEFAULT_BUTTON_SIZE);
		return button;
	}

	public static void main(String [] args) {
		String message = "Just a test.\nReally!\nAnd that is, what it should look like.";
		String title = "Test Optional Pane Dialog";
		showOptionDialog(null, 
				message, 
				title, 
				JOptionPane.DEFAULT_OPTION, 
				JOptionPane.ERROR_MESSAGE, 
				new JComponent[]{new JButton("ignore")});
		
		showMessageDialog(null, "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				+ "A very very long message this is. It is such a long message, you won't believe it!\n"
				, "Title", JOptionPane.QUESTION_MESSAGE);
		System.exit(0);
	}




	public static int showMessageDialog(JFrame parent, String message,
			String title, int type) {
		return showOptionDialog(parent, message, title, JOptionPane.DEFAULT_OPTION, type, null);
	}





	
}
