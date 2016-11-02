package model.map.fixtures.mobile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import model.map.FixtureIterable;
import model.map.HasImage;
import model.map.HasKind;
import model.map.HasName;
import model.map.HasOwner;
import model.map.IFixture;
import model.map.fixtures.FortressMember;
import model.map.fixtures.UnitMember;
import org.eclipse.jdt.annotation.NonNull;
import util.LineEnd;
import util.NullCleaner;

/**
 * An interface for units.
 *
 * TODO: Should mutability of name and owner be pushed down to implementations?
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
	 * @param turn the current turn
	 * @return that unit's latest orders as of that turn
	 */
	default String getLatestOrders(final int turn) {
		final NavigableMap<Integer, String> orders = getAllOrders();
		for (int i = turn; i >= -1; i--) {
			final String turnOrders = orders.get(Integer.valueOf(i)).trim();
			if (!turnOrders.isEmpty()) {
				return turnOrders;
			}
		}
		return "";
	}
	/**
	 * @return the unit's orders
	 * @param turn which turn these are orders for
	 */
	String getOrders(final int turn);

	/**
	 * @return the unit's orders for all turns
	 */
	NavigableMap<Integer, String> getAllOrders();

	/**
	 * @param turn which turn these are orders for
	 * @param newOrders the unit's new orders
	 */
	void setOrders(final int turn, String newOrders);

	/**
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
	 * @param obj     another unit
	 * @param ostream the stream to report results on
	 * @param context a string to print before every line of output, describing the
	 *                context
	 * @return whether the unit is a strict subset of this one.
	 * @throws IOException on I/O error writing output to the stream
	 */
	@Override
	default boolean isSubset(final IFixture obj, final Appendable ostream,
							final String context) throws IOException {
		if (obj.getID() != getID()) {
			ostream.append(context);
			ostream.append("\tFixtures have different IDs");
			ostream.append(LineEnd.LINE_SEP);
			return false;
		} else if (!(obj instanceof IUnit)) {
			ostream.append(context);
			ostream.append("Different kinds of fixtures for ID #");
			ostream.append(Integer.toString(obj.getID()));
			ostream.append(LineEnd.LINE_SEP);
			return false;
		} else if (areIntItemsEqual(ostream, getOwner().getPlayerId(),
				((IUnit) obj).getOwner().getPlayerId(), context, " Unit of ID #",
				Integer.toString(getID()), ":\tOwners differ.", LineEnd.LINE_SEP) &&
						   areObjectsEqual(ostream, getName(), ((IUnit) obj).getName(), context,
								   " Unit of ID #", Integer.toString(getID()),
								   ":\tNames differ", LineEnd.LINE_SEP) &&
						   areObjectsEqual(ostream, getKind(), ((IUnit) obj).getKind(), context,
								   " Unit of ID #", Integer.toString(getID()),
								   ":\tKinds differ", LineEnd.LINE_SEP)) {
			final Iterable<UnitMember> other = (IUnit) obj;
			final Map<Integer, UnitMember> ours = new HashMap<>();
			for (final UnitMember member : this) {
				ours.put(NullCleaner.assertNotNull(Integer.valueOf(member.getID())),
						member);
			}
			final String localContext =
					NullCleaner.assertNotNull(String.format(
							"%s In unit of kind %s named %s (ID #%d):",
							context, getKind(), getName(), Integer.valueOf(getID())));
			boolean retval = true;
			for (final UnitMember member : other) {
				if (!ours.containsKey(Integer.valueOf(member.getID()))) {
					ostream.append(localContext);
					ostream.append(" Extra member:\t");
					ostream.append(member.toString());
					ostream.append(", ID #");
					ostream.append(Integer.toString(member.getID()));
					ostream.append(LineEnd.LINE_SEP);
					retval = false;
				} else if (!NullCleaner.assertNotNull(
						ours.get(Integer.valueOf(member.getID())))
									.isSubset(member, ostream, localContext)) {
					retval = false;
				}
			}
			if (retval) {
				if (("unassigned".equals(getName()) || "unassigned".equals(getKind())) &&
							iterator().hasNext() && !other.iterator().hasNext()) {
					ostream.append(localContext);
					ostream.append(" Nonempty 'unassigned' when submap has it empty");
					ostream.append(LineEnd.LINE_SEP);
				}
				return true;
			} else {
				return false;
			}
			//			return retval;
		} else {
			return false;
		}
	}
}
