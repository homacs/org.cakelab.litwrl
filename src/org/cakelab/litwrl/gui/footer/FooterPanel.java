package org.cakelab.litwrl.gui.footer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.cakelab.litwrl.gui.GameStatus;
import org.cakelab.litwrl.gui.MainWindow;
import org.cakelab.litwrl.gui.utils.GUIUtils;
import org.cakelab.omcl.taskman.RunnableTask;

public class FooterPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;

	
	enum State {
		Play,
		Install,
		Upgrade,
		Apply,
		Initialising, 
		Invalid, 
	}

	public static FooterPanel create(boolean enabled) {
		FooterPanel footer = new FooterPanel(enabled);
		return footer;
	}


	private JProgressBar progressbar;
	private JButton launchButton;
	private VariantSelector variantSelector;
	private State state;
	
	
	private FooterPanel(boolean enabled) {
		state = State.Initialising;
		setLayout(new BorderLayout());
		
		launchButton = new JButton();
		launchButton.setFont(launchButton.getFont().deriveFont(Font.BOLD));
		add(launchButton, BorderLayout.CENTER);
		
		progressbar = new JProgressBar();
		progressbar.setMinimum(0);
		progressbar.setMaximum(100);
		Dimension dim = new Dimension(200,50);
		progressbar.setMinimumSize(dim);
		progressbar.setMaximumSize(dim);
		progressbar.setPreferredSize(dim);
		add(progressbar, BorderLayout.WEST);
		
		variantSelector = new VariantSelector();
		variantSelector.setToolTipText(GUIUtils.createMultilineTooltip("Here you can select one of the variants\n"
				+ "of Life in the Woods Renaissance\n"
				+ "you'd like to play: In 'Hungry'\n"
				+ "you will be .. well .. more hungry :D"));
		dim = new Dimension(200,50);
		variantSelector.setMinimumSize(dim);
		variantSelector.setMaximumSize(dim);
		variantSelector.setPreferredSize(dim);
		add(variantSelector, BorderLayout.EAST);
		
		setConfigurable(enabled);
		
	}

	
	public void init(boolean enabled) {
		launchButton.addActionListener(this);
		setConfigurable(enabled);
	}
	

	public VariantSelector getVariantSelector() {
		return this.variantSelector;
	}

	public void updateProgress(long total, long current, float percent,
			String status) {
		progressbar.setValue((int)(percent*100));
	}

	public void endProgress() {
		progressbar.setValue((int)0);
	}
	
	
	public void setGameStatus(GameStatus gameStatus) {
		switch(gameStatus) {
		case PLAYABLE:
			state = State.Play;
			break;
		case NEEDS_INSTALL:
			state = State.Install;
			break;
		case NEEDS_UPGRADE:
			state = State.Upgrade;
			break;
		case NEEDS_MODIFICATION:
			state = State.Apply;
			break;
		case INCONSISTENT:
			state = State.Invalid;
			break;
		}
		setConfigurable(launchButton.isEnabled());
	}



	
	public void setConfigurable(boolean enabled) {
		
		launchButton.setEnabled(enabled);
		variantSelector.setEnabled(enabled);
		
		if (!enabled) {
			switch (state) {
			case Play:
				launchButton.setText("running ..");
				break;
			case Install:
				launchButton.setText("installing ..");
				break;
			case Upgrade:
				launchButton.setText("upgrading ..");
				break;
			case Apply:
				launchButton.setText("applying changes ..");
				break;
			case Initialising:
				launchButton.setText("initialising ..");
				break;
			case Invalid:
				launchButton.setText("can't install/update");
				break;
			}
		} else {
			switch(state) {
			case Play:
				launchButton.setText(State.Play.name());
				break;
			case Install:
				launchButton.setText(State.Install.name());
				break;
			case Apply:
				launchButton.setText("Apply Changes");
				break;
			case Upgrade:
				launchButton.setText(State.Upgrade.name());
				break;
			case Invalid:
				launchButton.setText("can't install/update");
				break;
			default:
				assert("invalid state in footer" == null);
			}
		}
	}
	

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(launchButton)) {
			switch(state) {
			case Play:
				MainWindow.INSTANCE.playButtonPressed();
				break;
			case Install:
				MainWindow.INSTANCE.installButtonPressed();
				break;
			case Upgrade:
				MainWindow.INSTANCE.upgradeButtonPressed();
				break;
			case Apply:
				MainWindow.INSTANCE.applyButtonPressed();
				break;
			default:
				throw new IllegalArgumentException("Footer panel in inconsistent state.");
			}
		}
		
	}


	public void setCurrentTask(RunnableTask task) {
		if (task != null) {
			launchButton.setText(task.getUserInfo() + " ..");
		} else {
			setConfigurable(launchButton.isEnabled());
		}
	}


	public void setInvalidConfig() {
		state = State.Invalid;
		setConfigurable(false);
	}


	
	
	
}
