package controller.map.report;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;

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
import model.report.AbstractReportNode;
import model.report.EmptyReportNode;
import model.report.ListReportNode;
import model.report.SectionListReportNode;
import model.report.SimpleReportNode;
import util.DelayedRemovalMap;
import util.NullCleaner;
import util.Pair;

/**
 * A report generator for "immortals"---dragons, fairies, centaurs, and such.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2013-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of version 3 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 *
 */
public final class ImmortalsReportGenerator extends AbstractReportGenerator<MobileFixture> {
	/**
	 * @param comparator a comparator for pairs of Points and fixtures.
	 */
	public ImmortalsReportGenerator(final Comparator<@NonNull Pair<@NonNull Point, @NonNull IFixture>> comparator) {
		super(comparator);
	}
	/** // $codepro.audit.disable sourceLength
	 * Produce the sub-report dealing with "immortals".
	 *
	 * @param fixtures the set of fixtures
	 * @param map ignored
	 * @param currentPlayer the player for whom the report is being produced
	 * @return the part of the report listing "immortals"
	 */
	@Override
	public String produce(// $codepro.audit.disable cyclomaticComplexity
			final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
			final IMapNG map, final Player currentPlayer) {

		final List<Pair<Point, IFixture>> values = new ArrayList<>(fixtures.values());
		Collections.sort(values, pairComparator);
		final List<Point> griffins = new ArrayList<>();
		final List<Point> simurghs = new ArrayList<>();
		final List<Point> phoenixes = new ArrayList<>();
		final Map<String, List<Point>> centaurs = new HashMap<>();
		final List<Point> ogres = new ArrayList<>();
		final List<Point> minotaurs = new ArrayList<>();
		final Map<String, List<Point>> giants = new HashMap<>();
		final List<Point> sphinxes = new ArrayList<>();
		final List<Point> djinni = new ArrayList<>();
		final List<Point> trolls = new ArrayList<>();
		final Map<String, List<Point>> fairies = new HashMap<>();
		final Map<String, List<Point>> dragons = new HashMap<>();
		for (final Pair<Point, IFixture> pair : values) {
			final Point point = pair.first();
			final IFixture immortal = pair.second();
			if (immortal instanceof Dragon) {
				separateByKind(dragons, (Dragon) immortal, point);
				fixtures.remove(Integer.valueOf(immortal.getID()));
			} else if (immortal instanceof Fairy) {
				separateByKind(fairies, (Fairy) immortal, point);
				fixtures.remove(Integer.valueOf(immortal.getID()));
			} else if (immortal instanceof Troll) {
				trolls.add(point);
				fixtures.remove(Integer.valueOf(immortal.getID()));
			} else if (immortal instanceof Djinn) {
				djinni.add(point);
				fixtures.remove(Integer.valueOf(immortal.getID()));
			} else if (immortal instanceof Sphinx) {
				sphinxes.add(point);
				fixtures.remove(Integer.valueOf(immortal.getID()));
			} else if (immortal instanceof Giant) {
				separateByKind(giants, (Giant) immortal, point);
				fixtures.remove(Integer.valueOf(immortal.getID()));
			} else if (immortal instanceof Minotaur) {
				minotaurs.add(point);
				fixtures.remove(Integer.valueOf(immortal.getID()));
			} else if (immortal instanceof Ogre) {
				ogres.add(point);
				fixtures.remove(Integer.valueOf(immortal.getID()));
			} else if (immortal instanceof Centaur) {
				separateByKind(centaurs, (Centaur) immortal, point);
				fixtures.remove(Integer.valueOf(immortal.getID()));
			} else if (immortal instanceof Phoenix) {
				phoenixes.add(point);
				fixtures.remove(Integer.valueOf(immortal.getID()));
			} else if (immortal instanceof Simurgh) {
				simurghs.add(point);
				fixtures.remove(Integer.valueOf(immortal.getID()));
			} else if (immortal instanceof Griffin) {
				griffins.add(point);
				fixtures.remove(Integer.valueOf(immortal.getID()));
			}
		}
		final int totalSize = collSize(dragons.keySet(), griffins,
				fairies.keySet(), giants.keySet(), centaurs.keySet(), trolls,
				djinni, sphinxes, minotaurs, ogres, phoenixes, simurghs);
		final int len = 36 + 512 * totalSize;
		final StringBuilder builder = new StringBuilder(len);
		builder.append("<h4>Immortals</h4>\n").append(OPEN_LIST);
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
		builder.append(CLOSE_LIST);
		if (totalSize == 0) {
			return ""; // NOPMD
		} else {
			return NullCleaner.assertNotNull(builder.toString());
		}
	}

