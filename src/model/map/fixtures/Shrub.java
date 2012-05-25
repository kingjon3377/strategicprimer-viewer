package model.map.fixtures;

import model.map.HasImage;
import model.map.TileFixture;

/**
 * A TileFixture to represent shrubs, or their aquatic equivalents, on a tile.
 * @author Jonathan Lovelace
 *
 */
public class Shrub implements TileFixture, HasImage {
	/**
	 * @return an XML representation of the Fixture.
	 */
	@Override
	@Deprecated
	public String toXML() {
		return new StringBuilder("<shrub kind=\"").append(description)
				.append("\" id=\"").append(id).append("\" />").toString();
	}
	/**
	 * A description of what kind of shrub this is.
	 */
	private final String description;
	/**
	 * Constructor.
	 * @param desc a description of the shrub.
	 * @param idNum the ID number.
	 * @param fileName the file this was loaded from
	 */
	public Shrub(final String desc, final int idNum, final String fileName) {
		description = desc;
		id = idNum;
		file = fileName;
	}
	/**
	 * @return a description of the shrub
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @return the name of an image to represent the shrub.
	 */
	@Override
	public String getImage() {
		return "shrub.png";
	}
	/**
	 * @return the description of the shrub
	 */
	@Override
	public String toString() {
		return getDescription();
	}
	/**
	 * @return a z-value for use in determining the top fixture on a tile
	 */
	@Override
	public int getZValue() {
		return 15;
	}
	/**
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(final Object obj) {
		return obj instanceof Shrub
				&& description.equals(((Shrub) obj).description)
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
		return fix instanceof Shrub
				&& description.equals(((Shrub) fix).description);
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
		return new Shrub(getDescription(), getID(), getFile());
	}
}
