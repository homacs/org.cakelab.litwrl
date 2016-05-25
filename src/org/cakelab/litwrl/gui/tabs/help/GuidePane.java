package org.cakelab.litwrl.gui.tabs.help;

import java.awt.Dimension;
import java.io.IOException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Element;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import org.cakelab.litwrl.gui.resources.GUIResources;
import org.cakelab.omcl.gui.ExternalBrowser;
import org.cakelab.omcl.utils.log.Log;

public class GuidePane extends JScrollPane implements HyperlinkListener {

	private static final long serialVersionUID = 1L;
	private JEditorPane jEditorPane;
	private URL baseUrl;

	
	public GuidePane() {
		jEditorPane = new JEditorPane();
		jEditorPane.setEditable(false);

		HTMLEditorKit kit = new HTMLEditorKit();
		jEditorPane.setEditorKit(kit);

		// this document will be replaced later when the content has been loaded lazily
		jEditorPane.setDocument(kit.createDefaultDocument());

		jEditorPane.setMinimumSize(new Dimension());

		jEditorPane.addHyperlinkListener(this);

		setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		setViewportView(jEditorPane);
	}

	
	public void init() {

		baseUrl = GUIResources.getURL(GUIResources.GUIDE_PAGE);
		try {
			jEditorPane.setPage(baseUrl);
		} catch (IOException e) {
			Log.warn("Failed to load getting started guide from '" + baseUrl + "'.", e);
		}
	}

	@Override
	public void hyperlinkUpdate(HyperlinkEvent event) {
		if (!event.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
			return;
		}
		URL url = event.getURL();
		if (isPageLocalUrl(url)) {
			String elemId = getElementId(url.toString());
			if (elemId != null) {
				HTMLDocument doc = (HTMLDocument) jEditorPane.getDocument();
				Element element = doc.getElement(elemId);
				if (element != null) {
					jEditorPane.setCaretPosition(element.getStartOffset());
				} else {
					Log.info("elem " + elemId + " not found in " + baseUrl);
				}
			} else {
				Log.warn("unexpected local reference in guide pane: " + url);
			}
			
		} else {
			ExternalBrowser.open(event.getURL());
		}
	}


	private boolean isPageLocalUrl(URL url) {
		return url.toString().startsWith(baseUrl.toString());
	}


	private String getElementId(String href) {
		String id = null;
		if (href.contains("#")) {
			id = href.substring(href.indexOf("#") + 1, href.length());
		}
		return id;
	}

}
