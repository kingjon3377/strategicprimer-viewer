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
    Player,
    TileFixture,
    Point
}

import strategicprimer.model.common.map.fixtures.mobile {
    AnimalImpl,
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
    CLIDriver,
    emptyOptions,
    SPOptions
}

import strategicprimer.drivers.common.cli {
    ICLIHelper
}

import strategicprimer.drivers.exploration.common {
    Pathfinder,
    pathfinder
}

import ceylon.logging {
    logger,
    Logger
}

import lovelace.util.common {
    readFileContents,
    matchingValue,
    singletonRandom,
    narrowedStream,
    entryMap
}

import strategicprimer.model.common.map.fixtures.towns {
    Village
}

import strategicprimer.model.common.map.fixtures {
    Implement
}

"A logger."
Logger log = logger(`module strategicprimer.drivers.generators`);

"A driver to generate new workers."
// FIXME: Write stat-generating GUI
class StatGeneratingCLI satisfies CLIDriver {
    static String[6] statLabelArray = ["Str", "Dex", "Con", "Int", "Wis", "Cha"];

    "Get the index of the lowest value in an array."
    static Integer getMinIndex(Integer[] array) =>
            array.indexed.max(decreasingItem)?.key else 0;

    "Simulate a die-roll."
    static Integer die(Integer max) => singletonRandom.nextInteger(max) + 1;

    "Simulate rolling 3d6."
    static Integer threeDeeSix() => die(6) + die(6) + die(6);

    "The chance that someone from a village located a [[days]]-day journey away
     will come as a volunteer."
    static Float villageChance(Integer days) => 0.4.powerOfInteger(days);

    ICLIHelper cli;
    shared actual PopulationGeneratingModel model;
    shared actual SPOptions options = emptyOptions;
    shared new (ICLIHelper cli, PopulationGeneratingModel model) {
        this.cli = cli;
        this.model = model;
    }

