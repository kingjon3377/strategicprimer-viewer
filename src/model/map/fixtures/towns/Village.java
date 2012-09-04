package model.map.fixtures.towns;

import model.map.HasImage;
import model.map.TileFixture;
import model.map.XMLWritableImpl;

/**
 * A village on the map.
 *
 * @author Jonathan Lovelace
 *
 */
public class Village extends XMLWritableImpl implements TownFixture, HasImage {
	/**
	 * The status of the village.
	 */
	private final TownStatus status;
	/**
	 * The name of the village.
	 */
	private final String name;

	/**
	 * Constructor.
	 *
	 * @param vstatus the status of the village.
	 * @param vName the name of the village
	 * @param idNum the ID number.
	 * @param fileName the file this was loaded from
	 */
	public Village(final TownStatus vstatus, final String vName,
			final int idNum, final String fileName) {
		super(fileName);
		status = vstatus;
		name = vName;
		id = idNum;
	}

	/**
	 * @return a String representation of the village
	 */
	@Override
	public String toString() {
		return status.toString() + " village"
				+ (name.isEmpty() ? name : " named " + name);
	}

	/**
	 * @return the name of an image to represent the village
	 */
	@Override
	public String getImage() {
		return "village.png";
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
		return this == obj || (obj instanceof Village && status.equals(((Village) obj).status)
				&& name.equals(((Village) obj).name)
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
	 *
	 * @param fix a fixture
	 * @return whether it's an identical-but-for-ID village.
	 */
	@Override
	public boolean equalsIgnoringID(final TileFixture fix) {
		return fix instanceof Village && status.equals(((Village) fix).status)
				&& name.equals(((Village) fix).name);
	}

	/**
	 * @return a clone of this object
	 */
	@Override
	public TileFixture deepCopy() {
		return new Village(status(), name(), getID(), getFile());
	}
	/**
	 * @return the name of the village
	 */
	@Override
	public String name() {
		return name;
	}
	/**
	 * @return the status of the village
	 */
	@Override
	public TownStatus status() {
		return status;
	}
	/**
	 * All villages are small.
	 * @return the size of the village.
	 */
	@Override
	public TownSize size() {
		return TownSize.Small;
	}
}
