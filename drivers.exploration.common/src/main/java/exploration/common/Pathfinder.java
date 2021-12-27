package exploration.common;

import org.javatuples.Pair;
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

/**
 * An implementation of a pathfinding algorithm, such as Dijkstra's shortest-path algorithm.
 *
 * TODO: Uncomment sealed once we're up to Java 15 (or whenever that becomes a non-preview feature)
 */
public /* sealed */ interface Pathfinder {
	/**
	 * The shortest-path distance, avoiding obstacles, in MP, between two points.
	 *
	 * FIXME: Should we specify Long instead of Integer?
	 */
	Pair<Integer, Iterable<Point>> getTravelDistance(Point start, Point end);
}
