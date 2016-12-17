package controller.map.report;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
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
import util.LineEnd;
import util.MultiMapHelper;
import util.Pair;
import util.PatientMap;

import static util.Ternary.ternary;

/**
 * A report generator for harvestable fixtures (other than caves and battlefields, which
 * aren't really).
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
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
	 * @param collections a series of lists to be sorted
	 */
	@SafeVarargs
	private static void sortAll(final List<String>... collections) {
		Stream.of(collections).map(ArrayList::new).forEach(Collections::sort);
	}

	/**
	 * Produce the sub-reports dealing with "harvestable" fixtures. All fixtures referred
	 * to in this report are to be removed from the collection. Caves and battlefields,
	 * though HarvestableFixtures, are presumed to have been handled already.
	 *
	 * @param fixtures      the set of fixtures
	 * @param map           ignored
	 * @param currentPlayer the player for whom the report is being produced
	 * @return the part of the report listing things that can be harvested.
	 */
	@Override
	public String produce(final PatientMap<Integer, Pair<Point, IFixture>> fixtures,
						  final IMapNG map, final Player currentPlayer) {
		final List<Pair<Point, IFixture>> values = new ArrayList<>(fixtures.values());
		values.sort(pairComparator);
		final Map<String, Collection<Point>> stone = new HashMap<>();
		final Map<String, Collection<Point>> shrubs = new HashMap<>();
		final Map<String, Collection<Point>> minerals = new HashMap<>();
		final HeadedList<String> mines = new HtmlList("<h5>Mines</h5>");
		final HeadedList<String> meadows = new HtmlList("<h5>Meadows and fields</h5>");
		final HeadedList<String> groves = new HtmlList("<h5>Groves and orchards</h5>");
		final HeadedList<String> caches =
				new HtmlList("<h5>Caches collected by your explorers and workers:</h5>");
		for (final Pair<Point, IFixture> pair : values) {
			final IFixture item = pair.second();
			final Point point = pair.first();
			if (item instanceof CacheFixture) {
				caches.add(produce(fixtures, map, currentPlayer,
						(CacheFixture) item, point));
			} else if (item instanceof Grove) {
				groves.add(produce(fixtures, map, currentPlayer, (Grove) item, point));
			} else if (item instanceof Meadow) {
				meadows.add(produce(fixtures, map, currentPlayer, (Meadow) item,
						point));
			} else if (item instanceof Mine) {
				mines.add(produce(fixtures, map, currentPlayer, (Mine) item, point));
			} else if (item instanceof MineralVein) {
				final MineralVein mineral = (MineralVein) item;
				final String kind =
						ternary(mineral.isExposed(), "exposed ", "unexposed ") +
								mineral.getKind();
				MultiMapHelper.getMapValue(minerals, kind,
						AbstractReportGenerator::pointsListAt).add(point);
				fixtures.remove(Integer.valueOf(item.getID()));
			} else if (item instanceof Shrub) {
				MultiMapHelper
						.getMapValue(shrubs, ((Shrub) item).getKind(),
								AbstractReportGenerator::pointsListAt).add(point);
				fixtures.remove(Integer.valueOf(item.getID()));
			} else if (item instanceof StoneDeposit) {
				MultiMapHelper
						.getMapValue(stone, ((StoneDeposit) item).getKind(),
								AbstractReportGenerator::pointsListAt).add(point);
				fixtures.remove(Integer.valueOf(item.getID()));
			}
		}
		final HeadedList<String> shrubsText =
				mapToList(shrubs, "<h5>Shrubs, small trees, and such</h4>");
		final HeadedList<String> mineralsText =
				mapToList(minerals, "<h5>Mineral deposits</h5>");
		final HeadedList<String> stoneText =
				mapToList(stone, "<h5>Exposed stone deposits</h5>");
		sortAll(caches, groves, meadows, mines, mineralsText, stoneText, shrubsText);
		if (Stream.of(caches, groves, meadows, mines, mineralsText, stoneText,
				shrubsText).allMatch(Collection::isEmpty)) {
			return "";
		} else {
			return concat("<h4>Resource Sources</h4>", LineEnd.LINE_SEP,
					caches.toString(),
					groves.toString(), meadows.toString(), mines.toString(),
					mineralsText.toString(), stoneText.toString(),
					shrubsText.toString());
		}
	}
	/**
	 * @param map a map from kinds to HeadedLists of locations of those kinds
	 * @param heading what to title the returned list
	 * @return a HeadedList of those kinds and locations
	 */
	private static HeadedList<String> mapToList(final Map<String, Collection<Point>> map,
												final String heading) {
		return map.values().stream().map(Collection::toString)
					   .collect(() -> new HtmlList(heading), HtmlList::add,
							   HtmlList::addAll);
	}
	/**
	 * Produce the sub-reports dealing with "harvestable" fixtures. All fixtures referred
	 * to in this report are to be removed from the collection.
	 *
	 * @param fixtures      the set of fixtures
	 * @param map           ignored
	 * @param currentPlayer the player for whom the report is being produced
	 * @return the part of the report listing things that can be harvested.
	 */
	@Override
	public IReportNode produceRIR(final PatientMap<Integer, Pair<Point, IFixture>>
										  fixtures,
								  final IMapNG map, final Player currentPlayer) {
		//  TODO: Use Guava MultiMaps to reduce cyclomatic complexity
		final List<Pair<Point, IFixture>> values = new ArrayList<>(fixtures.values());
		values.sort(pairComparator);
		final Map<String, IReportNode> stone = new HashMap<>();
		final Map<String, IReportNode> shrubs = new HashMap<>();
		final Map<String, IReportNode> minerals = new HashMap<>();
		final IReportNode mines = new SortedSectionListReportNode(5, "Mines");
		final IReportNode meadows =
				new SortedSectionListReportNode(5, "Meadows and fields");
		final IReportNode groves =
				new SortedSectionListReportNode(5, "Groves and orchards");
		final IReportNode caches = new SortedSectionListReportNode(
				5, "Caches collected by your explorers and workers:");
		for (final Pair<Point, IFixture> pair : values) {
			if (pair.second() instanceof HarvestableFixture) {
				final HarvestableFixture item = (HarvestableFixture) pair.second();
				final Point loc = pair.first();
				if (item instanceof CacheFixture) {
					caches.add(produceRIR(fixtures, map, currentPlayer, item, loc));
				} else if (item instanceof Grove) {
					groves.add(produceRIR(fixtures, map, currentPlayer, item, loc));
				} else if (item instanceof Meadow) {
					meadows.add(produceRIR(fixtures, map, currentPlayer, item, loc));
				} else if (item instanceof Mine) {
					mines.add(produceRIR(fixtures, map, currentPlayer, item, loc));
				} else if (item instanceof MineralVein) {
					final MineralVein mineral = (MineralVein) item;
					final String kind =
							ternary(mineral.isExposed(), "exposed ",
									"unexposed ") +
									mineral.getKind();
					MultiMapHelper
							.getMapValue(minerals, kind, ListReportNode::new)
							.add(produceRIR(fixtures, map, currentPlayer, item, loc));
				} else if (item instanceof Shrub) {
					MultiMapHelper
							.getMapValue(shrubs, ((Shrub) item).getKind(),
									ListReportNode::new)
							.add(produceRIR(fixtures, map, currentPlayer, item, loc));
					fixtures.remove(Integer.valueOf(item.getID()));
				} else if (item instanceof StoneDeposit) {
					MultiMapHelper
							.getMapValue(stone, ((StoneDeposit) item).getKind(),
									ListReportNode::new)
							.add(produceRIR(fixtures, map, currentPlayer, item, loc));
				}
			}
		}
		final IReportNode shrubsNode =
				new SortedSectionListReportNode(5, "Shrubs, small trees, and such");
		shrubs.values().forEach(shrubsNode::add);
		final IReportNode mineralsNode =
				new SortedSectionListReportNode(5, "Mineral deposits");
		minerals.values().forEach(mineralsNode::add);
		final IReportNode stoneNode =
				new SortedSectionListReportNode(5, "Exposed stone deposits");
		stone.values().forEach(stoneNode::add);
		final SectionReportNode retval = new SectionReportNode(4, "Resource Sources");
		retval.addIfNonEmpty(caches, groves, meadows, mines, mineralsNode, stoneNode,
				shrubsNode);
		if (retval.getChildCount() != 0) {
			return retval;
		} else {
			return EmptyReportNode.NULL_NODE;
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
	public String produce(final PatientMap<Integer, Pair<Point, IFixture>> fixtures,
						  final IMapNG map, final Player currentPlayer,
						  final HarvestableFixture item, final Point loc) {
		final String retval;
		if (item instanceof CacheFixture) {
			final CacheFixture cache = (CacheFixture) item;
			retval = concat(atPoint(loc), " ", distCalculator.distanceString(loc),
					"A cache of ",
					cache.getKind(), ", containing ",
					cache.getContents());
		} else if (item instanceof Grove) {
			final Grove grove = (Grove) item;
			retval = concat(
					atPoint(loc),
					"A ",
					ternary(grove.isCultivated(), "cultivated ", "wild "),
					grove.getKind(),
					ternary(grove.isOrchard(), " orchard", " grove"), " ",
					distCalculator.distanceString(loc));
		} else if (item instanceof Meadow) {
			final Meadow meadow = (Meadow) item;
			retval = concat(
					atPoint(loc),
					"A ",
					meadow.getStatus().toString(),
					ternary(meadow.isCultivated(), " cultivated ",
							" wild or abandoned "), meadow.getKind(),
					ternary(meadow.isField(), " field", " meadow"), " ",
					distCalculator.distanceString(loc));
		} else if (item instanceof Mine) {
			retval = concat(atPoint(loc), item.toString(), " ",
					distCalculator.distanceString(loc));
		} else if (item instanceof MineralVein) {
			final MineralVein mineral = (MineralVein) item;
			retval = concat(
					atPoint(loc),
					"An ",
					ternary(mineral.isExposed(), "exposed ", "unexposed "), "vein of ",
					mineral.getKind(), " ",
					distCalculator.distanceString(loc));
		} else if (item instanceof Shrub) {
			retval = concat(atPoint(loc), ((Shrub) item).getKind(), " ",
					distCalculator.distanceString(loc));
		} else if (item instanceof StoneDeposit) {
			retval = concat(atPoint(loc), "An exposed ",
					((StoneDeposit) item).getKind(), " deposit",
					distCalculator.distanceString(loc));
		} else {
			throw new IllegalArgumentException("Unexpected HarvestableFixture type");
		}
		fixtures.remove(Integer.valueOf(item.getID()));
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
	public IReportNode produceRIR(final PatientMap<Integer, Pair<Point, IFixture>>
											   fixtures,
									   final IMapNG map, final Player currentPlayer,
									   final HarvestableFixture item, final Point loc) {
		final SimpleReportNode retval;
		if (item instanceof CacheFixture) {
			final CacheFixture cache = (CacheFixture) item;
			retval = new SimpleReportNode(loc, atPoint(loc), " ",
											   distCalculator.distanceString(loc),
											   " A cache of ",
											   cache.getKind(),
											   ", containing ",
											   cache.getContents());
		} else if (item instanceof Grove) {
			final Grove grove = (Grove) item;
			retval = new SimpleReportNode(loc, atPoint(loc), "A ",
												 ternary(grove.isCultivated(),
														 "cultivated ", "wild "),
											   grove.getKind(),
												 ternary(grove.isOrchard(),
														 " orchard", " grove"), " ",
											   distCalculator.distanceString(loc));
		} else if (item instanceof Meadow) {
			final Meadow meadow = (Meadow) item;
			retval = new SimpleReportNode(loc, atPoint(loc), "A ",
											   meadow.getStatus().toString(),
												 ternary(meadow.isCultivated(),
														 " cultivated ",
														 " wild or abandoned "),
											   meadow.getKind(),
												 ternary(meadow.isField(),
														 " field", " meadow"), " ",
											   distCalculator.distanceString(loc));
		} else if (item instanceof Mine) {
			retval = new SimpleReportNode(loc, atPoint(loc), item.toString(), " ",
											   distCalculator
													   .distanceString(loc));
		} else if (item instanceof MineralVein) {
			final MineralVein mineral = (MineralVein) item;
			retval = new SimpleReportNode(loc, atPoint(loc), "An ",
												 ternary(mineral.isExposed(), "exposed ",
														 "unexposed "), "vein of ",
											   mineral.getKind(), " ",
											   distCalculator.distanceString(loc));
		} else if (item instanceof Shrub) {
			final String kind = ((Shrub) item).getKind();
			retval = new SimpleReportNode(loc, atPoint(loc), kind, " ",
											   distCalculator
													   .distanceString(loc));
		} else if (item instanceof StoneDeposit) {
			retval = new SimpleReportNode(loc, atPoint(loc), "An exposed ",
											   ((StoneDeposit) item).getKind(),
											   " deposit", " ",
											   distCalculator.distanceString(loc));
		} else {
			throw new IllegalArgumentException("Unexpected HarvestableFixture type");
		}
		fixtures.remove(Integer.valueOf(item.getID()));
		return retval;
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
