package controller.map.report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import model.map.IFixture;
import model.map.IMapNG;
import model.map.Player;
import model.map.Point;
import model.map.fixtures.resources.CacheFixture;
import model.map.fixtures.resources.Grove;
import model.map.fixtures.resources.HarvestableFixture;
import model.map.fixtures.resources.Meadow;
import model.map.fixtures.resources.Mine;
import model.map.fixtures.resources.MineralVein;
import model.map.fixtures.resources.Shrub;
import model.map.fixtures.resources.StoneDeposit;
import model.report.EmptyReportNode;
import model.report.IReportNode;
import model.report.ListReportNode;
import model.report.SectionReportNode;
import model.report.SimpleReportNode;
import model.report.SortedSectionListReportNode;
import org.eclipse.jdt.annotation.NonNull;
import util.DelayedRemovalMap;
import util.Pair;

/**
 * A report generator for harvestable fixtures (other than caves and battlefields, which
 * aren't really).
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2015 Jonathan Lovelace
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
public final class HarvestableReportGenerator
		extends AbstractReportGenerator<HarvestableFixture> {
	/**
	 * @param comparator a comparator for pairs of Points and fixtures.
	 */
	public HarvestableReportGenerator(final Comparator<@NonNull Pair<@NonNull Point,
																			@NonNull
																					IFixture>> comparator) {
		super(comparator);
	}

	/**
	 * Produce the sub-reports dealing with "harvestable" fixtures. All fixtures referred
	 * to in this report are to be removed from the collection. Caves and battlefields,
	 * though HarvestableFixtures, are presumed to have been handled already.
	 *
	 * @param fixtures the set of fixtures
	 * @param map      ignored
	 * @param player   the player for whom the report is being produced
	 * @return the part of the report listing things that can be harvested.
	 */
	@Override
	public String produce(// $codepro.audit.disable cyclomaticComplexity
						  final DelayedRemovalMap<Integer, Pair<Point, IFixture>>
								  fixtures,
						  //NOPMD
						  final IMapNG map, final Player player) {
		// TODO: Use Guava Multimaps to reduce cyclomatic complexity
		final List<Pair<Point, IFixture>> values = new ArrayList<>(fixtures.values());
		Collections.sort(values, pairComparator);
		final HeadedList<String> stone = new HtmlList(
															 "<h5>Exposed stone " +
																	 "deposits</h5>");
		final Map<String, List<Point>> shrubs = new HashMap<>();
		final HeadedList<String> minerals = new HtmlList(
																"<h5>Mineral " +
																		"deposits</h5>");
		final HeadedList<String> mines = new HtmlList("<h5>Mines</h5>");
		final HeadedList<String> meadows = new HtmlList(
															   "<h5>Meadows and " +
																	   "fields</h5>");
		final HeadedList<String> groves = new HtmlList(
															  "<h5>Groves and " +
																	  "orchards</h5>");
		final HeadedList<String> caches = new HtmlList(
															  "<h5>Caches collected by " +
																	  "your explorers " +
																	  "and " +
																	  "workers:</h5>");
		for (final Pair<Point, IFixture> pair : values) {
			final IFixture item = pair.second();
			final Point point = pair.first();
			if (item instanceof CacheFixture) {
				caches.add(produce(fixtures, map, player,
						(CacheFixture) item, point));
			} else if (item instanceof Grove) {
				groves.add(produce(fixtures, map, player, (Grove) item, point));
			} else if (item instanceof Meadow) {
				meadows.add(produce(fixtures, map, player, (Meadow) item,
						point));
			} else if (item instanceof Mine) {
				mines.add(produce(fixtures, map, player, (Mine) item, point));
			} else if (item instanceof MineralVein) {
				// TODO: Handle these like shrubs.
				minerals.add(produce(fixtures, map, player,
						(MineralVein) item, point));
			} else if (item instanceof Shrub) {
				// TODO: Use a Guava Multimap
				final List<Point> shrubPoints; // NOPMD
				if (shrubs.containsKey(((Shrub) item).getKind())) {
					shrubPoints = shrubs.get(((Shrub) item).getKind());
				} else {
					shrubPoints = new ArrayList<>(); // NOPMD
					shrubs.put(((Shrub) item).getKind(), shrubPoints);
				}
				shrubPoints.add(point);
				fixtures.remove(Integer.valueOf(item.getID()));
			} else if (item instanceof StoneDeposit) {
				// TODO: Handle these like shrubs.
				stone.add(produce(fixtures, map, player, (StoneDeposit) item,
						point));
			}
		}
		final HeadedList<String> shrubsText = new HtmlList(
																  "<h5>Shrubs, small " +
																		  "trees, and " +
																		  "such</h5>");
		shrubsText.addAll(shrubs.entrySet().stream()
								  .map(entry -> {
									  List<Point> lst = entry.getValue();
									  StringBuilder builder = new StringBuilder(lst.size() * 10);
									  pointCSL(builder, lst);
									  return concat(entry.getKey(), ": at ",
											  builder.toString());
								  })
								  .collect(Collectors.toList()));
		sortAll(caches, groves, meadows, mines, minerals, stone, shrubsText);
		if (caches.isEmpty() && groves.isEmpty() && meadows.isEmpty()
					&& mines.isEmpty() && minerals.isEmpty() && stone.isEmpty()
					&& shrubs.isEmpty()) {
			return ""; // NOPMD
		} else {
			return concat("<h4>Resource Sources</h4>\n", caches.toString(),
					groves.toString(), meadows.toString(), mines.toString(),
					minerals.toString(), stone.toString(),
					shrubsText.toString());
		}
	}

	/**
	 * @param collections a series of lists to be sorted
	 */
	@SafeVarargs
	private static void sortAll(final List<String>... collections) {
		Stream.of(collections).forEach(Collections::sort);
	}

	/**
	 * Produce the sub-reports dealing with "harvestable" fixtures. All fixtures referred
	 * to in this report are to be removed from the collection.
	 *
	 * @param fixtures the set of fixtures
	 * @param map      ignored
	 * @param player   the player for whom the report is being produced
	 * @return the part of the report listing things that can be harvested.
	 */
	@Override
	public IReportNode produceRIR(
												final DelayedRemovalMap<Integer,
																			   Pair<Point, IFixture>> fixtures,
												final IMapNG map, final Player player) {
		//  TODO: Use Guava Multimaps to reduce cyclomatic complexity
		final List<Pair<Point, IFixture>> values = new ArrayList<>(fixtures.values());
		Collections.sort(values, pairComparator);
		final IReportNode stone =
				new SortedSectionListReportNode(5, "Exposed stone deposits");
		final Map<String, IReportNode> shrubs = new HashMap<>();
		final IReportNode minerals =
				new SortedSectionListReportNode(5, "Mineral deposits");
		final IReportNode mines = new SortedSectionListReportNode(5, "Mines");
		final IReportNode meadows =
				new SortedSectionListReportNode(5, "Meadows and fields");
		final IReportNode groves =
				new SortedSectionListReportNode(5, "Groves and orchards");
		final IReportNode caches = new SortedSectionListReportNode(5,
																				 "Caches collected by your explorers and workers:");
		for (final Pair<Point, IFixture> pair : values) {
			if (pair.second() instanceof HarvestableFixture) {
				final HarvestableFixture item = (HarvestableFixture) pair.second();
				final Point loc = pair.first();
				if (item instanceof CacheFixture) {
					caches.add(produceRIR(fixtures, map, player, item, loc));
				} else if (item instanceof Grove) {
					groves.add(produceRIR(fixtures, map, player, item, loc));
				} else if (item instanceof Meadow) {
					meadows.add(produceRIR(fixtures, map, player, item, loc));
				} else if (item instanceof Mine) {
					mines.add(produceRIR(fixtures, map, player, item, loc));
				} else if (item instanceof MineralVein) {
					// TODO: Handle these like shrubs.
					minerals.add(produceRIR(fixtures, map, player, item,
							loc));
				} else if (item instanceof Shrub) {
					final IReportNode collection; // NOPMD
					if (shrubs.containsKey(((Shrub) item).getKind())) {
						collection = shrubs.get(((Shrub) item).getKind());
					} else {
						collection = new ListReportNode(((Shrub) item).getKind());
						shrubs.put(((Shrub) item).getKind(), collection);
					}
					collection.add(produceRIR(fixtures, map, player, item, loc));
					fixtures.remove(Integer.valueOf(item.getID()));
				} else if (item instanceof StoneDeposit) {
					// TODO: Handle these like shrubs.
					stone.add(produceRIR(fixtures, map, player, item, loc));
				}
			}
		}
		final IReportNode shrubsNode =
				new SortedSectionListReportNode(5, "Shrubs, small trees, and such");
		for (final Entry<String, IReportNode> entry : shrubs.entrySet()) {
			shrubsNode.add(entry.getValue());
		}
		final SectionReportNode retval = new SectionReportNode(4, "Resource Sources");
		if (maybeAdd(retval, caches, groves, meadows, mines, minerals, stone,
				shrubsNode)) {
			return retval; // NOPMD
		} else {
			return EmptyReportNode.NULL_NODE;
		}
	}

	/**
	 * @param parent   a parent node
	 * @param children nodes to add iff they have children of their own
	 * @return whether any of them was added
	 */
	public static boolean maybeAdd(final DefaultMutableTreeNode parent,
								   final MutableTreeNode... children) {
		boolean retval = false;
		for (final MutableTreeNode child : children) {
			if (child.getChildCount() != 0) {
				parent.add(child);
				retval = true; // NOPMD
			}
		}
		return retval;
	}

	/**
	 * Produce the sub-sub-report dealing with a harvestable fixture.
	 *
	 * @param fixtures      the set of fixtures
	 * @param map           ignored
	 * @param item          the fixture to report on
	 * @param loc           its location
	 * @param currentPlayer the player for whom the report is being produced
	 * @return a sub-report dealing with the fixture
	 */
	@Override
	public String produce(
								 final DelayedRemovalMap<Integer, Pair<Point, IFixture>>
										 fixtures,
								 final IMapNG map, final Player currentPlayer,
								 final HarvestableFixture item, final Point loc) {
		if (item instanceof CacheFixture) {
			fixtures.remove(Integer.valueOf(item.getID()));
			return concat(atPoint(loc), " ", distCalculator.distanceString(loc),
					"A cache of ", // NOPMD
					((CacheFixture) item).getKind(), ", containing ",
					((CacheFixture) item).getContents());
		} else if (item instanceof Grove) {
			fixtures.remove(Integer.valueOf(item.getID()));
			return concat(//NOPMD
					atPoint(loc),
					"A ",
					ternary(((Grove) item).isCultivated(), "cultivated ",
							"wild "), ((Grove) item).getKind(),
					ternary(((Grove) item).isOrchard(), " orchard", " grove"), " ",
					distCalculator.distanceString(loc));
		} else if (item instanceof Meadow) {
			fixtures.remove(Integer.valueOf(item.getID()));
			return concat(// NOPMD
					atPoint(loc),
					"A ",
					((Meadow) item).getStatus().toString(),
					ternary(((Meadow) item).isCultivated(), " cultivated ",
							" wild or abandoned "), ((Meadow) item).getKind(),
					ternary(((Meadow) item).isField(), " field", " meadow"), " ",
					distCalculator.distanceString(loc));
		} else if (item instanceof Mine) {
			fixtures.remove(Integer.valueOf(item.getID()));
			return concat(atPoint(loc), item.toString(), " ",
					distCalculator.distanceString(loc)); // NOPMD
		} else if (item instanceof MineralVein) {
			fixtures.remove(Integer.valueOf(item.getID()));
			return concat(// NOPMD
					atPoint(loc),
					"An ",
					ternary(((MineralVein) item).isExposed(), "exposed ",
							"unexposed "), "vein of ",
					((MineralVein) item).getKind(), " ",
					distCalculator.distanceString(loc));
		} else if (item instanceof Shrub) {
			fixtures.remove(Integer.valueOf(item.getID()));
			return concat(atPoint(loc), ((Shrub) item).getKind(), " ",
					distCalculator.distanceString(loc)); // NOPMD
		} else if (item instanceof StoneDeposit) {
			fixtures.remove(Integer.valueOf(item.getID()));
			return concat(atPoint(loc), "An exposed ", // NOPMD
					((StoneDeposit) item).getKind(), " deposit",
					distCalculator.distanceString(loc));
		} else {
			throw new IllegalArgumentException("Unexpected HarvestableFixture type");
		}
	}

	/**
	 * Produce the sub-sub-report dealing with a harvestable fixture.
	 *
	 * @param fixtures      the set of fixtures
	 * @param map           ignored
	 * @param item          the fixture to report on
	 * @param loc           its location
	 * @param currentPlayer the player for whom the report is being produced
	 * @return a sub-report dealing with the fixture
	 */
	@Override
	public SimpleReportNode produceRIR(
											  final DelayedRemovalMap<Integer,
																			 Pair<Point,
																						 IFixture>> fixtures,
											  final IMapNG map,
											  final Player currentPlayer,
											  final HarvestableFixture item,
											  final Point loc) {
		if (item instanceof CacheFixture) {
			fixtures.remove(Integer.valueOf(item.getID()));
			return new SimpleReportNode(loc, atPoint(loc), " ",
											   distCalculator.distanceString(loc),
											   " A cache of ", // NOPMD
											   ((CacheFixture) item).getKind(),
											   ", containing ",
											   ((CacheFixture) item).getContents());
		} else if (item instanceof Grove) {
			fixtures.remove(Integer.valueOf(item.getID()));
			return new SimpleReportNode(loc, atPoint(loc), "A ",
											   ternary(// NOPMD
													   ((Grove) item).isCultivated(),
													   "cultivated ", "wild "),
											   ((Grove) item).getKind(),
											   ternary(((Grove) item).isOrchard(),
													   " orchard", " grove"), " ",
											   distCalculator.distanceString(loc));
		} else if (item instanceof Meadow) {
			fixtures.remove(Integer.valueOf(item.getID()));
			return new SimpleReportNode(loc, atPoint(loc), "A ",
											   ((Meadow) item).getStatus().toString(),
											   ternary(((Meadow) item).isCultivated(),
													   " cultivated ",
													   " wild or abandoned "),
											   ((Meadow) item).getKind(),
											   ternary(((Meadow) item).isField(),
													   " field", " meadow"), " ",
											   distCalculator.distanceString(loc));
		} else if (item instanceof Mine) {
			fixtures.remove(Integer.valueOf(item.getID()));
			return new SimpleReportNode(loc, atPoint(loc), item.toString(), " ",
											   distCalculator
													   .distanceString(loc)); //NOPMD
		} else if (item instanceof MineralVein) {
			fixtures.remove(Integer.valueOf(item.getID()));
			return new SimpleReportNode(loc, atPoint(loc), "An ", // NOPMD
											   ternary(((MineralVein) item).isExposed(),
													   "exposed ",
													   "unexposed "), "vein of ",
											   ((MineralVein) item).getKind(), " ",
											   distCalculator.distanceString(loc));
		} else if (item instanceof Shrub) {
			fixtures.remove(Integer.valueOf(item.getID()));
			final String kind = ((Shrub) item).getKind();
			return new SimpleReportNode(loc, atPoint(loc), kind, " ",
											   distCalculator
													   .distanceString(loc)); // NOPMD
		} else if (item instanceof StoneDeposit) {
			fixtures.remove(Integer.valueOf(item.getID()));
			return new SimpleReportNode(loc, atPoint(loc), "An exposed ", // NOPMD
											   ((StoneDeposit) item).getKind(),
											   " deposit", " ",
											   distCalculator.distanceString(loc));
		} else {
			throw new IllegalArgumentException("Unexpected HarvestableFixture type");
		}
	}

	/**
	 * We need this to reduce the calculated complexity.
	 *
	 * @param condition a Boolean
	 * @param first     what to return if true
	 * @param second    what to return if false
	 * @return the result of the ternary operator.
	 */
	private static String ternary(final boolean condition, final String first,
								  final String second) {
		if (condition) {
			return first; // NOPMD
		} else {
			return second;
		}
	}

	/**
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "HarvestableReportGenerator";
	}
}
