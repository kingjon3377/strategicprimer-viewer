import lovelace.util.common {
    DelayedRemovalMap,
    todo
}

import strategicprimer.model.map {
    Point,
    IFixture,
    IMapNG
}
import strategicprimer.report {
    IReportNode
}
import ceylon.collection {
    MutableMap
}
"An interface for report generators."
shared interface IReportGenerator<T> given T satisfies IFixture {
    "A list that knows what its title should be when its contents are written to HTML."
    shared /* static */ interface HeadedList<Element> satisfies List<Element> {
        "The header text."
        shared formal String header;
    }
    "A Map that knows what its title should be when its contents are written to HTML."
    shared /* static */ interface HeadedMap<out Key, Value> satisfies Map<Key, Value>
            given Key satisfies Object{
        "The header text."
        shared formal String header;
    }
    "A [[HeadedMap]] that is also mutable."
    shared /* static */ interface MutableHeadedMap<Key, Value>
            satisfies HeadedMap<Key, Value>&MutableMap<Key, Value>
            given Key satisfies Object {}
    "Write a (sub-)report to a stream. All fixtures that this report references should
     be removed from the set before returning."
    shared formal void produce(
            "The set of fixtures in the map."
            DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
            "The map. (Needed to get terrain type for some reports.)"
            IMapNG map,
            "The stream to write to"
            Anything(String) ostream);
    "Write a (sub-)report on a single item to a stream."
    todo("Move back into [[produce]] once eclipse/ceylon#2147 fixed")
    shared formal void produceSingle(
        "The set of fixtures in the map."
        DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
        "The map. (Needed to get terrain type for some reports.)"
        IMapNG map,
        "The stream to write to"
        Anything(String) ostream,
        "The specific item to write about."
        T item,
        "Its location"
        Point loc);
    "Produce an intermediate-representation form of the report representing a group of
     items. All fixtures that this report references should be removed from the set
     before returning."
    shared formal IReportNode produceRIR(
            "The set of fixtures in the map."
            DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
            "The map. (Needed to get terrain type for some reports.)"
            IMapNG map);
    "Produce an intermediate-representation form of the report representing an item. All
     fixtures that this report references should be removed from the set before
     returning."
    todo("Move back into [[produceRIR]] once eclipse/ceylon#2147 fixed")
    shared formal IReportNode produceRIRSingle(
        "The set of fixtures in the map."
        DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
        "The map. (Needed to get terrain type for some reports.)"
        IMapNG map,
        "The specific item to write about"
        T item,
        "Its location"
        Point loc);
    "A factory for a default formatter for [[writeMap]]."
    shared default Anything(T->Point, Anything(String)) defaultFormatter(
        DelayedRemovalMap<Integer, [Point, IFixture]> fixtures, IMapNG map) =>
            (T key->Point val, Anything(String) formatter) =>
                produceSingle(fixtures, map, formatter, key, val);
    "Write the contents of a Map to a stream as a list, but don't write anything
     if it is empty."
    shared default void writeMap<out Key>(
            "The stream to write to."
            Anything(String) ostream,
            "The map to write. Has to be a [[HeadedMap]] so we can get its heading."
            HeadedMap<Key, Point> map,
            "The method to write each item."
            Anything(Key->Point, Anything(String)) lambda,
            "An optional sorting method to run the map through before printing."
            Comparison(Key->Point, Key->Point)? sorter = null
            ) given Key satisfies Object {
        if (!map.empty) {
            ostream("``map.header``
                     <ul>
                     ");
            {<Key->Point>*} sorted;
            if (exists sorter) {
                sorted = map.sort(sorter);
            } else {
                sorted = map;
            }
            for (entry in sorted) {
                ostream("<li>");
                lambda(entry, ostream);
                ostream("""</li>
                           """);
            }
            ostream("""</ul>
                       """);
        }
    }
}
