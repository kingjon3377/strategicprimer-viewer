import lovelace.util.common {
    todo
}
import ceylon.collection {
    MutableMap,
    HashMap
}
import strategicprimer.model.map {
	CachingStrategy
}
shared variable CachingStrategy coordinateCachingStrategy = CachingStrategy.multilevel;
"Clear the [[Coordinate]] cache. This should only be called from test code."
shared void clearCoordinateCache() {
	coordinateCache.clear();
	coordinateTupleCache.clear();
}
"Cache of Coordinates. I use two levels of Maps rather than using Tuples as keys because I
 have a hunch this is faster."
todo("Measure that", "Java version used ConcurrentHashMap, while this isn't thread-safe",
    "Consider replacing Coordinate with Tuple entirely")
MutableMap<Integer, MutableMap<Integer, Coordinate>> coordinateCache =
        HashMap<Integer, MutableMap<Integer, Coordinate>>();
MutableMap<[Integer, Integer], Coordinate> coordinateTupleCache =
        HashMap<[Integer, Integer], Coordinate>();
"Factory method for [[Coordinate]]s."
shared Coordinate coordinateFactory(Integer x, Integer y) {
	switch (coordinateCachingStrategy)
    case (CachingStrategy.multilevel) {
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
    }
    case (CachingStrategy.tuple) {
        [Integer, Integer] tuple = [x, y];
        if (exists retval = coordinateTupleCache[tuple]) {
            return retval;
        } else {
            Coordinate retval = Coordinate(x, y);
            coordinateTupleCache[tuple] = retval;
            return retval;
        }
    }
    case (CachingStrategy.constructor) {
        return Coordinate(x, y);
    }
}
