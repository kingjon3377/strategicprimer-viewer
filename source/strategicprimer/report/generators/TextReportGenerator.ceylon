import lovelace.util.common {
    DelayedRemovalMap,
    comparingOn,
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
import strategicprimer.report {
    IReportNode
}
import strategicprimer.report.nodes {
    SimpleReportNode,
    SectionListReportNode,
    emptyReportNode
}

"A report generator for arbitrary-text notes."
shared class TextReportGenerator(Comparison([Point, IFixture], [Point, IFixture]) comp,
        MapDimensions dimensions, Point hq = Point.invalidPoint)
        extends AbstractReportGenerator<TextFixture>(comp, dimensions, hq) {
    "Produce the part of the report dealing with an arbitrary-text note. This does *not*
     remove it from the collection, because this method doesn't know the synthetic ID #
     that was assigned to it."
    shared actual void produceSingle(
            DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
            IMapNG map, Anything(String) ostream, TextFixture item, Point loc) {
        ostream("At ``loc`` ``distCalculator.distanceString(loc)``");
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
                    comparingOn(compose(TextFixture.turn,
                        compose(Tuple<TextFixture, TextFixture, []>.first,
                            compose(Tuple<Point|TextFixture, Point, [TextFixture]>.rest,
                                Entry<Integer, [Point, TextFixture]>.item))),
                        increasing<Integer>));
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

    "Produce the part of the report dealing with an arbitrary-text note, in report
     intermediate representation. This does *not* remove it from the collection, because
     this method doesn't know the synthetic ID # that was assigned to it."
    shared actual IReportNode produceRIRSingle(
            DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
            IMapNG map, TextFixture item, Point loc) {
        if (item.turn>=0) {
            return SimpleReportNode("At ``loc`` ``distCalculator
                .distanceString(loc)`` On turn ``item
                .turn``: ``item.text``");
        } else {
            return SimpleReportNode("At ``loc`` ``distCalculator
                .distanceString(loc)``: ``item.text``");
        }
    }

    "Produce the part of the report dealing with arbitrary-text note(s), in
     report intermediate representation."
    shared actual IReportNode produceRIR(
            DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
            IMapNG map) {
        IReportNode retval = SectionListReportNode(4, "Miscellaneous Notes");
        for (key->[loc, item] in fixtures) {
            if (is TextFixture fixture = item) {
                retval.appendNode(produceRIRSingle(fixtures, map, fixture, loc));
                fixtures.remove(key);
            }
        }
        if (retval.childCount > 0) {
            return retval;
        } else {
            return emptyReportNode;
        }
    }
}
