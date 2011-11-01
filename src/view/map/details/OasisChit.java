package view.map.details;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import util.ImageLoader;

import model.map.fixtures.Oasis;
import model.viewer.FixtureTransferable;
/**
 * A chit to represent an oasis on a tile.
 * @author Jonathan Lovelace
 *
 */
public class OasisChit extends Chit {
	/**
	 * @return a description of the chit
	 */
	@Override
	public String describe() {
		return "<html><p>Oasis</p></html>";
	}
	/**
	 * Constructor.
	 * @param oasis the oasis this chit represents
	 * @param listener the object listening for clicks on this chit
	 */
	public OasisChit(final Oasis oasis, final MouseListener listener) {
		super(listener, new FixtureTransferable(oasis));
	}
	/**
	 * The image we'll use to draw the chit.
	 */
	private static Image image;
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(OasisChit.class.getName());
	static {
		try {
			image = ImageLoader.getLoader().loadImage("oasis.png");
		} catch (FileNotFoundException e) {
			LOGGER.log(Level.SEVERE, "Oasis image file not found", e);
			image = createDefaultImage();
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "I/O error reading oasis image");
			image = createDefaultImage();
		}
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
