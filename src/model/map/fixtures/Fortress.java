package model.map.fixtures;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import model.map.HasImage;
import model.map.Player;
import model.map.Subsettable;
import model.map.TileFixture;

/**
 * A fortress on the map. A player can only have one fortress per tile, but
 * multiple players may have fortresses on the same tile.
 * 
 * FIXME: We need something about resources and buildings yet
 * 
 * @author Jonathan Lovelace
 * 
 */
public class Fortress implements TileFixture, HasImage, Subsettable<Fortress> {
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
	 * @param fortOwner
	 *            the player that owns the fortress
	 * @param fortName
	 *            the name of the fortress
	 * @param idNum the ID number.
	 */
	public Fortress(final Player fortOwner, final String fortName, final long idNum) {
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
	 * @param unit
	 *            the unit to add
	 */
	public final void addUnit(final Unit unit) {
		units.add(unit);
	}

	/**
	 * Remove a unit from the fortress.
	 * 
	 * @param unit
	 *            the unit to remove
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
	 * 
	 * @return the name of the fortress
	 */
	public final String getName() {
		return name;
	}

	/**
	 * @param obj
	 *            an object
	 * 
	 * @return whether it is an identical fortress
	 */
	@Override
	public boolean equals(final Object obj) {
		return this == obj
				|| (obj instanceof Fortress
						&& (name.equals(((Fortress) obj).name))
						&& ((Fortress) obj).owner.equals(owner) && ((Fortress) obj).units
							.equals(units) && ((TileFixture) obj).getID() == id);
	}

	/**
	 * 
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return (int) id;
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
	 * @param fix
	 *            A TileFixture to compare to
	 * @return the result of the comparison
	 */
	@Override
	public int compareTo(final TileFixture fix) {
		return fix.hashCode() - hashCode();
	}
	/**
	 * @return an XML representation of the fortress.
	 */
	@Override
	public String toXML() {
		final StringBuilder sbuild = new StringBuilder("<fortress owner=\"");
		sbuild.append(owner.getId());
		if (!"".equals(name)) {
			sbuild.append("\" name=\"");
			sbuild.append(name);
		}
		sbuild.append("\" id=\"");
		sbuild.append(id);
		sbuild.append("\">");
		if (!units.isEmpty()) {
			sbuild.append('\n');
			for (final Unit unit : units) {
				sbuild.append("\t\t\t\t");
				sbuild.append(unit.toXML());
			}
			sbuild.append("\t\t\t");
		}
		sbuild.append("</fortress>");
		return sbuild.toString();
	}
	/**
	 * TODO: Should perhaps be more granular.
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
	 */
	@Override
	public boolean isSubset(final Fortress obj) {
		if (name.equals(obj.name) && obj.owner.equals(owner)) {
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
	private final long id; // NOPMD
	/**
	 * @return a UID for the fixture.
	 */
	@Override
	public long getID() {
		return id;
	}
	/**
	 * FIXME: Uses equals() to compare units.
	 * @param fix a fixture
	 * @return whether it's identical to this except ID and DC.
	 */
	@Override
	public boolean equalsIgnoringID(final TileFixture fix) {
		return this == fix
				|| (fix instanceof Fortress
						&& (name.equals(((Fortress) fix).name))
						&& ((Fortress) fix).owner.equals(owner) && ((Fortress) fix).units
							.equals(units));
	}
}
