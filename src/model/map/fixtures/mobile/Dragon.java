package model.map.fixtures.mobile;

import org.eclipse.jdt.annotation.Nullable;

import model.map.HasImage;
import model.map.HasKind;
import model.map.IFixture;
import model.map.TileFixture;
import model.map.fixtures.UnitMember;

/**
 * A dragon. TODO: should probably be a unit, or something.
 *
 * @author Jonathan Lovelace
 *
 */
public class Dragon implements MobileFixture, HasImage, HasKind, UnitMember {
	/**
	 * Version UID for serialization.
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * What kind of dragon. (Usually blank, at least at first.)
	 */
	private String kind;

	/**
	 * Constructor.
	 *
	 * @param dKind the kind of dragon
	 * @param idNum the ID number.
	 */
	public Dragon(final String dKind, final int idNum) {
		super();
		kind = dKind;
		id = idNum;
	}

	/**
	 * @return the kind of dragon
	 */
	@Override
	public String getKind() {
		return kind;
	}

	/**
	 * @return a String representation of the dragon
	 */
	@Override
	public String toString() {
		return kind + (kind.isEmpty() ? "dragon" : " dragon");
	}

	/**
	 * @return the name of an image to represent the fairy
	 */
	@Override
	public String getDefaultImage() {
		return "dragon.png";
	}

	/**
	 * @return a z-value for use in determining the top fixture on a tile
	 */
	@Override
	public int getZValue() {
		return 40;
	}

	/**
	 * @param obj an object
	 * @return whether it's equal to this one
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return this == obj
				|| (obj instanceof Dragon && ((Dragon) obj).kind.equals(kind) && ((TileFixture) obj)
						.getID() == id);
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
		return fix instanceof Dragon && ((Dragon) fix).kind.equals(kind);
	}

	/**
	 * @param nKind the new kind
	 */
	@Override
	public final void setKind(final String nKind) {
		kind = nKind;
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
	 * @return a string describing all dragons as a class
	 */
	@Override
	public String plural() {
		return "Dragons";
	}
}
