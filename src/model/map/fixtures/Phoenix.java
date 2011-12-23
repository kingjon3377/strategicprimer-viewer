package model.map.fixtures;

import model.map.HasImage;
import model.map.TileFixture;

/**
 * A phoenix. TODO: should probably be a unit, or something.
 * @author Jonathan Lovelace
 *
 */
public class Phoenix implements TileFixture, HasImage {
	/**
	 * @return an XML representation of the phoenix
	 */
	@Override
	public String toXML() {
		return "<phoenix />";
	}
	/**
	 * @return a String representation of the djinn
	 */
	@Override
	public String toString() {
		return "phoenix";
	}
	/**
	 * @return the name of an image to represent the phoenix
	 */
	@Override
	public String getImage() {
		return "phoenix.png";
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
		return obj instanceof Phoenix;
	}
	/**
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return "phoenix".hashCode();
	}
	/**
	 * @param fix a TileFixture to compare to
	 * @return the result of the comparison
	 */
	@Override
	public int compareTo(final TileFixture fix) {
		return Integer.valueOf(getZValue()).compareTo(fix.getZValue());
	}
}
