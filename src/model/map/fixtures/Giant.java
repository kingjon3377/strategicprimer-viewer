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
	 */
	public Giant(final String gKind, final long idNum) {
		kind = gKind;
		id = idNum;
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
	public String toXML() {
		return new StringBuilder("<giant kind=\"").append(kind)
				.append("\" id=\"").append(id).append("\" />").toString();
	}
	/**
	 * @return a String representation of the giant
	 */
	@Override
	public String toString() {
		return kind + ("".equals(kind) ? "giant"  : " giant");
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
		return (int) id;
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
	private final long id; // NOPMD
	/**
	 * @return a UID for the fixture.
	 */
	@Override
	public long getID() {
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
}
