package model.viewer;

/**
 * A unit on the map.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class Unit implements Comparable<Unit>, TileFixture {
	/**
	 * The tile the unit is on.
	 */
	private Tile location;
	/**
	 * The player that owns the unit.
	 */
	private final Player owner;
	/**
	 * What kind of unit this is.
	 */
	private final String type;
	/**
	 * The name of this unit.
	 */
	private final String name;

	/**
	 * FIXME: We need some more members -- something about stats. What else?
	 */
	/**
	 * Constructor.
	 * 
	 * @param loc
	 *            the tile the unit is on
	 * @param unitOwner
	 *            the player that owns the unit
	 * @param unitType
	 *            the type of unit
	 * @param unitName
	 *            the name of this unit
	 */
	public Unit(final Tile loc, final Player unitOwner, final String unitType,
			final String unitName) {
		this(unitOwner, unitType, unitName);
		location = loc;
	}

	/**
	 * Constructor.
	 * 
	 * @param unitOwner
	 *            the player that owns the unit
	 * @param unitType
	 *            the type of unit
	 * @param unitName
	 *            the name of this unit
	 */
	public Unit(final Player unitOwner, final String unitType,
			final String unitName) {
		owner = unitOwner;
		type = unitType;
		name = unitName;
	}

	/**
	 * @return the tile the unit is on
	 */
	@Override
	public final Tile getLocation() {
		return location;
	}

	/**
	 * @return the player that owns the unit
	 */
	public final Player getOwner() {
		return owner;
	}

	/**
	 * @return the type of unit
	 */
	public final String getType() {
		return type;
	}

	/**
	 * @return the name of the unit
	 */
	public final String getName() {
		return name;
	}

	/**
	 * @param obj
	 *            an object
	 * @return whether it's an identical Unit.
	 */
	@Override
	public boolean equals(final Object obj) {
		return this == obj || (obj instanceof Unit
				&& ((Unit) obj).owner == owner
				&& ((type == null) ? ((Unit) obj).type == null
						: ((Unit) obj).type.equals(type))
				&& ((name == null) ? ((Unit) obj).name == null
						: ((Unit) obj).name.equals(name)));
	}

	/**
	 * @return a hash-code for the object
	 */
	@Override
	public int hashCode() {
		return ((type == null) ? 0 : type.hashCode()) + owner.hashCode() << 2 + ((name == null) ? 0
				: name.hashCode()) << 4;
	}

	/**
	 * @return a String representation of the Unit.
	 */
	@Override
	public String toString() {
		return "Unit of type " + type + ", belonging to player " + owner
				+ ", named " + name;
	}

	/**
	 * @param unit
	 *            A Unit to compare to
	 * @return the result of the comparison
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(final Unit unit) {
		return Integer.valueOf(hashCode()).compareTo(unit.hashCode());
	}
	/**
	 * @param loc the location of the unit.
	 */
	public void setLocation(final Tile loc) {
		location = loc;
	}
}
