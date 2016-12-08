package controller.map.report;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
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
import util.LineEnd;
import util.NullCleaner;
import util.Pair;
import util.PatientMap;
import util.SimpleMultiMap;

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
	 * @param collections a list of collections
	 * @return their total size
	 */
	private static int collSize(final Collection<?>... collections) {
		return Stream.of(collections).mapToInt(Collection::size).sum();
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
	 * Prints (to the builder) nothing if the map is empty, or for each entry in the
	 * entry
	 * set a list item beginning with the key, followed by the infix, followed by a
	 * comma-separated list of the points.
	 *
	 * @param mapping the mapping from kinds (or whatever) to lists of points
	 * @param infix   what to print in the middle of each item
	 * @param builder the builder to print to
	 */
	private static void optionallyPrintMap(final Map<String, Collection<Point>> mapping,
										   final String infix,
										   final StringBuilder builder) {
		for (final Map.Entry<String, Collection<Point>> entry : mapping.entrySet()) {
			builder.append(OPEN_LIST_ITEM).append(entry.getKey()).append(infix);
			pointCSL(builder, entry.getValue().stream().collect(Collectors.toList()));
			builder.append(CLOSE_LIST_ITEM);
		}
	}

	/**
	 * Prints (to the builder) nothing if the list is empty, or the prefix followed by a
	 * comma-separated list of the points, all enclosed in a list item.
	 *
	 * @param points  a list of points
	 * @param prefix  what to prepend to it if non-empty
	 * @param builder the builder to print to
	 */
	private static void optionallyPrintList(final List<Point> points,
											final String prefix,
											final StringBuilder builder) {
		if (!points.isEmpty()) {
			builder.append(OPEN_LIST_ITEM).append(prefix);
			pointCSL(builder, points);
			builder.append(CLOSE_LIST_ITEM);
		}
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
		if (mapping.containsKey(item.toString())) {
			return NullCleaner.assertNotNull(mapping.get(item.toString()));
		} else {
			final IReportNode retval =
					new ListReportNode(NullCleaner.assertNotNull(item.toString()));
			mapping.put(NullCleaner.assertNotNull(item.toString()), retval);
			return retval;
		}
	}
	/**
	 * @param list a list
	 * @return its add() method as a BiConsumer of Strings and Points.
	 */
	private static BiConsumer<String, Point> simplest(final List<Point> list) {
		return (kind, point) -> list.add(point);
	}
	/**
	 * @param collection a collection of lists of points. Must be a SimpleMultiMap.
	 * @return a reference to a method that gets the right list and adds the point to it
	 */
	private static BiConsumer<String, Point> complex(final Map<String, Collection<Point>>
													  collection) {
		return (kind, point) -> collection.get(kind).add(point);
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
		final List<Point> griffins = new ArrayList<>();
		final List<Point> simurghs = new ArrayList<>();
		final List<Point> phoenixes = new ArrayList<>();
		final Map<String, Collection<Point>> centaurs = new SimpleMultiMap<>();
		final List<Point> ogres = new ArrayList<>();
		final List<Point> minotaurs = new ArrayList<>();
		final Map<String, Collection<Point>> giants = new SimpleMultiMap<>();
		final List<Point> sphinxes = new ArrayList<>();
		final List<Point> djinni = new ArrayList<>();
		final List<Point> trolls = new ArrayList<>();
		final Map<String, Collection<Point>> fairies = new SimpleMultiMap<>();
		final Map<String, Collection<Point>> dragons = new SimpleMultiMap<>();
		final Map<Class<? extends IFixture>, BiConsumer<String, Point>> meta =
				new HashMap<>();
		meta.put(Dragon.class, complex(dragons));
		meta.put(Fairy.class, complex(fairies));
		meta.put(Troll.class, simplest(trolls));
		meta.put(Djinn.class, simplest(djinni));
		meta.put(Sphinx.class, simplest(sphinxes));
		meta.put(Giant.class, complex(giants));
		meta.put(Minotaur.class, simplest(minotaurs));
		meta.put(Ogre.class, simplest(ogres));
		meta.put(Centaur.class, complex(centaurs));
		meta.put(Phoenix.class, simplest(phoenixes));
		meta.put(Simurgh.class, simplest(simurghs));
		meta.put(Griffin.class, simplest(griffins));
		for (final Pair<Point, IFixture> pair : values) {
			final Point point = pair.first();
			final IFixture immortal = pair.second();
			if (meta.containsKey(immortal.getClass())) {
				meta.get(immortal.getClass()).accept(immortal.toString(), point);
				fixtures.remove(Integer.valueOf(immortal.getID()));
			}
		}
		final int totalSize = collSize(dragons.keySet(), griffins,
				fairies.keySet(), giants.keySet(), centaurs.keySet(), trolls,
				djinni, sphinxes, minotaurs, ogres, phoenixes, simurghs);
		if (totalSize == 0) {
			return "";
		} else {
			final StringBuilder builder = new StringBuilder(36 + (512 * totalSize));
			builder.append("<h4>Immortals</h4>").append(LineEnd.LINE_SEP).append(OPEN_LIST);
			optionallyPrintMap(dragons, "(s) at ", builder);
			optionallyPrintMap(fairies, " at ", builder);
			optionallyPrintList(trolls, "Troll(s) at ", builder);
			optionallyPrintList(djinni, "Djinn(i) at ", builder);
			optionallyPrintList(sphinxes, "Sphinx(es) at ", builder);
			optionallyPrintMap(giants, "(s) at ", builder);
			optionallyPrintList(minotaurs, "Minotaur(s) at ", builder);
			optionallyPrintList(ogres, "Ogre(s) at ", builder);
			optionallyPrintMap(centaurs, "(s) at ", builder);
			optionallyPrintList(phoenixes, "Phoenix(es) at ", builder);
			optionallyPrintList(simurghs, "Simurgh(s) at ", builder);
			optionallyPrintList(griffins, "Griffin(s) at ", builder);
			return builder.append(CLOSE_LIST).toString();
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
