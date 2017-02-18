import controller.map.misc {
    ICLIHelper,
    MenuBroker,
    WindowCloser
}
import model.misc {
    IDriverModel
}
import model.exploration {
    IExplorationModel,
    ExplorationModel,
    HuntingModel
}
import java.io {
    IOException
}
import view.exploration {
    ExplorationFrame
}
import javax.swing {
    SwingUtilities
}
import view.util {
    SystemOut
}
import model.exploration.old {
    ExplorationRunner,
    EncounterTable,
    QuadrantTable,
    RandomTable,
    TerrainTable,
    LegacyTable,
    ConstantTable
}
import lovelace.util.common {
    todo
}
import ceylon.collection {
    HashSet,
    MutableSet,
    MutableList,
    LinkedList,
    ArrayList,
    Queue
}
import java.lang {
    ObjectArray, JString=String,
    JInteger=Integer,
    IllegalArgumentException,
    IllegalStateException
}
import strategicprimer.viewer.about {
    aboutDialog
}
import ceylon.file {
    File,
    Directory,
    parsePath
}
import ceylon.interop.java {
    JavaList,
    javaString,
    CeylonList,
    CeylonIterable
}
import util {
    ComparablePair,
    Pair
}
import model.map {
    TileType,
    MapDimensions,
    MapDimensionsImpl,
    PointFactory,
    Point,
    TileFixture,
    Player,
    IMutableMapNG,
    HasOwner
}
import ceylon.test {
    assertEquals,
    test,
    assertThatException
}
import java.util.stream {
    Stream
}
import java.util {
    JList=List
}
import model.map.fixtures.mobile {
    IUnit,
    SimpleMovement,
    Animal
}
import model.listeners {
    MovementCostSource,
    MovementCostListener
}
import model.map.fixtures {
    Ground
}
import model.map.fixtures.terrain {
    Forest
}
import model.map.fixtures.resources {
    CacheFixture
}
"The logic split out of [[explorationCLI]]"
class ExplorationCLIHelper(IExplorationModel model, ICLIHelper cli)
        satisfies MovementCostSource {
    HuntingModel huntingModel = HuntingModel(model.map);
    MutableList<Anything(Integer)|MovementCostListener> listeners = ArrayList<Anything(Integer)|MovementCostListener>();
    shared actual void addMovementCostListener(MovementCostListener listener) =>
            listeners.add(listener);
    shared actual void removeMovementCostListener(MovementCostListener listener) =>
            listeners.remove(listener);
    void fireMovementCost(Integer cost) {
        for (listener in listeners) {
            if (is MovementCostListener listener) {
                listener.deduct(cost);
            } else {
                listener(cost);
            }
        }
    }
    "Have the user choose a player."
    shared Player? choosePlayer() {
        JList<Player> players = model.playerChoices;
        Integer playerNum = cli.chooseFromList(players,
            "Players shared by all the maps:", "No players shared by all the maps:",
            "Chosen player: ", true);
        return CeylonList(players).get(playerNum);
    }
    "Have the user choose a unit belonging to that player."
    shared IUnit? chooseUnit(Player player) {
        JList<IUnit> units = model.getUnits(player);
        Integer unitNum = cli.chooseFromList(units, "Player's units:",
            "That player has no units in the master map", "Chosen unit: ", true);
        return CeylonList(units).get(unitNum);
    }
    "The explorer's current movement speed."
    variable IExplorationModel.Speed speed = IExplorationModel.Speed.normal;
    "Let the user change the explorer's speed"
    void changeSpeed() {
        IExplorationModel.Speed[] speeds = `IExplorationModel.Speed`.caseValues;
        Integer newSpeed = cli.chooseFromList(
            JavaList(speeds),
            "Possible Speeds:", "No speeds available", "Chosen Speed: ", true);
        if (exists temp = speeds[newSpeed]) {
            speed = temp;
        }
    }
    "Copy the given fixture to subordinate maps and print it to the output stream."
    void printAndTransferFixture(Point destPoint, TileFixture? fixture, HasOwner mover) {
        if (exists fixture) {
            cli.println(fixture.string);
            Boolean zero;
            if (is HasOwner fixture, fixture.owner != mover.owner) {
                zero = true;
            } else {
                zero = false;
            }
            for (pair in model.subordinateMaps) {
                IMutableMapNG map = pair.first();
                if (is Ground fixture, !map.getGround(destPoint) exists) {
                    map.setGround(destPoint, fixture.copy(false));
                } else if (is Forest fixture, !map.getForest(destPoint) exists) {
                    map.setForest(destPoint, fixture.copy(false));
                } else {
                    map.addFixture(destPoint, fixture.copy(zero));
                }
            }
            if (is CacheFixture fixture) {
                model.map.removeFixture(destPoint, fixture);
            }
        }
    }
    "Have the player move the selected unit. Throws an assertion-error exception if no
     unit is selected. Movement cost is reported by the driver model to all registered
     MovementCostListeners, while any additional costs for non-movement actions are
     reported by this class, so a listener should be attached to both."
    shared void move() {
        assert (exists mover = model.selectedUnit);
        Integer directionNum = cli.inputNumber("Direction to move: ");
        if (directionNum == 9) {
            changeSpeed();
            return;
        } else if (directionNum > 9) {
            fireMovementCost(JInteger.maxValue);
            return;
        } else if (directionNum < 0) {
            return;
        }
        assert (exists direction = IExplorationModel.Direction.values()[directionNum]);
        Point point = model.selectedUnitLocation;
        Point destPoint = model.getDestination(point, direction);
        try {
            model.move(direction, speed);
        } catch (SimpleMovement.TraversalImpossibleException except) {
            log.debug("Attempted movement to impossible destination");
            cli.println("That direction is impassable; we've made sure all maps show that
                         at a cost of 1 MP");
            return;
        }
        MutableList<TileFixture> constants = ArrayList<TileFixture>();
        IMutableMapNG map = model.map;
        MutableList<TileFixture> allFixtures = ArrayList<TileFixture>();
        for (fixture in {map.getGround(destPoint), map.getForest(destPoint),
                *CeylonIterable(map.getOtherFixtures(destPoint))}.coalesced) {
            if (SimpleMovement.shouldAlwaysNotice(mover, fixture)) {
                constants.add(fixture);
            } else if (SimpleMovement.shouldSometimesNotice(mover, speed, fixture)) {
                allFixtures.add(fixture);
            }
        }
        String tracks;
        if (TileType.ocean == model.map.getBaseTerrain(destPoint)) {
            tracks = huntingModel.fish(destPoint, 1).get(0).string;
        } else {
            tracks = huntingModel.hunt(destPoint, 1).get(0).string;
        }
        if (HuntingModel.nothing != tracks) {
            allFixtures.add(Animal(tracks, true, false, "wild", -1));
        }
        if (IExplorationModel.Direction.nowhere == direction) {
            if (cli.inputBooleanInSeries(
                    "Should any village here swear to the player?  ")) {
                model.swearVillages();
            }
            if (cli.inputBooleanInSeries("Dig to expose some ground here? ")) {
                model.dig();
            }
        }
        String mtn;
        if (map.isMountainous(destPoint)) {
            mtn = "mountainous ";
            for (pair in model.subordinateMaps) {
                pair.first().setMountainous(destPoint, true);
            }
        } else {
            mtn = "";
        }
        cli.println("The explorer comes to ``destPoint``, a ``mtn``tile with terrain ``
            map.getBaseTerrain(destPoint)``");
        {TileFixture*} noticed = CeylonIterable(
            SimpleMovement.selectNoticed(JavaList(allFixtures), identity<TileFixture>,
                mover, speed));
        if (noticed.empty) {
            cli.println("The following were automatically noticed:");
        } else if (noticed.size > 1) {
            cli.println(
                "The following were noticed, all but the last ``noticed
                    .size`` automatically:");
        } else {
            cli.println("The following were noticed, all but the last automatically:");
        }
        constants.addAll(noticed);
        for (fixture in constants) {
            printAndTransferFixture(destPoint, fixture, mover);
        }
    }
    "Ask the user for directions the unit should move until it runs out of MP or the user
      decides to quit."
    todo("Inline back into [[explorationCLI]]?")
    shared void moveUntilDone() {
        if (exists mover = model.selectedUnit) {
            cli.println("Details of the unit:");
            cli.println(mover.verbose());
            Integer totalMP = cli.inputNumber("MP the unit has: ");
            variable Integer movement = totalMP;
            void handleCost(Integer cost) {
                movement -= cost;
            }
            model.addMovementCostListener(handleCost);
//            addMovementCostListener(handleCost);
            listeners.add(handleCost);
            while (movement > 0) {
                cli.println("``movement`` MP of ``totalMP`` remaining.");
                cli.println("Current speed: ``speed.name``");
                cli.println("""0 = N, 1 = NE, 2 = E, 3 = SE, 4 = S, 5 = SW, 6 = W, 7 = NW,
                               8 = Stay Here, 9 = Change Speed, 10 = Quit.""");
                move();
            }
        } else {
            cli.println("No unit is selected");
        }
    }
}
"A CLI to help running exploration."
object explorationCLI satisfies SimpleCLIDriver {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = false;
        shortOption = "-x";
        longOption = "--explore";
        paramsWanted = ParamCount.atLeastOne;
        shortDescription = "Run exploration.";
        longDescription = "Move a unit around the map, updating the player's map with what it sees.";
        supportedOptionsTemp = [ "--current-turn=NN" ];
    };
    shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options, IDriverModel model) {
        IExplorationModel explorationModel;
        if (is IExplorationModel model) {
            explorationModel = model;
        } else {
            explorationModel = ExplorationModel(model);
        }
        try {
            ExplorationCLIHelper eCLI = ExplorationCLIHelper(explorationModel, cli);
            if (exists player = eCLI.choosePlayer(), exists unit = eCLI.chooseUnit(player)) {
                explorationModel.selectUnit(unit);
                eCLI.moveUntilDone();
            }
        } catch (IOException except) {
            throw DriverFailedException(except, "I/O error interacting with user");
        }
    }
}
"An object to start the exploration GUI."
object explorationGUI satisfies SimpleDriver {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = true;
        shortOption = "-x";
        longOption = "--explore";
        paramsWanted = ParamCount.atLeastOne;
        shortDescription = "Run exploration.";
        longDescription = "Move a unit around the map, updating the player's map with what it sees.";
        supportedOptionsTemp = [ "--current-turn=NN" ];
    };
    shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        IExplorationModel explorationModel;
        if (is IExplorationModel model) {
            explorationModel = model;
        } else {
            explorationModel = ExplorationModel(model);
        }
        MenuBroker menuHandler = MenuBroker();
        menuHandler.register(IOHandler(explorationModel, options, cli), "load", "save",
            "save as", "new", "load secondary", "save all", "open in map viewer",
            "open secondary map in map viewer");
        menuHandler.register((event) => process.exit(0), "quit");
        SwingUtilities.invokeLater(() {
            ExplorationFrame frame = ExplorationFrame(explorationModel, menuHandler);
            menuHandler.register(WindowCloser(frame), "close");
            menuHandler.register((event) =>
                aboutDialog(frame, frame.windowName).setVisible(true), "about");
            frame.setVisible(true);
        });
    }
}
"""A driver to help debug "exploration tables", which were the second "exploration results" framework
   I implemented."""
