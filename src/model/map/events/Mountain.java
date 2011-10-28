package model.map.events;

import model.map.TerrainFixture;

/**
 * A mountain on the map---or at least a fixture representing mountainous terrain.
 * @author Jonathan Lovelace
 *
 */
public class Mountain implements TerrainFixture {
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
