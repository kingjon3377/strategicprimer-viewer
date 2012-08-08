package model.map.fixtures;

import model.map.HasImage;
import model.map.TileFixture;

/**
 * An ogre. TODO: should probably be a unit, or something.
 *
 * @author Jonathan Lovelace
 *
 */
public class Ogre implements TileFixture, HasImage {
	/**
	 * @param idNum the ID number.
	 * @param fileName the file this was loaded from
	 */
	public Ogre(final int idNum, final String fileName) {
		id = idNum;
		file = fileName;
	}

	/**
	 * @return an XML representation of the ogre
	 * @deprecated Replaced by SPIntermediateRepresentation-based output
	 */
	@Override
	@Deprecated
	public String toXML() {
		return new StringBuilder("<ogre id=\"").append(id).append("\" />")
				.toString();
	}

	/**
	 * @return a String representation of the ogre
	 */
	@Override
	public String toString() {
		return "ogre";
	}

	/**
	 * @return the name of an image to represent the ogre
	 */
	@Override
	public String getImage() {
		return "ogre.png";
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
		return obj instanceof Ogre && id == ((TileFixture) obj).getID();
	}

	/**
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return id;
	}

	/**
	 * @param fix a TileFixture to compare to
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
		return fix instanceof Ogre;
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
		return new Ogre(getID(), getFile());
	}
}
