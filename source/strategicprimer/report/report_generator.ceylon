import ceylon.logging {
    logger,
    Logger
}

import lovelace.util.common {
    todo,
    DelayedRemovalMap,
    IntMap,
    matchingValue,
    narrowedStream
}

import strategicprimer.model.common {
    DistanceComparator
}
import strategicprimer.model.common.idreg {
    IDRegistrar,
    createIDFactory
}
import strategicprimer.model.common.map {
    IFixture,
    Player,
    TileFixture,
    Point,
    MapDimensions,
    IMapNG
}
import strategicprimer.model.common.map.fixtures {
    TerrainFixture,
    Ground
}

import strategicprimer.model.common.map.fixtures.towns {
    Fortress
}
import strategicprimer.report.generators {
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
    TextReportGenerator,
    AdventureReportGenerator
}
import ceylon.collection {
    MutableMap,
    HashMap
}

"A logger."
Logger log = logger(`module strategicprimer.report`);

"An encapsulation of helper methods for report generators."
object reportGeneratorHelper {
    "Find the location of the given player's HQ in the given map, or [[null]]
     if not found."
    shared Point? findHQ(IMapNG map, Player player) {
        variable Point? retval = null;
        for (location->fixture in narrowedStream<Point, Fortress>(map.fixtures)
                .filter(compose(matchingValue(player, Fortress.owner),
                    Entry<Point, Fortress>.item))) {
            if ("hq" == fixture.name.lowercased) {
                return location;
            } else if (location.valid, !retval exists) {
                retval = location;
            }
        } else {
            return retval;
        }
    }

    "Create a mapping from ID numbers to Pairs of fixtures and their location for all
     fixtures in the map."
    shared DelayedRemovalMap<Integer, [Point, IFixture]> getFixtures(IMapNG map) {
        DelayedRemovalMap<Integer, [Point, IFixture]> retval =
                IntMap<[Point, IFixture]>();
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
                // We could use `retval[key] = val`, but that would be more confusing
                // here.
                if (exists existing = retval.put(key, val), existing != val) {
                    log.warn("Duplicate key, ``key``, for Pairs ``
                        existing`` and ``val``");
                }
            }
            if (is {IFixture*} fixture) {
                for (inner in fixture) {
                    addToMap(location, inner);
                }
            }
        }
        for (location->fixture in map.fixtures) {
            addToMap(location, fixture);
        }
        return retval;
    }

    void parentMapImpl(MutableMap<Integer, Integer> retval, IFixture parent,
            {IFixture*} stream) {
        for (fixture in stream) {
            retval.put(fixture.id, parent.id);
            if (is {IFixture*} fixture) {
                parentMapImpl(retval, fixture, fixture);
            }
        }
    }

    "Create a mapping from child ID numbers to parent ID numbers."
    shared Map<Integer, Integer> getParentMap(IMapNG map) {
        MutableMap<Integer, Integer> retval = HashMap<Integer, Integer>();
        for (fixture in map.fixtures.items.narrow<{IFixture*}>()) {
            parentMapImpl(retval, fixture, fixture);
        }
        return retval;
    }
}

"Produces reports based on maps."
shared object reportGenerator {
    "Produces sub-reports, appending them to the buffer and calling coalesce() on the
     fixtures collection after each."
    void createSubReports(StringBuilder builder,
            DelayedRemovalMap<Integer, [Point, IFixture]> fixtures, IMapNG map,
            Player player, IReportGenerator<out Object>* generators) {
        for (generator in generators) {
            generator.produce(fixtures, map, builder.append);
            fixtures.coalesce();
        }
    }

    "Create the report for the given player based on the given map."
    todo("Consider generating Markdown instead of HTML. OTOH, we'd have to keep a list
          nesting level parameter or something.")
    shared String createReport(IMapNG map, Player player = map.currentPlayer) {
        MapDimensions dimensions = map.dimensions;
        StringBuilder builder = StringBuilder();
        builder.append("""<!DOCTYPE html>
                          <html>
                          <head><title>Strategic Primer map summary report</title></head>
                          <body>
                          """);
        DelayedRemovalMap<Integer, [Point, IFixture]> fixtures =
                reportGeneratorHelper.getFixtures(map);
        Point? hq = reportGeneratorHelper.findHQ(map, player);
        Comparison([Point, IFixture], [Point, IFixture]) comparator;
        if (exists hq) {
            comparator = pairComparator(DistanceComparator(hq, dimensions).compare,
                byIncreasing(IFixture.hash));
        } else {
            comparator = pairComparator((Anything one, Anything two) => equal,
                byIncreasing(IFixture.hash));
        }
        createSubReports(builder, fixtures, map, player,
            FortressReportGenerator(comparator, player, dimensions, map.currentTurn, hq),
            UnitReportGenerator(comparator, player, dimensions, map.currentTurn, hq),
            TextReportGenerator(comparator, dimensions, hq),
            TownReportGenerator(comparator, player, dimensions, map.currentTurn, hq),
            FortressMemberReportGenerator(comparator, player, dimensions, map.currentTurn,
                hq),
            AdventureReportGenerator(comparator, player, dimensions, hq),
            ExplorableReportGenerator(comparator, player, dimensions, hq),
            HarvestableReportGenerator(comparator, dimensions, hq),
            AnimalReportGenerator(comparator, dimensions, map.currentTurn, hq),
            VillageReportGenerator(comparator, player, dimensions, hq),
            ImmortalsReportGenerator(comparator, dimensions, hq));
        builder.append("""</body>
                          </html>
                          """);
        for ([loc, fixture] in fixtures.items) {
            if (fixture.id < 0) {
                continue;
            } else if (is Ground|TerrainFixture fixture) {
                fixtures.remove(fixture.id);
                continue;
            }
            process.writeLine("Unhandled fixture:\t``fixture`` (ID # ``fixture.id``)");
        }
        return builder.string;
    }
}
