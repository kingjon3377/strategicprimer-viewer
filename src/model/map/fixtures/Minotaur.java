package model.map.fixtures;

import model.map.HasImage;
import model.map.TileFixture;

/**
 * A minotaur. TODO:Should probably be a unit, or something.
 * @author Jonathan Lovelace
 *
 */
public class Minotaur implements TileFixture, HasImage {
	/**
	 * @return an XML representation of the minotaur
	 */
	@Override
	public String toXML() {
		return "<minotaur />";
	}
	/**
	 * @return a String representation of the minotaur
	 */
	@Override
	public String toString() {
		return "minotaur";
	}
	/**
	 * @return the name of an image to represent the minotaur
	 */
	@Override
	public String getImage() {
		return "minotaur.png";
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
		return obj instanceof Minotaur;
	}
	/**
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return "minotaur".hashCode();
	}
	/**
	 * @param fix a TileFixture to compare to
	 * @return the result of the comparison
	 */
	@Override
	public int compareTo(final TileFixture fix) {
		return fix.getZValue() - getZValue();
	}
}
