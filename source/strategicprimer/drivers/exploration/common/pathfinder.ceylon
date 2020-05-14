import ceylon.collection {
    MutableSet,
    MutableMap,
    HashSet,
    HashMap,
    ArrayList,
    MutableList
}
import strategicprimer.model.common.map {
    IMapNG,
    Point
}
import strategicprimer.model.common.map.fixtures.terrain {
    Forest
}
import ceylon.logging {
    logger,
    Logger
}

"A logger."
Logger log = logger(`module strategicprimer.drivers.exploration.common`);

MutableMap<IMapNG, Pathfinder> pathfinderCache = HashMap<IMapNG, Pathfinder>();

"An encapsulation (for ease of importing and just in case I decide to do some
 caching between runs at some point) of an implementation of Dijkstra's
 shortest-path algorithm."
shared Pathfinder pathfinder(IMapNG map) {
    if (exists retval = pathfinderCache[map]) {
        return retval;
    } else {
        value retval = PathfinderImpl(map);
        pathfinderCache[map] = retval;
        return retval;
    }
}
"An implementation of a pathfinding algorithm, such as Dijkstra's shortest-path algorithm."
shared sealed interface Pathfinder {
    "The shortest-path distance, avoiding obstacles, in MP, between two points."
    shared formal [Integer, {Point*}] getTravelDistance(Point start, Point end);
}
        
class PathfinderImpl(IMapNG map) satisfies Pathfinder {
    MutableMap<[Point, Point], Integer> tentativeDistances = 
        HashMap<[Point, Point], Integer>();
    Boolean forUs(Point base, Set<Point> unvisited)(<[Point, Point]->Integer> entry) =>
            entry.key.first == base && entry.key.first in unvisited;
    Point? nextUnvisited(Point base, Set<Point> unvisited) {
            if (exists [start, dest] = tentativeDistances.filter(forUs(base, unvisited))
                    .sort(increasingItem).first?.key) {
                return dest;
            } else {
                return null;
            }
    }

    Direction getDirection(Point one, Point two) {
        switch (one.row <=> two.row)
        case (smaller) {
            switch (one.column <=> two.column)
            case (smaller) {
                return Direction.northeast;
            }
            case (equal) {
                return Direction.north;
            }
            case (larger) {
                return Direction.northwest;
            }
        }
        case (equal) {
            switch (one.column <=> two.column)
            case (smaller) {
                return Direction.east;
            }
            case (equal) {
                return Direction.nowhere;
            }
            case (larger) {
                return Direction.west;
            }
        }
        case (larger) {
            switch (one.column <=> two.column)
            case (smaller) {
                return Direction.southeast;
            }
            case (equal) {
                return Direction.south;
            }
            case (larger) {
                return Direction.southwest;
            }
        }
    }

    "The shortest-path distance, avoiding obstacles, in MP, between two points, using
     Dijkstra's algorithm."
    shared actual [Integer, {Point*}] getTravelDistance(Point start, Point end) {
        MutableSet<Point> unvisited = HashSet { elements = map.locations; };
        for (point in map.locations) {
            if (!tentativeDistances.contains([start, point])) {
                tentativeDistances[[start, point]] = runtime.maxArraySize;
            }
        }
        tentativeDistances[[start, start]] = 0;
        variable Point current = start;
        variable Integer iterations = 0;
        MutableMap<Point, Point> retval = HashMap<Point, Point>();
        while (!unvisited.empty) {
            iterations++;
            assert (exists Integer currentDistance = tentativeDistances[[start, current]]);
            if (current == end) {
                log.debug("Reached the end after ``iterations`` iterations");
                MutableList<Point> path = ArrayList<Point>();
                path.add(current);
                while (exists next = retval[current]) {
                    path.add(next);
                    current = next;
                }
                return [currentDistance, path.reversed];
            } else if (currentDistance >= runtime.maxArraySize) {
                log.debug("Considering an 'infinite-distance' tile after ``
                    iterations`` iterations");
                return [currentDistance, []];
            }
            for (neighbor in surroundingPointIterable(current, map.dimensions, 1)) {
                log.trace("At ``current``, considering ``neighbor``.");
                if (!unvisited.contains(neighbor)) {
                    log.trace("Already checked, so skipping.");
                    continue;
                }
                assert (exists estimate = tentativeDistances[[start, neighbor]]);
                Integer tentativeDistance = currentDistance +
                    simpleMovementModel.movementCost(map.baseTerrain[neighbor],
                        !map.fixtures.get(neighbor).narrow<Forest>().empty,
                        map.mountainous.get(neighbor),
                        simpleMovementModel.riversSpeedTravel(getDirection(current, neighbor),
                            map.rivers.get(current), map.rivers.get(neighbor)),
                        map.fixtures.get(neighbor));
                log.trace(
                    "Old estimate ``estimate``, new estimate ``tentativeDistance``");
                if (tentativeDistance < estimate) {
                    log.trace("Updating path");
                    retval[neighbor] = current;
                    tentativeDistances[[start, neighbor]] = tentativeDistance;
                }
                if (estimate < 0) {
                    log.warn("Old estimate at ``neighbor`` was negative");
                    return [runtime.maxArraySize, []];
                } else if (tentativeDistance < 0) {
                    log.warn("Recomputed estimate at ``neighbor`` was negative");
                    return [runtime.maxArraySize, []];
                }
            }
            log.trace("Finished checking neighbors of ``current``");
            unvisited.remove(current);
            if (exists next = nextUnvisited(start, unvisited)) {
                current = next;
            } else {
                log.debug("Couldn't find a smallest-estimate unchecked tile after ``
                    iterations`` iterations");
                return [runtime.maxArraySize, []];
            }
        }
        log.debug("Apparently ran out of tiles after ``iterations`` iterations");
        return [tentativeDistances[[start, end]] else runtime.maxArraySize, []];
    }
}
