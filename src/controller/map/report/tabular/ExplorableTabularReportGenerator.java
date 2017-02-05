package controller.map.report.tabular;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import model.map.DistanceComparator;
import model.map.IFixture;
import model.map.Player;
import model.map.Point;
import model.map.fixtures.TextFixture;
import model.map.fixtures.explorable.AdventureFixture;
import model.map.fixtures.explorable.Battlefield;
import model.map.fixtures.explorable.Cave;
import model.map.fixtures.explorable.ExplorableFixture;
import model.map.fixtures.explorable.Portal;
import util.Pair;
import util.PatientMap;

/**
 * A tabular report generator for things that can be explored and are not covered
 * elsewhere: caves, battlefields, adventure hooks, and portals.
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
public final class ExplorableTabularReportGenerator
		implements ITableGenerator<ExplorableFixture> {
	/**
	 * The player for whom the report is being created.
	 */
	private final Player player;
	/**
	 * The base point to use for distance calculations.
	 */
	private final Point base;

	/**
	 * Constructor.
	 *
	 * @param currentPlayer the player for whom the report is being created
	 * @param hq            the HQ location of the player for whom the report is being
	 *                      created
	 */
	public ExplorableTabularReportGenerator(final Player currentPlayer, final Point hq) {
		player = currentPlayer;
		base = hq;
	}

	/**
	 * Produce a report line about the given fixture.
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
						   final ExplorableFixture item, final Point loc)
			throws IOException {
		final String brief;
		final String owner;
		final String longDesc;
		if (item instanceof Battlefield) {
			brief = "ancient battlefield";
			owner = "---";
			longDesc = "";
		} else if (item instanceof Cave) {
			brief = "caves nearby";
			writeFieldDelimiter(ostream);
			owner = "---";
			longDesc = "";
		} else if (item instanceof Portal) {
			final Portal portal = (Portal) item;
			if (!portal.getDestinationCoordinates().isValid()) {
				brief = "portal to another world";
			} else {
				brief = "portal to world " + portal.getDestinationWorld();
			}
			owner = "---";
			longDesc = "";
		} else if (item instanceof AdventureFixture) {
			final AdventureFixture adventure = (AdventureFixture) item;
			brief = adventure.getBriefDescription();
			if (player.equals(adventure.getOwner())) {
				owner = "You";
			} else if (adventure.getOwner().isIndependent()) {
				owner = "No-one";
			} else {
				owner = getOwnerString(player, adventure.getOwner());
			}
			longDesc = adventure.getFullDescription();
		} else {
			return false;
		}
		writeDelimitedField(ostream, distanceString(loc, base));
		writeDelimitedField(ostream, loc.toString());
		writeDelimitedField(ostream, brief);
		writeDelimitedField(ostream, owner);
		writeField(ostream, longDesc);
		ostream.append(getRowDelimiter());
		return true;
	}

	@Override
	public String headerRow() {
		return "Distance,Location,\"Brief Description\",\"Claimed By\",\"Long " +
					   "Description\"";
	}

	@SuppressWarnings("QuestionableName")
	@Override
	public int comparePairs(final Pair<Point, ExplorableFixture> one,
							final Pair<Point, ExplorableFixture> two) {
		final Comparator<Point> comparator = new DistanceComparator(base);
		final ExplorableFixture first = one.second();
		final ExplorableFixture second = two.second();
		final int cmp = comparator.compare(one.first(), two.first());
		if (cmp == 0) {
			return first.toString().compareTo(second.toString());
		} else {
			return cmp;
		}
	}

	/**
	 * This generator can only handle ExplorableFixtures and TextFixtures.
	 * @param obj an object
	 * @return whether this report generator covers it
	 */
	@Override
	public boolean applies(final IFixture obj) {
		return (obj instanceof ExplorableFixture) || (obj instanceof TextFixture);
	}

	/**
	 * Produce a tabular report on a particular category of fixtures in the map. All
	 * fixtures covered in this table should be removed from the set before returning.
	 * We override this to, in addition to the fixtures covered by the type parameter,
	 * report on text fixtures.
	 *
	 * @param type     the type of object being looked for
	 * @param ostream  the stream to write the table to
	 * @param fixtures the set of fixtures
	 * @throws IOException on I/O error writing to the stream
	 */
	@Override
	@SuppressWarnings("QuestionableName")
	public void produce(final Appendable ostream,
						final PatientMap<Integer, Pair<Point, IFixture>> fixtures)
			throws IOException {
		final List<Pair<Integer, Pair<Point, IFixture>>> values =
				new ArrayList<>(fixtures.entrySet().stream()
										.filter(entry -> applies(entry.getValue()
																		 .second()))
										.map(entry -> Pair.of(entry.getKey(),
												Pair.of(entry.getValue().first(),
														entry.getValue()
																.second())))
										.collect(Collectors.toList()));
		values.sort((one, two) -> {
			final Pair<Point, IFixture> first = one.second();
			final Pair<Point, IFixture> second = two.second();
			final Comparator<Point> comparator = new DistanceComparator(base);
			final int cmp = comparator.compare(first.first(), second.first());
			if (cmp == 0) {
				return first.second().toString()
							   .compareTo(second.second().toString());
			} else {
				return cmp;
			}
		});
		if (!headerRow().isEmpty()) {
			ostream.append(headerRow());
			ostream.append(getRowDelimiter());
		}
		for (final Pair<Integer, Pair<Point, IFixture>> pair : values) {
			final IFixture item = pair.second().second();
			if ((item instanceof ExplorableFixture) && produce(ostream, fixtures,
					(ExplorableFixture) pair.second().second(), pair.second().first())) {
				fixtures.remove(pair.first());
			} else if (item instanceof TextFixture) {
				produceFromText(ostream, (TextFixture) pair.second().second(),
						pair.second().first());
				fixtures.remove(pair.first());
			}
		}
		fixtures.coalesce();
	}

	/**
	 * Produce a report line from a TextFixture.
	 * @param ostream the stream to write the row to
	 * @param item    the fixture to base the line on
	 * @param loc     its location
	 * @throws IOException on I/O error writing to the stream
	 */
	private void produceFromText(final Appendable ostream,
								final TextFixture item, final Point loc)
			throws IOException {
		writeDelimitedField(ostream, distanceString(loc, base));
		writeDelimitedField(ostream, loc.toString());
		if (item.getTurn() >= 0) {
			writeDelimitedField(ostream, String.format("Text Note (Turn %d)",
					Integer.valueOf(item.getTurn())));
		} else {
			writeDelimitedField(ostream, "Text Note");
		}
		writeDelimitedField(ostream, "---");
		writeField(ostream, item.getText());
		ostream.append(getRowDelimiter());
	}

	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "ExplorableTabularReportGenerator";
	}
	/**
	 * The type of objects we accept. Needed so the default
	 * {@link ITableGenerator#produce(Appendable, PatientMap)} can call the typesafe single-row
	 * produce() without causing class-cast exceptions or taking this Class object as a
	 * parameter.
	 * @return the type of the objects we accept
	 */
	@Override
	public Class<ExplorableFixture> type() {
		return ExplorableFixture.class;
	}
}
