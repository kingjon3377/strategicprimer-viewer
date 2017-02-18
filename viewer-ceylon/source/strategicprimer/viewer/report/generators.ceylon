import util {
    PairComparator,
    PatientMap,
    Pair
}
import model.map {
    Point,
    IFixture,
    IMapNG,
    Player
}
import controller.map.report {
    AbstractReportGenerator
}
import model.map.fixtures {
    TextFixture
}
import java.lang {
    JInteger = Integer
}
import java.util {
    Formatter
}
import ceylon.collection {
    MutableList,
    ArrayList
}
import model.report {
    IReportNode,
    SectionListReportNode,
    SimpleReportNode,
    EmptyReportNode
}
"A report generator for arbitrary-text notes."
class TextReportGenerator(PairComparator<Point, IFixture> comp)
        extends AbstractReportGenerator<TextFixture>(comp) {
    "Produce the part of the report dealing ith an individual arbitrary-text note. This
     does *not* remove the fixture from the collection, because this method doesn't know
     the synthetic ID # that was assigned to it."
    shared actual void produce(PatientMap<JInteger, Pair<Point, IFixture>> fixtures,
            IMapNG map, Player currentPlayer, TextFixture item, Point loc,
            Formatter ostream) {
        ostream.format("At %s %s", loc.string, distCalculator.distanceString(loc));
        if (item.turn >= 0) {
            ostream.format(": On turn %d", item.turn);
        }
        ostream.format(": %s", item.text);
    }
    "Produce the sub-report dealing with arbitrary-text notes."
    shared actual void produce(PatientMap<JInteger, Pair<Point, IFixture>> fixtures,
            IMapNG map, Player currentPlayer, Formatter ostream) {
        MutableList<[Point, TextFixture]> items = ArrayList<[Point, TextFixture]>();
        for (entry in fixtures.entrySet()) {
            value pair = entry.\ivalue;
            if (is TextFixture fixture = pair.second()) {
                items.add([pair.first(), fixture]);
                fixtures.remove(entry.key);
            }
        }
        items.sort(([Point, TextFixture] x, [Point, TextFixture] y) =>
                x[1].turn <=> y[1].turn);
        if (!items.empty) {
            ostream.format("<h4>Miscellaneous Notes</h4>%n<ul>%n");
            for ([location, item] in items) {
                ostream.format("<li>");
                produce(fixtures, map, currentPlayer, item, location, ostream);
                ostream.format("</li>%n");
            }
            ostream.format("</ul>%n");
        }
    }
    "Produce the part of the report dealing ith an individual arbitrary-text note, in
     report intermediate representation. This does *not* remove the fixture from the
     collection, because this method doesn't know the synthetic ID # that was assigned to
     it."
    shared actual IReportNode produceRIR(
            PatientMap<JInteger, Pair<Point, IFixture>> fixtures, IMapNG map,
            Player currentPlayer, TextFixture item, Point loc) {
        if (item.turn >= 0) {
            return SimpleReportNode(
                "At ``loc`` ``distCalculator.distanceString(loc)`` On turn ``item
                    .turn``: ``item.text``");
        } else {
            return SimpleReportNode(
                "At ``loc`` ``distCalculator.distanceString(loc)``: ``item.text``");
        }
    }
    "Produce the sub-report, in report-intermediate-representation, dealing with
     arbitrary-text notes."
    shared actual IReportNode produceRIR(
            PatientMap<JInteger, Pair<Point, IFixture>> fixtures, IMapNG map,
            Player currentPlayer) {
        IReportNode retval = SectionListReportNode(4, "Miscellaneous Notes");
        for (entry in fixtures.entrySet()) {
            value pair = entry.\ivalue;
            if (is TextFixture fixture = pair.second()) {
                retval.add(produceRIR(fixtures, map, currentPlayer, fixture,
                    pair.first()));
                fixtures.remove(entry.key);
            }
        }
        if (retval.childCount > 0) {
            return retval;
        } else {
            return EmptyReportNode.nullNode;
        }
    }
}