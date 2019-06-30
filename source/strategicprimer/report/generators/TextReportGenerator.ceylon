import lovelace.util.common {
    DelayedRemovalMap,
    narrowedStream
}

import strategicprimer.model.common.map {
    IFixture,
    Point,
    MapDimensions,
    IMapNG
}
import strategicprimer.model.common.map.fixtures {
    TextFixture
}

"A report generator for arbitrary-text notes."
shared class TextReportGenerator(Comparison([Point, IFixture], [Point, IFixture]) comp,
        MapDimensions dimensions, Point? hq = null)
        extends AbstractReportGenerator<TextFixture>(comp, dimensions, hq) {
    "Produce the part of the report dealing with an arbitrary-text note. This does *not*
     remove it from the collection, because this method doesn't know the synthetic ID #
     that was assigned to it."
    shared actual void produceSingle(
            DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
            IMapNG map, Anything(String) ostream, TextFixture item, Point loc) {
        ostream("At ``loc`` ``distanceString(loc)``");
        if (item.turn>=0) {
            ostream(": On turn ``item.turn``");
        }
        ostream(": ``item.text``");
    }

    "Produce the part of the report dealing with arbitrary-text notes."
    shared actual void produce(DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
            IMapNG map, Anything(String) ostream) {
        {<Integer->[Point, TextFixture]>*} items =
                narrowedStream<Integer, [Point, TextFixture]>(fixtures).sort(
                    byIncreasing(compose(TextFixture.turn,
                        compose(Tuple<TextFixture, TextFixture, []>.first,
                            compose(Tuple<Point|TextFixture, Point, [TextFixture]>.rest,
                                Entry<Integer, [Point, TextFixture]>.item)))));
        if (!items.empty) {
            ostream("""<h4>Miscellaneous Notes</h4>
                       <ul>
                       """);
            for (key->[location, item] in items) {
                fixtures.remove(key);
                ostream("<li>");
                produceSingle(fixtures, map, ostream, item, location);
                ostream("""</li>
                           """);
            }
            ostream("""</ul>
                       """);
        }
    }
}
