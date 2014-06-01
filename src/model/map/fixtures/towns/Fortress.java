package model.map.fixtures.towns;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import model.map.FixtureIterable;
import model.map.HasImage;
import model.map.IFixture;
import model.map.Player;
import model.map.SubsettableFixture;
import model.map.TileFixture;
import model.map.fixtures.mobile.IUnit;

import org.eclipse.jdt.annotation.Nullable;

import util.NullCleaner;

/**
 * A fortress on the map. A player can only have one fortress per tile, but
 * multiple players may have fortresses on the same tile.
 *
 * FIXME: We need something about resources and buildings yet
 *
 * @author Jonathan Lovelace
 *
 */
public class Fortress implements HasImage, ITownFixture,
		FixtureIterable<IUnit>, SubsettableFixture {
	/**
	 * The name of an image to use for this particular fixture.
	 */
	private String image = "";

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
	private final List<IUnit> units; // Should this be a Set?

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
	public final Iterator<IUnit> iterator() {
		return NullCleaner.assertNotNull(units.iterator());
	}

	/**
	 * Add a unit to the fortress.
	 *
	 * @param unit the unit to add
	 */
	public final void addUnit(final IUnit unit) {
		units.add(unit);
	}

	/**
	 * Remove a unit from the fortress.
	 *
	 * @param unit the unit to remove
	 */
	public final void removeUnit(final IUnit unit) {
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
	public boolean equals(@Nullable final Object obj) {
		return this == obj || obj instanceof Fortress
				&& name.equals(((Fortress) obj).name)
				&& ((Fortress) obj).owner.getPlayerId() == owner.getPlayerId()
				&& ((Fortress) obj).units.containsAll(units)
				&& units.containsAll(((Fortress) obj).units)
				&& ((Fortress) obj).id == id;
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
		final String ownerStr = owner.toString();
		// Assume each unit is at least half a K.
		final int len = 40 + name.length() + ownerStr.length() + units.size()
				* 512;
		final StringBuilder sbuild = new StringBuilder(len).append("Fortress ");
		sbuild.append(name);
		sbuild.append(", owned by player ");
		sbuild.append(ownerStr);
		sbuild.append(". Units:");
		int count = 0;
		for (final IUnit unit : units) {
			sbuild.append("\n\t\t\t");
			sbuild.append(unit.getName());
			if (unit.getOwner().equals(owner)) {
				sbuild.append(" (");
				sbuild.append(unit.getKind());
				sbuild.append(')');
			} else if (unit.getOwner().isIndependent()) {
				sbuild.append(", an independent ");
				sbuild.append(unit.getKind());
			} else {
				sbuild.append(" (");
				sbuild.append(unit.getKind());
				sbuild.append("), belonging to ");
				sbuild.append(unit.getOwner());
			}
			if (++count < units.size() - 1) {
				sbuild.append(';');
			}
		}
		return NullCleaner.assertNotNull(sbuild.toString());
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
	 * @param ostream a stream to write details to
	 * @throws IOException on I/O error writing output to the stream
	 */
	@Override
	public boolean isSubset(final IFixture obj, final Appendable ostream)
			throws IOException {
		if (!(obj instanceof Fortress)) {
			ostream.append("Incompatible types");
			return false;
		}
		Fortress fort = (Fortress) obj;
		if (name.equals(fort.name)
				&& fort.owner.getPlayerId() == owner.getPlayerId()) {
			boolean retval = true;
			final Map<Integer, IUnit> ours = new HashMap<>();
			for (final IUnit unit : this) {
				ours.put(Integer.valueOf(unit.getID()), unit);
			}
			for (final IUnit unit : fort) {
				if (unit == null) {
					continue;
				} else if (!ours.containsKey(Integer.valueOf(unit.getID()))) {
					ostream.append("Extra unit in fortress ");
					ostream.append(getName());
					ostream.append(":\t");
					ostream.append(unit.toString());
					ostream.append(", ID #");
					ostream.append(Integer.toString(unit.getID()));
					ostream.append('\n');
					retval = false;
				} else if (!ours.get(Integer.valueOf(unit.getID())).isSubset(
						unit, ostream)) {
					retval = false;
				}
			}
			return retval;
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
		return this == fix || fix instanceof Fortress
				&& name.equals(((Fortress) fix).name)
				&& ((Fortress) fix).owner.getPlayerId() == owner.getPlayerId()
				&& ((Fortress) fix).units.containsAll(units)
				&& units.containsAll(((Fortress) fix).units);
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
	 *
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
	 * @return a string describing all fortresses as a class
	 */
	@Override
	public String plural() {
		return "Fortresses";
	}
	/**
	 * @return a short description of the fixture
	 */
	@Override
	public String shortDesc() {
		if (owner.isCurrent()) {
			return "a fortress, " + getName() + ", owned by you"; // NOPMD
		} else {
			return "a fortress, " + getName() + ", owned by " + owner.getName();
		}
	}
}
