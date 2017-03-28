import ceylon.collection {
    HashSet,
    HashMap,
    MutableSet,
    LinkedList,
    Queue,
    MutableMap
}
import ceylon.interop.java {
    CeylonIterable
}
import ceylon.regex {
    Regex,
    regex
}
import ceylon.test {
    test,
    assertEquals,
    assertThatException,
    assertTrue
}

import controller.map.iointerfaces {
    ISPReader
}

import java.io {
    StringWriter,
    IOException,
    JFileReader=FileReader,
    FileNotFoundException,
    StringReader
}
import java.lang {
    Appendable,
    IllegalArgumentException
}
import java.nio.file {
    NoSuchFileException
}
import java.util {
    Formatter
}

import javax.xml {
    XMLConstants
}
import javax.xml.namespace {
    QName
}
import javax.xml.stream {
    XMLStreamException,
    XMLInputFactory
}
import javax.xml.stream.events {
    Attribute,
    EndElement,
    XMLEvent,
    StartElement,
    Characters,
    StartDocument,
    EndDocument
}

import lovelace.util.common {
    todo
}
import lovelace.util.jvm {
    shuffle,
    ConvertingIterable,
    EnumCounter
}

import strategicprimer.viewer.model.map {
    SPMapNG,
    IMutableMapNG,
    IMapNG
}
import model.map {
    River,
    Player,
    MapDimensionsImpl,
    Point,
    PointFactory,
    TileType,
    PlayerCollection,
    TileFixture,
    PlayerImpl
}
import model.map.fixtures {
    TextFixture,
    Ground
}
import model.map.fixtures.explorable {
    AdventureFixture
}
import model.map.fixtures.mobile {
    Dragon,
    Centaur,
    Animal
}
import strategicprimer.viewer.model.map.fixtures.mobile {
    Fairy,
    Giant,
    SimpleImmortalKind,
    SimpleImmortal,
    Unit,
    IUnit
}
import strategicprimer.viewer.model.map.fixtures.terrain {
    Hill,
    Forest
}

import strategicprimer.viewer.model {
    IDFactory,
    IDRegistrar
}
import strategicprimer.viewer.model.map.fixtures.resources {
    FieldStatus,
    Meadow,
    MineralVein,
    CacheFixture,
    Mine,
    StoneKind,
    StoneDeposit,
    Grove
}
import strategicprimer.viewer.model.map.fixtures.towns {
    TownStatus,
    TownSize,
    Village,
    City,
    Fortification,
    Town,
    Fortress
}
import strategicprimer.viewer.xmlio {
    readMap,
    testReaderFactory,
    TypesafeXMLEventReader,
    SPWriter
}

import util {
    Warning,
    LineEnd,
    IteratorWrapper
}

