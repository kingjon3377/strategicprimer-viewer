import ceylon.collection {
    LinkedList,
    Queue
}
import ceylon.file {
    Directory
}
import ceylon.test {
    assertEquals,
    test,
    assertThatException
}

import strategicprimer.model.common.map {
    MapDimensions,
    MapDimensionsImpl,
    Point,
    TileType
}
import ceylon.logging {
    logger,
    Logger
}
import strategicprimer.model.common.map.fixtures.terrain {
    Forest
}
import lovelace.util.common {
    defer
}

Logger log = logger(`module strategicprimer.drivers.exploration.old`);

"Load all tables in the specified path."
shared void loadAllTables(Directory path, ExplorationRunner runner) {
    // While it would probably be possible to exclude dotfiles using the `filter`
    // parameter to `Directory.files()`, this would be inefficient.
    for (child in path.files()) {
        if (child.hidden || child.name.startsWith(".")) {
            log.info("``child.name`` looks like a hidden file, skipping ...");
        } else {
            try {
                runner.loadTableFromFile(child);
            } catch (Exception except) {
                log.error("Error loading ``child.name``, continuing ...");
                log.debug("Details of that error:", except);
            }
        }
    }
}

"Tests of the table-loading functionality."// TODO: merge w/ other ExplorationRunner tests
object loadTableTests {
    ExplorationRunner runner = ExplorationRunner();
    "Test loading [[quadrant-based tables|QuadrantTable]]."
    test
    shared void testLoadQuadrantTable() {
        Queue<String> data = LinkedList<String>(["quadrant", "2", "one", "two", "three",
            "four", "five", "six"]);
        String table = "testLoadQuadrantTable().result";
        runner.loadTableFromDataStream(data.accept, table);
        Point point = Point(0, 0);
        MapDimensions dimensions = MapDimensionsImpl(69, 88, 2);
        assertEquals("one", runner.consultTable(table, point, TileType.tundra, false,
            [], dimensions), "loading quadrant table");
        Point pointTwo = Point(36, 30);
        assertEquals("one", runner.consultTable(table, point, TileType.ocean, false,
            [], dimensions), "quadrant table isn't a terrain table");
        assertEquals( runner.consultTable(table, pointTwo, TileType.tundra, false,
            [], dimensions), "five", "quadrant table isn't a constant table");
        assertEquals( runner.consultTable(table, pointTwo, TileType.tundra, false,
            [], MapDimensionsImpl(35, 32, 2)), "six",
            "quadrant table can use alternate dimensions");
        assertThatException(defer(runner.loadTableFromDataStream,
            [LinkedList{"quadrant"}.accept, "testLoadQuadrantTable().illegal"]));
    }

    "A mock object to make sure that tables other than [[QuadrantTable]] do not
     use the map dimensions."
    suppressWarnings("expressionTypeNothing")
    object mockDimensions satisfies MapDimensions {
        shared actual Integer rows => nothing;
        shared actual Integer columns => nothing;
        shared actual Integer version => nothing;
    }

    "Test loading [[random-number-based tables|RandomTable]]."
    test
    shared void testLoadRandomTable() {
        String table = "testLoadRandomTable()";
        runner.loadTableFromDataStream(LinkedList{"random", "0 one", "99 two"}.accept,
            table);
        assertEquals(runner.consultTable(table, Point.invalidPoint, TileType.tundra,
            false, [], mockDimensions), "one", "loading random table");
    }

    "Test loading [[terrain-based tables|TerrainTable]]."
    test
    shared void testLoadTerrainTable() {
        String table = "testLoadTerrainTable()";
        runner.loadTableFromDataStream(LinkedList{"terrain", "tundra one",
            "plains two", "ocean three", "mountain four", "temperate_forest five"}.accept,
            table);
        assertEquals(runner.consultTable(table, Point.invalidPoint, TileType.tundra,
            false, [], mockDimensions), "one", "loading terrain table: tundra");
        assertEquals(runner.consultTable(table, Point.invalidPoint, TileType.plains,
            false, [], mockDimensions), "two", "loading terrain table: plains");
        assertEquals(runner.consultTable(table, Point.invalidPoint, TileType.ocean, false,
            [], mockDimensions), "three", "loading terrain table: ocean");
        assertEquals(runner.consultTable(table, Point.invalidPoint, TileType.plains,
            false, {Forest("forestKind", false, 1)}, mockDimensions), "five", // TODO: [] instead of {}
            "loading terrain table: version 2 equivalent of temperate forest");
        assertEquals(runner.consultTable(table, Point.invalidPoint, TileType.plains, true,
            [], mockDimensions), "four",
            "loading terrain table: version 2 equivalent of mountain");
    }

    """Test loading [[constant "tables"|ConstantTable]]"""
    test
    shared void testLoadConstantTable() {
        String table = "testLoadConstantTable()";
        runner.loadTableFromDataStream(LinkedList{"constant", "one"}.accept, table);
        assertEquals(runner.consultTable(table, Point.invalidPoint, TileType.plains,
            false, [], mockDimensions), "one");
    }

    "Test that the table-loading code correctly rejects invalid input."
    test
    shared void testTableLoadingInvalidInput() {
        // no data
        assertThatException(defer(runner.loadTableFromDataStream,
            [LinkedList{""}.accept, "testTableLoadingInvalidInput().noData"]));
        // invalid header
        assertThatException(defer(runner.loadTableFromDataStream,
            [LinkedList{"2", "invalidData", "invalidData"}.accept,
                "testTableLoadingInvalidInput().invalidHeader"]));
    }
}
