import lovelace.util.common {
    DelayedRemovalMap
}

import strategicprimer.model.map {
    IFixture,
    IMapNG,
    Player,
    Point,
    MapDimensions
}
import strategicprimer.model.map.fixtures {
    TerrainFixture
}
import strategicprimer.report.generators.tabular {
    UnitTabularReportGenerator,
    FortressTabularReportGenerator,
    AnimalTabularReportGenerator,
    WorkerTabularReportGenerator,
    VillageTabularReportGenerator,
    TownTabularReportGenerator,
    CropTabularReportGenerator,
    DiggableTabularReportGenerator,
    ResourceTabularReportGenerator,
    ImmortalsTabularReportGenerator,
    ExplorableTabularReportGenerator
}
"A method to produce tabular reports based on a map for a player."
shared void createTabularReports(IMapNG map, Anything(String)(String) source) {
    DelayedRemovalMap<Integer, [Point, IFixture]> fixtures = getFixtures(map);
    Player player = map.currentPlayer;
    MapDimensions dimensions = map.dimensions;
    Point hq = findHQ(map, player);
    /*{ITableGenerator<out Object>*}*/ value generators = {
        FortressTabularReportGenerator(player, hq, dimensions),
        UnitTabularReportGenerator(player, hq, dimensions),
        AnimalTabularReportGenerator(hq, dimensions),
        WorkerTabularReportGenerator(hq, dimensions),
        VillageTabularReportGenerator(player, hq, dimensions),
        TownTabularReportGenerator(player, hq, dimensions),
        CropTabularReportGenerator(hq, dimensions),
        DiggableTabularReportGenerator(hq, dimensions),
        ResourceTabularReportGenerator(),
        ImmortalsTabularReportGenerator(hq, dimensions),
        ExplorableTabularReportGenerator(player, hq, dimensions)
    };
    for (generator in generators) {
        generator.produceTable(source(generator.tableName), fixtures);
        for ([loc, fixture] in fixtures.items) {
            if (is TerrainFixture fixture) {
                fixtures.remove(fixture.id);
            } else {
                process.writeLine("Unhandled fixture:   ``fixture``");
            }
        }
    }
}