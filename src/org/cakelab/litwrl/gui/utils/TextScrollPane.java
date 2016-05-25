package org.cakelab.litwrl.gui.utils;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

public class TextScrollPane extends JPanel {
	private static final long serialVersionUID = 1L;

	public static TextScrollPane create() {
		TextScrollPane logpane = new TextScrollPane();
		
		return logpane;
	}



	private JTextArea textarea;
	private JScrollPane scrollpane;
	
	protected TextScrollPane() {
		this.setLayout(new BorderLayout());
		
		textarea = new JTextArea();
		textarea.setEditable(false);
		textarea.setLineWrap(true);
		textarea.setWrapStyleWord(true);
		scrollpane = new JScrollPane(textarea);
		scrollpane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		add(scrollpane);
	}
	
	
	public void init() {
	}


	public void append(String text) {
		appendLater(text);
	}


	protected void appendLater(final String text) {
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				textarea.append(text);
			}
		});
	}


	public void clear() {
		textarea.setText("");
	}


}
