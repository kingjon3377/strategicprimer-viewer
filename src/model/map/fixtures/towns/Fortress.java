package model.map.fixtures.towns;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import model.map.FixtureIterable;
import model.map.HasMutableImage;
import model.map.IFixture;
import model.map.Player;
import model.map.SubsettableFixture;
import model.map.fixtures.FortressMember;
import model.map.fixtures.mobile.IUnit;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import util.EqualsAny;
import util.LineEnd;

import static util.NullCleaner.assertNotNull;

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
public class Fortress implements HasMutableImage, ITownFixture,
										FixtureIterable<@NonNull FortressMember>,
										SubsettableFixture {
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
	 * The size of the fortress.
	 */
	private final TownSize size;
	/**
	 * The units in the fortress.
	 */
	private final List<FortressMember> units; // Should this be a Set?

	/**
	 * Constructor.
	 *  @param fortOwner the player that owns the fortress
	 * @param fortName  the name of the fortress
	 * @param idNum     the ID number.
	 * @param fortSize the size of the fortress
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
	 * @return the units in the fortress.
	 */
	@Override
	public final Iterator<FortressMember> iterator() {
		return assertNotNull(units.iterator());
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
	 * @return the player that owns the fortress
	 */
	@Override
	public final Player getOwner() {
		return owner;
	}

	/**
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
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return id;
	}

	/**
	 * @return a String representation of the object.
	 */
	@Override
	public String toString() {
		final String ownerStr = owner.toString();
		// Assume each unit is at least half a K.
		final int len = 40 + name.length() + ownerStr.length() + (units.size() * 512);
		final StringBuilder builder = new StringBuilder(len).append("Fortress ");
		builder.append(name);
		builder.append(", owned by player ");
		builder.append(ownerStr);
		builder.append(". Members:");
		int count = 0;
		for (final FortressMember member : units) {
			builder.append(LineEnd.LINE_SEP);
			builder.append("\t\t\t");
			if (member instanceof IUnit) {
				final IUnit unit = (IUnit) member;
				builder.append(unit.getName());
				if (unit.getOwner().equals(owner)) {
					builder.append(" (");
					builder.append(unit.getKind());
					builder.append(')');
				} else if (unit.getOwner().isIndependent()) {
					builder.append(", an independent ");
					builder.append(unit.getKind());
				} else {
					builder.append(" (");
					builder.append(unit.getKind());
					builder.append("), belonging to ");
					builder.append(unit.getOwner());
				}
			} else {
				builder.append(member);
			}
			count++;
			if (count < (units.size() - 1)) {
				builder.append(';');
			}
		}
		return assertNotNull(builder.toString());
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
	 * @param obj     another Fortress
	 * @param ostream a stream to write details to
	 * @param context a string to print before every line of output, describing the
	 *                context
	 * @return whether it's a strict subset of this one
	 * @throws IOException on I/O error writing output to the stream
	 */
	@SuppressWarnings("CastToConcreteClass")
	@Override
	public boolean isSubset(final IFixture obj, final Appendable ostream,
							final String context) throws IOException {
		if (!(obj instanceof Fortress)) {
			ostream.append(context);
			ostream.append("Incompatible type to Fortress");
			ostream.append(LineEnd.LINE_SEP);
			return false;
		}
		final Fortress fort = (Fortress) obj;
		if (EqualsAny.equalsAny(fort.name, name, "unknown")
					&& (fort.owner.getPlayerId() == owner.getPlayerId())) {
			final Map<Integer, FortressMember> ours = new HashMap<>();
			for (final FortressMember member : this) {
				ours.put(assertNotNull(Integer.valueOf(member.getID())),
						member);
			}
			final String localContext =
					context + " In fortress " + name + " (ID #" + id + "):";
			boolean retval = true;
			for (final FortressMember unit : fort) {
				if (!isConditionTrue(ostream,
						ours.containsKey(Integer.valueOf(unit.getID())), localContext,
						"Extra unit:\t", unit.toString(), ", ID #",
						Integer.toString(unit.getID()), LineEnd.LINE_SEP) ||
							!assertNotNull(ours.get(Integer.valueOf(unit.getID())))
									 .isSubset(unit, ostream, localContext)) {
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
	private final int id;

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
	 * @return the fortress's name.
	 */
	@Override
	public String getName() {
		return name;
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
	 * @return the size of the fortress.
	 */
	@Override
	public TownSize size() {
		return size;
	}

	/**
	 * @param player the fort's new owner
	 */
	@Override
	public final void setOwner(final Player player) {
		owner = player;
	}

	/**
	 * @param newName the fort's new name
	 */
	@Override
	public final void setName(final String newName) {
		name = newName;
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
			return "a fortress, " + name + ", owned by you";
		} else {
			return "a fortress, " + name + ", owned by " + owner.getName();
		}
	}
	/**
	 * @return what kind of town this is
	 */
	@Override
	public final String kind() {
		return "fortress";
	}
	/**
	 * The filename of an image to use as a portrait for the fortress.
	 */
	private String portraitName = "";
	/**
	 * @return The filename of an image to use as a portrait for the fortress.
	 */
	@Override
	public String getPortrait() {
		return portraitName;
	}

	/**
	 * @param portrait The filename of an image to use as a portrait for the fortress.
	 */
	@Override
	public void setPortrait(final String portrait) {
		portraitName = portrait;
	}
}
