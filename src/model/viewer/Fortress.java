package model.viewer;

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
public class Fortress implements Comparable<Fortress> { // We need something about resources and buildings yet
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

	/**
	 * @param obj
	 *            an object
	 * @return whether it is an identical fortress
	 */
	@Override
	public boolean equals(final Object obj) {
		return obj instanceof Fortress && ((Fortress) obj).name.equals(name)
				&& ((Fortress) obj).owner == owner
				&& ((Fortress) obj).units.equals(units);
	}
	
	/**
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return name.hashCode() << owner + units.hashCode();
	}
	
	/**
	 * @return a String representation of the object.
	 */
	@Override
	public String toString() {
		final StringBuilder sbuild = new StringBuilder("Fortress ");
		sbuild.append(name);
		sbuild.append(", owned by player ");
		sbuild.append(owner);
		sbuild.append(". Units:");
		for (Unit unit : units) {
			sbuild.append("\n\t\t\t");
			sbuild.append(unit);
		}
		return sbuild.toString();
	}
	/**
	 * @param fort Another fortress
	 * @return the result of a comparison with it
	 *
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(final Fortress fort) {
		return Integer.valueOf(hashCode()).compareTo(fort.hashCode());
	}
}
