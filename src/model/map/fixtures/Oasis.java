package model.map.fixtures;

import model.map.HasImage;
import model.map.TerrainFixture;
import model.map.TileFixture;

/**
 * An oasis on the map.
 * @author Jonathan Lovelace
 *
 */
public class Oasis implements TerrainFixture, HasImage {
	/**
	 * @param idNum the ID number.
	 */
	public Oasis(final long idNum) {
		id = idNum;
	}
	/**
	 * @return a String representation of the oasis.
	 */
	@Override
	public String toString() {
		return "Oasis";
	}
	/**
	 * @return an XML representation of the oasis.
	 */
	@Override
	public String toXML() {
		return new StringBuilder("<oasis id=\"").append(id).append("\" />")
				.toString();
	}
	/**
	 * @return the name of an image to represent the oasis.
	 */
	@Override
	public String getImage() {
		return "oasis.png";
	}
	/**
	 * @return a z-value for use in determining the top fixture on a tile
	 */
	@Override
	public int getZValue() {
		return 25;
	}
	/**
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(final Object obj) {
		return obj instanceof Oasis && id == ((TileFixture) obj).getID();
	}
	/**
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return (int) id;
	}
	/**
	 * @param fix
	 *            A TileFixture to compare to
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
	private final long id; // NOPMD
	/**
	 * @return a UID for the fixture.
	 */
	@Override
	public long getID() {
		return id;
	}
}
