import controller.map.drivers {
    DriverFailedException
}
import controller.map.misc {
    ICLIHelper,
    MapReaderAdapter,
    IDFactoryFiller,
    IDRegistrar,
    IDFactory
}
import model.map {
    IMutableMapNG,
    IMapNG,
    MapDimensionsImpl,
    MapDimensions,
    SPMapNG,
    PlayerCollection,
    Player,
    PlayerImpl,
    Point,
    PointFactory,
    TileFixture,
    TileType,
    River
}
import java.nio.file {
    Paths, JPath = Path,
    NoSuchFileException
}
import util {
    Warning
}
import java.io {
    FileNotFoundException,
    IOException,
    StringWriter,
    StringReader
}
import strategicprimer.viewer.drivers { log }
import javax.xml.stream {
    XMLStreamException
}
import controller.map.formatexceptions {
    SPFormatException
}
import controller.map.converter {
    ResolutionDecreaseConverter
}
import model.exploration.old {
    ExplorationRunner,
    MissingTableException
}
import controller.exploration {
    TableLoader
}
import model.misc {
    IDriverModel,
    IMultiMapModel
}
import java.lang {
    IllegalStateException
}
import java.util {
    JOptional=Optional,
    Random,
    Formatter
}
import lovelace.util.common {
    todo
}
import ceylon.interop.java {
    CeylonIterable
}
import ceylon.collection {
    MutableList,
    LinkedList,
    Queue
}
import lovelace.util.jvm { shuffle }
import model.map.fixtures.towns {
    Village,
    ITownFixture,
    TownStatus,
    TownSize,
    Fortification,
    City
}
import model.workermgmt {
    RaceFactory
}
import model.map.fixtures {
    TextFixture,
    Ground
}
import model.map.fixtures.terrain {
    Forest,
    Sandbar,
    Hill
}
import model.map.fixtures.resources {
    Meadow,
    FieldStatus,
    Grove,
    Shrub,
    Mine,
    StoneDeposit,
    StoneKind
}
import ceylon.test {
    test,
    assertEquals,
    assertTrue
}
import controller.map.iointerfaces {
    SPWriter,
    TestReaderFactory
}
import ceylon.regex {
    Regex,
    regex
}
import model.map.fixtures.mobile {
    Fairy,
    Dragon,
    SimpleImmortal,
    Giant,
    Centaur,
    Animal
}
import model.map.fixtures.explorable {
    AdventureFixture
}
"A driver to convert maps: at present, halving their resolution."
class ConverterDriver(
    """Set to true when the provided [[ICLIHelper]] is connected to a graphical window
       instead of standard output."""
    Boolean gui = false) satisfies UtilityDriver {
    "The usage object."
    shared actual IDriverUsage usage = DriverUsage {
        graphical = gui;
        shortOption = "-v";
        longOption = "--convert";
        paramsWanted = ParamCount.one;
        shortDescription = "Convert a map's format";
        longDescription = "Convert a map. At present, this means reducing its resolution.";
        supportedOptionsTemp = [ "--current-turn=NN" ];
    };
    "Run the driver."
    shared actual void startDriverOnArguments(ICLIHelper cli, SPOptions options,
            String* args) {
        if (nonempty arguments = args.coalesced.sequence()) {
            MapReaderAdapter reader = MapReaderAdapter();
            for (filename in arguments) {
                cli.print("Reading ``filename ``... ");
                try {
                    IMutableMapNG old = reader.readMap(Paths.get(filename),
                        Warning.default);
                    if (options.hasOption("--current-turn")) {
                        value currentTurn =
                                Integer.parse(options.getArgument("--current-turn"));
                        if (is Integer currentTurn) {
                            old.setCurrentTurn(currentTurn);
                        } else {
                            log.error(
                                "Current turn passed on the command line must be an integer",
                                currentTurn);
                        }
                    }
                    cli.println(" ... Converting ... ");
                    IMapNG map = ResolutionDecreaseConverter.convert(old);
                    cli.println("About to write ``filename``.new.xml");
                    reader.write(Paths.get(filename + ".new.xml"), map);
                } catch (FileNotFoundException|NoSuchFileException except) {
                    log.error("``filename`` not found", except);
                } catch (IOException except) {
                    log.error("I/O error processing ``filename``", except);
                } catch (XMLStreamException except) {
                    log.error("XML stream error reading ``filename``", except);
                } catch (SPFormatException except) {
                    log.error("SP map format error in ``filename``", except);
                }
            }
        }
    }
}
"A class to convert a version-1 map to a version-2 map with greater resolution."
object oneToTwoConverter satisfies SimpleDriver {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = false;
        shortOption = "-12";
        longOption = "--one-to-two";
        paramsWanted = ParamCount.atLeastOne;
        shortDescription = "Convert a map's format from version 1 to 2";
        longDescription = "Convert a map from format version 1 to format version 2";
        firstParamDescription = "mainMap.xml";
        subsequentParamDescription = "playerMap.xml";
        supportedOptionsTemp = [ "--current-turn=NN" ];
    };
    "The next turn, to use when creating [[TextFixture]]s."
    Integer nextTurn = 15;
    "The factor by which to expand the map on each axis."
    Integer expansionFactor = 4;
    "The maximum number of iterations per tile."
    Integer maxIterations = 100;
    shared String maxIterationsWarning = "FIXME: A fixture here was force-added after ``
        maxIterations`` iterations";
    "Probability of turning a watered desert to plains."
    Float desertToPlains = 0.4;
    "Probability of adding a forest to a tile."
    Float addForestProbability = 0.1;
    "Source for forest and ground types"
    ExplorationRunner runner = ExplorationRunner();
    TableLoader.loadAllTables("tables", runner);
    "Convert a version-1 map to a higher-resolution version-2 map."
    shared IMapNG convert(
            "The version-1 map to convert"
            todo("Skip this if it's already version 2?")
            IMapNG old,
            "Whether the map is the main map (new encounter-type fixtures don't go on
             players' maps)"
            Boolean main) {
        MapDimensions oldDimensions = old.dimensions();
        IMutableMapNG retval = SPMapNG(MapDimensionsImpl(
            oldDimensions.rows * expansionFactor,
            oldDimensions.columns * expansionFactor, 2), PlayerCollection(), nextTurn);
        Player independent = CeylonIterable(old.players()).find(Player.independent)
            else PlayerImpl(-1, "independent");
        retval.addPlayer(independent);
        for (player in old.players()) {
            retval.addPlayer(player);
        }
        MutableList<Point> converted = LinkedList<Point>();
        IDRegistrar idFactory = IDFactoryFiller.createFactory(old);
        IMapNG oldCopy = old.copy(false, null);
        TileType equivalentTerrain(TileType original) {
            switch (original)
            case (TileType.mountain|TileType.temperateForest) { return TileType.plains; }
            case (TileType.borealForest) { return TileType.steppe; }
            else { return original; }
        }
        "Add a fixture to a tile if this is the main map."
        void addFixture(Point point, TileFixture fixture) {
            if (main) {
                if (is Ground fixture, !retval.getGround(point) exists) {
                    retval.setGround(point, fixture);
                } else if (is Forest fixture, !retval.getForest(point) exists) {
                    retval.setForest(point, fixture);
                } else {
                    retval.addFixture(point, fixture);
                }
            }
        }
        "Convert a tile. That is, change it from a forest or mountain type to the proper
         replacement type plus the proper fixture, and also add the proper Ground."
        void convertSubTile(Point point) {
            TileType originalTerrain = retval.getBaseTerrain(point);
            if (TileType.mountain == originalTerrain) {
                retval.setMountainous(point, true);
            } else if (!retval.getForest(point) exists,
                    !CeylonIterable(retval.getOtherFixtures(point))
                        .find((element) => element is Forest) exists,
                    (TileType.temperateForest == originalTerrain ||
                        TileType.borealForest == originalTerrain)) {
                retval.setForest(point, Forest(runner.getPrimaryTree(point,
                    originalTerrain, retval.streamAllFixtures(point),
                    retval.dimensions()), false, idFactory.createID()));
            }
            retval.setBaseTerrain(point, equivalentTerrain(originalTerrain));
            addFixture(point, Ground(idFactory.createID(),
                runner.getPrimaryRock(point, retval.getBaseTerrain(point),
                    retval.streamAllFixtures(point), retval.dimensions()), false));
        }
        "Convert a single version-1 tile to the equivalent version-2 tiles."
        {Point*} convertTile(Point point) {
            Point[] initial = [ for (i in 0..expansionFactor)
                for (j in 0..expansionFactor)
                    PointFactory.point(point.row * expansionFactor + i,
                        point.col * expansionFactor + j) ];
            for (subtile in initial) {
                retval.setBaseTerrain(subtile, oldCopy.getBaseTerrain(point));
                convertSubTile(subtile);
            }
            if (!oldCopy.isLocationEmpty(point)) {
                Integer idNum = idFactory.createID();
                if (is IMutableMapNG oldCopy) {
                    oldCopy.addFixture(point, Village(TownStatus.active, "", idNum,
                        independent, RaceFactory.getRace(Random(idNum))));
                }
                {TileFixture*} fixtures = {oldCopy.getGround(point),
                    oldCopy.getForest(point), *oldCopy.getOtherFixtures(point)}.coalesced;
                for (river in oldCopy.getRivers(point)) {
                    assert (expansionFactor == 4); // the river-dispersion algorithm is tuned
                    switch (river)
                    case (River.east) {
                        retval.addRivers(initial[10], River.east);
                        retval.addRivers(initial[11], River.east, River.west);
                    }
                    case (River.lake) { retval.addRivers(initial[10], River.lake); }
                    case (River.north) {
                        retval.addRivers(initial[2], River.north, River.south);
                        retval.addRivers(initial[6], River.north, River.south);
                        retval.addRivers(initial[10], River.north);
                    }
                    case (River.south) {
                        retval.addRivers(initial[10], River.south);
                        retval.addRivers(initial[14], River.south, River.north);
                    }
                    case (River.west) {
                        retval.addRivers(initial[8], River.west, River.east);
                        retval.addRivers(initial[9], River.west, River.east);
                        retval.addRivers(initial[10], River.west);
                    }
                }
                Random rng = Random((point.col.leftLogicalShift(32)) + point.row);
                Queue<Point> shuffledInitial = LinkedList(shuffle(initial,
                    rng.nextDouble));
                Queue<TileFixture> shuffledFixtures = LinkedList(shuffle(fixtures,
                    rng.nextDouble));
                for (iteration in 0..maxIterations) {
                    if (!shuffledFixtures.front exists) {
                        break;
                    } else if (exists currentSubtile = shuffledInitial.accept()) {
                        if (CeylonIterable(retval.getOtherFixtures(point)).every(
                                (fixture) =>
                                    fixture is Forest|Ground|Sandbar|Shrub|Meadow|Hill),
                                exists fixture = shuffledFixtures.accept()) {
                            if (is ITownFixture fixture) {
                                {TileFixture*} toRemove = {
                                    for (suspect in retval.getOtherFixtures(point))
                                        if (is Forest suspect) suspect };
                                for (suspect in toRemove) {
                                    retval.removeFixture(point, suspect);
                                }
                                retval.setForest(point, null);
                            }
                            addFixture(currentSubtile, fixture);
                        }
                        shuffledInitial.offer(currentSubtile);
                    }
                } else {
                    log.error("Maximum number of iterations reached on tile ``
                        point``; forcing ...");
                    while (exists fixture = shuffledFixtures.accept()) {
                        assert (exists subtile = shuffledInitial.accept());
                        addFixture(subtile, fixture);
                        retval.addFixture(subtile,
                            TextFixture(maxIterationsWarning, nextTurn));
                    }
                }
            }
            return initial;
        }
        for (row in 0..oldDimensions.rows) {
            for (column in 0..oldDimensions.columns) {
                converted.addAll(convertTile(PointFactory.point(row, column)));
            }
        }
        Random rng = Random(maxIterations);
        for (point in shuffle(converted, rng.nextDouble)) {
            // TODO: wrap around edges of map
            {Point*} neighbors = { for (row in (point.row - 1)..(point.row + 1))
                for (column in (point.col - 1)..(point.col + 1))
                    PointFactory.point(row, column) }.filter((element) => point != element);
            Boolean adjacentToTown() {
                for (neighbor in neighbors) {
                    for (fixture in retval.getOtherFixtures(neighbor)) {
                        if (is ITownFixture fixture) {
                            return true;
                        }
                    }
                } else {
                    return false;
                }
            }
            Boolean adjacentWater() {
                for (neighbor in neighbors) {
                    if (retval.getBaseTerrain(neighbor) == TileType.ocean ||
                            CeylonIterable(retval.getRivers(neighbor)).first exists) {
                        return true;
                    }
                } else {
                    return false;
                }
            }
            try {
                if (TileType.ocean != retval.getBaseTerrain(point)) {
                    if (adjacentToTown(), rng.nextDouble() < 0.6) {
                        Integer id = idFactory.createID();
                            if (rng.nextBoolean()) {
                                addFixture(point, Meadow(runner.recursiveConsultTable("grain",
                                    point, retval.getBaseTerrain(point),
                                    retval.streamAllFixtures(point), retval.dimensions()), true,
                                    true, id, FieldStatus.random(id)));
                            } else {
                                addFixture(point, Grove(true, true,
                                    runner.recursiveConsultTable("fruit_trees", point,
                                        retval.getBaseTerrain(point),
                                        retval.streamAllFixtures(point),
                                        retval.dimensions()), id));
                            }
                    } else if (TileType.desert == retval.getBaseTerrain(point)) {
                        Boolean watered = adjacentWater();
                        if ((watered && rng.nextDouble() < desertToPlains) ||
                            CeylonIterable(retval.getRivers(point)).first exists &&
                                rng.nextDouble() < 0.6) {
                            retval.setBaseTerrain(point, TileType.plains);
                        }
                    } else if (rng.nextDouble() < addForestProbability) {
                        String forestType = runner.recursiveConsultTable(
                            "temperate_major_tree", point, retval.getBaseTerrain(point),
                            retval.streamAllFixtures(point), retval.dimensions());
                        Forest? existingForest = retval.getForest(point);
                        if (exists existingForest, forestType == existingForest.kind) {
                            // do nothing
                        } else {
                            addFixture(point, Forest(forestType, false,
                                idFactory.createID()));
                        }
                    }
                }
            } catch (MissingTableException except) {
                log.warn("Missing encounter table", except);
            }
        }
        return retval;
    }
    void writeConvertedMap(JPath old, IMapNG map) {
        try {
            MapReaderAdapter().write(old.resolveSibling("``old.fileName``.converted.xml"),
                map);
        } catch (IOException except) {
            throw DriverFailedException(
                "I/O error writing to ``old.fileName``.converted.xml", except);
        }
    }
    shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        IMapNG oldMain = model.map;
        JPath oldMainPath = model.mapFile.orElseThrow(() =>
            DriverFailedException("No path for main map",
                IllegalStateException("No path for main map")));
        IMapNG newMain = convert(oldMain, true);
        writeConvertedMap(oldMainPath, newMain);
        if (is IMultiMapModel model) {
            for (pair in model.subordinateMaps) {
                IMapNG map = pair.first();
                JOptional<JPath> temp = pair.second();
                JPath path;
                if (temp.present) {
                    path = temp.get();
                } else {
                    log.warn("No file path associated with map, skipping ...");
                    continue;
                }
                IMapNG newMap = convert(map, false);
                writeConvertedMap(path, newMap);
            }
        }
    }
}
void assertModuloID(IMapNG map, String serialized, Formatter err) {
    Regex matcher = regex("id=\"[0-9]*\"", true);
    try (inStream = StringReader(matcher.replace(serialized, "id=\"-1\""))) {
        assertTrue(
            map.isSubset(MapReaderAdapter().readMapFromStream(inStream,
                Warning.ignore), err, ""),
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
        SPWriter writer = TestReaderFactory.createNewWriter();
        writer.write(outOne, converted);
        writer.write(outTwo, oneToTwoConverter.convert(original, true));
        Regex matcher = regex("id=\"[0-9]*\"", true);
        assertEquals(matcher.replace(outTwo.string, "id=\"-1\""), outOne.string,
            "Produces expected result");
    }
    try (outOne = StringWriter(), outTwo = StringWriter()) {
        SPWriter writer = TestReaderFactory.createOldWriter();
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
        SPWriter writer = TestReaderFactory.createNewWriter();
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
        SPWriter writer = TestReaderFactory.createNewWriter();
        writer.write(outOne, converted);
        writer.write(outTwo, oneToTwoConverter.convert(original, true));
        Regex matcher = regex("id=\"[0-9]*\"", true);
        assertEquals(matcher.replace(outTwo.string, "id=\"-1\""), outOne.string,
            "Produces expected result");
    }
    try (outOne = StringWriter(), outTwo = StringWriter()) {
        SPWriter writer = TestReaderFactory.createOldWriter();
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
        SPWriter writer = TestReaderFactory.createNewWriter();
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
        SimpleImmortal(SimpleImmortal.SimpleImmortalKind.simurgh, 5),
        SimpleImmortal(SimpleImmortal.SimpleImmortalKind.griffin, 6),
        City(TownStatus.ruined, TownSize.large, 0, "cityName", 7, independent),
        SimpleImmortal(SimpleImmortal.SimpleImmortalKind.ogre, 8),
        SimpleImmortal(SimpleImmortal.SimpleImmortalKind.minotaur, 9),
        Centaur("hill", 10), Giant("frost", 11),
        SimpleImmortal(SimpleImmortal.SimpleImmortalKind.djinn, 12),
        Fairy("lesser", 13), Dragon("ice", 14),
        SimpleImmortal(SimpleImmortal.SimpleImmortalKind.troll, 15),
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
        forest("btree1"), SimpleImmortal(SimpleImmortal.SimpleImmortalKind.troll, -1),
        forest("ttree1"));
    initialize(converted, PointFactory.point(0, 6), TileType.steppe, groundOne(),
        forest("btree1"), AdventureFixture(independent, "briefDescription",
            "fullDescription", -1));
    initialize(converted, PointFactory.point(0, 7), TileType.steppe, groundOne(),
        forest("btree1"), SimpleImmortal(SimpleImmortal.SimpleImmortalKind.griffin, -1),
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
        forest("btree1"), SimpleImmortal(SimpleImmortal.SimpleImmortalKind.djinn, -1), village("Danan"),
        TextFixture(oneToTwoConverter.maxIterationsWarning, 15),
        field(FieldStatus.growing));
    initialize(converted, PointFactory.point(2, 7), TileType.steppe, groundOne(),
        forest("btree1"), SimpleImmortal(SimpleImmortal.SimpleImmortalKind.ogre, -1));
    initialize(converted, PointFactory.point(3, 0), TileType.notVisible, groundOne(),
        orchard());
    initialize(converted, PointFactory.point(3, 4), TileType.steppe, groundOne(),
        forest("btree1"), SimpleImmortal(SimpleImmortal.SimpleImmortalKind.simurgh, -1),
        orchard());
    initialize(converted, PointFactory.point(3, 5), TileType.steppe, groundOne(),
        forest("btree1"), SimpleImmortal(SimpleImmortal.SimpleImmortalKind.minotaur, -1),
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
        SPWriter writer = TestReaderFactory.createNewWriter();
        writer.write(outOne, converted);
        writer.write(outTwo, oneToTwoConverter.convert(original, true));
        Regex matcher = regex("id=\"[0-9]*\"", true);
        assertEquals(matcher.replace(outTwo.string, "id=\"-1\""), outOne.string,
            "Produces expected result");
    }
    try (outOne = StringWriter(), outTwo = StringWriter()) {
        SPWriter writer = TestReaderFactory.createOldWriter();
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
        SPWriter writer = TestReaderFactory.createNewWriter();
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
        SPWriter writer = TestReaderFactory.createNewWriter();
        writer.write(outOne, converted);
        writer.write(outTwo, oneToTwoConverter.convert(original, true));
        assertEquals(outTwo.string, outOne.string, "Produces expected result");
    }
    try (outOne = StringWriter(), outTwo = StringWriter()) {
        SPWriter writer = TestReaderFactory.createOldWriter();
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