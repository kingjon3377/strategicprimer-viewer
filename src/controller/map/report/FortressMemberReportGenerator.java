package controller.map.report;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import model.map.IFixture;
import model.map.IMapNG;
import model.map.Player;
import model.map.Point;
import model.map.fixtures.FortressMember;
import model.map.fixtures.Implement;
import model.map.fixtures.ResourcePile;
import model.map.fixtures.mobile.IUnit;
import model.report.EmptyReportNode;
import model.report.IReportNode;
import model.report.ListReportNode;
import model.report.SectionListReportNode;
import model.report.SimpleReportNode;
import org.eclipse.jdt.annotation.NonNull;
import util.MultiMapHelper;
import util.Pair;
import util.PatientMap;

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
	 * Constructor.
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
	 * @param ostream       the Formatter to write to
	 */
	@Override
	public void produce(PatientMap<Integer, Pair<Point, IFixture>> fixtures, IMapNG map,
						Player currentPlayer, final Formatter ostream) {
		final List<Pair<Point, IFixture>> values = new ArrayList<>(fixtures.values());
		values.sort(pairComparator);
		final Map<Implement, Point> equipment =
				new TreeMap<>(Comparator.comparing(Implement::getKind)
									  .thenComparingInt(Implement::getID));
		final Map<String, Map<ResourcePile, Point>> resources = new HashMap<>();
		for (final Pair<Point, IFixture> pair : values) {
			if (pair.second() instanceof ResourcePile) {
				final ResourcePile resource = (ResourcePile) pair.second();
				MultiMapHelper.getMapValue(resources, resource.getKind(),
						key -> new TreeMap<>(Comparator.comparing(ResourcePile::getKind)
													 .thenComparing(
															 ResourcePile::getContents)
													 .thenComparing(
															 ResourcePile::getQuantity)
													 .thenComparingInt(
															 ResourcePile::getCreated)
													 .thenComparingInt(
															 ResourcePile::getID)))
						.put(resource, pair.first());
				fixtures.remove(Integer.valueOf(resource.getID()));
			} else if (pair.second() instanceof Implement) {
				equipment.put((Implement) pair.second(), pair.first());
				fixtures.remove(Integer.valueOf(pair.second().getID()));
			}
		}
		if (!equipment.isEmpty() && !resources.isEmpty()) {
			ostream.format("<h4>Resources and Equipment</h4>%n<ul>%n");
			if (!equipment.isEmpty()) {
				ostream.format("<li>Equipment:<ul>%n");
				for (final Map.Entry<Implement, Point> entry : equipment.entrySet()) {
					ostream.format("%s", OPEN_LIST_ITEM);
					produce(fixtures, map, currentPlayer, entry.getKey(), entry.getValue(), ostream);
					ostream.format("</li>%n");
				}
				ostream.format("</ul>%n</li>%n");
			}
			if (!resources.isEmpty()) {
				ostream.format("<li>Resources:<ul>%n");
				for (final Map.Entry<String, Map<ResourcePile, Point>> outer :
						resources.entrySet()) {
					ostream.format("<li>%s: <ul>%n", outer.getKey());
					for (final Map.Entry<ResourcePile, Point> inner :
							outer.getValue().entrySet()) {
						ostream.format("%s", OPEN_LIST_ITEM);
						produce(fixtures, map, currentPlayer, inner.getKey(),
								inner.getValue(), ostream);
						ostream.format("</li>%n");
					}
					ostream.format("</ul>%n</li>%n");
				}
				ostream.format("</ul>%n</li>%n");
			}
			ostream.format("</ul>%n");
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
		values.sort(pairComparator);
		final Map<String, IReportNode> resourceKinds = new HashMap<>();
		final IReportNode equip = new ListReportNode("Equipment:");
		for (final Pair<Point, IFixture> pair : values) {
			if (pair.second() instanceof ResourcePile) {
				final ResourcePile resource = (ResourcePile) pair.second();
				final String kind = resource.getKind();
				MultiMapHelper.getMapValue(resourceKinds, kind,
						key -> new ListReportNode(key + ':'))
						.add(produceRIR(fixtures, map, currentPlayer, resource,
								pair.first()));
			} else if (pair.second() instanceof Implement) {
				equip.add(produceRIR(fixtures, map, currentPlayer,
						(FortressMember) pair.second(), pair.first()));
			}
		}
		final IReportNode rsr = new ListReportNode("Resources:");
		resourceKinds.values().forEach(rsr::addIfNonEmpty);
		final IReportNode retval =
				new SectionListReportNode(4, "Resources and Equipment");
		retval.addIfNonEmpty(rsr, equip);
		if (retval.getChildCount() > 0) {
			return retval;
		} else {
			return EmptyReportNode.NULL_NODE;
		}
	}

	/**
	 * Produces a sub-report on a resource or piece of equipment.
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
						final FortressMember item, final Point loc, final Formatter ostream) {
		if (item instanceof IUnit) {
			new UnitReportGenerator(pairComparator)
					.produce(fixtures, map, currentPlayer, (IUnit) item, loc, ostream);
		} else if (item instanceof ResourcePile) {
			fixtures.remove(Integer.valueOf(item.getID()));
			final ResourcePile rsr = (ResourcePile) item;
			if (rsr.getQuantity().getUnits().isEmpty()) {
				ostream.format("A pile of %s %s (%s)", rsr.getQuantity().toString(),
						rsr.getContents(), rsr.getKind());
			} else {
				ostream.format("A pile of %s of %s (%s)", rsr.getQuantity().toString(),
						rsr.getContents(), rsr.getKind());
			}
			if (rsr.getCreated() >= 0) {
				ostream.format(" from turn %d", Integer.valueOf(rsr.getCreated()));
			}
		} else if (item instanceof Implement) {
			fixtures.remove(Integer.valueOf(item.getID()));
			ostream.format("Equipment: %s", ((Implement) item).getKind());
		} else {
			throw new IllegalArgumentException("Unexpected FortressMember type");
		}
	}

	/**
	 * Produces a sub-report on a resource or piece of equipment.
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
		if (item instanceof IUnit) {
			return new UnitReportGenerator(pairComparator)
						   .produceRIR(fixtures, map, currentPlayer,
								   (IUnit) item, loc);
		} else if (item instanceof ResourcePile) {
			fixtures.remove(Integer.valueOf(item.getID()));
			final ResourcePile rsr = (ResourcePile) item;
			final String age;
			if (rsr.getCreated() < 0) {
				age = "";
			} else {
				age = " from turn " + rsr.getCreated();
			}
			if (rsr.getQuantity().getUnits().isEmpty()) {
				return new SimpleReportNode("A pile of ",
												   rsr.getQuantity().getNumber()
														   .toString(),
												   rsr.getContents(), " (",
												   rsr.getKind(), ")", age);
			} else {
				return new SimpleReportNode("A pile of ", rsr.getQuantity().toString(),
												   " of ", rsr.getContents(), " (",
												   rsr.getKind(), ")", age);
			}
		} else if (item instanceof Implement) {
			fixtures.remove(Integer.valueOf(item.getID()));
			return new SimpleReportNode("Equipment: ", ((Implement) item).getKind());
		} else {
			throw new IllegalArgumentException("Unexpected FortressMember type");
		}
	}
}
