package view.map.details;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import util.ImageLoader;

import model.map.fixtures.Shrub;
import model.viewer.FixtureTransferable;

/**
 * A chit to represent a shrub.
 * @author Jonathan Lovelace
 *
 */
public class ShrubChit extends Chit {
	/**
	 * @return a description of the shrub
	 */
	@Override
	public String describe() {
		return desc;
	}
	/**
	 * A description of the shrub.
	 */
	private final String desc;
	/**
	 * Constructor.
	 * @param shrub the shrub this chit represents
	 * @param listener the object listening for clicks on this chit
	 */
	public ShrubChit(final Shrub shrub, final MouseListener listener) {
		super(listener, new FixtureTransferable(shrub));
		desc = "<html><p>" + shrub.getDescription() + "</p></html>";
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
			image = ImageLoader.getLoader().loadImage("shrub.png");
		} catch (FileNotFoundException e) {
			LOGGER.log(Level.SEVERE, "Ground image file not found", e);
			image = createDefaultImage();
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "I/O error reading ground image");
			image = createDefaultImage();
		}
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
