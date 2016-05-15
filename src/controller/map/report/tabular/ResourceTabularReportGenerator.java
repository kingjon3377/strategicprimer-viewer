package controller.map.report.tabular;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import model.map.IFixture;
import model.map.Point;
import model.map.fixtures.Implement;
import model.map.fixtures.ResourcePile;
import model.map.fixtures.resources.CacheFixture;
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
public class ResourceTabularReportGenerator implements ITableGenerator<IFixture> {
	/**
	 * @param obj an object
	 * @return whether this report generator covers it
	 */
	@Override
	public boolean applies(final IFixture obj) {
		return obj instanceof CacheFixture || obj instanceof ResourcePile ||
					   obj instanceof Implement;
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
			writeField(ostream, ((ResourcePile) item).getKind());
			writeFieldDelimiter(ostream);
			writeField(ostream,
					String.format("%d %s", ((ResourcePile) item).getQuantity(),
							((ResourcePile) item).getUnits()));
			writeFieldDelimiter(ostream);
			writeField(ostream, ((ResourcePile) item).getContents());
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
			writeField(ostream, ((CacheFixture) item).getKind());
			writeFieldDelimiter(ostream);
			writeField(ostream, "---");
			writeFieldDelimiter(ostream);
			writeField(ostream, ((CacheFixture) item).getContents());
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
				final int kindCmp = ((ResourcePile) first).getKind()
											.compareTo(((ResourcePile) second).getKind());
				if (kindCmp == 0) {
					final int contentsCmp = ((ResourcePile) first).getContents()
													.compareTo(((ResourcePile) second)
																	   .getContents());
					if (contentsCmp == 0) {
						final int unitsCmp = ((ResourcePile) first).getUnits().compareTo(
								((ResourcePile) second).getUnits());
						if (unitsCmp == 0) {
							return Integer.compare(((ResourcePile) first).getQuantity(),
									((ResourcePile) second).getQuantity());
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
				return ((Implement) first).getKind().compareTo(((Implement) second).getKind());
			} else if (second instanceof ResourcePile) {
				return 1;
			} else {
				return -1;
			}
		} else if (first instanceof CacheFixture) {
			if (second instanceof CacheFixture) {
				final int kindCmp = ((CacheFixture) first).getKind()
											.compareTo(((CacheFixture) second).getKind
																					   ());
				if (kindCmp == 0) {
					return ((CacheFixture) first).getContents()
								   .compareTo(((CacheFixture) second).getContents());
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
	 * @param ostream the stream to write the table to
	 * @param type the type of object being looked for
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
		Collections
				.sort(values, (one, two) -> comparePairs(one.second(), two.second()));
		ostream.append(headerRow());
		ostream.append('\n');
		final Map<String, Integer> implementCounts = new HashMap<>();
		for (final Pair<Integer, Pair<Point, IFixture>> pair : values) {
			final Pair<Point, IFixture> inner = pair.second();
			final IFixture fix = inner.second();
			if (fix instanceof Implement) {
				final String kind = ((Implement) fix).getKind();
				if (implementCounts.containsKey(kind)) {
					implementCounts.put(kind, Integer.valueOf(
							NullCleaner.assertNotNull(implementCounts.get(kind)).intValue() + 1));
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
			writeField(ostream, Integer.toString(entry.getValue()));
			writeFieldDelimiter(ostream);
			writeField(ostream, entry.getKey());
			ostream.append(getRowDelimiter());
		}
		fixtures.coalesce();
	}
}
