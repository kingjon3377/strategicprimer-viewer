import lovelace.util.common {
    todo
}
import ceylon.collection {
    MutableMap,
    HashMap
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
"Factory method for [[Coordinate]]s." // TODO: Remove in favor of the constructor
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
"How to acquire Coordinates and similar objects."
shared class CachingStrategy of constructor|multilevel|tuple {
    shared actual String string;
    "Delegate to the constructor instead of caching."
    shared new constructor { string = "no-cache"; }
    "Use a Map of Maps, with Integers as keys in both cases, as a cache."
    shared new multilevel { string = "multi-level"; }
    "Use a Map with Tuples as keys as a cache."
    shared new tuple { string = "tuple-based"; }
}
