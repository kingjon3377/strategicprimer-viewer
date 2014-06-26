package model.map.fixtures.resources;

import model.map.HasKind;
import model.map.IFixture;
import model.map.TileFixture;
import model.map.fixtures.towns.TownStatus;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A mine---a source of mineral resources.
 *
 * @author Jonathan Lovelace
 *
 */
public class Mine implements HarvestableFixture, HasKind {
	/**
	 * The name of an image to use for this particular fixture.
	 */
	private String image = "";

	/**
	 * What the mine produces.
	 */
	private String kind;
	/**
	 * The status of the mine.
	 */
	private final TownStatus status;

	/**
	 * Constructor.
	 *
	 * @param mineral what mineral this produces
	 * @param stat the status of the mine
	 * @param idNum the ID number.
	 */
	public Mine(final String mineral, final TownStatus stat, final int idNum) {
		super();
		kind = mineral;
		status = stat;
		id = idNum;
	}

	/**
	 * @return what the mine produces
	 */
	@Override
	public String getKind() {
		return kind;
	}

	/**
	 * @return the status of the mine
	 */
	public TownStatus getStatus() {
		return status;
	}

	/**
	 * @return the name of an image to represent the mine
	 */
	@Override
	public String getDefaultImage() {
		return "mine.png";
	}

	/**
	 * @return a string representation of the mine
	 */
	@Override
	public String toString() {
		return getStatus().toString() + " mine of " + getKind();
	}

	/**
	 * @return a z-value for use in determining the top fixture on a tile
	 */
	@Override
	public int getZValue() {
		return 45;
	}

	/**
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return this == obj || obj instanceof Mine
				&& kind.equals(((Mine) obj).kind)
				&& status.equals(((Mine) obj).status) && id == ((Mine) obj).id;
	}

	/**
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return id;
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
		return fix instanceof Mine && kind.equals(((Mine) fix).kind)
				&& status.equals(((Mine) fix).status);
	}

	/**
	 * @param nKind the new kind
	 */
	@Override
	public final void setKind(final String nKind) {
		kind = nKind;
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
	 * @return a string describing all mines as a class
	 */
	@Override
	public String plural() {
		return "Mines";
	}
	/**
	 * @return a short description of the fixture
	 */
	@Override
	public String shortDesc() {
		return getStatus().toString() + ' ' + getKind() + " mine";
	}
}
