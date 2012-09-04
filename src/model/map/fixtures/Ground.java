package model.map.fixtures;

import model.map.HasImage;
import model.map.HasKind;
import model.map.TileFixture;
import model.map.XMLWritableImpl;

/**
 * A TileFixture to represent the basic rock beneath the tile, possibly exposed.
 *
 * @author Jonathan Lovelace
 *
 */
public class Ground extends XMLWritableImpl implements TileFixture, HasImage, HasKind {
	/**
	 * Constructor.
	 *
	 * @param desc a description of the ground (the type of rock)
	 * @param exp whether it's exposed. (If not, the tile should also include a
	 *        grass or forest Fixture ...)
	 * @param fileName the file this was loaded from
	 */
	public Ground(final String desc, final boolean exp, final String fileName) {
		super(fileName);
		kind = desc;
		exposed = exp;
	}

	/**
	 * The kind of ground.
	 */
	private final String kind;
	/**
	 * Whether the ground is exposed.
	 */
	private final boolean exposed;

	/**
	 * @return whether the ground is exposed
	 */
	public boolean isExposed() {
		return exposed;
	}

	/**
	 * @return a description of the grond
	 */
	@Override
	public String getKind() {
		return kind;
	}

	/**
	 * @return the name of an image to represent the ground.
	 */
	@Override
	public String getImage() {
		return exposed ? "expground.png" : "blank.png";
	}

	/**
	 * TODO: Should perhaps depend on whether it's exposed or not.
	 *
	 * @return a z-value for use in determining the top fixture on a tile
	 */
	@Override
	public int getZValue() {
		return 0;
	}

	/**
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(final Object obj) {
		return this == obj || (obj instanceof Ground && kind.equals(((Ground) obj).kind)
				&& exposed == ((Ground) obj).exposed);
	}

	/**
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return kind.hashCode() << (exposed ? 1 : 0);
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
	 * @return a String representation of the Ground.
	 */
	@Override
	public String toString() {
		return (exposed ? "Exposed " : "Unexposed ") + "ground of kind " + kind;
	}

	/**
	 * TODO: make this different between instances.
	 *
	 * @return an ID number.
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
	public boolean equalsIgnoringID(final TileFixture fix) {
		return equals(fix);
	}

	/**
	 * @return a clone of this object
	 */
	@Override
	public TileFixture deepCopy() {
		return new Ground(getKind(), isExposed(), getFile());
	}
}
