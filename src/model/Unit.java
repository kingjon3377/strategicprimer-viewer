package model;
/**
 * A unit on the map.
 * @author Jonathan Lovelace
 *
 */
public class Unit {
	/**
	 * The tile the unit is on.
	 */
	private final Tile location; 
	/**
	 * The player that owns the unit.
	 */
	private final int owner; 
	/**
	 * What kind of unit this is.
	 */
	private final String type; 
	/**
	 * FIXME: We need some more members -- something about stats. What else?
	 */
	/**
	 * Constructor.
	 * @param loc the tile the unit is on
	 * @param _owner the player that owns the unit
	 * @param _type the type of unit
	 */
	public Unit(final Tile loc, final int _owner, final String _type) {
		location = loc;
		owner = _owner;
		type = _type;
	}
	/**
	 * @return the tile the unit is on
	 */
	public final Tile getLocation() {
		return location;
	}
	/**
	 * @return the player that owns the unit
	 */
	public final int getOwner() {
		return owner;
	}
	
	/**
	 * @return the type of unit
	 */
	public final String getType() {
		return type;
	}
}
