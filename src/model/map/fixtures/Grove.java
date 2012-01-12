package model.map.fixtures;

import model.map.HasImage;
import model.map.TileFixture;

/**
 * An orchard (fruit trees) or grove (other trees) on the map.
 * @author Jonathan Lovelace
 *
 */
public class Grove implements TileFixture, HasImage {
	/**
	 * Whether this is a fruit orchard.
	 */
	private final boolean orchard;
	/**
	 * Whether it's wild (if true) or cultivated.
	 */
	private final boolean wild;
	/**
	 * Kind of tree.
	 */
	private final String tree;
	/**
	 * Constructor.
	 * @param fruit whether the trees are fruit trees
	 * @param wildGrove whether the trees are wild
	 * @param kind what kind of trees are in the grove
	 */
	public Grove(final boolean fruit, final boolean wildGrove, final String kind) {
		orchard = fruit;
		wild = wildGrove;
		tree = kind;
	}
	/**
	 * @return true if this is an orchard, false otherwise
	 */
	public boolean isOrchard() {
		return orchard;
	}
	/**
	 * @return if this is a wild grove or orchard, false if it's a cultivated one
	 */
	public boolean isWild() {
		return wild;
	}
	/**
	 * @return what kind of trees are in the grove
	 */
	public String getTrees() {
		return tree;
	}
	/**
	 * @return an XML representation of the Fixture.
	 */
	@Override
	public String toXML() {
		final StringBuilder builder = new StringBuilder();
		if (orchard) {
			builder.append("<orchard");
		} else {
			builder.append("<grove");
		}
		builder.append(" wild=\"");
		builder.append(isWild());
		builder.append("\" tree=\"");
		builder.append(getTrees());
		builder.append("\" />");
		return builder.toString();
	}
	/**
	 * @return the name of an image to represent the grove or orchard
	 */
	@Override
	public String getImage() {
		return orchard ? "orchard.png" : "grove.png";
	}
	/**
	 * @return a String representation of the grove or orchard
	 */
	@Override
	public String toString() {
		return (isWild() ? "Wild " : "Cultivated ") + getTrees()
				+ (isOrchard() ? " orchard" : " grove");
	}
	/**
	 * @return a z-value for use in determining the top fixture on a tile
	 */
	@Override
	public int getZValue() {
		return 35;
	}
	/**
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(final Object obj) {
		return obj instanceof Grove && tree.equals(((Grove) obj).tree)
				&& orchard == ((Grove) obj).orchard && wild == ((Grove) obj).wild;
	}
	/**
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return tree.hashCode() << ((orchard ? 1 : 0) + (wild ? 2 : 0));
	}
	/**
	 * @param fix
	 *            A TileFixture to compare to
	 * 
	 * @return the result of the comparison
	 */
	@Override
	public int compareTo(final TileFixture fix) {
		return fix.getZValue() - getZValue();
	}
}
