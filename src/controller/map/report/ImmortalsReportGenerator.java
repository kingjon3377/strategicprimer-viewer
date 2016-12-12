package controller.map.report;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import model.map.HasKind;
import model.map.IFixture;
import model.map.IMapNG;
import model.map.Player;
import model.map.Point;
import model.map.fixtures.mobile.Centaur;
import model.map.fixtures.mobile.Djinn;
import model.map.fixtures.mobile.Dragon;
import model.map.fixtures.mobile.Fairy;
import model.map.fixtures.mobile.Giant;
import model.map.fixtures.mobile.Griffin;
import model.map.fixtures.mobile.Minotaur;
import model.map.fixtures.mobile.MobileFixture;
import model.map.fixtures.mobile.Ogre;
import model.map.fixtures.mobile.Phoenix;
import model.map.fixtures.mobile.Simurgh;
import model.map.fixtures.mobile.Sphinx;
import model.map.fixtures.mobile.Troll;
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
		extends AbstractReportGenerator<MobileFixture> {
	/**
	 * @param comparator a comparator for pairs of Points and fixtures.
	 */
	public ImmortalsReportGenerator(final Comparator<@NonNull Pair<@NonNull Point,
																		  @NonNull
																				  IFixture>> comparator) {
		super(comparator);
	}

	/**
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
	 * TODO: Create Immortal marker interface.
	 *
	 * @param item a fixture
	 * @return whether it's an immortal
	 */
	private static boolean isImmortal(final MobileFixture item) {
		return (item instanceof Dragon) || (item instanceof Fairy)
					   || (item instanceof Troll) || (item instanceof Djinn)
					   || (item instanceof Sphinx) || (item instanceof Giant)
					   || (item instanceof Minotaur) || (item instanceof Ogre)
					   || (item instanceof Centaur) || (item instanceof Phoenix)
					   || (item instanceof Simurgh) || (item instanceof Griffin);
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
	 * @param meta the mapping from types to Consumers
	 * @param cls a type of fixture
	 * @param heading the heading to put at the top of the list
	 * @return the list for that kind of fixture, with a reference to its add() method added to meta.
	 */
	private static HeadedList<Point> handleSimple(final Map<Class<? extends IFixture>,
																   BiConsumer<String,
																					 Point>> meta,
												  final Class<? extends IFixture> cls,
												  final String heading) {
		final HeadedList<Point> retval = new PointList(heading);
		meta.put(cls, (kind, point) -> retval.add(point));
		return retval;
	}
	/**
	 * @param meta the mapping from types to Consumers
	 * @param cls a type of fixture
	 * @param plural the pluralization to add after each list
	 * @return the collection of lists for that kind of fixture, with a Consumer
	 * handling them added to meta
	 */
	private static  Map<String, Collection<Point>> handleComplex(final Map<Class<? extends IFixture>,
																   BiConsumer<String,
																					 Point>> meta,
												  final Class<? extends IFixture> cls,
												  final String plural) {
		final  Map<String, Collection<Point>> retval = new HashMap<>();
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
	 * @return the part of the report listing "immortals"
	 */
	@Override
	public String produce(final PatientMap<Integer, Pair<Point, IFixture>> fixtures,
						  final IMapNG map, final Player currentPlayer) {
		final List<Pair<Point, IFixture>> values = new ArrayList<>(fixtures.values());
		values.sort(pairComparator);
		final Map<Class<? extends IFixture>, BiConsumer<String, Point>> meta =
				new HashMap<>();
		final HeadedList<Point> griffins =
				handleSimple(meta, Griffin.class, "Griffin(s) at ");
		final HeadedList<Point> simurghs =
				handleSimple(meta, Simurgh.class, "Simurgh(s) at ");
		final HeadedList<Point> phoenixes =
				handleSimple(meta, Phoenix.class, "Phoenix(es) at ");
		final Map<String, Collection<Point>> centaurs =
				handleComplex(meta, Centaur.class, "(s) at ");
		final HeadedList<Point> ogres = handleSimple(meta, Ogre.class, "Ogre(s) at ");
		final HeadedList<Point> minotaurs =
				handleSimple(meta, Minotaur.class, "Minotaur(s) at ");
		final Map<String, Collection<Point>> giants =
				handleComplex(meta, Giant.class, "(s) at ");
		final HeadedList<Point> sphinxes =
				handleSimple(meta, Sphinx.class, "Sphinx(es) at ");
		final HeadedList<Point> djinni = handleSimple(meta, Djinn.class, "Djinn(i) at ");
		final HeadedList<Point> trolls = handleSimple(meta, Troll.class,"Troll(s) at ");
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
		final HeadedList<String> retval = new HtmlList("<h4>Immortals</h4>");
		Stream.of(dragons, fairies, giants, centaurs).flatMap(m -> m.values().stream())
				.map(Collection::toString).forEach(retval::add);
		Stream.of(trolls, djinni, sphinxes, minotaurs, ogres, phoenixes, simurghs,
				griffins).map(Collection::toString).forEach(retval::add);
		return retval.toString();
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
		final IReportNode griffins = new ListReportNode("Griffins");
		final IReportNode simurghs = new ListReportNode("Simurghs");
		final IReportNode phoenixes = new ListReportNode("Phoenixes");
		final Map<String, IReportNode> centaurs = new HashMap<>();
		final IReportNode ogres = new ListReportNode("Ogres");
		final IReportNode minotaurs = new ListReportNode("Minotaurs");
		final Map<String, IReportNode> giants = new HashMap<>();
		final IReportNode sphinxes = new ListReportNode("Sphinxes");
		final IReportNode djinni = new ListReportNode("Djinni");
		final IReportNode trolls = new ListReportNode("Trolls");
		final Map<String, IReportNode> fairies = new HashMap<>();
		final Map<String, IReportNode> dragons = new HashMap<>();
		for (final Pair<Point, IFixture> pair : values) {
			final Point point = pair.first();
			final IFixture immortal = pair.second();
			if (immortal instanceof Dragon) {
				separateByKindRIR(dragons, (Dragon) immortal)
						.add(produceRIR(fixtures, map, currentPlayer,
								(MobileFixture) immortal, point));
			} else if (immortal instanceof Fairy) {
				separateByKindRIR(fairies, (Fairy) immortal)
						.add(produceRIR(fixtures, map, currentPlayer,
								(MobileFixture) immortal, point));
			} else if (immortal instanceof Troll) {
				trolls.add(produceRIR(fixtures, map, currentPlayer,
						(MobileFixture) immortal, point));
			} else if (immortal instanceof Djinn) {
				djinni.add(produceRIR(fixtures, map, currentPlayer,
						(MobileFixture) immortal, point));
			} else if (immortal instanceof Sphinx) {
				sphinxes.add(produceRIR(fixtures, map, currentPlayer,
						(MobileFixture) immortal, point));
			} else if (immortal instanceof Giant) {
				separateByKindRIR(giants, (Giant) immortal)
						.add(produceRIR(fixtures, map, currentPlayer,
								(MobileFixture) immortal, point));
			} else if (immortal instanceof Minotaur) {
				minotaurs.add(produceRIR(fixtures, map, currentPlayer,
						(MobileFixture) immortal, point));
			} else if (immortal instanceof Ogre) {
				ogres.add(produceRIR(fixtures, map, currentPlayer,
						(MobileFixture) immortal, point));
			} else if (immortal instanceof Centaur) {
				separateByKindRIR(centaurs, (Centaur) immortal)
						.add(produceRIR(fixtures, map, currentPlayer,
								(MobileFixture) immortal, point));
			} else if (immortal instanceof Phoenix) {
				phoenixes.add(produceRIR(fixtures, map, currentPlayer,
						(MobileFixture) immortal, point));
			} else if (immortal instanceof Simurgh) {
				simurghs.add(produceRIR(fixtures, map, currentPlayer,
						(MobileFixture) immortal, point));
			} else if (immortal instanceof Griffin) {
				griffins.add(produceRIR(fixtures, map, currentPlayer,
						(MobileFixture) immortal, point));
			}
		}
		final IReportNode retval = new SectionListReportNode(4, "Immortals");
		retval.addIfNonEmpty(coalesce("Dragons", dragons),
				coalesce("Fairies", fairies), trolls, djinni, sphinxes,
				coalesce("Giants", giants), minotaurs, ogres,
				coalesce("Centaurs", centaurs), phoenixes, simurghs, griffins);
		if (retval.getChildCount() == 0) {
			return EmptyReportNode.NULL_NODE;
		} else {
			return retval;
		}
	}

	/**
	 * @param fixtures      The set of fixtures
	 * @param map           ignored
	 * @param currentPlayer the current player
	 * @param item          a fixture
	 * @param loc           its location
	 * @return a sub-sub-report on just that fixture, or the empty string if it's not one
	 * we handle here.
	 */
	@Override
	public String produce(final PatientMap<Integer, Pair<Point, IFixture>> fixtures,
						  final IMapNG map, final Player currentPlayer,
						  final MobileFixture item, final Point loc) {
		if (isImmortal(item)) {
			fixtures.remove(Integer.valueOf(item.getID()));
			return concat(atPoint(loc), "A(n) ", item.toString(), " ",
					distCalculator.distanceString(loc));
		} else {
			return "";
		}
	}

	/**
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
								  final MobileFixture item, final Point loc) {
		if (isImmortal(item)) {
			fixtures.remove(Integer.valueOf(item.getID()));
			return new SimpleReportNode(loc, atPoint(loc), "A(n) ", item.toString(), " ",
											   distCalculator.distanceString(loc));
		} else {
			return EmptyReportNode.NULL_NODE;
		}
	}

	/**
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "ImmortalsReportGenerator";
	}
}
