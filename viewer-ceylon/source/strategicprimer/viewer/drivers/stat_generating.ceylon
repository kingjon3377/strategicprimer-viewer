import controller.map.drivers {
    SimpleCLIDriver,
    DriverUsage,
    ParamCount,
    IDriverUsage,
    SPOptions
}
import controller.map.misc {
    ICLIHelper,
    IDFactoryFiller,
    IDRegistrar
}
import model.misc {
    IDriverModel,
    IMultiMapModel
}
import model.exploration {
    IExplorationModel,
    ExplorationModel
}
import java.util {
    JList = List,
    JOptional = Optional,
    Random
}
import model.map {
    Player,
    IMapNG,
    IFixture,
    FixtureIterable,
    Point
}
import model.map.fixtures.mobile {
    IUnit,
    IWorker,
    Worker
}
import ceylon.collection {
    ArrayList,
    Queue
}
import ceylon.interop.java {
    CeylonIterable,
    JavaList,
    toIntegerArray,
    javaString
}
import model.map.fixtures.mobile.worker {
    WorkerStats,
    Job
}
import java.lang {
    JIterable = Iterable
}
import model.workermgmt {
    RaceFactory
}
import ceylon.math.float {
    random
}
import ceylon.file {
    File,
    parsePath
}
"A driver to let the user enter pre-generated stats for existing workers or generate new
 workers."
