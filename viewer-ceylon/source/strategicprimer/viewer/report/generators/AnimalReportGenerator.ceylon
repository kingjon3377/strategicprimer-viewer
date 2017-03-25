import ceylon.collection {
    MutableList,
    MutableMap,
    ArrayList,
    HashMap
}

import lovelace.util.common {
    todo,
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
import model.map.fixtures.mobile {
    Animal
}

import strategicprimer.viewer.report.nodes {
    IReportNode,
    SimpleReportNode,
    ListReportNode,
    emptyReportNode,
    SectionListReportNode
}
"A report generator for sightings of animals."
todo("Ensure that animal-tracks' synthetic IDs are used to remove them")
shared class AnimalReportGenerator(Comparison([Point, IFixture], [Point, IFixture]) comp, Point hq = PointFactory.invalidPoint)
        extends AbstractReportGenerator<Animal>(comp, DistanceComparator(hq)) {
    "Produce the sub-report about animals or an individual Animal."
    shared actual void produce(DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
            IMapNG map, Anything(String) ostream, [Animal, Point]? entry) {
        if (exists entry) {
            Animal item = entry.first;
            Point loc = entry.rest.first;
            ostream("At ``loc``:");
            if (item.traces) {
                ostream(" tracks or traces of");
            } else if (item.talking) {
                ostream(" talking");
            }
            ostream(" ``item.kind`` ``distCalculator.distanceString(loc)``");
        } else {
            MutableList<[Point, IFixture]> values =
                    ArrayList<[Point, IFixture]> { *fixtures.items
                        .sort(pairComparator) };
            MutableMap<String, MutableList<Point>> items =
                    HashMap<String, MutableList<Point>>();
            for ([loc, item] in values) {
                if (is Animal animal = item) {
                    String desc;
                    if (animal.traces) {
                        desc = "tracks or traces of ``animal.kind``";
                    } else if (animal.talking) {
                        desc = "talking ``animal.kind``";
                    } else {
                        desc = animal.kind;
                    }
                    MutableList<Point> list;
                    if (exists temp = items.get(desc)) {
                        list = temp;
                    } else {
                        list = PointList("``desc``: at ");
                        items.put(desc, list);
                    }
                    list.add(loc);
                    if (animal.id > 0) {
                        fixtures.remove(animal.id);
                    } else {
                        for (key->val in fixtures) {
                            if (val == [loc, item]) {
                                fixtures.remove(key);
                            }
                        }
                    }
                }
            }
            if (!items.empty) {
                ostream("""<h4>Animal sightings or encounters</h4>
                            <ul>
                            """);
                for (key->list in items) {
                    ostream("<li>``key``: ``list.string``</li>
                         ");
                }
                ostream("""</ul>
                       """);
            }
        }
    }
    "Produce the sub-report about animals or an individual Animal."
    shared actual IReportNode produceRIR(DelayedRemovalMap<Integer,[Point,IFixture]> fixtures,
            IMapNG map, [Animal, Point]? entry) {
        if (exists entry) {
            Animal item = entry.first;
            Point loc = entry.rest.first;
            if (item.traces) {
                return SimpleReportNode("At ``loc``: tracks or traces of ``item
                    .kind`` ``distCalculator.distanceString(loc)``", loc);
            } else if (item.talking) {
                return SimpleReportNode("At ``loc``: talking ``item
                    .kind`` ``distCalculator.distanceString(loc)``", loc);
            } else {
                return SimpleReportNode("At ``loc``: ``item.kind`` ``distCalculator
                    .distanceString(loc)``", loc);
            }
        } else {
            MutableList<[Point, IFixture]> values =
                    ArrayList<[Point, IFixture]> { *fixtures.items
                        .sort(pairComparator) };
            MutableMap<String, IReportNode> items = HashMap<String, IReportNode>();
            for ([loc, item] in values) {
                if (is Animal animal = item) {
                    IReportNode node;
                    if (exists temp = items.get(animal.kind)) {
                        node = temp;
                    } else {
                        node = ListReportNode(animal.kind);
                        items.put(animal.kind, node);
                    }
                    node.appendNode(produceRIR(fixtures, map, [animal, loc]));
                    if (animal.id > 0) {
                        fixtures.remove(animal.id);
                    } else {
                        for (key->val in fixtures) {
                            if (val == [loc, item]) {
                                fixtures.remove(key);
                            }
                        }
                    }
                }
            }
            if (items.empty) {
                return emptyReportNode;
            } else {
                IReportNode retval = SectionListReportNode(4,
                    "Animal sightings or encounters");
                for (node in items.items) {
                    retval.appendNode(node);
                }
                return retval;
            }
        }
    }
}
