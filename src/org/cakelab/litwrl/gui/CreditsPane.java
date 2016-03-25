package org.cakelab.litwrl.gui;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;

import org.cakelab.litwrl.gui.resources.GUIResources;
import org.cakelab.omcl.gui.ExternalBrowser;
import org.cakelab.omcl.utils.log.Log;

public class CreditsPane extends JScrollPane implements HyperlinkListener {

	private static final long serialVersionUID = 1L;
	private JEditorPane jEditorPane;
	private String testCreditsUrl = "file:doc/credits/credits.html";

	
	public CreditsPane() {
		jEditorPane = new JEditorPane();
		jEditorPane.setEditable(false);
		
		jEditorPane.setContentType("text/html;charset=UTF-8");

		HTMLEditorKit kit = new HTMLEditorKit();
		jEditorPane.setEditorKit(kit);

		Document doc = kit.createDefaultDocument();
		jEditorPane.setDocument(doc);

		jEditorPane.setMinimumSize(new Dimension());

		jEditorPane.addHyperlinkListener(this);

		setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		setViewportView(jEditorPane);
	}

	
	public void init(boolean fetchFromjar) {
		String url;
		boolean loaded = false;

		if (!fetchFromjar) {
			url = testCreditsUrl;
			try {
				URL turl = new URL(url);
				File f = new File(turl.getPath());
				if (f.canRead()) {
					jEditorPane.setPage(url);
					loaded = true;
				}
			} catch (Throwable e) {
				Log.warn("Failed to load credits page from '" + url + "'.", e);
				loaded = false;
			}
		} 
		
		if (!loaded) {
			url = GUIResources.getURL(GUIResources.CREDITS_PAGE).toString();
			try {
				jEditorPane.setPage(url);
			} catch (IOException e) {
				Log.warn("Failed to load credits page from '" + url + "'.", e);
			}
		}
	}

	@Override
	public void hyperlinkUpdate(HyperlinkEvent event) {
		if (!event.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
			return;
		}
		ExternalBrowser.open(event.getURL());
	}
	
}
