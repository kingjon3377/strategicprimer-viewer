package view.map.details;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import model.map.fixtures.Grove;
import model.viewer.FixtureTransferable;
import util.ImageLoader;

/**
 * A chit to represent a grove or an orchard on a tile.
 * @author Jonathan Lovelace
 *
 */
public class GroveChit extends Chit {
	/**
	 * A description of the grove or orchard.
	 */
	private final String desc;
	/**
	 * @return a description of the grove or orchard
	 */
	@Override
	public String describe() {
		return desc;
	}
	/**
	 * Constructor.
	 * @param grove the grove or orchard this chit represents
	 * @param listener the object listening for clicks on this chit
	 */
	public GroveChit(final Grove grove, final MouseListener listener) {
		super(listener, new FixtureTransferable(grove));
		desc = "<html><p>" + (grove.isWild() ? "Wild " : "Cultivated ")
				+ grove.getTrees()
				+ (grove.isOrchard() ? " orchard" : " grove") + "</p></html>";
		final String filename = (grove.isOrchard() ? "orchard.png" : "grove.png");
		// ESCA-JAVA0177:
		Image img;
		try {
			img = ImageLoader.getLoader().loadImage(filename);
		} catch (FileNotFoundException e) {
			LOGGER.log(Level.SEVERE, filename + " not found", e);
			img = createDefaultImage();
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "I/O error reading image from " + filename, e);
			img = createDefaultImage();
		}
		image = img;
	}
	/**
	 * The image on the chit.
	 */
	private final Image image;
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(GroveChit.class.getName());
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
