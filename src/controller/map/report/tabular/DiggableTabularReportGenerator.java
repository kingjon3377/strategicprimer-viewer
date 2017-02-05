package controller.map.report.tabular;

import java.io.IOException;
import java.util.Comparator;
import model.map.DistanceComparator;
import model.map.HasKind;
import model.map.IFixture;
import model.map.Point;
import model.map.fixtures.Ground;
import model.map.fixtures.MineralFixture;
import model.map.fixtures.resources.Mine;
import model.map.fixtures.resources.MineralVein;
import model.map.fixtures.resources.StoneDeposit;
import util.Pair;
import util.PatientMap;

import static util.Ternary.ternary;

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
public final class DiggableTabularReportGenerator
		implements ITableGenerator<MineralFixture> {
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
	 * Whether we can handle a given fixture.
	 * @param obj an object
	 * @return whether this report generator covers it
	 */
	@Override
	public boolean applies(final IFixture obj) {
		return obj instanceof MineralFixture;
	}

	/**
	 * Produce the report line for a fixture.
	 * @param ostream  the stream to write the row to
	 * @param fixtures the set of fixtures
	 * @param item     the fixture to base the line on
	 * @param loc      its location
	 * @throws IOException on I/O error writing to the stream
	 * @return whether to remove this item from the Map
	 */
	@Override
	public boolean produce(final Appendable ostream,
						   final PatientMap<Integer, Pair<Point, IFixture>> fixtures,
						   final MineralFixture item, final Point loc) throws IOException {
		final String classField;
		final String statusField;
		if (item instanceof Ground) {
			classField = "ground";
			statusField = ternary(((Ground) item).isExposed(), "exposed",
					"not exposed");
		} else if (item instanceof Mine) {
			classField = "mine";
			statusField = ((Mine) item).getStatus().toString();
		} else if (item instanceof StoneDeposit) {
			classField = "deposit";
			statusField = "exposed";
		} else if (item instanceof MineralVein) {
			classField = "vein";
			statusField = ternary(((MineralVein) item).isExposed(), "exposed",
					"not exposed");
		} else {
			return false;
		}
		writeDelimitedField(ostream, distanceString(loc, base));
		writeDelimitedField(ostream, loc.toString());
		writeDelimitedField(ostream, classField);
		writeDelimitedField(ostream, ((HasKind) item).getKind());
		writeField(ostream, statusField);
		ostream.append(getRowDelimiter());
		return true;
	}

	@Override
	public String headerRow() {
		return "Distance,Location,Kind,Product,Status";
	}

	@SuppressWarnings("QuestionableName")
	@Override
	public int comparePairs(final Pair<Point, MineralFixture> one,
							final Pair<Point, MineralFixture> two) {
		return Comparator.comparing(
				(Pair<Point, MineralFixture> pair) -> pair.second().getKind())
					   .thenComparing(Pair::first, new DistanceComparator(base))
					   .thenComparingInt(pair -> pair.second().hashCode())
					   .compare(one, two);
	}

	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "DiggableTabularReportGenerator";
	}
	/**
	 * The type of objects we accept. Needed so the default
	 * {@link ITableGenerator#produce(Appendable, PatientMap)} can call the typesafe single-row
	 * produce() without causing class-cast exceptions or taking this Class object as a
	 * parameter.
	 * @return the type of the objects we accept
	 */
	@Override
	public Class<MineralFixture> type() {
		return MineralFixture.class;
	}
	/**
	 * The file-name to (by default) write this table to.
	 */
	@Override
	public String getTableName() {
		return "minerals";
	}
}
