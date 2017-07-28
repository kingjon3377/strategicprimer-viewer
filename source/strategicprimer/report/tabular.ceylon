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
import java.awt {
    Component
}
import javax.swing {
    JTable,
    ScrollPaneConstants,
    JScrollPane
}
import lovelace.util.jvm {
    platform
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
        AnimalTabularReportGenerator(hq, dimensions, map.currentTurn),
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
    }
    for ([loc, fixture] in fixtures.items) {
        if (is TerrainFixture fixture) {
            fixtures.remove(fixture.id);
        } else {
            process.writeLine("Unhandled fixture:   ``fixture``");
        }
    }
}
"A method to produce tabular reports and add them to a GUI."
shared void createGUITabularReports(
        "The way to add the tables to the GUI."
        Anything(String, Component) consumer,
        "The map to base the reports on"
        IMapNG map) {
    DelayedRemovalMap<Integer, [Point, IFixture]> fixtures = getFixtures(map);
    Player player = map.currentPlayer;
    MapDimensions dimensions = map.dimensions;
    Point hq = findHQ(map, player);
    /*{ITableGenerator<out Object>*}*/ value generators = {
        FortressTabularReportGenerator(player, hq, dimensions),
        UnitTabularReportGenerator(player, hq, dimensions),
        AnimalTabularReportGenerator(hq, dimensions, map.currentTurn),
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
        value table = JTable(generator.produceTableModel(fixtures));
        Integer vertControl;
        Integer horizControl;
        if (platform.systemIsMac) {
            vertControl = ScrollPaneConstants.verticalScrollbarAlways;
            horizControl = ScrollPaneConstants.horizontalScrollbarAlways;
        } else {
            vertControl = ScrollPaneConstants.verticalScrollbarAsNeeded;
            horizControl = ScrollPaneConstants.horizontalScrollbarAsNeeded;
        }
        consumer(generator.tableName, JScrollPane(table, vertControl,
            horizControl));
    }
    // TODO: report this in a tab in the window
    for ([loc, fixture] in fixtures.items) {
        if (is TerrainFixture fixture) {
            fixtures.remove(fixture.id);
        } else {
            process.writeLine("Unhandled fixture:   ``fixture``");
        }
    }
}