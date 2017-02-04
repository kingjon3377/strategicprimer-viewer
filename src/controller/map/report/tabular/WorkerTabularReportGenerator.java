package controller.map.report.tabular;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.ToIntFunction;
import model.map.DistanceComparator;
import model.map.IFixture;
import model.map.Point;
import model.map.fixtures.mobile.IWorker;
import model.map.fixtures.mobile.worker.WorkerStats;
import util.Pair;
import util.PatientMap;

/**
 * A report generator for workers. We do not cover Jobs or Skills; see the main report
 * for that.
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
public final class WorkerTabularReportGenerator implements ITableGenerator<IWorker> {
	/**
	 * His or her HQ location.
	 */
	private final Point base;

	/**
	 * Constructor.
	 *
	 * @param hq his or her HQ location
	 */
	public WorkerTabularReportGenerator(final Point hq) {
		base = hq;
	}

	/**
	 * Produce a table line representing a worker.
	 * @param ostream  the stream to write the row to
	 * @param fixtures the set of fixtures
	 * @param item     the worker to base the line on
	 * @param loc      its location
	 * @throws IOException on I/O error writing to the stream
	 * @return true: remove this item from the Map
	 */
	@Override
	public boolean produce(final Appendable ostream,
						   final PatientMap<Integer, Pair<Point, IFixture>> fixtures,
						   final IWorker item, final Point loc) throws IOException {
		writeDelimitedField(ostream, distanceString(loc, base));
		writeDelimitedField(ostream, loc.toString());
		writeDelimitedField(ostream, item.getName());
		final Optional<WorkerStats> stats = Optional.ofNullable(item.getStats());
		// TODO: no-stats ends with field delimiter, with-stats doesn't
		// (and avoids using writeDelimitedField() to preserve that status quo)
		if (stats.isPresent()) {
			final WorkerStats actual = stats.get();
			writeDelimitedField(ostream, Integer.toString(actual.getHitPoints()));
			writeField(ostream, Integer.toString(actual.getMaxHitPoints()));
			for (final ToIntFunction<WorkerStats> field :
					Arrays.<ToIntFunction<WorkerStats>>asList(WorkerStats::getStrength,
							WorkerStats::getDexterity, WorkerStats::getConstitution,
							WorkerStats::getIntelligence, WorkerStats::getWisdom,
							WorkerStats::getCharisma)) {
				writeFieldDelimiter(ostream);
				writeField(ostream,
						WorkerStats.getModifierString(field.applyAsInt(actual)));
			}
		} else {
			for (int i = 0; i < 9; i++) {
				writeDelimitedField(ostream, "--");
			}
		}
		ostream.append(getRowDelimiter());
		return true;
	}

	/**
	 * The header row of the table.
	 * @return the header row for the tabular report
	 */
	@Override
	public String headerRow() {
		return "Distance,Location,Name,HP,\"Max HP\",Str,Dex,Con,Int,Wis,Cha";
	}

	/**
	 * Compare two worker-location pairs.
	 * @param one a Pair of one animal and its location (in the other order)
	 * @param two a Pair of another animal and its location (in the other order)
	 * @return the result of a comparison between the pairs.
	 */
	@SuppressWarnings("QuestionableName")
	@Override
	public int comparePairs(final Pair<Point, IWorker> one,
							final Pair<Point, IWorker> two) {
		final Comparator<Point> comparator = new DistanceComparator(base);
		final IWorker first = one.second();
		final IWorker second = two.second();
		final int cmp = comparator.compare(one.first(), two.first());
		if (cmp == 0) {
			return first.getName().compareTo(second.getName());
		} else {
			return cmp;
		}
	}

	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "WorkerTabularReportGenerator";
	}
}
