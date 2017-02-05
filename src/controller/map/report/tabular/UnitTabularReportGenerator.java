package controller.map.report.tabular;

import java.io.IOException;
import java.util.Comparator;
import model.map.DistanceComparator;
import model.map.IFixture;
import model.map.Player;
import model.map.Point;
import model.map.fixtures.UnitMember;
import model.map.fixtures.mobile.Animal;
import model.map.fixtures.mobile.IUnit;
import util.Pair;
import util.PatientMap;

/**
 * A tabular report generator for units.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class UnitTabularReportGenerator implements ITableGenerator<IUnit> {
	/**
	 * The player for whom this report is being produced.
	 */
	private final Player player;
	/**
	 * His or her HQ location.
	 */
	private final Point base;

	/**
	 * Constructor.
	 *
	 * @param currentPlayer the player for whom this report is being produced
	 * @param hq            his or her HQ location
	 */
	public UnitTabularReportGenerator(final Player currentPlayer, final Point hq) {
		player = currentPlayer;
		base = hq;
	}

	/**
	 * Write a table row representing a unit.
	 * @param ostream  the stream to write the row to
	 * @param fixtures the set of fixtures
	 * @param item     the fortress to base the line on
	 * @param loc      its location
	 * @throws IOException on I/O error writing to the stream
	 * @return true: remove this item from the Map
	 */
	@SuppressWarnings("IfStatementWithIdenticalBranches")
	@Override
	public boolean produce(final Appendable ostream,
						   final PatientMap<Integer, Pair<Point, IFixture>> fixtures,
						   final IUnit item,
						   final Point loc) throws IOException {
		writeDelimitedField(ostream, distanceString(loc, base));
		writeDelimitedField(ostream, loc.toString());
		writeDelimitedField(ostream, getOwnerString(player, item.getOwner()));
		writeDelimitedField(ostream, item.getKind());
		writeDelimitedField(ostream, item.getName());
		writeField(ostream, item.getAllOrders().lastEntry().getValue());
		ostream.append(getRowDelimiter());
		for (final UnitMember member : item) {
			if (member instanceof Animal) {
				// We don't want animals inside a unit showing up in the wild-animal
				// report
				fixtures.remove(Integer.valueOf(item.getID()));
			} else if (!player.equals(item.getOwner())) {
				// A player shouldn't be able to see the details of another player's
				// units
				fixtures.remove(Integer.valueOf(item.getID()));
			}
		}
		return true;
	}

	/**
	 * The header row for this table.
	 * @return the header row for the report
	 */
	@Override
	public String headerRow() {
		//noinspection HardcodedFileSeparator
		return "Distance,Location,Owner,Kind/Category,Name,Orders";
	}

	@SuppressWarnings("QuestionableName")
	@Override
	public int comparePairs(final Pair<Point, IUnit> one, final Pair<Point, IUnit> two) {
		final Comparator<Point> comparator = new DistanceComparator(base);
		final IUnit first = one.second();
		final IUnit second = two.second();
		final int cmp = comparator.compare(one.first(), two.first());
		if (cmp == 0) {
			final int playerCmp = first.getOwner().compareTo(second.getOwner());
			if (playerCmp == 0) {
				final int kindCmp = first.getKind().compareTo(second.getKind());
				if (kindCmp == 0) {
					return first.getName().compareTo(second.getName());
				} else {
					return kindCmp;
				}
			} else {
				return playerCmp;
			}
		} else {
			return cmp;
		}
	}

	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "UnitTabularReportGenerator";
	}
	/**
	 * The type of objects we accept. Needed so the default
	 * {@link ITableGenerator#produce(Appendable, PatientMap)} can call the typesafe single-row
	 * produce() without causing class-cast exceptions or taking this Class object as a
	 * parameter.
	 * @return the type of the objects we accept
	 */
	@Override
	public Class<IUnit> type() {
		return IUnit.class;
	}
}
