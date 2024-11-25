package exploration.common;

import java.util.Map;
import java.util.HashMap;

import legacy.map.ILegacyMap;

public final class PathfinderFactory {
	private PathfinderFactory() {
	}

	private static final Map<ILegacyMap, Pathfinder> pathfinderCache = new HashMap<>();

	/**
	 * An encapsulation (for ease of importing in the Ceylon version, and
	 * just in case I decide to do some caching between runs at some point)
	 * of an implementation of Dijkstra's shortest-path algorithm.
	 */
	public static Pathfinder pathfinder(final ILegacyMap map) {
		return pathfinderCache.computeIfAbsent(map, PathfinderImpl::new);
	}
}
