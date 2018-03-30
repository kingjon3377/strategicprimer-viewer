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
    lines
}

import strategicprimer.model.idreg {
    IDRegistrar,
    createIDFactory
}
import strategicprimer.model.map {
    Point,
    Player,
    IFixture,
    IMapNG
}
import strategicprimer.model.map.fixtures.mobile {
    Worker,
    IUnit,
    Unit,
    IWorker
}
import strategicprimer.model.map.fixtures.mobile.worker {
    WorkerStats,
    IJob,
    raceFactory
}
import strategicprimer.drivers.common {
    IMultiMapModel,
    IDriverModel,
    SimpleCLIDriver,
    IDriverUsage,
    DriverUsage,
    ParamCount,
    SPOptions
}
import strategicprimer.drivers.common.cli {
    ICLIHelper
}
import strategicprimer.drivers.exploration.common {
    IExplorationModel,
    ExplorationModel
}
import lovelace.util.jvm {
    readFileContents,
	singletonRandom
}
import ceylon.logging {
    logger,
    Logger
}

"A logger."
Logger log = logger(`module strategicprimer.drivers.generators`);
"A driver to let the user enter pre-generated stats for existing workers or generate new
 workers."
shared object statGeneratingCLI satisfies SimpleCLIDriver {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = false;
        invocations = ["-t", "--stats"];
        paramsWanted = ParamCount.atLeastOne;
        shortDescription = "Enter worker stats or generate new workers.";
        longDescription = "Enter stats for existing workers or generate new workers
                           randomly.";
        supportedOptionsTemp = [ "--current-turn=NN" ];
    };
    "The units in the given collection that have workers without stats."
    IUnit[] removeStattedUnits(IUnit* units) => units.filter(
                (unit) => unit.narrow<Worker>().map(Worker.stats)
                    .any((stats) => !stats exists)).sequence();
    "Find a fixture in a given iterable with the given ID."
    IFixture? findInIterable(Integer id, IFixture* fixtures) {
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
    IFixture? find(IMapNG map, Integer id) {
        for (location in map.locations) {
//            if (exists result = findInIterable(id, *map.fixtures[location])) { // TODO: syntax sugar once compiler bug fixed
            if (exists result = findInIterable(id, *map.fixtures.get(location))) {
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
        for (map in model.allMaps.map(Tuple.first)) {
            if (is Worker fixture = find(map, id), !fixture.stats exists) {
                fixture.stats = stats;
            }
        }
    }
    "Let the user enter stats for workers already in the maps that are part of one
     particular unit."
    void enterStatsInUnit(IMultiMapModel model, IUnit unit, ICLIHelper cli) {
        MutableList<Worker> workers = ArrayList { *unit.narrow<Worker>()
            .filter((worker) => !worker.stats exists) };
        while (!workers.empty, exists chosen = cli.chooseFromList(workers,
                "Which worker do you want to enter stats for?",
                "There are no workers without stats in that unit.",
                "Worker to modify: ", false).item) {
            workers.remove(chosen);
            enterStatsForWorker(model, chosen.id, cli);
            if (!cli.inputBoolean("Choose another worker?")) {
                break;
            }
        }
    }
    "Let the user enter stats for workers already in the maps that belong to one
     particular player."
    void enterStatsForPlayer(IExplorationModel model, Player player, ICLIHelper cli) {
        MutableList<IUnit> units = ArrayList { *removeStattedUnits(*model.getUnits(player)) };
        while (!units.empty, exists chosen = cli.chooseFromList(units,
                    "Which unit contains the worker in question?",
                    "All that player's units already have stats.", "Unit selection: ",
                    false).item) {
            units.remove(chosen);
            enterStatsInUnit(model, chosen, cli);
            if (!cli.inputBoolean("Choose another unit?")) {
                break;
            }
        }
    }
    "Let the user enter stats for workers already in the maps."
    void enterStats(IExplorationModel model, ICLIHelper cli) {
        MutableList<Player> players = ArrayList { *model.playerChoices };
        while (!players.empty, exists chosen = cli.chooseFromList(players,
                "Which player owns the worker in question?",
                "There are no players shared by all the maps.", "Player selection: ",
                true).item) {
            players.remove(chosen);
            enterStatsForPlayer(model, chosen, cli);
            if (!cli.inputBoolean("Choose another player?")) {
                break;
            }
        }
    }
    "Let the user enter which Jobs a worker's levels are in."
    void enterWorkerJobs(ICLIHelper cli, IWorker worker, Integer levels) {
        for (i in 0:levels) {
            String jobName = cli.inputString("Which Job does worker have a level in? ");
            IJob job = worker.getJob(jobName);
            job.level = job.level + 1;
        }
    }
    "Get the index of the lowest value in an array."
    Integer getMinIndex(Integer[] array) =>
            array.indexed.max((kOne->iOne, kTwo->iTwo) => iTwo <=> iOne)?.key else 0;
    MutableMap<String, WorkerStats> racialBonuses = HashMap<String, WorkerStats>();
    WorkerStats loadRacialBonus(String race) {
        if (exists retval = racialBonuses[race]) {
            return retval;
        } else if (exists textContent = readFileContents(`module strategicprimer.model`,
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
    variable Boolean alwaysLowest = false;
    "Create randomly-generated stats for a worker, with racial adjustments applied."
    WorkerStats createWorkerStats(String race, Integer levels, ICLIHelper cli) {
        Integer die(Integer max) => singletonRandom.nextInteger(max) + 1;
        WorkerStats base = WorkerStats.random(() => die(6) + die(6) + die(6));
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
        for (i in 0:count) {
            String race = raceFactory.randomRace();
            String name = cli.inputString("Worker is a ``race``. Worker name: ");
            Worker worker = Worker(name, race, idf.createID());
            Integer levels = {0, 1, 2}.count((int) => singletonRandom.nextInteger(20) == 0 );
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
        for (i in 0:count) {
            String name;
            if (exists temp = names.accept()) {
                name = temp.trimmed;
            } else {
                name = cli.inputString("Next worker name: ");
            }
            String race = raceFactory.randomRace();
            cli.println("Worker ``name`` is a ``race``");
            Worker worker = Worker(name, race, idf.createID());
            Integer levels = {0, 1, 2}.count((int) => singletonRandom.nextInteger(20) == 0 );
            if (levels == 1) {
                cli.println("Worker has 1 Job level.");
            } else if (levels > 1) {
                cli.println("Worker has ``levels`` Job levels.");
            }
            WorkerStats stats = createWorkerStats(race, levels, cli);
            worker.stats = stats;
            if (levels > 0) {
                cli.println("Generated stats:");
                cli.print(stats.string);
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
            *model.getUnits(player)
        };
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
                for (pair in model.allMaps) {
                    pair.first.addFixture(point, temp);
                }
                units.add(temp);
                item = temp;
            } else {
                break;
            }
            if (cli.inputBooleanInSeries(
                "Load names from file and use randomly generated stats?")) {
                createWorkersFromFile(model, idf, item, cli);
            } else {
                createWorkersForUnit(model, idf, item, cli);
            }
            if (!cli.inputBoolean("Choose another unit? ")) {
                break;
            }
        }
    }
    "Allow the user to create randomly-generated workers."
    void createWorkers(IExplorationModel model, IDRegistrar idf, ICLIHelper cli) {
        MutableList<Player> players = ArrayList { *model.playerChoices };
        while (!players.empty, exists chosen = cli.chooseFromList(players,
                "Which player owns the new worker(s)?",
                "There are no players shared by all the maps.",
                "Player selection: ", false).item) {
            players.remove(chosen);
            variable Boolean again = true;
            while (again) {
                createWorkersForPlayer(model, idf, chosen, cli);
                again = cli.inputBoolean("Add more workers to another unit?");
            }
            if (!cli.inputBoolean("Choose another player?")) {
                break;
            }
        }
    }
    shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        if (is IExplorationModel model) {
            if (cli.inputBooleanInSeries(
                    "Enter pregenerated stats for existing workers?")) {
                enterStats(model, cli);
            } else {
                createWorkers(model, createIDFactory(model.allMaps
                    .map((pair) => pair.first)), cli);
            }
        } else {
            startDriverOnModel(cli, options, ExplorationModel.copyConstructor(model));
        }
    }
}
