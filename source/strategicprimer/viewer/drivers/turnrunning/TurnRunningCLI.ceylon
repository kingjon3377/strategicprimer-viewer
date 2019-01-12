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
    IExplorationModel
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
class TurnApplet(shared actual void invoke(), shared actual String description,
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
    void herd() {
        assert (exists unit = model.selectedUnit);
        Fortress? home = containingFortress(unit);
        for (kind in unit.narrow<Animal>().map(Animal.kind).distinct
                .filter(not(herdModels.keys.contains))) {
            if (exists herdModel = chooseHerdModel(kind)) {
                herdModels[kind] = herdModel;
            } else {
                cli.println("Aborting ...");
                return;
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
        for (herdModel->animals in modelMap.asMap) {
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
                cli.print("Gathering ``combinedAnimal`` eggs took the ``workerCount`` ");
                cli.println("workers ``herdModel.dailyTime(flockPerHerder)`` min.");
                minutesSpent += herdModel.dailyTimePerHead * flockPerHerder;
                if (exists cleaningDay, cleaningDay) {
                    cli.print("Cleaning up after them takes ");
                    cli.print(Float.format(
                        herdModel.dailyExtraTime(flockPerHerder) / 60.0, 0, 1));
                    cli.println(" hours.");
                    minutesSpent += herdModel.extraTimePerHead * flockPerHerder;
                } else if (is Null cleaningDay) {
                    return;
                }
            }
            case (is MammalModel) {
                resourceProduced = "milk";
                cli.print("Between two milkings, tending the ");
                cli.print(animalPlurals.get(combinedAnimal.kind));
                Integer baseCost;
                if (experts) {
                    baseCost = flockPerHerder * (herdModel.dailyTimePerHead - 10);
                } else {
                    baseCost = flockPerHerder * herdModel.dailyTimePerHead;
                }
                cli.print(" took ``baseCost`` min, plus ");
                cli.print(herdModel.dailyTimeFloor.string);
                cli.println(" min to gather them.");
                minutesSpent += baseCost;
                minutesSpent += herdModel.dailyTimeFloor;
            }
            cli.print("This produced ");
            cli.print(Float.format(production.floatNumber, 0, 1));
            cli.print(" ``production.units``, ");
            cli.print(Float.format(pounds, 0, 1));
            cli.print(" lbs, of ``resourceProduced``.");
            if (exists home) {
                ResourcePile createdResource = ResourcePile(idf.createID(), "food", resourceProduced,
                    production);
                createdResource.created = model.map.currentTurn;
                home.addMember(createdResource);
            }
        }
        cli.print("In all, tending the animals took ``minutesSpent`` min, or ");
        cli.print(Float.format(minutesSpent / 60.0, 0, 1));
        cli.println(" hours.");
    }
    ExplorationCLIHelper explorationCLI = ExplorationCLIHelper(model, cli);
    AppletChooser<TurnApplet> appletChooser =
        AppletChooser(cli,
            TurnApplet(explorationCLI.moveUntilDone, "move", "move a unit"),
            TurnApplet(herd, "herd", "milk or gather eggs from animals"));
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
        while (true) {
            switch (command = appletChooser.chooseApplet())
            case (null|true) { continue; }
            case (false) { return ""; }
            case (is TurnApplet) {
                command.invoke();
                break;
            }
        }
        return cli.inputMultilineString("Results: ") else "";
        // TODO: Do and report on advancement
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
