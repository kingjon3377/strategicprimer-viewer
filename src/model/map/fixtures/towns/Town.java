package model.map.fixtures.towns;

import model.map.Player;

/**
 * An abandoned, ruined, or burned-out town.
 *
 * @author Jonathan Lovelace
 *
 */
public final class Town extends AbstractTown {
	/**
	 * Constructor.
	 *
	 * @param tStatus The status of the town
	 * @param tSize The size of the town
	 * @param discdc The DC to discover it.
	 * @param tName the name of the town, fortress, or city
	 * @param idNum the ID number.
	 * @param player the owner of the town
	 */
	public Town(final TownStatus tStatus, final TownSize tSize,
			final int discdc, final String tName, final int idNum,
			final Player player) {
		super(tStatus, tSize, tName, player);
		dc = discdc;
		id = idNum;
	}

	/**
	 * TODO: Should we "zero out" the name or owner?
	 * @return a copy of this town
	 * @param zero whether to zero out the DC
	 */
	@Override
	public Town copy(final boolean zero) {
		Town retval;
		if (zero) {
			retval = new Town(status(), size(), 0, getName(), id, getOwner());
		} else {
			retval = new Town(status(), size(), dc, getName(), id, getOwner());
		}
		retval.setImage(getImage());
		return retval;
	}
	/**
	 * The DC to discover the town. TODO: Should perhaps be mutable.
	 */
	private final int dc; // NOPMD

	/**
	 *
	 * @return the DC to discover the event.
	 */
	@Override
	public int getDC() {
		return dc;
	}

	/**
	 * ID number.
	 */
	private final int id; // NOPMD

	/**
	 * @return a UID for the fixture.
	 */
	@Override
	public int getID() {
		return id;
	}

	/**
	 * @return a string describing all towns as a class
	 */
	@Override
	public String plural() {
		return "Towns";
	}
	/**
	 * @return that this is a town
	 */
	@Override
	public String kind() {
		return "town";
	}
}
