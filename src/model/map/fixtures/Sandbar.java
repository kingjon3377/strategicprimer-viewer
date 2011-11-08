package model.map.fixtures;

import model.map.HasImage;
import model.map.TerrainFixture;
/**
 * A sandbar on the map.
 * @author Jonathan Lovelace
 *
 */
public class Sandbar implements TerrainFixture, HasImage {
	/**
	 * @return a String representation of the sandbar.
	 */
	@Override
	public String toString() {
		return "Sandbar";
	}
	/**
	 * @return an XML representaiton of the sandbar 
	 */
	@Override
	public String toXML() {
		return "<sandbar />";
	}
	/**
	 * @return the name o an image to represent the sandbar.
	 */
	@Override
	public String getImage() {
		return "sandbar.png";
	}
}
