package view.map.details;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import model.map.events.Forest;
import model.viewer.FixtureTransferable;
import util.LoadFile;
/**
 * A chit to represent a forest on a tile.
 * @author Jonathan Lovelace
 *
 */
public class ForestChit extends Chit {
	/**
	 * A description of the forest.
	 */
	private final String desc;
	/**
	 * Constructor.
	 * @param forest the forest this chit represents
	 * @param listener the object listening for clicks on this chit.
	 */
	public ForestChit(final Forest forest, final MouseListener listener) {
		super(listener, new FixtureTransferable(forest));
		desc = "<html><p>" + forest.toString() + "</p></html>";
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
	private static BufferedImage image;
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(ForestChit.class.getName());
	static {
		try {
			image = ImageIO.read(new LoadFile().doLoadFileAsStream("trees.png"));
		} catch (FileNotFoundException e) {
			LOGGER.log(Level.SEVERE, "Forest image file not found", e);
			image = null;
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "I/O error reading forest image");
			image = null;
		}
	}
	/**
	 * Paint the chit.
	 * @param pen the graphics context.
	 */
	@Override
	public void paint(final Graphics pen) {
		super.paint(pen);
		if (image == null) {
			final Color save = pen.getColor();
			pen.setColor(Color.green);
			pen.fillPolygon(new int[] { getWidth() / 3, getWidth() / 2,
					getWidth() * 2 / 3 }, new int[] { getHeight(), 0,
					getHeight() }, 3);
			pen.setColor(save);
		} else {
			pen.drawImage(image, 0, 0, getWidth(), getHeight(), this);
		}
	}
}
