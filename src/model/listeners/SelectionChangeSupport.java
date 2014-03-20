package model.listeners;

import java.util.ArrayList;
import java.util.List;

import model.map.ITile;
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
	 * @param newPoint the newly selected location. If null, we won't fire point
	 *        notifications at all
	 * @param oldTile the previously selected tile
	 * @param newTile the newly selected tile. If null, we won't fire tile
	 *        notifications at all.
	 */
	public void fireChanges(@Nullable final Point oldPoint,
			@Nullable final Point newPoint, @Nullable final ITile oldTile,
			@Nullable final ITile newTile) {
		for (final SelectionChangeListener list : listeners) {
			if (newPoint != null) {
				list.selectedPointChanged(oldPoint, newPoint);
			}
			if (newTile != null) {
				list.selectedTileChanged(oldTile, newTile);
			}
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
