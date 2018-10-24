import ceylon.collection {
    ArrayList,
    MutableMap,
    TreeMap,
    HashMap
}

import strategicprimer.model.common {
    DistanceComparator
}
import strategicprimer.model.common.map {
    IFixture,
    MapDimensions,
    Point
}

"An abstract superclass for classes that generate reports for particular kinds of SP
 objects. It's mostly interface and helper methods, but contains a couple of bits of
 shared state."
// We'd like to make methods and member types static, but a sealed class can't have
// constructors.
shared sealed abstract class AbstractReportGenerator<Type>(
        "A comparator of [[Point]]-fixture pairs."
        shared Comparison([Point, IFixture], [Point, IFixture]) pairComparator,
        "The dimensions of the map. If [[null]], [[distCalculator]] will give
         inaccurate results whenever the shortest distance between two points
         involves wrapping around an edge of the map."
        MapDimensions? mapDimensions,
        "The base point to use for distance calculations. Usually the
         location of the headquarters of the player for whom the report is
         being prepared."
        Point referencePoint = Point.invalidPoint)
        satisfies IReportGenerator<Type> given Type satisfies IFixture {
    "A calculator-comparator for subclasses to use to compare fixtures on the basis of 
     distance from [[referencePoint]] and to print that distance in the report."
    shared DistanceComparator distCalculator = DistanceComparator(referencePoint,
        mapDimensions);
    "A list that produces HTML in its [[string]] attribute."
    shared class HtmlList(shared actual String header, {String*} initial = [])
            extends ArrayList<String>(0, 1.5, initial)
            satisfies IReportGenerator<Type>.HeadedList<String> {
        "If there's nothing in the list, return the empty string, but otherwise produce an
         HTML list of our contents."
        shared actual String string {
            if (empty) {
                return "";
            } else {
                StringBuilder builder = StringBuilder();
                builder.append("``header``
                                <ul>
                                ");
                for (item in this) {
                    builder.append("<li>``item``</li>
                                    ");
                }
                builder.append("""</ul>
                                  """);
                return builder.string;
            }
        }
    }
    """Turn a series of items into a comma-separated list of their string representations,
       with "and" before the final item and a special no-comma case for a list of only
       two items."""
    // Should be static, but can't
    shared String commaSeparatedList(Object* list) {
        if (exists first = list.first) {
            StringBuilder builder = StringBuilder();
            builder.append(first.string);
            if (exists third = list.rest.rest.first) {
                variable {Object*} rest = list.rest;
                while (exists current = rest.first) {
                    if (rest.rest.first exists) {
                        builder.append(", ``current``");
                    } else {
                        builder.append(", and ``current``");
                    }
                    rest = rest.rest;
                }
            } else if (exists second = list.rest.first) {
                builder.append(" and ``second``");
            }
            return builder.string;
        } else {
            return "";
        }
    }
    """A list of Points that produces a comma-separated list in its [[string]] and has a
       "header"."""
    shared class PointList(
            "The 'header' to print before the points in the list."
            shared actual String header) extends ArrayList<Point>()
            satisfies IReportGenerator<Type>.HeadedList<Point> {
        shared actual String string {
            if (empty) {
                return "";
            } else {
                return "``header`` ``commaSeparatedList(this)``";
            }
        }
    }

    "An implementation of [[HeadedMap]]."
    shared class HeadedMapImpl<Key, Value>(
                "The header to prepend to the items in [[string]]."
                shared actual String header,
                "A comparator to sort the map by. If provided, we wrap a [[TreeMap]];
                 if not, we wrap a [[HashMap]]."
                Comparison(Key, Key)? comparator = null,
                "Inital entries in the map."
                {<Key->Value>*} initial = [])
            satisfies IReportGenerator<Type>.MutableHeadedMap<Key, Value>
            given Key satisfies Object {
        MutableMap<Key, Value> wrapped;
        if (exists comparator) {
            wrapped = TreeMap<Key, Value>(comparator, initial);
        } else {
            wrapped = HashMap<Key, Value> { entries = initial; };
        }
        shared actual Integer size => wrapped.size;
        shared actual Boolean empty => wrapped.empty;
        shared actual Integer hash => wrapped.hash;
        shared actual Boolean equals(Object that) { // TODO: Replace with (this of Map<Key, Value>.equals(that)
            if (is Map<Key, Value> that) {
                return that.containsEvery(this) && containsEvery(that);
            } else {
                return false;
            }
        }
        shared actual void clear() => wrapped.clear();
        shared actual MutableMap<Key,Value> clone() => HeadedMapImpl<Key, Value>(header,
            comparator, initial);
        shared actual Boolean defines(Object key) => wrapped.defines(key);
        shared actual Value? get(Object key) => wrapped.get(key);
        shared actual Iterator<Key->Value> iterator() => wrapped.iterator();
        shared actual Value? put(Key key, Value item) => wrapped[key] = item;
        shared actual Value? remove(Key key) => wrapped.remove(key);
    }
}
