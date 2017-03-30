import ceylon.logging {
    logger,
    Logger
}

import strategicprimer.viewer.model {
    DistanceComparator,
    IDRegistrar
}


import lovelace.util.common {
    todo,
    DelayedRemovalMap,
    IntMap
}
import strategicprimer.viewer.model.map.fixtures {
    TerrainFixture
}
import model.map {
    Player,
    IFixture,
    Point,
    PointFactory,
    TileFixture
}
import strategicprimer.viewer.model.map.fixtures.mobile {
    IUnit
}
import strategicprimer.viewer.model.map.fixtures.towns {
    Fortress
}
import strategicprimer.viewer.model.map {
    IMapNG,
    FixtureIterable
}

import strategicprimer.viewer.report.generators {
    pairComparator,
    AnimalReportGenerator,
    VillageReportGenerator,
    FortressReportGenerator,
    HarvestableReportGenerator,
    UnitReportGenerator,
    IReportGenerator,
    FortressMemberReportGenerator,
    TownReportGenerator,
    ExplorableReportGenerator,
    ImmortalsReportGenerator,
    TextReportGenerator
}
import strategicprimer.viewer.report.nodes {
    IReportNode,
    RootReportNode
}
import strategicprimer.viewer.drivers {
    createIDFactory
}
"A logger."
Logger log = logger(`module strategicprimer.viewer`);
"Find the location of the given player's HQ in the given map."
todo("""Return null instead of an "invalid" Point when not found?""")
Point findHQ(IMapNG map, Player player) {
    variable Point? retval = null;
    for (location in map.locations) {
        for (fixture in map.getOtherFixtures(location)) {
            if (is Fortress fixture, fixture.owner == player) {
                if ("hq" == fixture.name) {
                    return location;
                } else if (location.valid, !retval exists) {
                    retval = location;
                }
            }
        }
    } else {
        return retval else PointFactory.invalidPoint;
    }
}
"Create a mapping from ID numbers to Pairs of fixtures and their location for all fixtures
 in the map."
