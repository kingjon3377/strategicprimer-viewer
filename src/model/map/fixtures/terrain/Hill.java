package model.map.fixtures.terrain;

import model.map.HasImage;
import model.map.IFixture;
import model.map.TerrainFixture;
import model.map.TileFixture;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A hill on the map. Should increase unit's effective vision by a small
 * fraction when the unit is on it, if not in forest.
 *
 * @author Jonathan Lovelace
 *
 */
public class Hill implements TerrainFixture, HasImage {
	/**
	 * Version UID for serialization.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param idNum the ID number.
	 */
	public Hill(final int idNum) {
		super();
		id = idNum;
	}

	/**
	 * @return a String representation of the hill.
	 */
	@Override
	public String toString() {
		return "Hill";
	}

	/**
	 * @return the name of an image to represent the hill.
	 */
	@Override
	public String getDefaultImage() {
		return "hill.png";
	}

	/**
	 * @return a z-value for use in determining the top fixture on a tile
	 */
	@Override
	public int getZValue() {
		return 5;
	}

	/**
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return this == obj
				|| (obj instanceof Hill && id == ((TileFixture) obj).getID());
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
		return fix instanceof Hill;
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
	 * @return a string describing all hills as a class
	 */
	@Override
	public String plural() {
		return "Hills";
	}
}
