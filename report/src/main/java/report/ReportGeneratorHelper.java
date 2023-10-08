package report;

import common.map.fixtures.Implement;
import common.map.fixtures.mobile.IWorker;
import lovelace.util.LovelaceLogger;
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

/**
 * An encapsulation of helper methods for report generators.
 */
/* package */ final class ReportGeneratorHelper {
    private ReportGeneratorHelper() {
    }

    /**
     * Find the location of the given player's HQ in the given map, or null if not found.
     */
    public static @Nullable Point findHQ(final IMapNG map, final Player player) {
        Point retval = null;
        for (final Point location : map.getLocations()) {
            for (final TileFixture fixture : map.getFixtures(location)) {
                if (fixture instanceof IFortress f && player.equals(f.owner())) {
                    if ("hq".equals(f.getName())) {
                        return location;
                    } else if (location.isValid() && retval == null) {
                        retval = location;
                    }
                }
            }
        }
        return retval;
    }

    private static void addToMap(final Point location, final IFixture fixture, final IDRegistrar idf,
                                 final DelayedRemovalMap<Integer, Pair<Point, IFixture>> mapping) {
        if (fixture instanceof TileFixture || fixture.getId() >= 0) {
            final int key = checkID(idf, fixture);
            final Pair<Point, IFixture> val = Pair.with(location, fixture);
            if (mapping.containsKey(key) && !val.equals(mapping.get(key))) {
                LovelaceLogger.warning("Duplicate key, %d, for Pairs %s and %s",
                        key, mapping.get(key), val);
            }
            mapping.put(key, val);
            if (fixture instanceof FixtureIterable<?> fi) {
                for (final IFixture inner : fi) {
                    addToMap(location, inner, idf, mapping);
                }
            } else if (fixture instanceof IWorker w) {
                if (w.getMount() != null) {
                    addToMap(location, w.getMount(), idf, mapping);
                }
                for (final Implement inner : w.getEquipment()) {
                    addToMap(location, inner, idf, mapping);
                }
            }
        }
    }

    private static int checkID(final IDRegistrar idf, final IFixture fixture) {
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
    public static DelayedRemovalMap<Integer, Pair<Point, IFixture>> getFixtures(final IMapNG map) {
        final DelayedRemovalMap<Integer, Pair<Point, IFixture>> retval = new IntMap<>();
        final IDRegistrar idf = IDFactoryFiller.createIDFactory(map);
        for (final Point location : map.getLocations()) {
            for (final TileFixture fixture : map.getFixtures(location)) {
                addToMap(location, fixture, idf, retval);
            }
        }
        return retval;
    }

    private static void parentMapImpl(final Map<Integer, Integer> retval, final IFixture parent,
                                      final Iterable<? extends IFixture> stream) {
        for (final IFixture fixture : stream) {
            retval.put(fixture.getId(), parent.getId());
            if (fixture instanceof FixtureIterable<?> fi) {
                parentMapImpl(retval, fixture, fi);
            }
        }
    }

    /**
     * Create a mapping from child ID numbers to parent ID numbers.
     */
    public static Map<Integer, Integer> getParentMap(final IMapNG map) {
        final Map<Integer, Integer> retval = new HashMap<>();
        for (final Point location : map.getLocations()) {
            for (final TileFixture fixture : map.getFixtures(location)) {
                if (fixture instanceof FixtureIterable<?> fi) {
                    parentMapImpl(retval, fixture, fi);
                }
            }
        }
        return retval;
    }
}

