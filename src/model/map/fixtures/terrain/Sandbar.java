package model.map.fixtures.terrain;

import model.map.HasImage;
import model.map.TerrainFixture;
import model.map.TileFixture;
import model.map.XMLWritableImpl;

/**
 * A sandbar on the map.
 *
 * @author Jonathan Lovelace
 *
 */
public class Sandbar extends XMLWritableImpl implements TerrainFixture, HasImage {
	/**
	 * @param idNum the ID number.
	 * @param fileName the file this was loaded from
	 */
	public Sandbar(final int idNum, final String fileName) {
		super(fileName);
		id = idNum;
	}

	/**
	 * @return a String representation of the sandbar.
	 */
	@Override
	public String toString() {
		return "Sandbar";
	}

	/**
	 * @return an XML representaiton of the sandbar
	 * @deprecated Replaced by SPIntermediateRepresentation-based output
	 */
	@Override
	@Deprecated
	public String toXML() {
		return new StringBuilder("<sandbar id=\"").append(id).append("\" />")
				.toString();
	}

	/**
	 * @return the name o an image to represent the sandbar.
	 */
	@Override
	public String getImage() {
		return "sandbar.png";
	}

	/**
	 * @return a z-value for use in determining the top fixture on a tile
	 */
	@Override
	public int getZValue() {
		return 5;
	}

	/**
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(final Object obj) {
		return obj instanceof Sandbar && id == ((TileFixture) obj).getID();
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
		return fix instanceof Sandbar;
	}

	/**
	 * @return a clone of this object
	 */
	@Override
	public TileFixture deepCopy() {
		return new Sandbar(getID(), getFile());
	}
}
