import strategicprimer.drivers.common {
    CLIDriver
}
import strategicprimer.drivers.common.cli {
    ICLIHelper,
    Applet,
    AppletChooser
}
import ceylon.collection {
    MutableList,
    ArrayList,
    HashMap,
    MutableMap
}
import strategicprimer.model.common.map.fixtures.mobile {
    IUnit,
    Animal,
    IWorker,
    animalPlurals
}
import strategicprimer.drivers.exploration.common {
    IExplorationModel,
    HuntingModel
}
import strategicprimer.viewer.drivers.exploration {
    ExplorationCLIHelper
}
import strategicprimer.model.common.map.fixtures.towns {
    Fortress
}
import lovelace.util.common {
    matchingValue
}
import strategicprimer.drivers.query {
    HerdModel,
    MammalModel,
    PoultryModel
}
import ceylon.language.meta.model {
    ValueConstructor
}
import com.vasileff.ceylon.structures {
    HashMultimap,
    MutableMultimap
}
import strategicprimer.model.common.map.fixtures.mobile.worker {
    IJob
}
import strategicprimer.model.common.map.fixtures {
    Quantity,
    ResourcePile
}
import strategicprimer.model.common.idreg {
    IDRegistrar,
    createIDFactory
}
import strategicprimer.viewer.drivers.advancement {
    LevelGainListener,
    AdvancementCLIHelper
}
import strategicprimer.model.common.map {
    Point,
    TileFixture,
    HasPopulation
}
import strategicprimer.model.common.map.fixtures.resources {
    Grove,
    Shrub,
    Meadow
}
class TurnApplet(shared actual String() invoke, shared actual String description,
    shared actual String+ commands) satisfies Applet {}
