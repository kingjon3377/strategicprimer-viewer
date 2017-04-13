import java.io {
    JOutputStream=OutputStream,
    JPrintStream=PrintStream
}

import lovelace.util.common {
    DelayedRemovalMap,
    todo
}

import strategicprimer.model.map {
    IFixture,
    IMapNG,
    Player,
    Point
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
import ceylon.file {
    Writer
}
"A method to produce tabular reports based on a map for a player."
todo("Change the type of `source`, so we don't have to mark our import of `java.base` as
      `shared`")
shared void createTabularReports(IMapNG map, Anything(String)(String) source) {
    DelayedRemovalMap<Integer, [Point, IFixture]> fixtures = getFixtures(map);
    Player player = map.currentPlayer;
    Point hq = findHQ(map, player);
    /*{ITableGenerator<out Object>*}*/ value generators = {
        FortressTabularReportGenerator(player, hq),
        UnitTabularReportGenerator(player, hq),
        AnimalTabularReportGenerator(hq),
        WorkerTabularReportGenerator(hq),
        VillageTabularReportGenerator(player, hq),
        TownTabularReportGenerator(player, hq),
        CropTabularReportGenerator(hq),
        DiggableTabularReportGenerator(hq),
        ResourceTabularReportGenerator(),
        ImmortalsTabularReportGenerator(hq),
        ExplorableTabularReportGenerator(player, hq)
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