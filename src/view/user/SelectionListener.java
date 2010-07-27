package view.user;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.logging.Logger;

/**
 * A class to keep track of which tile is selected.
 * @author Jonathan Lovelace
 */
public class SelectionListener implements MouseListener {
	/**
	 * The currently-selected tile.
	 */
	private GUITile selection = null;
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(SelectionListener.class.getName());
	/**
	 * Handle mouse clicks.
	 * @param event the event to handle
	 */
	@Override
	public void mouseClicked(final MouseEvent event) {
		if (event.getComponent() instanceof GUITile) {
			if (selection != null) {
				selection.setSelected(false);
			}
			selection = (GUITile) event.getComponent();
			selection.setSelected(true);
			LOGGER.fine("Click");
		}
	}
	/**
	 * Ignored
	 * @param event ignored
	 */
	@Override
	public void mouseEntered(final MouseEvent event) {
		// Do nothing
	}

	/**
	 * Ignored
	 * @param event ignored
	 */
	@Override
	public void mouseExited(final MouseEvent event) {
		// Do nothing
	}

	/**
	 * Ignored
	 * @param event ignored
	 */
	@Override
	public void mousePressed(final MouseEvent event) {
		// Do nothing
	}

	/**
	 * Ignored
	 * @param event ignored
	 */
	@Override
	public void mouseReleased(final MouseEvent event) {
		// Do nothing
	}

}
