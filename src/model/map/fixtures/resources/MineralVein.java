package model.map.fixtures.resources;

import model.map.HasImage;
import model.map.HasKind;
import model.map.IEvent;
import model.map.IFixture;
import model.map.TileFixture;

/**
 * A vein of a mineral.
 *
 * @author Jonathan Lovelace
 *
 */
public final class MineralVein implements IEvent,
		HasImage, HarvestableFixture, HasKind {
	/**
	 * Version UID for serialization.
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Constructor.
	 *
	 * @param minkind what kind of mineral this is
	 * @param exp whether the vein is exposed
	 * @param discdc the dc to discover the vein
	 * @param idNum the ID number.
	 */
	public MineralVein(final String minkind, final boolean exp,
			final int discdc, final int idNum) {
		super();
		mineral = minkind;
		exposed = exp;
		dc = discdc;
		id = idNum;
	}

	/**
	 * What kind of mineral this is.
	 */
	private String mineral;

	/**
	 *
	 * @return what kind of mineral this is
	 */
	public String mineral() {
		return mineral;
	}

	/**
	 * Whether the vein is exposed.
	 *
	 * TODO: Perhaps this should be mutable and protected by accessor methods?
	 */
	private final boolean exposed;

	/**
	 *
	 * @return whether the vein is exposed
	 */
	public boolean isExposed() {
		return exposed;
	}

	/**
	 * The DC to discover the vein. TODO: Should perhaps be mutable.
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
	 *
	 * @return exploration-result text for the event.
	 */
	@Override
	public String getText() {
		final StringBuilder build = new StringBuilder("There is a");
		if (exposed) {
			build.append("n exposed");
		}
		build.append(" vein of ");
		build.append(mineral);
		build.append(" here");
		if (exposed) {
			build.append('.');
		} else {
			build.append(", but it's not exposed.");
		}
		return build.toString();
	}

	/**
	 * @param obj an object
	 *
	 * @return whether it's an identical event
	 */
	@Override
	public boolean equals(final Object obj) {
		return this == obj
				|| (obj instanceof MineralVein
						&& ((MineralVein) obj).mineral.equals(mineral)
						&& ((MineralVein) obj).exposed == exposed && ((TileFixture) obj)
						.getID() == id);
	}

	/**
	 *
	 * @return a hash value for the event
	 */
	@Override
	public int hashCode() {
		return id;
	}

	/**
	 *
	 * @return a string representation of the event
	 */
	@Override
	public String toString() {
		return "A " + mineral + " deposit, "
				+ (exposed ? "exposed" : "not exposed") + ", DC " + dc;
	}

	/**
	 * @return the kind of mineral
	 */
	@Override
	public String getKind() {
		return mineral;
	}

	/**
	 * @return the name of an image to represent the event
	 */
	@Override
	public String getImage() {
		return "mineral.png";
	}

	/**
	 * @return a z-value for use in determining the top fixture on a tile
	 */
	@Override
	public int getZValue() {
		return 40;
	}

	/**
	 * @param fix A TileFixture to compare to
	 *
	 * @return the result of the comparison
	 */
	@Override
	public int compareTo(final TileFixture fix) {
		return fix.hashCode() - hashCode();
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
	 * @param fix a fixture
	 * @return whether it's identical to this except ID and DC.
	 */
	@Override
	public boolean equalsIgnoringID(final IFixture fix) {
		return this == fix
				|| (fix instanceof MineralVein
						&& equalsContents((MineralVein) fix));
	}
	/**
	 * @param fix another MineralEvent
	 * @return whether its contents equal ours
	 */
	private boolean equalsContents(final MineralVein fix) {
		return fix.mineral.equals(mineral) && fix.exposed == exposed;
	}
	/**
	 * @param kind the new kind
	 */
	@Override
	public void setKind(final String kind) {
		mineral = kind;
	}
}