DelayedRemovalMap<Integer, [Point, IFixture]> getFixtures(IMapNG map) {
    DelayedRemovalMap<Integer, [Point, IFixture]> retval = IntMap<[Point, IFixture]>();
    IDRegistrar idf = createIDFactory(map);
    Integer checkID(IFixture fixture) {
        if (fixture.id < 0) {
            return idf.createID();
        } else {
            return fixture.id;
        }
    }
    void addToMap(Point location, IFixture fixture) {
        if (fixture is TileFixture || fixture.id >= 0) {
            Integer key = checkID(fixture);
            value val = [location, fixture];
            if (exists existing = retval.put(key, val)) {
                log.warn("Duplicate key, ``key``, for Pairs ``
                existing`` and ``val``");
            }
        }
        if (is FixtureIterable<out IFixture> fixture) {
            for (inner in fixture) {
                addToMap(location, inner);
            }
        }
    }
    for (location in map.locations) {
        for (IFixture fixture in {map.getGround(location), map.getForest(location),
                *map.getOtherFixtures(location)}.coalesced) {
            addToMap(location, fixture);
        }
    }
    return retval;
}
"Produces sub-reports, appending them to the buffer and calling coalesce() on the fixtures
 collection after each."
void createSubReports(StringBuilder builder,
        DelayedRemovalMap<Integer, [Point, IFixture]> fixtures, IMapNG map, Player player,
        IReportGenerator<out Object>* generators) {
    for (generator in generators) {
        generator.produce(fixtures, map, builder.append);
        fixtures.coalesce();
    }
}
"Create the report for the given player based on the given map."
todo("Consider generating Markdown instead of HTML. OTOH, we'd have to keep a list nesting
      level parameter or something.")
shared String createReport(IMapNG map, Player player = map.currentPlayer) {
    StringBuilder builder = StringBuilder();
    builder.append("""<html>
                      <head><title>Strategic Primer map summary report</title></head>
                      <body>
                      """);
    DelayedRemovalMap<Integer, [Point, IFixture]> fixtures = getFixtures(map);
    Point hq = findHQ(map, player);
    Comparison([Point, IFixture], [Point, IFixture]) comparator = pairComparator(
        DistanceComparator(hq).compare, byIncreasing(IFixture.hash));
    createSubReports(builder, fixtures, map, player,
        FortressReportGenerator(comparator, player, hq),
        UnitReportGenerator(comparator, player, hq), TextReportGenerator(comparator, hq),
        TownReportGenerator(comparator, player, hq),
        FortressMemberReportGenerator(comparator, player, hq),
        ExplorableReportGenerator(comparator, player, hq),
        HarvestableReportGenerator(comparator, hq),
        AnimalReportGenerator(comparator, hq),
        VillageReportGenerator(comparator, player, hq),
        ImmortalsReportGenerator(comparator, hq));
    builder.append("""</body>
                      </html>
                      """);
    for ([loc, fixture] in fixtures.items) {
        if (fixture.id < 0) {
            continue;
        } else if (is TerrainFixture fixture) {
            fixtures.remove(fixture.id);
            continue;
        }
        process.writeLine("Unhandled fixture:\t``fixture``");
    }
    return builder.string;
}
"Create a slightly abbreviated report, omitting the player's fortresses and units."
shared String createAbbreviatedReport(IMapNG map, Player player = map.currentPlayer) {
    StringBuilder builder = StringBuilder();
    builder.append(
        """<html>
            <head><title>Strategic Primer map summary abridged report</title></head>
            <body>
            """);
    DelayedRemovalMap<Integer, [Point, IFixture]> fixtures = getFixtures(map);
    Point hq = findHQ(map, player);
    Comparison([Point, IFixture], [Point, IFixture]) comparator = pairComparator(
        DistanceComparator(findHQ(map, player)).compare,
        byIncreasing(IFixture.hash));
    for ([loc, fixture] in fixtures.items) {
        if (is IUnit|Fortress fixture, fixture.owner == player) {
            fixtures.remove(fixture.id);
        }
    }
    fixtures.coalesce();
    createSubReports(builder, fixtures, map, player,
        FortressMemberReportGenerator(comparator, player, hq),
        FortressReportGenerator(comparator, player, hq),
        UnitReportGenerator(comparator, player, hq), TextReportGenerator(comparator, hq),
        TownReportGenerator(comparator, player, hq),
        ExplorableReportGenerator(comparator, player, hq),
        HarvestableReportGenerator(comparator, hq),
        AnimalReportGenerator(comparator, hq),
        VillageReportGenerator(comparator, player, hq),
        ImmortalsReportGenerator(comparator, hq));
    builder.append("""</body>
                      </html>
                      """);
    for ([loc, fixture] in fixtures.items) {
        if (fixture.id < 0) {
            continue;
        } else if (is TerrainFixture fixture) {
            fixtures.remove(fixture.id);
            continue;
        }
        process.writeLine("Unhandled fixture:\t``fixture``");
    }
    return builder.string;
}
"Produce sub-reports in report-intermediate-representation, adding them to the root node
 and calling coalesce() on the fixtures collection after each."
void createSubReportsIR(IReportNode root,
        DelayedRemovalMap<Integer, [Point, IFixture]> fixtures, IMapNG map, Player player,
        IReportGenerator<out Object>* generators) {
    for (generator in generators) {
        root.appendNode(generator.produceRIR(fixtures, map));
        fixtures.coalesce();
    }
}
"Create the report, in report-intermediate-representation, based on the given map."
shared IReportNode createReportIR(IMapNG map, Player player = map.currentPlayer) {
    IReportNode retval = RootReportNode("Strategic Primer map summary report");
    DelayedRemovalMap<Integer, [Point, IFixture]> fixtures = getFixtures(map);
    Point hq = findHQ(map, player);
    Comparison([Point, IFixture], [Point, IFixture]) comparator = pairComparator(
        DistanceComparator(findHQ(map, player)).compare,
        byIncreasing(IFixture.hash));
    createSubReportsIR(retval, fixtures, map, player,
        FortressReportGenerator(comparator, player, hq),
        UnitReportGenerator(comparator, player, hq), TextReportGenerator(comparator, hq),
        TownReportGenerator(comparator, player, hq),
        ExplorableReportGenerator(comparator, player, hq),
        HarvestableReportGenerator(comparator, hq),
        FortressMemberReportGenerator(comparator, player, hq),
        AnimalReportGenerator(comparator, hq),
        VillageReportGenerator(comparator, player, hq),
        ImmortalsReportGenerator(comparator, hq));
    return retval;
}
"Create a slightly abbreviated report, omitting the player's fortresses and units, in
 intermediate representation."
shared IReportNode createAbbreviatedReportIR(IMapNG map,
        Player player = map.currentPlayer) {
    DelayedRemovalMap<Integer, [Point, IFixture]> fixtures = getFixtures(map);
    Point hq = findHQ(map, player);
    Comparison([Point, IFixture], [Point, IFixture]) comparator = pairComparator(
        DistanceComparator(hq).compare,
        byIncreasing(IFixture.hash));
    for ([loc, fixture] in fixtures.items) {
        if (is IUnit|Fortress fixture, fixture.owner == player) {
            fixtures.remove(fixture.id);
        }
    }
    fixtures.coalesce();
    IReportNode retval = RootReportNode(
        "Strategic Primer map summary abbreviated report");
    createSubReportsIR(retval, fixtures, map, player,
        FortressMemberReportGenerator(comparator, player, hq),
        FortressReportGenerator(comparator, player, hq),
        UnitReportGenerator(comparator, player, hq),
        TextReportGenerator(comparator, hq),
        TownReportGenerator(comparator, player, hq),
        ExplorableReportGenerator(comparator, player, hq),
        HarvestableReportGenerator(comparator, hq),
        AnimalReportGenerator(comparator, hq),
        VillageReportGenerator(comparator, player, hq),
        ImmortalsReportGenerator(comparator, hq));
    return retval;
}