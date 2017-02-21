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
import util {
    PatientMap,
    Pair,
    IntMap
}
import java.lang {
    JInteger=Integer
}
import controller.map.report.tabular {
    ITableGenerator,
    UnitTabularReportGenerator,
    VillageTabularReportGenerator,
    TownTabularReportGenerator,
    ResourceTabularReportGenerator,
    ImmortalsTabularReportGenerator
}
"A method to produce tabular reports based on a map for a player."
shared void createTabularReports(IMapNG map, JOutputStream(String) source) {
    // TODO: Use Ceylon Integer and Tuples
    PatientMap<JInteger, Pair<Point, IFixture>> fixtures = IntMap<Pair<Point, IFixture>>();
    for (key->val in getFixtures(map)) {
        fixtures.put(JInteger(key), Pair.\iof<Point, IFixture>(val.first,
            val.rest.first));
    }
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
        assert (is ITableGenerator<out Object> generator);
        try (ostream = JPrintStream(source(generator.tableName))) {
            generator.produce(ostream, fixtures);
        }
        for (pair in fixtures.values()) {
            IFixture fixture = pair.second();
            if (is TerrainFixture fixture) {
                fixtures.remove(JInteger(fixture.id));
            } else {
                process.writeLine("Unhandled fixture:   ``fixture``");
            }
        }
    }
}