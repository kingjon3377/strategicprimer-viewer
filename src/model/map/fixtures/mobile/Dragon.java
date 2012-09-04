package model.map.fixtures.mobile;

import model.map.HasImage;
import model.map.HasKind;
import model.map.TileFixture;
import model.map.XMLWritableImpl;

/**
 * A dragon. TODO: should probably be a unit, or something.
 *
 * @author Jonathan Lovelace
 *
 */
public class Dragon extends XMLWritableImpl implements MobileFixture, HasImage, HasKind {
	/**
	 * What kind of dragon. (Usually blank, at least at first.)
	 */
	private final String kind;

	/**
	 * Constructor.
	 *
	 * @param dKind the kind of dragon
	 * @param idNum the ID number.
	 * @param fileName the file this was loaded from
	 */
	public Dragon(final String dKind, final int idNum, final String fileName) {
		super(fileName);
		kind = dKind;
		id = idNum;
	}

	/**
	 * @return the kind of dragon
	 */
	@Override
	public String getKind() {
		return kind;
	}

	/**
	 * @return an XML representation of the dragon
	 * @deprecated Replaced by SPIntermediateRepresentation-based output
	 */
	@Override
	@Deprecated
	public String toXML() {
		return new StringBuilder("<dragon kind=\"").append(kind)
				.append("\" id=\"").append(id).append("\" />").toString();
	}

	/**
	 * @return a String representation of the dragon
	 */
	@Override
	public String toString() {
		return kind + (kind.isEmpty() ? "dragon" : " dragon");
	}

	/**
	 * @return the name of an image to represent the fairy
	 */
	@Override
	public String getImage() {
		return "dragon.png";
	}

	/**
	 * @return a z-value for use in determining the top fixture on a tile
	 */
	@Override
	public int getZValue() {
		return 40;
	}

	/**
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(final Object obj) {
		return this == obj || (obj instanceof Dragon && ((Dragon) obj).kind.equals(kind)
				&& ((TileFixture) obj).getID() == id);
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
		return fix instanceof Dragon && ((Dragon) fix).kind.equals(kind);
	}

	/**
	 * @return a clone of this object
	 */
	@Override
	public TileFixture deepCopy() {
		return new Dragon(getKind(), getID(), getFile());
	}
}
