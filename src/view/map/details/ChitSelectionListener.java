package view.map.details;

import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;

import view.map.main.SelectionListener;

/**
 * A class to handle selecting Chits.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class ChitSelectionListener extends SelectionListener {
	/**
	 * Constructor.
	 * 
	 * @param list a listener to tell when the selected chit changes
	 */
	public ChitSelectionListener(final PropertyChangeListener list) {
		super();
		addPropertyChangeListener(list);
	}

	/**
	 * Handle mouse clicks.
	 * 
	 * @param event
	 *            the event to handle
	 */
	@Override
	public void mouseClicked(final MouseEvent event) {
		super.mouseClicked(event);
		getSupport().firePropertyChange("chit", null, selection());
	}

	/**
	 * Clear the selection.
	 */
	@Override
	public void clearSelection() {
		super.clearSelection();
		getSupport().firePropertyChange("chit", null, new Object());
	}
}
