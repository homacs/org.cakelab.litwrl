package org.cakelab.litwrl.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.cakelab.litwrl.gui.resources.GUIResources;
import org.cakelab.omcl.gui.ExternalBrowser;
import org.cakelab.omcl.utils.log.Log;

/**
 * Just some space to show a header
 * @author homac
 *
 */
public class HeaderPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;

	public static HeaderPanel create() {
		HeaderPanel header = new HeaderPanel();
		return header;
	}


	
	
	private JButton logo;

	
	private HeaderPanel() {

		logo = new JButton();
		try {
			logo = GUIUtils.createIconButton(GUIResources.asIcon(GUIResources.LOGO_200_IMAGE));
			logo.setToolTipText("http://lifeinthewoods.ca");
			logo.addActionListener(this);
			add(logo);
		} catch (IOException e) {
			Log.warn("Loading header image failed.", e);
		}
		
		Dimension dim = new Dimension(0, 200);
		setPreferredSize(dim);
		setMinimumSize(dim);
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(logo)) {
			SwingUtilities.invokeLater(new Runnable(){public void run() {
				ExternalBrowser.open("http://lifeinthewoods.ca");	
			}});
		}
	}


}