import view.util {
    SystemOut
}
void assertModuloID(IMapNG map, String serialized, Formatter err) {
    Regex matcher = regex("id=\"[0-9]*\"", true);
    try (inStream = StringReader(matcher.replace(serialized, "id=\"-1\""))) {
        assertTrue(
            map.isSubset(readMap(inStream, Warning.ignore), err, ""),
            "Actual is at least subset of expected converted, modulo IDs");
    }
}
void initialize(IMutableMapNG map, Point point, TileType? terrain, TileFixture* fixtures) {
    if (exists terrain, terrain != TileType.notVisible) {
        map.setBaseTerrain(point, terrain);
    }
    for (fixture in fixtures) {
        if (is Ground fixture, !map.getGround(point) exists) {
            map.setGround(point, fixture);
        } else if (is Forest fixture, !map.getForest(point) exists) {
            map.setForest(point, fixture);
        } else {
            map.addFixture(point, fixture);
        }
    }
}
ISPReader oldReader = testReaderFactory.oldReader;
ISPReader newReader = testReaderFactory.newReader;
SPWriter oldWriter = testReaderFactory.oldWriter;
SPWriter newWriter = testReaderFactory.newWriter;
test
shared void testOneToTwoConversion() {
    IMutableMapNG original = SPMapNG(MapDimensionsImpl(2, 2, 1), PlayerCollection(), 0);
    original.setBaseTerrain(PointFactory.point(0, 0), TileType.borealForest);
    original.setBaseTerrain(PointFactory.point(0, 1), TileType.temperateForest);
    original.setBaseTerrain(PointFactory.point(1, 0), TileType.desert);
    original.setBaseTerrain(PointFactory.point(1, 1), TileType.plains);
    Player player = PlayerImpl(1, "playerName");
    original.addPlayer(player);
    Player independent = PlayerImpl(2, "independent");
    original.addPlayer(independent);

    IMutableMapNG converted = SPMapNG(MapDimensionsImpl(8, 8, 2), PlayerCollection(), -1);
    converted.addPlayer(player);
    converted.addPlayer(independent);
    Ground groundOne() => Ground(-1, "rock1", false);
    Ground groundTwo() => Ground(-1, "rock4", false);
    Village village(String race) =>
            Village(TownStatus.active, "", -1, independent, race);
    Forest forest(String kind) => Forest(kind, false, -1);
    Meadow field(FieldStatus status, String kind = "grain1") =>
            Meadow(kind, true, true, -1, status);
    Grove orchard(String kind = "fruit1") => Grove(true, true, kind, -1);
    initialize(converted, PointFactory.point(0, 0), TileType.steppe, groundOne(),
        village("human"));
    initialize(converted, PointFactory.point(0, 1), TileType.steppe, groundOne(),
        forest("btree1"), field(FieldStatus.growing));
    initialize(converted, PointFactory.point(1, 0), TileType.steppe, groundOne(),
        forest("btree1"), orchard());
    initialize(converted, PointFactory.point(1, 1), TileType.steppe, groundOne(),
        forest("btree1"), field(FieldStatus.growing));
    initialize(converted, PointFactory.point(2, 1), TileType.steppe, groundOne(),
        forest("btree1"), forest("ttree1"));
    initialize(converted, PointFactory.point(2, 6), TileType.plains, groundOne(),
        forest("ttree1"), orchard());
    initialize(converted, PointFactory.point(3, 0), TileType.steppe, groundOne(),
        forest("btree1"), field(FieldStatus.growing));
    initialize(converted, PointFactory.point(3, 6), TileType.plains, groundOne(),
        forest("ttree1"), orchard());
    initialize(converted, PointFactory.point(3, 7), TileType.plains, groundOne(),
        village("dwarf"));
    initialize(converted, PointFactory.point(4, 0), TileType.desert, groundOne(),
        village("human"));
    initialize(converted, PointFactory.point(4, 1), TileType.desert, groundOne(),
        orchard());
    initialize(converted, PointFactory.point(4, 6), TileType.plains, groundTwo(),
        orchard("fruit4"));
    initialize(converted, PointFactory.point(4, 7), TileType.plains, groundTwo(),
        field(FieldStatus.growing, "grain4"));
    initialize(converted, PointFactory.point(5, 0), TileType.desert, groundOne(),
        field(FieldStatus.growing));
    initialize(converted, PointFactory.point(6, 5), TileType.plains, groundTwo(),
        orchard("fruit4"));
    initialize(converted, PointFactory.point(6, 6), TileType.plains, groundTwo(),
        field(FieldStatus.growing, "grain4"));
    initialize(converted, PointFactory.point(6, 7), TileType.plains, groundTwo(),
        field(FieldStatus.growing, "grain4"));
    initialize(converted, PointFactory.point(7, 6), TileType.plains, groundTwo(),
        village("human"));
    initialize(converted, PointFactory.point(5, 7), TileType.plains, groundTwo(),
        forest("ttree4"));
    initialize(converted, PointFactory.point(7, 7), TileType.plains, groundTwo(),
        orchard("fruit4"));
    for (loc in { PointFactory.point(0, 2), PointFactory.point(0, 3),
        PointFactory.point(1, 2), PointFactory.point(1, 3),
        PointFactory.point(2, 0), PointFactory.point(2, 2),
        PointFactory.point(2, 3), PointFactory.point(3, 1),
        PointFactory.point(3, 2), PointFactory.point(3, 3) }) {
        initialize(converted, loc, TileType.steppe, groundOne(), forest("btree1"));
    }
    for (loc in { PointFactory.point(0, 4), PointFactory.point(0, 5),
        PointFactory.point(0, 6), PointFactory.point(0, 7),
        PointFactory.point(1, 4), PointFactory.point(1, 5),
        PointFactory.point(1, 6), PointFactory.point(1, 7),
        PointFactory.point(2, 4), PointFactory.point(2, 5),
        PointFactory.point(2, 7), PointFactory.point(3, 4),
        PointFactory.point(3, 5) }) {
        initialize(converted, loc, TileType.plains, groundOne(), forest("ttree1"));
    }
    for (loc in { PointFactory.point(4, 2), PointFactory.point(5, 1),
        PointFactory.point(5, 2), PointFactory.point(6, 0),
        PointFactory.point(6, 1), PointFactory.point(6, 3),
        PointFactory.point(7, 0), PointFactory.point(7, 2) }) {
        initialize(converted, loc, TileType.desert, groundOne());
    }
    for (loc in { PointFactory.point(4, 3), PointFactory.point(5, 3),
        PointFactory.point(6, 2), PointFactory.point(7, 1),
        PointFactory.point(7, 3) }) {
        initialize(converted, loc, TileType.plains, groundOne());
    }
    for (loc in { PointFactory.point(4, 4), PointFactory.point(4, 5),
        PointFactory.point(5, 4), PointFactory.point(5, 5),
        PointFactory.point(5, 6), PointFactory.point(6, 4),
        PointFactory.point(7, 4), PointFactory.point(7, 5) }) {
        initialize(converted, loc, TileType.plains, groundTwo());
    }

    try (outOne = StringWriter(), outTwo = StringWriter()) {
        SPWriter writer = newWriter;
        writer.write(outOne, converted);
        writer.write(outTwo, oneToTwoConverter.convert(original, true));
        Regex matcher = regex("id=\"[0-9]*\"", true);
        assertEquals(matcher.replace(outTwo.string, "id=\"-1\""), outOne.string,
            "Produces expected result");
    }
    try (outOne = StringWriter(), outTwo = StringWriter()) {
        SPWriter writer = oldWriter;
        writer.write(outOne, converted);
        writer.write(outTwo, oneToTwoConverter.convert(original, true));
        Regex matcher = regex("id=\"[0-9]*\"", true);
        assertEquals(matcher.replace(outTwo.string, "id=\"-1\""), outOne.string,
            "Deprecated I/O produces expected result");
    }
    try (outOne = Formatter(), outTwo = Formatter()) {
        assertEquals(
            converted.isSubset(oneToTwoConverter.convert(original, true), outTwo, ""),
            converted.isSubset(oneToTwoConverter.convert(original, true), outOne, ""),
            "Products of two runs are both or neither subsets of expected");
        assertEquals(outTwo.\iout().string, outOne.\iout().string,
            "Two runs produce identical results");
    }
    try (outStream = StringWriter(), errStream = Formatter()) {
        SPWriter writer = newWriter;
        writer.writeSPObject(outStream, oneToTwoConverter.convert(original, true));
        assertModuloID(converted, outStream.string, errStream);
    }
}
test
shared void testMoreOneToTwoConversion() {
    IMutableMapNG original = SPMapNG(MapDimensionsImpl(2, 2, 1), PlayerCollection(), 0);
    initialize(original, PointFactory.point(0, 0), TileType.jungle);
    initialize(original, PointFactory.point(0, 1), TileType.temperateForest,
        Forest("ttree1", false, 1));
    initialize(original, PointFactory.point(1, 0), TileType.mountain);
    initialize(original, PointFactory.point(1, 1), TileType.tundra,
        Ground(-1, "rock1", false));
    Player player = PlayerImpl(1, "playerName");
    original.addPlayer(player);
    Player independent = PlayerImpl(2, "independent");
    original.addPlayer(independent);

    IMutableMapNG converted = SPMapNG(MapDimensionsImpl(8, 8, 2), PlayerCollection(), -1);
    converted.addPlayer(player);
    converted.addPlayer(independent);
    Ground groundOne() => Ground(-1, "rock1", false);
    Ground groundTwo() => Ground(-1, "rock4", false);
    Village village(String race) =>
            Village(TownStatus.active, "", -1, independent, race);
    Forest forest(String kind) => Forest(kind, false, -1);
    Meadow field(FieldStatus status, String kind = "grain1") =>
            Meadow(kind, true, true, -1, status);
    Grove orchard(String kind = "fruit1") => Grove(true, true, kind, -1);
    initialize(converted, PointFactory.point(0, 0), TileType.jungle, groundOne(),
        village("human"));
    initialize(converted, PointFactory.point(0, 1), TileType.jungle, groundOne(),
        field(FieldStatus.growing));
    initialize(converted, PointFactory.point(2, 3), TileType.jungle, groundOne(),
        field(FieldStatus.growing));
    initialize(converted, PointFactory.point(2, 5), TileType.plains, groundOne(),
        forest("ttree1"), orchard());
    initialize(converted, PointFactory.point(3, 0), TileType.jungle, groundOne(),
        field(FieldStatus.growing));
    initialize(converted, PointFactory.point(3, 1), TileType.jungle, groundOne(),
        field(FieldStatus.growing));
    initialize(converted, PointFactory.point(3, 4), TileType.plains, groundOne(),
        village("human"));
    initialize(converted, PointFactory.point(3, 5), TileType.plains, groundOne(),
        forest("ttree1"), field(FieldStatus.growing));
    initialize(converted, PointFactory.point(4, 0), TileType.plains, groundOne(),
        village("Danan"));
    initialize(converted, PointFactory.point(4, 3), TileType.plains, groundOne(),
        orchard());
    initialize(converted, PointFactory.point(4, 4), TileType.tundra, groundOne(),
        field(FieldStatus.growing, "grain4"));
    initialize(converted, PointFactory.point(5, 0), TileType.plains, groundOne(),
        orchard());
    initialize(converted, PointFactory.point(5, 1), TileType.plains, groundOne(),
        field(FieldStatus.growing));
    initialize(converted, PointFactory.point(5, 4), TileType.tundra, groundTwo(),
        forest("ttree4"));
    initialize(converted, PointFactory.point(5, 6), TileType.tundra, groundTwo(),
        groundOne());
    initialize(converted, PointFactory.point(6, 5), TileType.tundra, groundTwo(),
        field(FieldStatus.growing, "grain4"));
    initialize(converted, PointFactory.point(6, 6), TileType.tundra, groundTwo(),
        orchard("fruit4"));
    initialize(converted, PointFactory.point(7, 5), TileType.tundra, groundTwo(),
        field(FieldStatus.growing, "grain4"));
    initialize(converted, PointFactory.point(7, 6), TileType.tundra, groundTwo(),
        forest("ttree4"), village("dwarf"));
    for (loc in { PointFactory.point(0, 2), PointFactory.point(0, 3),
        PointFactory.point(1, 0), PointFactory.point(1, 1), PointFactory.point(1, 2),
        PointFactory.point(1, 3), PointFactory.point(2, 0), PointFactory.point(2, 1),
        PointFactory.point(2, 2), PointFactory.point(3, 2),
        PointFactory.point(3, 3) }) {
        initialize(converted, loc, TileType.jungle, groundOne());
    }
    for (loc in { PointFactory.point(0, 4), PointFactory.point(0, 5),
        PointFactory.point(0, 6), PointFactory.point(0, 7), PointFactory.point(1, 4),
        PointFactory.point(1, 5), PointFactory.point(1, 6), PointFactory.point(1, 7),
        PointFactory.point(2, 4), PointFactory.point(2, 6), PointFactory.point(2, 7),
        PointFactory.point(3, 6), PointFactory.point(3, 7) }) {
        initialize(converted, loc, TileType.plains, groundOne(), forest("ttree1"));
    }
    for (loc in { PointFactory.point(4, 1), PointFactory.point(5, 2),
        PointFactory.point(7, 1) }) {
        initialize(converted, loc, TileType.plains, groundOne(), forest("ttree1"));
        converted.setMountainous(loc, true);
    }
    for (loc in { PointFactory.point(4, 2), PointFactory.point(5, 3),
        PointFactory.point(6, 0), PointFactory.point(6, 1), PointFactory.point(6, 2),
        PointFactory.point(6, 3), PointFactory.point(7, 0), PointFactory.point(7, 2),
        PointFactory.point(7, 3) }) {
        initialize(converted, loc, TileType.plains, groundOne());
        converted.setMountainous(loc, true);
    }
    for (loc in { PointFactory.point(4, 5), PointFactory.point(4, 6),
        PointFactory.point(4, 7), PointFactory.point(5, 5), PointFactory.point(5, 7),
        PointFactory.point(6, 4), PointFactory.point(6, 7), PointFactory.point(7, 4),
        PointFactory.point(7, 7) }) {
        initialize(converted, loc, TileType.tundra, groundTwo());
    }
    // The converter adds a second forest here, but trying to do the same gets it
    // filtered out because it would have the same ID. So we create it, *then* reset
    // its ID.
    Forest tempForest = Forest("ttree1", false, 0);
    converted.addFixture(PointFactory.point(3, 7), tempForest);
    tempForest.id = -1;
    for (loc in { PointFactory.point(4, 0), PointFactory.point(4, 3),
        PointFactory.point(5, 0), PointFactory.point(5, 1) }) {
        converted.setMountainous(loc, true);
    }
    try (outOne = StringWriter(), outTwo = StringWriter()) {
        SPWriter writer = newWriter;
        writer.write(outOne, converted);
        writer.write(outTwo, oneToTwoConverter.convert(original, true));
        Regex matcher = regex("id=\"[0-9]*\"", true);
        assertEquals(matcher.replace(outTwo.string, "id=\"-1\""), outOne.string,
            "Produces expected result");
    }
    try (outOne = StringWriter(), outTwo = StringWriter()) {
        SPWriter writer = oldWriter;
        writer.write(outOne, converted);
        writer.write(outTwo, oneToTwoConverter.convert(original, true));
        Regex matcher = regex("id=\"[0-9]*\"", true);
        assertEquals(matcher.replace(outTwo.string, "id=\"-1\""), outOne.string,
            "Deprecated I/O produces expected result");
    }
    try (outOne = Formatter(), outTwo = Formatter()) {
        assertEquals(
            converted.isSubset(oneToTwoConverter.convert(original, true), outTwo, ""),
            converted.isSubset(oneToTwoConverter.convert(original, true), outOne, ""),
            "Products of two runs are both or neither subsets of expected");
        assertEquals(outTwo.\iout().string, outOne.\iout().string,
            "Two runs produce identical results");
    }
    try (outStream = StringWriter(), errStream = Formatter()) {
        SPWriter writer = newWriter;
        writer.writeSPObject(outStream, oneToTwoConverter.convert(original, true));
        assertModuloID(converted, outStream.string, errStream);
    }
}

