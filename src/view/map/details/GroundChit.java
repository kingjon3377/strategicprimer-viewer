package view.map.details;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseListener;
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
		desc = "<html><p>" + ground.getKind()
				+ (ground.isExposed() ? " (exposed)" : " (not exposed)")
				+ "</p></html>";
		// ESCA-JAVA0177:
		Image img;
		try {
			img = ImageLoader.getLoader().loadImage("expground.png");
		} catch (FileNotFoundException e) {
			LOGGER.log(Level.SEVERE, "Ground image file not found", e);
			img = createDefaultImage(ground);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "I/O error reading ground image");
			img = createDefaultImage(ground);
		}
		image = img;
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
	private final Image image;
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(GroundChit.class.getName());
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

