package model.map.fixtures.resources;

import model.map.HasImage;
import model.map.HasKind;
import model.map.IFixture;
import model.map.TileFixture;

/**
 * An orchard (fruit trees) or grove (other trees) on the map.
 *
 * @author Jonathan Lovelace
 *
 */
public class Grove implements HarvestableFixture,
		HasImage, HasKind {
	/**
	 * Whether this is a fruit orchard.
	 */
	private final boolean orchard;
	/**
	 * Whether it's wild (if false) or cultivated.
	 */
	private final boolean cultivated;
	/**
	 * Kind of tree.
	 */
	private final String kind;

	/**
	 * Constructor.
	 *
	 * @param fruit whether the trees are fruit trees
	 * @param cultivatedGrove whether the trees are cultivated
	 * @param tree what kind of trees are in the grove
	 * @param idNum the ID number.
	 */
	public Grove(final boolean fruit, final boolean cultivatedGrove,
			final String tree, final int idNum) {
		super();
		orchard = fruit;
		cultivated = cultivatedGrove;
		kind = tree;
		id = idNum;
	}

	/**
	 * @return true if this is an orchard, false otherwise
	 */
	public boolean isOrchard() {
		return orchard;
	}

	/**
	 * @return if this is a cultivated grove or orchard, false if it's a wild one
	 */
	public boolean isCultivated() {
		return cultivated;
	}
	/**
	 * @return what kind of trees are in the grove
	 */
	@Override
	public String getKind() {
		return kind;
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
		return (isCultivated() ? "Cultivated " : "Wild ") + getKind()
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
		return this == obj || (obj instanceof Grove && kind.equals(((Grove) obj).kind)
				&& orchard == ((Grove) obj).orchard
				&& cultivated == ((Grove) obj).cultivated
				&& id == ((TileFixture) obj).getID());
	}

	/**
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return id;
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
	 * ID number.
	 */
	private final int id; // NOPMD

	/**
	 * @return a UID for the fixture.
	 */
	@Override
	public int getID() {
		return id;
	}

	/**
	 * @param fix a fixture
	 * @return whether it's identical to this except ID and DC.
	 */
	@Override
	public boolean equalsIgnoringID(final IFixture fix) {
		return fix instanceof Grove && kind.equals(((Grove) fix).kind)
				&& orchard == ((Grove) fix).orchard
				&& cultivated == ((Grove) fix).cultivated;
	}
}
