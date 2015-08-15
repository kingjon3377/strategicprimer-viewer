package model.map.fixtures.towns;

import model.map.Player;

/**
 * An abandoned, ruined, or burned-out city.
 *
 * @author Jonathan Lovelace
 *
 */
public final class City extends AbstractTown {
	/**
	 * Constructor.
	 *
	 * @param tStatus The status of the city
	 * @param tSize The size of the city
	 * @param discdc The DC to discover it.
	 * @param tName the name of the town, fortress, or city
	 * @param idNum the ID number.
	 * @param player the owner of the city
	 */
	public City(final TownStatus tStatus, final TownSize tSize,
			final int discdc, final String tName, final int idNum,
			final Player player) {
		super(tStatus, tSize, tName, player);
		dc = discdc;
		id = idNum;
	}

	/**
	 * TODO: Should we "zero out" the name or owner?
	 * @return a copy of this city
	 * @param zero whether to zero out the DC
	 */
	@Override
	public City copy(final boolean zero) {
		City retval;
		if (zero) {
			retval = new City(status(), size(), 0, getName(), id, getOwner());
		} else {
			retval = new City(status(), size(), dc, getName(), id, getOwner());
		}
		retval.setImage(getImage());
		return retval;
	}
	/**
	 * The DC to discover the city. TODO: Should perhaps be mutable.
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
	 * @return a string describing all cities as a class
	 */
	@Override
	public String plural() {
		return "Cities";
	}
	/**
	 * @return that this is a city
	 */
	@Override
	public String kind() {
		return "city";
	}
}
