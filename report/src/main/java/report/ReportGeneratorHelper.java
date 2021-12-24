package report;

import org.jetbrains.annotations.Nullable;
import org.javatuples.Pair;
import lovelace.util.DelayedRemovalMap;
import lovelace.util.IntMap;

import common.idreg.IDRegistrar;
import common.idreg.IDFactoryFiller;

import common.map.IFixture;
import common.map.Player;
import common.map.TileFixture;
import common.map.Point;
import common.map.IMapNG;

import common.map.fixtures.FixtureIterable;

import common.map.fixtures.towns.IFortress;

import java.util.Map;
import java.util.HashMap;

import java.util.function.ToIntFunction;
import java.util.function.BiConsumer;
import java.util.logging.Logger;

/**
 * An encapsulation of helper methods for report generators.
 */
/* package */ class ReportGeneratorHelper {
	private ReportGeneratorHelper() {
	}

	private static final Logger LOGGER = Logger.getLogger(ReportGeneratorHelper.class.getName());

	/**
	 * Find the location of the given player's HQ in the given map, or null if not found.
	 */
	@Nullable
	public static Point findHQ(IMapNG map, Player player) {
		Point retval = null;
		for (Point location : map.getLocations()) {
			for (TileFixture fixture : map.getFixtures(location)) {
				if (fixture instanceof IFortress &&
						player.equals(((IFortress) fixture).getOwner())) {
					if ("hq".equals(((IFortress) fixture).getName())) {
						return location;
					} else if (location.isValid() && retval == null) {
						retval = location;
					}
				}
			}
		}
		return retval;
	}

	private static void addToMap(Point location, IFixture fixture, IDRegistrar idf,
			DelayedRemovalMap<Integer, Pair<Point, IFixture>> mapping) {
		if (fixture instanceof TileFixture || fixture.getId() >= 0) {
			int key = checkID(idf, fixture);
			Pair<Point, IFixture> val = Pair.with(location, fixture);
			if (mapping.containsKey(key) && !val.equals(mapping.get(key))) {
				LOGGER.warning(String.format("Duplicate key, %d, for Pairs %s and %s",
					key, mapping.get(key), val));
			}
			mapping.put(key, val);
			if (fixture instanceof FixtureIterable) {
				for (IFixture inner : (FixtureIterable<?>) fixture) {
					addToMap(location, inner, idf, mapping);
				}
			}
		}
	}

	private static int checkID(IDRegistrar idf, IFixture fixture) {
		if (fixture.getId() < 0) {
			return idf.createID();
		} else {
			return fixture.getId();
		}
	}

	/**
	 * Create a mapping from ID numbers to Pairs of fixtures and their
	 * location for all fixtures in the map.
	 */
	public static DelayedRemovalMap<Integer, Pair<Point, IFixture>> getFixtures(IMapNG map) {
		final DelayedRemovalMap<Integer, Pair<Point, IFixture>> retval = new IntMap<>();
		IDRegistrar idf = new IDFactoryFiller().createIDFactory(map);
		for (Point location : map.getLocations()) {
			for (TileFixture fixture : map.getFixtures(location)) {
				addToMap(location, fixture, idf, retval);
			}
		}
		return retval;
	}

	private static void parentMapImpl(Map<Integer, Integer> retval, IFixture parent,
			Iterable<? extends IFixture> stream) {
		for (IFixture fixture : stream) {
			retval.put(fixture.getId(), parent.getId());
			if (fixture instanceof FixtureIterable) {
				parentMapImpl(retval, fixture, (FixtureIterable<?>) fixture);
			}
		}
	}

	/**
	 * Create a mapping from child ID numbers to parent ID numbers.
	 */
	public static Map<Integer, Integer> getParentMap(IMapNG map) {
		Map<Integer, Integer> retval = new HashMap<>();
		for (Point location : map.getLocations()) {
			for (TileFixture fixture : map.getFixtures(location)) {
				if (fixture instanceof FixtureIterable) {
					parentMapImpl(retval, fixture, (FixtureIterable<?>) fixture);
				}
			}
		}
		return retval;
	}
}

