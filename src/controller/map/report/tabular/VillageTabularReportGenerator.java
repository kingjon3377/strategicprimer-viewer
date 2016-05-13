package controller.map.report.tabular;

import java.io.IOException;
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
public class VillageTabularReportGenerator implements ITableGenerator<Village> {
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
	public VillageTabularReportGenerator(final Player currentPlayer, final Point hq) {
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
						   final Village item, final Point loc) throws IOException {
		writeField(ostream, distanceString(loc, base));
		writeFieldDelimiter(ostream);
		writeField(ostream, loc.toString());
		writeFieldDelimiter(ostream);
		writeField(ostream, getOwnerString(player, item.getOwner()));
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
		return "Distance,Location,Owner,Name";
	}
	/**
	 * @param one a Pair of one village and its location (in the other order)
	 * @param two a Pair of another village and its location (in the other order)
	 * @return the result of a comparison between the pairs.
	 */
	@Override
	public int comparePairs(final Pair<Point, Village> one,
							final Pair<Point, Village> two) {
		final DistanceComparator comparator = new DistanceComparator(base);
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
}
