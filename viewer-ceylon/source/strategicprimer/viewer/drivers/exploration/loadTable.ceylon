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
import ceylon.interop.java {
    javaString,
    JavaList
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
    IllegalArgumentException,
    JString=String,
    JInteger=Integer
}
import java.util.stream {
    Stream
}

import model.exploration.old {
    EncounterTable,
    QuadrantTable,
    ConstantTable,
    LegacyTable
}
import model.map {
    PointFactory,
    TileType,
    TileFixture,
    MapDimensionsImpl,
    MapDimensions,
    Point
}

import util {
    ComparablePair,
    Pair
}
EncounterTable loadTable(String?()|File argument) {
    if (is File argument) {
        try (reader = argument.Reader()) {
            return loadTable(reader.readLine);
        }
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
                        MutableList<JString> items = LinkedList<JString>();
                        while (exists tableLine = argument()) {
                            items.add(javaString(tableLine));
                        }
                        return QuadrantTable(rows, JavaList(items));
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
                // TODO: Use Tuples once RandomTable is ported to Ceylon.
                MutableList<[Integer, String]> list =
                        ArrayList<[Integer, String]>();
                while (exists tableLine = argument()) {
                    value splitted = tableLine.split(" ".equals, true, false);
                    if (splitted.size < 2) {
                        log.error("Line with no blanks, coninuing ...");
                    } else {
                        String left = splitted.first;
                        assert (exists right = splitted.rest.reduce(
                                    (String partial, element) => "``partial`` ``element``"));
                        value leftNum = Integer.parse(left);
                        if (is Integer leftNum) {
                            list.add([leftNum, right]);
                        } else {
                            throw IOException("Non-numeric data", leftNum);
                        }
                    }
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
                // TODO: Use Tuples once RandomTable is ported to Ceylon.
                MutableList<TileType->String> list =
                        ArrayList<TileType->String>();
                while (exists tableLine = argument()) {
                    value splitted = tableLine.split(" ".equals, true, false);
                    if (splitted.size < 2) {
                        log.error("Line with no blanks, coninuing ...");
                    } else {
                        String left = splitted.first;
                        assert (exists right = splitted.rest.reduce(
                                    (String partial, element) => "``partial`` ``element``"));
                        value leftVal = TileType.getTileType(left);
                        list.add(leftVal->right);
                    }
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
    // TODO: is it possible to exclude dotfiles using the "filter" parameter to files()?
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
    Point point = PointFactory.point(0, 0);
    MapDimensions dimensions = MapDimensionsImpl(69, 88, 2);
    assertEquals("one",result.generateEvent(point, TileType.tundra,
        Stream.empty<TileFixture>(), dimensions), "loading quadrant table");
    Point pointTwo = PointFactory.point(36, 30);
    assertEquals("one",result.generateEvent(point, TileType.ocean,
        Stream.empty<TileFixture>(), dimensions), "quadrant table isn't a terrain table");
    assertEquals(result.generateEvent(pointTwo, TileType.tundra,
        Stream.empty<TileFixture>(), dimensions), "five",
        "quadrant table isn't a constant table");
    assertEquals(result.generateEvent(pointTwo, TileType.tundra,
        Stream.empty<TileFixture>(), MapDimensionsImpl(35, 32, 2)), "six",
        "quadrant table can use alternate dimensions");
    assertThatException(() => loadTable(LinkedList({"quadrant"}).accept));
}
object mockDimensions satisfies MapDimensions {
    shared actual Integer rows => nothing;
    shared actual Integer columns => nothing;
    shared actual Integer version => nothing;
}
object mockPoint satisfies Point {
    shared actual Integer col => nothing;
    shared actual Integer row => nothing;
}
test
void testLoadRandomTable() {
    EncounterTable result = loadTable(LinkedList({"random", "0 one", "99 two"}).accept);
    assertEquals(result.generateEvent(mockPoint, TileType.tundra, Stream.empty<TileFixture>(),
        mockDimensions), "one", "loading random table");
}
test
void testLoadTerrainTable() {
    EncounterTable result = loadTable(LinkedList({"terrain", "tundra one",
        "plains two", "ocean three"}).accept);
    assertEquals(result.generateEvent(mockPoint, TileType.tundra,
        Stream.empty<TileFixture>(), mockDimensions), "one",
        "loading terrain table: tundra");
    assertEquals(result.generateEvent(mockPoint, TileType.plains,
        Stream.empty<TileFixture>(), mockDimensions), "two",
        "loading terrain table: plains");
    assertEquals(result.generateEvent(mockPoint, TileType.ocean,
        Stream.empty<TileFixture>(), mockDimensions), "three",
        "loading terrain table: ocean");
}
test
void testLoadConstantTable() {
    EncounterTable result = loadTable(LinkedList({"constant", "one"}).accept);
    assertEquals(result.generateEvent(mockPoint, TileType.plains,
        Stream.empty<TileFixture>(), mockDimensions), "one");
}
test
void testTableLoadingInvalidInput() {
    // no data
    assertThatException(() => loadTable(LinkedList({""}).accept));
    // invalid header
    assertThatException(() => loadTable(LinkedList({"2", "invalidData",
        "invalidData"}).accept));
}
