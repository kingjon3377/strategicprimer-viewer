package model.map.fixtures;

import model.map.HasImage;
import model.map.TileFixture;

/**
 * A centaur. TODO: Should probably be a kind of unit instead, or something ...
 *
 * @author Jonathan Lovelace
 *
 */
public class Centaur implements TileFixture, HasImage {
	/**
	 * What kind of centaur.
	 */
	private final String kind;

	/**
	 * Constructor.
	 *
	 * @param centKind the kind of centaur
	 * @param idNum the ID number.
	 * @param fileName the file this was loaded from
	 */
	public Centaur(final String centKind, final int idNum, final String fileName) {
		kind = centKind;
		id = idNum;
		file = fileName;
	}

	/**
	 * @return the kind of centaur
	 */
	public String getKind() {
		return kind;
	}

	/**
	 * @return an XML representation of the centaur
	 * @deprecated Replaced by SPIntermediateRepresentation-based output
	 */
	@Override
	@Deprecated
	public String toXML() {
		return new StringBuilder("<centaur kind=\"").append(kind)
				.append("\" id=\"").append(id).append("\" />").toString();
	}

	/**
	 * @return a String representation of the centaur
	 */
	@Override
	public String toString() {
		return kind + " centaur";
	}

	/**
	 * @return the name of an image to represent the centaur
	 */
	@Override
	public String getImage() {
		return "centaur.png";
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
		return obj instanceof Centaur && ((Centaur) obj).kind.equals(kind)
				&& ((TileFixture) obj).getID() == id;
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
		return fix instanceof Centaur && ((Centaur) fix).kind.equals(kind);
	}

	/**
	 * @return The name of the file this is to be written to.
	 */
	@Override
	public String getFile() {
		return file;
	}

	/**
	 * @param fileName the name of the file this should be written to.
	 */
	@Override
	public void setFile(final String fileName) {
		file = fileName;
	}

	/**
	 * The name of the file this is to be written to.
	 */
	private String file;

	/**
	 * @return a clone of this object
	 */
	@Override
	public TileFixture deepCopy() {
		return new Centaur(getKind(), getID(), getFile());
	}
}
