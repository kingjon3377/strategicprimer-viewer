package model.map.fixtures.mobile;

import java.io.PrintWriter;

import model.map.HasImage;
import model.map.HasKind;
import model.map.IFixture;
import model.map.TileFixture;
import model.map.fixtures.UnitMember;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A giant. TODO: should probably be a unit, or something.
 *
 * @author Jonathan Lovelace
 *
 */
public class Giant implements MobileFixture, HasImage, HasKind, UnitMember {
	/**
	 * The name of an image to use for this particular fixture.
	 */
	private String image = "";

	/**
	 * What kind of giant. (Usually blank, at least at first.)
	 */
	private String kind;

	/**
	 * Constructor.
	 *
	 * @param gKind the kind of giant
	 * @param idNum the ID number.
	 */
	public Giant(final String gKind, final int idNum) {
		super();
		kind = gKind;
		id = idNum;
	}

	/**
	 * @return the kind of giant
	 */
	@Override
	public String getKind() {
		return kind;
	}

	/**
	 * @return a String representation of the giant
	 */
	@Override
	public String toString() {
		if (kind.isEmpty()) {
			return "giant"; // NOPMD
		} else {
			return kind + " giant";
		}
	}

	/**
	 * @return the name of an image to represent the fairy
	 */
	@Override
	public String getDefaultImage() {
		return "giant.png";
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
		return this == obj || obj instanceof Giant
				&& ((Giant) obj).kind.equals(kind)
				&& id == ((Giant) obj).getID();
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
		return fix instanceof Giant && ((Giant) fix).kind.equals(kind);
	}

	/**
	 * @param obj another UnitMember
	 * @param ostream a stream to report an explanation on
	 * @return whether that member equals this one
	 */
	@Override
	public boolean isSubset(final UnitMember obj, final PrintWriter ostream) {
		if (obj.getID() == id) {
			if (obj instanceof Giant) {
				if (kind.equals(((Giant) obj).getKind())) {
					return true;
				} else {
					ostream.print("Different kinds of giant for ID #");
					ostream.println(id);
					return false;
				}
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
	 * @param nKind the new kind
	 */
	@Override
	public final void setKind(final String nKind) {
		kind = nKind;
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
	 * @return a string describing all giants as a class
	 */
	@Override
	public String plural() {
		return "Giants";
	}
	/**
	 * @return a short description of the fixture
	 */
	@Override
	public String shortDesc() {
		return toString();
	}
}
