package model.listeners;

import java.util.EventListener;

import model.map.fixtures.mobile.Unit;

/**
 * An interface for things that want to accept a new user-created unit.
 *
 * @author Jonathan Lovelace
 */
public interface NewUnitListener extends EventListener {
	/**
	 * Add the new unit.
	 *
	 * @param unit the unit to add
	 */
	void addNewUnit(final Unit unit);
}
