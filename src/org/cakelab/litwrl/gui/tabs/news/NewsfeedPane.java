package org.cakelab.litwrl.gui.tabs.news;

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
import org.cakelab.omcl.utils.UrlConnectionUtils;
import org.cakelab.omcl.utils.log.Log;

public class NewsfeedPane extends JScrollPane implements HyperlinkListener {
	private static final long serialVersionUID = 1L;
	private JEditorPane jEditorPane;
	private static String onlineNewsUrl = "http://lifeinthewoods.ca/litwr/news/news.html";
	private static String localNewsUrl = "file:doc/offline-news/news.html";

	// set default text in case web page is not available
	private static final String newsErrorHtml = "<html>\n"
			+ "<body>\n"
			+ "<h3>Error loading newsfeed. See <a href=\""+ onlineNewsUrl + "\">Life in the Woods Renaissance</a> for news and updates.</h3>\n"
			+ "</body></html>\n";

	public static NewsfeedPane create() {
		NewsfeedPane newsfeed = new NewsfeedPane();
		return newsfeed;
	}

	private NewsfeedPane() {
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

	public void init(final boolean _online) {
		new Thread() {
			public void run() {
				boolean online = _online;
				if (online) {
					String url = onlineNewsUrl;
					try {
						// workaround to test first if the page is actually
						// available
						if (UrlConnectionUtils.testAvailabilitySilent(url)) {
							// it seems to be available ..
							jEditorPane.setPage(url);
							return;
						} else {
							online = false;
						}
					} catch (Throwable e) {
						Log.warn("Failed to load news from '" + url + "'.", e);
						online = false;
					}
				}

				//
				// Offline fallback
				//
				if (!online) {
					boolean loaded = false;
					try {
						URL url = new URL(localNewsUrl);
						File f = new File(url.getPath());
						if (f.canRead()) {
							jEditorPane.setPage(localNewsUrl);
							loaded = true;
						}
					} catch (IOException e) {
						Log.warn("Failed to load news from '" + localNewsUrl
								+ "'.", e);
					}

					if (!loaded) {
						String offlineNews = GUIResources.getURL(
								GUIResources.NEWS_PAGE).toString();
						try {
							jEditorPane.setPage(offlineNews);
						} catch (IOException e) {
							Log.warn("failed to load news from '" + offlineNews
									+ "'", e);
							try {
								jEditorPane.setPage(newsErrorHtml);
							} catch (IOException e1) {
								Log.error("failed to load error news page", e1);
							}
						}
					}
				}
			}
		}.start();
	}

	@Override
	public void hyperlinkUpdate(HyperlinkEvent event) {
		if (!event.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
			return;
		}
		ExternalBrowser.open(event.getURL());
	}
}
