package model.map.fixtures;

import model.map.HasImage;
import model.map.TileFixture;

/**
 * A simurgh. TODO: should probably be a unit, or something.
 * @author Jonathan Lovelace
 *
 */
public class Simurgh implements TileFixture, HasImage {
	/**
	 * @param idNum the ID number.
	 */
	public Simurgh(final long idNum) {
		id = idNum;
	}
	/**
	 * @return an XML representation of the simurgh
	 */
	@Override
	public String toXML() {
		return new StringBuilder("<simurgh id=\"").append(id).append("\" />")
				.toString();
	}
	/**
	 * @return a String representation of the djinn
	 */
	@Override
	public String toString() {
		return "simurgh";
	}
	/**
	 * @return the name of an image to represent the simurgh
	 */
	@Override
	public String getImage() {
		return "simurgh.png";
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
		return obj instanceof Simurgh && ((TileFixture) obj).getID() == id;
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
}
