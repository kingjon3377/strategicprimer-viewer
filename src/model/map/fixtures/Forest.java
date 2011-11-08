package model.map.fixtures;

import model.map.HasImage;
import model.map.TerrainFixture;

/**
 * A forest on a tile.
 * @author Jonathan Lovelace
 *
 */
public class Forest implements TerrainFixture, HasImage {
	/**
	 * Whether this is "rows of" trees.
	 */
	private final boolean rows;
	/**
	 * Constructor.
	 * @param kind what kind of trees dominate.
	 * @param rowed whether the trees are in rows
	 */
	public Forest(final String kind, final boolean rowed) {
		trees = kind;
		rows = rowed;
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
		return rows ? "Rows of " + trees + " trees." : "A " + trees + " forest.";
	}
	/**
	 * @return an XML representation of the forest.
	 */
	@Override
	public String toXML() {
		final StringBuilder sbuild = new StringBuilder("<forest kind=\"");
		sbuild.append(trees);
		if (rows) {
			sbuild.append("\" rows=\"true");
		}
		sbuild.append("\" />");
		return sbuild.toString();
	}
	/**
	 * TODO: Should differ based on what kind of tree.
	 * @return the name of an image to represent the forest.
	 */
	@Override
	public String getImage() {
		return "tree.png";
	}
	/**
	 * @return whether this is "rows of" trees.
	 */
	public boolean isRows() {
		return rows;
	}
}
