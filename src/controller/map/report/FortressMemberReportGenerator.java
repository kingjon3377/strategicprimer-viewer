package controller.map.report;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.map.IFixture;
import model.map.IMapNG;
import model.map.Player;
import model.map.Point;
import model.map.fixtures.FortressMember;
import model.map.fixtures.Implement;
import model.map.fixtures.ResourcePile;
import model.map.fixtures.mobile.Unit;
import model.report.EmptyReportNode;
import model.report.IReportNode;
import model.report.ListReportNode;
import model.report.SectionListReportNode;
import model.report.SimpleReportNode;
import org.eclipse.jdt.annotation.NonNull;
import util.LineEnd;
import util.NullCleaner;
import util.Pair;
import util.PatientMap;
import util.SimpleMultiMap;

/**
 * A report generator for equipment and resources.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class FortressMemberReportGenerator
		extends AbstractReportGenerator<FortressMember> {
	/**
	 * @param comparator a comparator for pairs of Points and fixtures.
	 */
	public FortressMemberReportGenerator(final Comparator<@NonNull Pair<@NonNull Point,
																			@NonNull
																					IFixture>> comparator) {
		super(comparator);
	}
	/**
	 * Produce the sub-report on equipment and resources. All fixtures referred to in
	 * this report are removed from the collection. This method should probably never
	 * actually be called and do anything, since nearly all resources will be in
	 * fortresses and should be reported as such, but we'll handle this properly anyway.
	 *
	 * @param fixtures      the set of fixtures
	 * @param map           ignored
	 * @param currentPlayer the player for whom the report is being produced
	 * @return the part of the report listing non-unit fortress members.
	 */
	@Override
	public String produce(final PatientMap<Integer, Pair<Point, IFixture>> fixtures,
						  final IMapNG map, final Player currentPlayer) {
		// At only two (albeit who-knows-how-long) list items, I doubt this will ever be
		// over one K, but we'll give it two just in case.
		final StringBuilder builder =
				new StringBuilder(2048).append("<h4>Resources and Equipment</h4>")
						.append(LineEnd.LINE_SEP).append(OPEN_LIST);
		final Map<String, Collection<Pair<Point, ResourcePile>>> resources =
				new SimpleMultiMap<>();
		// Similarly, I doubt either of these will ever be over half a K, but
		// we'll give each a whole K just in case.
		final StringBuilder rsrBuilder =
				new StringBuilder(1024).append(OPEN_LIST_ITEM).append("Resources:")
						.append(OPEN_LIST);
		final StringBuilder eqBuilder =
				new StringBuilder(1024).append(OPEN_LIST_ITEM).append("Equipment:")
						.append(OPEN_LIST);
		final List<Pair<Point, IFixture>> values = new ArrayList<>(fixtures.values());
		Collections.sort(values, pairComparator);
		boolean anyResources = false;
		boolean anyEquipment = false;
		for (final Pair<Point, IFixture> pair : values) {
			if (pair.second() instanceof ResourcePile) {
				anyResources = true;
				final ResourcePile resource = (ResourcePile) pair.second();
				resources.get(resource.getKind()).add(Pair.of(pair.first(), resource));
				fixtures.remove(Integer.valueOf(pair.second().getID()));
			} else if (pair.second() instanceof Implement) {
				anyEquipment = true;
				eqBuilder.append(OPEN_LIST_ITEM)
						.append(produce(fixtures, map, currentPlayer,
								(FortressMember) pair.second(), pair.first()))
						.append(CLOSE_LIST_ITEM);
				fixtures.remove(Integer.valueOf(pair.second().getID()));
			}
		}
		for (final Map.Entry<String, Collection<Pair<Point, ResourcePile>>> entry :
				resources.entrySet()) {
			rsrBuilder.append(OPEN_LIST_ITEM).append(entry.getKey()).append(':')
					.append(OPEN_LIST);
			for (final Pair<Point, ResourcePile> pair : entry.getValue()) {
				rsrBuilder.append(OPEN_LIST_ITEM)
						.append(produce(fixtures, map, currentPlayer, pair.second(),
								pair.first())).append(CLOSE_LIST_ITEM);
			}
			rsrBuilder.append(CLOSE_LIST);
		}
		eqBuilder.append(CLOSE_LIST).append(CLOSE_LIST_ITEM);
		rsrBuilder.append(CLOSE_LIST).append(CLOSE_LIST_ITEM);
		if (anyEquipment) {
			builder.append(eqBuilder);
		}
		if (anyResources) {
			builder.append(rsrBuilder);
		}
		if (anyEquipment || anyResources) {
			return NullCleaner.assertNotNull(builder.toString());
		} else {
			return "";
		}
	}
	/**
	 * Produce the sub-report on equipment and resources. All fixtures referred to in
	 * this report are removed from the collection. This method should probably never
	 * actually be called and do anything, since nearly all resources will be in
	 * fortresses and should be reported as such, but we'll handle this properly anyway.
	 *
	 * @param fixtures      the set of fixtures
	 * @param map           ignored
	 * @param currentPlayer the player for whom the report is being produced
	 * @return the part of the report listing equipment and resources.
	 */
	@Override
	public IReportNode produceRIR(final PatientMap<Integer, Pair<Point, IFixture>>
											  fixtures,
								  final IMapNG map, final Player currentPlayer) {
		final List<Pair<Point, IFixture>> values = new ArrayList<>(fixtures.values());
		Collections.sort(values, pairComparator);
		final IReportNode rsr = new ListReportNode("Resources:");
		final Map<String, IReportNode> resourceKinds = new HashMap<>();
		final IReportNode equip = new ListReportNode("Equipment:");
		for (final Pair<Point, IFixture> pair : values) {
			if (pair.second() instanceof ResourcePile) {
				final ResourcePile resource = (ResourcePile) pair.second();
				final String kind = resource.getKind();
				final IReportNode list;
				if (resourceKinds.containsKey(kind)) {
					list = resourceKinds.get(kind);
				} else {
					list = new ListReportNode(kind + ":");
					resourceKinds.put(kind, list);
					rsr.add(list);
				}
				list.add(
						produceRIR(fixtures, map, currentPlayer, resource, pair.first()));
			} else if (pair.second() instanceof Implement) {
				equip.add(produceRIR(fixtures, map, currentPlayer,
						(FortressMember) pair.second(), pair.first()));
			}
		}
		final IReportNode retval =
				new SectionListReportNode(4, "Resources and Equipment");
		if (rsr.getChildCount() > 0) {
			retval.add(rsr);
		}
		if (equip.getChildCount() > 0) {
			retval.add(equip);
		}
		if (retval.getChildCount() > 0) {
			return retval;
		} else {
			return EmptyReportNode.NULL_NODE;
		}
	}
	/**
	 * Produces a sub-report on a resource or piece of equipment
	 *
	 * @param fixtures      the set of fixtures.
	 * @param map           ignored
	 * @param item          the item to report on
	 * @param loc           its location
	 * @param currentPlayer the player for whom the report is being produced
	 * @return a sub-report on the item; calls UnitReportGenerator for units.
	 */
	@Override
	public String produce(final PatientMap<Integer, Pair<Point, IFixture>> fixtures,
						  final IMapNG map, final Player currentPlayer,
						  final FortressMember item, final Point loc) {
		if (item instanceof Unit) {
			return new UnitReportGenerator(pairComparator)
						.produce(fixtures, map, currentPlayer, (Unit) item, loc);
		} else if (item instanceof ResourcePile) {
			fixtures.remove(Integer.valueOf(item.getID()));
			final ResourcePile rsr = (ResourcePile) item;
			final String age;
			if (rsr.getCreated() < 0) {
				age = "";
			} else {
				age = " from turn " + rsr.getCreated();
			}
			if (rsr.getUnits().isEmpty()) {
				return NullCleaner.assertNotNull(String.format("A pile of %d %s (%s)%s",
						Integer.valueOf(rsr.getQuantity()), rsr.getContents(),
						rsr.getKind(), age));
			} else {
				return NullCleaner.assertNotNull(
						String.format("A pile of %d %s of %s (%s)%s",
								Integer.valueOf(rsr.getQuantity()), rsr.getUnits(),
								rsr.getContents(), rsr.getKind(), age));
			}
		} else if (item instanceof Implement) {
			fixtures.remove(Integer.valueOf(item.getID()));
			return "Equipment: " + ((Implement) item).getKind();
		} else {
			throw new IllegalArgumentException("Unexpected FortressMember type");
		}
	}
	/**
	 * Produces a sub-report on a resource or piece of equipment
	 *
	 * @param fixtures      the set of fixtures.
	 * @param map           ignored
	 * @param item          the item to report on
	 * @param loc           its location
	 * @param currentPlayer the player for whom the report is being produced
	 * @return a sub-report on the item; calls UnitReportGenerator for units.
	 */
	@Override
	public IReportNode produceRIR(final PatientMap<Integer, Pair<Point, IFixture>>
											  fixtures,
								  final IMapNG map, final Player currentPlayer,
								  final FortressMember item, final Point loc) {
		if (item instanceof Unit) {
			return new UnitReportGenerator(pairComparator).produceRIR(fixtures, map, currentPlayer,
					(Unit) item, loc);
		} else if (item instanceof ResourcePile) {
			fixtures.remove(Integer.valueOf(item.getID()));
			final ResourcePile rsr = (ResourcePile) item;
			final String age;
			if (rsr.getCreated() < 0) {
				age = "";
			} else {
				age = " from turn " + rsr.getCreated();
			}
			if (rsr.getUnits().isEmpty()) {
				return new SimpleReportNode("A pile of ",
												Integer.toString(rsr.getQuantity()),
												rsr.getContents(), " (", rsr.getKind(),
												")", age);
			} else {
				return new SimpleReportNode("A pile of ",
												Integer.toString(rsr.getQuantity()),
												" ", rsr.getUnits(), " of ",
												rsr.getContents(), " (", rsr.getKind(),
												")", age);
			}
		} else if (item instanceof Implement) {
			fixtures.remove(Integer.valueOf(item.getID()));
			return new SimpleReportNode("Equipment: ", ((Implement) item).getKind());
		} else {
			throw new IllegalArgumentException("Unexpected FortressMember type");
		}
	}
}
