import ceylon.collection {
    MutableList,
    ArrayList
}

import lovelace.util.common {
    DelayedRemovalMap
}

import model.map {
    IFixture,
    PointFactory,
    Point
}
import strategicprimer.viewer.model {
    DistanceComparator
}
import strategicprimer.viewer.model.map {
    IMapNG
}
import strategicprimer.viewer.model.map.fixtures {
    TextFixture
}

import strategicprimer.viewer.report.nodes {
    IReportNode,
    SimpleReportNode,
    SectionListReportNode,
    emptyReportNode
}
"A report generator for arbitrary-text notes."
shared class TextReportGenerator(Comparison([Point, IFixture], [Point, IFixture]) comp, Point hq = PointFactory.invalidPoint)
        extends AbstractReportGenerator<TextFixture>(comp, DistanceComparator(hq)) {
    "Produce the part of the report dealing with arbitrary-text notes. If an individual
     note is specified, this does *not* remove it from the collection, because this
     method doesn't know the synthetic ID # that was assigned to it."
    shared actual void produce(DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
            IMapNG map, Anything(String) ostream, [TextFixture, Point]? entry) {
        if (exists entry) {
            TextFixture item = entry.first;
            Point loc = entry.rest.first;
            ostream("At ``loc`` ``distCalculator.distanceString(loc)``");
            if (item.turn>=0) {
                ostream(": On turn ``item.turn``");
            }
            ostream(": ``item.text``");
        } else {
            MutableList<[Point, TextFixture]> items = ArrayList<[Point, TextFixture]>();
            for (key->tuple in fixtures) {
                Point loc = tuple.first;
                IFixture item = tuple.rest.first;
                if (is TextFixture fixture = item) {
                    items.add([loc, fixture]);
                    fixtures.remove(key);
                }
            }
            List<[Point, TextFixture]> retItems = items.sort(
                        ([Point, TextFixture] x, [Point, TextFixture] y) =>
                x[1].turn <=> y[1].turn);
            if (!retItems.empty) {
                ostream("""<h4>Miscellaneous Notes</h4>
                           <ul>
                           """);
                for ([location, item] in retItems) {
                    ostream("<li>");
                    produce(fixtures, map, ostream, [item, location]);
                    ostream("""</li>
                           """);
                }
                ostream("""</ul>
                       """);
            }
        }
    }
    "Produce the part of the report dealing with arbitrary-text note(s), in
     report intermediate representation. If an individual note is specified, this does
     *not* remove it from the collection, because this method doesn't know the synthetic
     ID # that was assigned to it."
    shared actual IReportNode produceRIR(DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
            IMapNG map, [TextFixture, Point]? entry) {
        if (exists entry) {
            TextFixture item = entry.first;
            Point loc = entry.rest.first;
            if (item.turn>=0) {
                return SimpleReportNode("At ``loc`` ``distCalculator
                    .distanceString(loc)`` On turn ``item
                    .turn``: ``item.text``");
            } else {
                return SimpleReportNode("At ``loc`` ``distCalculator
                    .distanceString(loc)``: ``item.text``");
            }
        } else {
            IReportNode retval = SectionListReportNode(4, "Miscellaneous Notes");
            for (key->tuple in fixtures) {
                Point loc = tuple.first;
                IFixture item = tuple.rest.first;
                if (is TextFixture fixture = item) {
                    retval.appendNode(produceRIR(fixtures, map, [fixture,
                        loc]));
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
}
