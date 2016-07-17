package org.cakelab.litwrl.gui;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.cakelab.litwrl.Launcher;
import org.cakelab.litwrl.config.Config;
import org.cakelab.litwrl.config.Variants;
import org.cakelab.litwrl.gui.utils.OptionalPane;
import org.cakelab.litwrl.setup.LitWRSetupParams;
import org.cakelab.omcl.gui.GUI;
import org.cakelab.omcl.gui.IExternalDownloadDialog;
import org.cakelab.omcl.plugins.PluginServices;
import org.cakelab.omcl.taskman.RunnableTask;
import org.cakelab.omcl.taskman.TaskMonitor;
import org.cakelab.omcl.utils.log.Log;

import com.jtattoo.plaf.acryl.AcrylLookAndFeel;

public class LitWRLGUI extends PluginAdapter implements TaskMonitor {

	private Launcher launcher;
	private MainWindow window;

	private boolean online = true;
	private boolean haveLitWRMod = true;

	
	private LitWRLGUI(Launcher launcher) {
		this.launcher = launcher;
		window = MainWindow.create(this);

	}
	
	public static LitWRLGUI create(Launcher launcher) {

		try {
			AcrylLookAndFeel.setTheme("Green-Large-Font");
			UIManager.setLookAndFeel("com.jtattoo.plaf.acryl.AcrylLookAndFeel");
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException e) {
			Log.warn("failed to set look and feel.", e);
		}
		
		LitWRLGUI gui = new LitWRLGUI(launcher);
		GUI.setInstance(gui);
		PluginServices.setListener(gui);
		return gui;
	}

	public void init(final Variants[] variants, final Config config) {
		online = !config.isOffline();
		window.init(variants, config);
		window.finishInitialisation();
	}

	public void exit(int status) {
		launcher.exit(status);
	}

	public boolean isOnline() {
		return online;
	}

	public void setOnline(boolean online) {
		this.online = online;
	}

