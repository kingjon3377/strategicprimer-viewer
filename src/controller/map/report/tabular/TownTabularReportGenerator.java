package controller.map.report.tabular;

import controller.map.misc.TownComparator;
import java.io.IOException;
import model.map.DistanceComparator;
import model.map.IFixture;
import model.map.Player;
import model.map.Point;
import model.map.fixtures.towns.AbstractTown;
import util.Pair;
import util.PatientMap;

/**
 * A tabular report generator for towns.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2016 Jonathan Lovelace
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
public class TownTabularReportGenerator implements ITableGenerator<AbstractTown> {
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
	 * @param currentPlayer the player for whom this report is being produced
	 * @param hq his or her HQ location
	 */
	public TownTabularReportGenerator(final Player currentPlayer, final Point hq) {
		player = currentPlayer;
		base = hq;
	}

	/**
	 * @param ostream the stream to write the row to
	 * @param fixtures the set of fixtures
	 * @param item the village to base the line on
	 * @param loc its location
	 * @throws IOException on I/O error writing to the stream
	 */
	@Override
	public boolean produce(final Appendable ostream,
						   final PatientMap<Integer, Pair<Point, IFixture>> fixtures,
						   final AbstractTown item, final Point loc) throws IOException {
		writeField(ostream, distanceString(loc, base));
		writeFieldDelimiter(ostream);
		writeField(ostream, loc.toString());
		writeFieldDelimiter(ostream);
		writeField(ostream, getOwnerString(player, item.getOwner()));
		writeFieldDelimiter(ostream);
		writeField(ostream, item.kind());
		writeFieldDelimiter(ostream);
		writeField(ostream, item.size().toString());
		writeFieldDelimiter(ostream);
		writeField(ostream, item.status().toString());
		writeFieldDelimiter(ostream);
		writeField(ostream, item.getName());
		ostream.append(getRowDelimiter());
		return true;
	}
	/**
	 * @return the header row for the tabular report
	 */
	@Override
	public String headerRow() {
		return "Distance,Location,Owner,Kind,Size,Status,Name";
	}
	/**
	 * @param one a Pair of one town and its location (in the other order)
	 * @param two a Pair of another town and its location (in the other order)
	 * @return the result of a comparison between the pairs.
	 */
	@SuppressWarnings("QuestionableName")
	@Override
	public int comparePairs(final Pair<Point, AbstractTown> one,
							final Pair<Point, AbstractTown> two) {
		final DistanceComparator comparator = new DistanceComparator(base);
		final AbstractTown first = one.second();
		final AbstractTown second = two.second();
		final int kindCmp = TownComparator.compareTownKind(first, second);
		if (kindCmp == 0) {
			final int ownerCmp = first.getOwner().compareTo(second.getOwner());
			if (ownerCmp == 0) {
				final int cmp = comparator.compare(one.first(), two.first());
				if (cmp == 0) {
					final int sizeCmp = TownComparator.compareTownSize(first.size(), second.size());
					if (sizeCmp == 0) {
						final int statusCmp = TownComparator.compareTownStatus(first.status(), second.status());
						if (statusCmp == 0) {
							return first.getName().compareTo(second.getName());
						} else {
							return statusCmp;
						}
					} else {
						return sizeCmp;
					}
				} else {
					return cmp;
				}
			} else {
				return ownerCmp;
			}
		} else {
			return kindCmp;
		}
	}

	@Override
	public String toString() {
		return "TownTabularReportGenerator";
	}
}
