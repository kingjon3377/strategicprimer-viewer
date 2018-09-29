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
    Point,
    invalidPoint
}
"An abstract superclass for classes that generate reports for particular kinds of SP
 objects. It's mostly interface and helper methods, but contains a couple of bits of
 shared state."
// We'd like to make methods and member types static, but a sealed class can't have
// constructors.
shared sealed abstract class AbstractReportGenerator<T>(
        shared Comparison([Point, IFixture], [Point, IFixture]) pairComparator,
        MapDimensions? mapDimensions, Point referencePoint = invalidPoint)
        satisfies IReportGenerator<T> given T satisfies IFixture {
    shared DistanceComparator distCalculator = DistanceComparator(referencePoint,
        mapDimensions);
    "A list that produces HTML in its [[string]] attribute."
    shared class HtmlList(shared actual String header, {String*} initial = [])
            extends ArrayList<String>(0, 1.5, initial)
            satisfies IReportGenerator<T>.HeadedList<String> {
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
    "Turn a series of items into a comma-separated list of their string representations."
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
    """A list of Points that produces a comma-separated list in its `string` and has a
       "header"."""
    shared class PointList(shared actual String header) extends ArrayList<Point>()
            satisfies IReportGenerator<T>.HeadedList<Point> {
        shared actual String string {
            if (empty) {
                return "";
            } else {
                StringBuilder builder = StringBuilder();
                builder.append(header);
                builder.append(" ");
                assert (exists firstItem = first);
                builder.append(firstItem.string);
                if (exists third = rest.rest.first) {
                    variable {Point*} temp = rest;
                    while (exists current = temp.first) {
                        if (temp.rest.first exists) {
                            builder.append(", ``current``");
                        } else {
                            builder.append(", and ``current``");
                        }
                        temp = temp.rest;
                    }
                } else if (exists second = rest.first) {
                    builder.append(" and ``second``");
                }
                return builder.string;
            }
        }
    }
    "An implementation of HeadedMap."
    shared class HeadedMapImpl<Key, Value>(shared actual String header,
            Comparison(Key, Key)? comparator = null, {<Key->Value>*} initial = [])
            satisfies IReportGenerator<T>.MutableHeadedMap<Key, Value>
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
        shared actual Boolean equals(Object that) {
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
