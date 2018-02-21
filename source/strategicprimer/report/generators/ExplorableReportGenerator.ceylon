import ceylon.collection {
    ArrayList,
    MutableList
}
import ceylon.language.meta {
    type
}
import ceylon.language.meta.model {
    Type
}

import lovelace.util.common {
    DRMap=DelayedRemovalMap
}

import strategicprimer.model.map {
    Point,
    IMapNG,
    invalidPoint,
    IFixture,
    Player,
    MapDimensions
}
import strategicprimer.model.map.fixtures.explorable {
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
import ceylon.language {
    createMap=map
}
"A report generator for caves, battlefields, and portals."
shared class ExplorableReportGenerator(
        Comparison([Point, IFixture], [Point, IFixture]) comp, Player currentPlayer,
        MapDimensions dimensions, Point hq = invalidPoint)
        extends AbstractReportGenerator<Battlefield|Cave|Portal>(comp, dimensions, hq) {
    "Produces a more verbose sub-report on a cave, battlefield, or portal, or the report
     on all such."
    shared actual void produce(DRMap<Integer, [Point, IFixture]> fixtures,
            IMapNG map, Anything(String) ostream, [Battlefield|Cave|Portal, Point]? entry) {
        if (exists entry) {
            Point loc = entry.rest.first;
            switch (item = entry.first)
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
        } else {
            MutableList<[Point, IFixture]> values =
                    ArrayList<[Point, IFixture]> { *fixtures.items
                        .sort(pairComparator) };
            MutableList<Point> portals = PointList("Portals to other worlds: ");
            MutableList<Point> battles = PointList(
                "Signs of long-ago battles on the following tiles:");
            MutableList<Point> caves = PointList("Caves beneath the following tiles: ");
            Map<Type<IFixture>, Anything([Point, IFixture])> collectors =
                    createMap  {
                        { `Portal`->(([Point, IFixture] pair) =>
                        portals.add(pair.first)),
                            `Battlefield`->(([Point, IFixture] pair) =>
                            battles.add(pair.first)),
                            `Cave`->(([Point, IFixture] pair) =>
                            caves.add(pair.first))};
                    };
            for ([loc, item] in values) {
                if (exists collector = collectors[type(item)]) {
                    collector([loc, item]);
                    fixtures.remove(item.id);
                }
            }
            if (!caves.empty || !battles.empty || !portals.empty) {
                ostream("<h4>Caves, Battlefields, and Portals</h4>
                         <ul>
                         ``caves````battles````portals``</ul>
                         ");
            }
        }
    }
    "Produces a more verbose sub-report on a cave or battlefield, or the report section on
     all such."
    shared actual IReportNode produceRIR(DRMap<Integer, [Point, IFixture]> fixtures,
            IMapNG map, [Battlefield|Cave|Portal, Point]? entry) {
        if (exists entry) {
            Point loc = entry.rest.first;
            switch (item = entry.first)
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
        } else {
            MutableList<[Point, IFixture]> values =
                    ArrayList<[Point, IFixture]> { *fixtures.items
                        .sort(pairComparator) };
            IReportNode portals = ListReportNode("Portals");
            IReportNode battles = ListReportNode("Battlefields");
            IReportNode caves = ListReportNode("Caves");
            Map<Type<IFixture>, IReportNode> nodes =
                createMap { { `Portal`->portals, `Battlefield`->battles,
                            `Cave`->caves };
                    };
            for ([loc, item] in values) {
                if (is Battlefield|Cave|Portal fixture = item,
                        exists node = nodes[type(fixture)]) {
                    node.appendNode(produceRIR(fixtures, map, [fixture, loc]));
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
}
