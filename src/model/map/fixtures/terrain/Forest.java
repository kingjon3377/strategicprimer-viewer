package model.map.fixtures.terrain;

import model.map.HasImage;
import model.map.HasKind;
import model.map.IFixture;
import model.map.TerrainFixture;
import model.map.TileFixture;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A forest on a tile.
 *
 * @author Jonathan Lovelace
 *
 */
public class Forest implements TerrainFixture, HasImage, HasKind {
	/**
	 * @return what kind of trees
	 */
	@Override
	public String getKind() {
		return trees;
	}

	/**
	 * Whether this is "rows of" trees.
	 */
	private final boolean rows;

	/**
	 * Constructor.
	 *
	 * @param kind what kind of trees dominate.
	 * @param rowed whether the trees are in rows
	 */
	public Forest(final String kind, final boolean rowed) {
		super();
		trees = kind;
		rows = rowed;
	}

	/**
	 * What kind of trees dominate the forest.
	 */
	private String trees;

	/**
	 * @return a String representation of the forest.
	 */
	@Override
	public String toString() {
		return rows ? "Rows of " + trees + " trees." : "A " + trees
				+ " forest.";
	}

	/**
	 * TODO: Should differ based on what kind of tree.
	 *
	 * @return the name of an image to represent the forest.
	 */
	@Override
	public String getDefaultImage() {
		return "trees.png";
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
	public boolean equals(@Nullable final Object obj) {
		return this == obj
				|| (obj instanceof Forest && equalsImpl((Forest) obj));
	}
	/**
	 * @param obj a forest
	 * @return whether it's equal to this one
	 */
	private boolean equalsImpl(final Forest obj) {
		return trees.equals(obj.trees) && rows == obj.rows;
	}
	/**
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return trees.hashCode() << (rows ? 1 : 0);
	}

	/**
	 * @param fix A TileFixture to compare to
	 *
	 * @return the result of the comparison
	 */
	@Override
	public int compareTo(final TileFixture fix) {
		return fix.hashCode() - hashCode();
	}

	/**
	 * TODO: support different IDs for different instances.
	 *
	 * @return an ID for the object
	 */
	@Override
	public int getID() {
		return -1;
	}

	/**
	 * @param fix a fixture
	 * @return whether it's identical to this except ID and DC.
	 */
	@Override
	public boolean equalsIgnoringID(final IFixture fix) {
		return equals(fix);
	}

	/**
	 * @param kind the new kind
	 */
	@Override
	public final void setKind(final String kind) {
		trees = kind;
	}

	/**
	 * The name of an image to use for this particular fixture.
	 */
	private String image = "";

	/**
	 * @param img the name of an image to use for this particular fixture
	 */
	@Override
	public void setImage(final String img) {
		image = img;
	}

	/**
	 * @return the name of an image to use for this particular fixture.
	 */
	@Override
	public String getImage() {
		return image;
	}

	/**
	 * @return a string describing all forests as a class
	 */
	@Override
	public String plural() {
		return "Forests";
	}
	/**
	 * @return a short description of the fixture
	 */
	@Override
	public String shortDesc() {
		return toString();
	}
}
