package model.map.fixtures;

import model.map.HasImage;
import model.map.TileFixture;

/**
 * A giant. TODO: should probably be a unit, or something.
 * @author Jonathan Lovelace
 *
 */
public class Giant implements TileFixture, HasImage {
	/**
	 * What kind of giant. (Usually blank, at least at first.)
	 */
	private final String kind;
	/**
	 * Constructor.
	 * @param gKind the kind of giant
	 * @param idNum the ID number.
	 * @param fileName the file this was loaded from
	 */
	public Giant(final String gKind, final int idNum, final String fileName) {
		kind = gKind;
		id = idNum;
		file = fileName;
	}
	/**
	 * @return the kind of giant
	 */
	public String getKind() {
		return kind;
	}
	/**
	 * @return an XML representation of the giant
	 */
	@Override
	@Deprecated
	public String toXML() {
		return new StringBuilder("<giant kind=\"").append(kind)
				.append("\" id=\"").append(id).append("\" />").toString();
	}
	/**
	 * @return a String representation of the giant
	 */
	@Override
	public String toString() {
		return kind + (kind.isEmpty() ? "giant"  : " giant");
	}
	/**
	 * @return the name of an image to represent the fairy
	 */
	@Override
	public String getImage() {
		return "giant.png";
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
		return obj instanceof Giant && ((Giant) obj).kind.equals(kind)
				&& id == ((TileFixture) obj).getID();
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
		return fix instanceof Giant && ((Giant) fix).kind.equals(kind);
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
}
