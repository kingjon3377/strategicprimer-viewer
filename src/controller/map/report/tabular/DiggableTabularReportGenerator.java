package controller.map.report.tabular;

import java.io.IOException;
import model.map.DistanceComparator;
import model.map.HasKind;
import model.map.IFixture;
import model.map.Point;
import model.map.TileFixture;
import model.map.fixtures.Ground;
import model.map.fixtures.resources.Mine;
import model.map.fixtures.resources.MineralVein;
import model.map.fixtures.resources.StoneDeposit;
import util.Pair;
import util.PatientMap;

/**
 * A tabular report generator for resources that can be mined---mines, mineral veins,
 * stone deposits, and Ground.
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
public class DiggableTabularReportGenerator implements ITableGenerator<TileFixture> {
	/**
	 * The base point to use for distance calculations.
	 */
	private final Point base;

	/**
	 * Constructor.
	 *
	 * @param distBase the point to use as the base for distance calculations
	 */
	public DiggableTabularReportGenerator(final Point distBase) {
		base = distBase;
	}

	/**
	 * @param obj an object
	 * @return whether this report generator covers it
	 */
	@Override
	public boolean applies(final IFixture obj) {
		return (obj instanceof Ground) || (obj instanceof Mine) ||
					   (obj instanceof MineralVein) ||
					   (obj instanceof StoneDeposit);
	}

	/**
	 * @param ostream  the stream to write the row to
	 * @param fixtures the set of fixtures
	 * @param item     the fixture to base the line on
	 * @param loc      its location
	 * @throws IOException on I/O error writing to the stream
	 */
	@Override
	public boolean produce(final Appendable ostream,
						   final PatientMap<Integer, Pair<Point, IFixture>> fixtures,
						   final TileFixture item, final Point loc) throws IOException {
		final String classField;
		final String statusField;
		if (item instanceof Ground) {
			classField = "ground";
			statusField = ternary(((Ground) item).isExposed(), "exposed", "not exposed");
		} else if (item instanceof Mine) {
			classField = "mine";
			statusField = ((Mine) item).getStatus().toString();
		} else if (item instanceof StoneDeposit) {
			classField = "deposit";
			statusField = "exposed";
		} else if (item instanceof MineralVein) {
			classField = "vein";
			statusField = ternary(((MineralVein) item).isExposed(), "exposed", "not exposed");
		} else {
			return false;
		}
		writeField(ostream, distanceString(loc, base));
		writeFieldDelimiter(ostream);
		writeField(ostream, loc.toString());
		writeFieldDelimiter(ostream);
		writeField(ostream, classField);
		writeFieldDelimiter(ostream);
		writeField(ostream, ((HasKind) item).getKind());
		writeFieldDelimiter(ostream);
		writeField(ostream, statusField);
		ostream.append(getRowDelimiter());
		return true;
	}

	/**
	 * @param condition a boolean value
	 * @param truth what to return if it is true
	 * @param falsehood what to return if it is false
	 * @return truth if condition is true, falsehood otherwise
	 */
	private <T> T ternary(final boolean condition, final T truth, final T falsehood) {
		if (condition) {
			return truth;
		} else {
			return falsehood;
		}
	}

	@Override
	public String headerRow() {
		return "Distance,Location,Kind,Product,Status";
	}

	@SuppressWarnings("QuestionableName")
	@Override
	public int comparePairs(final Pair<Point, TileFixture> one,
							final Pair<Point, TileFixture> two) {
		final TileFixture first = one.second();
		final TileFixture second = two.second();
		if (!applies(first) || !applies(second)) {
			throw new IllegalArgumentException("Unhandleable argument");
		}
		final int prodCmp =
				((HasKind) first).getKind().compareTo(((HasKind) second).getKind());
		if (prodCmp == 0) {
			final int cmp =
					new DistanceComparator(base).compare(one.first(), two.first());
			if (cmp == 0) {
				final int kindCmp = Integer.compare(first.getClass().hashCode(),
						second.getClass().hashCode());
				if (kindCmp == 0) {
					return Integer.compare(first.hashCode(), second.hashCode());
				} else {
					return kindCmp;
				}
			} else {
				return cmp;
			}
		} else {
			return prodCmp;
		}
	}

	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "DiggableTabularReportGenerator";
	}
}
