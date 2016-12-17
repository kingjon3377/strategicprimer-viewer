package controller.map.report;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
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
import util.LineEnd;
import util.Pair;
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
	 * @param comparator a comparator for pairs of Points and fixtures.
	 */
	public ExplorableReportGenerator(final Comparator<@NonNull Pair<@NonNull Point,
																		   @NonNull
																				   IFixture>> comparator) {
		super(comparator);
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
	public String produce(final PatientMap<Integer, Pair<Point, IFixture>> fixtures,
						  final IMapNG map, final Player currentPlayer) {
		// At only three (albeit potentially rather long) list items, I doubt this
		// will ever be over one K ... but we'll give it two just in case.
		final StringBuilder builder = new StringBuilder(2048).append(
				"<h4>Caves, Battlefields, and Portals</h4>").append(LineEnd.LINE_SEP)
											  .append(OPEN_LIST);
		final Collection<Point> caves =
				new PointList("Caves beneath the following tiles: ");
		final Collection<Point> battles =
				new PointList("Signs of long-ago battles on the following tiles: ");
		final Collection<Point> portals = new PointList("Portals to other worlds: ");
		// Similarly, I doubt either this will ever be over half a K, but
		// we'll give it a whole K just in case.
		final StringBuilder adventureBuilder = new StringBuilder(1024)
													   .append("<h4>Possible " +
																	   "Adventures</h4>")
													   .append(OPEN_LIST);
		final List<Pair<Point, IFixture>> values = new ArrayList<>(fixtures.values());
		values.sort(pairComparator);
		boolean anyAdventures = false;
		for (final Pair<Point, IFixture> pair : values) {
			if (pair.second() instanceof Cave) {
				caves.add(pair.first());
				fixtures.remove(Integer.valueOf(pair.second().getID()));
			} else if (pair.second() instanceof Battlefield) {
				battles.add(pair.first());
				fixtures.remove(Integer.valueOf(pair.second().getID()));
			} else if (pair.second() instanceof AdventureFixture) {
				anyAdventures = true;
				adventureBuilder.append(OPEN_LIST_ITEM)
						.append(produce(fixtures, map, currentPlayer,
								(ExplorableFixture) pair.second(),
								pair.first()))
						.append(CLOSE_LIST_ITEM);
				fixtures.remove(Integer.valueOf(pair.second().getID()));
			} else if (pair.second() instanceof Portal) {
				portals.add(pair.first());
				fixtures.remove(Integer.valueOf(pair.second().getID()));
			}
		}
		builder.append(caves);
		builder.append(battles);
		builder.append(portals);
		adventureBuilder.append(CLOSE_LIST);
		builder.append(CLOSE_LIST);
		if (caves.isEmpty() && battles.isEmpty() && portals.isEmpty()) {
			if (anyAdventures) {
				return adventureBuilder.toString();
			} else {
				return "";
			}
		} else {
			if (anyAdventures) {
				builder.append(adventureBuilder);
			}
			return builder.toString();
		}
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
		final IReportNode portals = new ListReportNode("Portals");
		final IReportNode battles = new ListReportNode("Battlefields");
		final IReportNode caves = new ListReportNode("Caves");
		final IReportNode adventures =
				new SectionListReportNode(4, "Possible Adventures");
		for (final Pair<Point, IFixture> pair : values) {
			if (pair.second() instanceof Cave) {
				caves.add(produceRIR(fixtures, map, currentPlayer,
						(ExplorableFixture) pair.second(), pair.first()));
			} else if (pair.second() instanceof Battlefield) {
				battles.add(produceRIR(fixtures, map, currentPlayer,
						(ExplorableFixture) pair.second(), pair.first()));
			} else if (pair.second() instanceof AdventureFixture) {
				adventures.add(produceRIR(fixtures, map, currentPlayer,
						(ExplorableFixture) pair.second(), pair.first()));
			} else if (pair.second() instanceof Portal) {
				portals.add(produceRIR(fixtures, map, currentPlayer,
						(ExplorableFixture) pair.second(), pair.first()));
			}
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
	 * Produces a more verbose sub-report on a cave or battlefield.
	 *
	 * @param fixtures      the set of fixtures.
	 * @param map           ignored
	 * @param item          the item to report on
	 * @param loc           its location
	 * @param currentPlayer the player for whom the report is being produced
	 * @return a sub-report (more verbose than the bulk produce() above reports, for
	 * caves
	 * and battlefields) on the item
	 */
	@Override
	public String produce(final PatientMap<Integer, Pair<Point, IFixture>> fixtures,
						  final IMapNG map, final Player currentPlayer,
						  final ExplorableFixture item, final Point loc) {
		if (item instanceof Cave) {
			fixtures.remove(Integer.valueOf(item.getID()));
			return concat("Caves beneath ", loc.toString(), " ",
					distCalculator.distanceString(loc));
		} else if (item instanceof Battlefield) {
			fixtures.remove(Integer.valueOf(item.getID()));
			return concat("Signs of a long-ago battle on ", loc.toString(), " ",
					distCalculator.distanceString(loc));
		} else if (item instanceof AdventureFixture) {
			final AdventureFixture adventure = (AdventureFixture) item;
			if (adventure.getOwner().isIndependent()) {
				return concat(adventure.getBriefDescription(),
						" at ", loc.toString(), ": ",
						adventure.getFullDescription(), " ",
						distCalculator.distanceString(loc));
			} else if (currentPlayer.equals(adventure.getOwner())) {
				return concat(adventure.getBriefDescription(),
						" at ", loc.toString(), ": ",
						adventure.getFullDescription(), " ",
						distCalculator.distanceString(loc),
						" (already investigated by you)");
			} else {
				return concat(adventure.getBriefDescription(),
						" at ", loc.toString(), ": ",
						adventure.getFullDescription(), " ",
						distCalculator.distanceString(loc),
						" (already investigated by another player)");
			}
		} else if (item instanceof Portal) {
			fixtures.remove(Integer.valueOf(item.getID()));
			return concat("A portal to another world at ", loc.toString(), " ",
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
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "ExplorableReportGenerator";
	}
}
