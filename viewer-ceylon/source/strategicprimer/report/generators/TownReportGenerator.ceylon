import ceylon.collection {
    MutableList,
    MutableMap,
    ArrayList,
    HashMap
}
import ceylon.language {
    createMap=map
}

import java.lang {
    IllegalStateException
}

import lovelace.util.common {
    todo,
    DelayedRemovalMap
}

import strategicprimer.model {
    DistanceComparator
}
import strategicprimer.model.map {
    Point,
    Player,
    IFixture,
    IMapNG,
    invalidPoint
}
import strategicprimer.model.map.fixtures.towns {
    TownStatus,
    ITownFixture,
    Fortress,
    Village,
    AbstractTown
}
import strategicprimer.report {
    IReportNode
}
import strategicprimer.report.nodes {
    SimpleReportNode,
    SectionListReportNode,
    emptyReportNode
}
"A report generator for towns."
todo("Figure out some way to report what was found at any of the towns.")
shared class TownReportGenerator(Comparison([Point, IFixture], [Point, IFixture]) comp,
        Player currentPlayer, Point hq = invalidPoint)
        extends AbstractReportGenerator<ITownFixture>(comp, DistanceComparator(hq)) {
    {TownStatus+} statuses = {TownStatus.active, TownStatus.abandoned, TownStatus.ruined,
        TownStatus.burned};
    "Separate towns by status."
    void separateByStatus<T>(Map<TownStatus, T> mapping,
            Collection<[Point, IFixture]> collection,
            Anything(T, [Point, IFixture]) func) {
        MutableList<[Point, IFixture]> list = ArrayList<[Point, IFixture]>();
        for (pair in collection) {
            if (pair.rest.first is AbstractTown) {
                list.add(pair);
            }
        }
        for ([loc, item] in list.sort(pairComparator)) {
            if (is ITownFixture item, exists result = mapping.get(item.status)) {
                func(result, [loc, item]);
            }
        }
    }
    "Produce a report for a town, or all towns. If a single fortress or village is passed
     in, handling it is delegated to its dedicated report-generating classes. The
     all-towns report omits fortresses and villages, and is sorted in a way that I hope
     is helpful. We remove the town(s) from the set of fixtures."
    shared actual void produce(DelayedRemovalMap<Integer,[Point, IFixture]> fixtures, IMapNG map,
            Anything(String) ostream, [ITownFixture, Point]? entry) {
        if (exists entry) {
            ITownFixture item = entry.first;
            Point loc = entry.rest.first;
            if (is Village item) {
                VillageReportGenerator(comp, currentPlayer)
                    .produce(fixtures, map, ostream, [item, loc]);
            } else if (is Fortress item) {
                FortressReportGenerator(comp, currentPlayer)
                    .produce(fixtures, map, ostream, [item, loc]);
            } else if (is AbstractTown item) {
                fixtures.remove(item.id);
                ostream("At ``loc``: ``item.name``, ");
                if (item.owner.independent) {
                    ostream("an independent ``item.townSize`` ``item.status`` ``item.kind``");
                } else {
                    ostream("a ``item.townSize`` ``item.status`` allied with ``
                        playerNameOrYou(item.owner)``");
                }
                ostream(" ``distCalculator.distanceString(loc)``");
            } else {
                throw IllegalStateException("Unhandled ITownFixture subclass");
            }
        } else {
            HeadedMap<ITownFixture, Point>&MutableMap<ITownFixture, Point> abandoned =
                    HeadedMapImpl<ITownFixture, Point>("<h5>Abandoned Communities</h5>");
            HeadedMap<ITownFixture, Point>&MutableMap<ITownFixture, Point> active =
                    HeadedMapImpl<ITownFixture, Point>("<h5>Active Communities</h5>");
            HeadedMap<ITownFixture, Point>&MutableMap<ITownFixture, Point> burned =
                    HeadedMapImpl<ITownFixture, Point>("<h5>Burned-Out Communities</h5>");
            HeadedMap<ITownFixture, Point>&MutableMap<ITownFixture, Point> ruined =
                    HeadedMapImpl<ITownFixture, Point>("<h5>Ruined Communities</h5>");
            Map<TownStatus, MutableMap<ITownFixture, Point>> separated =
                    createMap<TownStatus, MutableMap<ITownFixture, Point>> {
                        *{ TownStatus.abandoned->abandoned, TownStatus.active->active,
                            TownStatus.burned->burned, TownStatus.ruined->ruined }
                    };
            // separateByStatus() sorts using pairComparator, which should be by distance
            // from HQ
            separateByStatus(separated, fixtures.items,
                        (MutableMap<ITownFixture, Point> mapping, pair) {
                    assert (is ITownFixture town = pair.rest.first);
                    mapping.put(town, pair.first);
                });
            if (separated.items.any((mapping) => !mapping.empty)) {
                ostream("""<h4>Cities, towns, and/or fortifications you know about:</h4>
                       """);
                for (mapping in {abandoned, active, burned, ruined}) {
                    writeMap(ostream, mapping,
                                (ITownFixture key->Point val, formatter) =>
                        produce(fixtures, map, formatter, [key, val]));
                }
            }
        }
    }
    "Produce a report for a town or towns. Handling of fortresses and villages passed as
     [[entry]] is delegated to their dedicated report-generating classes. We remove the
     town from the set of fixtures."
    shared actual IReportNode produceRIR(DelayedRemovalMap<Integer,[Point, IFixture]> fixtures,
            IMapNG map, [ITownFixture, Point]? entry) {
        if (exists entry) {
            ITownFixture item = entry.first;
            Point loc = entry.rest.first;
            if (is Village item) {
                return VillageReportGenerator(comp, currentPlayer)
                    .produceRIR(fixtures, map, [item, loc]);
            } else if (is Fortress item) {
                return FortressReportGenerator(comp, currentPlayer)
                    .produceRIR(fixtures, map, [item, loc]);
            } else if (is AbstractTown item) {
                fixtures.remove(item.id);
                if (item.owner.independent) {
                    return SimpleReportNode("At ``loc``: ``item.name``, an independent ``item.townSize`` ``item
                        .status`` ``item.kind`` ``distCalculator
                        .distanceString(loc)``", loc);
                } else {
                    return SimpleReportNode("At ``loc``: ``item.name``, a ``item.townSize`` ``item
                        .status`` ``item.kind`` allied with ``playerNameOrYou(item.owner)
                        `` ``distCalculator.distanceString(loc)``", loc);
                }
            } else {
                throw IllegalStateException("Unhandled ITownFixture subclass");
            }
        } else {
            Map<TownStatus, IReportNode> separated = HashMap<TownStatus, IReportNode> {
                *{TownStatus.abandoned -> SectionListReportNode(5,
                    "Abandoned Communities"),
                    TownStatus.active->SectionListReportNode(5, "Active Communities"),
                    TownStatus.burned->SectionListReportNode(5,
                        "Burned-Out Communities"),
                    TownStatus.ruined->SectionListReportNode(5,
                        "Ruined Communities") }
            };
            separateByStatus(separated, fixtures.items,
                        (IReportNode node, pair) {
                    assert (is ITownFixture town = pair.rest.first);
                    node.appendNode(produceRIR(fixtures, map, [town, pair.first]));
                });
            IReportNode retval = SectionListReportNode(4,
                "Cities, towns, and/or fortifications you know about:");
            for (status in statuses) {
                if (exists node = separated.get(status)) {
                    retval.addIfNonEmpty(node);
                }
            }
            if (retval.childCount == 0) {
                return emptyReportNode;
            } else {
                return retval;
            }
        }
    }
}
