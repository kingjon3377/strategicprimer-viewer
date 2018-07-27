import ceylon.collection {
	MutableSet,
	MutableMap,
	HashSet,
	HashMap,
	ArrayList,
	MutableList
}
import strategicprimer.model.map {
	IMapNG,
	Point
}
import lovelace.util.common {
	comparingOn
}
import strategicprimer.model.map.fixtures.terrain {
	Forest
}
import ceylon.logging {
	logger,
	Logger
}
"A logger."
Logger log = logger(`module strategicprimer.drivers.exploration.common`);
shared object pathfinder {
	"The shortest-path distance, avoiding obstacles, in MP, between two points, using
	 Dijkstra's algorithm."
	shared [Integer, {Point*}] getTravelDistance(IMapNG map, Point start, Point end) {
		MutableSet<Point> unvisited = HashSet { elements = map.locations; };
		MutableMap<Point, Integer> tentativeDistances = HashMap<Point, Integer> {
			entries = map.locations
				.map(shuffle(curry(Entry<Point, Integer>))(runtime.maxArraySize)); };
		tentativeDistances[start] = 0;
		variable Point current = start;
		variable Integer iterations = 0;
		MutableMap<Point, Point> retval = HashMap<Point, Point>();
		while (!unvisited.empty) {
			iterations++;
			assert (exists Integer currentDistance = tentativeDistances[current]);
			if (currentDistance >= runtime.maxArraySize) {
				log.debug("Considering a tile estimated as an infinite distance away after ``iterations`` iterations");
				return [currentDistance, []];
			} else if (current == end) {
				log.debug("Reached the end after ``iterations`` iterations");
				MutableList<Point> path = ArrayList<Point>();
				path.add(current);
				while (exists next = retval[current]) {
					path.add(next);
					current = next;
				}
				return [currentDistance, path.reversed];
			}
			for (neighbor in surroundingPointIterable(current, map.dimensions, 1)) {
				if (!unvisited.contains(neighbor)) {
					continue;
				}
				assert (exists estimate = tentativeDistances[neighbor]);
				Integer tentativeDistance = currentDistance +
					simpleMovementModel.movementCost(map.baseTerrain[neighbor],
						!map.fixtures.get(neighbor).narrow<Forest>().empty, map.mountainous.get(neighbor),
						!map.rivers.get(neighbor).empty || !map.rivers.get(current).empty,
						map.fixtures.get(neighbor));
				if (tentativeDistance < estimate) {
					retval[neighbor] = current;
					tentativeDistances[neighbor] = tentativeDistance;
				}
				if (estimate < 0) {
					log.warn("Old estimate at ``neighbor`` was negative");
					return [runtime.maxArraySize, []];
				} else if (tentativeDistance < 0) {
					log.warn("Recomputed estimate at ``neighbor`` was negative");
					return [runtime.maxArraySize, []];
				}
			}
			unvisited.remove(current);
			if (exists next = tentativeDistances.sort(comparingOn(Entry<Point, Integer>.item,
					increasing<Integer>)).map(Entry.key).filter(unvisited.contains).first) {
				current = next;
			} else {
				log.debug("Couldn't find a smallest-estimate unchecked tile after ``iterations`` iterations");
				return [runtime.maxArraySize, []];
			}
		}
		log.debug("Apparently ran out of tiles after ``iterations`` iterations");
		return [tentativeDistances[end] else runtime.maxArraySize, []];
	}
}
