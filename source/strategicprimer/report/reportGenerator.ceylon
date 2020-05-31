import ceylon.logging {
    logger,
    Logger
}

import lovelace.util.common {
    todo,
    DelayedRemovalMap
}

import strategicprimer.model.common {
    DistanceComparator
}

import strategicprimer.model.common.map {
    IFixture,
    Player,
    Point,
    MapDimensions,
    IMapNG
}

import strategicprimer.model.common.map.fixtures {
    TerrainFixture,
    Ground
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

"A logger."
Logger log = logger(`module strategicprimer.report`);

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

    Comparison compareToEqual(Anything one, Anything two) => equal;

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
            comparator = pairComparator(compareToEqual, byIncreasing(IFixture.hash));
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
            process.writeLine("Unhandled fixture:\t``fixture`` (ID # ``fixture.id``)"); // TODO: Take ICLIHelper instead of using stdout; at laest ue stderr
        }
        return builder.string;
    }
}
