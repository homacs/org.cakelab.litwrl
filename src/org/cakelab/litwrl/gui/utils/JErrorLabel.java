package org.cakelab.litwrl.gui.utils;

import java.awt.Color;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JToolTip;

@SuppressWarnings("serial")
public class JErrorLabel extends JLabel {
	public JErrorLabel() {
		super();
	}

	public JErrorLabel(Icon image, int horizontalAlignment) {
		super(image, horizontalAlignment);
	}

	public JErrorLabel(Icon image) {
		super(image);
	}

	public JErrorLabel(String text, Icon icon, int horizontalAlignment) {
		super(text, icon, horizontalAlignment);
	}

	public JErrorLabel(String text, int horizontalAlignment) {
		super(text, horizontalAlignment);
	}

	public JErrorLabel(String text) {
		super(text);
	}

	public class ErrorToolTip extends JToolTip {
		ErrorToolTip(JErrorLabel component) {
			super();
	        setComponent(component);
	        setBackground(Color.black);
	        setForeground(Color.red);
		}
	}

	@Override
    public JToolTip createToolTip() {
        return (new ErrorToolTip(this));
    }
	
}
