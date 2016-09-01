package controller.map.report.tabular;

import java.io.IOException;
import java.util.Comparator;
import model.map.DistanceComparator;
import model.map.IFixture;
import model.map.Point;
import model.map.fixtures.mobile.Animal;
import util.Pair;
import util.PatientMap;

/**
 * A report generator for sightings of animals.
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
public class AnimalTabularReportGenerator implements ITableGenerator<Animal> {
	/**
	 * The base point to use for distance calculations
	 */
	private final Point base;
	/**
	 * Constructor.
	 * @param hq the HQ location of the player for whom the report is being produced
	 */
	public AnimalTabularReportGenerator(final Point hq) {
		base = hq;
	}
	/**
	 * Produce a single line of the tabular report on animals. The fixture that this
	 * report references should be removed from the set before returning.
	 * @param ostream the stream to write the row to
	 * @param fixtures the set of fixtures
	 * @param item the item to base the line on
	 * @param loc the location of this item
	 */
	@Override
	public boolean produce(final Appendable ostream,
						final PatientMap<Integer, Pair<Point, IFixture>> fixtures,
						final Animal item, final Point loc) throws IOException {
		writeField(ostream, distanceString(loc, base));
		writeFieldDelimiter(ostream);
		writeField(ostream, loc.toString());
		writeFieldDelimiter(ostream);
		if (item.isTraces()) {
			writeField(ostream, "tracks or traces of " + item.getKind());
		} else if (item.isTalking()) {
			writeField(ostream, "talking " + item.getKind());
		} else if (!"wild".equals(item.getStatus())) {
			writeField(ostream, item.getStatus() + ' ' + item.getKind());
		} else {
			writeField(ostream, item.getKind());
		}
		ostream.append(getRowDelimiter());
		return true;
	}

	/**
	 * @return the header row for the tabular report
	 */
	@Override
	public String headerRow() {
		return "Distance,Location,Kind";
	}

	/**
	 * @param one a Pair of one animal and its location (in the other order)
	 * @param two a Pair of another animal and its location (in the other order)
	 * @return the result of a comparison between the pairs.
	 */
	@SuppressWarnings({"QuestionableName", "IfStatementWithIdenticalBranches"})
	@Override
	public int comparePairs(final Pair<Point, Animal> one,
							final Pair<Point, Animal> two) {
		final Comparator<Point> comparator = new DistanceComparator(base);
		final int cmp = comparator.compare(one.first(), two.first());
		if (cmp == 0) {
			final Animal first = one.second();
			final Animal second = two.second();
			if (first.isTalking() && !second.isTalking()) {
				return -1;
			} else if (second.isTalking() && !first.isTalking()) {
				return 1;
			} else if (first.isTraces() && !second.isTraces()) {
				return 1;
			} else if (second.isTraces() && !first.isTraces()) {
				return -1;
			} else {
				return first.getKind().compareTo(second.getKind());
			}
		} else {
			return cmp;
		}
	}

	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "AnimalTabularReportGenerator";
	}
}
