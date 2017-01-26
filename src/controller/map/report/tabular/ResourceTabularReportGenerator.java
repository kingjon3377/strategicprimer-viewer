package controller.map.report.tabular;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import model.map.IFixture;
import model.map.Point;
import model.map.fixtures.Implement;
import model.map.fixtures.ResourcePile;
import model.map.fixtures.resources.CacheFixture;
import util.Accumulator;
import util.IntHolder;
import util.LineEnd;
import util.MultiMapHelper;
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
public final class ResourceTabularReportGenerator implements ITableGenerator<IFixture> {

	/**
	 * This generator can handle caches, resource piles, and implements.
	 * @param obj an object
	 * @return whether this report generator covers it
	 */
	@Override
	public boolean applies(final IFixture obj) {
		return (obj instanceof CacheFixture) || (obj instanceof ResourcePile) ||
					   (obj instanceof Implement);
	}

	/**
	 * Write a table row based on the given fixture.
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
						   final IFixture item, final Point loc) throws IOException {
		final String kind;
		final String quantity;
		final String specifics;
		if (item instanceof ResourcePile) {
			final ResourcePile pile = (ResourcePile) item;
			kind = pile.getKind();
			quantity = pile.getQuantity().toString();
			specifics = pile.getContents();
		} else if (item instanceof Implement) {
			kind = "equipment";
			quantity = "1";
			specifics = ((Implement) item).getKind();
		} else if (item instanceof CacheFixture) {
			final CacheFixture cache = (CacheFixture) item;
			kind = cache.getKind();
			quantity = "---";
			specifics = cache.getContents();
		} else {
			return false;
		}
		writeField(ostream, kind);
		writeFieldDelimiter(ostream);
		writeField(ostream, quantity);
		writeFieldDelimiter(ostream);
		writeField(ostream, specifics);
		ostream.append(getRowDelimiter());
		return true;
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
		// TODO: Try to use new-fangled Comparator style
		if (first instanceof ResourcePile) {
			if (second instanceof ResourcePile) {
				final ResourcePile firstPile = (ResourcePile) first;
				final ResourcePile secondPile = (ResourcePile) second;
				return Comparator.comparing(ResourcePile::getKind)
							   .thenComparing(ResourcePile::getContents)
							   .thenComparing(ResourcePile::getQuantity)
							   .compare(firstPile, secondPile);
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
				return Comparator.comparing(CacheFixture::getKind)
							   .thenComparing(CacheFixture::getContents)
							   .compare(firstCache, secondCache);
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
		final Map<String, Accumulator> implementCounts = new HashMap<>();
		for (final Pair<Integer, Pair<Point, IFixture>> pair : values) {
			final Pair<Point, IFixture> inner = pair.second();
			final IFixture fix = inner.second();
			if (fix instanceof Implement) {
				MultiMapHelper.getMapValue(implementCounts, ((Implement) fix).getKind(),
						key -> new IntHolder(0)).add(1);
				fixtures.remove(pair.first());
			} else if (produce(ostream, fixtures, fix, inner.first())) {
				fixtures.remove(pair.first());
			}
		}
		for (final Map.Entry<String, Accumulator> entry : implementCounts.entrySet()) {
			writeField(ostream, "equipment");
			writeFieldDelimiter(ostream);
			writeField(ostream, Integer.toString(entry.getValue().getValue()));
			writeFieldDelimiter(ostream);
			writeField(ostream, entry.getKey());
			ostream.append(getRowDelimiter());
		}
		fixtures.coalesce();
	}
}
