package model.map.fixtures.mobile;

import java.util.Iterator;
import java.util.Set;

import model.map.FixtureIterable;
import model.map.HasImage;
import model.map.HasKind;
import model.map.HasName;
import model.map.HasOwner;
import model.map.IFixture;
import model.map.Player;
import model.map.TileFixture;
import model.map.fixtures.UnitMember;
import util.ArraySet;

/**
 * A unit on the map.
 *
 * @author Jonathan Lovelace
 *
 */
public class Unit implements MobileFixture, HasImage,
		HasKind, FixtureIterable<UnitMember>, HasName, HasOwner {
	/**
	 * Version UID for serialization.
	 */
	private static final long serialVersionUID = 1L;
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
	 * @param member the member to add
	 */
	public void addMember(final UnitMember member) {
		members.add(member);
	}
	/**
	 * Remove a member from the unit.
	 * @param member the member to remove
	 */
	public void removeMember(final UnitMember member) {
		members.remove(member);
	}
	/**
	 * @return an iterator over the unit's members
	 */
	@Override
	public Iterator<UnitMember> iterator() {
		return members.iterator();
	}
	/**
	 * @param obj an object
	 *
	 * @return whether it's an identical Unit.
	 */
	@Override
	public boolean equals(final Object obj) {
		return this == obj
				|| (obj instanceof Unit
						&& ((Unit) obj).owner.getPlayerId() == owner
								.getPlayerId()
						&& (((Unit) obj).kind.equals(kind))
						&& (((Unit) obj).name.equals(name))
						&& (((Unit) obj).members.equals(members)) && ((TileFixture) obj)
						.getID() == id);
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
	 * @return a String representation of the Unit.
	 */
	@Override
	public String toString() {
		return owner.isIndependent() ? "Independent unit of type " + kind
				+ ", named " + name : "Unit of type " + kind
				+ ", belonging to " + owner + ", named " + name;
	}
	/**
	 * TODO: Use a StringBuilder, specifying the size.
	 * @param player the container's player
	 * @return a briefer String representation of the Unit, only naming the owner if different
	 */
	public String toStringInner(final Player player) {
		return owner.equals(player) ? name + " (" + kind + ')' : (owner
				.isIndependent() ? name + ", an independent " + kind : name
				+ " (" + kind + "), belonging to " + owner);
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
		return builder.toString();
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
	public int getID() {
		return id;
	}

	/**
	 *
	 * @param fix a fixture
	 * @return whether it's an identical-except-ID unit.
	 */
	@Override
	public boolean equalsIgnoringID(final IFixture fix) {
		return this == fix
				|| (fix instanceof Unit
						&& ((Unit) fix).owner.getPlayerId() == owner
								.getPlayerId()
						&& (((Unit) fix).kind.equals(kind)) && (((Unit) fix).name
							.equals(name)));
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
	 * The unit's orders. This is serialized to and from XML, but does not
	 * affect equality or hashing, and is not printed in toString.
	 */
	private String orders;
	/**
	 * @param newOrders the unit's new orders
	 */
	public void setOrders(final String newOrders) {
		orders = newOrders;
	}
	/**
	 * @return the unit's orders
	 */
	public String getOrders() {
		return orders;
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
	 * @return a phrase describing all units as a class
	 */
	@Override
	public String plural() {
		return "Units";
	}
}
