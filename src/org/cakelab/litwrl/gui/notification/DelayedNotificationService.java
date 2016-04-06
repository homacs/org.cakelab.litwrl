package org.cakelab.litwrl.gui.notification;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.Timer;

/**
 * Calls {@link DelayedNotificationReceiver#delayedNotification()} after 
 * given amount of milliseconds when {@link #receivedEvent()} was
 * called milliseconds earlier.
 * 
 * @author homac
 *
 */
public abstract class DelayedNotificationService  implements ActionListener {
	private Timer timer;
	private DelayedNotificationReceiver receiver;
	private JComponent component;
	private volatile boolean enabled;
	private int ms;
	private long nextTime;

	public DelayedNotificationService(DelayedNotificationReceiver receiver, JComponent component, int ms) {
		this.receiver = receiver;
		this.component = component;
		this.ms = ms;
		this.enabled = false;
		this.nextTime = Long.MAX_VALUE;
		this.timer = new Timer(ms, this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(timer)) {
			long now = System.currentTimeMillis();
			if (now >= nextTime) {
				if (isModified(component)) {
					receiver.delayedNotification(component);
				}
				nextTime = Long.MAX_VALUE;
			}
		}
	}

	protected abstract boolean isModified(JComponent component);

	protected void receivedEvent() {
		if (enabled) {
			nextTime = System.currentTimeMillis() + ms;
			timer.restart();
		}
	}


	public void setEnabled(boolean enabled) {
		if (enabled == this.enabled) return;
		
		if (enabled) {
			timer.start();
		} else {
			timer.stop();
			nextTime = Long.MAX_VALUE;
		}
		
		this.enabled = enabled;
	}
	

}
