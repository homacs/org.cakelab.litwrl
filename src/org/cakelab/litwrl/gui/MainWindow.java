package org.cakelab.litwrl.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import org.cakelab.litwrl.Launcher;
import org.cakelab.litwrl.config.Config;
import org.cakelab.litwrl.config.Variants;
import org.cakelab.litwrl.gui.footer.VariantSelector;
import org.cakelab.litwrl.gui.resources.GUIResources;
import org.cakelab.litwrl.setup.LitWRSetupParams;
import org.cakelab.omcl.setup.SetupStatus;
import org.cakelab.omcl.taskman.RunnableTask;

public class MainWindow extends JFrame implements WindowListener{
	private static final long serialVersionUID = 1L;


	private static final String TAB_GAME_LOG = "Game Log";

	
	static MainWindow INSTANCE = null;

	
	private LitWRLGUI gui;
	private NewsfeedPane newsfeed;
	private HeaderPanel header;
	private FooterPanel footer;
	private ConfigPane configPane;

	private LogPane logPane;
	private TextScrollPane gameLogPane;
	private CreditsPane credits;
	private JTabbedPane tabbedPane;
	private GuidePane guidePane;


	public static MainWindow create(LitWRLGUI gui) {
		MainWindow window = new MainWindow(gui);
		INSTANCE = window;
		window.setupWindow();
		return window;
	}

	public MainWindow(LitWRLGUI gui) {
		this.gui = gui;
	}

	private void setupWindow() {
		setTitle(Launcher.APPLICATION_NAME);

		setResizable(true);
		Dimension dim = new Dimension(800, 750);
		GUIUtils.adjustToDesktopSize(dim);
		setSize(dim);
		GUIUtils.centerWindow(this);

		
		JPanel framePanel = new BackgroundPanel();
		
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());

		header = HeaderPanel.create();
		header.setOpaque(false);
		framePanel.add(header, BorderLayout.NORTH);
		
		

		
		newsfeed = NewsfeedPane.create();
		configPane = ConfigPane.create();
		logPane = LogPane.create();
		logPane.init();

		gameLogPane = TextScrollPane.create();
		
		tabbedPane = new JTabbedPane();

		guidePane = new GuidePane();
		
		credits = new CreditsPane();
		
		tabbedPane.addTab("News", newsfeed);
		tabbedPane.addTab("Config", configPane);
		tabbedPane.addTab("Log", logPane);
		
		tabbedPane.addTab("Credits", credits);
		tabbedPane.addTab("?", guidePane);

		tabbedPane.setPreferredSize(new Dimension(0,750));
		tabbedPane.setMinimumSize(new Dimension(100,100));
		tabbedPane.setFocusable(false);

		tabbedPane.setOpaque(false);
		mainPanel.add(tabbedPane);
		
		mainPanel.setOpaque(false);
		framePanel.add(mainPanel, BorderLayout.CENTER);
		

		
		footer = FooterPanel.create(false);
		framePanel.add(footer, BorderLayout.SOUTH);

		
		
		add(framePanel);
		

		GUIUtils.setIcon(this, GUIResources.APPLICATION_ICON);
		
		setVisible(true);
		addWindowListener(this);
	}

	public void init(Variants[] variants, Config config) {
		newsfeed.init(gui.isOnline());
		footer.init(false);
		VariantSelector selector = footer.getVariantSelector();
		selector.init(variants, config);
		configPane.init(this, config, selector);
		credits.init(gui.isOnline());
		guidePane.init();
		if (config.isShowGameLog()) {
			tabbedPane.insertTab(TAB_GAME_LOG, null, gameLogPane, null, tabbedPane.indexOfComponent(credits));
			gameLogPane.setVisible(true);
			gameLogPane.init();
		} else {
			gameLogPane.setVisible(false);
		}
	}


	public void finishInitialisation() {
		configPane.loadWorkDir();
		configPane.setConfigurable(true);
		footer.setConfigurable(true);
		updateSetupStatus();
	}

	public void updateSetupStatus() {
		if (!configPane.hasValidContent()) {
			footer.setInvalidConfig();
		} else {
			SetupStatus status = Launcher.INSTANCE.getSetupStatus(configPane.getSetupParams());
			GameStatus gameStatus = GameStatus.PLAYABLE;
			if (status == null) gameStatus = GameStatus.INCONSISTENT;
			else if (status.hasUpgrade()) gameStatus = GameStatus.NEEDS_UPGRADE;
			else if (!status.isInstalled()) gameStatus = GameStatus.NEEDS_INSTALL;
			
			footer.setGameStatus(gameStatus); 
			footer.setConfigurable(true && gameStatus != GameStatus.INCONSISTENT);
		}
	}



	public void installButtonPressed() {
		LitWRSetupParams setup = configPane.getSetupParams();
		boolean configurable = gui.requestSetup(setup);
		configPane.setConfigurable(configurable);
		footer.setConfigurable(configurable);
		tabbedPane.setSelectedComponent(logPane);
		
	}
	
	public void upgradeButtonPressed() {
		LitWRSetupParams setup = configPane.getSetupParams();
		boolean configurable = gui.requestUpgrade(setup);
		configPane.setConfigurable(configurable);
		footer.setConfigurable(configurable);
		tabbedPane.setSelectedComponent(logPane);
	}


	public void setupFinished(boolean success) {
		// What to do if setup finished unsuccessful?
		
		if (success) footer.setGameStatus(GameStatus.PLAYABLE);
		// otherwise we dont change the current status of the footer
		
		configPane.setConfigurable(true);
		footer.setConfigurable(true);
	}


	public void playButtonPressed() {
		LitWRSetupParams setup = configPane.getSetupParams();
		boolean configurable = gui.requestLaunch(setup);
		configPane.setConfigurable(configurable);
		footer.setConfigurable(configurable);
		tabbedPane.setSelectedComponent(logPane);
		gameLogPane.clear();
	}
	
	public void launchFinished(boolean success) {
		this.setVisible(true);
		configPane.setConfigurable(true);
		footer.setConfigurable(true);
	}


	
	@Override
	public void windowClosing(WindowEvent arg0) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				gui.exit(0);
			}
		});
	}
	@Override
	public void windowActivated(WindowEvent arg0) {}
	@Override
	public void windowClosed(WindowEvent arg0) {}
	@Override
	public void windowDeactivated(WindowEvent arg0) {}
	@Override
	public void windowDeiconified(WindowEvent arg0) {}
	@Override
	public void windowIconified(WindowEvent arg0) {}
	@Override
	public void windowOpened(WindowEvent arg0) {}



	public void updateProgress(long total, long current, float percent,
			String status) {
		footer.updateProgress(total, current, percent, status);
	}



	public void endProgress() {
		footer.endProgress();
	}



	public void setCurrentTask(RunnableTask task) {
		footer.setCurrentTask(task);
	}



	public void taskFailed(RunnableTask task) {
		footer.setCurrentTask(null);
		gui.showError(toTitleString(task.getUserInfo()) + 
						" failed.\n", task.getDetailedErrorMessage());
			
		
	}



	protected String toTitleString(String str) {
		if (str == null) return null;
		Character firstLetter = Character.toTitleCase(str.charAt(0));
		if (str.length() == 1) {
			return firstLetter.toString();
		} else {
			return firstLetter + str.substring(1);
		}
	}



	public void logGameOutput(String msg) {
		if (gameLogPane.isVisible()) gameLogPane.append(msg);
	}

}
