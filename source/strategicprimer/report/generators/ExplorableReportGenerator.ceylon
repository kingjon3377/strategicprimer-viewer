import ceylon.collection {
    MutableList
}
import ceylon.language.meta {
    type
}
import ceylon.language.meta.model {
    Type
}

import lovelace.util.common {
    DRMap=DelayedRemovalMap,
    simpleMap
}

import strategicprimer.model.common.map {
    Player,
    IFixture,
    Point,
    MapDimensions,
    IMapNG
}
import strategicprimer.model.common.map.fixtures.explorable {
    Cave,
    Portal,
    Battlefield
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
"A report generator for caves, battlefields, and portals."
shared class ExplorableReportGenerator(
        Comparison([Point, IFixture], [Point, IFixture]) comp, Player currentPlayer,
        MapDimensions dimensions, Point hq = Point.invalidPoint)
        extends AbstractReportGenerator<Battlefield|Cave|Portal>(comp, dimensions, hq) {
    "Produces a more verbose sub-report on a cave, battlefield, or portal."
    shared actual void produceSingle(DRMap<Integer, [Point, IFixture]> fixtures,
            IMapNG map, Anything(String) ostream, Battlefield|Cave|Portal item,
            Point loc) {
        switch (item)
        case (is Cave) {
            fixtures.remove(item.id);
            ostream("Caves beneath ``loc````distCalculator.distanceString(loc)``");
        }
        case (is Battlefield) {
            fixtures.remove(item.id);
            ostream("Signs of a long-ago battle on ``loc````distCalculator
                .distanceString(loc)``");
        }
        case (is Portal) {
            fixtures.remove(item.id);
            ostream("A portal to another world at ``loc`` ``distCalculator
                .distanceString(loc)``");
        }
    }
    "Produces the report on all caves, battlefields, and portals."
    shared actual void produce(DRMap<Integer, [Point, IFixture]> fixtures,
            IMapNG map, Anything(String) ostream) {
        MutableList<Point> portals = PointList("Portals to other worlds: ");
        MutableList<Point> battles = PointList(
            "Signs of long-ago battles on the following tiles:");
        MutableList<Point> caves = PointList("Caves beneath the following tiles: ");
        for ([loc, item] in fixtures.items.narrow<[Point, Portal|Battlefield|Cave]>()
                .sort(pairComparator)) {
            switch (item)
            case (is Portal) {
                portals.add(loc);
            }
            case (is Battlefield) {
                battles.add(loc);
            }
            case (is Cave) {
                caves.add(loc);
            }
            fixtures.remove(item.id);
        }
        if (!caves.empty || !battles.empty || !portals.empty) {
            ostream("<h4>Caves, Battlefields, and Portals</h4>
                     <ul>");
            for (list in [ caves, battles, portals ]
                    .filter(not(Iterable<Anything>.empty))) { // Sugaring to {Anything*} won't compile
                ostream("<li>``list``</li>");
            }
            ostream("</ul>``operatingSystem.newline``");
        }
    }
    "Produces a more verbose sub-report on a cave, battlefield, or portal."
    shared actual IReportNode produceRIRSingle(DRMap<Integer, [Point, IFixture]> fixtures,
            IMapNG map, Battlefield|Cave|Portal item, Point loc) {
        switch (item)
        case (is Cave) {
            fixtures.remove(item.id);
            return SimpleReportNode("Caves beneath ``loc`` ``distCalculator
                .distanceString(loc)``", loc);
        }
        case (is Battlefield) {
            fixtures.remove(item.id);
            return SimpleReportNode(
                "Signs of a long-ago battle on ``loc`` ``distCalculator
                    .distanceString(loc)``", loc);
        }
        case (is Portal) {
            fixtures.remove(item.id);
            return SimpleReportNode("A portal to another world at ``loc`` ``
                distCalculator.distanceString(loc)``", loc);
        }
    }
    "Produces the report section on all caves, battlefields, and portals."
    shared actual IReportNode produceRIR(
            DRMap<Integer, [Point, IFixture]> fixtures, IMapNG map) {
        IReportNode portals = ListReportNode("Portals");
        IReportNode battles = ListReportNode("Battlefields");
        IReportNode caves = ListReportNode("Caves");
        Map<Type<IFixture>, IReportNode> nodes =
                simpleMap(`Portal`->portals, `Battlefield`->battles,
            `Cave`->caves);
//        for ([loc, item] in fixtures.items.narrow<[Point, Battlefield|Cave|Portal]>().sort(pairComparator)) { // TODO: switch to this form once eclipse/ceylon#7372 fixed
//            if (exists node = nodes[type(item)]) {
        for ([loc, item] in fixtures.items.sort(pairComparator)) {
            if (is Battlefield|Cave|Portal item,
                    exists node = nodes[type(item)]) {
                node.appendNode(produceRIRSingle(fixtures, map, item, loc));
            }
        }
        IReportNode retval = SectionListReportNode(4,
            "Caves, Battlefields, and Portals");
        retval.addIfNonEmpty(caves, battles, portals);
        if (retval.childCount == 0) {
            return emptyReportNode;
        } else {
            return retval;
        }
    }
}
