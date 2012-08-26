package model.map.fixtures.mobile;

import model.map.HasImage;
import model.map.TileFixture;
import model.map.XMLWritableImpl;

/**
 * A minotaur. TODO:Should probably be a unit, or something.
 *
 * @author Jonathan Lovelace
 *
 */
public class Minotaur extends XMLWritableImpl implements MobileFixture, HasImage {
	/**
	 * @param idNum the ID number.
	 * @param fileName the file this was loaded from
	 */
	public Minotaur(final int idNum, final String fileName) {
		super(fileName);
		id = idNum;
	}

	/**
	 * @return an XML representation of the minotaur
	 * @deprecated Replaced by SPIntermediateRepresentation-based output
	 */
	@Override
	@Deprecated
	public String toXML() {
		return new StringBuilder("<minotaur id=\"").append(id).append("\" />")
				.toString();
	}

	/**
	 * @return a String representation of the minotaur
	 */
	@Override
	public String toString() {
		return "minotaur";
	}

	/**
	 * @return the name of an image to represent the minotaur
	 */
	@Override
	public String getImage() {
		return "minotaur.png";
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
		return this == obj || (obj instanceof Minotaur && id == ((TileFixture) obj).getID());
	}

	/**
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return id;
	}

	/**
	 * @param fix a TileFixture to compare to
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
		return fix instanceof Minotaur;
	}

	/**
	 * @return a clone of this object
	 */
	@Override
	public TileFixture deepCopy() {
		return new Minotaur(getID(), getFile());
	}
}
