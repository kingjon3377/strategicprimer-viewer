import ceylon.regex {
    Regex,
    regex
}
import ceylon.test {
    test,
    assertEquals,
    assertTrue
}

import java.io {
    StringWriter,
    StringReader
}
import java.util {
    Formatter
}

import model.map {
    Player,
    MapDimensionsImpl,
    Point,
    PlayerImpl
}

import strategicprimer.viewer.model {
    IDFactory,
    IDRegistrar
}
import strategicprimer.viewer.model.map {
    River,
    PlayerCollection,
    TileFixture,
    TileType,
    SPMapNG,
    IMutableMapNG,
    IMapNG,
    pointFactory
}
import strategicprimer.viewer.model.map.fixtures {
    TextFixture,
    Ground
}
import strategicprimer.viewer.model.map.fixtures.explorable {
    AdventureFixture
}
import strategicprimer.viewer.model.map.fixtures.mobile {
    Animal,
    Dragon,
    Centaur,
    Fairy,
    Giant,
    SimpleImmortalKind,
    SimpleImmortal
}
import strategicprimer.viewer.model.map.fixtures.resources {
    FieldStatus,
    Meadow,
    Mine,
    StoneKind,
    StoneDeposit,
    Grove
}
import strategicprimer.viewer.model.map.fixtures.terrain {
    Hill,
    Forest
}
import strategicprimer.viewer.model.map.fixtures.towns {
    TownStatus,
    TownSize,
    Village,
    City,
    Fortification
}
import strategicprimer.viewer.xmlio {
    ISPReader,
    readMap,
    testReaderFactory,
    SPWriter,
    warningLevels
}

