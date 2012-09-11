package model.map.fixtures.towns;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import model.map.HasImage;
import model.map.Player;
import model.map.Subsettable;
import model.map.TileFixture;
import model.map.XMLWritableImpl;
import model.map.fixtures.mobile.Unit;

/**
 * A fortress on the map. A player can only have one fortress per tile, but
 * multiple players may have fortresses on the same tile.
 *
 * FIXME: We need something about resources and buildings yet
 *
 * @author Jonathan Lovelace
 *
 */
public class Fortress extends XMLWritableImpl implements HasImage,
		Subsettable<Fortress>, TownFixture {
	/**
	 * The player that owns the fortress.
	 */
	private final Player owner;
	/**
	 * The name of the fortress.
	 */
	private final String name;
	/**
	 * The units in the fortress.
	 */
	private final List<Unit> units; // Should this be a Set?

	/**
	 * Constructor.
	 *
	 * @param fortOwner the player that owns the fortress
	 * @param fortName the name of the fortress
	 * @param idNum the ID number.
	 * @param fileName the file this was loaded from
	 */
	public Fortress(final Player fortOwner, final String fortName,
			final int idNum, final String fileName) {
		super(fileName);
		owner = fortOwner;
		name = fortName;
		units = new ArrayList<Unit>();
		id = idNum;
	}

	/**
	 *
	 * @return the units in the fortress.
	 */
	public final List<Unit> getUnits() {
		return Collections.unmodifiableList(units);
	}

	/**
	 * Add a unit to the fortress.
	 *
	 * @param unit the unit to add
	 */
	public final void addUnit(final Unit unit) {
		units.add(unit);
	}

	/**
	 * Remove a unit from the fortress.
	 *
	 * @param unit the unit to remove
	 */
	public final void removeUnit(final Unit unit) {
		units.remove(unit);
	}

	/**
	 *
	 * @return the player that owns the fortress
	 */
	public final Player getOwner() {
		return owner;
	}

	/**
	 * @param obj an object
	 *
	 * @return whether it is an identical fortress
	 */
	@Override
	public boolean equals(final Object obj) {
		return this == obj
				|| (obj instanceof Fortress
						&& (name.equals(((Fortress) obj).name))
						&& ((Fortress) obj).owner.getPlayerId() == owner
								.getPlayerId()
						&& ((Fortress) obj).units.equals(units) && ((TileFixture) obj)
						.getID() == id);
	}

	/**
	 *
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return id;
	}

	/**
	 *
	 * @return a String representation of the object.
	 */
	@Override
	public String toString() {
		final StringBuilder sbuild = new StringBuilder("Fortress ");
		sbuild.append(name);
		sbuild.append(", owned by player ");
		sbuild.append(owner);
		sbuild.append(". Units:");
		for (final Unit unit : units) {
			sbuild.append("\n\t\t\t");
			sbuild.append(unit);
		}
		return sbuild.toString();
	}

	/**
	 * @param fix A TileFixture to compare to
	 * @return the result of the comparison
	 */
	@Override
	public int compareTo(final TileFixture fix) {
		return fix.hashCode() - hashCode();
	}

	/**
	 * TODO: Should perhaps be more granular.
	 *
	 * @return the name of an image to represent the fortress.
	 */
	@Override
	public String getImage() {
		return "fortress.png";
	}

	/**
	 * @return a z-value for use in determining the top fixture on a tile
	 */
	@Override
	public int getZValue() {
		return 60;
	}

	/**
	 * @param obj another Fortress
	 * @return whether it's a strict subset of this one
	 * @param out a stream to write details to
	 */
	@Override
	public boolean isSubset(final Fortress obj, final PrintStream out) {
		if (name.equals(obj.name)
				&& obj.owner.getPlayerId() == owner.getPlayerId()) {
			final Set<Unit> temp = new HashSet<Unit>(obj.units);
			temp.removeAll(units);
			return temp.isEmpty(); // NOPMD
		} else {
			return false;
		}
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
	 * FIXME: Uses equals() to compare units.
	 *
	 * @param fix a fixture
	 * @return whether it's identical to this except ID and DC.
	 */
	@Override
	public boolean equalsIgnoringID(final TileFixture fix) {
		return this == fix
				|| (fix instanceof Fortress
						&& (name.equals(((Fortress) fix).name))
						&& ((Fortress) fix).owner.getPlayerId() == owner
								.getPlayerId() && ((Fortress) fix).units
							.equals(units));
		// if (this == fix) {
		// return true;
		// } else if (fix instanceof Fortress
		// && (name.equals(((Fortress) fix).name))
		// && ((Fortress) fix).owner.getId() == owner.getId()) {
		// final Set<Unit> ours = Collections.synchronizedSet(new
		// HashSet<Unit>(units));
		// final Set<Unit> theirs = Collections.synchronizedSet(new
		// HashSet<Unit>(((Fortress) fix).units));
		// ours.removeAll(((Fortress) fix).units);
		// theirs.removeAll(units);
		// for (Unit one : ours) {
		// for (Unit two : theirs) {
		// if (one.equalsIgnoringID(two)) {
		// ours.remove(one);
		// theirs.remove(two);
		// }
		// }
		// }
		// return ours.isEmpty() && theirs.isEmpty();
		// } else {
		// return false;
		// }
	}

	/**
	 * @return the fortress's name.
	 */
	@Override
	public String name() {
		return name;
	}

	/**
	 * TODO: Add support for having a different status (but leave 'active' the
	 * default).
	 *
	 * @return the status of the fortress
	 */
	@Override
	public TownStatus status() {
		return TownStatus.Active;
	}
	/**
	 * TODO: Add support for different sizes (but leave 'small' the default).
	 * @return the size of the fortress.
	 */
	@Override
	public TownSize size() {
		return TownSize.Small;
	}
}
