package model.map.fixtures;

import model.map.HasImage;
import model.map.TerrainFixture;

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
}
