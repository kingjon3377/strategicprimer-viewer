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

import strategicprimer.model.map {
    Point,
    MapDimensions,
    MapDimensionsImpl,
    TileType,
    pointFactory
}
import ceylon.logging {
    logger,
    Logger
}
import strategicprimer.model.map.fixtures.terrain {
    Forest
}

Logger log = logger(`module strategicprimer.drivers.exploration.old`);
// Made shared so the oneToTwoConverter tests can get tables as classpath resources and
// load them from there. Also used by the 'town contents' generator.
shared EncounterTable loadTable(String?()|{String*}|File|Resource argument) {
    if (is File argument) {
        try (reader = argument.Reader()) {
            return loadTable(reader.readLine);
        }
    } else if (is Resource argument) {
        String text = argument.textContent();
        {String+} split = text.split('\n'.equals);
        return loadTable(ArrayList { *split }.accept);
    } else if (is {String*} argument) {
        // TODO: This is probably highly inefficient ...
        variable {String*} temp = argument;
        String? lambda() {
            String? retval = temp.first;
            temp = temp.rest;
            return retval;
        }
        return loadTable(lambda);
    } else {
        if (exists line = argument()) {
            switch (line[0])
            case (null) {
                throw IOException("File doesn't start by specifying which kind of table");
            }
            case ('q'|'Q') {
                if (exists firstLine = argument()) {
                    value rows = Integer.parse(firstLine);
                    if (is Integer rows) {
                        MutableList<String> items = LinkedList<String>();
                        while (exists tableLine = argument()) {
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
                while (exists tableLine = argument()) {
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
                        assert (exists right = splitted.rest.reduce(
                                    (String partial, element) =>
                                    "``partial`` ``element``"));
                        value leftNum = Integer.parse(left);
                        if (is Integer leftNum) {
                            list.add([leftNum, right]);
                        } else {
                            throw IOException("Non-numeric data", leftNum);
                        }
                    }
                    first = false;
                }
                return RandomTable(*list);
            }
            case ('c'|'C') {
                if (exists tableLine = argument()) {
                    return ConstantTable(tableLine);
                } else {
                    throw IOException("constant value not present");
                }
            }
            case ('l'|'L') { return LegacyTable(); }
            case ('t'|'T') {
                MutableList<TileType->String> list =
                        ArrayList<TileType->String>();
                variable Boolean first = true;
                while (exists tableLine = argument()) {
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
                        assert (exists right = splitted.rest.reduce(
                                    (String partial, element) =>
                                    "``partial`` ``element``"));
                        value leftVal = TileType.parse(left);
                        if (is TileType leftVal) {
                            list.add(leftVal->right);
                        } else {
                            throw IllegalArgumentException(
                                "'Tile type' wasn't a recognized tile type", leftVal);
                        }
                    }
                    first = false;
                }
                return TerrainTable(*list);
            }
            else { throw IllegalArgumentException("unknown table type"); }
        } else {
            throw IOException("File doesn't specify a table type");
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
            runner.loadTable(child.name, loadTable(child));
        }
    }
}
test
void testLoadQuadrantTable() {
    Queue<String> data = LinkedList<String>({"quadrant", "2", "one", "two", "three",
        "four", "five", "six"});
    EncounterTable result = loadTable(data.accept);
    Point point = pointFactory(0, 0);
    MapDimensions dimensions = MapDimensionsImpl(69, 88, 2);
    assertEquals("one",result.generateEvent(point, TileType.tundra, false,
        {}, dimensions), "loading quadrant table");
    Point pointTwo = pointFactory(36, 30);
    assertEquals("one",result.generateEvent(point, TileType.ocean, false,
        {}, dimensions), "quadrant table isn't a terrain table");
    assertEquals(result.generateEvent(pointTwo, TileType.tundra, false,
        {}, dimensions), "five", "quadrant table isn't a constant table");
    assertEquals(result.generateEvent(pointTwo, TileType.tundra, false,
        {}, MapDimensionsImpl(35, 32, 2)), "six",
        "quadrant table can use alternate dimensions");
    assertThatException(() => loadTable(LinkedList{"quadrant"}.accept));
}
suppressWarnings("expressionTypeNothing")
object mockDimensions satisfies MapDimensions {
    shared actual Integer rows => nothing;
    shared actual Integer columns => nothing;
    shared actual Integer version => nothing;
}
suppressWarnings("expressionTypeNothing")
object mockPoint satisfies Point {
    shared actual Integer column => nothing;
    shared actual Integer row => nothing;
}
test
void testLoadRandomTable() {
    EncounterTable result = loadTable(LinkedList{"random", "0 one", "99 two"}.accept);
    assertEquals(result.generateEvent(mockPoint, TileType.tundra, false, {},
        mockDimensions), "one", "loading random table");
}
test
void testLoadTerrainTable() {
    EncounterTable result = loadTable(LinkedList{"terrain", "tundra one",
        "plains two", "ocean three", "mountain four", "temperate_forest five"}.accept);
    assertEquals(result.generateEvent(mockPoint, TileType.tundra, false,
        {}, mockDimensions), "one",
        "loading terrain table: tundra");
    assertEquals(result.generateEvent(mockPoint, TileType.plains, false,
        {}, mockDimensions), "two", "loading terrain table: plains");
    assertEquals(result.generateEvent(mockPoint, TileType.ocean, false,
        {}, mockDimensions), "three",
        "loading terrain table: ocean");
    assertEquals(result.generateEvent(mockPoint, TileType.plains, false,
        {Forest("forestKind", false, 1)}, mockDimensions), "five",
        "loading terrain table: version 2 equivalent of temperate forest");
    assertEquals(result.generateEvent(mockPoint, TileType.plains, true, {},
        mockDimensions), "four",
        "loading terrain table: version 2 equivalent of mountain");
}
test
void testLoadConstantTable() {
    EncounterTable result = loadTable(LinkedList{"constant", "one"}.accept);
    assertEquals(result.generateEvent(mockPoint, TileType.plains, false,
        {}, mockDimensions), "one");
}
test
void testTableLoadingInvalidInput() {
    // no data
    assertThatException(() => loadTable(LinkedList{""}.accept));
    // invalid header
    assertThatException(() => loadTable(LinkedList{"2", "invalidData",
        "invalidData"}.accept));
}
