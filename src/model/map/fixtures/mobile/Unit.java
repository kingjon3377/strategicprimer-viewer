package model.map.fixtures.mobile;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.stream.Collectors;
import model.map.FixtureIterable;
import model.map.HasMutableImage;
import model.map.HasMutableKind;
import model.map.HasMutableName;
import model.map.HasMutableOwner;
import model.map.HasPortrait;
import model.map.IFixture;
import model.map.Player;
import model.map.TileFixture;
import model.map.fixtures.UnitMember;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import util.ArraySet;
import util.LineEnd;
import util.TypesafeLogger;

/**
 * A unit on the map.
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
public class Unit implements IUnit, HasMutableKind, HasMutableName, HasMutableImage,
									 HasMutableOwner, HasPortrait {
	/**
	 * The unit's orders. This is serialized to and from XML, but does not affect
	 * equality or hashing, and is not printed in toString.
	 */
	private final NavigableMap<Integer, String> orders = new TreeMap<>();
	/**
	 * The unit's orders. This is serialized to and from XML, but does not affect
	 * equality or hashing, and is not printed in toString.
	 */
	private final NavigableMap<Integer, String> results = new TreeMap<>();
	/**
	 * The members of the unit.
	 */
	private final Collection<UnitMember> members = new ArraySet<>();
	/**
	 * ID number.
	 */
	private final int id;
	/**
	 * The name of an image to use for this particular fixture.
	 */
	private String image = "";
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
	 * The filename of an image to use as a portrait for the unit.
	 */
	private String portraitName = "";

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
	}

	/**
	 * The unit's orders for all turns.
	 * @return the unit's orders for all turns.
	 */
	@Override
	public NavigableMap<Integer, String> getAllOrders() {
		return Collections.unmodifiableNavigableMap(orders);
	}

	/**
	 * The unit's results for all turns.
	 * @return the unit's results for all turns.
	 */
	@Override
	public NavigableMap<Integer, String> getAllResults() {
		return Collections.unmodifiableNavigableMap(results);
	}

	/**
	 * TODO: There should be some way to convey the unit's *size* without the
	 * *details* of its contents. Or maybe we should give the contents but not *their*
	 * details?
	 *
	 * @param zero whether to omit its contents and orders
	 * @return a copy of this unit
	 */
	@SuppressWarnings("MethodReturnOfConcreteClass")
	@Override
	public Unit copy(final boolean zero) {
		final Unit retval = new Unit(owner, kind, name, id);
		if (!zero) {
			retval.orders.putAll(orders);
			retval.results.putAll(results);
			for (final UnitMember member : this) {
				retval.addMember(member.copy(false));
			}
		}
		retval.image = image;
		return retval;
	}

	/**
	 * The unit's owner.
	 * @return the player that owns the unit
	 */
	@Override
	public final Player getOwner() {
		return owner;
	}

	/**
	 * Change the unit's owner.
	 * @param player the unit's new owner
	 */
	@Override
	public final void setOwner(final Player player) {
		owner = player;
	}

	/**
	 * The unit's "kind". For player-owned units this is usually their "category" (e.g.
	 * "agriculture"); for independent units it's more descriptive.
	 * @return the kind of unit
	 */
	@Override
	public final String getKind() {
		return kind;
	}

	/**
	 * Set the unit's "kind".
	 * @param nKind the new kind
	 */
	@Override
	public final void setKind(final String nKind) {
		kind = nKind;
	}

	/**
	 * The "name" of the unit. For independent units this is often something like "party
	 * from the village".
	 * @return the name of the unit
	 */
	@Override
	public final String getName() {
		return name;
	}

	/**
	 * Set the unit's name.
	 * @param newName the unit's new name
	 */
	@Override
	public final void setName(final String newName) {
		name = newName;
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
	 * An iterator over the unit's members.
	 * @return an iterator over the unit's members
	 */
	@Override
	@NonNull
	public final Iterator<@NonNull UnitMember> iterator() {
		return members.iterator();
	}

	/**
	 * An object is equal iff it is an IUnit owned by the same player, with the same
	 * kind, ID, and name, and with equal members.
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
	 * Test equality of members only.
	 * @param obj another unit
	 * @return whether its "members" are the same as ours
	 */
	private boolean areMembersEqual(final FixtureIterable<? extends UnitMember> obj) {
		final Collection<UnitMember> theirs =
				obj.stream().collect(Collectors.toSet());
		return members.containsAll(theirs) && theirs.containsAll(members);
	}

	/**
	 * Use the ID number for hashing.
	 * @return a hash-code for the object
	 */
	@Override
	public int hashCode() {
		return id;
	}

	/**
	 * A String representation of the unit.
	 * @return a String representation of the Unit.
	 */
	@Override
	public String toString() {
		if (owner.isIndependent()) {
			return String.format("Independent unit of type %s, named %s", kind, name);
		} else {
			return String.format("Unit of type %s, belonging to %s, named %s", kind,
					owner.toString(), name);
		}
	}

	/**
	 * A more verbose String representation of the unit, including its members.
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
			builder.append(LineEnd.LINE_SEP);
			builder.append(member);
		}
		return builder.toString();
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
	 * The ID number.
	 * @return a UID for the fixture.
	 */
	@Override
	public final int getID() {
		return id;
	}

	/**
	 * If we ignore ID (and members, which we probably shouldn't), a fixture is equal
	 * iff it is a IUnit owned by the same player with the same kind and name.
	 * FIXME: Should this look at unit members?
	 *
	 * @param fix a fixture
	 * @return whether it's an identical-except-ID unit.
	 */
	@SuppressWarnings("ObjectEquality")
	@Override
	public boolean equalsIgnoringID(final IFixture fix) {
		return (this == fix) || ((fix instanceof IUnit)
										 && (((IUnit) fix).getOwner().getPlayerId() ==
													 owner.getPlayerId())
										 && ((IUnit) fix).getKind().equals(kind)
										 && ((IUnit) fix).getName().equals(name));
	}

	/**
	 * Set orders for a turn.
	 * @param turn the turn to set orders for
	 * @param newOrders the unit's new orders for that turn
	 */
	@Override
	public final void setOrders(final int turn, final String newOrders) {
		orders.put(Integer.valueOf(turn), newOrders);
	}

	/**
	 * Get orders for a turn.
	 * @param turn the turn to get orders for
	 * @return the unit's orders for that turn
	 */
	@Override
	public String getOrders(final int turn) {
		if (orders.containsKey(Integer.valueOf(turn))) {
			return orders.get(Integer.valueOf(turn));
		} else if (turn < 0 && orders.containsKey(Integer.valueOf(-1))) {
			return orders.get(Integer.valueOf(-1));
		} else {
			return "";
		}
	}

	/**
	 * Set results for a turn.
	 * @param turn       a turn
	 * @param newResults the unit's new results for that turn
	 */
	@Override
	public final void setResults(final int turn, final String newResults) {
		results.put(Integer.valueOf(turn), newResults);
	}

	/**
	 * Get results for a turn.
	 * @param turn a turn
	 * @return the unit's results for that turn
	 */
	@Override
	public String getResults(final int turn) {
		if (results.containsKey(Integer.valueOf(turn))) {
			return results.get(Integer.valueOf(turn));
		} else if (turn < 0 && results.containsKey(Integer.valueOf(-1))) {
			return results.get(Integer.valueOf(-1));
		} else {
			return "";
		}
	}

	/**
	 * Get the per-instance icon filename.
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
	public final void setImage(final String img) {
		image = img;
	}

	/**
	 * A short description of the fixture, giving its kind and owner, but not name.
	 * @return a short description of the fixture
	 */
	@Override
	public String shortDesc() {
		if (owner.isCurrent()) {
			return "a(n) " + kind + " unit belonging to you";
		} else {
			return "a(n) " + kind + " unit belonging to " + owner.getName();
		}
	}

	/**
	 * The portrait image filename.
	 * @return The filename of an image to use as a portrait for the unit.
	 */
	@Override
	public String getPortrait() {
		return portraitName;
	}

	/**
	 * Set the portrait image filename.
	 * @param portrait The filename of an image to use as a portrait for the unit.
	 */
	@Override
	public void setPortrait(final String portrait) {
		portraitName = portrait;
	}
	/**
	 * The required Perception check for an explorer to find the fixture.
	 *
	 * @return the DC to discover the fixture.
	 */
	@Override
	public int getDC() {
		final int natural = 25 - members.size();
		final int memberDC = stream().filter(TileFixture.class::isInstance)
									 .map(TileFixture.class::cast)
									 .mapToInt(TileFixture::getDC).min().orElse(100);
		return Integer.min(natural, memberDC);
	}
}
