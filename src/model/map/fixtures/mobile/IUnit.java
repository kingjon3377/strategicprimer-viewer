package model.map.fixtures.mobile;

import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import model.map.FixtureIterable;
import model.map.HasImage;
import model.map.HasKind;
import model.map.HasName;
import model.map.HasOwner;
import model.map.IFixture;
import model.map.fixtures.FortressMember;
import model.map.fixtures.UnitMember;
import util.EqualsAny;

/**
 * An interface for units.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2014-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public interface IUnit extends MobileFixture, HasImage, HasKind, HasName,
									   FixtureIterable<@NonNull UnitMember>, HasOwner,
									   FortressMember {
	/**
	 * The unit's latest orders as of the given turn.
	 * @param turn the current turn
	 * @return that unit's latest orders as of that turn
	 */
	default String getLatestOrders(final int turn) {
		final NavigableMap<Integer, String> orders = getAllOrders();
		for (int i = turn; i >= -1; i--) {
			if (orders.containsKey(Integer.valueOf(i))) {
				@Nullable String temp = orders.get(Integer.valueOf(i));
				assert (temp != null);
				final String turnOrders = temp.trim();
				if (!turnOrders.isEmpty()) {
					return turnOrders;
				}
			}
		}
		return "";
	}

	/**
	 * The latest turn the given orders were the current orders.
	 * @param orders an orders string
	 * @return the latest turn those orders are our orders, or -1 if they're not
	 */
	default int getOrdersTurn(final String orders) {
		int retval = -1;
		for (final Map.Entry<Integer, String> entry : getAllOrders().entrySet()) {
			if (orders.equals(entry.getValue()) && entry.getKey().intValue() > retval) {
				retval = entry.getKey().intValue();
			}
		}
		return retval;
	}

	/**
	 * The unit's orders for the given turn.
	 * @param turn which turn these are orders for
	 * @return the unit's orders
	 */
	String getOrders(final int turn);

	/**
	 * The unit's orders history.
	 * @return the unit's orders for all turns
	 */
	NavigableMap<Integer, String> getAllOrders();

	/**
	 * Set the unit's orders for a turn.
	 * @param turn      which turn these are orders for
	 * @param newOrders the unit's new orders
	 */
	void setOrders(final int turn, String newOrders);

	/**
	 * The unit's results for the given turn.
	 * @param turn which turn these are results for
	 * @return the unit's results
	 */
	String getResults(final int turn);

	/**
	 * The unit's results history.
	 * @return the unit's results for all turns
	 */
	NavigableMap<Integer, String> getAllResults();

	/**
	 * Set the unit's results for the given turn.
	 * @param turn       which turn these are results for
	 * @param newResults the unit's new results
	 */
	void setResults(final int turn, String newResults);

	/**
	 * Get the unit's latest results as of the given turn.
	 * @param turn the current turn
	 * @return that unit's latest results as of that turn
	 */
	default String getLatestResults(final int turn) {
		final NavigableMap<Integer, String> results = getAllResults();
		for (int i = turn; i >= -1; i--) {
			@Nullable String temp = results.get(Integer.valueOf(i));
			assert (temp != null);
			final String turnResults = temp.trim();
			if (!turnResults.isEmpty()) {
				return turnResults;
			}
		}
		return "";
	}

	/**
	 * A verbose description of the unit.
	 * @return a verbose description of the Unit.
	 */
	String verbose();

	/**
	 * Add a member.
	 *
	 * @param member the member to add
	 */
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	void addMember(UnitMember member);

	/**
	 * Remove a member.
	 *
	 * @param member the member to remove
	 */
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	void removeMember(UnitMember member);

	/**
	 * A specialization of the method from IFixture.
	 *
	 * @param zero whether to "zero out" or omit sensitive information
	 * @return a copy of the member
	 */
	@Override
	IUnit copy(boolean zero);

	/**
	 * The plural of Unit is Units.
	 * @return "Units"
	 */
	@Override
	default String plural() {
		return "Units";
	}

	/**
	 * A fixture is a subset if it is a unit with the same ID and no extra members, and
	 * all corresponding (by ID, presumably) members are either equal or themselves
	 * subsets.
	 * @param obj     another unit
	 * @param ostream the stream to report results on
	 * @param context a string to print before every line of output, describing the
	 *                context
	 * @return whether the unit is a strict subset of this one.
	 */
	@Override
	default boolean isSubset(final IFixture obj, final Formatter ostream,
							 final String context) {
		if (obj.getID() == getID()) {
			if (obj instanceof IUnit) {
				if (areIntItemsEqual(ostream, getOwner().getPlayerId(),
						((IUnit) obj).getOwner().getPlayerId(),
						"%s Unit of ID #%d:\tOwners differ%n", context,
						Integer.valueOf(getID())) &&
								   areObjectsEqual(ostream, getName(), ((IUnit) obj).getName(),
										   "%s Unit of ID #%d\tNames differ%n", context,
										   Integer.valueOf(getID())) &&
								   areObjectsEqual(ostream, getKind(), ((IUnit) obj).getKind(),
										   "%s Unit of ID #%d\tKinds differ%n", context,
										   Integer.valueOf(getID()))) {
					final Iterable<UnitMember> other = (IUnit) obj;
					final Map<Integer, UnitMember> ours = new HashMap<>();
					for (final UnitMember member : this) {
						ours.put(Integer.valueOf(member.getID()), member);
					}
					boolean retval = true;
					for (final UnitMember member : other) {
						if (!ours.containsKey(Integer.valueOf(member.getID()))) {
							ostream.format(
									"%s In unit of kind %s named %s (ID #%d): " +
											"Extra member:\t%s, ID #%d%n",
									context, getKind(), getName(), Integer.valueOf(getID()),
									member.toString(), Integer.valueOf(member.getID()));
							retval = false;
						} else {
							@Nullable UnitMember ourMember = ours.get(Integer.valueOf(member.getID()));
							assert (ourMember != null);
							if (!ourMember
											.isSubset(member, ostream, String.format(
													"%s In unit of kind %s named %s (ID #%d):",
													context, getKind(), getName(),
													Integer.valueOf(getID())))) {
								retval = false;
							}
						}
					}
					if (retval) {
						if (EqualsAny.equalsAny("unassigned", getName(), getKind()) &&
									iterator().hasNext() && !other.iterator().hasNext()) {
							ostream.format(
									"%s In unit of kind %s named %s (ID #%d): Nonempty " +
											"'unassigned' when submap has it empty%n",
									context, getKind(), getName(), Integer.valueOf(getID()));
						}
						return true;
					} else {
						return false;
					}
					//			return retval;
				} else {
					return false;
				}
			} else {
				ostream.format("%s\tDifferent kinds of fixtures for ID #%d%n", context,
						Integer.valueOf(getID()));
				return false;
			}
		} else {
			ostream.format("%s\tFixtures have different IDs%n", context);
			return false;
		}
	}
}
