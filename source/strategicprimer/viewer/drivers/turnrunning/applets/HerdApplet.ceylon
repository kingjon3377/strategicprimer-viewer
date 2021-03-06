import strategicprimer.drivers.common.cli {
    ICLIHelper
}

import lovelace.util.common {
    matchingValue
}
import strategicprimer.model.common.map.fixtures.towns {
    IFortress
}
import strategicprimer.model.common.map.fixtures.mobile.worker {
    IJob
}
import strategicprimer.model.common.map.fixtures.mobile {
    IWorker,
    Animal,
    animalPlurals,
    IUnit
}
import strategicprimer.model.common.map.fixtures {
    Quantity
}
import strategicprimer.drivers.query {
    MammalModel,
    PoultryModel,
    SmallAnimalModel,
    HerdModel
}
import com.vasileff.ceylon.structures {
    MutableMultimap,
    HashMultimap
}
import ceylon.language.meta.model {
    ValueConstructor
}
import ceylon.collection {
    MutableMap,
    HashMap
}
import strategicprimer.model.common.idreg {
    IDRegistrar
}

import strategicprimer.viewer.drivers.turnrunning {
    ITurnRunningModel
}

service(`interface TurnAppletFactory`)
shared class HerdAppletFactory() satisfies TurnAppletFactory {
    shared actual TurnApplet create(ITurnRunningModel model, ICLIHelper cli, IDRegistrar idf) =>
        HerdApplet(model, cli, idf);
}

class HerdApplet(ITurnRunningModel model, ICLIHelper cli, IDRegistrar idf)
        extends AbstractTurnApplet(model, cli, idf) {
    shared actual [String+] commands = ["herd"];
    shared actual String description = "milk or gather eggs from animals";

    // TODO: Pull up to AbstractTurnApplet for use by other applets?
    IFortress? containingFortress(IUnit unit) =>
        model.map.fixtures.get(model.find(unit)).narrow<IFortress>()
            .find(matchingValue(unit.owner, IFortress.owner));

    MutableMap<String, HerdModel> herdModels = HashMap<String, HerdModel>();
    HerdModel? chooseHerdModel(String animal) => cli.chooseFromList(
        `MammalModel`.getValueConstructors().chain(`PoultryModel`.getValueConstructors())
            .chain(`SmallAnimalModel`.getValueConstructors())
            .narrow<ValueConstructor<HerdModel>>().map((model) => model.get()).sequence(),
        "What kind of animal(s) is/are ``animal``?", "No animal kinds found",
        "Kind of animal:", false).item;


    shared actual String? run() {
        assert (exists unit = model.selectedUnit);
        StringBuilder buffer = StringBuilder();
        IFortress? home = containingFortress(unit);
        for (kind in unit.narrow<Animal>().filter((animal) => ["domesticated", "tame"].contains(animal.status))
                .map(Animal.kind).distinct.filter(not(herdModels.keys.contains))) {
            if (exists herdModel = chooseHerdModel(kind)) {
                herdModels[kind] = herdModel;
            } else if (exists cont = cli.inputBoolean("Skip?"), cont) {
                continue;
            } else {
                cli.println("Aborting ...");
                return null;
            }
        }
        MutableMultimap<HerdModel, Animal> modelMap = HashMultimap<HerdModel, Animal>();
        for (group in unit.narrow<Animal>()
                .filter(or(matchingValue("tame", Animal.status),
                    matchingValue("domesticated", Animal.status)))) {
            if (exists herdModel = herdModels[group.kind]) {
                modelMap.put(herdModel, group);
            } else if (exists cont = cli.inputBoolean("No model for ``group.kind``. Really skip?"), cont) {
                continue;
            } else {
                cli.println("Aborting ...");
                return null;
            }
        }
        variable Integer workerCount = unit.narrow<IWorker>().size;
        if (exists addendum = cli.inputNumber(
                    "``workerCount`` workers in this unit. Any additional workers to account for:"),
                !addendum.negative) {
            workerCount += addendum;
        } else {
            return null;
        }
        Boolean experts = unit.narrow<IWorker>().map(shuffle(IWorker.getJob)("herder"))
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
                    return null;
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
            case (is SmallAnimalModel) {
                addToOrders("Tending the ");
                addToOrders(animalPlurals.get(combinedAnimal.kind));
                Integer baseCost;
                if (experts) {
                    baseCost = ((flockPerHerder * herdModel.dailyTimePerHead +
                    herdModel.dailyTimeFloor) * 0.9).integer;
                } else {
                    baseCost = flockPerHerder * herdModel.dailyTimePerHead +
                    herdModel.dailyTimeFloor;
                }
                minutesSpent += baseCost;
                addLineToOrders(" took the ``workerCount`` workers ``baseCost`` min.");
                switch (cli.inputBoolean("Is this the one turn in every ``
                herdModel.extraChoresInterval + 1`` to clean up after the animals?"))
                case (true) {
                    addToOrders("Cleaning up after them took ");
                    addToOrders((herdModel.extraTimePerHead * flockPerHerder).string);
                    addLineToOrders(" minutes.");
                    minutesSpent += herdModel.extraTimePerHead * flockPerHerder;
                }
                case (false) {}
                case (null) { return null; }
                continue;
            }
            addToOrders("This produced ");
            addToOrders(Float.format(production.floatNumber, 0, 1));
            addToOrders(" ``production.units``, ");
            addToOrders(Float.format(pounds, 0, 1));
            addLineToOrders(" lbs, of ``resourceProduced``.");
            if (exists home) {
                // FIXME: 'production' is in gallons; we want only pound-denominated food resources in the map
                // TODO: If 'home' is null, should probably add to the unit itself ...
                model.addResource(home, idf.createID(), "food", resourceProduced, production, model.map.currentTurn);
            }
        }
        addToOrders("In all, tending the animals took ``inHours(minutesSpent)``.");
        return buffer.string.trimmed;
    }
}
