package org.cakelab.litwrl.gui.notification;

import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class JTextAreaChangeNotificationService extends DelayedNotificationService implements DocumentListener {

	private String originalText;
	private JTextArea textArea;

	public JTextAreaChangeNotificationService(DelayedNotificationReceiver receiver,
			JTextArea textArea, int ms) {
		super(receiver, textArea, ms);
		this.textArea = textArea;
		originalText = textArea.getText();
		textArea.getDocument().addDocumentListener(this);
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		receivedEvent();
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		receivedEvent();
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		receivedEvent();
	}

	@Override
	protected boolean isModified(JComponent component) {
		if ((!textArea.getText().equals(originalText))) {
			originalText = textArea.getText();
			return true;
		}
		return false;
	}

}
