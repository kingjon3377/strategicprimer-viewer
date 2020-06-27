import strategicprimer.model.common.map {
    Point
}
import strategicprimer.drivers.exploration.common {
    HuntingModel,
    IExplorationModel
}
import ceylon.numeric.float {
    round=halfEven
}
import strategicprimer.model.common.map.fixtures.mobile {
    AnimalTracks,
    Animal,
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
abstract class HuntGeneralApplet(String verb, IExplorationModel model, ICLIHelper cli, IDRegistrar idf)
        of HuntingApplet|FishingApplet extends AbstractTurnApplet(model, cli, idf) {
    shared HuntingModel huntingModel = HuntingModel(model.map);
    ResourceAddingCLIHelper resourceAddingHelper = ResourceAddingCLIHelper(cli, idf);

    "A description of what could be a single animal or a population of animals."
    String populationDescription(Animal animal) {
        if (animal.population > 1) {
            return "a group of perhaps ``animal.population
//              `` ``animalPlurals[animal.kind]``"; // TODO: syntax sugar
            `` ``animalPlurals.get(animal.kind)``";
        } else {
            return animal.kind;
        }
    }

    Integer? handleEncounter(StringBuilder buffer, Integer time, Point loc,
            Animal|AnimalTracks|HuntingModel.NothingFound find) {
        if (is HuntingModel.NothingFound find) {
            cli.println("Found nothing for the next ``noResultCost`` minutes.");
            return noResultCost;
        } else if (is AnimalTracks find) {
            addToSubMaps(loc, find, true);
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
                variable Integer cost = cli.inputNumber("Time to ``verb``: ")
                else runtime.maxArraySize;
                Boolean? processNow =
                    cli.inputBooleanInSeries("Handle processing now?");
                if (is Null processNow) {
                    return null;
                } else if (processNow) {
                    // TODO: somehow handle processing-in-parallel case
                    for (i in 0:(cli.inputNumber("How many animals?") else 0)) {
                        Integer mass = cli.inputNumber(
                            "Weight of this animal's meat in pounds: ")
                        else runtime.maxArraySize;
                        Integer hands = cli.inputNumber(
                            "# of workers processing this carcass: ") else 1;
                        cost += round(HuntingModel.processingTime(mass) / hands)
                            .integer;
                    }
                }
                switch (cli.inputBooleanInSeries(
                    "Reduce animal group population of ``find.population``?"))
                // FIXME: Support capturing animals
                case (true) { reducePopulation(loc, find, "animals", true); }
                case (false) { addToSubMaps(loc, find, true); }
                case (null) { return null; }
                cli.print(inHours(time));
                cli.println(" remaining.");
                if (exists unit = model.selectedUnit) {
                    cli.println(
                        "Enter resources produced (any empty string aborts):");
                    while (exists resource =
                        resourceAddingHelper.enterResource()) {
                        if (resource.kind == "food") {
                            resource.created = model.map.currentTurn;
                        }
                        addResourceToMaps(resource, unit.owner);
                    }
                }
                return cost;
            } else {
                addToSubMaps(loc, find, true);
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
    shared actual TurnApplet create(IExplorationModel model, ICLIHelper cli, IDRegistrar idf) =>
        HuntingApplet(model, cli, idf);
}

class HuntingApplet(IExplorationModel model, ICLIHelper cli, IDRegistrar idf)
        extends HuntGeneralApplet("fight and process", model, cli, idf) {
    shared actual String description = "search for wild animals";
    shared actual [String+] commands = ["hunt"];
    shared actual String? run() => impl("hunt", huntingModel.hunt);
}

service(`interface TurnAppletFactory`)
shared class FishingAppletFactory() satisfies TurnAppletFactory {
    shared actual TurnApplet create(IExplorationModel model, ICLIHelper cli, IDRegistrar idf) =>
        FishingApplet(model, cli, idf);
}

class FishingApplet(IExplorationModel model, ICLIHelper cli, IDRegistrar idf)
        extends HuntGeneralApplet("try to catch and process", model, cli, idf) {
    shared actual String description = "search for aquatic animals";
    shared actual [String+] commands = ["fish"];
    shared actual String? run() => impl("fish", huntingModel.fish);
}