	/**
	 * @param collections a list of collections
	 * @return their total size
	 */
	private static int collSize(final Collection<?>... collections) {
		return Stream.of(collections).collect(Collectors.summingInt(Collection::size)).intValue();
	}
	/**
	 * Produce the sub-report dealing with "immortals".
	 *
	 * @param fixtures the set of fixtures
	 * @param map ignored
	 * @param currentPlayer the player for whom the report is being produced
	 * @return the part of the report listing "immortals"
	 */
	@Override
	public AbstractReportNode produceRIR(
			final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
			final IMapNG map, final Player currentPlayer) {
		final List<Pair<Point, IFixture>> values = new ArrayList<>(fixtures.values());
		Collections.sort(values, pairComparator);
		final AbstractReportNode griffins = new ListReportNode("Griffins");
		final AbstractReportNode simurghs = new ListReportNode("Simurghs");
		final AbstractReportNode phoenixes = new ListReportNode("Phoenixes");
		final Map<String, AbstractReportNode> centaurs = new HashMap<>();
		final AbstractReportNode ogres = new ListReportNode("Ogres");
		final AbstractReportNode minotaurs = new ListReportNode("Minotaurs");
		final Map<String, AbstractReportNode> giants = new HashMap<>();
		final AbstractReportNode sphinxes = new ListReportNode("Sphinxes");
		final AbstractReportNode djinni = new ListReportNode("Djinni");
		final AbstractReportNode trolls = new ListReportNode("Trolls");
		final Map<String, AbstractReportNode> fairies = new HashMap<>();
		final Map<String, AbstractReportNode> dragons = new HashMap<>();
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
		final AbstractReportNode retval = new SectionListReportNode(4,
				                                                           "Immortals");
		optionallyAdd(retval, coalesce("Dragons", dragons),
				coalesce("Fairies", fairies), trolls, djinni, sphinxes,
				coalesce("Giants", giants), minotaurs, ogres,
				coalesce("Centaurs", centaurs), phoenixes, simurghs, griffins);
		if (retval.getChildCount() == 0) {
			return EmptyReportNode.NULL_NODE; // NOPMD
		} else {
			return retval;
		}
	}
	/**
	 * @param header the heading to put above the children
	 * @param mapping a mapping from kinds to nodes
	 * @return a node with all of the nodes as children
	 */
	private static AbstractReportNode coalesce(final String header,
			final Map<String, AbstractReportNode> mapping) {
		final AbstractReportNode retval = new ListReportNode(header);
		mapping.values().forEach(retval::add);
		return retval;
	}
	/**
	 * @param parent a node
	 * @param children possible children to add, if they have children of their own
	 */
	private static void optionallyAdd(final AbstractReportNode parent,
			final AbstractReportNode... children) {
		Stream.of(children).filter(child -> child.getChildCount() > 0).forEach(parent::add);
	}
	/**
	 * @param fixtures The set of fixtures
	 * @param map ignored
	 * @param currentPlayer the current player
	 * @param item a fixture
	 * @param loc its location
	 * @return a sub-sub-report on just that fixture, or the empty string if
	 *         it's not one we handle here.
	 */
	@Override
	public String produce(// $codepro.audit.disable cyclomaticComplexity
			final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
			final IMapNG map, final Player currentPlayer,
			final MobileFixture item, final Point loc) {
		//  TODO: Create Immortal marker interface
		if (item instanceof Dragon || item instanceof Fairy
				|| item instanceof Troll || item instanceof Djinn
				|| item instanceof Sphinx || item instanceof Giant
				|| item instanceof Minotaur || item instanceof Ogre
				|| item instanceof Centaur || item instanceof Phoenix
				|| item instanceof Simurgh || item instanceof Griffin) {
			fixtures.remove(Integer.valueOf(item.getID()));
			return concat(atPoint(loc), "A(n) ", item.toString(), " ", distCalculator.distanceString(loc));
		} else {
			return "";
		}
	}

