package controller.map.report;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import model.map.IFixture;
import model.map.IMapNG;
import model.map.Player;
import model.map.Point;
import model.map.fixtures.towns.Village;
import model.report.EmptyReportNode;
import model.report.IReportNode;
import model.report.SectionListReportNode;
import model.report.SectionReportNode;
import model.report.SimpleReportNode;
import org.eclipse.jdt.annotation.NonNull;
import util.MultiMapHelper;
import util.Pair;
import util.PatientMap;

/**
 * A report generator for Villages.
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
public final class VillageReportGenerator extends AbstractReportGenerator<Village> {
	/**
	 * Constructor.
	 * @param comparator a comparator for pairs of Points and fixtures.
	 */
	public VillageReportGenerator(final Comparator<@NonNull Pair<@NonNull Point,
																		@NonNull
																				IFixture>> comparator) {
		super(comparator);
	}

	/**
	 * Produce the report on all villages. All fixtures referred to in this report are
	 * removed from the collection.
	 *
	 * @param fixtures      the set of fixtures
	 * @param currentPlayer the player for whom the report is being produced
	 * @param map           ignored
	 * @param ostream       the Formatter to write to
	 */
	@Override
	public void produce(PatientMap<Integer, Pair<Point, IFixture>> fixtures, IMapNG map,
						Player currentPlayer, final Formatter ostream) {
		final List<Pair<Point, IFixture>> values = new ArrayList<>(fixtures.values());
		values.sort(pairComparator);
		final Comparator<Village> villageComparator =
				Comparator.comparing(Village::getName).thenComparing(Village::getRace)
						.thenComparingInt(Village::getID);
		// TODO: sort by distance somehow?
		final Map<Village, Point> own = new TreeMap<>(villageComparator);
		final Map<Village, Point> independents = new TreeMap<>(villageComparator);
		final Map<Player, Map<Village, Point>> others = new HashMap<>();
		values.stream().filter(pair -> pair.second() instanceof Village)
				.forEach(pair -> {
					final Village village = (Village) pair.second();
					if (village.getOwner().isCurrent()) {
						own.put(village, pair.first());
					} else if (village.getOwner().isIndependent()) {
						independents.put(village, pair.first());
					} else {
						MultiMapHelper.getMapValue(others, village.getOwner(),
								key -> new TreeMap<>(villageComparator))
								.put(village, pair.first());
					}
				});
		final BiConsumer<Map.Entry<Village, Point>, Formatter> writer =
				(entry, formatter) -> produce(fixtures, map, currentPlayer,
						entry.getKey(), entry.getValue(), formatter);
		writeMap(ostream, own, "<h4>Villages pledged to your service:</h4>", writer);
		writeMap(ostream, independents, "<h4>Villages you think are independent:</h4>",
				writer);
		if (!others.isEmpty()) {
			ostream.format("<h4>Other villages you know about:</h4>%n");
			for (final Map.Entry<Player, Map<Village, Point>> outer : others.entrySet()) {
				writeMap(ostream, outer.getValue(),
						String.format("<h5>Villages sworn to %s</h5>%n<ul>%n",
								outer.getKey().getName()), writer);
			}
		}
	}

	/**
	 * Produce the report on all villages. All fixtures referred to in this report are
	 * removed from the collection.
	 *
	 * @param fixtures      the set of fixtures
	 * @param currentPlayer the player for whom the report is being produced
	 * @param map           ignored
	 * @return the part of the report dealing with villages.
	 */
	@Override
	public IReportNode produceRIR(final PatientMap<Integer, Pair<Point, IFixture>>
										  fixtures,
								  final IMapNG map, final Player currentPlayer) {
		final List<Pair<Point, IFixture>> values = new ArrayList<>(fixtures.values());
		values.sort(pairComparator);
		final IReportNode own = new SectionListReportNode(
				5, "Villages pledged to your service:");
		final IReportNode independents =
				new SectionListReportNode(5, "Villages you think are independent:");
		@SuppressWarnings("TooBroadScope") final Map<Player, IReportNode> othersMap =
				new HashMap<>();
		values.stream().filter(pair -> pair.second() instanceof Village)
				.forEach(pair -> {
			final Village village = (Village) pair.second();
			final Player owner = village.getOwner();
			final IReportNode product = produceRIR(fixtures, map, currentPlayer, village,
					pair.first());
			if (owner.isCurrent()) {
				own.add(product);
			} else if (owner.isIndependent()) {
				independents.add(product);
			} else {
				MultiMapHelper.getMapValue(othersMap, owner,
						player -> new SectionListReportNode(6, "Villages sworn to " +
																	   player.getName())).add(product);
			}
		});
		final IReportNode others =
				new SectionListReportNode(5, "Other villages you know about:");
		othersMap.values().forEach(others::addIfNonEmpty);
		final IReportNode retval = new SectionReportNode(4, "Villages:");
		retval.addIfNonEmpty(own, independents, others);
		if (retval.getChildCount() == 0) {
			return EmptyReportNode.NULL_NODE;
		} else {
			return retval;
		}
	}

	/**
	 * Produce the (very brief) report for a particular village. We're probably in the
	 * middle of a bulleted list, but we don't assume that.
	 *
	 * @param fixtures      the set of fixtures---we remove the specified village from
	 *                      it.
	 * @param map           ignored
	 * @param item          the village to report on
	 * @param loc           its location
	 * @param currentPlayer the player for whom the report is being produced
	 * @param ostream	    the Formatter to write to
	 */
	@Override
	public void produce(final PatientMap<Integer, Pair<Point, IFixture>> fixtures,
						final IMapNG map, final Player currentPlayer,
						final Village item, final Point loc, final Formatter ostream) {
		fixtures.remove(Integer.valueOf(item.getID()));
		ostream.format("%s %s, a(n) %s village, ", atPoint(loc), item.getName(),
				item.getRace());
		if (item.getOwner().isIndependent()) {
			ostream.format("independent");
		} else {
			ostream.format("sworn to %s", playerNameOrYou(item.getOwner()));
		}
		ostream.format(" %s", distCalculator.distanceString(loc));
	}

	/**
	 * Produce the (very brief) report for a particular village.
	 *
	 * @param fixtures      the set of fixtures---we remove the specified village from
	 *                      it.
	 * @param map           ignored
	 * @param item          the village to report on
	 * @param loc           its location
	 * @param currentPlayer the player for whom the report is being produced
	 * @return the report on the village (its location and name, nothing more)
	 */
	@Override
	public IReportNode produceRIR(final PatientMap<Integer, Pair<Point, IFixture>>
											   fixtures,
									   final IMapNG map, final Player currentPlayer,
									   final Village item, final Point loc) {
		fixtures.remove(Integer.valueOf(item.getID()));
		if (item.getOwner().isIndependent()) {
			return new SimpleReportNode(loc, atPoint(loc), item.getName(),
											   ", a(n) ", item.getRace(), " village",
											   ", independent ",
											   distCalculator.distanceString(loc));
		} else {
			return new SimpleReportNode(loc, atPoint(loc), item.getName(),
											   ", a(n) ", item.getRace(), " village",
											   ", sworn to ",
											   playerNameOrYou(item.getOwner()), " ",
											   distCalculator.distanceString(loc));
		}
	}

	/**
	 * A trivial toString().
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "VillageReportGenerator";
	}
}
