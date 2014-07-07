package model.map.fixtures.resources;

import model.map.HasKind;
import model.map.IEvent;
import model.map.IFixture;
import model.map.TileFixture;

import org.eclipse.jdt.annotation.Nullable;

import util.NullCleaner;

/**
 * A vein of a mineral.
 *
 * @author Jonathan Lovelace
 *
 */
public final class MineralVein implements IEvent, HarvestableFixture,
		HasKind {
	/**
	 * Whether the vein is exposed.
	 *
	 * TODO: Perhaps this should be mutable and protected by accessor methods?
	 */
	private final boolean exposed;

	/**
	 * What kind of mineral this is.
	 */
	private String mineral;

	/**
	 * The name of an image to use for this particular fixture.
	 */
	private String image = "";

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
		mineral = minkind;
		exposed = exp;
		dc = discdc;
		id = idNum;
	}

	/**
	 *
	 * @return what kind of mineral this is
	 */
	public String mineral() {
		return mineral;
	}

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
		final StringBuilder build = new StringBuilder(48 + mineral.length())
				.append("There is a");
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
		return NullCleaner.assertNotNull(build.toString());
	}

	/**
	 * @param obj an object
	 *
	 * @return whether it's an identical event
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return this == obj || obj instanceof MineralVein
				&& mineral.equals(((MineralVein) obj).mineral)
				&& exposed == ((MineralVein) obj).exposed
				&& id == ((MineralVein) obj).id;
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
		if (exposed) {
			return "A " + mineral + " deposit, exposed, DC " + dc; // NOPMD
		} else {
			return "A " + mineral + " deposit, not exposed, DC " + dc;
		}
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
	public String getDefaultImage() {
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
	public int compareTo(@Nullable final TileFixture fix) {
		if (fix == null) {
			throw new IllegalArgumentException("Compared to null fixture");
		}
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
		return this == fix || fix instanceof MineralVein
				&& equalsContents((MineralVein) fix);
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

	/**
	 * @param img the name of an image to use for this particular fixture
	 */
	@Override
	public void setImage(final String img) {
		image = img;
	}

	/**
	 * @return the name of an image to use for this particular fixture.
	 */
	@Override
	public String getImage() {
		return image;
	}

	/**
	 * @return a string describing all mineral veins as a class
	 */
	@Override
	public String plural() {
		return "Mineral veins";
	}
	/**
	 * @return a short description of the fixture
	 */
	@Override
	public String shortDesc() {
		return toString();
	}
}
