package controller.map.report;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import model.map.HasOwner;
import model.map.IFixture;
import model.map.IMapNG;
import model.map.Player;
import model.map.Point;
import model.map.River;
import model.map.TileFixture;
import model.map.fixtures.FortressMember;
import model.map.fixtures.Implement;
import model.map.fixtures.ResourcePile;
import model.map.fixtures.mobile.IUnit;
import model.map.fixtures.terrain.Forest;
import model.map.fixtures.terrain.Hill;
import model.map.fixtures.terrain.Mountain;
import model.map.fixtures.terrain.Oasis;
import model.map.fixtures.towns.Fortress;
import model.report.ComplexReportNode;
import model.report.EmptyReportNode;
import model.report.IReportNode;
import model.report.ListReportNode;
import model.report.SectionListReportNode;
import model.report.SectionReportNode;
import model.report.SimpleReportNode;
import org.eclipse.jdt.annotation.NonNull;
import util.LineEnd;
import util.MultiMapHelper;
import util.NullCleaner;
import util.Pair;
import util.PatientMap;

/**
 * A report generator for fortresses.
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
public final class FortressReportGenerator extends AbstractReportGenerator<Fortress> {
	/**
	 * Instance we use.
	 */
	private final IReportGenerator<IUnit> urg = new UnitReportGenerator(pairComparator);
	/**
	 * Instance we use.
	 */
	private final IReportGenerator<FortressMember> memberReportGenerator =
			new FortressMemberReportGenerator(pairComparator);
	/**
	 * @param comparator a comparator for pairs of Points and fixtures.
	 */
	public FortressReportGenerator(final Comparator<@NonNull Pair<@NonNull Point,
																		 @NonNull
																				 IFixture>> comparator) {
		super(comparator);
	}

	/**
	 * @param map      the map
	 * @param point    a point
	 * @param fixtures the set of fixtures, so we can schedule the removal the terrain
	 *                 fixtures from it
	 * @return a String describing the terrain on it
	 */
	private static String getTerrain(final IMapNG map, final Point point,
									 final PatientMap<Integer, Pair<Point, IFixture>>
											 fixtures) {
		final StringBuilder builder = new StringBuilder(130).append(
				"Surrounding terrain: ").append(
				map.getBaseTerrain(point).toXML().replace('_', ' '));
		boolean unforested = true;
		final Forest forest = map.getForest(point);
		if (forest != null) {
			builder.append(", forested with ").append(forest.getKind());
			unforested = false;
		}
		if (map.isMountainous(point)) {
			builder.append(", mountainous");
		}
		for (final TileFixture fix : map.getOtherFixtures(point)) {
			if (fix instanceof Forest) {
				if (unforested) {
					unforested = false;
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
			builder.append("<li>There is a nearby lake.</li>");
			builder.append(LineEnd.LINE_SEP);
			rivers.remove(River.Lake);
		}
		if (!rivers.isEmpty()) {
			builder.append(OPEN_LIST_ITEM);
			builder.append("There is a river on the tile, ");
			builder.append("flowing through the following borders: ");
			builder.append(rivers.stream().map(River::getDescription)
								   .collect(Collectors.joining(", ")));
			builder.append(CLOSE_LIST_ITEM);
		}
		return NullCleaner.assertNotNull(builder.toString());
	}

	/**
	 * @param loc    where this is
	 * @param parent the node to add nodes describing rivers to
	 * @param rivers the collection of rivers
	 */
	private static void riversToNode(final Point loc, final IReportNode parent,
									 final Collection<River> rivers) {
		if (rivers.contains(River.Lake)) {
			parent.add(new SimpleReportNode(loc, "There is a nearby lake."));
			rivers.remove(River.Lake);
		}
		if (!rivers.isEmpty()) {
			parent.add(new SimpleReportNode(loc,
												   "There is a river on the tile, " +
														   "flowing " +
														   "through the following " +
														   "borders: ",
												   rivers.stream()
														   .map(River::getDescription)
														   .collect(Collectors.joining(
																   ", "))));
		}
	}

	/**
	 * All fixtures referred to in this report are removed from the collection.
	 *
	 * @param fixtures      the set of fixtures
	 * @param currentPlayer the player for whom the report is being produced
	 * @param map           the map (needed to get terrain information)
	 * @return the part of the report dealing with fortresses
	 */
	@Override
	public String produce(final PatientMap<Integer, Pair<Point, IFixture>> fixtures,
						  final IMapNG map, final Player currentPlayer) {
		// This can get long. We'll give it 16K.
		final StringBuilder ours =
				new StringBuilder(16384).append("<h4>Your fortresses in the map:</h4>")
						.append(LineEnd.LINE_SEP);
		final StringBuilder builder =
				new StringBuilder(16384).append("<h4>Other fortresses in the map:</h4>")
						.append(LineEnd.LINE_SEP);
		final List<Pair<Point, IFixture>> values = new ArrayList<>(fixtures.values());
		values.sort(pairComparator);
		boolean anyForts = false;
		boolean anyOurs = false;
		for (final Pair<Point, IFixture> pair : values) {
			if (pair.second() instanceof Fortress) {
				final Fortress fort = (Fortress) pair.second();
				if (currentPlayer.equals(fort.getOwner())) {
					anyOurs = true;
					ours.append(produce(fixtures, map, currentPlayer, fort,
							pair.first()));
				} else {
					anyForts = true;
					builder.append(produce(fixtures, map, currentPlayer,
							fort, pair.first()));
				}
			}
		}
		if (anyOurs) {
			if (anyForts) {
				ours.append(builder);
			}
			return NullCleaner.assertNotNull(ours.toString());
		} else if (anyForts) {
			return NullCleaner.assertNotNull(builder.toString());
		} else {
			return "";
		}
	}

	/**
	 * All fixtures referred to in this report are removed from the collection.
	 *
	 * @param fixtures      the set of fixtures
	 * @param currentPlayer the player for whom the report is being produced
	 * @param map           the map (needed to get terrain information)
	 * @return the part of the report dealing with fortresses
	 */
	@Override
	public IReportNode produceRIR(final PatientMap<Integer, Pair<Point, IFixture>>
										  fixtures,
								  final IMapNG map, final Player currentPlayer) {
		final List<Pair<Point, IFixture>> values = new ArrayList<>(fixtures.values());
		values.sort(pairComparator);
		final IReportNode foreign =
				new SectionReportNode(4, "Foreign fortresses in the map:");
		final IReportNode ours = new SectionReportNode(4, "Your fortresses in the map:");
		values.stream().filter(pair -> pair.second() instanceof Fortress)
				.forEach(pair -> {
					final HasOwner fort = (HasOwner) pair.second();
					if (currentPlayer.equals(fort.getOwner())) {
						ours.add(produceRIR(fixtures, map, currentPlayer,
								(Fortress) pair.second(), pair.first()));
					} else {
						foreign.add(produceRIR(fixtures, map, currentPlayer,
								(Fortress) pair.second(), pair.first()));
					}
				});
		if (ours.getChildCount() == 0) {
			if (foreign.getChildCount() == 0) {
				return EmptyReportNode.NULL_NODE;
			} else {
				return foreign;
			}
		} else if (foreign.getChildCount() == 0) {
			return ours;
		} else {
			final IReportNode retval = new ComplexReportNode();
			retval.add(ours);
			retval.add(foreign);
			return retval;
		}
	}

	/**
	 * All fixtures referred to in this report are removed from the collection.
	 *
	 * @param item          the fortress to report on
	 * @param loc           its location
	 * @param fixtures      the set of fixtures
	 * @param map           the map (needed to get terrain information)
	 * @param currentPlayer the player for whom the report is being produced
	 * @return the part of the report dealing with fortresses
	 */
	@Override
	public String produce(final PatientMap<Integer, Pair<Point, IFixture>> fixtures,
						  final IMapNG map, final Player currentPlayer,
						  final Fortress item, final Point loc) {
		// This can get long. we'll give it 16K.
		final StringBuilder builder = new StringBuilder(16384).append("<h5>Fortress ")
											  .append(item.getName())
											  .append(" belonging to ")
											  .append(playerNameOrYou(item.getOwner()))
											  .append("</h5>").append(LineEnd.LINE_SEP)
											  .append(OPEN_LIST).append(OPEN_LIST_ITEM)
											  .append("Located at ")
											  .append(loc).append(' ')
											  .append(distCalculator.distanceString(loc))
											  .append(CLOSE_LIST_ITEM)
											  .append(OPEN_LIST_ITEM);
		builder.append(getTerrain(map, loc, fixtures)).append(CLOSE_LIST_ITEM);
		builder.append(riversToString(
				StreamSupport.stream(map.getRivers(loc).spliterator(), false)
						.collect(
								Collectors.toSet())));
		final HeadedList<String> units = new HtmlList("Units on the tile:");
		final HeadedList<String> resourcesText = new HtmlList("Resources:");
		final HeadedList<String> equipment = new HtmlList("Equipment:");
		final Map<String, Collection<String>> resources = new HashMap<>();
		final HeadedList<String> contents = new HtmlList("Other fortress contents:");
		for (final FortressMember member : item) {
			if (member instanceof IUnit) {
				units.add(urg.produce(fixtures, map, currentPlayer, (IUnit) member, loc));
			} else if (member instanceof Implement) {
				equipment.add(memberReportGenerator
									  .produce(fixtures, map, currentPlayer, member,
											  loc));
			} else if (member instanceof ResourcePile) {
				final ResourcePile pile = (ResourcePile) member;
				final String kind = pile.getKind();
				MultiMapHelper
						.getMapValue(resources, kind, key -> new HtmlList(key + ':'))
						.add(memberReportGenerator
									 .produce(fixtures, map, currentPlayer, pile, loc));
			} else {
				contents.add(memberReportGenerator
									 .produce(fixtures, map, currentPlayer, member, loc));
			}
		}
		builder.append(units);
		resources.values().stream().map(Collection::toString)
				.forEach(resourcesText::add);
		builder.append(resources);
		builder.append(equipment);
		fixtures.remove(Integer.valueOf(item.getID()));
		return NullCleaner.assertNotNull(builder.toString());
	}

	/**
	 * All fixtures referred to in this report are removed from the collection.
	 *
	 * @param item          the fortress to report on
	 * @param loc           its location
	 * @param fixtures      the set of fixtures
	 * @param map           the map (needed to get terrain information)
	 * @param currentPlayer the player for whom the report is being produced
	 * @return the part of the report dealing with the fortress
	 */
	@Override
	public SectionListReportNode produceRIR(final PatientMap<Integer, Pair<Point,
																				  IFixture>> fixtures,
											final IMapNG map, final Player currentPlayer,
											final Fortress item, final Point loc) {
		final SectionListReportNode retval = new SectionListReportNode(loc, 5, concat(
				"Fortress ", item.getName(), " belonging to ",
				playerNameOrYou(item.getOwner())));
		retval.add(new SimpleReportNode(loc, "Located at ", loc.toString(), " ",
											   distCalculator.distanceString(loc)));
		retval.add(new SimpleReportNode(loc, getTerrain(map, loc, fixtures)));
		if (map.getRivers(loc).iterator().hasNext()) {
			riversToNode(loc, retval,
					StreamSupport.stream(map.getRivers(loc).spliterator(), false)
							.collect(Collectors.toSet()));
		}
		final IReportNode units = new ListReportNode(loc, "Units on the tile:");
		final IReportNode resources = new ListReportNode(loc, "Resources");
		final Map<String, IReportNode> resourceKinds = new HashMap<>();
		final IReportNode equipment = new ListReportNode(loc, "Equipment:");
		final IReportNode contents =
				new ListReportNode(loc, "Other Contents of Fortress:");
		for (final FortressMember unit : item) {
			if (unit instanceof IUnit) {
				units.add(
						urg.produceRIR(fixtures, map, currentPlayer, (IUnit) unit, loc));
			} else if (unit instanceof Implement) {
				equipment.add(memberReportGenerator
									  .produceRIR(fixtures, map, currentPlayer, unit,
											  loc));
			} else if (unit instanceof ResourcePile) {
				MultiMapHelper.getMapValue(resourceKinds,
						((ResourcePile) unit).getKind(),
						key -> new ListReportNode(key + ':'))
						.add(memberReportGenerator
									 .produceRIR(fixtures, map, currentPlayer, unit,
											 loc));
			} else {
				contents.add(memberReportGenerator
									 .produceRIR(fixtures, map, currentPlayer, unit,
											 loc));
			}
			resourceKinds.values().forEach(resources::addIfNonEmpty);
			retval.addIfNonEmpty(units, resources, equipment, contents);
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
		return "FortressReportGenerator";
	}
}
