package model.map.fixtures.mobile;

import org.eclipse.jdt.annotation.Nullable;

import model.map.HasImage;
import model.map.IFixture;
import model.map.TileFixture;
import model.map.fixtures.UnitMember;

/**
 * A phoenix. TODO: should probably be a unit, or something.
 *
 * @author Jonathan Lovelace
 *
 */
public class Phoenix implements MobileFixture, HasImage, UnitMember {
	/**
	 * Version UID for serialization.
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * @param idNum the ID number.
	 */
	public Phoenix(final int idNum) {
		super();
		id = idNum;
	}

	/**
	 * @return a String representation of the djinn
	 */
	@Override
	public String toString() {
		return "phoenix";
	}

	/**
	 * @return the name of an image to represent the phoenix
	 */
	@Override
	public String getDefaultImage() {
		return "phoenix.png";
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
	public boolean equals(@Nullable final Object obj) {
		return this == obj || (obj instanceof Phoenix && id == ((TileFixture) obj).getID());
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
	public boolean equalsIgnoringID(final IFixture fix) {
		return fix instanceof Phoenix;
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
	 * @return a String describing all phoenixes as a class
	 */
	@Override
	public String plural() {
		return "Phoenixes";
	}
}