	/**
	 * @param fixtures The set of fixtures
	 * @param map ignored
	 * @param currentPlayer the current player
	 * @param item a fixture
	 * @param loc its location
	 * @return a sub-sub-report on just that fixture, or null if it's not one we
	 *         handle here.
	 */
	@Override
	public AbstractReportNode produceRIR(
			final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
			final IMapNG map, final Player currentPlayer,
			final MobileFixture item, final Point loc) {
		// TODO: Create Immortal marker interface
		if (item instanceof Dragon || item instanceof Fairy
				|| item instanceof Troll || item instanceof Djinn
				|| item instanceof Sphinx || item instanceof Giant
				|| item instanceof Minotaur || item instanceof Ogre
				|| item instanceof Centaur || item instanceof Phoenix
				|| item instanceof Simurgh || item instanceof Griffin) {
			fixtures.remove(Integer.valueOf(item.getID()));
			return new SimpleReportNode(loc, atPoint(loc), "A(n) ", item.toString(), " ", distCalculator.distanceString(loc));
		} else {
			return EmptyReportNode.NULL_NODE;
		}
	}

	/**
	 * Prints (to the builder) nothing if the map is empty, or for each entry in
	 * the entry set a list item beginning with the key, followed by the infix,
	 * followed by a comma-separated list of the points.
	 *
	 * @param mapping the mapping from kinds (or whatever) to lists of points
	 * @param infix what to print in the middle of each item
	 * @param builder the builder to print to
	 */
	private static void optionallyPrintMap(final Map<String, List<Point>> mapping,
			final String infix, final StringBuilder builder) {
		for (final Entry<String, List<Point>> entry : mapping.entrySet()) {
			builder.append(OPEN_LIST_ITEM).append(entry.getKey()).append(infix)
			.append(pointCSL(entry.getValue())).append(CLOSE_LIST_ITEM);
		}
	}

	/**
	 * Prints (to the builder) nothing if the list is empty, or the prefix
	 * followed by a comma-separated list of the points, all enclosed in a list
	 * item.
	 *
	 * @param points a list of points
	 * @param prefix what to prepend to it if non-empty
	 * @param builder the builder to print to
	 */
	private static void optionallyPrintList(final List<Point> points,
			final String prefix, final StringBuilder builder) {
		if (!points.isEmpty()) {
			builder.append(OPEN_LIST_ITEM).append(prefix)
			.append(pointCSL(points)).append(CLOSE_LIST_ITEM);
		}
	}

	/**
	 * If there's an entry in the map for the thing's kind already, add the
	 * point to its list; if not, create such an entry and add the point to it.
	 *
	 * @param mapping the mapping we're dealing with
	 * @param item the item under consideration
	 * @param point its location in the map
	 */
	private static void separateByKind(final Map<String, List<Point>> mapping,
			final HasKind item, final Point point) {
		final List<Point> points; // NOPMD
		// For the three classes we deal with here, we don't want just the kind,
		// we want the full toString, so we use that instead of getKind.
		if (mapping.containsKey(item.toString())) {
			points = mapping.get(item.toString());
		} else {
			points = new ArrayList<>();
			mapping.put(item.toString(), points);
		}
		points.add(point);
	}
	/**
	 * If there's an entry in the map for the thing's kind already, return that
	 * entry; if not, create one, add it to the map, and return it..
	 *
	 * @param mapping
	 *            the mapping we're dealing with
	 * @param item
	 *            the item under consideration
	 * @return the entry in the map for the item's kind
	 */
	private static AbstractReportNode separateByKindRIR(
			final Map<String, AbstractReportNode> mapping,
			final HasKind item) {
		// For the three classes we deal with here, we don't want just the kind,
		// we want the full toString, so we use that instead of getKind.
		if (mapping.containsKey(item.toString())) {
			return NullCleaner.assertNotNull(mapping.get(item.toString()));
		} else {
			final AbstractReportNode retval = new ListReportNode(
					NullCleaner.assertNotNull(item.toString()));
			mapping.put(item.toString(), retval);
			return retval;
		}
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "ImmortalsReportGenerator";
	}
}
