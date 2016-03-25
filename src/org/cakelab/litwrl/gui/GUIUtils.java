package org.cakelab.litwrl.gui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Window;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.Spring;
import javax.swing.SpringLayout;

public class GUIUtils {
	/**
	 * Centres a window on the screen.
	 * 
	 * @param frame
	 *            The window / {@link JFrame} which is centred on the screen.
	 */
	public static void centerWindow(Window frame) {
		Dimension dim = frame.getToolkit().getScreenSize();
		Rectangle abounds = frame.getBounds();
		frame.setLocation((dim.width - abounds.width) / 2, (dim.height - abounds.height) / 2);
	}

	
	public static void setIcon(Window frame, String resource) {
		
		InputStream in = GUIUtils.class.getClassLoader().getResourceAsStream(resource);
		try {
			frame.setIconImage(ImageIO.read(in));
		} catch (IOException e) {
			// So what? No image?
			e.printStackTrace();
		}

	}
	
	

    /**
     * Aligns the first <code>rows</code> * <code>cols</code>
     * components of <code>parent</code> in
     * a grid. Each component is as big as the maximum
     * preferred width and height of the components.
     * The parent is made just big enough to fit them all.
     *
     * @param rows number of rows
     * @param cols number of columns
     * @param initialX x location to start the grid at
     * @param initialY y location to start the grid at
     * @param xPad x padding between cells
     * @param yPad y padding between cells
     */
    public static void makeGrid(Container parent,
                                int rows, int cols,
                                int initialX, int initialY,
                                int xPad, int yPad) {
        SpringLayout layout;
        try {
            layout = (SpringLayout)parent.getLayout();
        } catch (ClassCastException exc) {
            System.err.println("The first argument to makeGrid must use SpringLayout.");
            return;
        }

        Spring xPadSpring = Spring.constant(xPad);
        Spring yPadSpring = Spring.constant(yPad);
        Spring initialXSpring = Spring.constant(initialX);
        Spring initialYSpring = Spring.constant(initialY);
        int max = rows * cols;

        //Calculate Springs that are the max of the width/height so that all
        //cells have the same size.
        for (int col = 0; col < cols; col++) {
            Spring maxWidthSpring = layout.getConstraints(parent.getComponent(col)).
                    getWidth();
            Spring maxHeightSpring = layout.getConstraints(parent.getComponent(col)).
                    getHeight();
        	for (int row = 0 ; row < rows; row++) {
                SpringLayout.Constraints cons = layout.getConstraints(
                        parent.getComponent(row*cols + col));

                		maxWidthSpring = Spring.max(maxWidthSpring, cons.getWidth());
                		maxHeightSpring = Spring.max(maxHeightSpring, cons.getHeight());
        	}
            for (int row = 0; row < rows; row++) {
                SpringLayout.Constraints cons = layout.getConstraints(
                                                parent.getComponent(row*cols + col));

                cons.setWidth(maxWidthSpring);
                cons.setHeight(maxHeightSpring);
            }
        }

        //Apply the new width/height Spring. This forces all the
        //components to have the same size.

        //Then adjust the x/y constraints of all the cells so that they
        //are aligned in a grid.
        SpringLayout.Constraints lastCons = null;
        SpringLayout.Constraints lastRowCons = null;
        for (int i = 0; i < max; i++) {
            SpringLayout.Constraints cons = layout.getConstraints(
                                                 parent.getComponent(i));
            if (i % cols == 0) { //start of new row
                lastRowCons = lastCons;
                cons.setX(initialXSpring);
            } else { //x position depends on previous component
                cons.setX(Spring.sum(lastCons.getConstraint(SpringLayout.EAST),
                                     xPadSpring));
            }

            if (i / cols == 0) { //first row
                cons.setY(initialYSpring);
            } else { //y position depends on previous row
                cons.setY(Spring.sum(lastRowCons.getConstraint(SpringLayout.SOUTH),
                                     yPadSpring));
            }
            lastCons = cons;
        }

        //Set the parent's size.
        SpringLayout.Constraints pCons = layout.getConstraints(parent);
        pCons.setConstraint(SpringLayout.SOUTH,
                            Spring.sum(
                                Spring.constant(yPad),
                                lastCons.getConstraint(SpringLayout.SOUTH)));
        pCons.setConstraint(SpringLayout.EAST,
                            Spring.sum(
                                Spring.constant(xPad),
                                lastCons.getConstraint(SpringLayout.EAST)));
    }


	public static JButton createIconButton(Icon icon) throws IOException {
		JButton button = new JButton();
		button.setIcon(icon);
		button.setBorder(null);
		button.setFocusable(false);
		


		button.setOpaque(false);
		button.setContentAreaFilled(false);
		button.setBorderPainted(false);


		return button;
	}


	public static String createMultilineTooltip(String tooltip) {
		if (!tooltip.trim().startsWith("<html>")) {
			tooltip = "<html>" + tooltip.replace("\n", "<br>") + "<html/>";
		} 
		return tooltip;
	}


	public static void adjustToDesktopSize(JFrame f) {
		Dimension dim = f.getSize();
		adjustToDesktopSize(dim);
		f.setSize(dim);
	}
	
	public static void adjustToDesktopSize(Dimension dim) {
		
		GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
		Rectangle bounds = env.getMaximumWindowBounds();
		
		Dimension desktopSize = bounds.getSize();
		
		if (desktopSize.height<dim.height) dim.height = desktopSize.height;
		if (desktopSize.width<dim.width) dim.width = desktopSize.width;
	}

}
