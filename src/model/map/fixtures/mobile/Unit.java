package model.map.fixtures.mobile;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import model.map.IFixture;
import model.map.Player;
import model.map.fixtures.UnitMember;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import util.ArraySet;
import util.NullCleaner;
import util.TypesafeLogger;

/**
 * A unit on the map.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2012-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public class Unit implements IUnit {
	/**
	 * The name of an image to use for this particular fixture.
	 */
	private String image = "";

	/**
	 * The unit's orders. This is serialized to and from XML, but does not affect
	 * equality
	 * or hashing, and is not printed in toString.
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
	private final Collection<UnitMember> members = new ArraySet<>();

	/**
	 * FIXME: We need some more members -- something about stats. What else?
	 *
	 * Constructor.
	 *
	 * @param unitOwner the player that owns the unit
	 * @param unitType  the type of unit
	 * @param unitName  the name of this unit
	 * @param idNum     the ID number.
	 */
	public Unit(final Player unitOwner, final String unitType,
				final String unitName, final int idNum) {
		owner = unitOwner;
		kind = unitType;
		name = unitName;
		id = idNum;
		orders = "";
	}

	/**
	 * TODO: There should be some way to convey the unit's *size* without the
	 * *details* of its contents. Or maybe we should give the contents but not *their*
	 * details?
	 *
	 * @param zero whether to omit its contents and orders
	 * @return a copy of this unit
	 */
	@Override
	public Unit copy(final boolean zero) {
		final Unit retval = new Unit(owner, kind, name, id);
		if (!zero) {
			retval.orders = orders;
			for (final UnitMember member : this) {
				retval.addMember(member.copy(false));
			}
		}
		retval.image = image;
		return retval;
	}

	/**
	 * @return the player that owns the unit
	 */
	@Override
	public final Player getOwner() {
		return owner;
	}

	/**
	 * @return the kind of unit
	 */
	@Override
	public final String getKind() {
		return kind;
	}

	/**
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
	@Override
	public void addMember(final UnitMember member) {
		if (member instanceof ProxyFor) {
			TypesafeLogger.getLogger(Unit.class).log(Level.SEVERE,
					"ProxyWorker added to Unit",
					new IllegalStateException("ProxyWorker added to Unit"));
		}
		members.add(member);
	}

	/**
	 * Remove a member from the unit.
	 *
	 * @param member the member to remove
	 */
	@Override
	public final void removeMember(final UnitMember member) {
		members.remove(member);
	}

	/**
	 * @return an iterator over the unit's members
	 */
	@Override
	@NonNull
	public final Iterator<@NonNull UnitMember> iterator() {
		return NullCleaner.assertNotNull(members.iterator());
	}

	/**
	 * @param obj an object
	 * @return whether it's an identical Unit.
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return (this == obj) || ((obj instanceof IUnit)
										 && (((IUnit) obj).getOwner().getPlayerId() ==
													 owner.getPlayerId())
										 && ((IUnit) obj).getKind().equals(kind)
										 && ((IUnit) obj).getName().equals(name)
										 && areMembersEqual((IUnit) obj)
										 && (((IUnit) obj).getID() == id));
	}

	/**
	 * @param obj another unit
	 * @return whether its "members" are the same as ours
	 */
	private boolean areMembersEqual(final Iterable<UnitMember> obj) {
		final Collection<UnitMember> theirs =
				StreamSupport.stream(obj.spliterator(), false)
						.collect(Collectors.toSet());
		return members.containsAll(theirs) && theirs.containsAll(members);
	}

	/**
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
	@Override
	public String verbose() {
		// Assume each member is half a K.
		final String orig = toString();
		final int len = orig.length() + (members.size() * 512);
		final StringBuilder builder = new StringBuilder(len).append(orig);
		builder.append(", consisting of:");
		for (final UnitMember member : members) {
			builder.append('\n');
			builder.append(member.toString());
		}
		return NullCleaner.assertNotNull(builder.toString());
	}

	/**
	 * TODO: Should be per-unit-type ...
	 *
	 * This image from OpenGameArt.org, uploaded by jreijonen, http://opengameart
	 * .org/content/faction-symbols-allies-axis
	 * .
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
	 * FIXME: Should this look at unit members?
	 *
	 * @param fix a fixture
	 * @return whether it's an identical-except-ID unit.
	 */
	@Override
	public boolean equalsIgnoringID(final IFixture fix) {
		return (this == fix) || ((fix instanceof IUnit)
										 && (((IUnit) fix).getOwner().getPlayerId() ==
													 owner.getPlayerId())
										 && ((IUnit) fix).getKind().equals(kind)
										 && ((IUnit) fix).getName().equals(name));
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
	@Override
	public final void setOrders(final String newOrders) {
		orders = newOrders;
	}

	/**
	 * @return the unit's orders
	 */
	@Override
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
			return "a(n) " + kind + " unit belonging to you"; // NOPMD
		} else {
			return "a(n) " + kind + " unit belonging to " + owner.getName();
		}
	}

	/**
	 * @param obj     another unit
	 * @param ostream the stream to report results on
	 * @param context a string to print before every line of output, describing the
	 *                context
	 * @return whether the unit is a strict subset of this one.
	 * @throws IOException on I/O error writing output to the stream
	 */
	@Override
	public boolean isSubset(final IFixture obj, final Appendable ostream,
							final String context) throws IOException {
		if (obj.getID() != id) {
			ostream.append(context);
			ostream.append("\tFixtures have different IDs\n");
			return false; // NOPMD
		} else if (!(obj instanceof IUnit)) {
			ostream.append(context);
			ostream.append("Different kinds of fixtures for ID #");
			ostream.append(Integer.toString(obj.getID()));
			ostream.append('\n');
			return false;
		} else if (areIntItemsEqual(ostream, owner.getPlayerId(),
				((IUnit) obj).getOwner().getPlayerId(), context, " Unit of ID #",
				Integer.toString(id), ":\tOwners differ.\n") &&
				           areItemsEqual(ostream, name, ((IUnit) obj).getName(), context,
						           " Unit of ID #", Integer.toString(id),
						           ":\tNames differ\n") &&
				           areItemsEqual(ostream, kind, ((IUnit) obj).getKind(), context,
						           " Unit of ID #", Integer.toString(id),
						           ":\tKinds differ\n")) {
			final Iterable<UnitMember> other = (IUnit) obj;
			final Map<Integer, UnitMember> ours = new HashMap<>();
			for (final UnitMember member : this) {
				ours.put(NullCleaner.assertNotNull(Integer.valueOf(member.getID())),
						member);
			}
			final String ctxt =
					NullCleaner.assertNotNull(String.format(
							"%s In unit of kind %s named %s (ID #%d):",
							context, kind, name, Integer.valueOf(id)));
			boolean retval = true;
			for (final UnitMember member : other) {
				if (!ours.containsKey(Integer.valueOf(member.getID()))) {
					ostream.append(ctxt);
					ostream.append(" Extra member:\t");
					ostream.append(member.toString());
					ostream.append(", ID #");
					ostream.append(Integer.toString(member.getID()));
					ostream.append('\n');
					retval = false;
				} else if (!ours.get(Integer.valueOf(member.getID())).isSubset(
						member, ostream, ctxt)) {
					retval = false;
				}
			}
			if (retval) {
				if ("unassigned".equals(name) || "unassigned".equals(kind)) {
					if (!members.isEmpty() && !other.iterator().hasNext()) {
						ostream.append(ctxt);
						ostream.append(
								" Nonempty 'unassigned' when submap has it empty\n");
					}
				}
				return true;
			} else {
				return false;
			}
			//			return retval;
		} else {
			return false; // NOPMD
		}
	}
}