void assertModuloID(IMapNG map, String serialized, Anything(String) err) {
    Regex matcher = regex("id=\"[0-9]*\"", true);
    try (inStream = StringReader(matcher.replace(serialized, "id=\"-1\""))) {
        assertTrue(
            map.isSubset(readMap(inStream, warningLevels.ignore), err),
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
    original.setBaseTerrain(pointFactory(0, 0), TileType.borealForest);
    original.setBaseTerrain(pointFactory(0, 1), TileType.temperateForest);
    original.setBaseTerrain(pointFactory(1, 0), TileType.desert);
    original.setBaseTerrain(pointFactory(1, 1), TileType.plains);
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
    initialize(converted, pointFactory(0, 0), TileType.steppe, groundOne(),
        village("human"));
    initialize(converted, pointFactory(0, 1), TileType.steppe, groundOne(),
        forest("btree1"), field(FieldStatus.growing));
    initialize(converted, pointFactory(1, 0), TileType.steppe, groundOne(),
        forest("btree1"), orchard());
    initialize(converted, pointFactory(1, 1), TileType.steppe, groundOne(),
        forest("btree1"), field(FieldStatus.growing));
    initialize(converted, pointFactory(2, 1), TileType.steppe, groundOne(),
        forest("btree1"), forest("ttree1"));
    initialize(converted, pointFactory(2, 6), TileType.plains, groundOne(),
        forest("ttree1"), orchard());
    initialize(converted, pointFactory(3, 0), TileType.steppe, groundOne(),
        forest("btree1"), field(FieldStatus.growing));
    initialize(converted, pointFactory(3, 6), TileType.plains, groundOne(),
        forest("ttree1"), orchard());
    initialize(converted, pointFactory(3, 7), TileType.plains, groundOne(),
        village("dwarf"));
    initialize(converted, pointFactory(4, 0), TileType.desert, groundOne(),
        village("human"));
    initialize(converted, pointFactory(4, 1), TileType.desert, groundOne(),
        orchard());
    initialize(converted, pointFactory(4, 6), TileType.plains, groundTwo(),
        orchard("fruit4"));
    initialize(converted, pointFactory(4, 7), TileType.plains, groundTwo(),
        field(FieldStatus.growing, "grain4"));
    initialize(converted, pointFactory(5, 0), TileType.desert, groundOne(),
        field(FieldStatus.growing));
    initialize(converted, pointFactory(6, 5), TileType.plains, groundTwo(),
        orchard("fruit4"));
    initialize(converted, pointFactory(6, 6), TileType.plains, groundTwo(),
        field(FieldStatus.growing, "grain4"));
    initialize(converted, pointFactory(6, 7), TileType.plains, groundTwo(),
        field(FieldStatus.growing, "grain4"));
    initialize(converted, pointFactory(7, 6), TileType.plains, groundTwo(),
        village("human"));
    initialize(converted, pointFactory(5, 7), TileType.plains, groundTwo(),
        forest("ttree4"));
    initialize(converted, pointFactory(7, 7), TileType.plains, groundTwo(),
        orchard("fruit4"));
    for (loc in { pointFactory(0, 2), pointFactory(0, 3),
        pointFactory(1, 2), pointFactory(1, 3),
        pointFactory(2, 0), pointFactory(2, 2),
        pointFactory(2, 3), pointFactory(3, 1),
        pointFactory(3, 2), pointFactory(3, 3) }) {
        initialize(converted, loc, TileType.steppe, groundOne(), forest("btree1"));
    }
    for (loc in { pointFactory(0, 4), pointFactory(0, 5),
        pointFactory(0, 6), pointFactory(0, 7),
        pointFactory(1, 4), pointFactory(1, 5),
        pointFactory(1, 6), pointFactory(1, 7),
        pointFactory(2, 4), pointFactory(2, 5),
        pointFactory(2, 7), pointFactory(3, 4),
        pointFactory(3, 5) }) {
        initialize(converted, loc, TileType.plains, groundOne(), forest("ttree1"));
    }
    for (loc in { pointFactory(4, 2), pointFactory(5, 1),
        pointFactory(5, 2), pointFactory(6, 0),
        pointFactory(6, 1), pointFactory(6, 3),
        pointFactory(7, 0), pointFactory(7, 2) }) {
        initialize(converted, loc, TileType.desert, groundOne());
    }
    for (loc in { pointFactory(4, 3), pointFactory(5, 3),
        pointFactory(6, 2), pointFactory(7, 1),
        pointFactory(7, 3) }) {
        initialize(converted, loc, TileType.plains, groundOne());
    }
    for (loc in { pointFactory(4, 4), pointFactory(4, 5),
        pointFactory(5, 4), pointFactory(5, 5),
        pointFactory(5, 6), pointFactory(6, 4),
        pointFactory(7, 4), pointFactory(7, 5) }) {
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
    StringBuilder outOne = StringBuilder();
    StringBuilder outTwo = StringBuilder();
    assertEquals(
        converted.isSubset(oneToTwoConverter.convert(original, true), (String str) => writeLine(outTwo, str)),
        converted.isSubset(oneToTwoConverter.convert(original, true), (String str) => writeLine(outOne, str)),
        "Products of two runs are both or neither subsets of expected");
    assertEquals(outTwo.string, outOne.string,
        "Two runs produce identical results");
    try (outStream = StringWriter(), errStream = Formatter()) {
        SPWriter writer = newWriter;
        writer.writeSPObject(outStream, oneToTwoConverter.convert(original, true));
        assertModuloID(converted, outStream.string, noop);
    }
}
test
shared void testMoreOneToTwoConversion() {
    IMutableMapNG original = SPMapNG(MapDimensionsImpl(2, 2, 1), PlayerCollection(), 0);
    initialize(original, pointFactory(0, 0), TileType.jungle);
    initialize(original, pointFactory(0, 1), TileType.temperateForest,
        Forest("ttree1", false, 1));
    initialize(original, pointFactory(1, 0), TileType.mountain);
    initialize(original, pointFactory(1, 1), TileType.tundra,
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
    initialize(converted, pointFactory(0, 0), TileType.jungle, groundOne(),
        village("human"));
    initialize(converted, pointFactory(0, 1), TileType.jungle, groundOne(),
        field(FieldStatus.growing));
    initialize(converted, pointFactory(2, 3), TileType.jungle, groundOne(),
        field(FieldStatus.growing));
    initialize(converted, pointFactory(2, 5), TileType.plains, groundOne(),
        forest("ttree1"), orchard());
    initialize(converted, pointFactory(3, 0), TileType.jungle, groundOne(),
        field(FieldStatus.growing));
    initialize(converted, pointFactory(3, 1), TileType.jungle, groundOne(),
        field(FieldStatus.growing));
    initialize(converted, pointFactory(3, 4), TileType.plains, groundOne(),
        village("human"));
    initialize(converted, pointFactory(3, 5), TileType.plains, groundOne(),
        forest("ttree1"), field(FieldStatus.growing));
    initialize(converted, pointFactory(4, 0), TileType.plains, groundOne(),
        village("Danan"));
    initialize(converted, pointFactory(4, 3), TileType.plains, groundOne(),
        orchard());
    initialize(converted, pointFactory(4, 4), TileType.tundra, groundOne(),
        field(FieldStatus.growing, "grain4"));
    initialize(converted, pointFactory(5, 0), TileType.plains, groundOne(),
        orchard());
    initialize(converted, pointFactory(5, 1), TileType.plains, groundOne(),
        field(FieldStatus.growing));
    initialize(converted, pointFactory(5, 4), TileType.tundra, groundTwo(),
        forest("ttree4"));
    initialize(converted, pointFactory(5, 6), TileType.tundra, groundTwo(),
        groundOne());
    initialize(converted, pointFactory(6, 5), TileType.tundra, groundTwo(),
        field(FieldStatus.growing, "grain4"));
    initialize(converted, pointFactory(6, 6), TileType.tundra, groundTwo(),
        orchard("fruit4"));
    initialize(converted, pointFactory(7, 5), TileType.tundra, groundTwo(),
        field(FieldStatus.growing, "grain4"));
    initialize(converted, pointFactory(7, 6), TileType.tundra, groundTwo(),
        forest("ttree4"), village("dwarf"));
    for (loc in { pointFactory(0, 2), pointFactory(0, 3),
        pointFactory(1, 0), pointFactory(1, 1), pointFactory(1, 2),
        pointFactory(1, 3), pointFactory(2, 0), pointFactory(2, 1),
        pointFactory(2, 2), pointFactory(3, 2),
        pointFactory(3, 3) }) {
        initialize(converted, loc, TileType.jungle, groundOne());
    }
    for (loc in { pointFactory(0, 4), pointFactory(0, 5),
        pointFactory(0, 6), pointFactory(0, 7), pointFactory(1, 4),
        pointFactory(1, 5), pointFactory(1, 6), pointFactory(1, 7),
        pointFactory(2, 4), pointFactory(2, 6), pointFactory(2, 7),
        pointFactory(3, 6), pointFactory(3, 7) }) {
        initialize(converted, loc, TileType.plains, groundOne(), forest("ttree1"));
    }
    for (loc in { pointFactory(4, 1), pointFactory(5, 2),
        pointFactory(7, 1) }) {
        initialize(converted, loc, TileType.plains, groundOne(), forest("ttree1"));
        converted.setMountainous(loc, true);
    }
    for (loc in { pointFactory(4, 2), pointFactory(5, 3),
        pointFactory(6, 0), pointFactory(6, 1), pointFactory(6, 2),
        pointFactory(6, 3), pointFactory(7, 0), pointFactory(7, 2),
        pointFactory(7, 3) }) {
        initialize(converted, loc, TileType.plains, groundOne());
        converted.setMountainous(loc, true);
    }
    for (loc in { pointFactory(4, 5), pointFactory(4, 6),
        pointFactory(4, 7), pointFactory(5, 5), pointFactory(5, 7),
        pointFactory(6, 4), pointFactory(6, 7), pointFactory(7, 4),
        pointFactory(7, 7) }) {
        initialize(converted, loc, TileType.tundra, groundTwo());
    }
    // The converter adds a second forest here, but trying to do the same gets it
    // filtered out because it would have the same ID. So we create it, *then* reset
    // its ID.
    Forest tempForest = Forest("ttree1", false, 0);
    converted.addFixture(pointFactory(3, 7), tempForest);
    tempForest.id = -1;
    for (loc in { pointFactory(4, 0), pointFactory(4, 3),
        pointFactory(5, 0), pointFactory(5, 1) }) {
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
    StringBuilder outOne = StringBuilder();
    StringBuilder outTwo = StringBuilder();
    assertEquals(
        converted.isSubset(oneToTwoConverter.convert(original, true), (String str) => writeLine(outTwo, str)),
        converted.isSubset(oneToTwoConverter.convert(original, true), (String str) => writeLine(outOne, str)),
        "Products of two runs are both or neither subsets of expected");
    assertEquals(outTwo.string, outOne.string,
        "Two runs produce identical results");
    try (outStream = StringWriter(), errStream = Formatter()) {
        SPWriter writer = newWriter;
        writer.writeSPObject(outStream, oneToTwoConverter.convert(original, true));
        assertModuloID(converted, outStream.string, noop);
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
    initialize(original, pointFactory(0, 0), TileType.notVisible, groundOne());
    original.addPlayer(independent);
    initialize(original, pointFactory(0, 1), TileType.borealForest,
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
    initialize(original, pointFactory(1, 0), TileType.mountain);
    initialize(original, pointFactory(1, 1), TileType.tundra);
    original.addRivers(pointFactory(0, 1), River.lake, River.south);
    original.addRivers(pointFactory(1, 0), River.east, River.west);
    original.addRivers(pointFactory(1, 1), River.west, River.north);
    Player player = PlayerImpl(1, "playerName");
    original.addPlayer(player);

    IMutableMapNG converted = SPMapNG(MapDimensionsImpl(8, 8, 2), PlayerCollection(), -1);
    converted.addPlayer(player);
    converted.addPlayer(independent);
    initialize(converted, pointFactory(0, 4), TileType.steppe, groundOne(),
        forest("btree1"), Hill(-1), Centaur("hill", -1),
        field(FieldStatus.growing, "grain1"));
    initialize(converted, pointFactory(0, 5), TileType.steppe, groundOne(),
        forest("btree1"), SimpleImmortal(SimpleImmortalKind.troll, -1),
        forest("ttree1"));
    initialize(converted, pointFactory(0, 6), TileType.steppe, groundOne(),
        forest("btree1"), AdventureFixture(independent, "briefDescription",
            "fullDescription", -1));
    initialize(converted, pointFactory(0, 7), TileType.steppe, groundOne(),
        forest("btree1"), SimpleImmortal(SimpleImmortalKind.griffin, -1),
        forest("ttree1"));
    initialize(converted, pointFactory(1, 0), TileType.notVisible, groundOne(),
        forest("ttree1"));
    initialize(converted, pointFactory(1, 3), TileType.notVisible, groundOne(),
        forest("ttree1"), village("half-elf"));
    initialize(converted, pointFactory(1, 4), TileType.steppe, groundOne(),
        forest("btree1"), Mine("mineral", TownStatus.active, -1),
        field(FieldStatus.growing));
    initialize(converted, pointFactory(1, 5), TileType.steppe, groundOne(),
        forest("btree1"), Giant("frost", -1), forest("ttree1"));
    initialize(converted, pointFactory(1, 6), TileType.steppe, groundOne(),
        forest("btree1"), StoneDeposit(StoneKind.conglomerate, 0, -1));
    initialize(converted, pointFactory(1, 7), TileType.steppe, groundOne(),
        forest("btree1"), Dragon("ice", -1), field(FieldStatus.growing));
    initialize(converted, pointFactory(2, 2), TileType.notVisible, groundOne(),
        orchard());
    initialize(converted, pointFactory(2, 3), TileType.notVisible, groundOne(),
        field(FieldStatus.growing));
    initialize(converted, pointFactory(2, 4), TileType.steppe, groundOne(),
        forest("btree1"), Fairy("lesser", -1), field(FieldStatus.growing));
    initialize(converted, pointFactory(2, 5), TileType.steppe, groundOne(),
        Fortification(TownStatus.burned, TownSize.medium, 0, "townName", -1, independent),
        field(FieldStatus.growing));
    initialize(converted, pointFactory(2, 6), TileType.steppe, groundOne(),
        forest("btree1"), SimpleImmortal(SimpleImmortalKind.djinn, -1), village("Danan"),
        TextFixture(oneToTwoConverter.maxIterationsWarning, 15),
        field(FieldStatus.growing));
    initialize(converted, pointFactory(2, 7), TileType.steppe, groundOne(),
        forest("btree1"), SimpleImmortal(SimpleImmortalKind.ogre, -1));
    initialize(converted, pointFactory(3, 0), TileType.notVisible, groundOne(),
        orchard());
    initialize(converted, pointFactory(3, 4), TileType.steppe, groundOne(),
        forest("btree1"), SimpleImmortal(SimpleImmortalKind.simurgh, -1),
        orchard());
    initialize(converted, pointFactory(3, 5), TileType.steppe, groundOne(),
        forest("btree1"), SimpleImmortal(SimpleImmortalKind.minotaur, -1),
        forest("ttree1"));
    initialize(converted, pointFactory(3, 6), TileType.steppe, groundOne(),
        forest("ttree1"),
        City(TownStatus.ruined, TownSize.large, 0, "cityName", -1, independent));
    initialize(converted, pointFactory(3, 7), TileType.steppe, groundOne(),
        forest("btree1"), Animal("animalKind", false, false, "wild", -1), orchard());
    initialize(converted, pointFactory(4, 0), TileType.plains, groundOne(),
        forest("ttree1"), village("dwarf"));
    initialize(converted, pointFactory(4, 6), TileType.tundra, groundTwo(),
        field(FieldStatus.growing, "grain4"));
    initialize(converted, pointFactory(4, 7), TileType.tundra, groundTwo(),
        field(FieldStatus.growing, "grain4"));
    initialize(converted, pointFactory(5, 1), TileType.plains, groundOne(),
        orchard());
    initialize(converted, pointFactory(6, 1), TileType.plains, groundOne(),
        forest("ttree1"));
    for (loc in {pointFactory(6, 5), pointFactory(6, 7),
        pointFactory(7, 5) }) {
        initialize(converted, loc, TileType.tundra, groundTwo(),
            field(FieldStatus.growing, "grain4"));
    }
    initialize(converted, pointFactory(7, 6), TileType.tundra, groundTwo(),
        village("human"));
    for (loc in { pointFactory(0, 0), pointFactory(0, 1),
        pointFactory(1, 1), pointFactory(1, 2), pointFactory(2, 0),
        pointFactory(2, 1), pointFactory(3, 1), pointFactory(3, 2),
        pointFactory(3, 3) }) {
        initialize(converted, loc, TileType.notVisible, groundOne());
    }
    for (loc in { pointFactory(0, 2), pointFactory(0, 3) }) {
        initialize(converted, loc, TileType.notVisible, groundOne(),
            field(FieldStatus.growing));
    }
    for (loc in { pointFactory(4, 1), pointFactory(4, 2),
        pointFactory(4, 3), pointFactory(5, 0), pointFactory(5, 2),
        pointFactory(5, 3), pointFactory(6, 0), pointFactory(6, 2),
        pointFactory(6, 3), pointFactory(7, 0), pointFactory(7, 1),
        pointFactory(7, 2), pointFactory(7, 3) }) {
        initialize(converted, loc, TileType.plains, groundOne());
        converted.setMountainous(loc, true);
    }
    for (loc in { pointFactory(4, 4), pointFactory(4, 5),
        pointFactory(5, 4), pointFactory(5, 5), pointFactory(5, 6),
        pointFactory(5, 7), pointFactory(6, 4), pointFactory(6, 6),
        pointFactory(7, 4), pointFactory(7, 7) }) {
        initialize(converted, loc, TileType.tundra, groundTwo());
    }
    converted.addRivers(pointFactory(2, 6), River.lake, River.south);
    converted.addRivers(pointFactory(3, 6), River.north, River.south);
    converted.setMountainous(pointFactory(4, 0), true);
    converted.addRivers(pointFactory(4, 6), River.north, River.south);
    converted.setMountainous(pointFactory(5, 1), true);
    converted.addRivers(pointFactory(5, 6), River.north, River.south);
    converted.addRivers(pointFactory(6, 0), River.east, River.west);
    converted.addRivers(pointFactory(6, 1), River.east, River.west);
    converted.setMountainous(pointFactory(6, 1), true);
    converted.addRivers(pointFactory(6, 2), River.east, River.west);
    converted.addRivers(pointFactory(6, 3), River.east, River.west);
    converted.addRivers(pointFactory(6, 4), River.east, River.west);
    converted.addRivers(pointFactory(6, 5), River.east, River.west);
    converted.addRivers(pointFactory(6, 6), River.north, River.west);
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
    StringBuilder outOne = StringBuilder();
    StringBuilder outTwo = StringBuilder();
    assertEquals(
        converted.isSubset(oneToTwoConverter.convert(original, true), (String str) => writeLine(outTwo, str)),
        converted.isSubset(oneToTwoConverter.convert(original, true), (String str) => writeLine(outOne, str)),
        "Products of two runs are both or neither subsets of expected");
    assertEquals(outTwo.string, outOne.string,
        "Two runs produce identical results");
    try (outStream = StringWriter(), errStream = Formatter()) {
        SPWriter writer = newWriter;
        writer.writeSPObject(outStream, oneToTwoConverter.convert(original, true));
        assertModuloID(converted, outStream.string, noop);
    }
}

void writeLine(StringBuilder ostream, String line) {
    ostream.append(line);
    ostream.appendNewline();
}
test
shared void testFourthOneToTwoConversion() {
    IMutableMapNG original = SPMapNG(MapDimensionsImpl(2, 2, 1), PlayerCollection(), 0);
    initialize(original, pointFactory(0, 0), TileType.ocean);
    for (point in { pointFactory(0, 1), pointFactory(1, 0),
        pointFactory(1, 1) }) {
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
    initialize(converted, pointFactory(0, 0), TileType.ocean,
        register(Village(TownStatus.active, "", 16, independent, "human")));
    initialize(converted, pointFactory(3, 7), TileType.plains,
        register(Village(TownStatus.active, "", 33, independent, "half-elf")));
    initialize(converted, pointFactory(4, 0), TileType.plains,
        register(Village(TownStatus.active, "", 50, independent, "human")));
    for (i in 0..4) {
        for (j in 0..4) {
            initialize(converted, pointFactory(i, j), TileType.notVisible,
                Ground(testFactory.createID(), "rock1", false));
        }
    }
    for (i in 0..4) {
        for (j in 4..8) {
            initialize(converted, pointFactory(i, j), TileType.notVisible,
                Ground(testFactory.createID(), "rock1", false));
        }
    }
    for (i in 4..8) {
        for (j in 0..4) {
            initialize(converted, pointFactory(i, j), TileType.notVisible,
                Ground(testFactory.createID(), "rock1", false));
        }
    }
    for (i in 4..8) {
        for (j in 4..8) {
            initialize(converted, pointFactory(i, j), TileType.notVisible,
                Ground(testFactory.createID(), "rock4", false));
        }
    }
    initialize(converted, pointFactory(3, 6), TileType.desert,
        register(Meadow("grain1", true, true, 75, FieldStatus.growing)));
    initialize(converted, pointFactory(4, 1), TileType.desert,
        register(Meadow("grain1", true, true, 72, FieldStatus.growing)));
    initialize(converted, pointFactory(4, 6), TileType.desert,
        register(Grove(true, true, "fruit4", 78)));
    initialize(converted, pointFactory(5, 1), TileType.desert,
        register(Grove(true, true, "fruit1", 71)));
    initialize(converted, pointFactory(6, 5), TileType.desert,
        register(Grove(true, true, "fruit4", 73)));
    initialize(converted, pointFactory(6, 6), TileType.desert,
        register(Meadow("grain4", true, true, 76, FieldStatus.growing)));
    initialize(converted, pointFactory(6, 7), TileType.desert,
        register(Meadow("grain4", true, true, 77, FieldStatus.growing)));
    initialize(converted, pointFactory(7, 5), TileType.desert,
        register(Grove(true, true, "fruit4", 74)));
    initialize(converted, pointFactory(7, 6), TileType.desert,
        register(Village(TownStatus.active, "", 67, independent, "human")));
    initialize(converted, pointFactory(2, 6), TileType.desert,
        register(Grove(true, true, "fruit1", 68)));
    initialize(converted, pointFactory(2, 7), TileType.desert,
        register(Meadow("grain1", true, true, 69, FieldStatus.growing)));
    initialize(converted, pointFactory(4, 7), TileType.desert,
        register(Meadow("grain4", true, true, 70, FieldStatus.growing)));
    for (point in { pointFactory(0, 1), pointFactory(0, 2),
        pointFactory(0, 3), pointFactory(1, 0), pointFactory(1, 1),
        pointFactory(1, 2), pointFactory(1, 3), pointFactory(2, 0),
        pointFactory(2, 1), pointFactory(2, 2), pointFactory(2, 3),
        pointFactory(3, 0), pointFactory(3, 1), pointFactory(3, 2),
        pointFactory(3, 3) }) {
        initialize(converted, point, TileType.ocean);
    }
    for (point in { pointFactory(0, 4), pointFactory(0, 5),
        pointFactory(1, 4), pointFactory(3, 5), pointFactory(4, 2),
        pointFactory(4, 3), pointFactory(5, 0), pointFactory(5, 2),
        pointFactory(6, 0), pointFactory(6, 1), pointFactory(7, 0),
        pointFactory(7, 1), pointFactory(7, 3), pointFactory(4, 5),
        pointFactory(5, 5), pointFactory(5, 7) }) {
        initialize(converted, point, TileType.desert);
    }
    for (point in { pointFactory(0, 6), pointFactory(0, 7),
        pointFactory(1, 5), pointFactory(1, 6), pointFactory(1, 7),
        pointFactory(2, 4), pointFactory(2, 5), pointFactory(3, 4),
        pointFactory(5, 3), pointFactory(6, 2), pointFactory(6, 3),
        pointFactory(7, 2), pointFactory(4, 4), pointFactory(5, 4),
        pointFactory(5, 6), pointFactory(6, 4), pointFactory(7, 4),
        pointFactory(7, 7) }) {
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
    StringBuilder outOne = StringBuilder();
    StringBuilder outTwo = StringBuilder();
    assertEquals(
        converted.isSubset(oneToTwoConverter.convert(original, true), (String str) => writeLine(outTwo, str)),
        converted.isSubset(oneToTwoConverter.convert(original, true), (String str) => writeLine(outOne, str)),
        "Products of two runs are both or neither subsets of expected");
    assertEquals(outTwo.string, outOne.string,
        "Two runs produce identical results");
    assertTrue(
        converted.isSubset(oneToTwoConverter.convert(original, true), noop),
        "Actual is at least subset of expected converted");
}