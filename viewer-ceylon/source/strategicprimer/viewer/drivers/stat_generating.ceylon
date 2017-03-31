import ceylon.collection {
    ArrayList,
    Queue,
    MutableMap,
    HashMap,
    MutableList
}
import ceylon.file {
    File,
    parsePath,
    Directory,
    lines
}
import ceylon.math.float {
    random
}

import java.lang {
    IllegalStateException
}
import java.nio.file {
    JPaths=Paths
}
import java.util {
    Random
}

import lovelace.util.common {
    todo
}
import lovelace.util.jvm {
    singletonRandom
}

import model.map {
    Point
}

import strategicprimer.viewer.drivers.advancement {
    randomRace
}
import strategicprimer.viewer.drivers.exploration {
    loadAllTables,
    ExplorationRunner,
    ExplorationModel,
    IExplorationModel
}
import strategicprimer.viewer.model {
    IMultiMapModel,
    IDriverModel,
    IDRegistrar
}
import strategicprimer.viewer.model.map {
    Player,
    IFixture,
    TileType,
    IMapNG,
    FixtureIterable,
    pointFactory
}
import strategicprimer.viewer.model.map.fixtures.mobile {
    Worker,
    IUnit,
    Unit,
    IWorker
}
import strategicprimer.viewer.model.map.fixtures.mobile.worker {
    WorkerStats,
    IJob
}
import strategicprimer.viewer.model.map.fixtures.towns {
    Village
}
import strategicprimer.viewer.xmlio {
    readMap,
    warningLevels
}

"A driver to let the user enter pre-generated stats for existing workers or generate new
 workers."
