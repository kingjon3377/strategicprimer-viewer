import ceylon.collection {
    MutableMap
}

import lovelace.util.common {
    todo,
    DelayedRemovalMap,
    simpleMap
}

import strategicprimer.model.common.map {
    Player,
    IFixture,
    Point,
    MapDimensions,
    IMapNG
}
import strategicprimer.model.common.map.fixtures.towns {
    TownStatus,
    ITownFixture,
    Fortress,
    Village,
    AbstractTown
}

"A report generator for towns."
todo("Figure out some way to report what was found at any of the towns.")
shared class TownReportGenerator(Comparison([Point, IFixture], [Point, IFixture]) comp,
        Player currentPlayer, MapDimensions dimensions, Integer currentTurn,
        Point? hq = null)
        extends AbstractReportGenerator<ITownFixture>(comp, dimensions, hq) {
    "Separate towns by status."
    void separateByStatus<T>(Map<TownStatus, T> mapping,
            Collection<[Point, IFixture]> collection,
            Anything(T, [Point, ITownFixture]) func) {
        for ([loc, item] in collection.narrow<[Point, AbstractTown]>()
                .sort(pairComparator)) {
            if (exists result = mapping[item.status]) {
                func(result, [loc, item]);
            }
        }
    }

    "Produce a report for a town. If a single fortress or village is passed in, handling
     it is delegated to its dedicated report-generating class. We remove the town from
     the set of fixtures."
    shared actual void produceSingle(
            DelayedRemovalMap<Integer,[Point, IFixture]> fixtures,
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
            ostream(" ``distanceString(loc)``");
        }
    }

    void separateByStatusInner(MutableMap<ITownFixture, Point> mapping,
        [Point, ITownFixture] pair) => mapping[pair.rest.first] = pair.first;

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
                simpleMap<TownStatus, MutableMap<ITownFixture, Point>>(
                    TownStatus.abandoned->abandoned, TownStatus.active->active,
                        TownStatus.burned->burned, TownStatus.ruined->ruined);
        // separateByStatus() sorts using pairComparator, which should be by distance
        // from HQ
        separateByStatus(separated, fixtures.items, separateByStatusInner);
        // N.b. Sugaring Iterable<Anything> to {Anything*} won't compile
        if (separated.items.any(not(Iterable<Anything>.empty))) {
            ostream("""<h4>Cities, towns, and/or fortifications you know about:</h4>
                   """);
            for (mapping in [abandoned, active, burned, ruined]) {
                writeMap(ostream, mapping, defaultFormatter(fixtures, map));
            }
        }
    }
}
