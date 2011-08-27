package view.map.main;

import java.awt.Graphics;
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
		if (!sel.equals(selection)) {
				selection.setSelected(false);
			selection = sel;
				selection.setSelected(true);
		}
	}
	/**
	 * Constructor.
	 */
	public SelectionListener() {
		super();
		selection = NullSelection.EMPTY;
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
			setSelection(event.getComponent() == null ? NullSelection.EMPTY
					: (Selectable) (event.getComponent()));
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
		selection = NullSelection.EMPTY;
	}
	/**
	 * A class to replace all cases where a Selectable variable is null.
	 * @author Jonathan Lovelace
	 */
	public static final class NullSelection extends Selectable {
		/**
		 * Singleton.
		 */
		public static final NullSelection EMPTY = new NullSelection();
		/**
		 * Singleton.
		 */
		private NullSelection() {
			super();
		}
		/**
		 * Don't waste resources doing any painting.
		 * @param pen ignored
		 */
		@Override
		// ESCA-JAVA0025:
		public void paint(final Graphics pen) {
			// Do nothing.
		}
	}
}
