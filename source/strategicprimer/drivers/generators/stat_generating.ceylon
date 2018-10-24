import ceylon.collection {
    ArrayList,
    Queue,
    MutableMap,
    HashMap,
    MutableList,
    LinkedList
}
import ceylon.file {
    File,
    parsePath,
    lines
}

import strategicprimer.model.common.idreg {
    IDRegistrar,
    createIDFactory
}
import strategicprimer.model.common.map {
    IFixture,
    Player,
    TileFixture,
    Point,
    HasOwner,
    IMapNG,
    IMutableMapNG
}
import strategicprimer.model.common.map.fixtures.mobile {
    IUnit,
    Unit,
    Worker,
    IWorker
}
import strategicprimer.model.common.map.fixtures.mobile.worker {
    WorkerStats,
    IJob,
    raceFactory,
    Job
}
import strategicprimer.drivers.common {
    IDriverModel,
    IDriverUsage,
    DriverUsage,
    ParamCount,
    SPOptions,
    CLIDriver,
    DriverFactory,
    ModelDriverFactory,
    ModelDriver
}
import strategicprimer.drivers.common.cli {
    ICLIHelper
}
import strategicprimer.drivers.exploration.common {
    IExplorationModel,
    ExplorationModel,
    pathfinder
}
import ceylon.logging {
    logger,
    Logger
}
import lovelace.util.common {
    readFileContents,
    matchingValue,
    comparingOn,
    singletonRandom,
    narrowedStream,
    entryMap,
    PathWrapper
}
import strategicprimer.model.common.map.fixtures.towns {
    Village
}

"A logger."
Logger log = logger(`module strategicprimer.drivers.generators`);

"A factory for a driver to let the user enter pre-generated stats for existing workers or
 generate new workers."
service(`interface DriverFactory`)
shared class StatGeneratingCLIFactory() satisfies ModelDriverFactory {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = false;
        invocations = ["-t", "--stats"];
        paramsWanted = ParamCount.atLeastOne;
        shortDescription = "Enter worker stats or generate new workers.";
        longDescription = "Enter stats for existing workers or generate new workers
                           randomly.";
        includeInCLIList = true;
        includeInGUIList = false;
        supportedOptions = [ "--current-turn=NN" ];
    };

    shared actual ModelDriver createDriver(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        if (is IExplorationModel model) {
            return StatGeneratingCLI(cli, model);
        } else {
            return createDriver(cli, options, ExplorationModel.copyConstructor(model));
        }
    }

    shared actual IDriverModel createModel(IMutableMapNG map, PathWrapper? path) =>
            ExplorationModel(map, path);
}

"A driver to let the user enter pre-generated stats for existing workers or generate new
 workers."
// FIXME: Write stat-generating/stat-entering GUI
class StatGeneratingCLI satisfies CLIDriver {
    static String[6] statLabelArray = ["Str", "Dex", "Con", "Int", "Wis", "Cha"];

    "Whether the unit has any workers without stats."
    static Boolean hasUnstattedWorker(IUnit unit) =>
            unit.narrow<Worker>().any(matchingValue(null, Worker.stats));

    "The units in the given collection that have workers without stats."
    static IUnit[] removeStattedUnits(IUnit* units) => units.select(hasUnstattedWorker);

    "Find a fixture in a given iterable with the given ID."
    static IFixture? findInIterable(Integer id, IFixture* fixtures) { // TODO: Take two parameter lists, so we can convert the loop in find() to an Iterable.map().find() call.
        for (fixture in fixtures) {
            if (fixture.id == id) {
                return fixture;
            } else if (is {IFixture*} fixture,
                exists result = findInIterable(id, *fixture)) {
                return result;
            }
        }
        return null;
    }

    "Find a fixture in a map by ID number."
    static IFixture? find(IMapNG map, Integer id) {
        // We don't want to use fixtureEntries here because we'd have to spread it,
        // which is probably an eager operation on that *huge* stream.
        for (location in map.locations) {
//            if (exists result = findInIterable(id, *map.fixtures[location])) { // TODO: syntax sugar once compiler bug fixed
            if (exists result = findInIterable(id, *map.fixtures.get(location))) {
                return result;
            }
        }
        return null;
    }

    "Get the index of the lowest value in an array."
    static Integer getMinIndex(Integer[] array) =>
            array.indexed.max(comparingOn(Entry<Integer, Integer>.item,
                decreasing<Integer>))?.key else 0;

