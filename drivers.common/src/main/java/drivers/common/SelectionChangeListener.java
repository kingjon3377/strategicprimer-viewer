package drivers.common;

import org.jetbrains.annotations.Nullable;
import legacy.map.Point;
import legacy.map.fixtures.mobile.IUnit;

/**
 * An interface for objects that want to know when the selected tile, or the
 * selected unit if the app has a notion of a selected unit, changes.
 */
public interface SelectionChangeListener {
	/**
	 * The selected tile's location changed.
	 */
	void selectedPointChanged(@Nullable Point previousSelection, Point newSelection);

	/**
	 * The selected unit changed.
	 */
	void selectedUnitChanged(@Nullable IUnit previousSelection, @Nullable IUnit newSelection);

	/**
	 * The "interaction point" changed. (That is, the user right-clicked on
	 * something.) Because this the current value of the "interaction
	 * point" is not supposed to be cached, and is supposed to basically
	 * expire as soon as the action is completed, this may or may not be
	 * called when the point is set to null, and callers must get the value
	 * from where it is stored themselves.
	 */
	void interactionPointChanged();

	/**
	 * The point pointed to by the scroll-bars changed.
	 */
	void cursorPointChanged(@Nullable Point previousCursor, Point newCursor);
}
