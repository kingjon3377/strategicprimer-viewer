import ceylon.collection {
    MutableList,
    ArrayList
}
import ceylon.interop.java {
    createJavaStringArray,
	JavaComparator
}

import java.awt {
    Component
}
import java.lang {
    JString=String
}

import javax.swing {
    JTable,
    ScrollPaneConstants,
    JScrollPane,
    JList,
    JLabel
}

import lovelace.util.common {
    DelayedRemovalMap
}
import lovelace.util.jvm {
    platform,
    BorderedPanel
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
    ExplorableTabularReportGenerator,
    SkillTabularReportGenerator,
	ITableGenerator
}
import java.util {
    JComparator=Comparator
}
import javax.swing.table {
    TableRowSorter,
    TableModel
}
shared object tabularReportGenerator {
	"A method to produce tabular reports based on a map for a player."
	shared void createTabularReports(IMapNG map, Anything(String)(String) source) {
	    DelayedRemovalMap<Integer, [Point, IFixture]> fixtures = reportGeneratorHelper.getFixtures(map);
	    Map<Integer, Integer> parentMap = reportGeneratorHelper.getParentMap(map);
	    Player player = map.currentPlayer;
	    MapDimensions dimensions = map.dimensions;
	    Point hq = reportGeneratorHelper.findHQ(map, player);
	    {ITableGenerator<out Object>*} generators = [
	        FortressTabularReportGenerator(player, hq, dimensions),
	        UnitTabularReportGenerator(player, hq, dimensions),
	        AnimalTabularReportGenerator(hq, dimensions, map.currentTurn),
	        SkillTabularReportGenerator(),
	        WorkerTabularReportGenerator(hq, dimensions),
	        VillageTabularReportGenerator(player, hq, dimensions),
	        TownTabularReportGenerator(player, hq, dimensions),
	        CropTabularReportGenerator(hq, dimensions),
	        DiggableTabularReportGenerator(hq, dimensions),
	        ResourceTabularReportGenerator(),
	        ImmortalsTabularReportGenerator(hq, dimensions),
	        ExplorableTabularReportGenerator(player, hq, dimensions)
	    ];
	    for (generator in generators) {
	        generator.produceTable(source(generator.tableName), fixtures, parentMap);
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
	    DelayedRemovalMap<Integer, [Point, IFixture]> fixtures = reportGeneratorHelper.getFixtures(map);
	    Map<Integer, Integer> parentMap = reportGeneratorHelper.getParentMap(map);
	    Player player = map.currentPlayer;
	    MapDimensions dimensions = map.dimensions;
	    Point hq = reportGeneratorHelper.findHQ(map, player);
	    {ITableGenerator<out Object>*} generators = [
	        FortressTabularReportGenerator(player, hq, dimensions),
	        UnitTabularReportGenerator(player, hq, dimensions),
	        AnimalTabularReportGenerator(hq, dimensions, map.currentTurn),
	        SkillTabularReportGenerator(),
	        WorkerTabularReportGenerator(hq, dimensions),
	        VillageTabularReportGenerator(player, hq, dimensions),
	        TownTabularReportGenerator(player, hq, dimensions),
	        CropTabularReportGenerator(hq, dimensions),
	        DiggableTabularReportGenerator(hq, dimensions),
	        ResourceTabularReportGenerator(),
	        ImmortalsTabularReportGenerator(hq, dimensions),
	        ExplorableTabularReportGenerator(player, hq, dimensions)
	    ];
	    Comparison sorter(Object one, Object two) {
	        String actualOne;
	        String actualTwo;
	        if (is String one) {
	            actualOne = one;
	        } else {
	            actualOne = one.string;
	        }
	        if (is String two) {
	            actualTwo = two;
	        } else {
	            actualTwo = two.string;
	        }
	        if (is Float floatOne = Float.parse(actualOne), is Float floatTwo = Float.parse(actualTwo)) {
	            return floatOne <=> floatTwo;
	        } else {
	            return actualOne <=> actualTwo;
	        }
	    }
	    JComparator<out Object> wrapped = JavaComparator(sorter);
	    for (generator in generators) {
	        value tableModel = generator.produceTableModel(fixtures, parentMap);
	        value table = JTable(tableModel);
	        value modelSorter = TableRowSorter<TableModel>(tableModel);
	        value distanceFields = generator.headerRow.locations("distance".equalsIgnoringCase);
	        for (index->field in distanceFields) {
	            modelSorter.setComparator(index, wrapped);
	        }
	        table.rowSorter = modelSorter;
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
	    MutableList<String> unhandled = ArrayList<String>();
	    for ([loc, fixture] in fixtures.items) {
	        if (is TerrainFixture fixture) {
	            fixtures.remove(fixture.id);
	        } else {
	            unhandled.add(fixture.string);
	        }
	    }
	    if (!unhandled.empty) {
	        consumer("other", BorderedPanel.verticalPanel(
	            JLabel("Fixtures not covered in any of the reports:"),
	            JList<JString>(createJavaStringArray(unhandled)), null));
	    }
	}
}