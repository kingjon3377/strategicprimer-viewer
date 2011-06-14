package view.map.main;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * A class to handle selecting one of a class of GUI elements.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class SelectionListener implements MouseListener {

	/**
	 * The currently-selected tile.
	 */
	private Selectable selection;
	/**
	 * @return the currently-selected item.
	 */
	protected Selectable selection() {
		return selection;
	}
	/**
	 * Set the selection, and notify both the old and the new values of the change.
	 * @param sel the new currently-selected item
	 */
	protected void setSelection(final Selectable sel) {
		if ((sel == null ? selection != null : !sel.equals(selection))) {
			if (selection != null) {
				selection.setSelected(false);
			}
			selection = sel;
			if (selection != null) {
				selection.setSelected(true);
			}
		}
	}
	/**
	 * Constructor.
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
			setSelection((Selectable) (event.getComponent()));
		}
	}

	/**
	 * Ignored.
	 * 
	 * @param event
	 *            ignored
	 */
	@Override
	public void mouseEntered(final MouseEvent event) {
		// Do nothing
	}

	/**
	 * Ignored.
	 * 
	 * @param event
	 *            ignored
	 */
	@Override
	public void mouseExited(final MouseEvent event) {
		// Do nothing
	}

	/**
	 * Ignored.
	 * 
	 * @param event
	 *            the event to handle
	 */
	@Override
	public void mousePressed(final MouseEvent event) {
		// Do nothing
	}

	/**
	 * Ignored.
	 * 
	 * @param event
	 *            the event to handle
	 */
	@Override
	public void mouseReleased(final MouseEvent event) {
		// Do nothing.
	}

	/**
	 * Clear the selection. This should be overridden to something that'll take
	 * care of clearing the view of the selection.
	 */
	public void clearSelection() {
		selection = null; // NOPMD
	}
}
