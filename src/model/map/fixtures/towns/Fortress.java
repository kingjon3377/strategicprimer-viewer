package model.map.fixtures.towns;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import model.map.FixtureIterable;
import model.map.HasMutableImage;
import model.map.HasMutableName;
import model.map.IFixture;
import model.map.Player;
import model.map.SubsettableFixture;
import model.map.TileFixture;
import model.map.fixtures.FortressMember;
import model.map.fixtures.mobile.IUnit;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import util.EqualsAny;

/**
 * A fortress on the map. A player can only have one fortress per tile, but multiple
 * players may have fortresses on the same tile.
 *
 * FIXME: We need something about buildings yet
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2012-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public class Fortress implements HasMutableImage, ITownFixture, HasMutableName,
										 FixtureIterable<@NonNull FortressMember>,
										 SubsettableFixture {
	/**
	 * The size of the fortress.
	 */
	private final TownSize size;
	/**
	 * The units in the fortress.
	 */
	private final List<FortressMember> units; // Should this be a Set?
	/**
	 * ID number.
	 */
	private final int id;
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
	 * The filename of an image to use as a portrait for the fortress.
	 */
	private String portraitName = "";

	/**
	 * Constructor.
	 *
	 * @param fortOwner the player that owns the fortress
	 * @param fortName  the name of the fortress
	 * @param idNum     the ID number.
	 * @param fortSize  the size of the fortress
	 */
	public Fortress(final Player fortOwner, final String fortName,
					final int idNum, final TownSize fortSize) {
		owner = fortOwner;
		name = fortName;
		units = new ArrayList<>();
		id = idNum;
		size = fortSize;
	}

	/**
	 * Clone the fortress.
	 * @param zero whether to omit the fortress's contents
	 * @return a copy of this fortress
	 */
	@SuppressWarnings("MethodReturnOfConcreteClass")
	@Override
	public Fortress copy(final boolean zero) {
		final Fortress retval;
		if (zero) {
			retval = new Fortress(owner, "unknown", id, size);
		} else {
			retval = new Fortress(owner, name, id, size);
			for (final FortressMember unit : this) {
				retval.addMember(unit.copy(false));
			}
		}
		retval.image = image;
		return retval;
	}

	/**
	 * An iterator over the members of the fortress.
	 * @return the units in the fortress.
	 */
	@Override
	public final Iterator<FortressMember> iterator() {
		return units.iterator();
	}

	/**
	 * Add a unit to the fortress.
	 *
	 * @param unit the unit to add
	 */
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	public final void addMember(final FortressMember unit) {
		units.add(unit);
	}

	/**
	 * Remove a unit from the fortress.
	 *
	 * @param unit the unit to remove
	 */
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	public final void removeMember(final FortressMember unit) {
		units.remove(unit);
	}

	/**
	 * The owner of the fortress.
	 * @return the player that owns the fortress
	 */
	@Override
	public final Player getOwner() {
		return owner;
	}

	/**
	 * Set the owner of the fortress.
	 * @param player the fort's new owner
	 */
	@Override
	public final void setOwner(final Player player) {
		owner = player;
	}

	/**
	 * An object is equal iff it is a Fortress with the same name, owner, ID, and
	 * members.
	 * @param obj an object
	 * @return whether it is an identical fortress
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return (this == obj) || ((obj instanceof Fortress)
										 && name.equals(((Fortress) obj).name)
										 && (((Fortress) obj).owner.getPlayerId() ==
													 owner.getPlayerId())
										 && ((Fortress) obj).units.containsAll(units)
										 && units.containsAll(((Fortress) obj).units)
										 && (((Fortress) obj).id == id));
	}

	/**
	 * Use our ID for hashing.
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return id;
	}

	/**
	 * A fairly complex String representation of the Fortress.
	 * @return a String representation of the object.
	 */
	@Override
	public String toString() {
		final String ownerStr = owner.toString();
		// Assume each unit is at least half a K.
		final int len = 40 + name.length() + ownerStr.length() + (units.size() * 512);
		final StringBuilder builder = new StringBuilder(len);
		try (final Formatter formatter = new Formatter(builder)) {
			formatter.format("Fortress %s, owned by player %s. Members:", name, ownerStr);
			int count = 0;
			for (final FortressMember member : units) {
				formatter.format("%n\t\t\t");
				if (member instanceof IUnit) {
					final IUnit unit = (IUnit) member;
					formatter.format("%s", unit.getName());
					builder.append(unit.getName());
					if (unit.getOwner().equals(owner)) {
						formatter.format(" (%s)", unit.getKind());
					} else if (unit.getOwner().isIndependent()) {
						formatter.format(", an independent %s", unit.getKind());
					} else {
						formatter.format(" (%s), belonging to %s", unit.getKind(),
								unit.getOwner().toString());
					}
				} else {
					formatter.format("%s", member.toString());
				}
				count++;
				if (count < (units.size() - 1)) {
					formatter.format(";");
				}
			}
		}
		return builder.toString();
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
	 * A fixture is a subset if it is a Fortress with the same ID, owner, and name (or
	 * it has the name "unknown") and every member it has is equal to, or a subset of,
	 * one of our members.
	 * @param obj     another Fortress
	 * @param ostream a stream to write details to
	 * @param context a string to print before every line of output, describing the
	 *                context
	 * @return whether it's a strict subset of this one
	 */
	@SuppressWarnings("CastToConcreteClass")
	@Override
	public boolean isSubset(final IFixture obj, final Formatter ostream,
							final String context) {
		if (!(obj instanceof Fortress)) {
			ostream.format("%sIncompatible type to Fortress%n", context);
			return false;
		}
		final Fortress fort = (Fortress) obj;
		// TODO: Check ID first
		if (EqualsAny.equalsAny(fort.name, name, "unknown")
					&& (fort.owner.getPlayerId() == owner.getPlayerId())) {
			final Map<Integer, FortressMember> ours = stream().collect(
					Collectors.toMap(FortressMember::getID, x -> x, (one, two) -> one));
			boolean retval = true;
			for (final FortressMember unit : fort) {
				final Integer memberId = Integer.valueOf(unit.getID());
				if (!isConditionTrue(ostream, ours.containsKey(memberId),
						"%s In fortress %s (ID #%d): Extra member:\t%s, ID #%d%n",
						context, name, Integer.valueOf(id), unit.toString(), memberId) ||
							!ours.get(memberId).isSubset(unit, ostream,
									String.format("%s In fortress %s (ID #%d):", context,
											name, Integer.valueOf(id)))) {
					retval = false;
				}
			}
			return retval;
		} else {
			return false;
		}
	}

	/**
	 * The ID of the fortress.
	 * @return a UID for the fixture.
	 */
	@Override
	public int getID() {
		return id;
	}

	/**
	 * A fixture is equal, if we ignore ID, to this one if it is a Fortress with the
	 * same name, owner, and members.
	 * @param fix a fixture
	 * @return whether it's identical to this except ID and DC.
	 */
	@SuppressWarnings({"ObjectEquality", "CastToConcreteClass"})
	@Override
	public boolean equalsIgnoringID(final IFixture fix) {
		return (this == fix) || ((fix instanceof Fortress)
										 && name.equals(((Fortress) fix).name)
										 && (((Fortress) fix).owner.getPlayerId() ==
													 owner.getPlayerId())
										 && ((Fortress) fix).units.containsAll(units)
										 && units.containsAll(((Fortress) fix).units));
	}

	/**
	 * The name of the fortress.
	 * @return the fortress's name.
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * Set the name of the fortress.
	 * @param newName the fort's new name
	 */
	@Override
	public final void setName(final String newName) {
		name = newName;
	}

	/**
	 * TODO: Add support for having a different status? (but leave 'active' the default).
	 *
	 * Or maybe a non-'active' fortress is a Fortification, and an active fortification
	 * is a Fortress.
	 *
	 * @return the status of the fortress
	 */
	@Override
	public TownStatus status() {
		return TownStatus.Active;
	}

	/**
	 * The size of the fortress.
	 * @return the size of the fortress.
	 */
	@Override
	public TownSize size() {
		return size;
	}

	/**
	 * The per-instance icon filename.
	 * @return the name of an image to use for this particular fixture.
	 */
	@Override
	public String getImage() {
		return image;
	}

	/**
	 * Set the per-instance icon filename.
	 * @param img the name of an image to use for this particular fixture
	 */
	@Override
	public void setImage(final String img) {
		image = img;
	}

	/**
	 * The plural of Fortress is Fortresses.
	 * @return a string describing all fortresses as a class
	 */
	@Override
	public String plural() {
		return "Fortresses";
	}

	/**
	 * Either "a fortress, [name], owned by you" or "a fortress, [name], owned by
	 * [owner]". TODO: Handle independent fortresses specially?
	 * @return a short description of the fixture
	 */
	@Override
	public String shortDesc() {
		if (owner.isCurrent()) {
			return "a fortress, " + name + ", owned by you";
		} else {
			return "a fortress, " + name + ", owned by " + owner.getName();
		}
	}

	/**
	 * The kind of town this is is a fortress.
	 * @return what kind of town this is
	 */
	@Override
	public final String kind() {
		return "fortress";
	}

	/**
	 * The portrait image filename, if any.
	 * @return The filename of an image to use as a portrait for the fortress.
	 */
	@Override
	public String getPortrait() {
		return portraitName;
	}

	/**
	 * Set the portrait image.
	 * @param portrait The filename of an image to use as a portrait for the fortress.
	 */
	@Override
	public void setPortrait(final String portrait) {
		portraitName = portrait;
	}
	/**
	 * The required Perception check for an explorer to find the fixture.
	 *
	 * TODO: should depend on size
	 *
	 * @return the DC to discover the fixture.
	 */
	@Override
	public int getDC() {
		final int natural = 20 - units.size();
		final int memberDC = stream().filter(TileFixture.class::isInstance)
									 .map(TileFixture.class::cast)
									 .mapToInt(TileFixture::getDC).min().orElse(100);
		return Integer.min(natural, memberDC);
	}
}
