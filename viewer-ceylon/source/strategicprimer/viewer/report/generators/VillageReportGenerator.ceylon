import ceylon.collection {
    MutableList,
    ArrayList,
    MutableMap,
    HashMap
}

import lovelace.util.common {
    DelayedRemovalMap
}

import strategicprimer.viewer.model {
    DistanceComparator
}
import strategicprimer.viewer.model.map {
    Point,
    IFixture,
    IMapNG,
    invalidPoint,
    Player
}
import strategicprimer.viewer.model.map.fixtures.towns {
    Village
}
import strategicprimer.viewer.report.nodes {
    IReportNode,
    SimpleReportNode,
    SectionListReportNode,
    SectionReportNode,
    emptyReportNode
}
"A report generator for Villages."
shared class VillageReportGenerator(Comparison([Point, IFixture], [Point, IFixture]) comp, Player currentPlayer, Point hq = invalidPoint)
        extends AbstractReportGenerator<Village>(comp, DistanceComparator(hq)) {
    "Produce the (very brief) report for a particular village (we're probably in the
     middle of a bulleted list, but we don't assume that), or the report on all known
     villages."
    shared actual void produce(DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
            IMapNG map, Anything(String) ostream, [Village, Point]? entry) {
        if (exists entry) {
            Village item = entry.first;
            Point loc = entry.rest.first;
            fixtures.remove(item.id);
            ostream("At ``loc``: ``item.name``, a(n) ``item.race`` village, ");
            if (item.owner.independent) {
                ostream("independent");
            } else if (item.owner == currentPlayer) {
                ostream("sworn to you");
            } else {
                ostream("sworn to ``item.owner.name``");
            }
            ostream(" ``distCalculator.distanceString(loc)``");
        } else {
            MutableList<[Point, IFixture]> values =
                    ArrayList<[Point, IFixture]> { *fixtures.items
                        .sort(pairComparator) };
            value villageComparator = comparing(byIncreasing(Village.name),
                byIncreasing(Village.race), byIncreasing(Village.id));
            // TODO: sort by distance somehow?
            HeadedMap<Village, Point>&MutableMap<Village, Point> own =
                    HeadedMapImpl<Village, Point>(
                        "<h4>Villages pledged to your service:</h4>", villageComparator);
            HeadedMap<Village, Point>&MutableMap<Village, Point> independents =
                    HeadedMapImpl<Village, Point>(
                        "<h4>Villages you think are independent:</h4>", villageComparator);
            MutableMap<Player, HeadedMap<Village, Point>
            &MutableMap<Village, Point>> others =
                    HashMap<Player, HeadedMap<Village, Point>
                    &MutableMap<Village, Point>>();
            for ([loc, item] in values) {
                if (is Village village = item) {
                    if (village.owner == currentPlayer) {
                        own.put(village, loc);
                    } else if (village.owner.independent) {
                        independents.put(village, loc);
                    } else {
                        HeadedMap<Village, Point>&MutableMap<Village, Point> mapping;
                        if (exists temp = others.get(village.owner)) {
                            mapping = temp;
                        } else {
                            mapping = HeadedMapImpl<Village, Point>(
                                "<h5>Villages sworn to ``village.owner.name``</h5>
                                 <ul>
                                 ", villageComparator);
                            others.put(village.owner, mapping);
                        }
                        mapping.put(village, loc);
                    }
                }
            }
            Anything(Village->Point, Anything(String)) writer =
                            (Village key->Point val, Anything(String) formatter) =>
                    produce(fixtures, map, formatter, [key, val]);
            writeMap(ostream, own, writer);
            writeMap(ostream, independents, writer);
            if (!others.empty) {
                ostream("""<h4>Other villages you know about:</h4>
                       """);
                for (mapping in others.items) {
                    writeMap(ostream, mapping, writer);
                }
            }
        }
    }
    "Produce the (very brief) report for a particular village, or the report on all
     known villages."
    shared actual IReportNode produceRIR(DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
            IMapNG map, [Village, Point]? entry) {
        if (exists entry) {
            Village item = entry.first;
            Point loc = entry.rest.first;
            fixtures.remove(item.id);
            if (item.owner.independent) {
                return SimpleReportNode("At ``loc``: ``item.name``, a(n) ``item
                    .race`` village, independent ``distCalculator.distanceString(loc)``", loc);
            } else if (item.owner == currentPlayer) {
                return SimpleReportNode("At ``loc``: ``item.name``, a(n) ``item
                    .race`` village, sworn to you ``distCalculator
                    .distanceString(loc)``", loc);
            } else {
                return SimpleReportNode("At ``loc``: ``item.name``, a(n) ``item
                    .race`` village, sworn to ``item.owner`` ``distCalculator
                    .distanceString(loc)``", loc);
            }
        } else {
            MutableList<[Point, IFixture]> values =
                    ArrayList<[Point, IFixture]> { *fixtures.items
                        .sort(pairComparator) };
            IReportNode own = SectionListReportNode(5,
                "Villages pledged to your service:");
            IReportNode independents =
                    SectionListReportNode(5, "Villages you think are independent:");
            MutableMap<Player, IReportNode> othersMap = HashMap<Player, IReportNode>();
            for ([loc, item] in values) {
                if (is Village village = item) {
                    Player owner = village.owner;
                    IReportNode parent;
                    if (owner == currentPlayer) {
                        parent = own;
                    } else if (owner.independent) {
                        parent = independents;
                    } else if (exists temp = othersMap.get(owner)) {
                        parent = temp;
                    } else {
                        parent = SectionListReportNode(6, "Villages sworn to ``owner``");
                        othersMap.put(owner, parent);
                    }
                    parent.appendNode(produceRIR(fixtures, map, [village, loc]));
                }
            }
            IReportNode others = SectionListReportNode(5,
                "Other villages you know about:");
            others.addIfNonEmpty(*othersMap.items);
            IReportNode retval = SectionReportNode(4, "Villages:");
            retval.addIfNonEmpty(own, independents, others);
            if (retval.childCount == 0) {
                return emptyReportNode;
            } else {
                return retval;
            }
        }
    }
}
