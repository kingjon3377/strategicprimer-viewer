import ceylon.collection {
    MutableMap
}
import ceylon.language {
    createMap=map
}

import lovelace.util.common {
    todo,
    DelayedRemovalMap,
	matchingValue
}

import strategicprimer.model.map {
    Point,
    Player,
    IFixture,
    IMapNG,
    invalidPoint,
    MapDimensions
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
        Player currentPlayer, MapDimensions dimensions, Integer currentTurn, Point hq = invalidPoint)
        extends AbstractReportGenerator<ITownFixture>(comp, dimensions, hq) {
    {TownStatus+} statuses = [TownStatus.active, TownStatus.abandoned, TownStatus.ruined,
        TownStatus.burned];
    "Separate towns by status."
    void separateByStatus<T>(Map<TownStatus, T> mapping,
            Collection<[Point, IFixture]> collection,
            Anything(T, [Point, IFixture]) func) {
        for ([loc, item] in collection.narrow<[Point, AbstractTown]>().sort(pairComparator)) {
            if (exists result = mapping[item.status]) {
                func(result, [loc, item]);
            }
        }
    }
    "Produce a report for a town. If a single fortress or village is passed in, handling
     it is delegated to its dedicated report-generating class. We remove the town from
     the set of fixtures."
    shared actual void produceSingle(DelayedRemovalMap<Integer,[Point, IFixture]> fixtures,
            IMapNG map, Anything(String) ostream, ITownFixture item, Point loc) {
        assert (is Village|Fortress|AbstractTown item);
        switch (item)
        case (is Village) {
            VillageReportGenerator(comp, currentPlayer, dimensions, hq)
                .produceSingle(fixtures, map, ostream, item, loc);
        }
        case (is Fortress) {
            FortressReportGenerator(comp, currentPlayer, dimensions, currentTurn, hq)
                .produceSingle(fixtures, map, ostream, item, loc);
        }
        case (is AbstractTown) {
            fixtures.remove(item.id);
            ostream("At ``loc``: ``item.name``, ");
            if (item.owner.independent) {
                ostream("an independent ``item.townSize`` ``item.status`` ``item
                    .kind``");
            } else if (item.owner == currentPlayer) {
                ostream("a ``item.townSize`` ``item.status`` allied with you");
            } else {
                ostream("a ``item.townSize`` ``item.status`` allied with ``
                    item.owner``");
            }
            ostream(" ``distCalculator.distanceString(loc)``");
        }
    }
    "Produce a report on all towns. This report omits fortresses and villages, and is
     sorted in a way that I hope is helpful. We remove the town from the set of fixtures."
    shared actual void produce(DelayedRemovalMap<Integer,[Point, IFixture]> fixtures,
	        IMapNG map, Anything(String) ostream) {
        MutableHeadedMap<ITownFixture, Point> abandoned =
                HeadedMapImpl<ITownFixture, Point>("<h5>Abandoned Communities</h5>");
        MutableHeadedMap<ITownFixture, Point> active =
                HeadedMapImpl<ITownFixture, Point>("<h5>Active Communities</h5>");
        MutableHeadedMap<ITownFixture, Point> burned =
                HeadedMapImpl<ITownFixture, Point>("<h5>Burned-Out Communities</h5>");
        MutableHeadedMap<ITownFixture, Point> ruined =
                HeadedMapImpl<ITownFixture, Point>("<h5>Ruined Communities</h5>");
        Map<TownStatus, MutableMap<ITownFixture, Point>> separated =
                createMap<TownStatus, MutableMap<ITownFixture, Point>>([TownStatus.abandoned->abandoned,
			            TownStatus.active->active, TownStatus.burned->burned, TownStatus.ruined->ruined]);
        // separateByStatus() sorts using pairComparator, which should be by distance
        // from HQ
        separateByStatus(separated, fixtures.items,
                    (MutableMap<ITownFixture, Point> mapping, pair) {
                assert (is ITownFixture town = pair.rest.first);
                mapping[town] = pair.first;
            });
        if (separated.items.any(matchingValue(false, Iterable<Anything>.empty))) {
            ostream("""<h4>Cities, towns, and/or fortifications you know about:</h4>
                   """);
            for (mapping in [abandoned, active, burned, ruined]) {
                writeMap(ostream, mapping,
                            (ITownFixture key->Point val, formatter) =>
                    produceSingle(fixtures, map, formatter, key, val));
            }
        }
    }
    "Produce a report for a town. Handling of fortresses and villages is delegated
     to their dedicated report-generating classes. We remove the town from the set of fixtures."
    shared actual IReportNode produceRIRSingle(
            DelayedRemovalMap<Integer,[Point, IFixture]> fixtures,
            IMapNG map, ITownFixture item, Point loc) {
        assert (is Village|Fortress|AbstractTown item);
        switch (item)
        case (is Village) {
            return VillageReportGenerator(comp, currentPlayer, dimensions, hq)
                .produceRIRSingle(fixtures, map, item, loc);
        }
        case (is Fortress) {
            return FortressReportGenerator(comp, currentPlayer, dimensions,
                currentTurn, hq).produceRIRSingle(fixtures, map, item, loc);
        }
        case (is AbstractTown) {
            fixtures.remove(item.id);
            if (item.owner.independent) {
                return SimpleReportNode("At ``loc``: ``item.name``, an independent ``
                    item.townSize`` ``item.status`` ``item.kind`` ``distCalculator
                    .distanceString(loc)``", loc);
            } else if (item.owner == currentPlayer) {
                return SimpleReportNode("At ``loc``: ``item.name``, a ``item.townSize
                    `` ``item.status`` ``item.kind`` allied with you ``distCalculator
                        .distanceString(loc)``", loc);
            } else {
                return SimpleReportNode("At ``loc``: ``item.name``, a ``item.townSize
                    `` ``item.status`` ``item.kind`` allied with ``item.owner`` ``
                    distCalculator.distanceString(loc)``", loc);
            }
        }
    }
    "Produce a report for all towns. (Fortresses and villages are not included in this report.)
     We remove the towns from the set of fixtures."
    shared actual IReportNode produceRIR(DelayedRemovalMap<Integer,[Point, IFixture]> fixtures,
	        IMapNG map) {
        Map<TownStatus, IReportNode> separated = createMap([TownStatus.abandoned -> SectionListReportNode(5,
                "Abandoned Communities"),
                TownStatus.active->SectionListReportNode(5, "Active Communities"),
                TownStatus.burned->SectionListReportNode(5,
                    "Burned-Out Communities"),
                TownStatus.ruined->SectionListReportNode(5,
                    "Ruined Communities")]);
        separateByStatus(separated, fixtures.items,
                    (IReportNode node, pair) {
                assert (is ITownFixture town = pair.rest.first);
                node.appendNode(produceRIRSingle(fixtures, map, town, pair.first));
            });
        IReportNode retval = SectionListReportNode(4,
            "Cities, towns, and/or fortifications you know about:");
        retval.addIfNonEmpty(*statuses.map(separated.get).coalesced);
        if (retval.childCount == 0) {
            return emptyReportNode;
        } else {
            return retval;
        }
    }
}
