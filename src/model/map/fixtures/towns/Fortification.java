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
		super(tStatus, tSize, tName, player);
		dc = discdc;
		id = idNum;
	}

	/**
	 * TODO: Should we "zero out" the name or owner?
	 * @return a copy of this fortification
	 * @param zero whether to zero out the DC
	 */
	@Override
	public Fortification copy(final boolean zero) {
		Fortification retval;
		if (zero) {
			retval = new Fortification(status(), size(), 0, getName(), id, getOwner());
		} else {
			retval = new Fortification(status(), size(), dc, getName(), id, getOwner());
		}
		retval.setImage(getImage());
		return retval;
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
	public String getDefaultImage() {
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

	/**
	 * @return a string describing all fortifications as a class
	 */
	@Override
	public String plural() {
		return "Fortifications";
	}
	/**
	 * @return that this is a fortification
	 */
	@Override
	public String kind() {
		return "fortification";
	}
}
