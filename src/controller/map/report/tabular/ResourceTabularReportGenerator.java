package controller.map.report.tabular;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import model.map.IFixture;
import model.map.Point;
import model.map.fixtures.Implement;
import model.map.fixtures.ResourcePile;
import model.map.fixtures.resources.CacheFixture;
import util.LineEnd;
import util.NullCleaner;
import util.Pair;
import util.PatientMap;

/**
 * A tabular report generator for resources, including caches, resource piles, and
 * implements.
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
public class ResourceTabularReportGenerator implements ITableGenerator<IFixture> {
	/**
	 * Compare two Numbers. If they're both Integers or BigDecimals, use the native
	 * conversion. If their integer parts are equal, compare using doubleValue(); if
	 * not, compare using those integer parts.
	 *
	 * @param first  the first number
	 * @param second the second number
	 * @return the result of the comparison
	 */
	private static int compareNumbers(final Number first, final Number second) {
		if (first instanceof Integer && second instanceof Integer) {
			return ((Integer) first).compareTo((Integer) second);
		} else if (first instanceof BigDecimal && second instanceof BigDecimal) {
			return ((BigDecimal) first).compareTo((BigDecimal) second);
		} else if (first.intValue() == second.intValue()) {
			return Double.compare(first.doubleValue(), second.doubleValue());
		} else {
			return Integer.compare(first.intValue(), second.intValue());
		}
	}

	/**
	 * @param obj an object
	 * @return whether this report generator covers it
	 */
	@Override
	public boolean applies(final IFixture obj) {
		return (obj instanceof CacheFixture) || (obj instanceof ResourcePile) ||
					   (obj instanceof Implement);
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
						   final IFixture item, final Point loc) throws IOException {
		if (item instanceof ResourcePile) {
			final ResourcePile pile = (ResourcePile) item;
			writeField(ostream, pile.getKind());
			writeFieldDelimiter(ostream);
			writeField(ostream,
					String.format("%s %s", pile.getQuantity().toString(),
							pile.getUnits()));
			writeFieldDelimiter(ostream);
			writeField(ostream, pile.getContents());
			ostream.append(getRowDelimiter());
			return true;
		} else if (item instanceof Implement) {
			writeField(ostream, "equipment");
			writeFieldDelimiter(ostream);
			writeField(ostream, "1");
			writeFieldDelimiter(ostream);
			writeField(ostream, ((Implement) item).getKind());
			ostream.append(getRowDelimiter());
			return true;
		} else if (item instanceof CacheFixture) {
			final CacheFixture cache = (CacheFixture) item;
			writeField(ostream, cache.getKind());
			writeFieldDelimiter(ostream);
			writeField(ostream, "---");
			writeFieldDelimiter(ostream);
			writeField(ostream, cache.getContents());
			ostream.append(getRowDelimiter());
			return true;
		} else {
			return false;
		}
	}

	@Override
	public String headerRow() {
		return "Kind,Quantity,Specifics";
	}

	@SuppressWarnings("QuestionableName")
	@Override
	public int comparePairs(final Pair<Point, IFixture> one,
							final Pair<Point, IFixture> two) {
		final IFixture first = one.second();
		final IFixture second = two.second();
		if (!applies(first) || !applies(second)) {
			throw new IllegalArgumentException("Unhandleable argument");
		}
		if (first instanceof ResourcePile) {
			if (second instanceof ResourcePile) {
				final ResourcePile firstPile = (ResourcePile) first;
				final ResourcePile secondPile = (ResourcePile) second;
				final int kindCmp = firstPile.getKind()
											.compareTo(secondPile.getKind());
				if (kindCmp == 0) {
					final int contentsCmp = firstPile.getContents()
													.compareTo(secondPile
																	   .getContents());
					if (contentsCmp == 0) {
						final int unitsCmp = firstPile.getUnits().compareTo(
								secondPile.getUnits());
						if (unitsCmp == 0) {
							return compareNumbers(firstPile.getQuantity(),
									secondPile.getQuantity());
						} else {
							return unitsCmp;
						}
					} else {
						return contentsCmp;
					}
				} else {
					return kindCmp;
				}
			} else {
				return -1;
			}
		} else if (first instanceof Implement) {
			if (second instanceof Implement) {
				return ((Implement) first).getKind()
							   .compareTo(((Implement) second).getKind());
			} else if (second instanceof ResourcePile) {
				return 1;
			} else {
				return -1;
			}
		} else if (first instanceof CacheFixture) {
			if (second instanceof CacheFixture) {
				final CacheFixture firstCache = (CacheFixture) first;
				final CacheFixture secondCache = (CacheFixture) second;
				final int kindCmp = firstCache.getKind()
											.compareTo(secondCache.getKind());
				if (kindCmp == 0) {
					return firstCache.getContents().compareTo(secondCache.getContents());
				} else {
					return kindCmp;
				}
			} else {
				return 1;
			}
		} else {
			throw new IllegalArgumentException("Unhandleable argument");
		}
	}

	/**
	 * Produce a tabular report on a particular category of fixtures in the map. All
	 * fixtures covered in this table should be removed from the set before returning.
	 *
	 * @param ostream  the stream to write the table to
	 * @param type     the type of object being looked for
	 * @param fixtures the set of fixtures
	 * @throws IOException on I/O error writing to the stream
	 */
	@SuppressWarnings("QuestionableName")
	@Override
	public void produce(final Appendable ostream, final Class<IFixture> type,
						final PatientMap<Integer, Pair<Point, IFixture>> fixtures)
			throws IOException {
		final List<Pair<Integer, Pair<Point, IFixture>>> values =
				new ArrayList<>(fixtures.entrySet().stream()
										.filter(entry -> applies(entry.getValue()
																		 .second()))
										.map(entry -> Pair.of(entry.getKey(),
												Pair.of(entry.getValue().first(),
														type.cast(entry.getValue()
																		  .second()))))
										.collect(Collectors.toList()));
		values.sort((one, two) -> comparePairs(one.second(), two.second()));
		ostream.append(headerRow());
		ostream.append(LineEnd.LINE_SEP);
		final Map<String, Integer> implementCounts = new HashMap<>();
		for (final Pair<Integer, Pair<Point, IFixture>> pair : values) {
			final Pair<Point, IFixture> inner = pair.second();
			final IFixture fix = inner.second();
			if (fix instanceof Implement) {
				final String kind = ((Implement) fix).getKind();
				if (implementCounts.containsKey(kind)) {
					implementCounts.put(kind, Integer.valueOf(
							NullCleaner.assertNotNull(implementCounts.get(kind))
									.intValue() + 1));
				} else {
					implementCounts.put(kind, Integer.valueOf(1));
				}
				fixtures.remove(pair.first());
			} else if (produce(ostream, fixtures, fix, inner.first())) {
				fixtures.remove(pair.first());
			}
		}
		for (final Map.Entry<String, Integer> entry : implementCounts.entrySet()) {
			writeField(ostream, "equipment");
			writeFieldDelimiter(ostream);
			writeField(ostream, Integer.toString(entry.getValue().intValue()));
			writeFieldDelimiter(ostream);
			writeField(ostream, entry.getKey());
			ostream.append(getRowDelimiter());
		}
		fixtures.coalesce();
	}
}
