package view.map.details;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import util.ImageLoader;

import model.map.fixtures.Mine;
import model.viewer.FixtureTransferable;

/**
 * A chit to represent a mine.
 * @author Jonathan Lovelace
 *
 */
public class MineChit extends Chit {
	/**
	 * A description of the mine.
	 */
	private final String desc;
	/**
	 * @return a description of the mine
	 */
	@Override
	public String describe() {
		return desc;
	}
	/**
	 * Constructor.
	 * @param mine the mine this chit represents
	 * @param listener the object listening for clicks on this chit.
	 */
	public MineChit(final Mine mine, final MouseListener listener) {
		super(listener, new FixtureTransferable(mine));
		desc = "<html><p>" + mine.toString() + "</p></html>";
	}
	/**
	 * The image that represents a mine.
	 */
	private static Image image;
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(MineChit.class.getName());
	static {
		try {
			image = ImageLoader.getLoader().loadImage("mine.png");
		} catch (FileNotFoundException e) {
			LOGGER.log(Level.SEVERE, "Mine image file not found", e);
			image = createDefaultImage();
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "I/O error reading mine image");
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
