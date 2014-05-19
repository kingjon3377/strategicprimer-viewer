package model.map.fixtures.mobile;

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import model.map.FixtureIterable;
import model.map.HasImage;
import model.map.HasKind;
import model.map.HasName;
import model.map.HasOwner;
import model.map.IFixture;
import model.map.Player;
import model.map.Subsettable;
import model.map.TileFixture;
import model.map.fixtures.UnitMember;

import org.eclipse.jdt.annotation.Nullable;

import util.ArraySet;
import util.NullCleaner;

/**
 * A unit on the map.
 *
 * @author Jonathan Lovelace
 *
 */
public class Unit implements MobileFixture, HasImage, HasKind,
		FixtureIterable<UnitMember>, HasName, HasOwner, Subsettable<Unit> {
	/**
	 * The name of an image to use for this particular fixture.
	 */
	private String image = "";

	/**
	 * The unit's orders. This is serialized to and from XML, but does not
	 * affect equality or hashing, and is not printed in toString.
	 */
	private String orders;

	/**
	 * The player that owns the unit.
	 */
	private Player owner;
	/**
	 * What kind of unit this is.
	 */
	private String kind;
	/**
	 * The name of this unit.
	 */
	private String name;

	/**
	 * The members of the unit.
	 */
	private final Set<UnitMember> members = new ArraySet<>();

	/**
	 * FIXME: We need some more members -- something about stats. What else?
	 *
	 * Constructor.
	 *
	 * @param unitOwner the player that owns the unit
	 * @param unitType the type of unit
	 * @param unitName the name of this unit
	 * @param idNum the ID number.
	 */
	public Unit(final Player unitOwner, final String unitType,
			final String unitName, final int idNum) {
		super();
		owner = unitOwner;
		kind = unitType;
		name = unitName;
		id = idNum;
		orders = "";
	}

	/**
	 *
	 * @return the player that owns the unit
	 */
	@Override
	public final Player getOwner() {
		return owner;
	}

	/**
	 *
	 * @return the kind of unit
	 */
	@Override
	public final String getKind() {
		return kind;
	}

	/**
	 *
	 * @return the name of the unit
	 */
	@Override
	public final String getName() {
		return name;
	}

	/**
	 * Add a member.
	 *
	 * @param member the member to add
	 */
	public void addMember(final UnitMember member) {
		members.add(member);
	}

	/**
	 * Remove a member from the unit.
	 *
	 * @param member the member to remove
	 */
	public final void removeMember(final UnitMember member) {
		members.remove(member);
	}

	/**
	 * @return an iterator over the unit's members
	 */
	@Override
	public final Iterator<UnitMember> iterator() {
		return NullCleaner.assertNotNull(members.iterator());
	}

	/**
	 * @param obj an object
	 *
	 * @return whether it's an identical Unit.
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return this == obj || obj instanceof Unit
				&& ((Unit) obj).owner.getPlayerId() == owner.getPlayerId()
				&& ((Unit) obj).kind.equals(kind)
				&& ((Unit) obj).name.equals(name)
				&& ((Unit) obj).members.equals(members)
				&& ((Unit) obj).id == id;
	}

	/**
	 *
	 * @return a hash-code for the object
	 */
	@Override
	public int hashCode() {
		return id;
	}

	/**
	 * TODO: Use a StringBuilder, specifying the size.
	 *
	 * @return a String representation of the Unit.
	 */
	@Override
	public String toString() {
		if (owner.isIndependent()) {
			return "Independent unit of type " + kind + ", named " + name; // NOPMD
		} else {
			return "Unit of type " + kind + ", belonging to " + owner
					+ ", named " + name;
		}
	}

	/**
	 * @return a verbose description of the Unit.
	 */
	public String verbose() {
		// Assume each member is half a K.
		final String orig = toString();
		final int len = orig.length() + members.size() * 512;
		final StringBuilder builder = new StringBuilder(len).append(orig);
		builder.append(", consisting of:");
		for (final UnitMember member : members) {
			builder.append('\n');
			builder.append(member.toString());
		}
		return NullCleaner.assertNotNull(builder.toString());
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
	 * TODO: Should be per-unit-type ...
	 *
	 * This image from OpenGameArt.org, uploaded by jreijonen,
	 * http://opengameart.org/content/faction-symbols-allies-axis .
	 *
	 * @return the name of an image to represent the unit.
	 */
	@Override
	public String getDefaultImage() {
		return "unit.png";
	}

	/**
	 * TODO: But how to determine which unit?
	 *
	 * @return a z-value for use in determining the top fixture on a tile
	 */
	@Override
	public int getZValue() {
		return 70;
	}

	/**
	 * ID number.
	 */
	private final int id; // NOPMD

	/**
	 * @return a UID for the fixture.
	 */
	@Override
	public final int getID() {
		return id;
	}

	/**
	 *
	 * @param fix a fixture
	 * @return whether it's an identical-except-ID unit.
	 */
	@Override
	public boolean equalsIgnoringID(final IFixture fix) {
		return this == fix || fix instanceof Unit
				&& ((Unit) fix).owner.getPlayerId() == owner.getPlayerId()
				&& ((Unit) fix).kind.equals(kind)
				&& ((Unit) fix).name.equals(name);
	}

	/**
	 * @param player the town's new owner
	 */
	@Override
	public final void setOwner(final Player player) {
		owner = player;
	}

	/**
	 * @param nomen the unit's new name
	 */
	@Override
	public final void setName(final String nomen) {
		name = nomen;
	}

	/**
	 * @param nKind the new kind
	 */
	@Override
	public final void setKind(final String nKind) {
		kind = nKind;
	}

	/**
	 * @param newOrders the unit's new orders
	 */
	public final void setOrders(final String newOrders) {
		orders = newOrders;
	}

	/**
	 * @return the unit's orders
	 */
	public String getOrders() {
		return orders;
	}

	/**
	 * @param img the name of an image to use for this particular fixture
	 */
	@Override
	public final void setImage(final String img) {
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
	 * @return a phrase describing all units as a class
	 */
	@Override
	public String plural() {
		return "Units";
	}
	/**
	 * @return a short description of the fixture
	 */
	@Override
	public String shortDesc() {
		if (owner.isCurrent()) {
			return "a(n) " + getKind() + " unit belonging to you"; // NOPMD
		} else {
			return "a(n) " + getKind() + " unit belonging to "
					+ owner.getName();
		}
	}
	/**
	 * @param obj another unit
	 * @param ostream the stream to report results on
	 * @return whether the unit is a strict subset of this one.
	 */
	@Override
	public boolean isSubset(final Unit obj, final PrintWriter ostream) {
		if (obj.getID() != id) {
			ostream.println("Units have different IDs");
			return false; // NOPMD
		} else if (obj.owner.getPlayerId() != owner.getPlayerId()) {
			ostream.print("Unit of ID #");
			ostream.print(id);
			ostream.println(":\tOwners differ");
			return false; // NOPMD
		} else if (!name.equals(obj.getName())) {
			ostream.print("Unit of ID #");
			ostream.print(id);
			ostream.println(":\tNames differ");
			return false; // NOPMD
		} else if (!kind.equals(obj.getKind())) {
			ostream.print("Unit of ID #");
			ostream.print(id);
			ostream.println(":\tKinds differ");
			return false; // NOPMD
		} else {
			final Set<UnitMember> theirs = new HashSet<>(obj.members);
			theirs.removeAll(members);
			for (final UnitMember member : theirs) {
				ostream.print("In unit of ID #");
				ostream.print(id);
				ostream.print(" of kind ");
				ostream.print(kind);
				ostream.print(" named ");
				ostream.print(name);
				ostream.print(": Extra member:\t");
				ostream.print(member.toString());
				ostream.print(", ID #");
				ostream.println(member.getID());
			}
			return theirs.isEmpty();
		}
	}
}
