import lovelace.util.common {
    todo
}
import ceylon.collection {
    MutableMap,
    HashMap
}
"Cache of Coordinates. I use two levels of Maps rather than using Tuples as keys because I
 have a hunch this is faster."
todo("Measure that", "Java version used ConcurrentHashMap, while this isn't thread-safe",
	"Consider replacing Coordinate with Tuple entirely")
MutableMap<Integer, MutableMap<Integer, Coordinate>> coordinateCache =
		HashMap<Integer, MutableMap<Integer, Coordinate>>();
"Factory method for [[Coordinate]]s."
shared Coordinate coordinateFactory(Integer x, Integer y, Boolean useCache = true) {
	if (useCache) {
		if (exists inner = coordinateCache.get(x)) {
			if (exists retval = inner.get(y)) {
				return retval;
			} else {
				Coordinate retval = Coordinate(x, y);
				inner.put(y, retval);
				return retval;
			}
		} else {
			Coordinate retval = Coordinate(x, y);
			MutableMap<Integer, Coordinate> inner = HashMap { y->retval };
			coordinateCache.put(x, inner);
			return retval;
		}
	} else {
		return Coordinate(x, y);
	}
}
