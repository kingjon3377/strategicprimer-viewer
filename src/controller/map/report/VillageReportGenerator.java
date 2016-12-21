package controller.map.report;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import util.LineEnd;
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
	 * @return the part of the report dealing with villages.
	 */
	@Override
	public String produce(final PatientMap<Integer, Pair<Point, IFixture>> fixtures,
						  final IMapNG map, final Player currentPlayer) {
		final List<Pair<Point, IFixture>> values = new ArrayList<>(fixtures.values());
		values.sort(pairComparator);
		final Collection<String> own =
				new HtmlList("<h4>Villages pledged to your service:</h4>");
		final Collection<String> independents =
				new HtmlList("<h4>Villages you think are independent:</h4>");
		final Map<Player, Collection<String>> others = new HashMap<>();
		values.stream().filter(pair -> pair.second() instanceof Village)
				.forEach(pair -> {
			final Village village = (Village) pair.second();
			final String product =
					produce(fixtures, map, currentPlayer, village,
							pair.first());
			if (village.getOwner().isCurrent()) {
				own.add(product);
			} else if (village.getOwner().isIndependent()) {
				independents.add(product);
			} else if (others.containsKey(village.getOwner())) {
				others.get(village.getOwner()).add(product);
			} else {
				final Collection<String> coll = new HtmlList("<h5>Villages sworn to " +
																	 village.getOwner()
																			 .getName() +
																	 "</h5>");
				coll.add(product);
				others.put(village.getOwner(), coll);
			}
		});
		final String ownString = own.toString();
		final String independentsString = independents.toString();
		final StringBuilder retval =
				new StringBuilder(40 + ownString.length() + independentsString.length() +
										  (others.values().stream()
												   .mapToInt(Collection::size).sum() *
												   512));
		// HtmlLists will return the empty string if they are empty.
		retval.append(ownString);
		retval.append(independentsString);
		if (!others.isEmpty()) {
			retval.append("<h4>Other villages you know about:</h4>");
			retval.append(LineEnd.LINE_SEP);
			others.values().stream().map(Object::toString).forEach(retval::append);
		}
		return retval.toString();
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
		final IReportNode own = new SectionListReportNode(5,
																 "Villages " +
																		 "pledged" +
																		 " to " +
																		 "your " +
																		 "service:");
		final IReportNode independents =
				new SectionListReportNode(5, "Villages you think are independent:");
		final IReportNode others =
				new SectionListReportNode(5, "Other villages you know about:");
		@SuppressWarnings("TooBroadScope") final Map<Player, IReportNode> othersMap =
				new HashMap<>();
		values.stream().filter(pair -> pair.second() instanceof Village)
				.forEach(pair -> {
			final Village village = (Village) pair.second();
			final Player owner = village.getOwner();
			final IReportNode product =
					produceRIR(fixtures, map, currentPlayer, village,
							pair.first());
			if (owner.isCurrent()) {
				own.add(product);
			} else if (owner.isIndependent()) {
				independents.add(product);
			} else if (othersMap.containsKey(owner)) {
				// TODO: Use MultiMapHelper
				othersMap.get(owner).add(product);
			} else {
				final IReportNode node =
						new SectionListReportNode(6, "Villages sworn to " +
															 owner.getName());
				node.add(product);
				others.add(node);
				othersMap.put(owner, node);
			}
		});
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
	 * @return the report on the village (its location and name, nothing more)
	 */
	@Override
	public String produce(final PatientMap<Integer, Pair<Point, IFixture>> fixtures,
						  final IMapNG map, final Player currentPlayer,
						  final Village item, final Point loc) {
		fixtures.remove(Integer.valueOf(item.getID()));
		if (item.getOwner().isIndependent()) {
			return concat(atPoint(loc), item.getName(), ", a(n) ",
					item.getRace(), " village", ", independent ",
					distCalculator.distanceString(loc));
		} else {
			return concat(atPoint(loc), item.getName(), ", a(n) ",
					item.getRace(), " village", ", sworn to "
														+
														playerNameOrYou(item.getOwner()),
					" ", distCalculator.distanceString(loc));
		}
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
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "VillageReportGenerator";
	}
}
