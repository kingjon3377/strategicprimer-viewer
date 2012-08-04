package view.map.details;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import model.map.HasImage;
import model.map.TileFixture;
import model.viewer.FixtureTransferable;
import util.ImageLoader;

/**
 * A simple Chit, for most cases, so we don't have to have a new Chit subclass
 * for every kind of Fixture.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class SimpleChit extends Chit {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(SimpleChit.class.getName());
	/**
	 * Constructor.
	 * @param fix the fixture this chit is to represent
	 * @param listener the listener to tell about mouse events.
	 */
	public SimpleChit(final TileFixture fix, final MouseListener listener) {
		super(listener, new FixtureTransferable(fix));
		desc = fix.toString();
		// ESCA-JAVA0177:
		Image localImage;
		if (fix instanceof HasImage) {
			try {
				localImage = ImageLoader.getLoader().loadImage(((HasImage) fix).getImage());
			} catch (FileNotFoundException e) {
				LOGGER.log(Level.SEVERE, "image file images/" + (((HasImage) fix).getImage()) + " not found");
				localImage = createDefaultImage(fix);
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, "I/O error reading image");
				localImage = createDefaultImage(fix);
			}
			image = localImage;
		} else {
			image = createDefaultImage(fix);
		}
	}
	/**
	 * An image to use to represent the fixture.
	 */
	private final Image image;
	/**
	 * A description of the fixture this chit represents.
	 */
	private final String desc;
	/**
	 * @return a description of the fixture
	 */
	@Override
	public String describe() {
		return desc;
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