object statGeneratingCLI satisfies SimpleCLIDriver {
    DriverUsage usageObject = DriverUsage(false, "-t", "--stats", ParamCount.atLeastOne,
        "Enter worker stats or generate new workers.",
        "Enter stats for existing workers or generate new workers randomly.");
    usageObject.addSupportedOption("--current-turn=NN");
    shared actual IDriverUsage usage() => usageObject;
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
            } else if (is FixtureIterable<out Object> fixture) {
                assert (is JIterable<out IFixture> fixture);
                if (exists result = findInIterable(id, *CeylonIterable(fixture))) {
                    return result;
                }
            }
        }
        return null;
    }
    "Find a fixture in a map by ID number."
    IFixture? find(IMapNG map, Integer id) {
        for (location in map.locations()) {
            if (exists forest = map.getForest(location), forest.id == id) {
                return forest;
            } else if (exists ground = map.getGround(location), ground.id == id) {
                return ground;
            }
            else if (exists result = findInIterable(id,
                    *CeylonIterable(map.getOtherFixtures(location)))) {
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
            IMapNG map = pair.first();
            if (is Worker fixture = find(map, id), !fixture.stats exists) {
                fixture.stats = stats;
            }
        }
    }
    "Let the user enter stats for workers already in the maps that are part of one
     particular unit."
    void enterStatsInUnit(IMultiMapModel model, IUnit unit, ICLIHelper cli) {
        JList<Worker> workers = JavaList(ArrayList(0, 1.0,
            { for (member in unit) if (is Worker member, !member.stats exists) member}));
        cli.loopOnList(workers, (clh) => clh.chooseFromList(workers,
                "Which worker do you want to enter stats for?",
                "There are no workers without stats in that unit.",
                "Worker to modify: ", false), "Choose another worker? ",
            (IWorker worker, clh) => enterStatsForWorker(model, worker.id, clh));
    }
    "Let the user enter stats for workers already in the maps that belong to one
     particular player."
    void enterStatsForPlayer(IExplorationModel model, Player player, ICLIHelper cli) {
        JList<IUnit> units = JavaList<IUnit>(ArrayList(0, 1.0,
            removeStattedUnits(*CeylonIterable(model.getUnits(player)))));
        cli.loopOnList(units, (clh) =>
            clh.chooseFromList(units, "Which unit contains the worker in question?",
                "All that player's units already have stats.", "Unit selection: ", false),
            "Choose another unit? ", (IUnit unit, clh) =>
                enterStatsInUnit(model, unit, clh));
    }
    "Let the user enter stats for workers already in the maps."
    void enterStats(IExplorationModel model, ICLIHelper cli) {
        JList<Player> players = model.playerChoices;
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
            if (exists job = worker.getJob(jobName)) {
                job.level = job.level + 1;
            } else {
                worker.addJob(Job(jobName, 1));
            }
        }
    }
    "Get the index of the lowest value in an array."
    Integer getMinIndex(Array<Integer> array) {
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
        WorkerStats base = WorkerStats.factory(() {
            Random rng = Random(system.milliseconds);
            return rng.nextInt(6) + rng.nextInt(6) + rng.nextInt(6) + 3;
        });
        Integer lowestScore = getMinIndex(toIntegerArray(base.toArray()));
        WorkerStats racialBonus;
        switch (race)
        case ("dwarf") { racialBonus = WorkerStats.factory(2, 0, 2, 0, 0, -2); }
        case ("elf") { racialBonus = WorkerStats.factory(-1, 2, 0, 1, 0, 0); }
        case ("gnome") { racialBonus = WorkerStats.factory(-2, 1, -1, 2, 0, 0); }
        case ("half-elf") { racialBonus = WorkerStats.factory(0, 1, 0, 1, 0, 0); }
        case ("Danan") { racialBonus = WorkerStats.factory(-2, 1, 1, 1, -2, 1); }
        else { // treat undefined as human
            Integer chosenBonus = cli.chooseStringFromList(JavaList(ArrayList(7, 1.0,
                {"Strength", "Dexterity", "Constitution", "Intelligence", "Wisdom",
                    "Charisma", "Lowest"}.map(javaString))),
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
        return WorkerStats(hp, base, racialBonus);
    }
    "Add a worker to a unit in all maps."
    void addWorkerToUnit(IMultiMapModel model, IFixture unit, IWorker worker) {
        for (pair in model.allMaps) {
            if (is IUnit fixture = find(pair.first(), unit.id)) {
                fixture.addMember(worker.copy(false));
                Integer turn = pair.first().currentTurn;
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
            String race = RaceFactory.race;
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
        Queue<String> names = ArrayList<String>();
        if (is File file = parsePath(filename).resource) {
            try (reader = file.Reader()) {
                while (exists line = reader.readLine()) {
                    names.offer(line);
                }
            }
        } else {
            cli.println("No such file.");
        }
        for (i in 0..count) {
            String name;
            if (exists temp = names.accept()) {
                name = temp;
            } else {
                name = cli.inputString("Next worker name: ");
            }
            String race = RaceFactory.race;
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
        JList<IUnit> units = JavaList<IUnit>(ArrayList(0, 1.0,
            removeStattedUnits(*CeylonIterable(model.getUnits(player)))));
        cli.loopOnMutableList(units, (clh) => clh.chooseFromList(units,
                "Which unit contains the worker in question? (Select -1 to create new.)",
                "There are no units owned by that player.", "Unit selection: ",
                false), "Choose another unit? ", (JList<out IUnit> list, clh) {
            Point point = clh.inputPoint("Where to put new unit? ");
            IUnit temp = UnitConstructor.unit(player, clh.inputString("Kind of unit: "),
                clh.inputString("Unit name: "), idf.createID());
            for (pair in model.allMaps) {
                pair.first().addFixture(point, temp);
            }
            units.add(temp);
            return JOptional.\iof(temp);
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
        JList<Player> players = model.playerChoices;
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
    shared actual void startDriver(ICLIHelper cli, SPOptions options, IDriverModel model) {
        if (is IExplorationModel model) {
            if (cli.inputBooleanInSeries(
                    "Enter pregenerated stats for existing workers?")) {
                enterStats(model, cli);
            } else {
                createWorkers(model, IDFactoryFiller.createFactory(model), cli);
            }
        } else {
            startDriver(cli, options, ExplorationModel(model));
        }
    }
}