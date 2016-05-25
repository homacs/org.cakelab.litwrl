package org.cakelab.litwrl.gui.utils.notification;

import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class JTextFieldChangeNotificationService extends
		DelayedNotificationService implements DocumentListener {

	private JTextField textField;
	private String originalText;

	public JTextFieldChangeNotificationService(
			DelayedNotificationReceiver receiver, JTextField component, int ms) {
		super(receiver, component, ms);
		textField = component;
		originalText = textField.getText();
		textField.getDocument().addDocumentListener(this);
	}

	@Override
	protected boolean isModified(JComponent component) {
		if (!originalText.equals(textField.getText())) {
			originalText = textField.getText();
			return true;
		}
		return false;
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		super.receivedEvent();
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		super.receivedEvent();
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		super.receivedEvent();
	}


}
