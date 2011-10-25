package model.map.events;

import model.map.TileFixture;

/**
 * A forest on a tile.
 * @author Jonathan Lovelace
 *
 */
public class Forest implements TileFixture {
	/**
	 * Constructor.
	 * @param kind what kind of trees dominate.
	 */
	public Forest(final String kind) {
		trees = kind;
	}
	/**
	 * What kind of trees dominate the forest.
	 */
	private final String trees;
	/**
	 * @return a String representation of the forest.
	 */
	@Override
	public String toString() {
		return "A " + trees + " forest.";
	}
	/**
	 * @return an XML representation of the forest.
	 */
	@Override
	public String toXML() {
		final StringBuilder sbuild = new StringBuilder("<forest kind=\"");
		sbuild.append(trees);
		sbuild.append("\" />");
		return sbuild.toString();
	}
}