    "Simulate a die-roll."
    static Integer die(Integer max) => singletonRandom.nextInteger(max) + 1;

    "Simulate rolling 3d6."
    static Integer threeDeeSix() => die(6) + die(6) + die(6);

    "The chance that someone from a village located a [[days]]-day journey away
     will come as a volunteer."
    static Float villageChance(Integer days) => 0.4.powerOfInteger(days);

    ICLIHelper cli;
    shared actual IExplorationModel model;
    shared new (ICLIHelper cli, IExplorationModel model) {
        this.cli = cli;
        this.model = model;
    }

    "Let the user enter a number, assert that there was not EOF, and return the number."
    Integer inputNumber(String prompt) {
        assert (exists retval = cli.inputNumber(prompt));
        return retval;
    }

    "Let the user enter stats for a worker."
    WorkerStats enterStatsCollection() {
        Integer maxHP = inputNumber("Max HP: ");
        return WorkerStats(maxHP, maxHP, inputNumber("Str: "), inputNumber("Dex: "), // TODO: Replace last 6 params with *statLabelArray.map(shuffle(plus)(": ")).map(inputNumber)
            inputNumber("Con: "), inputNumber("Int: "), inputNumber("Wis: "),
            inputNumber("Cha: "));
    }

    "Let the user enter stats for one worker in particular."
    void enterStatsForWorker(Integer id) {
        WorkerStats stats = enterStatsCollection();
        for (map in model.allMaps.map(Entry.key)) {
            if (is Worker fixture = find(map, id), !fixture.stats exists) {
                fixture.stats = stats;
                model.setModifiedFlag(map, true);
            }
        }
    }

    "Let the user enter stats for workers already in the maps that are part of one
     particular unit."
    void enterStatsInUnit(IUnit unit) {
        MutableList<Worker> workers = ArrayList { elements = unit.narrow<Worker>()
            .filter(matchingValue(null, Worker.stats)); };
        while (!workers.empty, exists chosen = cli.chooseFromList(workers,
            	"Which worker do you want to enter stats for?",
            	"There are no workers without stats in that unit.",
            	"Worker to modify: ", false).item) {
            workers.remove(chosen);
            enterStatsForWorker(chosen.id);
            if (!cli.inputBoolean("Choose another worker?")) {
                break;
            }
        }
    }

    "Let the user enter stats for workers already in the maps that belong to one
     particular player."
    void enterStatsForPlayer(Player player) {
        // TODO: can we avoid either the spread or the named-argument invocation?
        MutableList<IUnit> units = ArrayList { elements =
            removeStattedUnits(*model.getUnits(player)); };
        while (!units.empty, exists chosen = cli.chooseFromList(units,
            	"Which unit contains the worker in question?",
            	"All that player's units already have stats.", "Unit selection: ",
            	false).item) {
            units.remove(chosen);
            enterStatsInUnit(chosen);
            if (!cli.inputBoolean("Choose another unit?")) {
                break;
            }
        }
    }

    "Let the user enter stats for workers already in the maps."
    void enterStats() {
        MutableList<Player> players = ArrayList { elements = model.playerChoices; };
        while (!players.empty, exists chosen = cli.chooseFromList(players,
            	"Which player owns the worker in question?",
            	"There are no players shared by all the maps.", "Player selection: ",
            	true).item) {
            players.remove(chosen);
            enterStatsForPlayer(chosen);
            if (!cli.inputBoolean("Choose another player?")) {
                break;
            }
        }
    }

    "Let the user enter which Jobs a worker's levels are in."
    void enterWorkerJobs(IWorker worker, Integer levels) {
        for (i in 0:levels) {
            IJob job = worker.getJob(cli.inputString(
                "Which Job does worker have a level in? "));
            job.level = job.level + 1; // TODO: Replace with += 1?
        }
    }

    "Add a worker to a unit in all maps."
    void addWorkerToUnit(IFixture unit, IWorker worker) {
        for (map->[file, _] in model.allMaps) {
            if (is IUnit fixture = find(map, unit.id)) {
                fixture.addMember(worker.copy(false));
                Integer turn = map.currentTurn;
                if (fixture.getOrders(turn).empty) {
                    fixture.setOrders(turn, "TODO: assign");
                }
                model.setModifiedFlag(map, true);
            }
        }
    }

