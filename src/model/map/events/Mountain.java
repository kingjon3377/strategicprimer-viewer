package model.map.events;

import model.map.TileFixture;

/**
 * A mountain on the map---or at least a fixture representing mountainous terrain.
 * @author Jonathan Lovelace
 *
 */
public class Mountain implements TileFixture {
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
}
