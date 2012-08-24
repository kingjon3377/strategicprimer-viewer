package model.map.fixtures.terrain;

import model.map.HasImage;
import model.map.TerrainFixture;
import model.map.TileFixture;
import model.map.XMLWritableImpl;

/**
 * A hill on the map. Should increase unit's effective vision by a small
 * fraction when the unit is on it, if not in forest.
 *
 * @author Jonathan Lovelace
 *
 */
public class Hill extends XMLWritableImpl implements TerrainFixture, HasImage {
	/**
	 * @param idNum the ID number.
	 * @param fileName the file this was loaded from
	 */
	public Hill(final int idNum, final String fileName) {
		super(fileName);
		id = idNum;
	}

	/**
	 * @return a String representation of the hill.
	 */
	@Override
	public String toString() {
		return "Hill";
	}

	/**
	 * @return an XML representation of the hill.
	 * @deprecated Replaced by SPIntermediateRepresentation-based output
	 */
	@Override
	@Deprecated
	public String toXML() {
		return new StringBuilder("<hill id=\"").append(id).append("\" />")
				.toString();
	}

	/**
	 * @return the name of an image to represent the hill.
	 */
	@Override
	public String getImage() {
		return "hill.png";
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
		return this == obj || (obj instanceof Hill && id == ((TileFixture) obj).getID());
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
		return fix instanceof Hill;
	}

	/**
	 * @return a clone of this object
	 */
	@Override
	public TileFixture deepCopy() {
		return new Hill(getID(), getFile());
	}
}
