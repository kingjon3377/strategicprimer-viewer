import ceylon.collection {
    MutableMap,
    HashMap
}

import lovelace.util.common {
    DelayedRemovalMap
}

import strategicprimer.model.common.map {
    Player,
    IFixture,
    Point,
    MapDimensions,
    IMapNG
}
import strategicprimer.model.common.map.fixtures {
    IResourcePile,
    Implement,
    FortressMember
}
import strategicprimer.model.common.map.fixtures.mobile {
    IUnit
}

"A report generator for equipment and resources."
shared class FortressMemberReportGenerator(
            Comparison([Point, IFixture], [Point, IFixture]) comp, Player currentPlayer,
            MapDimensions dimensions, Integer currentTurn, Point? hq = null)
        extends AbstractReportGenerator<FortressMember>(comp, dimensions, hq) {
    "Produces a sub-report on a resource or piece of equipment. All fixtures referred
     to in this report are removed from the collection."
    shared actual void produceSingle(
            DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
            IMapNG map, Anything(String) ostream, FortressMember item, Point loc) {
        assert (is IUnit|IResourcePile|Implement item);
        if (is IUnit item) {
            UnitReportGenerator(pairComparator, currentPlayer, dimensions,
                currentTurn, hq).produceSingle(fixtures, map, ostream, item, loc);
        } else {
            switch (item)
            case (is Implement) {
                fixtures.remove(item.id);
                ostream("Equipment: ``item.kind``");
                if (item.count > 1) {
                    ostream(" (``item.count``)");
                }
            }
            else case (is IResourcePile) {
                fixtures.remove(item.id);
                if (item.quantity.units.empty) {
                    ostream("A pile of ``item.quantity`` ``item.contents`` (``item
                        .kind``)");
                } else {
                    ostream(
                        "A pile of ``item.quantity`` of ``item.contents`` (``item
                                .kind``)");
                }
                if (item.created >= 0) {
                    ostream(" from turn ``item.created``");
                }
            }
        }
    }

    "Produces a sub-report on all fortress members. All fixtures referred to in this
     report are removed from the collection. This method should probably never actually
     be called, since nearly all resources will be in fortresses and should be reported
     as such, but we'll handle this properly anyway."
    shared actual void produce(DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
            IMapNG map, Anything(String) ostream) {
        MutableHeadedMap<Implement, Point> equipment =
                HeadedMapImpl<Implement, Point>("<li>Equipment:",
                    comparing(byIncreasing(Implement.kind),
                        byDecreasing(Implement.count),
                        byIncreasing(Implement.id)));
        MutableMap<String, MutableHeadedMap<IResourcePile, Point>> resources =
                HashMap<String, MutableHeadedMap<IResourcePile, Point>>();
        for ([loc, item] in fixtures.items.narrow<[Point, IResourcePile|Implement]>()
                .sort(pairComparator)) {
            if (is IResourcePile resource = item) {
                MutableHeadedMap<IResourcePile, Point> pileMap;
                if (exists temp = resources[resource.kind]) {
                    pileMap = temp;
                } else {
                    pileMap = HeadedMapImpl<IResourcePile, Point>("``resource.kind``:",
                        comparing(byIncreasing(IResourcePile.kind),
                            byIncreasing(IResourcePile.contents),
                            byDecreasing(IResourcePile.quantity),
                            byIncreasing(IResourcePile.created),
                            byIncreasing(IResourcePile.id)));
                    resources[resource.kind] = pileMap;
                }
                pileMap[resource] = loc;
                fixtures.remove(resource.id);
            } else if (is Implement implement = item) {
                equipment[implement] = loc;
                fixtures.remove(implement.id);
            }
        }
        if (!equipment.empty || !resources.empty) {
            ostream("""<h4>Resources and Equipment</h4>
                       <ul>
                       """);
            writeMap(ostream, equipment, defaultFormatter(fixtures, map));
            if (!resources.empty) {
                ostream("""<li>Resources:<ul>
                            """);
                for (kind->mapping in resources) {
                    ostream("""<li>""");
                    writeMap(ostream, mapping, defaultFormatter(fixtures, map));
                    ostream("""</li>
                           """);
                }
                ostream("""</ul>
                           </li>
                           """);
            }
            ostream("""</ul>
                   """);
        }
    }
}
