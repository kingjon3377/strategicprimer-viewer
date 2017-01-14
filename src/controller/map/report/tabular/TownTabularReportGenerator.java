package controller.map.report.tabular;

import controller.map.misc.TownComparator;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
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
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class TownTabularReportGenerator implements ITableGenerator<AbstractTown> {
	/**
	 * The player for whom this report is being produced.
	 */
	private final Player player;
	/**
	 * His or her HQ location.
	 */
	private final Point base;
	/**
	 * The list of comparators to try in turn in comparePairs() until one produces a
	 * non-zero result.
	 */
	private final List<Comparator<Pair<Point, AbstractTown>>> comps;

	/**
	 * Constructor.
	 *
	 * @param currentPlayer the player for whom this report is being produced
	 * @param hq            his or her HQ location
	 */
	@SuppressWarnings("QuestionableName")
	public TownTabularReportGenerator(final Player currentPlayer, final Point hq) {
		player = currentPlayer;
		base = hq;
		comps = Arrays.asList(
				(one, two) -> TownComparator.compareTownKind(one.second(), two.second()),
				(one, two) -> new DistanceComparator(base)
									  .compare(one.first(), two.first()),
				(one, two) -> TownComparator.compareTownSize(one.second().size(),
						two.second().size()),
				(one, two) -> TownComparator.compareTownStatus(one.second().status(),
						two.second().status()),
				Comparator.comparing(one -> one.second().getName()));
	}

	/**
	 * Produce a table line representing a town.
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
	 * The header row for the tabular report on towns.
	 * @return the header row for the tabular report
	 */
	@Override
	public String headerRow() {
		return "Distance,Location,Owner,Kind,Size,Status,Name";
	}

	/**
	 * Compare two pairs.
	 * @param one a Pair of one town and its location (in the other order)
	 * @param two a Pair of another town and its location (in the other order)
	 * @return the result of a comparison between the pairs.
	 */
	@SuppressWarnings("QuestionableName")
	@Override
	public int comparePairs(final Pair<Point, AbstractTown> one,
							final Pair<Point, AbstractTown> two) {
		int retval = 0;
		for (final Comparator<Pair<Point, AbstractTown>> comparator : comps) {
			retval = comparator.compare(one, two);
			if (retval != 0) {
				return retval;
			}
		}
		return retval;
	}

	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "TownTabularReportGenerator";
	}
}
