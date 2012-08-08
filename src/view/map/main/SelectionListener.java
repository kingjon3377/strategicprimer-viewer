package view.map.main;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import util.PropertyChangeSource;

/**
 * A class to handle selecting one of a class of GUI elements.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class SelectionListener implements MouseListener, PropertyChangeSource {
	/**
	 * A helper object to handle property-change listeners for us.
	 */
	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	/**
	 * The currently-selected tile.
	 */
	private Selectable selection;

	/**
	 * 
	 * @return the currently-selected item.
	 */
	protected Selectable selection() {
		return selection;
	}

	/**
	 * Set the selection, and notify both the old and the new values of the
	 * change.
	 * 
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
	 * @param event the event to handle
	 */
	@Override
	public void mouseClicked(final MouseEvent event) {
		if (event.getComponent() instanceof Selectable) {
			setSelection((event.getComponent() == null) ? NullSelection.EMPTY
					: (Selectable) (event.getComponent()));
		}
	}

	/**
	 * Ignored.
	 * 
	 * @param event ignored
	 */
	@Override
	public void mouseEntered(final MouseEvent event) {
		// Do nothing
	}

	/**
	 * Ignored.
	 * 
	 * @param event ignored
	 */
	@Override
	public void mouseExited(final MouseEvent event) {
		// Do nothing
	}

	/**
	 * Ignored.
	 * 
	 * @param event the event to handle
	 */
	@Override
	public void mousePressed(final MouseEvent event) {
		// Do nothing
	}

	/**
	 * Ignored.
	 * 
	 * @param event the event to handle
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
	 * 
	 * @return the helper object managing our property-change listeners.
	 */
	protected final PropertyChangeSupport getSupport() {
		return pcs;
	}

	/**
	 * Add a property-change listener.
	 * 
	 * @param list the listener to add
	 */
	@Override
	public void addPropertyChangeListener(final PropertyChangeListener list) {
		pcs.addPropertyChangeListener(list);
	}

	/**
	 * Remove a property-change listener.
	 * 
	 * @param list the listener to remove.
	 */
	@Override
	public void removePropertyChangeListener(final PropertyChangeListener list) {
		pcs.removePropertyChangeListener(list);
	}

	/**
	 * 
	 * @return a String representation of the object.
	 */
	@Override
	public String toString() {
		return "SelectionListener";
	}
}