object statGeneratingCLI satisfies SimpleCLIDriver {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = false;
        shortOption = "-t";
        longOption = "--stats";
        paramsWanted = ParamCount.atLeastOne;
        shortDescription = "Enter worker stats or generate new workers.";
        longDescription = "Enter stats for existing workers or generate new workers randomly.";
        supportedOptionsTemp = [ "--current-turn=NN" ];
    };
    "The units in the given collection that have workers without stats."
    {IUnit*} removeStattedUnits(IUnit* units) {
        return units.filter((unit) {
            variable Boolean retval = false;
            for (member in unit) {
                if (is Worker member) {
                    if (is Null stats = member.stats) {
                        return false;
                    } else {
                        retval = true;
                    }
                }
            }
            return retval;
        });
    }
    "Find a fixture in a given iterable with the given ID."
    IFixture? findInIterable(Integer id, IFixture* fixtures) {
        for (fixture in fixtures) {
            if (fixture.id == id) {
                return fixture;
            } else if (is FixtureIterable<out IFixture> fixture) {
                if (exists result = findInIterable(id, *fixture)) {
                    return result;
                }
            }
        }
        return null;
    }
    "Find a fixture in a map by ID number."
    IFixture? find(IMapNG map, Integer id) {
        for (location in map.locations) {
            if (exists forest = map.getForest(location), forest.id == id) {
                return forest;
            } else if (exists ground = map.getGround(location), ground.id == id) {
                return ground;
            }
            else if (exists result = findInIterable(id,
                    *map.getOtherFixtures(location))) {
                return result;
            }
        }
        return null;
    }
    "Let the user enter stats for a worker."
    WorkerStats enterStatsCollection(ICLIHelper cli) {
        Integer maxHP = cli.inputNumber("Max HP: ");
        return WorkerStats(maxHP, maxHP, cli.inputNumber("Str: "),
            cli.inputNumber("Dex: "), cli.inputNumber("Con: "), cli.inputNumber("Int: "),
            cli.inputNumber("Wis: "), cli.inputNumber("Cha: "));
    }
    "Let the user enter stats for one worker in particular."
    void enterStatsForWorker(IMultiMapModel model, Integer id, ICLIHelper cli) {
        WorkerStats stats = enterStatsCollection(cli);
        for (pair in model.allMaps) {
            IMapNG map = pair.first;
            if (is Worker fixture = find(map, id), !fixture.stats exists) {
                fixture.stats = stats;
            }
        }
    }
    "Let the user enter stats for workers already in the maps that are part of one
     particular unit."
    void enterStatsInUnit(IMultiMapModel model, IUnit unit, ICLIHelper cli) {
        Worker[] workers = [
            for (member in unit)
                if (is Worker member, !member.stats exists) member ];
        cli.loopOnList(workers, (clh) => clh.chooseFromList(workers,
                "Which worker do you want to enter stats for?",
                "There are no workers without stats in that unit.",
                "Worker to modify: ", false), "Choose another worker? ",
            (IWorker worker, clh) => enterStatsForWorker(model, worker.id, clh));
    }
    "Let the user enter stats for workers already in the maps that belong to one
     particular player."
    void enterStatsForPlayer(IExplorationModel model, Player player, ICLIHelper cli) {
        IUnit[] units = [*removeStattedUnits(*model.getUnits(player))];
        cli.loopOnList(units, (clh) =>
            clh.chooseFromList(units, "Which unit contains the worker in question?",
                "All that player's units already have stats.", "Unit selection: ", false),
            "Choose another unit? ", (IUnit unit, clh) =>
                enterStatsInUnit(model, unit, clh));
    }
    "Let the user enter stats for workers already in the maps."
    void enterStats(IExplorationModel model, ICLIHelper cli) {
        Player[] players = [*model.playerChoices];
        cli.loopOnList(players, (clh) => clh.chooseFromList(players,
                "Which player owns the worker in question?",
                "There are no players shared by all the maps.", "Player selection: ",
                true), "Choose another player?", (Player player, clh) =>
            enterStatsForPlayer(model, player, clh));
    }
    "Let the user enter which Jobs a worker's levels are in."
    void enterWorkerJobs(ICLIHelper cli, IWorker worker, Integer levels) {
        for (i in 0..levels) {
            String jobName = cli.inputString("Which Job does worker have a level in? ");
            IJob job = worker.getJob(jobName);
            job.level = job.level + 1;
        }
    }
    "Get the index of the lowest value in an array."
    Integer getMinIndex(Integer[] array) {
        variable Integer retval = 0;
        for (index->item in array.indexed) {
            if (exists retItem = array[retval], item < retItem) {
                retval = index;
            }
        }
        return retval;
    }
    "Create randomly-generated stats for a worker, with racial adjustments applied."
    WorkerStats createWorkerStats(String race, Integer levels, ICLIHelper cli) {
        // TODO: Only instantiate the Random once
        // TODO: Better yet, use Ceylonic RNG interfaces
        WorkerStats base = WorkerStats.random(() {
            Random rng = Random(system.milliseconds);
            return rng.nextInt(6) + rng.nextInt(6) + rng.nextInt(6) + 3;
        });
        Integer lowestScore = getMinIndex(base.array);
        WorkerStats racialBonus;
        switch (race)
        case ("dwarf") { racialBonus = WorkerStats.factory(2, 0, 2, 0, 0, -2); }
        case ("elf") { racialBonus = WorkerStats.factory(-1, 2, 0, 1, 0, 0); }
        case ("gnome") { racialBonus = WorkerStats.factory(-2, 1, -1, 2, 0, 0); }
        case ("half-elf") { racialBonus = WorkerStats.factory(0, 1, 0, 1, 0, 0); }
        case ("Danan") { racialBonus = WorkerStats.factory(-2, 1, 1, 1, -2, 1); }
        else { // treat undefined as human
            Integer chosenBonus = cli.chooseStringFromList(["Strength", "Dexterity",
                    "Constitution", "Intelligence", "Wisdom", "Charisma", "Lowest"],
                "Character is a ``race``; which stat should get a +2 bonus?", "",
                "Stat for bonus:", false);
            Integer bonusStat;
            if (chosenBonus < 6) {
                bonusStat = chosenBonus;
            } else {
                bonusStat = lowestScore;
            }
            switch (bonusStat)
            case (0) { racialBonus = WorkerStats.factory(2, 0, 0, 0, 0, 0); }
            case (1) { racialBonus = WorkerStats.factory(0, 2, 0, 0, 0, 0); }
            case (2) { racialBonus = WorkerStats.factory(0, 0, 2, 0, 0, 0); }
            case (3) { racialBonus = WorkerStats.factory(0, 0, 0, 2, 0, 0); }
            case (4) { racialBonus = WorkerStats.factory(0, 0, 0, 0, 2, 0); }
            else { racialBonus = WorkerStats.factory(0, 0, 0, 0, 0, 2); }
        }
        Integer conBonus = WorkerStats.getModifier(base.constitution +
            racialBonus.constitution);
        variable Integer hp = 8 + conBonus;
        Random rng = Random(system.milliseconds);
        for (level in 0..levels) {
            hp = hp + rng.nextInt(8) + 1 + conBonus;
        }
        return WorkerStats.adjusted(hp, base, racialBonus);
    }
    "Add a worker to a unit in all maps."
    void addWorkerToUnit(IMultiMapModel model, IFixture unit, IWorker worker) {
        for (pair in model.allMaps) {
            if (is IUnit fixture = find(pair.first, unit.id)) {
                fixture.addMember(worker.copy(false));
                Integer turn = pair.first.currentTurn;
                if (fixture.getOrders(turn).empty) {
                    fixture.setOrders(turn, "TODO: assign");
                }
            }
        }
    }
    "Let the user create randomly-generated workers in a specific unit."
    void createWorkersForUnit(IMultiMapModel model, IDRegistrar idf, IFixture unit,
            ICLIHelper cli) {
        Integer count = cli.inputNumber("How many workers to generate? ");
        for (i in 0..count) {
            String race = randomRace();
            String name = cli.inputString("Worker is a ``race``. Worker name: ");
            Worker worker = Worker(name, race, idf.createID());
            Integer levels = {0, 1, 2}.count((int) => (random() * 20).integer == 0 );
            if (levels == 1) {
                cli.println("Worker has 1 Job level.");
            } else if (levels > 1) {
                cli.println("Worker has ``levels`` Job levels.");
            }
            if (cli.inputBooleanInSeries("Enter pregenerated stats?" )) {
                worker.stats = enterStatsCollection(cli);
            } else {
                WorkerStats stats = createWorkerStats(race, levels, cli);
                worker.stats = stats;
                if (levels > 0) {
                    cli.println("Generated stats:");
                    cli.print(stats.string);
                }
            }
            enterWorkerJobs(cli, worker, levels);
            addWorkerToUnit(model, unit, worker);
        }
    }
    "Let the user create randomly-generated workers, with names read from file, in a
     unit."
    void createWorkersFromFile(IMultiMapModel model, IDRegistrar idf, IFixture unit,
            ICLIHelper cli) {
        Integer count = cli.inputNumber("How many workers to generate? ");
        String filename = cli.inputString("Filename to load names from: ");
        Queue<String> names;
        if (is File file = parsePath(filename).resource) {
            names = ArrayList { *lines(file) };
        } else {
            names = ArrayList<String>();
            cli.println("No such file.");
        }
        for (i in 0..count) {
            String name;
            if (exists temp = names.accept()) {
                name = temp;
            } else {
                name = cli.inputString("Next worker name: ");
            }
            String race = randomRace();
            cli.println("Worker ``name`` is a ``race``");
            Worker worker = Worker(name, race, idf.createID());
            Integer levels = {0, 1, 2}.count((int) => (random() * 20).integer == 0 );
            if (levels == 1) {
                cli.println("Worker has 1 Job level.");
            } else if (levels > 1) {
                cli.println("Worker has ``levels`` Job levels.");
            }
            if (cli.inputBooleanInSeries("Enter pregenerated stats?" )) {
                worker.stats = enterStatsCollection(cli);
            } else {
                WorkerStats stats = createWorkerStats(race, levels, cli);
                worker.stats = stats;
                if (levels > 0) {
                    cli.println("Generated stats:");
                    cli.print(stats.string);
                }
            }
            enterWorkerJobs(cli, worker, levels);
            addWorkerToUnit(model, unit, worker);
        }
    }
    "Allow the user to create randomly-generated workers belonging to a particular
     player."
    void createWorkersForPlayer(IExplorationModel model, IDRegistrar idf, Player player,
            ICLIHelper cli) {
        MutableList<IUnit> units = ArrayList{
            *removeStattedUnits(*model.getUnits(player))
        };
        cli.loopOnMutableList(units, (clh) => clh.chooseFromList(units,
                "Which unit contains the worker in question? (Select -1 to create new.)",
                "There are no units owned by that player.", "Unit selection: ",
                false), "Choose another unit? ", (MutableList<out IUnit> list, clh) {
            Point point = clh.inputPoint("Where to put new unit? ");
            IUnit temp = Unit(player, clh.inputString("Kind of unit: "),
                clh.inputString("Unit name: "), idf.createID());
            for (pair in model.allMaps) {
                pair.first.addFixture(point, temp);
            }
            units.add(temp);
            return temp;
        }, (IUnit unit, clh) {
            if (clh.inputBooleanInSeries(
                    "Load names from file and use randomly generated stats?")) {
                createWorkersFromFile(model, idf, unit, clh);
            } else {
                createWorkersForUnit(model, idf, unit, clh);
            }
        });
    }
    "Allow the user to create randomly-generated workers."
    void createWorkers(IExplorationModel model, IDRegistrar idf, ICLIHelper cli) {
        Player[] players = [*model.playerChoices];
        cli.loopOnList(players, (clh) => clh.chooseFromList(players,
                "Which player owns the new worker(s)?",
                "There are no players shared by all the maps.",
                "Player selection: ", false), "Choose another player? ",
            (Player player, clh) {
                variable Boolean again = true;
                while (again) {
                    createWorkersForPlayer(model, idf, player, clh);
                    again = cli.inputBoolean("Add more workers to another unit?");
                }
            });
    }
    shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        if (is IExplorationModel model) {
            if (cli.inputBooleanInSeries(
                    "Enter pregenerated stats for existing workers?")) {
                enterStats(model, cli);
            } else {
                createWorkers(model, createIDFactory(model), cli);
            }
        } else {
            startDriverOnModel(cli, options, ExplorationModel.copyConstructor(model));
        }
    }
}
"A class to non-interactively generate a tile's contents."
todo("Figure out how to run the Ceylon version repeatedly on a single JVM")
class TileContentsGenerator(IMapNG map) {
    ExplorationRunner runner = ExplorationRunner();
    if (is Directory directory = parsePath("tables").resource) {
        loadAllTables(directory, runner);
    } else {
        throw IllegalStateException(
            "Tile-contents generator requires a tables directory");
    }
    shared void generateTileContents(Point point,
            TileType terrain = map.getBaseTerrain(point)) {
        Integer reps = singletonRandom.nextInt(4) + 1;
        for (i in 0..reps) {
            process.writeLine(runner.recursiveConsultTable("fisher", point, terrain,
                {}, map.dimensions));
        }
    }
}
todo("What's the Ceylon equivalent of Collections.synchronizedMap()?")
MutableMap<String, TileContentsGenerator> tileContentsInstances =
        HashMap<String, TileContentsGenerator>();
