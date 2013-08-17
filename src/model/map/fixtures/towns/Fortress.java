package model.map.fixtures.towns;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import model.map.FixtureIterable;
import model.map.HasImage;
import model.map.IFixture;
import model.map.Player;
import model.map.Subsettable;
import model.map.TileFixture;
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
public class Fortress implements HasImage,
		Subsettable<Fortress>, ITownFixture, FixtureIterable<Unit> {
	/**
	 * Version UID for serialization.
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * The player that owns the fortress.
	 */
	private Player owner;
	/**
	 * The name of the fortress.
	 */
	private String name;
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
	 */
	public Fortress(final Player fortOwner, final String fortName,
			final int idNum) {
		super();
		owner = fortOwner;
		name = fortName;
		units = new ArrayList<>();
		id = idNum;
	}

	/**
	 *
	 * @return the units in the fortress.
	 */
	@Override
	public final Iterator<Unit> iterator() {
		return units.iterator();
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
	@Override
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
						&& ((Fortress) obj).units.containsAll(units)
						&& units.containsAll(((Fortress) obj).units) && ((TileFixture) obj)
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
		int count = 0;
		for (final Unit unit : units) {
			sbuild.append("\n\t\t\t");
			sbuild.append(unit.toStringInner(owner));
			if (++count < units.size() - 1) {
				sbuild.append(';');
			}
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
	public String getDefaultImage() {
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
	public boolean isSubset(final Fortress obj, final PrintWriter out) {
		if (name.equals(obj.name)
				&& obj.owner.getPlayerId() == owner.getPlayerId()) {
			final Set<Unit> temp = new HashSet<>(obj.units);
			// TODO: Differences between _versions_ of a unit
			temp.removeAll(units);
			for (final Unit unit : temp) {
				out.print("Extra unit in fortress ");
				out.print(getName());
				out.print(":\t");
				out.println(unit.toString());
			}
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
	 * @param fix a fixture
	 * @return whether it's identical to this except ID and DC.
	 */
	@Override
	public boolean equalsIgnoringID(final IFixture fix) {
		return this == fix
				|| (fix instanceof Fortress
						&& (name.equals(((Fortress) fix).name))
						&& ((Fortress) fix).owner.getPlayerId() == owner
								.getPlayerId()
						&& ((Fortress) fix).units.containsAll(units) && units
							.containsAll(((Fortress) fix).units));
	}

	/**
	 * @return the fortress's name.
	 */
	@Override
	public String getName() {
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
	/**
	 * @param player the fort's new owner
	 */
	@Override
	public final void setOwner(final Player player) {
		owner = player;
	}
	/**
	 * @param nomen the fort's new name
	 */
	@Override
	public final void setName(final String nomen) {
		name = nomen;
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
}
