package model.map.fixtures;

import model.map.HasImage;
import model.map.TerrainFixture;

/**
 * An oasis on the map.
 * @author Jonathan Lovelace
 *
 */
public class Oasis implements TerrainFixture, HasImage {
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
		return "<oasis />";
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
}
