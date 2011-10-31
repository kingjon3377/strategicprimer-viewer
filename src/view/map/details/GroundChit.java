package view.map.details;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import model.map.fixtures.Ground;
import model.viewer.FixtureTransferable;
import util.ImageLoader;
/**
 * A chit to represent ground.
 * @author Jonathan Lovelace
 *
 */
public class GroundChit extends Chit {
	/**
	 * A description of the forest.
	 */
	private final String desc;
	/**
	 * Constructor.
	 * @param ground the ground this chit represents
	 * @param listener the object listening for clicks on this chit.
	 */
	public GroundChit(final Ground ground, final MouseListener listener) {
		super(listener, new FixtureTransferable(ground));
		desc = "<html><p>" + ground.getDescription()
				+ (ground.isExposed() ? " (exposed)" : " (not exposed)")
				+ "</p></html>";
	}
	/**
	 * @return a description of the forest.
	 */
	@Override
	public String describe() {
		return desc;
	}
	/**
	 * The image we'll use to draw the chit.
	 */
	private static Image image;
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(ForestChit.class.getName());
	static {
		try {
			image = ImageLoader.getLoader().loadImage("trees.png");
		} catch (FileNotFoundException e) {
			LOGGER.log(Level.SEVERE, "Forest image file not found", e);
			image = createImage();
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "I/O error reading forest image");
			image = createImage();
		}
	}
	/**
	 * Create a backup image.
	 * @return an image of a tree.
	 */
	private static Image createImage() {
		final BufferedImage temp = new BufferedImage(24, 24, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D pen = temp.createGraphics();
		pen.setColor(Color.green);
		// ESCA-JAVA0076:
		pen.fillPolygon(new int[] { 8, 12, 16 }, new int[] { 24, 0, 24 }, 3);
		return temp;
	}
	/**
	 * Paint the chit.
	 * @param pen the graphics context.
	 */
	@Override
	public void paint(final Graphics pen) {
		super.paint(pen);
		pen.drawImage(image, 0, 0, getWidth(), getHeight(), this);
	}
}

