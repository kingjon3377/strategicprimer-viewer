package model.map.fixtures;

import model.map.TerrainFixture;

/**
 * An oasis on the map.
 * @author Jonathan Lovelace
 *
 */
public class Oasis implements TerrainFixture {
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
}
