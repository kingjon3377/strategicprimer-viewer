package controller.map.report.tabular;

import java.io.IOException;
import java.util.Comparator;
import model.map.DistanceComparator;
import model.map.IFixture;
import model.map.Player;
import model.map.Point;
import model.map.fixtures.FortressMember;
import model.map.fixtures.towns.Fortress;
import util.Pair;
import util.PatientMap;

/**
 * A tabular report generator for fortresses.
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
public class FortressTabularReportGenerator implements ITableGenerator<Fortress> {
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
	public FortressTabularReportGenerator(final Player currentPlayer, final Point hq) {
		player = currentPlayer;
		base = hq;
	}

	/**
	 * @param ostream  the stream to write the row to
	 * @param fixtures the set of fixtures
	 * @param item     the fortress to base the line on
	 * @param loc      its location
	 * @throws IOException on I/O error writing to the stream
	 */
	@Override
	public boolean produce(final Appendable ostream,
						   final PatientMap<Integer, Pair<Point, IFixture>> fixtures,
						   final Fortress item, final Point loc) throws IOException {
		writeField(ostream, distanceString(loc, base));
		writeFieldDelimiter(ostream);
		writeField(ostream, loc.toString());
		writeFieldDelimiter(ostream);
		writeField(ostream, getOwnerString(player, item.getOwner()));
		writeFieldDelimiter(ostream);
		writeField(ostream, item.getName());
		ostream.append(getRowDelimiter());
		// Players shouldn't be able to see the contents of others' fortresses.
		if (!player.equals(item.getOwner())) {
			for (final FortressMember member : item) {
				fixtures.remove(Integer.valueOf(member.getID()));
			}
		}
		return true;
	}

	/**
	 * @return the header row for the tabular report
	 */
	@Override
	public String headerRow() {
		return "Distance,Location,Owner,Name";
	}

	/**
	 * @param one a Pair of one fortress and its location (in the other order)
	 * @param two a Pair of another fortress and its location (in the other order)
	 * @return the result of a comparison between the pairs.
	 */
	@SuppressWarnings("QuestionableName")
	@Override
	public int comparePairs(final Pair<Point, Fortress> one,
							final Pair<Point, Fortress> two) {
		final Comparator<Point> comparator = new DistanceComparator(base);
		final Fortress first = one.second();
		final Fortress second = two.second();
		final int cmp = comparator.compare(one.first(), two.first());
		if (player.equals(first.getOwner()) && !player.equals(second.getOwner())) {
			return -1;
		} else if (!player.equals(first.getOwner()) && player.equals(second.getOwner()
		)) {
			return 1;
		} else if (cmp == 0) {
			final int nameCmp = first.getName().compareTo(second.getName());
			if ("HQ".equals(first.getName()) && !"HQ".equals(second.getName())) {
				return -1;
			} else if (!"HQ".equals(first.getName()) && "HQ".equals(second.getName())) {
				return 1;
			} else if (nameCmp == 0) {
				return first.getOwner().compareTo(second.getOwner());
			} else {
				return nameCmp;
			}
		} else {
			return cmp;
		}
	}

	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "FortressTabularReportGenerator";
	}
}
