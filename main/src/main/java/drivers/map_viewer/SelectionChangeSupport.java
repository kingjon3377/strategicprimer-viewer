package drivers.map_viewer;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import legacy.map.Point;
import drivers.common.SelectionChangeListener;
import drivers.common.SelectionChangeSource;

/**
 * A helper class to proxy selection-changing calls.
 */
public class SelectionChangeSupport implements SelectionChangeSource {
	/**
	 * The list of listeners to notify.
	 */
	private final Collection<SelectionChangeListener> listeners = new ArrayList<>();

	/**
	 * Notify the given listener of future selection changes.
	 */
	@Override
	public void addSelectionChangeListener(final SelectionChangeListener listener) {
		listeners.add(listener);
	}

	/**
	 * Stop notifying the given listener of selection changes.
	 */
	@Override
	public void removeSelectionChangeListener(final SelectionChangeListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Tell all listeners about a change.
	 *
	 * We don't satisfy SelectionChangeListener ourselves to avoid
	 * accidental infinite recursion.
	 */
	public void fireChanges(final @Nullable Point oldSelection, final Point newSelection) {
		for (final SelectionChangeListener listener : listeners) {
			listener.selectedPointChanged(oldSelection, newSelection);
		}
	}

	/**
	 * Tell all listeners about a change to the cursor location.
	 */
	public void fireCursorChanges(final @Nullable Point oldCursor, final Point newCursor) {
		for (final SelectionChangeListener listener : listeners) {
			listener.cursorPointChanged(oldCursor, newCursor);
		}
	}

	/**
	 * Tell all listeners about a change to the interaction point.
	 */
	public void fireInteraction() {
		for (final SelectionChangeListener listener : listeners) {
			listener.interactionPointChanged();
		}
	}
}