TileContentsGenerator tileContentsInstance(String filename) {
    if (exists retval = tileContentsInstances.get(filename)) {
        return retval;
    } else {
        TileContentsGenerator retval =
                TileContentsGenerator(readMap(JPaths.get(filename), warningLevels.default));
        tileContentsInstances.put(filename, retval);
        return retval;
    }
}
shared void tileContentsGenerator() {
    // FIXME: Does process.arguments get reset properly when called from NailGun or equiv?
    String[] args = process.arguments;
    if (exists filename = args[0], exists second = args[1],
            is Integer row = Integer.parse(second), exists third = args[2],
            is Integer column = Integer.parse(third)) {
        tileContentsInstance(filename).generateTileContents(pointFactory(row, column));
    } else {
        process.writeErrorLine("Usage: tileContentsGenerator map_name.xml row col");
    }
}
abstract class SimpleTerrain() of unforested | forested | ocean { }
"Plains, desert, and mountains"
object unforested extends SimpleTerrain() { }
"Temperate forest, boreal forest, and steppe"
object forested extends SimpleTerrain() { }
"Ocean."
object ocean extends SimpleTerrain() { }
"""A hackish driver to fix TODOs (missing content) in the map, namely units with "TODO"
   for their "kind" and aquatic villages with non-aquatic races."""