    "Villages from which newcomers have arrived either recently or already this turn."
    MutableMap<Village, Boolean> excludedVillages = HashMap<Village, Boolean>();

    "Get from the cache, or if not present there ask the user, if a newcomer
     has come from the given village recently."
    Boolean hasLeviedRecently(Village village) {
        if (exists retval = excludedVillages[village]) {
            return retval;
        } else {
            Boolean retval = cli.inputBoolean(
                "Has a newcomer come from ``village.name`` in the last 7 turns?");
            excludedVillages[village] = retval;
            return retval;
        }
    }

    "Racial stat bonuses."
    MutableMap<String, WorkerStats> racialBonuses = HashMap<String, WorkerStats>();

    "Load racial stat bonuses for the given race from the cache, or if not present there from file."
    WorkerStats loadRacialBonus(String race) {
        if (exists retval = racialBonuses[race]) {
            return retval;
        } else if (exists textContent = readFileContents(`module strategicprimer.model.common`,
            	"racial_stat_adjustments/``race``.txt")) {
            value parsed = textContent.lines.map(Integer.parse)
                    .narrow<Integer>().sequence();
            assert (is Integer[6] temp = [parsed[0], parsed[1], parsed[2], parsed[3],
                parsed[4], parsed[5]]);
            WorkerStats retval = WorkerStats.factory(*temp);
            racialBonuses[race] = retval;
            return retval;
        } else {
            log.warn("No stat adjustments found for ``race``");
            return WorkerStats.factory(0, 0, 0, 0, 0, 0);
        }
    }

    "Whether the user has said to always give a human's racial stat bonus to the lowest stat."
    variable Boolean alwaysLowest = false;

    "Create randomly-generated stats for a worker, with racial adjustments applied."
    WorkerStats createWorkerStats(String race, Integer levels) {
        WorkerStats base = WorkerStats.random(threeDeeSix);
        Integer lowestScore = getMinIndex(base.array);
        WorkerStats racialBonus;
        if (race == "human") {
            Integer bonusStat;
            if (alwaysLowest) {
                bonusStat = lowestScore;
            } else {
                Integer chosenBonus = cli.chooseStringFromList(["Strength", "Dexterity",
                    "Constitution", "Intelligence", "Wisdom", "Charisma", "Lowest",
                    "Always Choose Lowest"],
                    "Character is a ``race``; which stat should get a +2 bonus?", "",
                    "Stat for bonus:", false).key;
                if (chosenBonus<6) {
                    bonusStat = chosenBonus;
                } else if (chosenBonus == 7) {
                    bonusStat = lowestScore;
                    alwaysLowest = true;
                } else {
                    bonusStat = lowestScore;
                }
            }
            switch (bonusStat)
            case (0) { racialBonus = WorkerStats.factory(2, 0, 0, 0, 0, 0); }
            case (1) { racialBonus = WorkerStats.factory(0, 2, 0, 0, 0, 0); }
            case (2) { racialBonus = WorkerStats.factory(0, 0, 2, 0, 0, 0); }
            case (3) { racialBonus = WorkerStats.factory(0, 0, 0, 2, 0, 0); }
            case (4) { racialBonus = WorkerStats.factory(0, 0, 0, 0, 2, 0); }
            else { racialBonus = WorkerStats.factory(0, 0, 0, 0, 0, 2); }
        } else {
            racialBonus = loadRacialBonus(race);
        }
        Integer conBonus = WorkerStats.getModifier(base.constitution +
            racialBonus.constitution);
        variable Integer hp = 8 + conBonus;
        for (level in 0:levels) {
            hp = hp + die(8) + conBonus;
        }
        return WorkerStats.adjusted(hp, base, racialBonus);
    }

