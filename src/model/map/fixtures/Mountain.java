package model.map.fixtures;

import model.map.HasImage;
import model.map.TerrainFixture;
import model.map.TileFixture;

/**
 * A mountain on the map---or at least a fixture representing mountainous terrain.
 * @author Jonathan Lovelace
 *
 */
public class Mountain implements TerrainFixture, HasImage {
	/**
	 * @return a String representation of the forest.
	 */
	@Override
	public String toString() {
		return "Mountain.";
	}
	/**
	 * @return an XML representation of the forest.
	 */
	@Override
	@Deprecated
	public String toXML() {
		return "<mountain />";
	}
	/**
	 * @return the name of an image to represent the mountain.
	 */
	@Override
	public String getImage() {
		return "mountain.png";
	}
	/**
	 * @return a z-value for use in determining the top fixture on a tile
	 */
	@Override
	public int getZValue() {
		return 10;
	}
	/**
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(final Object obj) {
		return obj instanceof Mountain;
	}
	/**
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return 1;
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
	 * TODO: Perhaps make this per-mountain?
	 * @return an ID number
	 */
	@Override
	public int getID() {
		return -1;
	}
	/**
	 * @param fix a fixture
	 * @return whether it's identical to this except ID and DC.
	 */
	@Override
	public boolean equalsIgnoringID(final TileFixture fix) {
		return equals(fix);
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
