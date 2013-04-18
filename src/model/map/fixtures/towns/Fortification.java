package model.map.fixtures.towns;

import model.map.Player;


/**
 * An abandoned, ruined, or burned-out fortification.
 *
 * FIXME: We want this to share a tag, and model code, with Fortress.
 *
 * @author Jonathan Lovelace
 *
 */
public final class Fortification extends AbstractTown {
	/**
	 * Version UID for serialization.
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Constructor.
	 *
	 * @param tStatus The status of the fortification
	 * @param tSize The size of the fortification
	 * @param discdc The DC to discover it.
	 * @param tName the name of the town, fortress, or city
	 * @param idNum the ID number.
	 * @param player the owner of the fortification
	 */
	public Fortification(final TownStatus tStatus, final TownSize tSize,
			final int discdc, final String tName, final int idNum,
			final Player player) {
		super(TownKind.Fortification, tStatus, tSize, tName, player);
		dc = discdc;
		id = idNum;
	}

	/**
	 * The DC to discover the fortification. TODO: Should perhaps be mutable.
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
	 * @return the name of an image to represent the event.
	 */
	@Override
	public String getImage() {
		return "fortification.png";
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