    "Generate a worker with race and Job levels based on the population of the
     given village."
    Worker generateWorkerFrom(Village village, String name, IDRegistrar idf) {
        Worker worker = Worker(name, village.race, idf.createID());
        if (exists populationDetails = village.population) {
            MutableList<IJob> candidates = ArrayList<IJob>();
            for (job->level in populationDetails.highestSkillLevels) {
                void addCandidate(Integer lvl) => candidates.add(Job(job, lvl));
//                Anything(Integer) addCandidate = compose(candidates.add, curry(Job)(job)); // TODO: Switch to this form if it compiles without error under 1.3.4/1.4 (TODO: derive MWE and report)
                if (level > 16) {
                    addCandidate(level - 3);
                    addCandidate(level - 4);
                    addCandidate(level - 6);
                    addCandidate(level - 7);
                    singletonRandom.integers(4).map(5.plus).take(16).each(addCandidate);
                    singletonRandom.integers(4).map(1.plus).take(32).each(addCandidate);
                } else if (level > 12) {
                    addCandidate(level - 3);
                    singletonRandom.integers(4).map(5.plus).take(8).each(addCandidate);
                    singletonRandom.integers(4).map(1.plus).take(16).each(addCandidate);
                } else if (level > 8) {
                    singletonRandom.integers(4).map(5.plus).take(3).each(addCandidate);
                    singletonRandom.integers(4).map(1.plus).take(6).each(addCandidate);
                } else if (level > 4) {
                    singletonRandom.integers(4).map(1.plus).take(2).each(addCandidate);
                }
            }
            if (candidates.empty) {
                cli.println("No training available in ``village.name``.");
                WorkerStats stats = createWorkerStats(village.race, 0);
                worker.stats = stats;
                cli.println("``name`` is a ``village.race`` from ``village.name``. Stats:");
                cli.println(", ".join(zipPairs(statLabelArray,
                    stats.array.map(WorkerStats.getModifierString)).map(" ".join)));
                return worker;
            } else {
                assert (exists training = singletonRandom.nextElement(candidates));
                while (true) {
                    worker.addJob(training);
                    WorkerStats stats = createWorkerStats(village.race, training.level);
                    cli.println(
                        "``name``, a ``village.race``, is a level-``training.level`` ``training.name`` from ``village.name``. Proposed stats:");
                    cli.println(", ".join(zipPairs(statLabelArray,
                        stats.array.map(WorkerStats.getModifierString)).map(" ".join)));
                    if (cli.inputBoolean("Do those stats fit that profile?")) {
                        worker.stats = stats;
                        return worker;
                    }
                }

            }
        } else {
            cli.println("No population details, so no levels.");
            WorkerStats stats = createWorkerStats(village.race, 0);
            worker.stats = stats;
            cli.println("``name`` is a ``village.race`` from ``village.name``. Stats:");
            cli.println(", ".join(zipPairs(statLabelArray,
                stats.array.map(WorkerStats.getModifierString)).map(" ".join)));
            return worker;
        }
    }

    "Let the user create randomly-generated workers in a specific unit."
    void createWorkersForUnit(IDRegistrar idf, IFixture unit) {
        Integer count = cli.inputNumber("How many workers to generate? ") else 0;
        for (i in 0:count) {
            String race = raceFactory.randomRace();
            String name = cli.inputString("Worker is a ``race``. Worker name: ");
            Worker worker = Worker(name, race, idf.createID());
            Integer levels = singletonRandom.integers(20).take(3).count(0.equals);
            if (levels == 1) {
                cli.println("Worker has 1 Job level.");
            } else if (levels > 1) {
                cli.println("Worker has ``levels`` Job levels.");
            }
            if (cli.inputBooleanInSeries("Enter pregenerated stats?" )) {
                worker.stats = enterStatsCollection();
            } else {
                WorkerStats stats = createWorkerStats(race, levels);
                worker.stats = stats;
                if (levels > 0) {
                    cli.println("Generated stats:");
                    cli.print(stats.string);
                }
            }
            enterWorkerJobs(worker, levels);
            addWorkerToUnit(unit, worker);
        }
    }

