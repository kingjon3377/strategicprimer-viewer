package controller.map.report;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Formatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import model.map.IFixture;
import model.map.IMapNG;
import model.map.Player;
import model.map.Point;
import model.map.fixtures.towns.AbstractTown;
import model.map.fixtures.towns.Fortress;
import model.map.fixtures.towns.ITownFixture;
import model.map.fixtures.towns.TownStatus;
import model.map.fixtures.towns.Village;
import model.report.EmptyReportNode;
import model.report.IReportNode;
import model.report.SectionListReportNode;
import model.report.SimpleReportNode;
import org.eclipse.jdt.annotation.NonNull;
import util.Pair;
import util.PatientMap;

/**
 * A report generator for towns.
 *
 * TODO: Figure out some way to report what was found at any of the towns.
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
public final class TownReportGenerator extends AbstractReportGenerator<ITownFixture> {
	/**
	 * Header for the 'towns' section.
	 */
	@SuppressWarnings("HardcodedFileSeparator")
	private static final String TOWN_HDR =
			"Cities, towns, and/or fortifications you know about:";
	/**
	 * The order of statuses we want to use.
	 */
	private static final List<TownStatus> STATUSES = Collections.unmodifiableList(
			Arrays.asList(TownStatus.Active, TownStatus.Abandoned, TownStatus.Ruined,
					TownStatus.Burned));
	/**
	 * Constructor.
	 * @param comparator a comparator for pairs of Points and fixtures.
	 */
	public TownReportGenerator(final Comparator<@NonNull Pair<@NonNull Point, @NonNull
																					  IFixture>> comparator) {
		super(comparator);
	}

	/**
	 * Produce the part of the report dealing with towns, sorted in a way I hope is
	 * helpful. Note that while this class specifies {@link ITownFixture}, this method
	 * ignores {@link Fortress}es and {@link Village}s. All fixtures referred to in this
	 * report are removed from the collection.
	 *
	 * @param fixtures      the set of fixtures
	 * @param map           ignored
	 * @param currentPlayer the player for whom the report is being produced
	 * @param ostream       the Formatter to write to
	 */
	@Override
	public void produce(PatientMap<Integer, Pair<Point, IFixture>> fixtures, IMapNG map,
						Player currentPlayer, final Formatter ostream) {
		final Map<TownStatus, Collection<String>> separated =
				new EnumMap<>(TownStatus.class);
		separated.put(TownStatus.Abandoned,
				new HtmlList("<h5>Abandoned Communities</h5>"));
		separated.put(TownStatus.Active, new HtmlList("<h5>Active Communities</h5>"));
		separated.put(TownStatus.Burned, new HtmlList("<h5>Burned-Out " +
															  "Communities</h5>"));
		separated.put(TownStatus.Ruined, new HtmlList("<h5>Ruined Communities</h5>"));
		separateByStatus(separated, fixtures.values(), (list, pair) -> list.add(
				produce(fixtures, map, currentPlayer, (ITownFixture) pair.second(),
						pair.first())));
		final Collection<Collection<String>> filtered =
				STATUSES.stream().map(separated::get).filter(Objects::nonNull)
						.filter(coll -> !coll.isEmpty()).collect(Collectors.toList());
		if (!filtered.isEmpty()) {
			ostream.format(
					"<h4>Cities, towns, and/or fortifications you know about:</h4>%n");
			ostream.format("<ul>%n");
			for (final Collection<String> coll : filtered) {
				ostream.format("<li>%s</li>%n", coll.toString());
			}
			ostream.format("</ul>%n");
		}
	}
	/**
	 * Separate towns by status.
	 * @param <T> the type of things stored in the given mapping
	 * @param <U> the type of things our caller wants to return
	 * @param coll the collection of fixtures.
	 * @param mapping the collection of collections to put the products into
	 * @param function the function to produce the products and put them into the
	 */
	private <T, U> void separateByStatus(final Map<TownStatus, T> mapping,
										 final Collection<Pair<Point, IFixture>> coll,
										 final BiConsumer<T, Pair<Point, IFixture>> function) {
		coll.stream().filter(pair -> pair.second() instanceof AbstractTown)
				.sorted(pairComparator).forEach(pair -> function.accept(
				mapping.get(((ITownFixture) pair.second()).status()),
				pair));

	}
	/**
	 * Produce the part of the report dealing with towns. Note that while this class
	 * specifies {@link ITownFixture}, this method ignores {@link Fortress}es and {@link
	 * Village}s. All fixtures referred to in this report are removed from the
	 * collection.
	 *
	 * @param fixtures      the set of fixtures
	 * @param map           ignored
	 * @param currentPlayer the player for whom the report is being produced
	 * @return the part of the report dealing with towns, sorted in a way I hope is
	 * helpful.
	 */
	@Override
	public IReportNode produceRIR(final PatientMap<Integer, Pair<Point, IFixture>>
										  fixtures,
								  final IMapNG map, final Player currentPlayer) {
		final Map<TownStatus, IReportNode> separated = new EnumMap<>(TownStatus.class);
		separated.put(TownStatus.Abandoned,
				new SectionListReportNode(5, "Abandoned Communities"));
		separated.put(TownStatus.Active,
				new SectionListReportNode(5, "Active Communities"));
		separated.put(TownStatus.Burned,
				new SectionListReportNode(5, "Burned-Out Communities"));
		separated.put(TownStatus.Ruined,
				new SectionListReportNode(5, "Ruined Communities"));
		separateByStatus(separated, fixtures.values(), (node, pair) -> node.add(
				produceRIR(fixtures, map, currentPlayer, (ITownFixture) pair.second(),
						pair.first())));
		final IReportNode retval = new SectionListReportNode(4, TOWN_HDR);
		STATUSES.stream().map(separated::get).filter(Objects::nonNull)
				.forEach(retval::addIfNonEmpty);
		if (retval.getChildCount() == 0) {
			return EmptyReportNode.NULL_NODE;
		} else {
			return retval;
		}
	}

	/**
	 * Produce a report for a town. Handling of fortresses and villages is delegated to
	 * their dedicated report-generating classes. We remove the town from the set of
	 * fixtures.
	 *
	 * @param fixtures      the set of fixtures
	 * @param map           ignored
	 * @param item          the town to report on
	 * @param loc           its location
	 * @param currentPlayer the player for whom the report is being produced
	 * @return the sub-report dealing with the town.
	 */
	@Override
	public String produce(final PatientMap<Integer, Pair<Point, IFixture>> fixtures,
						  final IMapNG map, final Player currentPlayer,
						  final ITownFixture item, final Point loc) {
		if (item instanceof Village) {
			return new VillageReportGenerator(pairComparator).produce(fixtures, map,
					currentPlayer, (Village) item, loc);
		} else if (item instanceof Fortress) {
			return new FortressReportGenerator(pairComparator).produce(fixtures, map,
					currentPlayer, (Fortress) item, loc);
		} else if (item instanceof AbstractTown) {
			fixtures.remove(Integer.valueOf(item.getID()));
			if (item.getOwner().isIndependent()) {
				return concat(atPoint(loc), item.getName(), ", an independent ",
						item.size().toString(), " ", item.status().toString(), " ",
						item.kind(), " ", distCalculator.distanceString(loc));
			} else {
				return concat(atPoint(loc), item.getName(), ", a ",
						item.size().toString(), " ", item.status().toString(), " ",
						item.kind(), " allied with ", playerNameOrYou(item.getOwner()),
						" ", distCalculator.distanceString(loc));
			}
		} else {
			throw new IllegalStateException("Unhandled ITownFixture subclass");
		}
	}

	/**
	 * Produce a report for a town. Handling of fortresses and villages is delegated to
	 * their dedicated report-generating classes. We remove the town from the set of
	 * fixtures.
	 *
	 * @param fixtures      the set of fixtures
	 * @param map           ignored
	 * @param item          the town to report on
	 * @param loc           its location
	 * @param currentPlayer the player for whom the report is being produced
	 * @return the sub-report dealing with the town.
	 */
	@Override
	public IReportNode produceRIR(final PatientMap<Integer, Pair<Point, IFixture>>
										  fixtures,
								  final IMapNG map, final Player currentPlayer,
								  final ITownFixture item, final Point loc) {
		if (item instanceof Village) {
			return new VillageReportGenerator(pairComparator).produceRIR(fixtures,
					map, currentPlayer, (Village) item, loc);
		} else if (item instanceof Fortress) {
			return new FortressReportGenerator(pairComparator).produceRIR(fixtures,
					map, currentPlayer, (Fortress) item, loc);
		} else if (item instanceof AbstractTown) {
			fixtures.remove(Integer.valueOf(item.getID()));
			if (item.getOwner().isIndependent()) {
				return new SimpleReportNode(loc, atPoint(loc), item.getName(),
												   ", an independent ",
												   item.size().toString(), " ",
												   item.status().toString(), " ",
												   item.kind(), " ",
												   distCalculator.distanceString(loc));
			} else {
				return new SimpleReportNode(loc, atPoint(loc), item.getName(), ", a ",
												   item.size().toString(), " ",
												   item.status().toString(), " ",
												   item.kind(),
												   " allied with " + playerNameOrYou(
														   item.getOwner()),
												   " ",
												   distCalculator.distanceString(loc));
			}
		} else {
			throw new IllegalStateException("Unhandled ITownFixture subclass");
		}
	}

	/**
	 * A trivial toString().
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "TownReportGenerator";
	}
}
