package model.map.fixtures;

import model.map.HasImage;
import model.map.TerrainFixture;
import model.map.TileFixture;

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
	@Deprecated
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
	/**
	 * @return a z-value for use in determining the top fixture on a tile
	 */
	@Override
	public int getZValue() {
		return 20;
	}
	/**
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(final Object obj) {
		return obj instanceof Forest && trees.equals(((Forest) obj).trees)
				&& rows == ((Forest) obj).rows;
	}
	/**
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return trees.hashCode() << (rows ? 1 : 0);
	}
	/**
	 * @param fix
	 *            A TileFixture to compare to
	 * 
	 * @return the result of the comparison
	 */
	@Override
	public int compareTo(final TileFixture fix) {
		return fix.hashCode() - hashCode();
	}
	/**
	 * TODO: support different IDs for different instances.
	 * @return an ID for the object
	 */
	@Override
	public long getID() {
		return -1;
	}
	/**
	 * @param fix a fixture
	 * @return whether it's identical to this except ID and DC.
	 */
	@Override
	public boolean equalsIgnoringID(final TileFixture fix) {
		return equals(fix);
	}
	/**
	 * @return The name of the file this is to be written to.
	 */
	@Override
	public String getFile() {
		return file;
	}
	/**
	 * @param fileName the name of the file this should be written to.
	 */
	@Override
	public void setFile(final String fileName) {
		file = fileName;
	}
	/**
	 * The name of the file this is to be written to.
	 */
	private String file;
}
