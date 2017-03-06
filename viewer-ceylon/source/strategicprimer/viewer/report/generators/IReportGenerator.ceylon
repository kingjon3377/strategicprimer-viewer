import lovelace.util.common {
    DelayedRemovalMap
}

import model.map {
    IFixture,
    IMapNG,
    Point
}

import strategicprimer.viewer.report.nodes {
    IReportNode
}
"An interface for report generators."
shared interface IReportGenerator<T> given T satisfies IFixture {
    "A list that knows what its title should be when its contents are written to HTML."
    shared /* static */ interface HeadedList<Element> satisfies List<Element> {
        "The header text."
        shared formal String header;
    }
    "A Map that knows what its title should be when its contents are written to HTML."
    shared /* static */ interface HeadedMap<Key, Value> satisfies Map<Key, Value>
            given Key satisfies Object{
        "The header text."
        shared formal String header;
    }
    "Write a (sub-)report to a stream. All fixtures that this report references should
     be removed from the set before returning."
    shared formal void produce(
            "The set of fixtures in the map."
            DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
            "The map. (Needed to get terrain type for some reports.)"
            IMapNG map,
            "The stream to write to"
            Anything(String) ostream,
            "The specific item to write about and its location; if null, write about all
             matching items."
            [T, Point]? entry = null);
    "Produce an intermediate-representation form of the report representing an item or a
     group of items. All fixtures that this report references should be removed from the
     set before returning."
    shared formal IReportNode produceRIR(
            "The set of fixtures in the map."
            DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
            "The map. (Needed to get terrain type for some reports.)"
            IMapNG map,
            "The specific item to write about and its location; if null, write about all
             matching items."
            [T, Point]? entry = null);
    "Write the contents of a Map to a stream as a list, but don't write anything
     if it is empty."
    shared default void writeMap<out Key>(
            "The stream to write to."
            Anything(String) ostream,
            "The map to write. Has to be a [[HeadedMap]] so we can get its heading."
            HeadedMap<Key, Point> map,
            "The method to write each item."
            Anything(Key->Point, Anything(String)) lambda) given Key satisfies Object {
        if (!map.empty) {
            ostream("``map.header``
                     <ul>
                     ");
            for (entry in map) {
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
