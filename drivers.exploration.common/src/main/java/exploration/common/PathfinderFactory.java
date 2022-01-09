package exploration.common;

import java.util.Set;
import java.util.Map;
import java.util.HashSet;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import common.map.IMapNG;
import common.map.Direction;
import common.map.Point;
import common.map.fixtures.terrain.Forest;
import java.util.logging.Logger;

public final class PathfinderFactory {
	private PathfinderFactory() {}
	private static Logger LOGGER = Logger.getLogger(PathfinderFactory.class.getName());
	private static Map<IMapNG, Pathfinder> pathfinderCache = new HashMap<>();

	/**
	 * An encapsulation (for ease of importing in the Ceylon version, and
	 * just in case I decide to do some caching between runs at some point)
	 * of an implementation of Dijkstra's shortest-path algorithm.
	 */
	public static Pathfinder pathfinder(IMapNG map) {
		if (pathfinderCache.containsKey(map)) {
			return pathfinderCache.get(map);
		} else {
			Pathfinder retval = new PathfinderImpl(map);
			pathfinderCache.put(map, retval);
			return retval;
		}
	}
}