test
shared void testThirdOneToTwoConversion() {
    Ground groundOne() => Ground(-1, "rock1", false);
    Ground groundTwo() => Ground(-1, "rock4", false);
    Player independent = PlayerImpl(2, "independent");
    Village village(String race) =>
            Village(TownStatus.active, "", -1, independent, race);
    Forest forest(String kind) => Forest(kind, false, -1);
    Meadow field(FieldStatus status, String kind = "grain1") =>
            Meadow(kind, true, true, -1, status);
    Grove orchard(String kind = "fruit1") => Grove(true, true, kind, -1);

    IMutableMapNG original = SPMapNG(MapDimensionsImpl(2, 2, 1), PlayerCollection(), 0);
    initialize(original, PointFactory.point(0, 0), TileType.notVisible, groundOne());
    original.addPlayer(independent);
    initialize(original, PointFactory.point(0, 1), TileType.borealForest,
        Forest("ttree1", false, 1), Hill(1), Animal("animalKind", false, false, "wild", 2),
        Mine("mineral", TownStatus.active, 3), AdventureFixture(independent,
            "briefDescription", "fullDescription", 4),
        SimpleImmortal(SimpleImmortalKind.simurgh, 5),
        SimpleImmortal(SimpleImmortalKind.griffin, 6),
        City(TownStatus.ruined, TownSize.large, 0, "cityName", 7, independent),
        SimpleImmortal(SimpleImmortalKind.ogre, 8),
        SimpleImmortal(SimpleImmortalKind.minotaur, 9),
        Centaur("hill", 10), Giant("frost", 11),
        SimpleImmortal(SimpleImmortalKind.djinn, 12),
        Fairy("lesser", 13), Dragon("ice", 14),
        SimpleImmortal(SimpleImmortalKind.troll, 15),
        Fortification(TownStatus.burned, TownSize.medium, 0, "townName", 16, independent),
        StoneDeposit(StoneKind.conglomerate, 0, 17));
    initialize(original, PointFactory.point(1, 0), TileType.mountain);
    initialize(original, PointFactory.point(1, 1), TileType.tundra);
    original.addRivers(PointFactory.point(0, 1), River.lake, River.south);
    original.addRivers(PointFactory.point(1, 0), River.east, River.west);
    original.addRivers(PointFactory.point(1, 1), River.west, River.north);
    Player player = PlayerImpl(1, "playerName");
    original.addPlayer(player);

    IMutableMapNG converted = SPMapNG(MapDimensionsImpl(8, 8, 2), PlayerCollection(), -1);
    converted.addPlayer(player);
    converted.addPlayer(independent);
    initialize(converted, PointFactory.point(0, 4), TileType.steppe, groundOne(),
        forest("btree1"), Hill(-1), Centaur("hill", -1),
        field(FieldStatus.growing, "grain1"));
    initialize(converted, PointFactory.point(0, 5), TileType.steppe, groundOne(),
        forest("btree1"), SimpleImmortal(SimpleImmortalKind.troll, -1),
        forest("ttree1"));
    initialize(converted, PointFactory.point(0, 6), TileType.steppe, groundOne(),
        forest("btree1"), AdventureFixture(independent, "briefDescription",
            "fullDescription", -1));
    initialize(converted, PointFactory.point(0, 7), TileType.steppe, groundOne(),
        forest("btree1"), SimpleImmortal(SimpleImmortalKind.griffin, -1),
        forest("ttree1"));
    initialize(converted, PointFactory.point(1, 0), TileType.notVisible, groundOne(),
        forest("ttree1"));
    initialize(converted, PointFactory.point(1, 3), TileType.notVisible, groundOne(),
        forest("ttree1"), village("half-elf"));
    initialize(converted, PointFactory.point(1, 4), TileType.steppe, groundOne(),
        forest("btree1"), Mine("mineral", TownStatus.active, -1),
        field(FieldStatus.growing));
    initialize(converted, PointFactory.point(1, 5), TileType.steppe, groundOne(),
        forest("btree1"), Giant("frost", -1), forest("ttree1"));
    initialize(converted, PointFactory.point(1, 6), TileType.steppe, groundOne(),
        forest("btree1"), StoneDeposit(StoneKind.conglomerate, 0, -1));
    initialize(converted, PointFactory.point(1, 7), TileType.steppe, groundOne(),
        forest("btree1"), Dragon("ice", -1), field(FieldStatus.growing));
    initialize(converted, PointFactory.point(2, 2), TileType.notVisible, groundOne(),
        orchard());
    initialize(converted, PointFactory.point(2, 3), TileType.notVisible, groundOne(),
        field(FieldStatus.growing));
    initialize(converted, PointFactory.point(2, 4), TileType.steppe, groundOne(),
        forest("btree1"), Fairy("lesser", -1), field(FieldStatus.growing));
    initialize(converted, PointFactory.point(2, 5), TileType.steppe, groundOne(),
        Fortification(TownStatus.burned, TownSize.medium, 0, "townName", -1, independent),
        field(FieldStatus.growing));
    initialize(converted, PointFactory.point(2, 6), TileType.steppe, groundOne(),
        forest("btree1"), SimpleImmortal(SimpleImmortalKind.djinn, -1), village("Danan"),
        TextFixture(oneToTwoConverter.maxIterationsWarning, 15),
        field(FieldStatus.growing));
    initialize(converted, PointFactory.point(2, 7), TileType.steppe, groundOne(),
        forest("btree1"), SimpleImmortal(SimpleImmortalKind.ogre, -1));
    initialize(converted, PointFactory.point(3, 0), TileType.notVisible, groundOne(),
        orchard());
    initialize(converted, PointFactory.point(3, 4), TileType.steppe, groundOne(),
        forest("btree1"), SimpleImmortal(SimpleImmortalKind.simurgh, -1),
        orchard());
    initialize(converted, PointFactory.point(3, 5), TileType.steppe, groundOne(),
        forest("btree1"), SimpleImmortal(SimpleImmortalKind.minotaur, -1),
        forest("ttree1"));
    initialize(converted, PointFactory.point(3, 6), TileType.steppe, groundOne(),
        forest("ttree1"),
        City(TownStatus.ruined, TownSize.large, 0, "cityName", -1, independent));
    initialize(converted, PointFactory.point(3, 7), TileType.steppe, groundOne(),
        forest("btree1"), Animal("animalKind", false, false, "wild", -1), orchard());
    initialize(converted, PointFactory.point(4, 0), TileType.plains, groundOne(),
        forest("ttree1"), village("dwarf"));
    initialize(converted, PointFactory.point(4, 6), TileType.tundra, groundTwo(),
        field(FieldStatus.growing, "grain4"));
    initialize(converted, PointFactory.point(4, 7), TileType.tundra, groundTwo(),
        field(FieldStatus.growing, "grain4"));
    initialize(converted, PointFactory.point(5, 1), TileType.plains, groundOne(),
        orchard());
    initialize(converted, PointFactory.point(6, 1), TileType.plains, groundOne(),
        forest("ttree1"));
    for (loc in {PointFactory.point(6, 5), PointFactory.point(6, 7),
        PointFactory.point(7, 5) }) {
        initialize(converted, loc, TileType.tundra, groundTwo(),
            field(FieldStatus.growing, "grain4"));
    }
    initialize(converted, PointFactory.point(7, 6), TileType.tundra, groundTwo(),
        village("human"));
    for (loc in { PointFactory.point(0, 0), PointFactory.point(0, 1),
        PointFactory.point(1, 1), PointFactory.point(1, 2), PointFactory.point(2, 0),
        PointFactory.point(2, 1), PointFactory.point(3, 1), PointFactory.point(3, 2),
        PointFactory.point(3, 3) }) {
        initialize(converted, loc, TileType.notVisible, groundOne());
    }
    for (loc in { PointFactory.point(0, 2), PointFactory.point(0, 3) }) {
        initialize(converted, loc, TileType.notVisible, groundOne(),
            field(FieldStatus.growing));
    }
    for (loc in { PointFactory.point(4, 1), PointFactory.point(4, 2),
        PointFactory.point(4, 3), PointFactory.point(5, 0), PointFactory.point(5, 2),
        PointFactory.point(5, 3), PointFactory.point(6, 0), PointFactory.point(6, 2),
        PointFactory.point(6, 3), PointFactory.point(7, 0), PointFactory.point(7, 1),
        PointFactory.point(7, 2), PointFactory.point(7, 3) }) {
        initialize(converted, loc, TileType.plains, groundOne());
        converted.setMountainous(loc, true);
    }
    for (loc in { PointFactory.point(4, 4), PointFactory.point(4, 5),
        PointFactory.point(5, 4), PointFactory.point(5, 5), PointFactory.point(5, 6),
        PointFactory.point(5, 7), PointFactory.point(6, 4), PointFactory.point(6, 6),
        PointFactory.point(7, 4), PointFactory.point(7, 7) }) {
        initialize(converted, loc, TileType.tundra, groundTwo());
    }
    converted.addRivers(PointFactory.point(2, 6), River.lake, River.south);
    converted.addRivers(PointFactory.point(3, 6), River.north, River.south);
    converted.setMountainous(PointFactory.point(4, 0), true);
    converted.addRivers(PointFactory.point(4, 6), River.north, River.south);
    converted.setMountainous(PointFactory.point(5, 1), true);
    converted.addRivers(PointFactory.point(5, 6), River.north, River.south);
    converted.addRivers(PointFactory.point(6, 0), River.east, River.west);
    converted.addRivers(PointFactory.point(6, 1), River.east, River.west);
    converted.setMountainous(PointFactory.point(6, 1), true);
    converted.addRivers(PointFactory.point(6, 2), River.east, River.west);
    converted.addRivers(PointFactory.point(6, 3), River.east, River.west);
    converted.addRivers(PointFactory.point(6, 4), River.east, River.west);
    converted.addRivers(PointFactory.point(6, 5), River.east, River.west);
    converted.addRivers(PointFactory.point(6, 6), River.north, River.west);
    try (outOne = StringWriter(), outTwo = StringWriter()) {
        SPWriter writer = newWriter;
        writer.write(outOne, converted);
        writer.write(outTwo, oneToTwoConverter.convert(original, true));
        Regex matcher = regex("id=\"[0-9]*\"", true);
        assertEquals(matcher.replace(outTwo.string, "id=\"-1\""), outOne.string,
            "Produces expected result");
    }
    try (outOne = StringWriter(), outTwo = StringWriter()) {
        SPWriter writer = oldWriter;
        writer.write(outOne, converted);
        writer.write(outTwo, oneToTwoConverter.convert(original, true));
        Regex matcher = regex("id=\"[0-9]*\"", true);
        assertEquals(matcher.replace(outTwo.string, "id=\"-1\""), outOne.string,
            "Deprecated I/O produces expected result");
    }
    try (outOne = Formatter(), outTwo = Formatter()) {
        assertEquals(
            converted.isSubset(oneToTwoConverter.convert(original, true), outTwo, ""),
            converted.isSubset(oneToTwoConverter.convert(original, true), outOne, ""),
            "Products of two runs are both or neither subsets of expected");
        assertEquals(outTwo.\iout().string, outOne.\iout().string,
            "Two runs produce identical results");
    }
    try (outStream = StringWriter(), errStream = Formatter()) {
        SPWriter writer = newWriter;
        writer.writeSPObject(outStream, oneToTwoConverter.convert(original, true));
        assertModuloID(converted, outStream.string, errStream);
    }
}

