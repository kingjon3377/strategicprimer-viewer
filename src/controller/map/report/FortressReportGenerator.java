package controller.map.report;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.eclipse.jdt.annotation.NonNull;

import model.map.IFixture;
import model.map.IMapNG;
import model.map.Player;
import model.map.Point;
import model.map.River;
import model.map.TileFixture;
import model.map.fixtures.FortressMember;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.terrain.Forest;
import model.map.fixtures.terrain.Hill;
import model.map.fixtures.terrain.Mountain;
import model.map.fixtures.terrain.Oasis;
import model.map.fixtures.towns.Fortress;
import model.report.AbstractReportNode;
import model.report.ComplexReportNode;
import model.report.EmptyReportNode;
import model.report.ListReportNode;
import model.report.SectionListReportNode;
import model.report.SectionReportNode;
import model.report.SimpleReportNode;
import util.DelayedRemovalMap;
import util.NullCleaner;
import util.Pair;

import javax.swing.tree.MutableTreeNode;

/**
 * A report generator for fortresses.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2013-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of version 3 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class FortressReportGenerator extends AbstractReportGenerator<Fortress> {
	/**
	 * @param comparator a comparator for pairs of Points and fixtures.
	 */
	public FortressReportGenerator(final Comparator<@NonNull Pair<@NonNull Point, @NonNull IFixture>> comparator) {
		super(comparator);
	}
	/**
	 * Instance we use.
	 */
	private final UnitReportGenerator urg = new UnitReportGenerator(pairComparator);

	/**
	 * The longest a river report could be.
	 */
	private static final int RIVER_RPT_LEN = ("There is a river on the tile, "
			+ "flowing through the following borders: "
			+ "north, south, east, west").length();
	/**
	 * All fixtures referred to in this report are removed from the collection.
	 *
	 * @param fixtures the set of fixtures
	 * @param currentPlayer the player for whom the report is being produced
	 * @param map the map (needed to get terrain information)
	 * @return the part of the report dealing with fortresses
	 */
	@Override
	public String produce(
			final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
			final IMapNG map, final Player currentPlayer) {
		// This can get long. We'll give it 16K.
		final StringBuilder ours = new StringBuilder(16384)
				.append("<h4>Your fortresses in the map:</h4>\n");
		final StringBuilder builder =
				new StringBuilder(16384)
				.append("<h4>Foreign fortresses in the map:</h4>\n");
		final List<Pair<Point, IFixture>> values = new ArrayList<>(fixtures.values());
		Collections.sort(values, pairComparator);
		boolean anyforts = false;
		boolean anyours = false;
		for (final Pair<Point, IFixture> pair : values) {
			if (pair.second() instanceof Fortress) {
				final Fortress fort = (Fortress) pair.second();
				if (currentPlayer.equals(fort.getOwner())) {
					anyours = true;
					ours.append(produce(fixtures, map, currentPlayer, fort,
							pair.first()));
				} else {
					anyforts = true;
					builder.append(produce(fixtures, map, currentPlayer,
							fort, pair.first()));
				}
			}
		}
		if (anyours) {
			if (anyforts) {
				ours.append(builder.toString());
			}
			return NullCleaner.assertNotNull(ours.toString()); // NOPMD
		} else if (anyforts) {
			return NullCleaner.assertNotNull(builder.toString()); // NOPMD
		} else {
			return "";
		}
	}

	/**
	 * All fixtures referred to in this report are removed from the collection.
	 *
	 * @param fixtures the set of fixtures
	 * @param currentPlayer the player for whom the report is being produced
	 * @param map the map (needed to get terrain information)
	 * @return the part of the report dealing with fortresses
	 */
	@Override
	public AbstractReportNode produceRIR(
			final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
			final IMapNG map, final Player currentPlayer) {
		final List<Pair<Point, IFixture>> values = new ArrayList<>(fixtures.values());
		Collections.sort(values, pairComparator);
		final AbstractReportNode foreign = new SectionReportNode(4,
				                                                        "Foreign fortresses in the map:");
		final AbstractReportNode ours = new SectionReportNode(4,
				                                                     "Your fortresses in the map:");
		values.stream().filter(pair -> pair.second() instanceof Fortress).forEach(pair -> {
			final Fortress fort = (Fortress) pair.second();
			if (currentPlayer.equals(fort.getOwner())) {
				ours.add(produceRIR(fixtures, map, currentPlayer,
						(Fortress) pair.second(), pair.first()));
			} else {
				foreign.add(produceRIR(fixtures, map, currentPlayer,
						(Fortress) pair.second(), pair.first()));
			}
		});
		final AbstractReportNode retval = new ComplexReportNode("");
		if (ours.getChildCount() != 0) {
			retval.add(ours);
		}
		if (foreign.getChildCount() != 0) {
			retval.add(foreign);
		}
		if (retval.getChildCount() == 0) {
			return EmptyReportNode.NULL_NODE; // NOPMD
		} else {
			return retval;
		}
	}

	/**
	 * @param map the map
	 * @param point a point
	 * @param fixtures the set of fixtures, so we can schedule the removal the
	 *        terrain fixtures from it
	 * @return a String describing the terrain on it
	 */
	private static String getTerrain(final IMapNG map, final Point point,
			final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures) {
		final StringBuilder builder = new StringBuilder(130).append(
				"Surrounding terrain: ").append(
						map.getBaseTerrain(point).toXML().replace('_', ' '));
		boolean hasForest = false;
		final Forest forest = map.getForest(point);
		if (forest != null) {
			builder.append(", forested with ").append(forest.getKind());
			hasForest = true;
		}
		if (map.isMountainous(point)) {
			builder.append(", mountainous");
		}
		for (final TileFixture fix : map.getOtherFixtures(point)) {
			if (fix instanceof Forest) {
				if (!hasForest) {
					hasForest = true;
					builder.append(", forested with ").append(
							((Forest) fix).getKind());
				}
				fixtures.remove(Integer.valueOf(fix.getID()));
			} else if (fix instanceof Mountain) {
				builder.append(", mountainous");
				fixtures.remove(Integer.valueOf(fix.getID()));
			} else if (fix instanceof Hill) {
				builder.append(", hilly");
				fixtures.remove(Integer.valueOf(fix.getID()));
			} else if (fix instanceof Oasis) {
				builder.append(", with a nearby oasis");
				fixtures.remove(Integer.valueOf(fix.getID()));
			}
		}
		return NullCleaner.assertNotNull(builder.toString());
	}

	/**
	 * @param rivers a collection of rivers
	 * @return an equivalent string.
	 */
	private static String riversToString(final Collection<River> rivers) {
		final StringBuilder builder = new StringBuilder(64);
		if (rivers.contains(River.Lake)) {
			builder.append("<li>There is a nearby lake.</li>\n");
			rivers.remove(River.Lake);
		}
		if (!rivers.isEmpty()) {
			builder.append(OPEN_LIST_ITEM);
			builder.append("There is a river on the tile, "); // NOPMD
			builder.append("flowing through the following borders: ");
			builder.append(StreamSupport.stream(rivers.spliterator(), false).map(River::getDescription)
					               .collect(Collectors.joining(", ")));
			builder.append(CLOSE_LIST_ITEM);
		}
		return NullCleaner.assertNotNull(builder.toString());
	}
	/**
	 * @param loc where this is
	 * @param parent the node to add nodes describing rivers to
	 * @param rivers the collection of rivers
	 */
	private static void riversToNode(final Point loc,
			final AbstractReportNode parent, final Collection<River> rivers) {
		if (rivers.contains(River.Lake)) {
			parent.add(new SimpleReportNode(loc, "There is a nearby lake."));
			rivers.remove(River.Lake);
		}
		if (!rivers.isEmpty()) {
			parent.add(
					new SimpleReportNode(loc, "There is a river on the tile, flowing through the following borders: ",
							                    StreamSupport.stream(rivers.spliterator(), false)
									                    .map(River::getDescription)
									                    .collect(Collectors.joining(", "))));
		}
	}

	/**
	 * All fixtures referred to in this report are removed from the collection.
	 *
	 * @param item the fortress to report on
	 * @param loc its location
	 * @param fixtures the set of fixtures
	 * @param map the map (needed to get terrain information)
	 * @param currentPlayer the player for whom the report is being produced
	 * @return the part of the report dealing with fortresses
	 */
	@Override
	public String produce(
			final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
			final IMapNG map, final Player currentPlayer,
			final Fortress item, final Point loc) {
		// This can get long. we'll give it 16K.
		final StringBuilder builder = new StringBuilder(16384).append("<h5>Fortress ")
				.append(item.getName()).append(" belonging to ")
				.append(playerNameOrYou(item.getOwner())).append("</h5>\n")
				.append(OPEN_LIST).append(OPEN_LIST_ITEM).append("Located at ")
				.append(loc).append(' ').append(distCalculator.distanceString(loc))
				.append(CLOSE_LIST_ITEM).append(OPEN_LIST_ITEM);
		builder.append(getTerrain(map, loc, fixtures)).append(CLOSE_LIST_ITEM);
		if (map.getRivers(loc).iterator().hasNext()) {
			builder.append(riversToString(StreamSupport.stream(map.getRivers(loc).spliterator(), false).collect(
					Collectors.toSet())));
		}
		if (item.iterator().hasNext()) {
			builder.append(OPEN_LIST_ITEM).append("Units on the tile:\n")
			.append(OPEN_LIST);
			final Collection<FortressMember> contents = new ArrayList<>();
			for (final FortressMember member : item) {
				if (member instanceof Unit) {
					builder.append(OPEN_LIST_ITEM)
					.append(urg.produce(fixtures, map, currentPlayer,
							(Unit) member, loc)).append(CLOSE_LIST_ITEM);
				} else {
					contents.add(member);
				}
			}
			builder.append(CLOSE_LIST).append(CLOSE_LIST_ITEM);
			if (!contents.isEmpty()) {
				builder.append(OPEN_LIST_ITEM)
				.append("Other fortress contents:\n").append(OPEN_LIST);
				for (final FortressMember member : contents) {
					// FIXME: Produce and append the proper sub-report
				}
				builder.append(CLOSE_LIST).append(CLOSE_LIST_ITEM);
			}
		}
		builder.append(CLOSE_LIST);
		fixtures.remove(Integer.valueOf(item.getID()));
		return NullCleaner.assertNotNull(builder.toString());
	}

	/**
	 * All fixtures referred to in this report are removed from the collection.
	 *
	 * @param item the fortress to report on
	 * @param loc its location
	 * @param fixtures the set of fixtures
	 * @param map the map (needed to get terrain information)
	 * @param currentPlayer the player for whom the report is being produced
	 * @return the part of the report dealing with the fortress
	 */
	@Override
	public SectionListReportNode produceRIR(
			final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
			final IMapNG map, final Player currentPlayer,
			final Fortress item, final Point loc) {
		final SectionListReportNode retval = new SectionListReportNode(loc, 5,
				concat("Fortress ", item.getName(), " belonging to ",
						playerNameOrYou(item.getOwner())));
		retval.add(new SimpleReportNode(loc, "Located at ", loc.toString(), " ",
				distCalculator.distanceString(loc)));
		retval.add(new SimpleReportNode(loc, getTerrain(map, loc, fixtures)));
		if (map.getRivers(loc).iterator().hasNext()) {
			riversToNode(loc, retval,
					StreamSupport.stream(map.getRivers(loc).spliterator(), false).collect(Collectors.toSet()));
		}
		if (item.iterator().hasNext()) {
			final AbstractReportNode units = new ListReportNode(loc,
					"Units on the tile:");
			final MutableTreeNode contents =
					new ListReportNode(loc, "Other Contents of Fortress:");
			for (final FortressMember unit : item) {
				if (unit instanceof Unit) {
					units.add(urg.produceRIR(fixtures, map, currentPlayer,
							(Unit) unit, loc));
				} else {
					// FIXME: Produce the sub-report using the proper generator.
				}
			}
			if (units.getChildCount() != 0) {
				retval.add(units);
			}
			if (contents.getChildCount() != 0) {
				retval.add(contents);
			}
		}
		fixtures.remove(Integer.valueOf(item.getID()));
		return retval;
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "FortressReportGenerator";
	}
}
