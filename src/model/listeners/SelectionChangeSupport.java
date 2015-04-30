package model.listeners;

import java.util.ArrayList;
import java.util.List;

import model.map.Point;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A helper class to proxy selection-changing calls.
 *
 * @author Jonathan Lovelace
 *
 */
public final class SelectionChangeSupport implements SelectionChangeSource {
	/**
	 * The list of listeners to notify.
	 */
	private final List<SelectionChangeListener> listeners = new ArrayList<>();

	/**
	 * @param list a listener to add
	 */
	@Override
	public void addSelectionChangeListener(final SelectionChangeListener list) {
		listeners.add(list);
	}

	/**
	 * @param list a listener to remove
	 */
	@Override
	public void removeSelectionChangeListener(final SelectionChangeListener list) {
		listeners.remove(list);
	}

	/**
	 * Tell all listeners about a change. All in one like this rather than
	 * implementing {@link SelectionChangeListener} to prevent accidental
	 * infinite recursion.
	 *
	 * @param oldPoint the previously selected location
	 * @param newPoint the newly selected location.
	 */
	public void fireChanges(@Nullable final Point oldPoint,
			final Point newPoint) {
		for (final SelectionChangeListener list : listeners) {
			list.selectedPointChanged(oldPoint, newPoint);
		}
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "SelectionChangeSupport";
	}
}
