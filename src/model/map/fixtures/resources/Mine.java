package model.map.fixtures.resources;

import model.map.HasImage;
import model.map.HasKind;
import model.map.TileFixture;
import model.map.XMLWritableImpl;
import model.map.fixtures.towns.TownStatus;

/**
 * A mine---a source of mineral resources.
 *
 * @author Jonathan Lovelace
 *
 */
public class Mine extends XMLWritableImpl implements HarvestableFixture,
		HasImage, HasKind {
	/**
	 * Constructor.
	 *
	 * @param mineral what mineral this produces
	 * @param stat the status of the mine
	 * @param idNum the ID number.
	 * @param fileName the file this was loaded from
	 */
	public Mine(final String mineral, final TownStatus stat, final int idNum,
			final String fileName) {
		super(fileName);
		kind = mineral;
		status = stat;
		id = idNum;
	}

	/**
	 * What the mine produces.
	 */
	private final String kind;
	/**
	 * The status of the mine.
	 */
	private final TownStatus status;

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
	 * @return an XML representation of the mine
	 * @deprecated Replaced by SPIntermediateRepresentation-based output
	 */
	@Override
	@Deprecated
	public String toXML() {
		return new StringBuilder("<mine kind=\"").append(kind)
				.append("\" status=\"").append(status.toString())
				.append("\" id=\"").append(id).append("\" />").toString();
	}

	/**
	 * @return the name of an image to represent the mine
	 */
	@Override
	public String getImage() {
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
	public boolean equals(final Object obj) {
		return this == obj || (obj instanceof Mine && kind.equals(((Mine) obj).kind)
				&& status.equals(((Mine) obj).status)
				&& id == ((TileFixture) obj).getID());
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
	public boolean equalsIgnoringID(final TileFixture fix) {
		return fix instanceof Mine && kind.equals(((Mine) fix).kind)
				&& status.equals(((Mine) fix).status);
	}

	/**
	 * @return a clone of this object
	 */
	@Override
	public TileFixture deepCopy() {
		return new Mine(getKind(), getStatus(), getID(), getFile());
	}
}