test
shared void testFourthOneToTwoConversion() {
    IMutableMapNG original = SPMapNG(MapDimensionsImpl(2, 2, 1), PlayerCollection(), 0);
    initialize(original, PointFactory.point(0, 0), TileType.ocean);
    for (point in { PointFactory.point(0, 1), PointFactory.point(1, 0),
        PointFactory.point(1, 1) }) {
        initialize(original, point, TileType.desert);
    }
    Player player = PlayerImpl(1, "playerName");
    original.addPlayer(player);
    Player independent = PlayerImpl(2, "independent");
    original.addPlayer(independent);

    IMutableMapNG converted = SPMapNG(MapDimensionsImpl(8, 8, 2), PlayerCollection(), -1);
    converted.addPlayer(player);
    converted.addPlayer(independent);
    IDRegistrar testFactory = IDFactory();
    TileFixture register(TileFixture fixture) {
        testFactory.register(fixture.id);
        return fixture;
    }
    initialize(converted, PointFactory.point(0, 0), TileType.ocean,
        register(Village(TownStatus.active, "", 16, independent, "human")));
    initialize(converted, PointFactory.point(3, 7), TileType.plains,
        register(Village(TownStatus.active, "", 33, independent, "half-elf")));
    initialize(converted, PointFactory.point(4, 0), TileType.plains,
        register(Village(TownStatus.active, "", 50, independent, "human")));
    for (i in 0..4) {
        for (j in 0..4) {
            initialize(converted, PointFactory.point(i, j), TileType.notVisible,
                Ground(testFactory.createID(), "rock1", false));
        }
    }
    for (i in 0..4) {
        for (j in 4..8) {
            initialize(converted, PointFactory.point(i, j), TileType.notVisible,
                Ground(testFactory.createID(), "rock1", false));
        }
    }
    for (i in 4..8) {
        for (j in 0..4) {
            initialize(converted, PointFactory.point(i, j), TileType.notVisible,
                Ground(testFactory.createID(), "rock1", false));
        }
    }
    for (i in 4..8) {
        for (j in 4..8) {
            initialize(converted, PointFactory.point(i, j), TileType.notVisible,
                Ground(testFactory.createID(), "rock4", false));
        }
    }
    initialize(converted, PointFactory.point(3, 6), TileType.desert,
        register(Meadow("grain1", true, true, 75, FieldStatus.growing)));
    initialize(converted, PointFactory.point(4, 1), TileType.desert,
        register(Meadow("grain1", true, true, 72, FieldStatus.growing)));
    initialize(converted, PointFactory.point(4, 6), TileType.desert,
        register(Grove(true, true, "fruit4", 78)));
    initialize(converted, PointFactory.point(5, 1), TileType.desert,
        register(Grove(true, true, "fruit1", 71)));
    initialize(converted, PointFactory.point(6, 5), TileType.desert,
        register(Grove(true, true, "fruit4", 73)));
    initialize(converted, PointFactory.point(6, 6), TileType.desert,
        register(Meadow("grain4", true, true, 76, FieldStatus.growing)));
    initialize(converted, PointFactory.point(6, 7), TileType.desert,
        register(Meadow("grain4", true, true, 77, FieldStatus.growing)));
    initialize(converted, PointFactory.point(7, 5), TileType.desert,
        register(Grove(true, true, "fruit4", 74)));
    initialize(converted, PointFactory.point(7, 6), TileType.desert,
        register(Village(TownStatus.active, "", 67, independent, "human")));
    initialize(converted, PointFactory.point(2, 6), TileType.desert,
        register(Grove(true, true, "fruit1", 68)));
    initialize(converted, PointFactory.point(2, 7), TileType.desert,
        register(Meadow("grain1", true, true, 69, FieldStatus.growing)));
    initialize(converted, PointFactory.point(4, 7), TileType.desert,
        register(Meadow("grain4", true, true, 70, FieldStatus.growing)));
    for (point in { PointFactory.point(0, 1), PointFactory.point(0, 2),
        PointFactory.point(0, 3), PointFactory.point(1, 0), PointFactory.point(1, 1),
        PointFactory.point(1, 2), PointFactory.point(1, 3), PointFactory.point(2, 0),
        PointFactory.point(2, 1), PointFactory.point(2, 2), PointFactory.point(2, 3),
        PointFactory.point(3, 0), PointFactory.point(3, 1), PointFactory.point(3, 2),
        PointFactory.point(3, 3) }) {
        initialize(converted, point, TileType.ocean);
    }
    for (point in { PointFactory.point(0, 4), PointFactory.point(0, 5),
        PointFactory.point(1, 4), PointFactory.point(3, 5), PointFactory.point(4, 2),
        PointFactory.point(4, 3), PointFactory.point(5, 0), PointFactory.point(5, 2),
        PointFactory.point(6, 0), PointFactory.point(6, 1), PointFactory.point(7, 0),
        PointFactory.point(7, 1), PointFactory.point(7, 3), PointFactory.point(4, 5),
        PointFactory.point(5, 5), PointFactory.point(5, 7) }) {
        initialize(converted, point, TileType.desert);
    }
    for (point in { PointFactory.point(0, 6), PointFactory.point(0, 7),
        PointFactory.point(1, 5), PointFactory.point(1, 6), PointFactory.point(1, 7),
        PointFactory.point(2, 4), PointFactory.point(2, 5), PointFactory.point(3, 4),
        PointFactory.point(5, 3), PointFactory.point(6, 2), PointFactory.point(6, 3),
        PointFactory.point(7, 2), PointFactory.point(4, 4), PointFactory.point(5, 4),
        PointFactory.point(5, 6), PointFactory.point(6, 4), PointFactory.point(7, 4),
        PointFactory.point(7, 7) }) {
        initialize(converted, point, TileType.plains);
    }

    try (outOne = StringWriter(), outTwo = StringWriter()) {
        SPWriter writer = newWriter;
        writer.write(outOne, converted);
        writer.write(outTwo, oneToTwoConverter.convert(original, true));
        assertEquals(outTwo.string, outOne.string, "Produces expected result");
    }
    try (outOne = StringWriter(), outTwo = StringWriter()) {
        SPWriter writer = oldWriter;
        writer.write(outOne, converted);
        writer.write(outTwo, oneToTwoConverter.convert(original, true));
        assertEquals(outTwo.string, outOne.string, "Deprecated I/O produces expected result");
    }
    try (outOne = Formatter(), outTwo = Formatter()) {
        assertEquals(
            converted.isSubset(oneToTwoConverter.convert(original, true), outTwo, ""),
            converted.isSubset(oneToTwoConverter.convert(original, true), outOne, ""),
            "Products of two runs are both or neither subsets of expected");
        assertEquals(outTwo.\iout().string, outOne.\iout().string,
            "Two runs produce identical results");
    }
    try (outStream = Formatter()) {
        assertTrue(
            converted.isSubset(oneToTwoConverter.convert(original, true), outStream, ""),
            "Actual is at least subset of expected converted");
    }
}
object zeroToOneConverter {
    MutableMap<Integer, String> equivalents = HashMap<Integer, String>();
    void addXML(String xml, Integer* numbers) {
        for (number in numbers) {
            equivalents.put(number, xml);
        }
    }
    addXML("""<mineral kind="iron" exposed="true" dc="0" />""", 200, 206);
    addXML("""<mineral kind="iron" exposed="false" dc="0" />""", 201, 202, 207, 208);
    addXML("""<mineral kind="copper" exposed="true" dc="0" />""", 203, 209);
    addXML("""<mineral kind="copper" exposed="false" dc="0" />""", 204, 205, 210, 211);
    addXML("""<mineral kind="gold" exposed="true" dc="0" />""", 212);
    addXML("""<mineral kind="gold" exposed="true" dc="0" />""", 213);
    addXML("""<mineral kind="silver" exposed="false" dc="0" />""", 214);
    addXML("""<mineral kind="silver" exposed="false" dc="0" />""", 215);
    addXML("""<mineral kind="coal" exposed="true" dc="0" />""", 216, 219);
    addXML("""<mineral kind="coal" exposed="false" dc="0" />""", 217, 218, 220, 221);
    addXML("""<town status="active" size="small" dc="0" />""", 222);
    addXML("""<town status="abandoned" size="small" dc="0" />""", 223, 227, 231);
    addXML("""<fortification status="abandoned" size="small" dc="0" />""", 224, 228, 232);
    addXML("""<town status="burned" size="small" dc="0" />""", 225, 229, 233);
    addXML("""<fortification status="burned" size="small" dc="0" />""", 226, 230, 234);
    addXML("""<battlefield dc="0" />""", *(235..241));
    addXML("""<city status="ruined" size="medium" dc="0" />""", 241, 243);
    addXML("""<fortification status="ruined" size="medium" dc="0" />""", 242, 244);
    addXML("""<city status="ruined" size="large" dc="0" />""", 245);
    addXML("""<fortification status="ruined" size="large" dc="0" />""", 246);
    addXML("""<stone kind="limestone" dc="0" />""", 247, 248, 249);
    addXML("""<stone kind="marble" dc="0" />""", 250, 251, 252);
    addXML("""<cave dc="0" />""", 253, 254, 255);
    Boolean isSpecifiedTag(QName tag, String desired) {
        return tag == QName(ISPReader.namespace, desired) || tag == QName(desired);
    }
    void printAttribute(Appendable ostream, Attribute attribute) {
        // TODO: namespace
        ostream.append(" ``attribute.name.localPart``=``attribute.\ivalue``");
    }
    "Convert the version attribute of the map"
    void convertMap(Appendable ostream, StartElement element, {Attribute*} attributes) {
        ostream.append('<');
        if (XMLConstants.defaultNsPrefix != element.name.namespaceURI) {
            ostream.append("``element.name.prefix``:");
        }
        ostream.append(element.name.localPart);
        for (namespace in ConvertingIterable(element.namespaces)) {
            ostream.append(" ``namespace``");
        }
        for (attribute in attributes) {
            if ("version" == attribute.name.localPart.lowercased) {
                ostream.append(" version=\"1\"");
            } else {
                printAttribute(ostream, attribute);
            }
        }
        ostream.append('>');
    }
    void printEvent(Appendable ostream, Integer number) {
        if (exists val = equivalents.get(number)) {
            ostream.append(val);
        }
    }
    void printEndElement(Appendable ostream, EndElement element) {
        if (XMLConstants.defaultNsPrefix == element.name.namespaceURI) {
            ostream.append("</``element.name.localPart``>");
        } else {
            ostream.append("</``element.name.prefix``:``element.name.localPart``>");
        }
    }
    void printStartElement(Appendable ostream, StartElement element) {
        ostream.append('<');
        if (XMLConstants.defaultNsPrefix != element.name.namespaceURI) {
            ostream.append("``element.name.prefix``:");
        }
        ostream.append(element.name.localPart);
        for (attribute in ConvertingIterable(element.attributes)) {
            printAttribute(ostream, attribute);
        }
        ostream.append('>');
    }
    void convertTile(Appendable ostream, StartElement element, {Attribute*} attributes) {
        ostream.append('<');
        if (XMLConstants.defaultNsPrefix != element.name.namespaceURI) {
            ostream.append("``element.name.prefix``:");
        }
        ostream.append(element.name.localPart);
        Queue<Integer> events = LinkedList<Integer>();
        for (attribute in attributes) {
            if ("event" == attribute.name.localPart.lowercased) {
                value number = Integer.parse(attribute.\ivalue);
                if (is Integer number) {
                    events.offer(number);
                } else {
                    log.error("Non-numeric 'event' in line ``element.location
                        .lineNumber``",
                        number);
                }
            } else {
                printAttribute(ostream, attribute);
            }
        }
        ostream.append('>');
        while (exists event = events.accept()) {
            ostream.append(LineEnd.lineSep);
            printEvent(ostream, event);
        }
    }
    "Read version-0 XML from the input stream and write version-1 equivalent XML to the
     output stream."
    todo("Convert to ceylon.io and/or ceylon.file, at least for output")
    shared void convert({XMLEvent*} stream, Appendable ostream) {
        for (event in stream) {
            if (is StartElement event) {
                if (isSpecifiedTag(event.name, "tile")) {
                    convertTile(ostream, event, ConvertingIterable(event.attributes));
                } else if (isSpecifiedTag(event.name, "map")) {
                    convertMap(ostream, event, ConvertingIterable(event.attributes));
                } else {
                    printStartElement(ostream, event);
                }
            } else if (is Characters event) {
                ostream.append(event.data.trimmed);
            } else if (is EndElement event) {
                printEndElement(ostream, event);
            } else if (is StartDocument event) {
                ostream.append("""<?xml version="1.0"?>""");
                ostream.append(LineEnd.lineSep);
            } else if (is EndDocument event) {
                break;
            } else {
                log.warn("Unhandled element type ``event.eventType``");
            }
        }
        ostream.append(LineEnd.lineSep);
    }
}
test
void testZeroToOneConversion() {
    // FIXME: Include tile fixtures beyond those implicit in events
    String orig = "<map xmlns:sp=\"``ISPReader.namespace``\" version='0' rows='2'
                   columns='2'><player number='0' code_name='Test Player' />
                   <row index='0'><tile row='0' column='0' type='tundra' event='0'>
                   Random event here</tile><tile row='0' column='1' type='boreal_forest'
                   event='183'></tile></row><row index='1'><sp:tile row='1' column='0'
                   type='mountain' event='229'><sp:fortress name='HQ' owner='0' id='15'
                   /></sp:tile><tile row='1'column='1' type='temperate_forest'
                   event='219'></tile></row></map>";
    StringWriter ostream = StringWriter();
    zeroToOneConverter.convert(CeylonIterable(IteratorWrapper(TypesafeXMLEventReader(
            XMLInputFactory.newInstance().createXMLEventReader(StringReader(orig))))),
        ostream);
    StringWriter actualXML = StringWriter();
    SPWriter writer = oldWriter;
    writer.writeSPObject(actualXML,
        readMap(StringReader(ostream.string), Warning.ignore));
    IMutableMapNG expected = SPMapNG(MapDimensionsImpl(2, 2, 1), PlayerCollection(), 0);
    Player player = PlayerImpl(0, "Test Player");
    expected.addPlayer(player);
    initialize(expected, PointFactory.point(0, 0), TileType.tundra,
        TextFixture("Random event here", -1));
    initialize(expected, PointFactory.point(0, 1), TileType.borealForest);
    initialize(expected, PointFactory.point(1, 0), TileType.mountain,
        Town(TownStatus.burned, TownSize.small, 0, "", 0, PlayerImpl(-1, "Independent")),
        Fortress(player, "HQ", 15, TownSize.small));
    initialize(expected, PointFactory.point(1, 1), TileType.temperateForest,
        MineralVein("coal", true, 0, 1));
    StringWriter expectedXML = StringWriter();
    writer.writeSPObject(expectedXML, expected);
    assertEquals(actualXML.string, expectedXML.string,
        "Converted map's serialized form was as expected");
    assertEquals(readMap(StringReader(ostream.string),
        Warning.ignore), expected, "Converted map was as expected");
}
"Convert files provided on command line; prints results to standard output."
todo("Write results to file")
shared void convertZeroToOne() {
    for (argument in process.arguments) {
        try (reader = JFileReader(argument)) {
            zeroToOneConverter.convert(ConvertingIterable<XMLEvent>(
                    XMLInputFactory.newInstance().createXMLEventReader(reader)),
                SystemOut.sysOut);
        } catch (FileNotFoundException|NoSuchFileException except) {
            log.error("File ``argument`` not found", except);
        } catch (XMLStreamException except) {
            log.error("Malformed XML in ``argument``", except);
        } catch (IOException except) {
            log.error("I/O error dealing with file ``argument``", except);
        }
    }
}
"A utility to convert a map to an equivalent half-resolution one."
IMapNG decreaseResolution(IMapNG old) {
    if (old.dimensions.rows % 2 != 0 || old.dimensions.columns %2 != 0) {
        throw IllegalArgumentException(
            "Can only convert maps with even numbers of rows and columns");
    }
    PlayerCollection players = PlayerCollection();
    for (player in old.players) {
        players.add(player);
    }
    Integer newColumns = old.dimensions.columns / 2;
    Integer newRows = old.dimensions.rows / 2;
    IMutableMapNG retval = SPMapNG(MapDimensionsImpl(newRows, newColumns, 2), players,
        old.currentTurn);
    TileType consensus([TileType+] types) {
        assert (is TileType[4] types); // the algorithm assumes only possible splits are
        // 4-0, 3-1, 2-2, 2-1-1, and 1-1-1-1
        // TODO: more Ceylonic algorithm/implementation
        EnumCounter<TileType> counter = EnumCounter<TileType>();
        counter.countMany(*types);
        MutableSet<TileType> twos = HashSet<TileType>();
        for (type in TileType.values()) {
            switch (counter.getCount(type))
            case (0|1) { }
            case (2) { twos.add(type); }
            else { return type; }
        }
        if (twos.size == 1) {
            assert (exists type = twos.first);
            return type;
        } else {
            assert (exists type = shuffle(types).first);
            return type;
        }
    }
    for (row in 0..newRows) {
        for (column in 0..newColumns) {
            Point point = PointFactory.point(row, column);
            Point[4] subPoints = [PointFactory.point(row * 2, column * 2),
                PointFactory.point(row * 2, (column * 2) + 1),
                PointFactory.point((row * 2) + 1, column * 2),
                PointFactory.point((row * 2) + 1, (column * 2) + 1) ];
            retval.setBaseTerrain(point,
                consensus([ *subPoints.map(old.getBaseTerrain) ]));
            for (oldPoint in subPoints) {
                if (old.isMountainous(oldPoint)) {
                    retval.setMountainous(point, true);
                }
                if (exists ground = old.getGround(oldPoint)) {
                    if (retval.getGround(point) exists) {
                        retval.addFixture(point, ground);
                    } else {
                        retval.setGround(point, ground);
                    }
                }
                if (exists forest = old.getForest(oldPoint)) {
                    if (retval.getForest(point) exists) {
                        retval.addFixture(point, forest);
                    } else {
                        retval.setForest(point, forest);
                    }
                }
                for (fixture in old.getOtherFixtures(oldPoint)) {
                    retval.addFixture(point, fixture);
                }
            }
            MutableSet<River> upperLeftRivers =HashSet<River> {
                *old.getRivers(subPoints[0]) };
            MutableSet<River> upperRightRivers = HashSet<River> {
                *old.getRivers(subPoints[1]) };
            MutableSet<River> lowerLeftRivers =HashSet<River> {
                *old.getRivers(subPoints[2]) };
            MutableSet<River> lowerRightRivers = HashSet<River> {
                *old.getRivers(subPoints[3]) };
            upperLeftRivers.removeAll({River.east, River.south});
            upperRightRivers.removeAll({River.west, River.south});
            lowerLeftRivers.removeAll({River.east, River.north});
            lowerRightRivers.removeAll({River.west, River.north});
            retval.addRivers(point, *({ upperLeftRivers, upperRightRivers, lowerLeftRivers,
                lowerRightRivers}.reduce((Set<River> partial, Set<River> element) =>
            partial.union(element))));
        }
    }
    return retval;
}
test
void testResolutionReduction() {
    IMutableMapNG start = SPMapNG(MapDimensionsImpl(2, 2, 2), PlayerCollection(), 0);
    Animal fixture = Animal("animal", false, true, "domesticated", 1);
    initialize(start, PointFactory.point(0, 0), TileType.desert, fixture);
    CacheFixture fixtureTwo = CacheFixture("gemstones", "small", 2);
    initialize(start, PointFactory.point(0, 1), TileType.desert, fixtureTwo);
    IUnit fixtureThree = Unit(PlayerImpl(0, "A. Player"), "legion", "eagles", 3);
    initialize(start, PointFactory.point(1, 0), TileType.desert, fixtureThree);
    Fortress fixtureFour = Fortress(PlayerImpl(1, "B. Player"), "HQ", 4, TownSize.small);
    initialize(start, PointFactory.point(1, 1), TileType.plains, fixtureFour);

    IMapNG converted = decreaseResolution(start);
    Point zeroPoint = PointFactory.point(0, 0);
    assertTrue(converted.getOtherFixtures(zeroPoint)
        .containsEvery({fixture, fixtureTwo, fixtureThree, fixtureFour}),
        "Combined tile should contain fixtures from all four original tiles");
    assertEquals(converted.getBaseTerrain(zeroPoint), TileType.desert,
        "Combined tile has type of most of input tiles");
}

