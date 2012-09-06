package model.map.fixtures.towns;

import model.map.events.EventKind;

/**
 * An abandoned, ruined, or burned-out town.
 *
 * @author Jonathan Lovelace
 *
 */
public final class TownEvent extends AbstractTownEvent {
	/**
	 * Constructor.
	 *
	 * @param tStatus The status of the town
	 * @param tSize The size of the town
	 * @param discdc The DC to discover it.
	 * @param tName the name of the town, fortress, or city
	 * @param idNum the ID number.
	 */
	public TownEvent(final TownStatus tStatus, final TownSize tSize,
			final int discdc, final String tName, final int idNum) {
		super(EventKind.Town, tStatus, tSize, tName);
		dc = discdc;
		id = idNum;
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
}
