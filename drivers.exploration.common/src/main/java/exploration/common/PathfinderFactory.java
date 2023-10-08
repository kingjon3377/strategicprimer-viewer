package exploration.common;

import java.util.Map;
import java.util.HashMap;

import common.map.IMapNG;

public final class PathfinderFactory {
    private PathfinderFactory() {
    }

    private static final Map<IMapNG, Pathfinder> pathfinderCache = new HashMap<>();

    /**
     * An encapsulation (for ease of importing in the Ceylon version, and
     * just in case I decide to do some caching between runs at some point)
     * of an implementation of Dijkstra's shortest-path algorithm.
     */
    public static Pathfinder pathfinder(final IMapNG map) {
        if (pathfinderCache.containsKey(map)) {
            return pathfinderCache.get(map);
        } else {
            final Pathfinder retval = new PathfinderImpl(map);
            pathfinderCache.put(map, retval);
            return retval;
        }
    }
}
