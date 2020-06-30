import strategicprimer.model.common.idreg {
    IDRegistrar
}
import strategicprimer.drivers.common.cli {
    ICLIHelper
}
import strategicprimer.drivers.exploration.common {
    IExplorationModel,
    HuntingModel
}
import lovelace.util.common {
    matchingValue
}
import strategicprimer.model.common.map {
    Point
}
import ceylon.numeric.float {
    round=halfEven
}
import strategicprimer.model.common.map.fixtures.mobile {
    AnimalTracks,
    Animal
}
import strategicprimer.viewer.drivers.resourceadding {
    ResourceAddingCLIHelper
}

service(`interface TurnAppletFactory`)
shared class TrappingAppletFactory() satisfies TurnAppletFactory {
    shared actual TurnApplet create(IExplorationModel model, ICLIHelper cli, IDRegistrar idf) =>
        TrappingApplet(model, cli, idf);
}
class TrappingApplet(IExplorationModel model, ICLIHelper cli, IDRegistrar idf)
        extends HuntGeneralApplet("trap", model, cli, idf) {
    shared actual [String+] commands = ["trap"];
    shared actual String description = "check traps for animals or fish they may have caught";
    TrapperCommand[] trapperCommands = sort(`TrapperCommand`.caseValues);
    ResourceAddingCLIHelper resourceAddingHelper = ResourceAddingCLIHelper(cli, idf);

    Integer? handleFound(Point center, Point loc, Animal item) {
        variable Integer cost;
        cli.println("Found either ``item.kind`` or evidence of it escaping.");
        if (exists num = cli.inputNumber("How long to check and deal with the animal? ")) {
            cost = num;
        } else {
            return null;
        }
        switch (cli.inputBooleanInSeries("Is an animal captured live?"))
        case (true) {
            if (handleCapture(item) is Null) {
                return null;
            }
        }
        case (false) {}
        case (null) { return null; }
        switch (cli.inputBooleanInSeries("Handle processing now?"))
        case (true) {
            if (exists processingTime = processMeat()) {
                cost += processingTime;
            } else {
                return null;
            }
        }
        case (false) { }
        case (null) { return null; }
        switch (cli.inputBooleanInSeries(
            "Reduce animal group population of ``item.population``?"))
        case (true) {
            if (exists delenda = cli.inputNumber("Animals to remove: ")) {
                Integer count = Integer.smallest(delenda, item.population);
                if (count > 0) {
                    for (map in model.allMaps.map(Entry.key)) {
                        if (exists population = map.fixtures.get(loc).narrow<Animal>()
                                .find(matchingValue(item.id, Animal.id)), population.population > 0) {
                            map.removeFixture(loc, population);
                            Integer remaining = population.population - count;
                            if (remaining > 0) {
                                map.addFixture(loc, population.reduced(remaining));
                            }
                        }
                    }
                    if (model.map.fixtures.get(loc).narrow<Animal>()
                            .any(matchingValue(item.id, Animal.id))) {
                        addToSubMaps(center, AnimalTracks(item.kind), false);
                    }
                }
            }
        } case (false) {
            addToSubMaps(center, AnimalTracks(item.kind), false);
        }
        case (null) {
            return null;
        }
        if (exists unit = model.selectedUnit) {
            cli.println("Enter resources produced (any empty string aborts):");
            while (exists resource = resourceAddingHelper.enterResource()) {
                if (resource.kind == "food") {
                    resource.created = model.map.currentTurn;
                }
                addResourceToMaps(resource, unit.owner);
            }
        }
        return cost;
    }

    shared actual String? run() {
        StringBuilder buffer = StringBuilder();
        if (exists fishing = cli.inputBooleanInSeries(
                    "Is this a fisherman trapping fish rather than a trapper?"),
                exists center = confirmPoint("Location to search around: "),
                exists startingTime = cli.inputNumber("Minutes to spend working: ")) {
            variable {<Point->Animal|AnimalTracks|HuntingModel.NothingFound>*} encounters;
            String prompt;
            Integer nothingCost;
            if (fishing) {
                encounters = huntingModel.fish(center);
                prompt = "What should the fisherman do next?";
                nothingCost = 5;
            } else {
                encounters = huntingModel.hunt(center);
                prompt = "What should the trapper do next?";
                nothingCost = 10;
            }
            variable Integer time = startingTime;
            while (time > 0, exists command = cli.chooseFromList(trapperCommands, prompt,
                    "Oops! No commands", "Next action: ", false).item,
                    command != TrapperCommand.quit) {
                switch (command)
                case (TrapperCommand.check) {
                    value find = encounters.first;
                    if (!find exists) {
                        cli.println("Ran out of results!");
                        break;
                    }
                    assert (exists loc->item = find);
                    if (is HuntingModel.NothingFound item) {
                        cli.println("Nothing in the trap");
                        time -= nothingCost;
                    } else if (is AnimalTracks item) {
                        cli.println("Found evidence of ``item.kind`` escaping");
                        addToSubMaps(center, item, true);
                        time -= nothingCost;
                    } else if (exists cost = handleFound(center, loc, item)) {
                        time -= cost;
                    } else {
                        return null;
                    }
                }
                case (TrapperCommand.easyReset) {
                    if (fishing) {
                        time -= 20;
                    } else {
                        time -= 5;
                    }
                }
                case (TrapperCommand.move) { time -= 2; }
                case (TrapperCommand.quit) { time = 0; }
                case (TrapperCommand.setTrap) {
                    if (fishing) {
                        time -= 30;
                    } else {
                        time -= 45;
                    }
                }
                cli.print(inHours(time));
                cli.println(" remaining.");
                if (exists addendum = cli.inputMultilineString(
                        "Add to results about that:")) {
                    buffer.append(addendum);
                } else {
                    return null;
                }
            }
        }
        return buffer.string;
    }
}