    "Let the user create randomly-generated workers, with names read from file, in a
     unit."
    void createWorkersFromFile(IDRegistrar idf, IFixture&HasOwner unit) {
        Integer count = cli.inputNumber("How many workers to generate? ") else 0;
        String filename = cli.inputString("Filename to load names from: ");
        Queue<String> names;
        if (is File file = parsePath(filename).resource) {
            names = LinkedList(lines(file));
        } else {
            names = LinkedList<String>();
            cli.println("No such file.");
        }
        Point hqLoc;
        if (exists found = model.map.fixtures.find(
                matchingValue<Point->TileFixture, Integer>(unit.id,
                    compose(TileFixture.id, Entry<Point, TileFixture>.item)))?.key) {
            hqLoc = found;
        } else {
            cli.println("That unit's location not found in main map.");
            hqLoc = cli.inputPoint("Location to use for village distances:");
        }
        Integer travelDistance(Point dest) =>
                pathfinder.getTravelDistance(model.map, hqLoc, dest).first;
        value villages = narrowedStream<Point, Village>(model.map.fixtures)
            .filter(matchingValue(unit.owner,
                compose(Village.owner, Entry<Point, Village>.item)))
            .map(entryMap(travelDistance, identity<Village>)).sort(increasingKey);
        Integer mpPerDay = cli.inputNumber("MP per day for village volunteers:") else -1;
        for (i in 0:count) {
            String name;
            if (exists temp = names.accept()) {
                name = temp.trimmed;
            } else {
                name = cli.inputString("Next worker name: ");
            }
            Village? home;
            for (distance->village in villages) {
                if (hasLeviedRecently(village)) {
                    continue;
                } else if (singletonRandom.nextFloat() <
                        villageChance(distance / mpPerDay + 1)) {
                    excludedVillages[village] = true;
                    home = village;
                    break;
                }
            } else {
                home = null;
            }
            Worker worker;
            if (exists home) {
                worker = generateWorkerFrom(home, name, idf);
            } else {
                String race = raceFactory.randomRace();
                cli.println("Worker ``name`` is a ``race``");
                worker = Worker(name, race, idf.createID());
                Integer levels = singletonRandom.integers(20).take(3).count(0.equals);
                if (levels == 1) {
                    cli.println("Worker has 1 Job level.");
                } else if (levels>1) {
                    cli.println("Worker has ``levels`` Job levels.");
                }
                WorkerStats stats = createWorkerStats(race, levels);
                worker.stats = stats;
                if (levels>0) {
                    cli.println("Generated stats:");
                    cli.print(stats.string);
                }
                enterWorkerJobs(worker, levels);
                cli.println("``name`` is a ``race`` Stats:");
                cli.println(", ".join(zipPairs(statLabelArray,
                    stats.array.map(WorkerStats.getModifierString)).map(" ".join)));
            }
            addWorkerToUnit(unit, worker);
        }
    }

    "Allow the user to create randomly-generated workers belonging to a particular
     player."
    void createWorkersForPlayer(IDRegistrar idf, Player player) {
        MutableList<IUnit> units = ArrayList { elements = model.getUnits(player); };
        while (true) {
            value chosen = cli.chooseFromList(units,
                "Which unit contains the worker in question? (Select -1 to create new.)",
                "There are no units owned by that player.", "Unit selection: ",
                false);
            IUnit item;
            if (exists temp = chosen.item) {
                item = temp;
            } else if (chosen.key <= units.size) {
                Point point = cli.inputPoint("Where to put new unit? ");
                IUnit temp = Unit(player, cli.inputString("Kind of unit: "),
                    cli.inputString("Unit name: "), idf.createID());
                for (indivMap->[file, _] in model.allMaps) {
                    indivMap.addFixture(point, temp);
                }
                units.add(temp);
                item = temp;
            } else {
                break;
            }
            if (cli.inputBooleanInSeries(
                "Load names from file and use randomly generated stats?")) {
                createWorkersFromFile(idf, item);
            } else {
                createWorkersForUnit(idf, item);
            }
            if (!cli.inputBoolean("Choose another unit? ")) {
                break;
            }
        }
    }

    "Allow the user to create randomly-generated workers."
    void createWorkers(IDRegistrar idf) {
        MutableList<Player> players = ArrayList { elements = model.playerChoices; };
        while (!players.empty, exists chosen = cli.chooseFromList(players,
            "Which player owns the new worker(s)?",
            "There are no players shared by all the maps.",
            "Player selection: ", false).item) {
            players.remove(chosen);
            variable Boolean again = true;
            while (again) {
                createWorkersForPlayer(idf, chosen);
                again = cli.inputBoolean("Add more workers to another unit?");
            }
            if (!cli.inputBoolean("Choose another player?")) {
                break;
            }
        }
    }

    shared actual void startDriver() {
        if (cli.inputBooleanInSeries(
                "Enter pregenerated stats for existing workers?")) {
            enterStats();
        } else {
            createWorkers(createIDFactory(model.allMaps.map(Entry.key)));
        }
    }
}
