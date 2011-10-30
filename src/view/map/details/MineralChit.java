package view.map.details;

import java.awt.Graphics;
import java.awt.event.MouseListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import model.map.events.MineralEvent;
import util.ImageLoader;

/**
 * A Chit for MineralEvents.
 * @author Jonathan Lovelace
 *
 */
public class MineralChit extends EventChit {
	/**
	 * Constructor.
	 * @param event The Event this represents
	 * @param listener the listener to listen for mouse events.
	 */
	public MineralChit(final MineralEvent event, final MouseListener listener) {
		super(event, listener);
	}
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(StoneChit.class.getName()); 
	/**
	 * Paint the chit, except for the "selected" box if selected.
	 * @param pen the graphics context.
	 */
	@Override
	protected void paintChit(final Graphics pen) {
		try {
			pen.drawImage(ImageLoader.getLoader().loadImage("mineral.png"), 0, 0,
					getWidth(), getHeight(), this);
		} catch (FileNotFoundException e) {
			LOGGER.log(Level.SEVERE, "Stone icon file not found", e);
			super.paintChit(pen);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "I/O error reading stone icon file", e);
			super.paintChit(pen);
		}
	}
}
