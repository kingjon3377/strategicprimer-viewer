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
	 * Version UID for serialization.
	 */
	private static final long serialVersionUID = 1L;
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
		super(TownKind.City, tStatus, tSize, tName, player);
		dc = discdc;
		id = idNum;
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
}
