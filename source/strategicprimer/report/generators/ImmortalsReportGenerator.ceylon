import ceylon.collection {
    MutableList,
    MutableMap,
    HashMap,
    ArrayList
}
import ceylon.language.meta {
    type
}
import ceylon.language.meta.model {
    Type
}

import lovelace.util.common {
    DelayedRemovalMap
}

import strategicprimer.model.map {
    Point,
    IFixture,
    IMapNG,
    invalidPoint,
    MapDimensions
}
import strategicprimer.model.map.fixtures.mobile {
    Centaur,
    Fairy,
    Giant,
    SimpleImmortal,
    Dragon,
    Immortal,
	Ogre,
	Troll,
	Sphinx,
	Phoenix,
	Griffin,
	Djinn,
	Simurgh,
	Minotaur
}
import strategicprimer.report {
    IReportNode
}
import strategicprimer.report.nodes {
    SimpleReportNode,
    ListReportNode,
    SectionListReportNode,
    emptyReportNode
}
"""A report generator for "immortals"---dragons, fairies, centaurs, and such."""
shared class ImmortalsReportGenerator(
        Comparison([Point, IFixture], [Point, IFixture]) comp, MapDimensions dimensions,
        Point hq = invalidPoint)
        extends AbstractReportGenerator<Immortal>(comp, dimensions, hq) {
    "Produce a report on an individual immortal, or on all immortals."
    shared actual void produce(DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
            IMapNG map, Anything(String) ostream, [Immortal, Point]? entry) {
        if (exists entry) {
            Immortal item = entry.first;
            Point loc = entry.rest.first;
            fixtures.remove(item.id);
            ostream("At ``loc``: A(n) ``item`` ``distCalculator
                .distanceString(loc)``");
        } else {
            MutableList<[Point, IFixture]> values =
                    ArrayList<[Point, IFixture]> { *fixtures.items
                        .sort(pairComparator) };
            MutableMap<Type<IFixture>, Anything(String, Point)> meta =
                    HashMap<Type<IFixture>, Anything(String, Point)>();
            MutableMap<Type<SimpleImmortal>,
                    HeadedList<Point>&MutableList<Point>> simples =
                    HashMap<Type<SimpleImmortal>,
                    HeadedList<Point>&MutableList<Point>>();
            void handleSimple(Type<SimpleImmortal> type, String plural) {
                meta.put(type, (_, point) {
                    if (exists list = simples[type]) {
                        list.add(point);
                    } else {
                        value list = PointList("``plural`` at ");
                        simples[type] = list;
                        list.add(point);
                    }
                    });
            }
            handleSimple(`Sphinx`, "Sphinx(es)");
            handleSimple(`Djinn`, "Djinn(i)");
            handleSimple(`Griffin`, "Griffin(s)");
            handleSimple(`Minotaur`, "Minotaur(s)");
            handleSimple(`Ogre`, "Ogre(s)");
            handleSimple(`Phoenix`, "Phoenix(es)");
            handleSimple(`Simurgh`, "Simurgh(s)");
            handleSimple(`Troll`, "Troll(s)");
            MutableMap<String, MutableList<Point>> handleComplex(Type<Immortal> type,
                    String plural = "(s)") {
                MutableMap<String, MutableList<Point>> retval =
                        HashMap<String, MutableList<Point>>();
                // If we use Correspondence syntax sugar, we have to specify types
                // for the lambda.
                meta.put(type, (kind, point) {
                    if (exists list = retval[kind]) {
                        list.add(point);
                    } else {
                        value list = PointList("``kind````plural`` at ");
                        retval[kind] = list;
                        list.add(point);
                    }
                });
                return retval;
            }
            MutableMap<String, MutableList<Point>> centaurs = handleComplex(`Centaur`);
            MutableMap<String, MutableList<Point>> giants = handleComplex(`Giant`);
            MutableMap<String, MutableList<Point>> fairies = handleComplex(`Fairy`, "");
            MutableMap<String, MutableList<Point>> dragons = handleComplex(`Dragon`);
            for ([point, immortal] in values) {
                if (exists func = meta[type(immortal)]) {
                    func(immortal.string, point);
                    fixtures.remove(immortal.id);
                }
            }
            if (!centaurs.empty || !giants.empty, !fairies.empty || !dragons.empty ||
            !simples.empty) {
                ostream("""<h4>Immortals</h4>
                       """);
                for (coll in {centaurs.items, giants.items, fairies.items, dragons.items,
                    simples.items}) {
                    for (inner in coll) {
                        ostream(inner.string);
                    }
                }
            }
        }
    }
    "Produce a report node on an individual immortal, or the intermediate-representation
     report on all immortals."
    shared actual IReportNode produceRIR(
            DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
            IMapNG map, [Immortal, Point]? entry) {
        if (exists entry) {
            Immortal item = entry.first;
            Point loc = entry.rest.first;
            fixtures.remove(item.id);
            return SimpleReportNode("At ``loc``: A(n) ``item`` ``distCalculator
                .distanceString(loc)``", loc);
        } else {
            MutableList<[Point, IFixture]> values =
                    ArrayList<[Point, IFixture]> { *fixtures.items
                        .sort(pairComparator) };
            MutableMap<String, IReportNode> simples = HashMap<String, IReportNode>();
            MutableMap<String, IReportNode> centaurs = HashMap<String, IReportNode>();
            MutableMap<String, IReportNode> giants = HashMap<String, IReportNode>();
            MutableMap<String, IReportNode> fairies = HashMap<String, IReportNode>();
            MutableMap<String, IReportNode> dragons = HashMap<String, IReportNode>();
            IReportNode separateByKind(MutableMap<String, IReportNode> mapping,
                    Immortal item) {
                // For the classes we deal with here, we don't want just the kind, we want
                // the full `string`, so we use that instead of specifying HasKind and
                // using `kind`.
                if (exists node = mapping[item.string]) {
                    return node;
                } else {
                    IReportNode node = ListReportNode(item.string);
                    mapping[item.string] = node;
                    return node;
                }
            }
            for ([point, item] in values) {
                IFixture immortal = item;
                if (is Dragon immortal) {
                    separateByKind(dragons, immortal)
                        .appendNode(produceRIR(fixtures, map, [immortal, point]));
                } else if (is Fairy immortal) {
                    separateByKind(fairies, immortal)
                        .appendNode(produceRIR(fixtures, map, [immortal, point]));
                } else if (is SimpleImmortal immortal) {
                    IReportNode node;
                    if (exists temp = simples[immortal.plural]) {
                        node = temp;
                    } else {
                        node = ListReportNode(immortal.plural);
                        simples[immortal.plural] = node;
                    }
                    node.appendNode(produceRIR(fixtures, map, [immortal, point]));
                } else if (is Giant immortal) {
                    separateByKind(giants, immortal)
                        .appendNode(produceRIR(fixtures, map, [immortal, point]));
                } else if (is Centaur immortal) {
                    separateByKind(centaurs, immortal)
                        .appendNode(produceRIR(fixtures, map, [immortal, point]));
                }
            }
            IReportNode retval = SectionListReportNode(4, "Immortals");
            retval.addIfNonEmpty(*simples.items);
            IReportNode coalesce(String header, Map<String, IReportNode> mapping) {
                IReportNode retval = ListReportNode(header);
                retval.addIfNonEmpty(*mapping.items);
                return retval;
            }
            retval.addIfNonEmpty(coalesce("Dragons", dragons),
                coalesce("Fairies", fairies), coalesce("Giants", giants),
                coalesce("Centaurs", centaurs));
            if (retval.childCount == 0) {
                return emptyReportNode;
            } else {
                return retval;
            }
        }
    }
}
