package controller.map.report.tabular;

import java.io.IOException;
import java.util.Comparator;
import model.map.DistanceComparator;
import model.map.IFixture;
import model.map.Player;
import model.map.Point;
import model.map.fixtures.towns.Village;
import util.Pair;
import util.PatientMap;

/**
 * A tabular report generator for villages.
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
public final class VillageTabularReportGenerator implements ITableGenerator<Village> {
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
	public VillageTabularReportGenerator(final Player currentPlayer, final Point hq) {
		player = currentPlayer;
		base = hq;
	}

	/**
	 * Write a row of the table based on the given village.
	 * @param ostream  the stream to write the row to
	 * @param fixtures the set of fixtures
	 * @param item     the village to base the line on
	 * @param loc      its location
	 * @throws IOException on I/O error writing to the stream
	 * @return true: remove this item from the Map
	 */
	@Override
	public boolean produce(final Appendable ostream,
						   final PatientMap<Integer, Pair<Point, IFixture>> fixtures,
						   final Village item, final Point loc) throws IOException {
		writeDelimitedField(ostream, distanceString(loc, base));
		writeDelimitedField(ostream, loc.toString());
		writeDelimitedField(ostream, getOwnerString(player, item.getOwner()));
		writeField(ostream, item.getName());
		ostream.append(getRowDelimiter());
		return true;
	}

	/**
	 * The header of this table.
	 * @return the header row for the tabular report
	 */
	@Override
	public String headerRow() {
		return "Distance,Location,Owner,Name";
	}

	/**
	 * Compare two location-and-village pairs.
	 * @param one a Pair of one village and its location (in the other order)
	 * @param two a Pair of another village and its location (in the other order)
	 * @return the result of a comparison between the pairs.
	 */
	@SuppressWarnings("QuestionableName")
	@Override
	public int comparePairs(final Pair<Point, Village> one,
							final Pair<Point, Village> two) {
		final Comparator<Point> comparator = new DistanceComparator(base);
		final Village first = one.second();
		final Village second = two.second();
		final int cmp = comparator.compare(one.first(), two.first());
		final int playerCmp = first.getOwner().compareTo(second.getOwner());
		if (playerCmp == 0) {
			if (cmp == 0) {
				return first.getName().compareTo(second.getName());
			} else {
				return cmp;
			}
		} else if (player.equals(first.getOwner())) {
			return -1;
		} else {
			return playerCmp;
		}
	}

	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "VillageTabularReportGenerator";
	}
}
