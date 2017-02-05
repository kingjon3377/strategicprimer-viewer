package controller.map.report;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import model.map.IFixture;
import model.map.IMapNG;
import model.map.Player;
import model.map.Point;
import model.map.fixtures.explorable.AdventureFixture;
import model.map.fixtures.explorable.Battlefield;
import model.map.fixtures.explorable.Cave;
import model.map.fixtures.explorable.ExplorableFixture;
import model.map.fixtures.explorable.Portal;
import model.report.ComplexReportNode;
import model.report.EmptyReportNode;
import model.report.IReportNode;
import model.report.ListReportNode;
import model.report.SectionListReportNode;
import model.report.SimpleReportNode;
import org.eclipse.jdt.annotation.NonNull;
import util.Pair;
import util.PairComparator;
import util.PatientMap;

/**
 * A report generator for caves and battlefields.
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
public final class ExplorableReportGenerator
		extends AbstractReportGenerator<ExplorableFixture> {

	/**
	 * Constructor.
	 * @param comparator a comparator for pairs of Points and fixtures.
	 */
	public ExplorableReportGenerator(final PairComparator<@NonNull Point, @NonNull
																				  IFixture> comparator) {
		super(comparator);
	}

	/**
	 * Produce the sub-report on non-town things that can be explored. All fixtures
	 * referred to in this report are removed from the collection.
	 *
	 * @param fixtures      the set of fixtures
	 * @param map           ignored
	 * @param currentPlayer the player for whom the report is being produced
	 * @param ostream       the Formatter to write to
	 */
	@Override
	public void produce(PatientMap<Integer, Pair<Point, IFixture>> fixtures, IMapNG map,
						Player currentPlayer, final Formatter ostream) {
		final List<Pair<Point, IFixture>> values = new ArrayList<>(fixtures.values());
		values.sort(pairComparator);
		Map<Class<? extends IFixture>, Consumer<Pair<Point, IFixture>>>
				collectors = new HashMap<>();
		final Collection<Point> portals = new PointList("Portals to other worlds: ");
		collectors.put(Portal.class, pair -> portals.add(pair.first()));
		final Collection<Point> battles =
				new PointList("Signs of long-ago battles on the following tiles: ");
		collectors.put(Battlefield.class, pair -> battles.add(pair.first()));
		final Collection<Point> caves =
				new PointList("Caves beneath the following tiles: ");
		collectors.put(Cave.class, pair -> caves.add(pair.first()));
		final HeadedMap<AdventureFixture, Point> adventures =
				new HeadedMapImpl<>("<h4>Possible Adventures</h4>");
		collectors.put(AdventureFixture.class,
				pair -> adventures.put((AdventureFixture) pair.second(), pair.first()));
		for (final Pair<Point, IFixture> pair : values) {
			final Optional<Consumer<Pair<Point, IFixture>>> collector =
			Optional.ofNullable(collectors.get(pair.second().getClass()));
			if (collector.isPresent()) {
				collector.get().accept(pair);
				fixtures.remove(Integer.valueOf(pair.second().getID()));
			}
		}
		if (!caves.isEmpty() || !battles.isEmpty() || !portals.isEmpty()) {
			ostream.format("<h4>Caves, Battlefields, and Portals</h4>%n<ul>%n");
			ostream.format("%s%s%s</ul>%n", caves.toString(), battles.toString(),
					portals.toString());
		}
		writeMap(ostream, adventures,
				(entry, formatter) -> produce(fixtures, map, currentPlayer,
						entry.getKey(), entry.getValue(), formatter));
	}

	/**
	 * Produce the sub-report on non-town things that can be explored. All fixtures
	 * referred to in this report are removed from the collection.
	 *
	 * @param fixtures      the set of fixtures
	 * @param map           ignored
	 * @param currentPlayer the player for whom the report is being produced
	 * @return the part of the report listing things that can be explored.
	 */
	@Override
	public IReportNode produceRIR(final PatientMap<Integer, Pair<Point, IFixture>>
										  fixtures,
								  final IMapNG map, final Player currentPlayer) {
		final List<Pair<Point, IFixture>> values = new ArrayList<>(fixtures.values());
		values.sort(pairComparator);
		final Map<Class<? extends IFixture>, IReportNode> nodes = new HashMap<>();
		final IReportNode portals = new ListReportNode("Portals");
		final IReportNode battles = new ListReportNode("Battlefields");
		final IReportNode caves = new ListReportNode("Caves");
		final IReportNode adventures =
				new SectionListReportNode(4, "Possible Adventures");
		nodes.put(Portal.class, portals);
		nodes.put(Battlefield.class, battles);
		nodes.put(Cave.class, caves);
		nodes.put(AdventureFixture.class, adventures);
		for (final Pair<Point, IFixture> pair : values) {
			Optional.ofNullable(nodes.get(pair.second().getClass())).ifPresent(
					node -> node.add(produceRIR(fixtures, map, currentPlayer,
							(ExplorableFixture) pair.second(), pair.first())));
		}
		final IReportNode retval =
				new SectionListReportNode(4, "Caves, Battlefields, and Portals");
		retval.addIfNonEmpty(caves, battles, portals);
		if (retval.getChildCount() > 0) {
			if (adventures.getChildCount() > 0) {
				final IReportNode real = new ComplexReportNode();
				real.add(retval);
				real.add(adventures);
				return real;
			} else {
				return retval;
			}
		} else if (adventures.getChildCount() > 0) {
			return adventures;
		} else {
			return EmptyReportNode.NULL_NODE;
		}
	}

	/**
	 * Produces a more verbose sub-report on a cave, battlefield, portal, or adventure
	 * hook.
	 *
	 * @param fixtures      the set of fixtures.
	 * @param map           ignored
	 * @param item          the item to report on
	 * @param loc           its location
	 * @param currentPlayer the player for whom the report is being produced
	 * @param ostream	    the Formatter to write to
	 */
	@Override
	public void produce(final PatientMap<Integer, Pair<Point, IFixture>> fixtures,
						final IMapNG map, final Player currentPlayer,
						final ExplorableFixture item, final Point loc, final Formatter ostream) {
		if (item instanceof Cave) {
			fixtures.remove(Integer.valueOf(item.getID()));
			ostream.format("Caves beneath %s %s", loc.toString(),
					distCalculator.distanceString(loc));
		} else if (item instanceof Battlefield) {
			fixtures.remove(Integer.valueOf(item.getID()));
			ostream.format("Signs of a long-ago battle on %s %s", loc.toString(),
					distCalculator.distanceString(loc));
		} else if (item instanceof AdventureFixture) {
			fixtures.remove(Integer.valueOf(item.getID()));
			final AdventureFixture adventure = (AdventureFixture) item;
			ostream.format("%s at %s: %s %s", adventure.getBriefDescription(),
					loc.toString(), adventure.getFullDescription(),
					distCalculator.distanceString(loc));
			if (adventure.getOwner().isIndependent()) {
			} else if (currentPlayer.equals(adventure.getOwner())) {
				ostream.format(" (already investigated by you)");
			} else {
				ostream.format(" (already investigated by another player)");
			}
		} else if (item instanceof Portal) {
			fixtures.remove(Integer.valueOf(item.getID()));
			ostream.format("A portal to another world at %s %s", loc.toString(),
					distCalculator.distanceString(loc));
		} else {
			throw new IllegalArgumentException("Unexpected ExplorableFixture type");
		}
	}

	/**
	 * Produces a more verbose sub-report on a cave or battlefield.
	 *
	 * @param fixtures      the set of fixtures.
	 * @param map           ignored
	 * @param item          the item to report on
	 * @param loc           its location
	 * @param currentPlayer the player for whom the report is being produced
	 * @return a sub-report (more verbose than the bulk produce() above reports) on the
	 * item
	 */
	@Override
	public IReportNode produceRIR(final PatientMap<Integer, Pair<Point, IFixture>>
											   fixtures,
									   final IMapNG map, final Player currentPlayer,
									   final ExplorableFixture item, final Point loc) {
		if (item instanceof Cave) {
			fixtures.remove(Integer.valueOf(item.getID()));
			return new SimpleReportNode(loc, "Caves beneath ", loc.toString(), " ",
											   distCalculator.distanceString(loc));
		} else if (item instanceof Battlefield) {
			fixtures.remove(Integer.valueOf(item.getID()));
			return new SimpleReportNode(loc, "Signs of a long-ago battle on ",
											   loc.toString(), " ",
											   distCalculator.distanceString(loc));
		} else if (item instanceof AdventureFixture) {
			fixtures.remove(Integer.valueOf(item.getID()));
			final AdventureFixture adventure = (AdventureFixture) item;
			if (adventure.getOwner().isIndependent()) {
				return new SimpleReportNode(loc,
												   adventure
														   .getBriefDescription(),
												   " at ",
												   loc.toString(),
												   adventure
														   .getFullDescription(),
												   " ",
												   distCalculator.distanceString(loc));
			} else if (currentPlayer.equals(adventure.getOwner())) {
				return new SimpleReportNode(loc,
												   adventure
														   .getBriefDescription(),
												   " at ",
												   loc.toString(),
												   adventure
														   .getFullDescription(),
												   " ",
												   distCalculator.distanceString(loc),
												   " (already investigated by you)");
			} else {
				return new SimpleReportNode(loc,
												   adventure
														   .getBriefDescription(),
												   " at ",
												   loc.toString(),
												   adventure
														   .getFullDescription(),
												   " ",
												   distCalculator.distanceString(loc),
												   " (already investigated by another " +
														   "player)");
			}
		} else if (item instanceof Portal) {
			fixtures.remove(Integer.valueOf(item.getID()));
			return new SimpleReportNode(loc, "A portal to another world at ",
											   loc.toString(), " ",
											   distCalculator.distanceString(loc));
		} else {
			throw new IllegalArgumentException("Unexpected ExplorableFixture type");
		}
	}

	/**
	 * A trivial toString().
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "ExplorableReportGenerator";
	}
}