	public boolean requestSetup(final LitWRSetupParams params) {
		new Thread("setup") {
			public void run ()  {
				final boolean success = launcher.setupLitWR(params);
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						window.setupFinished(success);
					}
					
				});
			}
		}.start();
		return false;
	}

	public boolean requestUpgrade(final LitWRSetupParams params) {
		new Thread() {
			public void run ()  {
				final boolean success = launcher.upgradeLitWR(params);
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						window.setupFinished(success);
					}
					
				});
			}
		}.start();
		return false;
	}
	
	public boolean requestModification(final LitWRSetupParams setup) {
		new Thread() {
			public void run ()  {
				final boolean success = launcher.modifyLitWR(setup);
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						window.setupFinished(success);
					}
					
				});
			}
		}.start();
		return false;
	}


	
	public boolean requestLaunch(final LitWRSetupParams setup) {
		new Thread() {
			public void run ()  {
				final boolean success = launcher.launchLitWR(setup);
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						window.launchFinished(success);
					}
					
				});
			}
		}.start();
		return false;
	}


	@Override
	public void updateProgress(long total, long current, float percent,
			String status) {
		window.updateProgress(total, current, percent, status);
		
	}

	@Override
	public void endProgress() {
		window.endProgress();
	}

	@Override
	public void startExecution(RunnableTask task) {
		window.setCurrentTask(task);
	}

	@Override
	public void finishedExecution(RunnableTask task) {
		window.setCurrentTask(null);
		window.endProgress();
	}

	@Override
	public void failedExecution(RunnableTask task, Throwable e) {
		window.taskFailed(task);
		window.endProgress();
	}

	@Override
	public void cancelledExecution(RunnableTask task) {
		// no need to inform user .. he cancelled the task
		window.setCurrentTask(null);
		window.endProgress();
	}


	@Override
	public JFrame getFrame() {
		return window;
	}
	
	public void showError(String message, String reason) {
		reason = formatMessage(reason);
		
		OptionalPane.showMessageDialog(window, message + "\n\n" + reason, Launcher.APPLICATION_NAME + " - Error", JOptionPane.ERROR_MESSAGE);
	}
	
	private String formatMessage(String reason) {
		int maxlen = 80;
		if (reason == null) return "unknown";
		
		StringBuffer formatted = new StringBuffer();
		
		for (String line : reason.split("\n")) {
			int pos = 0, len = 0;
			if (line.length() > pos) {
				len = Math.min(maxlen, line.length() - pos);
				formatted.append(line.substring(pos, pos+len));
				for (pos += len; pos < line.length(); pos += len) {
					formatted.append('\n');
					len = Math.min(maxlen, line.length() - pos);
					formatted.append(line.substring(pos, pos+len));
				}
			}
			formatted.append('\n');
		}
		return formatted.toString();
	}

	public void showWarning(String message, String reason) {
		OptionalPane.showMessageDialog(window, message + "\n\n" + reason, Launcher.APPLICATION_NAME + " - Warning", JOptionPane.ERROR_MESSAGE);
	}

	public void showInfo(String message, String reason) {
		OptionalPane.showMessageDialog(window, message + "\n\n" + reason, Launcher.APPLICATION_NAME, JOptionPane.INFORMATION_MESSAGE);
	}

	public boolean showFirstLaunchHint() {
		JCheckBox checkbox = new JCheckBox("Don't show this dialog again");

		String message = "<html>"
				+ "<h2 align=\"center\">Some note(s) before we start for the first time</h2>"
				+ "<p><ol>";
		if (!haveLitWRMod) {
			message = message
				+ "<li><b>Select \"Biomes O' Plenty\" as World Type</b>"
				+ "<p>When you create a new world you can select it under<br/>"
				+ "<br/>"
				+ "<em>\"More World Options...\" --&gt; \"World Type: Biomes O' Plenty\"</em><br/>"
				+ "<br/>"
				+ "It creates all the beautiful additional biomes you don't want to miss.</p>"
				+ "</li>";
		}
		message = message
				+ "<li><b>Be pacient when you start the game for the first time</b>"
				+ "<p>It can cost quite some time (up to a minute) when the world<br/>"
				+ "is generated for the first time. Just wait."
				+ "</p>"
				+ "</li>"
				+ "</ol></p>"
				+ "<br/><br/><p align=\"center\">Have fun!</p>"
				+ "</html>";
		
		OptionalPane.showOptionDialog(window, message, Launcher.APPLICATION_NAME, JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, new JComponent[]{checkbox});
		return checkbox.isSelected();
	}

	public boolean showShaderHint() {
		JCheckBox checkbox = new JCheckBox("Don't show this hint again");
		String message = "<html>"
				+ "<h2 align=\"center\">Do you want fancy graphics?</h2>"
				+ "<p>"
				+ "In the last column on the configuration page, you can select<br/>"
				+ "a shader which improves the looks of your game.</p>"
				+ "</html>";
		
		OptionalPane.showOptionDialog(window, message, Launcher.APPLICATION_NAME, JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, new JComponent[]{checkbox});
		return checkbox.isSelected();
	}

	public boolean showAdWarning() {
		Config config = Launcher.INSTANCE.getConfig();
		JComponent[] components;
		JCheckBox checkbox = new JCheckBox("Don't show this warning again (you can always open it via the 'Help!' button)");
		checkbox.setSelected(config.isDontShowAdWarning());
		if (!config.isDontShowAdWarning()) {
			components = new JComponent[]{checkbox};
		} else {
			components = new JComponent[0];
		}
		String message = "<html><head>"
				+ "<style>"
				+ "li {margin-top: .25cm; }"
				+ "</style>"
				+ "</head><body>"
				+ "<h2 align=\"center\">Security Guide<br/>to<br/>Advertising Sites</h2><br/>"
				+ "<p align=\"center\">"
				+ "We have to open certain advertising sites to download required software.<br/>"
				+ "Advertising sites try to trick you in order to install spyware and malware!<br/>"
				+ "<br/>"
				+ "To protect your privacy and system please follow this guideline<br/>"
				+ "when we ask you to download files through your internet browser.<br/><br/>"
				+ ""
				+ "<ol>"
				+ ""
				+ "<li style=\"font-weight: bold;\">Do not Open Downloaded Files<br/>"
				+ "<p style=\"font-weight: normal;\">"
				+ "Just save the files - we will do a security check before we do anything else with them."
				+ "</p></li>"
				+ ""
				+ "<li style=\"font-weight: bold;\"><b>Be Aware of Fake Download Buttons and Links</b><br/>"
				+ "<p style=\"font-weight: normal;\">"
				+ "Advertising sites often contain lots of fake download buttons and links. Don't click those!<br/>"
				+ "We explain below how to find the correct link."
				+ "</p></li>"
				+ ""
				+ "<li style=\"font-weight: bold;\"><b>Refresh the Page if it Asks for Cookies</b><br/>"
				+ "<p style=\"font-weight: normal;\">"
				+ "Adfly pages sometimes get stuck and ask you to enable cookies. Don't do it. Either click<br/>"
				+ "the refresh button in your browser or click our 'Download' button in the previous dialog again."
				+ "</p></li>"
				+ ""
				+ "<li style=\"font-weight: bold;\"><b>Click on \"SKIP AD\" in the Upper Right Corner</b><br/>"
				+ "<p style=\"font-weight: normal;\">"
				+ "On most advertising sites, the first page which opens shows advertisements and fake<br/>"
				+ "downloads only. In this case, there will be a button saying \"SKIP AD\" in the upper right corner,<br/>"
				+ "which gets available after 5 seconds. Click it to proceed to the page with the download link.<br/>"
				+ "Unfortunately, the next page can be another advertising page with another \"SKIP AD\" button<br/>"
				+ "Just keep clicking the \"SKIP AD\" buttons until you reach the actual download page."
				+ ""
				+ "<li style=\"font-weight: bold;\"><b>Find the Link that Matches the Exact File Name</b><br/>"
				+ "<p style=\"font-weight: normal;\">"
				+ "There can be multiple buttons and links saying 'download' or similar but only one link which<br/>"
				+ "matches the exact file name we asked for. When you hover your mouse over the link, your<br/>"
				+ "browser will tell you the address of this link in the button line of its window. Make sure that the<br/>"
				+ "end of the address matches exactly the file name we asked for. Don't worry, if you have<br/>"
				+ "downloaded the wrong file, we will detect it and ask you to try again.</p></li>"
				+ ""
				+ "<li style=\"font-weight: bold;\"><b>Ask for Help</b><br/>"
				+ "<p style=\"font-weight: normal;\">"
				+ "Those advertising sites are the worst and we totally understand if you can't find the correct<br/>"
				+ "download links. In this case just hit 'Cancel' and ask someone to help you. In the mean time<br/>"
				+ "you can turn shaders off and install/play Life in the Woods Renaissance without them."
				+ "</p></li>"
				+ ""
				+ "</ol>"
				+ "</p>"
				+ "<br/><br/><p align=\"center\">By the way:<br/>"
				+ "There are rumours about browser plugins called AdBlock .. I wonder what these are made for .."
				+ "</p>"
				+ "</body></html>";
		
		OptionalPane.showOptionDialog(window, message, Launcher.APPLICATION_NAME, JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, components);
		return checkbox.isSelected();
	}

	public boolean showUpgradeWillResetWarning() {
		Config config = Launcher.INSTANCE.getConfig();
		if (config.isDontShowUpgradeWarning()) return true;
		
		JCheckBox checkbox = new JCheckBox("Don't show this reminder again");
		String message = "<html>"
				+ "<h2 align=\"center\">Upgrade will Reset Configs and Mods!</h2>"
				+ "<p>"
				+ "<b>Note:</b> If you want to keep the version you have currently<br/>"
				+ "installed, then hit <b>Cancel</b>, go to the config tab and tick<br/>"
				+ "the radio button behind the version field.<br/>"
				+ "<br/>"
				+ "In order to upgrade to a new version of the mod-pack the<br/>"
				+ "launcher will first remove everything from the sub-folders<br/>"
				+ "<code>mods</code> and <code>config</code> in your game folder.<br/>"
				+ "<br/>"
				+ "Hit <b>Cancel</b> if you don't want to upgrade now. Otherwise click <b>OK</b><br/>"
				+ "to proceed.</p>"
				+ "</html>";
		checkbox.setSelected(config.isDontShowUpgradeWarning());
		boolean result = (JOptionPane.OK_OPTION == OptionalPane.showOptionDialog(window, message, Launcher.APPLICATION_NAME, JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, new JComponent[]{checkbox}));
		if (config.setDownShowUpgradeWarning(checkbox.isSelected()));
		return result;
	}

	@Override
	public IExternalDownloadDialog getExternalDownloadDialog() {
		return new ExternalDownloadDialog(this);
	}

	@Override
	public void toFront() {
		window.toFront();
	}

	@Override
	public void logGameOutput(String msg) {
		System.out.println(msg);
		window.logGameOutput(msg + "\n");
	}

	
}