class TurnRunningCLI(ICLIHelper cli, model) satisfies CLIDriver {
    shared actual IExplorationModel model;
    IDRegistrar idf = createIDFactory(model.allMaps.map(Entry.key));
    Boolean unfinishedResults(Integer turn)(IUnit unit) {
        String results = unit.getResults(turn);
        return results.empty || results.lowercased.containsAny(["fixme", "todo", "xxx"]);
    }
    Fortress? containingFortress(IUnit unit) =>
        model.map.fixtures.get(model.find(unit)).narrow<Fortress>()
            .find(matchingValue(unit.owner, Fortress.owner));
    MutableMap<String, HerdModel> herdModels = HashMap<String, HerdModel>();
    HerdModel? chooseHerdModel(String animal) => cli.chooseFromList(
        `MammalModel`.getValueConstructors().chain(`PoultryModel`.getValueConstructors())
            .narrow<ValueConstructor<HerdModel>>().map((model) => model.get()).sequence(),
        "What kind of animal(s) is/are ``animal``?", "No animal kinds found",
        "Kind of animal:", false).item;
    String herd() {
        assert (exists unit = model.selectedUnit);
        StringBuilder buffer = StringBuilder();
        Fortress? home = containingFortress(unit);
        for (kind in unit.narrow<Animal>().map(Animal.kind).distinct
                .filter(not(herdModels.keys.contains))) {
            if (exists herdModel = chooseHerdModel(kind)) {
                herdModels[kind] = herdModel;
            } else {
                cli.println("Aborting ...");
                return "";
            }
        }
        MutableMultimap<HerdModel, Animal> modelMap = HashMultimap<HerdModel, Animal>();
        for (group in unit.narrow<Animal>()
                .filter(or(matchingValue("tame", Animal.status),
                    matchingValue("domesticated", Animal.status)))) {
            assert (exists herdModel = herdModels[group.kind]);
            modelMap.put(herdModel, group);
        }
        Integer workerCount = unit.narrow<IWorker>().size;
        Boolean experts = unit.narrow<IWorker>().map((worker) => worker.getJob("herder"))
            .map(IJob.level).map((-5).plus).any(Integer.positive);
        variable Integer minutesSpent = 0;
        void addToOrders(String string) {
            cli.print(string);
            buffer.append(string);
        }
        void addLineToOrders(String string) {
            cli.println(string);
            buffer.append(string);
            buffer.appendNewline();
        }
        for (herdModel->animals in modelMap.asMap) {
            if (!buffer.empty) {
                buffer.appendNewline();
                buffer.appendNewline();
            }
            assert (exists combinedAnimal = animals.reduce(uncurry(Animal.combined)));
            Integer flockPerHerder =
                (combinedAnimal.population + workerCount - 1) / workerCount;
            Quantity production = herdModel.scaledProduction(combinedAnimal.population);
            Float pounds = herdModel.scaledPoundsProduction(combinedAnimal.population);
            String resourceProduced;
            switch (herdModel)
            case (is PoultryModel) {
                resourceProduced = combinedAnimal.kind + " eggs";
                Boolean? cleaningDay = cli.inputBoolean("Is this the one turn in every ``
                        herdModel.extraChoresInterval + 1`` to clean up after birds?");
                addToOrders("Gathering ``combinedAnimal`` eggs took the ``workerCount``");
                addLineToOrders(" workers ``herdModel.dailyTime(flockPerHerder)`` min.");
                minutesSpent += herdModel.dailyTimePerHead * flockPerHerder;
                if (exists cleaningDay, cleaningDay) {
                    addToOrders("Cleaning up after them takes ");
                    addToOrders(Float.format(
                        herdModel.dailyExtraTime(flockPerHerder) / 60.0, 0, 1));
                    addLineToOrders(" hours.");
                    minutesSpent += herdModel.extraTimePerHead * flockPerHerder;
                } else if (is Null cleaningDay) {
                    return "";
                }
            }
            case (is MammalModel) {
                resourceProduced = "milk";
                addToOrders("Between two milkings, tending the ");
                addToOrders(animalPlurals.get(combinedAnimal.kind));
                Integer baseCost;
                if (experts) {
                    baseCost = flockPerHerder * (herdModel.dailyTimePerHead - 10);
                } else {
                    baseCost = flockPerHerder * herdModel.dailyTimePerHead;
                }
                addToOrders(" took ``baseCost`` min, plus ");
                addToOrders(herdModel.dailyTimeFloor.string);
                addLineToOrders(" min to gather them.");
                minutesSpent += baseCost;
                minutesSpent += herdModel.dailyTimeFloor;
            }
            addToOrders("This produced ");
            addToOrders(Float.format(production.floatNumber, 0, 1));
            addToOrders(" ``production.units``, ");
            addToOrders(Float.format(pounds, 0, 1));
            addLineToOrders(" lbs, of ``resourceProduced``.");
            if (exists home) {
                ResourcePile createdResource = ResourcePile(idf.createID(), "food", resourceProduced,
                    production);
                createdResource.created = model.map.currentTurn;
                home.addMember(createdResource);
            }
        }
        addToOrders("In all, tending the animals took ``minutesSpent`` min, or ");
        addToOrders(Float.format(minutesSpent / 60.0, 0, 1));
        addLineToOrders(" hours.");
        // TODO: aid option to account for remaining time in results
        return buffer.string.trimmed;
    }
    ExplorationCLIHelper explorationCLI = ExplorationCLIHelper(model, cli);
    model.addMovementCostListener(explorationCLI);
    String explore() {
        StringBuilder buffer = StringBuilder();
        model.addSelectionChangeListener(explorationCLI);
        assert (exists mover = model.selectedUnit);
        // Ask the user about total MP
        model.selectedUnit = mover;
        while (explorationCLI.movement > 0) {
            explorationCLI.moveOneStep();
            if (exists addendum = cli.inputMultilineString("Add to results:")) {
                buffer.append(addendum);
            } else {
                return "";
            }
        }
        // We don't want to be asked about MP for any other applets
        model.removeSelectionChangeListener(explorationCLI);
        return buffer.string;
    }

    HuntingModel huntingModel = HuntingModel(model.map);

    Integer encountersPerHour = 4;

    Integer noResultCost = 60 / encountersPerHour;

    "If argument is a meadow, its status in the format used below; otherwise the empty
     string."
    // TODO: If this class switches from initializer to constructor, make static
    String meadowStatus(Anything argument) {
        if (is Meadow argument) {
            return " (``argument.status``)";
        } else {
            return "";
        }
    }
    "Add a copy of the given fixture to all submaps at the given location iff no fixture
     with the same ID is already there."
    void addToSubMaps(Point point, TileFixture fixture, Boolean zero) {
        for (map->[file, _] in model.subordinateMaps) {
            if (!map.fixtures.get(point).map(TileFixture.id).any(fixture.id.equals)) {
                map.addFixture(point, fixture.copy(zero));
            }
        }
    }
    "Reduce the population of a group of plants, animals, etc., and copy the reduced form
     into all subordinate maps."
    void reducePopulation(Point point, HasPopulation<out TileFixture>&TileFixture fixture,
        String plural, Boolean zero) {
        Integer count = Integer.smallest(cli.inputNumber(
            "How many ``plural`` to remove: ") else 0, fixture.population);
        if (count > 0) {
            model.map.removeFixture(point, fixture);
            Integer remaining = fixture.population - count;
            if (remaining > 0) {
                value addend = fixture.reduced(remaining);
                model.map.addFixture(point, addend);
                for (map->[file , _]in model.subordinateMaps) {
                    if (exists found = map.fixtures.get(point)
                            .find(shuffle(curry(fixture.isSubset))(noop))) {
                        map.removeFixture(point, found);
                    }
                    map.addFixture(point, addend.copy(zero));
                }
            } else {
                for (map->[file, _] in model.subordinateMaps) {
                    if (exists found = map.fixtures.get(point)
                            .find(shuffle(curry(fixture.isSubset))(noop))) {
                        map.removeFixture(point, found);
                    }
                }
            }
        } else {
            addToSubMaps(point, fixture, zero);
        }
    }
    String gather() {
        StringBuilder buffer = StringBuilder();
        // TODO: Ask player to confirm the distance this takes the unit from 
        if (exists center = cli.inputPoint("Location to search around: "),
                exists startingTime = cli.inputNumber("Minutes to spend gathering: ")) {
            variable Integer time = startingTime;
            variable {<Point->Grove|Shrub|Meadow|HuntingModel.NothingFound>*} encounters =
                huntingModel.gather(center);
            while (time > 0, exists loc->find = encounters.first) {
                encounters = encounters.rest;
                if (is HuntingModel.NothingFound find) {
                    cli.println("Found nothing for the next ``noResultCost`` minutes.");
                    time -= noResultCost;
                } else {
                    switch (cli.inputBooleanInSeries(
                        "Gather from ``find.shortDescription````meadowStatus(find)``?",
                        find.kind))
                    case (true) {
                        Integer cost = cli.inputNumber("Time to gather: ")
                            else runtime.maxArraySize;
                        time -= cost;
                        // TODO: Once model supports remaining-quantity-in-fields data, offer to reduce it here
                        if (is Shrub find, find.population>0) {
                            switch (cli.inputBooleanInSeries(
                                "Reduce shrub population here?"))
                            case (true) {
                                reducePopulation(loc, find, "plants", true);
                                cli.println("``time`` minutes remaining.");
                                continue;
                            }
                            case (false) {}
                            case (null) { return ""; }
                        }
                        cli.println("``time`` minutes remaining.");
                    }
                    case (false) { time -= noResultCost; }
                    case (null) { return ""; }
                    addToSubMaps(loc, find, true);
                }
                if (exists addendum = cli.inputMultilineString(
                        "Add to results about that:")) {
                    buffer.append(addendum);
                } else {
                    return "";
                }
            }
        }
        return buffer.string.trimmed;
    }

    AdvancementCLIHelper advancementCLI = AdvancementCLIHelper(cli);
    AppletChooser<TurnApplet> appletChooser =
        AppletChooser(cli,
            TurnApplet(explore, "move", "move a unit"),
            TurnApplet(herd, "herd", "milk or gather eggs from animals"),
            TurnApplet(gather, "gather", "gather vegetation from surrounding area"));
    String createResults(IUnit unit, Integer turn) {
        model.selectedUnit = unit;
        cli.print("Orders for unit ");
        cli.print(unit.name);
        cli.print(" (");
        cli.print(unit.kind);
        cli.print(") for turn ");
        cli.print(turn.string);
        cli.print(": ");
        cli.println(unit.getLatestOrders(turn));
        StringBuilder buffer = StringBuilder();
        while (true) {
            switch (command = appletChooser.chooseApplet())
            case (null|true) { continue; }
            case (false) { return ""; }
            case (is TurnApplet) {
                buffer.append(command.invoke());
                break;
            }
        }
        String prompt;
        if (buffer.empty) {
            prompt = "Results: ";
        } else {
            prompt = "Additional Results: ";
        }
        if (exists addendum = cli.inputMultilineString(prompt)) {
            buffer.append(addendum);
        } else {
            return "";
        }
        if (exists runAdvancement =
                    cli.inputBooleanInSeries("Run advancement for this unit now?"),
                exists expertMentoring = cli.inputBooleanInSeries(
                    "Account for expert mentoring?")) {
            buffer.appendNewline();
            buffer.appendNewline();
            object levelListener satisfies LevelGainListener {
                shared actual void level(String workerName, String jobName,
                        String skillName, Integer gains, Integer currentLevel) {
                    buffer.append(workerName);
                    buffer.append(" showed improvement in the skill of ");
                    buffer.append(skillName);
                    if (gains > 1) {
                        buffer.append(" ``gains`` skill ranks)");
                    }
                    buffer.append(". ");
                }
            }
            advancementCLI.addLevelGainListener(levelListener);
            advancementCLI.advanceWorkersInUnit(unit, expertMentoring);
            advancementCLI.removeLevelGainListener(levelListener);
        }
        return buffer.string.trimmed;
    }
    shared actual void startDriver() {
        Integer currentTurn = model.map.currentTurn;
        if (exists player = cli.chooseFromList(model.playerChoices.sequence(),
                "Players in the maps:", "No players found", "Player to run:",
                false).item) {
            MutableList<IUnit> units = ArrayList {
                elements = model.getUnits(player).filter(unfinishedResults(currentTurn));
            };
            while (true) {
                value index->unit = cli.chooseFromList(units,
                    "Units belonging to ``player``:",
                    "Player has no units without apparently-final results",
                    "Unit to run:", false);
                if (exists unit) {
                    String results = createResults(unit, currentTurn);
                    unit.setResults(currentTurn, results);
                    if (!unfinishedResults(currentTurn)(unit)) {
                        units.delete(index);
                    }
                } else {
                    break;
                }
                if (units.empty) {
                    break;
                }
            }
        }
    }
}
