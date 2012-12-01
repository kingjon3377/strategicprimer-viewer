package model.map.fixtures.mobile;

import java.util.Iterator;
import java.util.Set;

import model.map.HasImage;
import model.map.HasKind;
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
		HasKind, Iterable<UnitMember> {
	/**
	 * The player that owns the unit.
	 */
	private final Player owner;
	/**
	 * What kind of unit this is.
	 */
	private final String kind;
	/**
	 * The name of this unit.
	 */
	private final String name;

	/**
	 * The members of the unit.
	 */
	private final Set<UnitMember> members = new ArraySet<UnitMember>();
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
	}

	/**
	 *
	 * @return the player that owns the unit
	 */
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
	 *
	 * @return a String representation of the Unit.
	 */
	@Override
	public String toString() {
		return "Unit of type " + kind + ", belonging to player " + owner
				+ ", named " + name;
	}
	/**
	 * @return a verbose description of the Unit.
	 */
	public String verbose() {
		final StringBuilder builder = new StringBuilder(toString());
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
	public String getImage() {
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
	public boolean equalsIgnoringID(final TileFixture fix) {
		return this == fix
				|| (fix instanceof Unit
						&& ((Unit) fix).owner.getPlayerId() == owner
								.getPlayerId()
						&& (((Unit) fix).kind.equals(kind)) && (((Unit) fix).name
							.equals(name)));
	}
}