object tableDebugger satisfies SimpleCLIDriver {
    ExplorationRunner runner = ExplorationRunner();
    if (is Directory directory = parsePath("tables").resource) {
        loadAllTables(directory, runner);
    } else {
        throw IllegalStateException("Table debugger requires a tables directory");
    }
    shared actual IDriverUsage usage = DriverUsage(false, "-T", "--table-debug", ParamCount.none,
        "Debug old-model encounter tables",
        "See whether old-model encounter tables refer to a nonexistent table");
    shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options, IDriverModel model) {
        log.warn("tableDebugger doesn't need a driver model");
        startDriverNoArgs();
    }
    "Print all possible results from a table."
    void debugSingleTable(
            "The string to print before each result (passed from the calling table)"
            String before,
            "The string to print after each result (passed from the calling table)"
            String after,
            "The table to debug"
            EncounterTable table,
            "The name of that table"
            String tableName,
            "The stream to write to"
            Anything(String) ostream,
            "The set of tables already on the stack, to prevent infinite recursion"
            todo("Use plain {EncounterTable*} instead of a Set?")
            MutableSet<EncounterTable> set) {
        if (set.contains(table)) {
            ostream("table ``tableName`` is already on the stack, skipping ...");
            ostream("The cause was: ``before``#``tableName``#``after``");
            return;
        }
        set.add(table);
        for (item in table.allEvents()) {
            if (item.contains("#")) {
                // FIXME: This relies on java.lang.String.split(), not ceylon.lang.String
                ObjectArray<JString> parsed = item.split("#", 3);
                String callee = (parsed[1] else nothing).string;
                debugSingleTable("``before````parsed[0] else ""``",
                    "``parsed[2] else ""````after``", runner.getTable(callee), callee,
                    ostream, set);
            } else {
                ostream("``before````item````after``");
            }
        }
        set.remove(table);
    }
    todo("If a CLIHelper was passed in, write to it")
    shared actual void startDriverNoArgs() {
        runner.verboseRecursiveCheck(SystemOut.sysOut);
        EncounterTable mainTable = runner.getTable("main");
        debugSingleTable("", "", mainTable, "main",
            (string) => SystemOut.sysOut.println(string), HashSet<EncounterTable>());
    }
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
                MutableList<ComparablePair<JInteger, JString>> list =
                        ArrayList<ComparablePair<JInteger, JString>>();
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
                            list.add(ComparablePair.\iof(JInteger(leftNum),
                                javaString(right)));
                        } else {
                            throw IOException("Non-numeric data", leftNum);
                        }
                    }
                }
                return RandomTable(JavaList(list));
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
                MutableList<Pair<TileType, JString>> list =
                        ArrayList<Pair<TileType, JString>>();
                while (exists tableLine = argument()) {
                    value splitted = tableLine.split(" ".equals, true, false);
                    if (splitted.size < 2) {
                        log.error("Line with no blanks, coninuing ...");
                    } else {
                        String left = splitted.first;
                        assert (exists right = splitted.rest.reduce(
                            (String partial, element) => "``partial`` ``element``"));
                        value leftVal = TileType.getTileType(left);
                        list.add(Pair.\iof(leftVal, javaString(right)));
                    }
                }
                return TerrainTable(JavaList(list));
            }
            else { throw IllegalArgumentException("unknown table type"); }
        } else {
            throw IOException("File doesn't specify a table type");
        }
    }
}
"Load all tables in the specified path."
void loadAllTables(Directory path, ExplorationRunner runner) {
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