package exploration.common;

import org.javatuples.Pair;
import common.map.Point;

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