    "Let the user enter which Jobs a worker's levels are in."
    void enterWorkerJobs(IUnit unit, IWorker worker, Integer levels) {
        for (i in 0:levels) {
            if (exists jobName =
                    cli.inputString("Which Job does worker have a level in? ")) {
                if (!model.addJobLevel(unit, worker, jobName)) {
                    log.warn("Adding or incrementing Job failed somehow ...");
                }
            } else {
                break;
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
            assert (exists retval = cli.inputBoolean(
                "Has a newcomer come from ``village.name`` in the last 7 turns?"));
            excludedVillages[village] = retval;
            return retval;
        }
    }

    "Racial stat bonuses."
    MutableMap<String, WorkerStats> racialBonuses = HashMap<String, WorkerStats>();

    "Load racial stat bonuses for the given race from the cache, or if not present there
     from file."
    WorkerStats loadRacialBonus(String race) {
        if (exists retval = racialBonuses[race]) {
            return retval;
        } else if (exists textContent = readFileContents(
                `module strategicprimer.model.common`,
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

    "Whether the user has said to always give a human's racial stat bonus to the lowest
     stat."
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
        worker.notes[village.owner] = "From ``village.name``.";
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
                cli.print(name);
                cli.println(" is a ``village.race`` from ``village.name``. Stats:");
                cli.println(", ".join(zipPairs(statLabelArray,
                    stats.array.map(WorkerStats.getModifierString)).map(" ".join)));
                return worker;
            } else {
                assert (exists training = singletonRandom.nextElement(candidates));
                worker.addJob(training);
                while (true) {
                    WorkerStats stats = createWorkerStats(village.race, training.level);
                    cli.println(
                        "``name``, a ``village.race``, is a level-``training.level`` ``
                            training.name`` from ``village.name``. Proposed stats:");
                    cli.println(", ".join(zipPairs(statLabelArray,
                        stats.array.map(WorkerStats.getModifierString)).map(" ".join)));
                    assert (exists acceptance =
                        cli.inputBoolean("Do those stats fit that profile?"));
                    if (acceptance) {
                        worker.stats = stats;
                        break;
                    }
                }
                assert (exists hasMount = cli.inputBooleanInSeries("Is the worker mounted?"));
                if (hasMount, exists mountKind = cli.inputString("Kind of mount: "), !mountKind.empty) {
                    worker.mount = AnimalImpl(mountKind, false, "tame", idf.createID());
                }
                variable String equipmentPrompt = "Does the worker have any equipment?";
                assert (exists woolen = cli.inputBooleanInSeries("Is the worker's tunic woolen rather than linen?"));
                worker.addEquipment(Implement((woolen) then "woolen tunic" else "linen tunic", idf.createID()));
                if ((worker.map(IJob.level).reduce(plus) else 0) > 1 || singletonRandom.nextFloat() < 0.9) {
                    worker.addEquipment(Implement("woolen cloak", idf.createID()));
                } else {
                    cli.println("Not adding woolen cloak");
                }
                if ((worker.map(IJob.level).reduce(plus) else 0) > 1 || singletonRandom.nextFloat() < 0.8) {
                    worker.addEquipment(Implement("pair leather boots", idf.createID()));
                } else {
                    cli.println("Not adding leather boots");
                }
                if ((worker.map(IJob.level).reduce(plus) else 0) > 1 || singletonRandom.nextFloat() < 0.75) {
                    worker.addEquipment(Implement("leather satchel", idf.createID()));
                } else {
                    cli.println("Not adding leather satchel");
                }
                if ((worker.map(IJob.level).reduce(plus) else 0) > 1 || singletonRandom.nextFloat() < 0.75) {
                    worker.addEquipment(Implement("leather waterskin", idf.createID()));
                } else {
                    cli.println("Not adding leather waterskin");
                }
                // TODO: Should probably automatically add some job-specific equipment
                variable Boolean?(String) equipmentQuery = cli.inputBooleanInSeries;
                while (exists hasEquipment = equipmentQuery(equipmentPrompt), hasEquipment,
                        exists equipment = cli.inputString("Kind of equipment: "), !equipment.empty) {
                    worker.addEquipment(Implement(equipment, idf.createID()));
                    equipmentPrompt = "Does the worker have any more equipment?";
                    equipmentQuery = cli.inputBoolean;
                }
                return worker;

            }
        } else {
            cli.println("No population details, so no levels.");
            WorkerStats stats = createWorkerStats(village.race, 0);
            worker.stats = stats;
            cli.println("``name`` is a ``village.race`` from ``village.name``. Stats:");
            cli.println(", ".join(zipPairs(statLabelArray,
                stats.array.map(WorkerStats.getModifierString)).map(" ".join)));
            assert (exists woolen = cli.inputBooleanInSeries("Is the worker's tunic woolen rather than linen?"));
            worker.addEquipment(Implement((woolen) then "woolen tunic" else "linen tunic", idf.createID()));
            if (singletonRandom.nextFloat() < 0.9) {
                worker.addEquipment(Implement("woolen cloak", idf.createID()));
            }
            if (singletonRandom.nextFloat() < 0.8) {
                worker.addEquipment(Implement("pair leather boots", idf.createID()));
            }
            if (singletonRandom.nextFloat() < 0.75) {
                worker.addEquipment(Implement("leather satchel", idf.createID()));
            }
            if (singletonRandom.nextFloat() < 0.75) {
                worker.addEquipment(Implement("leather waterskin", idf.createID()));
            }
            return worker;
        }
    }

    "Let the user create randomly-generated workers in a specific unit."
    void createWorkersForUnit(IDRegistrar idf, IUnit unit) {
        Integer count = cli.inputNumber("How many workers to generate? ") else 0;
        for (i in 0:count) {
            String race = raceFactory.randomRace();
            Worker worker;
            if (exists name = cli.inputString("Worker is a ``race``. Worker name: ")) {
                worker = Worker(name, race, idf.createID());
            } else {
                break;
            }
            Integer levels = singletonRandom.integers(20).take(3).count(0.equals);
            if (levels == 1) {
                cli.println("Worker has 1 Job level.");
            } else if (levels > 1) {
                cli.println("Worker has ``levels`` Job levels.");
            }
            WorkerStats stats = createWorkerStats(race, levels);
            worker.stats = stats;
            if (levels > 0) {
                cli.println("Generated stats:");
                cli.print(stats.string);
            }
            model.addWorkerToUnit(unit, worker);
            enterWorkerJobs(unit, worker, levels);
        }
    }

    "Let the user create randomly-generated workers, with names read from file, in a
     unit."
    void createWorkersFromFile(IDRegistrar idf, IUnit unit) {
        Integer count = cli.inputNumber("How many workers to generate? ") else 0;
        Queue<String> names;
        if (exists filename = cli.inputString("Filename to load names from: ")) {
            if (is File file = parsePath(filename).resource) {
                names = LinkedList(lines(file));
            } else {
                names = LinkedList<String>();
                cli.println("No such file.");
            }
        } else {
            return;
        }
        Point hqLoc;
        if (exists found = model.map.fixtures.find(
                matchingValue<Point->TileFixture, Integer>(unit.id,
                    compose(TileFixture.id, Entry<Point, TileFixture>.item)))?.key) {
            hqLoc = found;
        } else {
            cli.println("That unit's location not found in main map.");
            if (exists point = cli.inputPoint("Location to use for village distances:")) {
                hqLoc = point;
            } else {
                return;
            }
        }
        Pathfinder pather = pathfinder(model.map);
        [Integer, Float] travelDistance(Point dest) =>
                [pather.getTravelDistance(hqLoc, dest).first, model.mapDimensions.distance(hqLoc, dest)];
        value villages = narrowedStream<Point, Village>(model.map.fixtures)
            .filter(matchingValue(unit.owner,
                compose(Village.owner, Entry<Point, Village>.item)))
            .map(entryMap(travelDistance, identity<Village>))
            .sort(byIncreasing(compose(Tuple<Integer|Float, Integer, [Float]>.first,
                Entry<[Integer, Float], Village>.key)));
        Integer mpPerDay = cli.inputNumber("MP per day for village volunteers:") else -1;
        for (i in 0:count) {
            String name;
            if (exists temp = names.accept()) {
                name = temp.trimmed;
            } else if (exists temp = cli.inputString("Next worker name: ")) {
                name = temp;
            } else {
                break;
            }
            Village? home;
            for ([mpDistance, tileDistance]->village in villages) {
                if (hasLeviedRecently(village)) {
                    continue;
                } else if (singletonRandom.nextFloat() <
                        villageChance(smallest(mpDistance.float / mpPerDay, tileDistance / 12.0).integer + 1)) {
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
                model.addWorkerToUnit(unit, worker);
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
                model.addWorkerToUnit(unit, worker);
                enterWorkerJobs(unit, worker, levels);
                cli.println("``name`` is a ``race`` Stats:");
                cli.println(", ".join(zipPairs(statLabelArray,
                    stats.array.map(WorkerStats.getModifierString)).map(" ".join)));
            }
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
                if (exists point = cli.inputPoint("Where to put new unit? "),
                        exists kind = cli.inputString("Kind of unit: "),
                        exists name = cli.inputString("Unit name: ")) {
                    IUnit temp = Unit(player, kind, name, idf.createID());
                    model.addUnitAtLocation(temp, point);
                    units.add(temp);
                    item = temp;
                } else {
                    return;
                }
            } else {
                break;
            }
            switch (cli.inputBooleanInSeries(
                "Load names from file and use randomly generated stats?"))
            case (true) { createWorkersFromFile(idf, item); }
            case (false) { createWorkersForUnit(idf, item); }
            case (null) { return; }
            if (exists continuation = cli.inputBoolean("Choose another unit? "),
                    continuation) {
                // continue;
            } else {
                break;
            }
        }
    }

    shared actual void startDriver() {
        IDRegistrar idf = createIDFactory(model.allMaps);
        MutableList<Player> players = ArrayList { elements = model.playerChoices; };
        while (!players.empty, exists chosen = cli.chooseFromList(players,
                "Which player owns the new worker(s)?",
                "There are no players shared by all the maps.", "Player selection: ",
                false).item) {
            players.remove(chosen);
            while (true) {
                createWorkersForPlayer(idf, chosen);
                switch (cli.inputBoolean("Add more workers to another unit?"))
                case (null) { return; }
                case (false) { break; }
                case (true) {}
            }
            if (exists continuation = cli.inputBoolean("Choose another player?"),
                    continuation) {
                // continue;
            } else {
                break;
            }
        }
    }
}
