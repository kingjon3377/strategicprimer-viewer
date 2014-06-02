package controller.map.report;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import model.map.HasKind;
import model.map.IFixture;
import model.map.ITileCollection;
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
import model.report.SectionListReportNode;
import model.report.SimpleReportNode;
import util.DelayedRemovalMap;
import util.NullCleaner;
import util.Pair;

/**
 * A report generator for "immortals"---dragons, fairies, centaurs, and such.
 *
 * @author Jonathan Lovelace
 *
 */
public class ImmortalsReportGenerator extends
		AbstractReportGenerator<MobileFixture> {
	/** // $codepro.audit.disable sourceLength
	 * Produce the sub-report dealing with "immortals".
	 *
	 * @param fixtures the set of fixtures
	 * @param tiles ignored
	 * @param currentPlayer the player for whom the report is being produced
	 * @return the part of the report listing "immortals"
	 */
	@Override
	public String produce(// $codepro.audit.disable cyclomaticComplexity
			final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
			final ITileCollection tiles, final Player currentPlayer) {
		final Map<String, List<Point>> dragons = new HashMap<>();
		final Map<String, List<Point>> fairies = new HashMap<>();
		final List<Point> trolls = new ArrayList<>();
		final List<Point> djinni = new ArrayList<>();
		final List<Point> sphinxes = new ArrayList<>();
		final Map<String, List<Point>> giants = new HashMap<>();
		final List<Point> minotaurs = new ArrayList<>();
		final List<Point> ogres = new ArrayList<>();
		final Map<String, List<Point>> centaurs = new HashMap<>();
		final List<Point> phoenixes = new ArrayList<>();
		final List<Point> simurghs = new ArrayList<>();
		final List<Point> griffins = new ArrayList<>();

		for (final Pair<Point, IFixture> pair : fixtures.values()) {
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
		int total = 0;
		for (final Collection<?> coll : collections) {
			total += coll.size();
		}
		return total;
	}
	/**
	 * Produce the sub-report dealing with "immortals".
	 *
	 * @param fixtures the set of fixtures
	 * @param tiles ignored
	 * @param currentPlayer the player for whom the report is being produced
	 * @return the part of the report listing "immortals"
	 */
	@Override
	public AbstractReportNode produceRIR(
			final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
			final ITileCollection tiles, final Player currentPlayer) {
		final AbstractReportNode retval = new SectionListReportNode(4,
				"Immortals");
		final Map<String, List<Point>> dragons = new HashMap<>();
		final Map<String, List<Point>> fairies = new HashMap<>();
		final List<Point> trolls = new ArrayList<>();
		final List<Point> djinni = new ArrayList<>();
		final List<Point> sphinxes = new ArrayList<>();
		final Map<String, List<Point>> giants = new HashMap<>();
		final List<Point> minotaurs = new ArrayList<>();
		final List<Point> ogres = new ArrayList<>();
		final Map<String, List<Point>> centaurs = new HashMap<>();
		final List<Point> phoenixes = new ArrayList<>();
		final List<Point> simurghs = new ArrayList<>();
		final List<Point> griffins = new ArrayList<>();

		for (final Pair<Point, IFixture> pair : fixtures.values()) {
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
				fixtures.remove(Integer.valueOf(immortal.getID()));
				phoenixes.add(point);
			} else if (immortal instanceof Simurgh) {
				fixtures.remove(Integer.valueOf(immortal.getID()));
				simurghs.add(point);
			} else if (immortal instanceof Griffin) {
				griffins.add(point);
				fixtures.remove(Integer.valueOf(immortal.getID()));
			}
		}
		optionallyAddRIR(dragons, "(s) at ", retval);
		optionallyAddRIR(fairies, " at ", retval);
		optionallyAdd(trolls, "Troll(s) at ", retval);
		optionallyAdd(djinni, "Djinn(i) at ", retval);
		optionallyAdd(sphinxes, "Sphinx(es) at ", retval);
		optionallyAddRIR(giants, "(s) at ", retval);
		optionallyAdd(minotaurs, "Minotaur(s) at ", retval);
		optionallyAdd(ogres, "Ogre(s) at ", retval);
		optionallyAddRIR(centaurs, "(s) at ", retval);
		optionallyAdd(phoenixes, "Phoenix(es) at ", retval);
		optionallyAdd(simurghs, "Simurgh(s) at ", retval);
		optionallyAdd(griffins, "Griffin(s) at ", retval);
		if (retval.getChildCount() == 0) {
			return EmptyReportNode.NULL_NODE; // NOPMD
		} else {
			return retval;
		}
	}

	/**
	 * @param fixtures The set of fixtures
	 * @param tiles ignored
	 * @param currentPlayer the current player
	 * @param item a fixture
	 * @param loc its location
	 * @return a sub-sub-report on just that fixture, or the empty string if
	 *         it's not one we handle here.
	 */
	@Override
	public String produce(// $codepro.audit.disable cyclomaticComplexity
			final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
			final ITileCollection tiles, final Player currentPlayer,
			final MobileFixture item, final Point loc) {
		//  TODO: Create Immortal marker interface
		if (item instanceof Dragon || item instanceof Fairy
				|| item instanceof Troll || item instanceof Djinn
				|| item instanceof Sphinx || item instanceof Giant
				|| item instanceof Minotaur || item instanceof Ogre
				|| item instanceof Centaur || item instanceof Phoenix
				|| item instanceof Simurgh || item instanceof Griffin) {
			fixtures.remove(Integer.valueOf(item.getID()));
			return concat(atPoint(loc), "A(n) ", item.toString());
		} else {
			return "";
		}
	}

	/**
	 * @param fixtures The set of fixtures
	 * @param tiles ignored
	 * @param currentPlayer the current player
	 * @param item a fixture
	 * @param loc its location
	 * @return a sub-sub-report on just that fixture, or null if it's not one we
	 *         handle here.
	 */
	@Override
	public AbstractReportNode produceRIR(
			final DelayedRemovalMap<Integer, Pair<Point, IFixture>> fixtures,
			final ITileCollection tiles, final Player currentPlayer,
			final MobileFixture item, final Point loc) {
		// TODO: Create Immortal marker interface
		if (item instanceof Dragon || item instanceof Fairy
				|| item instanceof Troll || item instanceof Djinn
				|| item instanceof Sphinx || item instanceof Giant
				|| item instanceof Minotaur || item instanceof Ogre
				|| item instanceof Centaur || item instanceof Phoenix
				|| item instanceof Simurgh || item instanceof Griffin) {
			fixtures.remove(Integer.valueOf(item.getID()));
			return new SimpleReportNode(atPoint(loc), "A(n) ", item.toString());
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
	 * Add to the parent node nothing if the map is empty, or for each entry in
	 * the entry set a simple node containing the key plus the infix plus a
	 * comma-separated list of the points.
	 *
	 * @param parent the parent node
	 * @param mapping the mapping from kinds (or whatever) to lists of points
	 * @param infix what to print in the middle of each item
	 */
	private static void optionallyAddRIR(final Map<String, List<Point>> mapping,
			final String infix, final AbstractReportNode parent) {
		for (final Entry<String, List<Point>> entry : mapping.entrySet()) {
			parent.add(new SimpleReportNode(entry.getKey(), infix, //NOPMD
					pointCSL(entry.getValue())));
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
	 * Add to the parent node nothing if the list is empty, or a simple node of
	 * the prefix followed by a comma-separated list of all the points if it is
	 * not.
	 *
	 * @param parent the parent to add the item to.
	 * @param points a list of points
	 * @param prefix what to prepend to it if non-empty
	 */
	private static void optionallyAdd(final List<Point> points,
			final String prefix, final AbstractReportNode parent) {
		if (!points.isEmpty()) {
			parent.add(new SimpleReportNode(prefix, pointCSL(points)));
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
		// ESCA-JAVA0177:
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
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "ImmortalsReportGenerator";
	}
}
