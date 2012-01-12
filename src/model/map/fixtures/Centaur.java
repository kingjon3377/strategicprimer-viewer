package model.map.fixtures;

import model.map.HasImage;
import model.map.TileFixture;

/**
 * A centaur. TODO: Should probably be a kind of unit instead, or something ...
 * @author Jonathan Lovelace
 *
 */
public class Centaur implements TileFixture, HasImage {
	/**
	 * What kind of centaur.
	 */
	private final String kind;
	/**
	 * Constructor.
	 * @param centKind the kind of centaur
	 */
	public Centaur(final String centKind) {
		kind = centKind;
	}
	/**
	 * @return the kind of centaur
	 */
	public String getKind() {
		return kind;
	}
	/**
	 * @return an XML representation of the centaur
	 */
	@Override
	public String toXML() {
		return new StringBuilder("<centaur kind=\"").append(kind).append("\" />").toString();
	}
	/**
	 * @return a String representation of the centaur
	 */
	@Override
	public String toString() {
		return kind + " centaur";
	}
	/**
	 * @return the name of an image to represent the centaur
	 */
	@Override
	public String getImage() {
		return "centaur.png";
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
		return obj instanceof Centaur && ((Centaur) obj).kind.equals(kind);
	}
	/**
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return kind.hashCode();
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

