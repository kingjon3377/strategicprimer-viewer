package controller.map.report.tabular;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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
public class ExplorableTabularReportGenerator
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
	 * @param currentPlayer the player for whom the report is being created
	 * @param hq the HQ location of the player for whom the report is being created
	 */
	public ExplorableTabularReportGenerator(final Player currentPlayer, final Point hq) {
		player = currentPlayer;
		base = hq;
	}
	/**
	 * @param ostream the stream to write the row to
	 * @param fixtures the set of fixtures
	 * @param item the fixture to base the line on
	 * @param loc its location
	 * @throws IOException on I/O error writing to the stream
	 */
	@Override
	public boolean produce(final Appendable ostream,
						   final PatientMap<Integer, Pair<Point, IFixture>> fixtures,
						   final ExplorableFixture item, final Point loc) throws IOException {
		if (item instanceof Battlefield) {
			writeField(ostream, distanceString(loc, base));
			writeFieldDelimiter(ostream);
			writeField(ostream, loc.toString());
			writeFieldDelimiter(ostream);
			writeField(ostream, "ancient battlefield");
			writeFieldDelimiter(ostream);
			writeField(ostream, "---");
			writeFieldDelimiter(ostream);
			ostream.append(getRowDelimiter());
			return true;
		} else if (item instanceof Cave) {
			writeField(ostream, distanceString(loc, base));
			writeFieldDelimiter(ostream);
			writeField(ostream, loc.toString());
			writeFieldDelimiter(ostream);
			writeField(ostream, "caves nearby");
			writeFieldDelimiter(ostream);
			writeField(ostream, "---");
			writeFieldDelimiter(ostream);
			ostream.append(getRowDelimiter());
			return true;
		} else if (item instanceof Portal) {
			writeField(ostream, distanceString(loc, base));
			writeFieldDelimiter(ostream);
			writeField(ostream, loc.toString());
			writeFieldDelimiter(ostream);
			if (((Portal) item).getDestinationCoordinates().getRow() < 0) {
				writeField(ostream, "portal to another world");
			} else {
				writeField(ostream,
						"portal to world " + ((Portal) item).getDestinationWorld());
			}
			writeFieldDelimiter(ostream);
			writeField(ostream, "---");
			writeFieldDelimiter(ostream);
			ostream.append(getRowDelimiter());
			return true;
		} else if (item instanceof AdventureFixture) {
			writeField(ostream, distanceString(loc, base));
			writeFieldDelimiter(ostream);
			writeField(ostream, loc.toString());
			writeFieldDelimiter(ostream);
			writeField(ostream, ((AdventureFixture) item).getBriefDescription());
			writeFieldDelimiter(ostream);
			if (player.equals(((AdventureFixture) item).getOwner())) {
				writeField(ostream, "You");
			} else if (((AdventureFixture) item).getOwner().isIndependent()) {
				writeField(ostream, "No-one");
			} else {
				writeField(ostream,
						getOwnerString(player, ((AdventureFixture) item).getOwner()));
			}
			writeFieldDelimiter(ostream);
			writeField(ostream, ((AdventureFixture) item).getFullDescription());
			ostream.append(getRowDelimiter());
			return true;
		} else {
			return false;
		}
	}
	@Override
	public String headerRow() {
		return "Distance,Location,\"Brief Description\",\"Claimed By\",\"Long Description\"";
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
	 * @param ostream the stream to write the table to
	 * @param type the type of object being looked for
	 * @param fixtures the set of fixtures
	 * @throws IOException on I/O error writing to the stream
	 */
	@Override
	@SuppressWarnings("QuestionableName")
	public void produce(final Appendable ostream, final Class<ExplorableFixture> type,
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
		Collections
				.sort(values, (one, two) -> {
					final Pair<Point, IFixture> first = one.second();
					final Pair<Point, IFixture> second = two.second();
					final Comparator<Point> comparator = new DistanceComparator(base);
					final int cmp = comparator.compare(first.first(), second.first());
					if (cmp == 0) {
						return first.second().toString().compareTo(second.second().toString());
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
			if (((item instanceof ExplorableFixture) && produce(ostream, fixtures,
					(ExplorableFixture) pair.second().second(), pair.second().first())
			) ||
						((item instanceof TextFixture) &&
								 produceFromText(ostream, fixtures,
										 (TextFixture) pair.second().second(),
										 pair.second().first()))) {
				fixtures.remove(pair.first());
			}
		}
		fixtures.coalesce();
	}
	/**
	 * @param ostream the stream to write the row to
	 * @param fixtures the set of fixtures
	 * @param item the fixture to base the line on
	 * @param loc its location
	 * @throws IOException on I/O error writing to the stream
	 */
	@SuppressWarnings("BooleanMethodNameMustStartWithQuestion")
	public boolean produceFromText(final Appendable ostream,
								   final PatientMap<Integer, Pair<Point, IFixture>> fixtures,
								   final TextFixture item, final Point loc) throws IOException {
		writeField(ostream, distanceString(loc, base));
		writeFieldDelimiter(ostream);
		writeField(ostream, loc.toString());
		writeFieldDelimiter(ostream);
		if (item.getTurn() >= 0) {
			writeField(ostream, String.format("Text Note (Turn %d)",
					Integer.valueOf(item.getTurn())));
		} else {
			writeField(ostream, "Text Note");
		}
		writeFieldDelimiter(ostream);
		writeField(ostream, "---");
		writeFieldDelimiter(ostream);
		writeField(ostream, item.getText());
		ostream.append(getRowDelimiter());
		return true;
	}

	@Override
	public String toString() {
		return "ExplorableTabularReportGenerator";
	}
}
