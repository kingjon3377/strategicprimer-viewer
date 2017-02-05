package controller.map.report.tabular;

import java.io.IOException;
import java.util.Comparator;
import model.map.DistanceComparator;
import model.map.IFixture;
import model.map.Point;
import model.map.fixtures.mobile.Immortal;
import model.map.fixtures.mobile.MobileFixture;
import util.Pair;
import util.PatientMap;

/**
 * A tabular report generator for "immortals."
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
public final class ImmortalsTabularReportGenerator
		implements ITableGenerator<MobileFixture> {
	/**
	 * The base point to use for distance calculations.
	 */
	private final Point base;

	/**
	 * Constructor.
	 *
	 * @param hq the HQ location of the player for whom the report is being created
	 */
	public ImmortalsTabularReportGenerator(final Point hq) {
		base = hq;
	}

	/**
	 * Whether we can handle the given object.
	 * @param obj an object
	 * @return whether this report generator covers it
	 */
	@Override
	public boolean applies(final IFixture obj) {
		return obj instanceof Immortal;
	}

	/**
	 * Produce a table row for the given fixture.
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
						   final MobileFixture item, final Point loc) throws
			IOException {
		if (applies(item)) {
			writeDelimitedField(ostream, distanceString(loc, base));
			writeDelimitedField(ostream, loc.toString());
			writeField(ostream, item.toString());
			ostream.append(getRowDelimiter());
			return true;
		} else {
			return false;
		}
	}

	@Override
	public String headerRow() {
		return "Distance,Location,Immortal";
	}

	@SuppressWarnings("QuestionableName")
	@Override
	public int comparePairs(final Pair<Point, MobileFixture> one,
							final Pair<Point, MobileFixture> two) {
		final Comparator<Point> comparator = new DistanceComparator(base);
		final MobileFixture first = one.second();
		final MobileFixture second = two.second();
		final int cmp = comparator.compare(one.first(), two.first());
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
	}

	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "ImmortalsTabularReportGenerator";
	}
	/**
	 * FIXME: Use Immortal as generic type parameter and here
	 * The type of objects we accept. Needed so the default
	 * {@link ITableGenerator#produce(Appendable, PatientMap)} can call the typesafe single-row
	 * produce() without causing class-cast exceptions or taking this Class object as a
	 * parameter.
	 * @return the type of the objects we accept
	 */
	@Override
	public Class<MobileFixture> type() {
		return MobileFixture.class;
	}
	/**
	 * The file-name to (by default) write this table to.
	 */
	@Override
	public String getTableName() {
		return "immortals";
	}
}
