package view.map;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
/**
 * A class to handle selecting one of a class of GUI elements.
 * @author Jonathan Lovelace
 *
 */
public class SelectionListener implements MouseListener {

	/**
	 * The currently-selected tile.
	 */
	protected Selectable selection;
	/**
	 * Constructor
	 */
	public SelectionListener() {
		super();
		selection = null; // NOPMD
	}

	/**
	 * Handle mouse clicks.
	 * 
	 * @param event
	 *            the event to handle
	 */
	@Override
	public void mouseClicked(final MouseEvent event) {
		if (event.getComponent() instanceof Selectable) {
			if (selection != null) {
				selection.setSelected(false);
			}
			selection = (Selectable) event.getComponent();
			selection.setSelected(true);
		}
	}

	/**
	 * Ignored
	 * 
	 * @param event
	 *            ignored
	 */
	@Override
	public void mouseEntered(final MouseEvent event) {
		// Do nothing
	}

	/**
	 * Ignored
	 * 
	 * @param event
	 *            ignored
	 */
	@Override
	public void mouseExited(final MouseEvent event) {
		// Do nothing
	}

	/**
	 * Ignored
	 * 
	 * @param event
	 *            the event to handle
	 */
	@Override
	public void mousePressed(final MouseEvent event) {
		// Do nothing
	}

	/**
	 * Ignored
	 * 
	 * @param event
	 *            the event to handle
	 */
	@Override
	public void mouseReleased(final MouseEvent event) {
		// Do nothing.
	}

}