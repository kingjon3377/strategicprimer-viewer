package model.map.fixtures.resources;

import model.map.HasImage;
import model.map.TileFixture;
import model.map.XMLWritableImpl;

/**
 * An orchard (fruit trees) or grove (other trees) on the map.
 *
 * @author Jonathan Lovelace
 *
 */
public class Grove extends XMLWritableImpl implements HarvestableFixture,
		HasImage {
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
	 * @param fileName the file this was loaded from
	 */
	public Grove(final boolean fruit, final boolean cultivatedGrove,
			final String tree, final int idNum, final String fileName) {
		super(fileName);
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
	public String getKind() {
		return kind;
	}

	/**
	 * @return an XML representation of the Fixture.
	 * @deprecated Replaced by SPIntermediateRepresentation-based output
	 */
	@Override
	@Deprecated
	public String toXML() {
		return new StringBuilder(orchard ? "<orchard" : "<grove")
				.append(" cultivated=\"").append(isCultivated()).append("\" kind=\"")
				.append(getKind()).append("\" id=\"").append(id)
				.append("\" />").toString();
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
	public boolean equalsIgnoringID(final TileFixture fix) {
		return fix instanceof Grove && kind.equals(((Grove) fix).kind)
				&& orchard == ((Grove) fix).orchard
				&& cultivated == ((Grove) fix).cultivated;
	}

	/**
	 * @return a clone of this object
	 */
	@Override
	public TileFixture deepCopy() {
		return new Grove(isOrchard(), isCultivated(), getKind(), getID(), getFile());
	}
}
