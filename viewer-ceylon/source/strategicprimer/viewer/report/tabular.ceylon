import java.io {
    JOutputStream=OutputStream, JPrintStream=PrintStream
}
import model.map {
    IMapNG,
    Point,
    IFixture,
    Player,
    TerrainFixture
}
import lovelace.util.common {
    DelayedRemovalMap
}
"A method to produce tabular reports based on a map for a player."
shared void createTabularReports(IMapNG map, JOutputStream(String) source) {
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
        // TODO: Use ceylon.file / ceylon.io for file output
        try (ostream = JPrintStream(source(generator.tableName))) {
            generator.produceTable((String string) => ostream.print(string), fixtures);
        }
        for ([loc, fixture] in fixtures.items) {
            if (is TerrainFixture fixture) {
                fixtures.remove(fixture.id);
            } else {
                process.writeLine("Unhandled fixture:   ``fixture``");
            }
        }
    }
}