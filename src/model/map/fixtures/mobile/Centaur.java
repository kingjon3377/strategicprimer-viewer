package model.map.fixtures.mobile;

import model.map.HasImage;
import model.map.HasKind;
import model.map.TileFixture;
import model.map.XMLWritableImpl;
import model.map.fixtures.UnitMember;

/**
 * A centaur. TODO: Should probably be a kind of unit instead, or something ...
 *
 * @author Jonathan Lovelace
 *
 */
public class Centaur extends XMLWritableImpl implements MobileFixture, HasImage, HasKind, UnitMember {
	/**
	 * What kind of centaur.
	 */
	private final String kind;

	/**
	 * Constructor.
	 *
	 * @param centKind the kind of centaur
	 * @param idNum the ID number.
	 * @param fileName the file this was loaded from
	 */
	public Centaur(final String centKind, final int idNum, final String fileName) {
		super(fileName);
		kind = centKind;
		id = idNum;
	}

	/**
	 * @return the kind of centaur
	 */
	@Override
	public String getKind() {
		return kind;
	}

	/**
	 * @return a String representation of the centaur
	 */
	@Override
	public String toString() {
		return kind + " centaur";
	}

	/**
	 * @return the name of an image to represent the centaur
	 */
	@Override
	public String getImage() {
		return "centaur.png";
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
		return this == obj || (obj instanceof Centaur && ((Centaur) obj).kind.equals(kind)
				&& ((TileFixture) obj).getID() == id);
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
		return fix instanceof Centaur && ((Centaur) fix).kind.equals(kind);
	}
}
