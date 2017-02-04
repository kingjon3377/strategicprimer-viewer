package controller.map.report;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import model.map.HasKind;
import model.map.IFixture;
import model.map.IMapNG;
import model.map.Player;
import model.map.Point;
import model.map.fixtures.mobile.Centaur;
import model.map.fixtures.mobile.Dragon;
import model.map.fixtures.mobile.Fairy;
import model.map.fixtures.mobile.Giant;
import model.map.fixtures.mobile.Immortal;
import model.map.fixtures.mobile.SimpleImmortal;
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
 * A report generator for "immortals"---dragons, fairies, centaurs, and such.
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
public final class ImmortalsReportGenerator
		extends AbstractReportGenerator<Immortal> {
	/**
	 * Constructor.
	 * @param comparator a comparator for pairs of Points and fixtures.
	 */
	public ImmortalsReportGenerator(final Comparator<@NonNull Pair<@NonNull Point,
																		  @NonNull
																				  IFixture>> comparator) {
		super(comparator);
	}

	/**
	 * Turn a map from kinds to nodes into a parent node.
	 * @param header  the heading to put above the children
	 * @param mapping a mapping from kinds to nodes
	 * @return a node with all of the nodes as children
	 */
	private static IReportNode coalesce(final String header,
										final Map<String, IReportNode> mapping) {
		final IReportNode retval = new ListReportNode(header);
		mapping.values().forEach(retval::add);
		return retval;
	}

	/**
	 * If there's an entry in the map for the thing's kind already, return that entry; if
	 * not, create one, add it to the map, and return it..
	 *
	 * @param mapping the mapping we're dealing with
	 * @param item    the item under consideration
	 * @return the entry in the map for the item's kind
	 */
	private static IReportNode separateByKindRIR(final Map<String, IReportNode> mapping,
												 final HasKind item) {
		// For the three classes we deal with here, we don't want just the kind,
		// we want the full toString, so we use that instead of getKind.
		return MultiMapHelper.getMapValue(mapping, item.toString(), ListReportNode::new);
	}

	/**
	 * Handle a more complex fixture class.
	 * @param meta the mapping from types to Consumers
	 * @param cls a type of fixture
	 * @param plural the pluralization to add after each list
	 * @return the collection of lists for that kind of fixture, with a Consumer
	 * handling them added to meta
	 */
	private static  Map<String, Collection<Point>> handleComplex(
			final Map<Class<? extends IFixture>, BiConsumer<String, Point>> meta,
			final Class<? extends IFixture> cls, final String plural) {
		final  Map<String, Collection<Point>> retval = new HashMap<>();
		//noinspection StringConcatenationMissingWhitespace
		meta.put(cls, (kind, point) -> MultiMapHelper.getMapValue(retval, kind,
				key -> new PointList(key + plural)).add(point));
		return retval;
	}
	/**
	 * Produce the sub-report dealing with "immortals".
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
		final Map<Class<? extends IFixture>, BiConsumer<String, Point>> meta =
				new HashMap<>();
		final Map<SimpleImmortal.SimpleImmortalKind, HeadedList<Point>> simples =
				new EnumMap<>(SimpleImmortal.SimpleImmortalKind.class);
		for (final SimpleImmortal.SimpleImmortalKind kind : SimpleImmortal.SimpleImmortalKind.values()) {
			simples.put(kind, new PointList(kind.plural() + " at:"));
		}
		meta.put(SimpleImmortal.class,
				(s, point) -> simples.get(SimpleImmortal.SimpleImmortalKind.parse(s))
									  .add(point));
		final Map<String, Collection<Point>> centaurs =
				handleComplex(meta, Centaur.class, "(s) at ");
		final Map<String, Collection<Point>> giants =
				handleComplex(meta, Giant.class, "(s) at ");
		final Map<String, Collection<Point>> fairies =
				handleComplex(meta, Fairy.class, " at ");
		final Map<String, Collection<Point>> dragons =
				handleComplex(meta, Dragon.class, "(s) at ");
		for (final Pair<Point, IFixture> pair : values) {
			final Point point = pair.first();
			final IFixture immortal = pair.second();
			if (meta.containsKey(immortal.getClass())) {
				meta.get(immortal.getClass()).accept(immortal.toString(), point);
				fixtures.remove(Integer.valueOf(immortal.getID()));
			}
		}
		if (!centaurs.isEmpty() && !giants.isEmpty() && !fairies.isEmpty() &&
					!dragons.isEmpty() && !simples.isEmpty()) {
			ostream.format("<h4>Immortals</h4>%n");
			for (final Collection<? extends Collection<Point>> coll : Arrays.asList(centaurs.values(),
					giants.values(), fairies.values(), dragons.values(),
					simples.values())) {
				for (final Collection<Point> inner : coll) {
					ostream.format("%s", inner);
				}
			}
		}
	}

	/**
	 * Produce the sub-report dealing with "immortals".
	 *
	 * @param fixtures      the set of fixtures
	 * @param map           ignored
	 * @param currentPlayer the player for whom the report is being produced
	 * @return the part of the report listing "immortals"
	 */
	@Override
	public IReportNode produceRIR(final PatientMap<Integer, Pair<Point, IFixture>>
										  fixtures,
								  final IMapNG map, final Player currentPlayer) {
		final List<Pair<Point, IFixture>> values = new ArrayList<>(fixtures.values());
		values.sort(pairComparator);
		final Map<SimpleImmortal.SimpleImmortalKind, IReportNode> simples =
				new EnumMap<>(SimpleImmortal.SimpleImmortalKind.class);
		final Map<String, IReportNode> centaurs = new HashMap<>();
		final Map<String, IReportNode> giants = new HashMap<>();
		final Map<String, IReportNode> fairies = new HashMap<>();
		final Map<String, IReportNode> dragons = new HashMap<>();
		for (final Pair<Point, IFixture> pair : values) {
			final Point point = pair.first();
			final IFixture immortal = pair.second();
			if (immortal instanceof Dragon) {
				separateByKindRIR(dragons, (Dragon) immortal)
						.add(produceRIR(fixtures, map, currentPlayer,
								(Immortal) immortal, point));
			} else if (immortal instanceof Fairy) {
				separateByKindRIR(fairies, (Fairy) immortal)
						.add(produceRIR(fixtures, map, currentPlayer,
								(Immortal) immortal, point));
			} else if (immortal instanceof SimpleImmortal) {
				MultiMapHelper.getMapValue(simples, ((SimpleImmortal) immortal).kind(),
						kind -> new ListReportNode(kind.plural()))
						.add(produceRIR(fixtures, map, currentPlayer,
								(Immortal) immortal, point));
			} else if (immortal instanceof Giant) {
				separateByKindRIR(giants, (Giant) immortal)
						.add(produceRIR(fixtures, map, currentPlayer,
								(Immortal) immortal, point));
			} else if (immortal instanceof Centaur) {
				separateByKindRIR(centaurs, (Centaur) immortal)
						.add(produceRIR(fixtures, map, currentPlayer,
								(Immortal) immortal, point));
			}
		}
		final IReportNode retval = new SectionListReportNode(4, "Immortals");
		simples.values().forEach(retval::addIfNonEmpty);
		retval.addIfNonEmpty(coalesce("Dragons", dragons),
				coalesce("Fairies", fairies),
				coalesce("Giants", giants),
				coalesce("Centaurs", centaurs));
		if (retval.getChildCount() == 0) {
			return EmptyReportNode.NULL_NODE;
		} else {
			return retval;
		}
	}

	/**
	 * Produce a report on an individual immortal.
	 * @param fixtures      The set of fixtures
	 * @param map           ignored
	 * @param currentPlayer the current player
	 * @param item          a fixture
	 * @param loc           its location
	 * @param ostream	    the Formatter to write to
	 */
	@Override
	public void produce(final PatientMap<Integer, Pair<Point, IFixture>> fixtures,
						final IMapNG map, final Player currentPlayer,
						final Immortal item, final Point loc, final Formatter ostream) {
		fixtures.remove(Integer.valueOf(item.getID()));
		ostream.format("%sA(n) %s %s", atPoint(loc), item.toString(),
				distCalculator.distanceString(loc));
	}

	/**
	 * Produce a report node on an individual fixture.
	 * @param fixtures      The set of fixtures
	 * @param map           ignored
	 * @param currentPlayer the current player
	 * @param item          a fixture
	 * @param loc           its location
	 * @return a sub-sub-report on just that fixture, or null if it's not one we handle
	 * here.
	 */
	@Override
	public IReportNode produceRIR(final PatientMap<Integer, Pair<Point, IFixture>>
										  fixtures,
								  final IMapNG map, final Player currentPlayer,
								  final Immortal item, final Point loc) {
		if (item instanceof Immortal) {
			fixtures.remove(Integer.valueOf(item.getID()));
			return new SimpleReportNode(loc, atPoint(loc), "A(n) ", item.toString(), " ",
											   distCalculator.distanceString(loc));
		} else {
			return EmptyReportNode.NULL_NODE;
		}
	}

	/**
	 * A trivial toString().
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "ImmortalsReportGenerator";
	}
}
