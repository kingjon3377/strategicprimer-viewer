import strategicprimer.model.common.map {
    Player,
    Point
}
import strategicprimer.drivers.exploration.common {
    HuntingModel
}
import ceylon.numeric.float {
    round=halfEven
}
import strategicprimer.model.common.map.fixtures.mobile {
    AnimalTracks,
    Animal,
    IUnit,
    animalPlurals
}
import strategicprimer.drivers.common.cli {
    ICLIHelper
}
import strategicprimer.model.common.idreg {
    IDRegistrar
}
import strategicprimer.viewer.drivers.resourceadding {
    ResourceAddingCLIHelper
}

import strategicprimer.viewer.drivers.turnrunning {
    ITurnRunningModel
}

abstract class HuntGeneralApplet(String verb, ITurnRunningModel model, ICLIHelper cli, IDRegistrar idf)
        of HuntingApplet|FishingApplet|TrappingApplet extends AbstractTurnApplet(model, cli, idf) {
    shared HuntingModel huntingModel = HuntingModel(model.map);
    ResourceAddingCLIHelper resourceAddingHelper = ResourceAddingCLIHelper(cli, idf);

    "A description of what could be a single animal or a population of animals."
    String populationDescription(Animal animal) {
        if (animal.population > 1) {
            return "a group of perhaps ``animal.population
                `` ``animalPlurals.get(animal.kind)``"; // TODO: syntax sugar
        } else {
            return animal.kind;
        }
    }

    String describeUnit(IUnit unit) => "``unit.name`` (``unit.kind``)";

    shared Boolean? handleCapture(Animal find) {
        if (exists unit = chooseFromList(
                    model.getUnits(model.selectedUnit?.owner else model.map.currentPlayer)
                .narrow<IUnit>().sequence(),
                "Available Units:", "No units", "Unit to add animals to:", false, describeUnit)) {
            if (exists num = cli.inputNumber("Number captured:")) {
                return model.addAnimal(unit, find.kind, "wild", idf.createID(), num);
            } else {
                return null;
            }
        } else {
            return false;
        }
    }

    shared Integer? processMeat() {
        variable Integer cost = 0;
        // TODO: somehow handle processing-in-parallel case
        value iterations = cli.inputNumber("How many carcasses?");
        if (is Null iterations) {
            return null;
        }
        for (i in 0:iterations) {
            if (exists mass = cli.inputNumber("Weight of this animal's meat in pounds: "),
                    exists hands = cli.inputNumber("# of workers processing this carcass: ")) {
                cost += round(HuntingModel.processingTime(mass) / hands).integer;
            } else {
                return null;
            }
        }
        return cost;
    }

    shared void resourceEntry(Player owner) {
        cli.println("Enter resources produced (any empty string aborts):");
        while (exists resource = resourceAddingHelper.enterResource()) {
            if (resource.kind == "food") {
                resource.created = model.map.currentTurn;
            }
            addResourceToMaps(resource, owner);
        }
    }

    Integer? handleFight(Point loc, Animal find, Integer time) {
        variable Integer cost;
        if (exists temp = cli.inputNumber("Time to ``verb``: ")) {
            cost = temp;
        } else {
            return null;
        }
        Boolean? capture = cli.inputBooleanInSeries("Capture any animals?");
        if (is Null capture) {
            return null;
        } else if (capture) {
            if (!handleCapture(find) exists) {
                return null;
            }
        }
        Boolean? processNow =
            cli.inputBooleanInSeries("Process carcasses now?");
        if (is Null processNow) {
            return null;
        } else if (processNow) {
            if (exists processingTime = processMeat()) {
                cost += processingTime;
            } else {
                return null;
            }
        }
        switch (cli.inputBooleanInSeries(
            "Reduce animal group population of ``find.population``?"))
        case (true) { reducePopulation(loc, find, "animals", true); }
        case (false) { model.copyToSubMaps(loc, find, true); }
        case (null) { return null; }
        if (exists unit = model.selectedUnit) {
            resourceEntry(unit.owner);
        }
        return cost;
    }

    Integer? handleEncounter(StringBuilder buffer, Integer time, Point loc,
            Animal|AnimalTracks|HuntingModel.NothingFound find) {
        if (is HuntingModel.NothingFound find) {
            cli.println("Found nothing for the next ``noResultCost`` minutes.");
            return noResultCost;
        } else if (is AnimalTracks find) {
            model.copyToSubMaps(loc, find, true);
            cli.println("Found only tracks or traces from ``
                find.kind`` for the next ``noResultCost`` minutes.");
            return noResultCost;
        } else {
            Boolean? fight = cli.inputBooleanInSeries("Found ``
                populationDescription(find)``. Should they ``verb``?",
                    find.kind);
            if (is Null fight) {
                return null;
            } else if (fight) {
                return handleFight(loc, find, time);
            } else {
                model.copyToSubMaps(loc, find, true);
                return noResultCost;
            }
        }
    }

    // TODO: Distinguish hunting from fishing in no-result time cost (encounters / hour)?
    shared String? impl(String command,
            {<Point->Animal|AnimalTracks|HuntingModel.NothingFound>*}(Point) encounterSrc) {
        StringBuilder buffer = StringBuilder();
        if (exists center = confirmPoint("Location to search around: "),
            exists startingTime = cli
                .inputNumber("Minutes to spend ``command``ing: ")) {
            variable Integer time = startingTime;
            variable {<Point->Animal|AnimalTracks|HuntingModel.NothingFound>*} encounters
                = encounterSrc(center);
            while (time > 0, exists loc->find = encounters.first) {
                cli.print(inHours(time));
                cli.println(" remaining.");
                encounters = encounters.rest;
                if (exists cost = handleEncounter(buffer, time, loc, find)) {
                    time -= cost;
                } else {
                    return null;
                }
                if (exists addendum = cli.inputMultilineString(
                        "Add to results about that:")) {
                    buffer.append(addendum);
                } else {
                    return null;
                }
            }
        }
        return buffer.string.trimmed;
    }
}

service(`interface TurnAppletFactory`)
shared class HuntingAppletFactory() satisfies TurnAppletFactory {
    shared actual TurnApplet create(ITurnRunningModel model, ICLIHelper cli, IDRegistrar idf) =>
        HuntingApplet(model, cli, idf);
}

class HuntingApplet(ITurnRunningModel model, ICLIHelper cli, IDRegistrar idf)
        extends HuntGeneralApplet("fight and process", model, cli, idf) {
    shared actual String description = "search for wild animals";
    shared actual [String+] commands = ["hunt"];
    shared actual String? run() => impl("hunt", huntingModel.hunt);
}

service(`interface TurnAppletFactory`)
shared class FishingAppletFactory() satisfies TurnAppletFactory {
    shared actual TurnApplet create(ITurnRunningModel model, ICLIHelper cli, IDRegistrar idf) =>
        FishingApplet(model, cli, idf);
}

class FishingApplet(ITurnRunningModel model, ICLIHelper cli, IDRegistrar idf)
        extends HuntGeneralApplet("try to catch and process", model, cli, idf) {
    shared actual String description = "search for aquatic animals";
    shared actual [String+] commands = ["fish"];
    shared actual String? run() => impl("fish", huntingModel.fish);
}
