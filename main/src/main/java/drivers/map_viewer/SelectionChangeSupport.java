package drivers.map_viewer;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import common.map.Point;
import drivers.common.SelectionChangeListener;
import drivers.common.SelectionChangeSource;

/**
 * A helper class to proxy selection-changing calls.
 */
public class SelectionChangeSupport implements SelectionChangeSource {
	/**
	 * The list of listeners to notify.
	 */
	private final List<SelectionChangeListener> listeners = new ArrayList<>();

	/**
	 * Notify the given listener of future selection changes.
	 */
	@Override
	public void addSelectionChangeListener(SelectionChangeListener listener) {
		listeners.add(listener);
	}

	/**
	 * Stop notifying the given listener of selection changes.
	 */
	@Override
	public void removeSelectionChangeListener(SelectionChangeListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Tell all listeners about a change.
	 *
	 * We don't satisfy SelectionChangeListener ourselves to avoid
	 * accidental infinite recursion.
	 */
	public void fireChanges(@Nullable Point oldSelection, Point newSelection) {
		for (SelectionChangeListener listener : listeners) {
			listener.selectedPointChanged(oldSelection, newSelection);
		}
	}

	/**
	 * Tell all listeners about a change to the cursor location.
	 */
	public void fireCursorChanges(@Nullable Point oldCursor, Point newCursor) {
		for (SelectionChangeListener listener : listeners) {
			listener.cursorPointChanged(oldCursor, newCursor);
		}
	}

	/**
	 * Tell all listeners about a change to the interaction point.
	 */
	public void fireInteraction() {
		for (SelectionChangeListener listener : listeners) {
			listener.interactionPointChanged();
		}
	}
}