todo("Write tests of this functionality")
object todoFixerCLI satisfies SimpleCLIDriver {
    "A list of unit kinds (jobs) for plains etc."
    MutableList<String> plainsList = ArrayList<String>();
    "A list of unit kinds (jobs) for forest and jungle."
    MutableList<String> forestList = ArrayList<String>();
    "A list of unit kinds (jobs) for ocean."
    MutableList<String> oceanList = ArrayList<String>();
    "A map from village IDs to races."
    MutableMap<Integer, String> raceMap = HashMap<Integer, String>();
    "A list of aqautic races."
    MutableList<String> raceList = ArrayList<String>();
    "How many units we've fixed."
    variable Integer count = -1;
    shared actual IDriverUsage usage = DriverUsage(false, "-o", "--fix-todos",
        ParamCount.atLeastOne, "Fix TODOs in maps",
        "Fix TODOs in unit kinds and aquatic villages with non-aquatic races");
    "Get the simplified-terrain-model instance covering the map's terrain at the given
     location."
    todo("Just use TileType now we have union types available")
    SimpleTerrain getTerrain(IMapNG map, Point location) {
        switch (map.getBaseTerrain(location))
        case (TileType.jungle|TileType.borealForest|TileType.steppe|
                TileType.temperateForest) { return forested; }
        case (TileType.desert|TileType.mountain|TileType.tundra|TileType.notVisible) {
            return unforested; }
        case (TileType.ocean) { return ocean; }
        case (TileType.plains) {
            if (map.isMountainous(location)) {
                return unforested;
            } else if (map.getForest(location) exists) {
                return forested;
            } else {
                return unforested;
            }
        }
    }
    "Search for and fix aquatic villages with non-aquatic races."
    void fixAllVillages(IMapNG map, ICLIHelper cli) {
        Village[] villages = [ for (point in map.locations)
            if (map.getBaseTerrain(point) == TileType.ocean)
                for (fixture in map.getOtherFixtures(point))
                    if (is Village fixture, landRaces.contains(fixture.race))
                        fixture ];
        if (nonempty villages) {
            if (raceList.empty) {
                while (true) {
                    String race = cli.inputString("Next aquatic race: ").trimmed;
                    if (race.empty) {
                        break;
                    }
                    raceList.add(race);
                }
            }
            for (village in villages) {
                if (exists race = raceMap.get(village.id)) {
                    village.race = race;
                } else {
                    Random rng = Random(village.id);
                    // TODO: assert its existence instead
                    String race = raceList.get(rng.nextInt(raceList.size)) else nothing;
                    village.race = race;
                    raceMap.put(village.id, race);
                }
            }
        }
    }
    "Fix a stubbed-out kind for a unit."
    void fixUnit(Unit unit, SimpleTerrain terrain, ICLIHelper cli) {
        Random rng = Random(unit.id);
        count++;
        MutableList<String> jobList;
        String description;
        switch (terrain)
        case (unforested) {
            jobList = plainsList;
            description = "plains, desert, or mountains";
        }
        case (forested) {
            jobList = forestList;
            description = "forest or jungle";
        }
        case (ocean) {
            jobList = oceanList;
            description = "ocean";
        }
        for (job in jobList) {
            if (rng.nextBoolean()) {
                cli.println("Setting unit with ID #``
                    unit.id`` (``count`` / 5328) to kind ``job``");
                unit.kind = job;
                return;
            }
        }
        String kind = cli.inputString(
            "What's the next possible kind for ``description``? ");
        unit.kind = kind;
        jobList.add(kind);
    }
    "Search for and fix units with kinds missing."
    void fixAllUnits(IMapNG map, ICLIHelper cli) {
        for (point in map.locations) {
            SimpleTerrain terrain = getTerrain(map, point);
            for (fixture in map.getOtherFixtures(point)) {
                if (is Unit fixture, "TODO" == fixture.kind) {
                    fixUnit(fixture, terrain, cli);
                }
            }
        }
    }
    shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        if (is IMultiMapModel model) {
            for (pair in model.allMaps) {
                fixAllUnits(pair.first, cli);
                fixAllVillages(pair.first, cli);
            }
        } else {
            fixAllUnits(model.map, cli);
            fixAllVillages(model.map, cli);
        }
    }
}