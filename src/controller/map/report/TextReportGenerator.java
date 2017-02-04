package controller.map.report;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Formatter;
import java.util.List;
import java.util.Map;
import model.map.IFixture;
import model.map.IMapNG;
import model.map.Player;
import model.map.Point;
import model.map.fixtures.TextFixture;
import model.report.EmptyReportNode;
import model.report.IReportNode;
import model.report.SectionListReportNode;
import model.report.SimpleReportNode;
import org.eclipse.jdt.annotation.NonNull;
import util.Pair;
import util.PatientMap;

/**
 * A report generator for arbitrary-text notes.
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
public final class TextReportGenerator extends AbstractReportGenerator<TextFixture> {
	/**
	 * Constructor.
	 * @param comparator a comparator for pairs of Points and fixtures.
	 */
	public TextReportGenerator(final Comparator<@NonNull Pair<@NonNull Point, @NonNull
																					  IFixture>> comparator) {
		super(comparator);
	}

	/**
	 * Produce the sub-report dealing with arbitrary-text notes.
	 * @param fixtures      the set of fixtures
	 * @param map           ignored
	 * @param currentPlayer the player for whom the report is being produced
	 * @param ostream       the Formatter to write to
	 */
	@Override
	public void produce(PatientMap<Integer, Pair<Point, IFixture>> fixtures, IMapNG map,
						Player currentPlayer, final Formatter ostream) {
		final List<Pair<Point, TextFixture>> items = new ArrayList<>();
		for (final Map.Entry<Integer, Pair<Point, IFixture>> entry :
				fixtures.entrySet()) {
			final Pair<Point, IFixture> pair = entry.getValue();
			if (pair.second() instanceof TextFixture) {
				items.add(Pair.of(pair.first(), (TextFixture) pair.second()));
				fixtures.remove(entry.getKey());
			}
		}
		items.sort(Comparator.comparingInt(pair -> pair.second().getTurn()));
		if (!items.isEmpty()) {
			ostream.format("<h4>Miscellaneous Notes</h4>%n<ul>%n");
			for (final Pair<Point, TextFixture> item : items) {
				ostream.format("%s", OPEN_LIST_ITEM);
				produce(fixtures, map, currentPlayer, item.second(), item.first(), ostream);
				ostream.format("</li>%n");
			}
			ostream.format("</ul>%n");
		}
	}

	/**
	 * Produce a report on an individual arbitrary-text note. This does *not* remove the
	 * fixture from the collection, because it doesn't know the synthetic ID # that was
	 * assigned to it.
	 *
	 * @param fixtures      the set of fixtures
	 * @param map           ignored
	 * @param currentPlayer the player for whom the report is being produced
	 * @param item          an arbitrary-text note
	 * @param loc           where it is located
	 * @param ostream	    the Formatter to write to
	 */
	@Override
	public void produce(final PatientMap<Integer, Pair<Point, IFixture>> fixtures,
						final IMapNG map, final Player currentPlayer,
						final TextFixture item, final Point loc, final Formatter ostream) {
		ostream.format("At %s %s", loc.toString(),
				distCalculator.distanceString(loc));
		if (item.getTurn() >= 0) {
			ostream.format(": On turn %d", Integer.valueOf(item.getTurn()));
		}
		ostream.format(": %s", item.getText());
	}

	/**
	 * Produce the sub-report, in RIR, dealing with arbitrary-text notes.
	 * @param fixtures      the set of fixtures
	 * @param map           ignored
	 * @param currentPlayer the player for whom the report is being produced
	 * @return the sub-report dealing with arbitrary-text notes
	 */
	@Override
	public IReportNode produceRIR(final PatientMap<Integer, Pair<Point, IFixture>>
										  fixtures,
								  final IMapNG map, final Player currentPlayer) {
		final IReportNode retval =
				new SectionListReportNode(4, "Miscellaneous Notes");
		for (final Map.Entry<Integer, Pair<Point, IFixture>> entry :
				fixtures.entrySet()) {
			final Pair<Point, IFixture> pair = entry.getValue();
			if (pair.second() instanceof TextFixture) {
				retval.add(produceRIR(fixtures, map, currentPlayer,
						(TextFixture) pair.second(), pair.first()));
				fixtures.remove(entry.getKey());
			}
		}
		if (retval.getChildCount() > 0) {
			return retval;
		} else {
			return EmptyReportNode.NULL_NODE;
		}
	}

	/**
	 * This does *not* remove the fixture from the collection, because it doesn't know
	 * the
	 * synthetic ID # that was assigned to it.
	 *
	 * @param fixtures      the set of fixtures
	 * @param map           ignored
	 * @param currentPlayer the player for whom the report is being produced
	 * @param item          an arbitrary-text note
	 * @param loc           where it is located
	 * @return the sub-report dealing with that note
	 */
	@Override
	public IReportNode produceRIR(final PatientMap<Integer, Pair<Point, IFixture>>
										  fixtures,
								  final IMapNG map, final Player currentPlayer,
								  final TextFixture item, final Point loc) {
		if (item.getTurn() >= 0) {
			return new SimpleReportNode(loc, "At ", loc.toString(), " ",
											   distCalculator.distanceString(loc), ": ",
											   "On turn ",
											   Integer.toString(item.getTurn()), ": ",
											   item.getText());
		} else {
			return new SimpleReportNode(loc, "At ", loc.toString(), " ",
											   distCalculator.distanceString(loc), ": ",
											   item.getText());
		}
	}

	/**
	 * A trivial toString().
	 * @return a string representation of this class
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "TextReportGenerator";
	}
}
