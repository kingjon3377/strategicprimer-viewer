package model.map.fixtures.mobile;

import java.io.PrintWriter;

import model.map.HasImage;
import model.map.IFixture;
import model.map.TileFixture;
import model.map.fixtures.UnitMember;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A troll. TODO: should probably be a unit, or something.
 *
 * @author Jonathan Lovelace
 *
 */
public class Troll implements MobileFixture, HasImage, UnitMember {
	/**
	 * The name of an image to use for this particular fixture.
	 */
	private String image = "";

	/**
	 * Constructor.
	 *
	 * @param idNum the ID number.
	 */
	public Troll(final int idNum) {
		super();
		id = idNum;
	}

	/**
	 * @return a String representation of the djinn
	 */
	@Override
	public String toString() {
		return "troll";
	}

	/**
	 * @return the name of an image to represent the troll
	 */
	@Override
	public String getDefaultImage() {
		return "troll.png";
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
		return this == obj || obj instanceof Troll
				&& ((TileFixture) obj).getID() == id;
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
	 * @return whether it's identical except ID.
	 */
	@Override
	public boolean equalsIgnoringID(final IFixture fix) {
		return fix instanceof Troll;
	}

	/**
	 * @param obj another UnitMember
	 * @param ostream a stream to report an explanation on
	 * @return whether that member equals this one
	 */
	@Override
	public boolean isSubset(final UnitMember obj, final PrintWriter ostream) {
		if (obj.getID() == id) {
			if (obj instanceof Troll) {
				return true;
			} else {
				ostream.print("For ID #");
				ostream.print(id);
				ostream.print(", different kinds of members");
				return false;
			}
		} else {
			ostream.print("Called with different IDs, #");
			ostream.print(id);
			ostream.print(" and #");
			ostream.println(obj.getID());
			return false;
		}
	}
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
	 * @return a string describing all trolls as a class
	 */
	@Override
	public String plural() {
		return "Trolls";
	}
	/**
	 * @return a short description of the fixture
	 */
	@Override
	public String shortDesc() {
		return "a troll";
	}
}
