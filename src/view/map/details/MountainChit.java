package view.map.details;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import model.map.events.Mountain;
import model.viewer.FixtureTransferable;
import util.LoadFile;

/**
 * A chit to represent a mountain on a tile.
 * @author Jonathan Lovelace
 *
 */
public class MountainChit extends Chit {
	/**
	 * @return a description of the mountain.
	 */
	@Override
	public String describe() {
		return "<html><p>Mountain.</p></html>";
	}
	/**
	 * Constructor.
	 * @param mountain the mountain this chit represents
	 * @param listener the object listening for clicks on this chit
	 */
	public MountainChit(final Mountain mountain, final MouseListener listener) {
		super(listener, new FixtureTransferable(mountain));
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
			image = ImageIO.read(new LoadFile().doLoadFileAsStream("mountain.png"));
		} catch (FileNotFoundException e) {
			LOGGER.log(Level.SEVERE, "Mountain image file not found", e);
			image = createImage();
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "I/O error reading mountain image");
			image = createImage();
		}
	}
	/**
	 * Create a backup image.
	 * @return an image of a mountain.
	 */
	private static Image createImage() {
		final BufferedImage temp = new BufferedImage(24, 24, BufferedImage.TYPE_INT_ARGB);
		final Graphics pen = temp.createGraphics();
		pen.setColor(Color.orange);
		// ESCA-JAVA0076:
		pen.fillPolygon(new int[] { 0, 12, 24 }, new int[] { 24, 0, 24 }, 3);
		return temp;
	}
	/**
	 * Paint the chit.
	 * @param pen the graphics context.
	 */
	@Override
	public void paint(final Graphics pen) {
		pen.drawImage(image, 0, 0, getWidth(), getHeight(), this);
		super.paint(pen);
	}
}
