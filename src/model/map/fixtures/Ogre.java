package model.map.fixtures;

import model.map.HasImage;
import model.map.TileFixture;

/**
 * An ogre. TODO: should probably be a unit, or something.
 * @author Jonathan Lovelace
 *
 */
public class Ogre implements TileFixture, HasImage {
	/**
	 * @return an XML representation of the ogre
	 */
	@Override
	public String toXML() {
		return "<ogre />";
	}
	/**
	 * @return a String representation of the ogre
	 */
	@Override
	public String toString() {
		return "ogre";
	}
	/**
	 * @return the name of an image to represent the ogre
	 */
	@Override
	public String getImage() {
		return "ogre.png";
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
		return obj instanceof Ogre;
	}
	/**
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return "ogre".hashCode();
	}
	/**
	 * @param fix a TileFixture to compare to
	 * @return the result of the comparison
	 */
	@Override
	public int compareTo(final TileFixture fix) {
		return fix.hashCode() - hashCode();
	}
}
