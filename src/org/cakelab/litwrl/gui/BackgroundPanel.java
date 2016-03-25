package org.cakelab.litwrl.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.cakelab.litwrl.gui.resources.GUIResources;

public class BackgroundPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;


	protected static final int ANIMATION_SPEED = 60;
	
	private BufferedImage bg;
	private Dimension imageSize = new Dimension();
	
	private Timer timer;


	private int offset_times_2;
	private int direction;


	private BufferedImage bgHalfStep;


	private URL image_resource;


	public BackgroundPanel() {
		try {
			image_resource = GUIResources.getURL(GUIResources.randomPanoramaImage());
			bg = ImageIO.read(image_resource);

		} catch (IOException e) {
			// this is not critical and happens only during development
			e.printStackTrace();
		}
		updateWidth(bg.getWidth(this));
		updateHeight(bg.getHeight(this));
		
		setLayout(new BorderLayout());
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				init();
			}
		});
			
		
	}

	protected void init() {
		
		
		setDoubleBuffered(true);
		
		direction = 1;
		offset_times_2 = 0;
		timer = new Timer(ANIMATION_SPEED, BackgroundPanel.this);
		timer.setInitialDelay(ANIMATION_SPEED);
		timer.start();


		try {
			bgHalfStep = ImageIO.read(image_resource);
			Graphics2D g2d = bgHalfStep.createGraphics();
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
					RenderingHints.VALUE_RENDER_QUALITY);
			g2d.drawImage(bg, AffineTransform.getTranslateInstance(0.5,  0), BackgroundPanel.this);
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_DEFAULT);
			g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
					RenderingHints.VALUE_RENDER_DEFAULT);
		} catch (IOException e) {
			// happens only during development and is not critical
			e.printStackTrace();
		}


		
	}

	@Override
	public boolean imageUpdate(Image img, int infoflags, int x, int y,
			int width, int height) {
		if (0 != (infoflags & WIDTH)) {
			updateWidth(width);
		}
		if (0 != (infoflags & HEIGHT)) {
			updateHeight(height);
		}
		return true;
	}

	private void updateHeight(int height) {
		synchronized (imageSize) {
			imageSize.height = height;
		}
	}

	private void updateWidth(int width) {
		synchronized (imageSize) {
			imageSize.width = width;
		}
	}

	public Component add(JComponent comp) {
		comp.setOpaque(false);
		return super.add(comp);
	}

	@Override
	public void paintComponent(Graphics g) {
		int maxX = getSize().getSize().width;

		Graphics2D g2d = (Graphics2D) g;
		if (offset_times_2%2 == 0) {
			for (int x = offset_times_2/2 - imageSize.width; x < maxX; x += imageSize.width ) {
				g2d.drawImage(bg, x,  0, this);
			}
		} else {
			for (int x = offset_times_2/2 - imageSize.width; x < maxX; x += imageSize.width ) {
				g2d.drawImage(bgHalfStep, x,  0, this);
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == timer) {
			offset_times_2 += direction;
			if (offset_times_2 >= imageSize.getWidth()*2) offset_times_2 = 0;
			repaint();
		}
	}
	

}
