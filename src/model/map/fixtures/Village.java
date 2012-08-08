package model.map.fixtures;

import model.map.HasImage;
import model.map.TileFixture;
import model.map.events.TownStatus;

/**
 * A village on the map.
 *
 * @author Jonathan Lovelace
 *
 */
public class Village implements TileFixture, HasImage {
	/**
	 * The status of the village.
	 */
	private final TownStatus status;
	/**
	 * The name of the village.
	 */
	private final String name;

	/**
	 * @return the name of the village
	 */
	public String getName() {
		return name;
	}

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
		status = vstatus;
		name = vName;
		id = idNum;
		file = fileName;
	}

	/**
	 * @return the status of the village
	 */
	public TownStatus getStatus() {
		return status;
	}

	/**
	 * @return an XML representation of the village
	 * @deprecated Replaced by SPIntermediateRepresentation-based output
	 */
	@Override
	@Deprecated
	public String toXML() {
		final StringBuilder sbuild = new StringBuilder("<village status=\"");
		sbuild.append(status.toString());
		if (!name.isEmpty()) {
			sbuild.append("\" name=\"");
			sbuild.append(name);
		}
		sbuild.append("\" id=\"");
		sbuild.append(id);
		sbuild.append("\" />");
		return sbuild.toString();
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
		return obj instanceof Village && status.equals(((Village) obj).status)
				&& name.equals(((Village) obj).name)
				&& id == ((TileFixture) obj).getID();
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

	/**
	 * @return a clone of this object
	 */
	@Override
	public TileFixture deepCopy() {
		return new Village(getStatus(), getName(), getID(), getFile());
	}
}
