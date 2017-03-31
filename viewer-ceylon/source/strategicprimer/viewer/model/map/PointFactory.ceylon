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
"Clear the Point cache. Should only be called by performance-testing code."
shared void clearPointCache() => pointCache.clear();
"A wrapper around the [[Point]] constructor that caches Points.

 Fairly early in the development of the Java version, I implemented this to try to speed
 things up, then considered replacing the cache with simply a constructor call. After
 performance testing (only using the [[drawHelperComparator]] and [[echoDriver]], not any
 more realistic test, though) it appeared that the cache is faster as the map's size and
 complexity increased, so I decided to leave it."
shared Point pointFactory(Integer row, Integer column, Boolean useCache = true) {
	if (useCache) {
		if (exists inner = pointCache.get(row)) {
			if (exists retval = inner.get(column)) {
				return retval;
			} else {
				Point retval = PointImpl(row, column);
				inner.put(column, retval);
				return retval;
			}
		} else {
			Point retval = PointImpl(row, column);
			MutableMap<Integer, Point> inner = HashMap { column->retval };
			pointCache.put(row, inner);
			return retval;
		}
	} else {
		return PointImpl(row, column);
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