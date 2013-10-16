package model.listeners;

import model.map.fixtures.mobile.Unit;

import org.eclipse.jdt.annotation.Nullable;

/**
 * An interface for objects that want to know when the user selects a Unit from a
 * list or tree.
 *
 * @author Jonathan Lovelace
 *
 */
public interface UnitSelectionListener {
	/**
	 * @param unit the newly selected Unit. May be null if no selection.
	 */
	void selectUnit(@Nullable final Unit unit);
}
