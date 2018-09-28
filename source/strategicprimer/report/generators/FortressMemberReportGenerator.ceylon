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
	invalidPoint
}

import strategicprimer.model.impl.map {
    IMapNG
}
import strategicprimer.model.common.map.fixtures {
    ResourcePile,
    Implement,
    FortressMember
}
import strategicprimer.model.common.map.fixtures.mobile {
    IUnit
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
"A report generator for equipment and resources."
shared class FortressMemberReportGenerator(
            Comparison([Point, IFixture], [Point, IFixture]) comp, Player currentPlayer,
            MapDimensions dimensions, Integer currentTurn, Point hq = invalidPoint)
        extends AbstractReportGenerator<FortressMember>(comp, dimensions, hq) {
    "Produces a sub-report on a resource or piece of equipment. All fixtures referred
     to in this report are removed from the collection."
    shared actual void produceSingle(
		    DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
            IMapNG map, Anything(String) ostream, FortressMember item, Point loc) {
        assert (is IUnit|ResourcePile|Implement item);
        if (is IUnit item) {
            UnitReportGenerator(pairComparator, currentPlayer, dimensions,
                currentTurn, hq).produceSingle(fixtures, map, ostream, item, loc);
        } else {
            switch (item)
            case (is ResourcePile) {
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
            } case (is Implement) {
                fixtures.remove(item.id);
                ostream("Equipment: ``item.kind``");
                if (item.count > 1) {
                    ostream(" (``item.count``)");
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
        MutableMap<String, MutableHeadedMap<ResourcePile, Point>> resources =
                HashMap<String, MutableHeadedMap<ResourcePile, Point>>();
        for ([loc, item] in fixtures.items.narrow<[Point, ResourcePile|Implement]>()
                .sort(pairComparator)) {
            if (is ResourcePile resource = item) {
                MutableHeadedMap<ResourcePile, Point> pileMap;
                if (exists temp = resources[resource.kind]) {
                    pileMap = temp;
                } else {
                    pileMap = HeadedMapImpl<ResourcePile, Point>(
                        "<li>``resource.kind``:",
                        comparing(byIncreasing(ResourcePile.kind),
                            byIncreasing(ResourcePile.contents),
                            byDecreasing(ResourcePile.quantity),
                            byIncreasing(ResourcePile.created),
                            byIncreasing(ResourcePile.id)));
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
    "Produces a sub-report on a resource or piece of equipment. All fixtures referred
     to in this report are removed from the collection."
    shared actual IReportNode produceRIRSingle(
            DelayedRemovalMap<Integer, [Point, IFixture]> fixtures,
            IMapNG map, FortressMember item, Point loc) {
        assert (is IUnit|ResourcePile|Implement item);
        if (is IUnit item) {
            return UnitReportGenerator(pairComparator, currentPlayer, dimensions,
                currentTurn, hq).produceRIRSingle(fixtures, map, item, loc);
        } else {
            switch (item)
            case (is ResourcePile) {
                fixtures.remove(item.id);
                String age;
                if (item.created < 0) {
                    age = "";
                } else {
                    age = " from turn ``item.created``";
                }
                if (item.quantity.units.empty) {
                    return SimpleReportNode(
                        "A pile of ``item.quantity.number`` ``item.contents`` (``item
                                .kind``)``age``");
                } else {
                    return SimpleReportNode(
                        "A pile of ``item.quantity`` of ``item.contents`` (``item
                                .kind``)``age``");
                }
            }
            case (is Implement) {
                fixtures.remove(item.id);
                if (item.count > 1) {
                    return SimpleReportNode("Equipment: ``item.kind`` (``item.count``)");
                } else {
                    return SimpleReportNode("Equipment: ``item.kind``");
                }
            }
        }
    }
    "Produces a sub-report on all fortress members. All fixtures referred to in this
     report are removed from the collection. This method should probably never actually
     be called, since nearly all resources will be in fortresses and should be reported
     as such, but we'll handle this properly anyway."
    shared actual IReportNode produceRIR(
            DelayedRemovalMap<Integer, [Point, IFixture]> fixtures, IMapNG map) {
        MutableMap<String, IReportNode> resourceKinds =
                HashMap<String, IReportNode>();
        IReportNode equipment = ListReportNode("Equipment:");
        for ([loc, item] in fixtures.items.narrow<[Point, ResourcePile|Implement]>()
                .sort(pairComparator)) {
            if (is ResourcePile resource = item) {
                String kind = resource.kind;
                IReportNode node;
                if (exists temp = resourceKinds[kind]) {
                    node = temp;
                } else {
                    node = ListReportNode("``kind``:");
                    resourceKinds[kind] = node;
                }
                node.appendNode(produceRIRSingle(fixtures, map, resource,
                    loc));
            } else if (is Implement implement = item) {
                equipment.appendNode(produceRIRSingle(fixtures, map, implement,
                    loc));
            }
        }
        IReportNode resources = ListReportNode("Resources:");
        resources.addIfNonEmpty(*resourceKinds.items);
        IReportNode retval = SectionListReportNode(4, "Resources and Equipment:");
        retval.addIfNonEmpty(resources, equipment);
        if (retval.childCount == 0) {
            return emptyReportNode;
        } else {
            return retval;
        }
    }
}
