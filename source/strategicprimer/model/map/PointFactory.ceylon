import ceylon.collection {
    MutableMap,
    HashMap
}

import lovelace.util.common {
    todo
}

"The cache of Points. I use two levels of Maps rather than using Tuples as keys because I
 have a hunch this is faster."
todo("Measure that",
        "The Java version used ConcurrentHashMap, while this isn't thread-safe")
MutableMap<Integer, MutableMap<Integer, Point>> pointCache =
        HashMap<Integer, MutableMap<Integer, Point>>();
MutableMap<[Integer, Integer], Point> pointTupleCache = HashMap<[Integer, Integer], Point>();
"Clear the Point cache. Should only be called by performance-testing code."
shared void clearPointCache() {
	pointCache.clear();
	pointTupleCache.clear();
}
"How to acquire Points and similar objects."
shared class CachingStrategy of constructor|multilevel|tuple {
	shared actual String string;
	"Delegate to the constructor instead of caching."
	shared new constructor { string = "no-cache"; }
	"Use a Map of Maps, with Integers as keys in both cases, as a cache."
	shared new multilevel { string = "multi-level"; }
	"Use a Map with Tuples as keys as a cache."
	shared new tuple { string = "tuple-based"; }
}
"How to acquire [[Point]]s."
shared variable CachingStrategy pointCachingStrategy = CachingStrategy.multilevel;
"A wrapper around the [[Point]] constructor that caches Points.

 Fairly early in the development of the Java version, I implemented this to try to speed
 things up, then considered replacing the cache with simply a constructor call. After
 performance testing (only using the [[draw helper
 comparator|strategicprimer.viewer.drivers.map_viewer::drawHelperComparator]] and [[echo
 driver|strategicprimer.viewer.drivers::echoDriver]], not any more realistic test, though)
 it appeared that the cache is faster as the map's size and complexity increased, so I
 decided to leave it."
suppressWarnings("doclink")
shared Point pointFactory(Integer row, Integer column) {
	switch (pointCachingStrategy)
	case (CachingStrategy.multilevel) {
        if (exists inner = pointCache[row]) {
            if (exists retval = inner[column]) {
                return retval;
            } else {
                Point retval = PointImpl(row, column);
                inner[column] = retval;
                return retval;
            }
        } else {
            Point retval = PointImpl(row, column);
            MutableMap<Integer, Point> inner = HashMap { column->retval };
            pointCache[row] = inner;
            return retval;
        }
    }
    case (CachingStrategy.constructor) {
        return PointImpl(row, column);
    }
    case (CachingStrategy.tuple) {
        [Integer, Integer] tuple = [row, column];
        if (exists retval = pointTupleCache[tuple]) {
            return retval;
        } else {
            Point retval = PointImpl(row, column);
            pointTupleCache[tuple] = retval;
            return retval;
        }
    }
}
"A structure encapsulating two coordinates."
class PointImpl(row, column) satisfies Point {
    shared actual Integer row;
    shared actual Integer column;
    shared actual Boolean equals(Object obj) {
        if (is Point obj) {
            return obj.row == row && obj.column == column;
        } else {
            return false;
        }
    }
    shared actual Integer hash => row.leftLogicalShift(9) + column;
    shared actual String string => "(``row``, ``column``)";
}
"""The standard "invalid point.""""
todo("Replace with [[null]]?")
shared Point invalidPoint = PointImpl(-1, -1);
