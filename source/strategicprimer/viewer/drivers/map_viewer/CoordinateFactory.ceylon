import lovelace.util.common {
    todo
}
import ceylon.collection {
    MutableMap,
    HashMap
}
variable Boolean useCache = true;
"Enable the cache of [[Coordinate]]s. This should only be called from test code."
shared void enableCoordinateCache(Boolean enabled) => useCache = enabled;
// FIXME: Provide, and call from test code where appropriate, a method to clear the cache.
"Cache of Coordinates. I use two levels of Maps rather than using Tuples as keys because I
 have a hunch this is faster."
todo("Measure that", "Java version used ConcurrentHashMap, while this isn't thread-safe",
    "Consider replacing Coordinate with Tuple entirely")
MutableMap<Integer, MutableMap<Integer, Coordinate>> coordinateCache =
        HashMap<Integer, MutableMap<Integer, Coordinate>>();
"Factory method for [[Coordinate]]s."
shared Coordinate coordinateFactory(Integer x, Integer y) {
    if (useCache) {
        if (exists inner = coordinateCache[x]) {
            if (exists retval = inner[y]) {
                return retval;
            } else {
                Coordinate retval = Coordinate(x, y);
                inner[y] = retval;
                return retval;
            }
        } else {
            Coordinate retval = Coordinate(x, y);
            MutableMap<Integer, Coordinate> inner = HashMap { y->retval };
            coordinateCache[x] = inner;
            return retval;
        }
    } else {
        return Coordinate(x, y);
    }
}