test
void testMoreReduction() {
    IMutableMapNG start = SPMapNG(MapDimensionsImpl(2, 2, 2), PlayerCollection(), 0);
    Point pointOne = PointFactory.point(0, 0);
    start.setMountainous(pointOne, true);
    start.addRivers(pointOne, River.east, River.south);
    Ground groundOne = Ground(-1, "groundOne", false);
    initialize(start, pointOne, TileType.steppe, groundOne);
    Point pointTwo = PointFactory.point(0, 1);
    start.addRivers(pointTwo, River.lake);
    Ground groundTwo = Ground(-1, "groundTwo", false);
    initialize(start, pointTwo, TileType.steppe, groundTwo);
    Point pointThree = PointFactory.point(1, 0);
    Forest forestOne = Forest("forestOne", false, 1);
    initialize(start, pointThree, TileType.plains, forestOne);
    Point pointFour = PointFactory.point(1, 1);
    Forest forestTwo = Forest("forestTwo", false, 2);
    initialize(start, pointFour, TileType.desert, forestTwo);

    IMapNG converted = decreaseResolution(start);
    Point zeroPoint = PointFactory.point(0, 0);
    assertTrue(converted.isMountainous(zeroPoint),
        "One mountainous point makes the reduced point mountainous");
    assertEquals(converted.getGround(zeroPoint), groundOne, "Ground carries over");
    assertEquals(converted.getForest(zeroPoint), forestOne, "Forest carries over");
    assertTrue(converted.getOtherFixtures(zeroPoint)
        .containsEvery({groundTwo, forestTwo}),
        "Ground and forest carry over even when already set");
    assertTrue(converted.getRivers(zeroPoint)
        .containsEvery({River.lake, River.east, River.south}),
        "Non-interior rivers carry over");
    // TODO: ensure that the original included some interior rivers
    assertTrue(!converted.getRivers(zeroPoint).containsAny({River.north, River.west}),
        "Interior rivers do not carry over");
    assertEquals(converted.getBaseTerrain(zeroPoint), TileType.steppe,
        "Combined tile has most common terrain type among inputs");
}

test
void testResolutionDecreaseRequirement() {
    assertThatException(
                () => decreaseResolution(SPMapNG(MapDimensionsImpl(3, 3, 2),
            PlayerCollection(), -1)))
        .hasType(`IllegalArgumentException`);
}