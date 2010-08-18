package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A fortress on the map. A player can only have one fortress per tile, but
 * multiple players may have fortresses on the same tile.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class Fortress { // We need something about resources and buildings yet
	/**
	 * The player that owns the fortress.
	 */
	private final int owner;
	/**
	 * The name of the fortress.
	 */
	private final String name;
	/**
	 * The tile the fortress is on.
	 */
	private final Tile tile;
	/**
	 * The units in the fortress.
	 */
	private final List<Unit> units; // Should this be a Set?

	/**
	 * Csontructor.
	 * 
	 * @param _tile
	 *            the tile the fortress is on
	 * @param _owner
	 *            the player that owns the fortress
	 * @param _name
	 *            the name of the fortress
	 */
	public Fortress(final Tile _tile, final int _owner, final String _name) {
		tile = _tile;
		owner = _owner;
		name = _name;
		units = new ArrayList<Unit>();
	}

	/**
	 * @return the units in the fortress.
	 */
	public final List<Unit> getUnits() {
		return Collections.unmodifiableList(units);
	}

	/**
	 * Add a unit to the fortress.
	 * 
	 * @param unit
	 *            the unit to add
	 */
	public final void addUnit(final Unit unit) {
		units.add(unit);
	}

	/**
	 * Remove a unit from the fortress.
	 * 
	 * @param unit
	 *            the unit to remove
	 */
	public final void removeUnit(final Unit unit) {
		units.remove(unit);
	}

	/**
	 * @return the player that owns the fortress
	 */
	public final int getOwner() {
		return owner;
	}

	/**
	 * @return the tile the fortress is on
	 */
	public final Tile getTile() {
		return tile;
	}

	/**
	 * @return the name of the fortress
	 */
	public final String getName() {
		return name;
	}
}
