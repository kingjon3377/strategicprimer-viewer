import ceylon.collection {
    LinkedList,
    Queue,
    MutableList,
    ArrayList
}
import ceylon.file {
    Directory,
    File
}
import ceylon.test {
    assertEquals,
    test,
    assertThatException
}

import java.io {
    IOException
}
import java.lang {
    IllegalArgumentException
}

import strategicprimer.model.impl.map {
    Point,
    MapDimensions,
    MapDimensionsImpl,
    TileType,
    invalidPoint
}
import ceylon.logging {
    logger,
    Logger
}
import strategicprimer.model.impl.map.fixtures.terrain {
    Forest
}
import lovelace.util.common {
    defer
}

Logger log = logger(`module strategicprimer.drivers.exploration.old`);
// Made shared so the oneToTwoConverter tests can get tables as classpath resources and
// load them from there. Also used by the 'town contents' generator.
shared EncounterTable loadTable(<String|Finished>?()|{String*}|File|Resource argument,
        String name) {
    if (is File argument) {
        try (reader = argument.Reader()) {
            return loadTable(reader.readLine, argument.name);
        }
    } else if (is Resource argument) {
        {String+} split = argument.textContent().split('\n'.equals);
        return loadTable(LinkedList(split).accept, argument.name);
    } else if (is {String*} argument) {
        return loadTable(argument.iterator().next, name);
    } else {
        if (is String line = argument()) {
            switch (line[0])
            case (null) {
                throw IOException("File doesn't start by specifying which kind of table");
            }
            case ('q'|'Q') {
                if (is String firstLine = argument()) {
                    value rows = Integer.parse(firstLine);
                    if (is Integer rows) {
                        MutableList<String> items = LinkedList<String>();
                        while (is String tableLine = argument()) {
                            items.add(tableLine);
                        }
                        return QuadrantTable(rows, *items);
                    } else {
                        throw IOException(
                            "File doesn't start with number of rows of quadrants", rows);
                    }
                } else {
                    throw IOException(
                        "File doesn't start with number of rows of quadrants");
                }
            }
            case ('r'|'R') {
                MutableList<[Integer, String]> list =
                        ArrayList<[Integer, String]>();
                variable Boolean first = true;
                while (is String tableLine = argument()) {
                    value splitted = tableLine.split(' '.equals, true, false);
                    if (splitted.size < 2) {
                        if (first, tableLine == line) {
                            log.debug("Ceylon tried to read the same line twice again");
                        } else if (tableLine.empty) {
                            log.debug("Unexpected blank line");
                        } else {
                            log.error("Line with no blanks, coninuing ...");
                            log.info("It was '``tableLine``'");
                        }
                    } else {
                        String left = splitted.first;
                        value leftNum = Integer.parse(left);
                        if (is Integer leftNum) {
                            list.add([leftNum, " ".join(splitted.rest)]);
                        } else {
                            throw IOException("Non-numeric data", leftNum);
                        }
                    }
                    first = false;
                }
                return RandomTable(*list);
            }
            case ('c'|'C') {
                if (is String tableLine = argument()) {
                    return ConstantTable(tableLine);
                } else {
                    throw IOException("constant value not present");
                }
            }
            case ('t'|'T') {
                MutableList<TileType->String> list =
                        ArrayList<TileType->String>();
                variable Boolean first = true;
                while (is String tableLine = argument()) {
                    value splitted = tableLine.split(' '.equals, true, false);
                    if (splitted.size < 2) {
                        if (first, tableLine == line) {
                            log.debug("Ceylon tried to read the same line twice again");
                        } else if (tableLine.empty) {
                            log.debug("Unexpected blank line");
                        } else {
                            log.error("Line with no blanks, coninuing ...");
                            log.info("It was '``tableLine``'");
                        }
                    } else {
                        String left = splitted.first;
                        value leftVal = TileType.parse(left);
                        "Terrain tables must only contain recognized tile types"
                        assert (is TileType leftVal);
                        list.add(leftVal->(" ".join(splitted.rest)));
                    }
                    first = false;
                }
                return TerrainTable(*list);
            }
            else {
                throw AssertionError("unknown table type '``line`` in file ``name``");
            }
        } else {
            throw IOException("File ``name`` doesn't specify a table type");
        }
    }
}
"Load all tables in the specified path."
shared void loadAllTables(Directory path, ExplorationRunner runner) {
    // While it would probably be possible to exclude dotfiles using the `filter`
    // parameter to `Directory.files()`, this would be inefficient.
    for (child in path.files()) {
        if (child.hidden || child.name.startsWith(".")) {
            log.info("``child.name`` looks like a hidden file, skipping ...");
        } else {
            try {
                runner.loadTable(child.name, loadTable(child, child.name));
            } catch (IllegalArgumentException except) {
                log.error("Error loading ``child.name``, continuing ...");
                log.debug("Details of that error:", except);
            }
        }
    }
}
object loadTableTests {
    test
    shared void testLoadQuadrantTable() {
        Queue<String> data = LinkedList<String>(["quadrant", "2", "one", "two", "three",
            "four", "five", "six"]);
        EncounterTable result = loadTable(data.accept, "testLoadQuadrantTable().result");
        Point point = Point(0, 0);
        MapDimensions dimensions = MapDimensionsImpl(69, 88, 2);
        assertEquals("one",result.generateEvent(point, TileType.tundra, false,
            [], dimensions), "loading quadrant table");
        Point pointTwo = Point(36, 30);
        assertEquals("one",result.generateEvent(point, TileType.ocean, false,
            [], dimensions), "quadrant table isn't a terrain table");
        assertEquals(result.generateEvent(pointTwo, TileType.tundra, false,
            [], dimensions), "five", "quadrant table isn't a constant table");
        assertEquals(result.generateEvent(pointTwo, TileType.tundra, false,
            [], MapDimensionsImpl(35, 32, 2)), "six",
            "quadrant table can use alternate dimensions");
        assertThatException(defer(loadTable,
            [LinkedList{"quadrant"}.accept, "testLoadQuadrantTable().illegal"]));
    }
    suppressWarnings("expressionTypeNothing")
    object mockDimensions satisfies MapDimensions {
        shared actual Integer rows => nothing;
        shared actual Integer columns => nothing;
        shared actual Integer version => nothing;
    }
    test
    shared void testLoadRandomTable() {
        EncounterTable result = loadTable(LinkedList{"random", "0 one", "99 two"}.accept,
            "testLoadRandomTable()");
        assertEquals(result.generateEvent(invalidPoint, TileType.tundra, false, [],
            mockDimensions), "one", "loading random table");
    }
    test
    shared void testLoadTerrainTable() {
        EncounterTable result = loadTable(LinkedList{"terrain", "tundra one",
            "plains two", "ocean three", "mountain four", "temperate_forest five"}.accept,
            "testLoadTerrainTable()");
        assertEquals(result.generateEvent(invalidPoint, TileType.tundra, false,
            [], mockDimensions), "one",
            "loading terrain table: tundra");
        assertEquals(result.generateEvent(invalidPoint, TileType.plains, false,
            [], mockDimensions), "two", "loading terrain table: plains");
        assertEquals(result.generateEvent(invalidPoint, TileType.ocean, false,
            [], mockDimensions), "three",
            "loading terrain table: ocean");
        assertEquals(result.generateEvent(invalidPoint, TileType.plains, false,
            {Forest("forestKind", false, 1)}, mockDimensions), "five",
            "loading terrain table: version 2 equivalent of temperate forest");
        assertEquals(result.generateEvent(invalidPoint, TileType.plains, true, [],
            mockDimensions), "four",
            "loading terrain table: version 2 equivalent of mountain");
    }
    test
    shared void testLoadConstantTable() {
        EncounterTable result = loadTable(LinkedList{"constant", "one"}.accept,
            "testLoadConstantTable()");
        assertEquals(result.generateEvent(invalidPoint, TileType.plains, false,
            [], mockDimensions), "one");
    }
    test
    shared void testTableLoadingInvalidInput() {
        // no data
        assertThatException(defer(loadTable,
            [LinkedList{""}.accept, "testTableLoadingInvalidInput().noData"]));
        // invalid header
        assertThatException(defer(loadTable, [LinkedList{"2", "invalidData",
            "invalidData"}.accept, "testTableLoadingInvalidInput().invalidHeader"]));
    }
}