import strategicprimer.model.common.idreg {
    IDRegistrar
}
import strategicprimer.drivers.common.cli {
    ICLIHelper
}
import strategicprimer.drivers.exploration.common {
    HuntingModel
}
import strategicprimer.model.common.map {
    Point
}
import strategicprimer.model.common.map.fixtures.resources {
    Meadow,
    Shrub,
    Grove
}
import strategicprimer.viewer.drivers.resourceadding {
    ResourceAddingCLIHelper
}

import strategicprimer.viewer.drivers.turnrunning {
    ITurnRunningModel
}

service(`interface TurnAppletFactory`)
shared class GatherAppletFactory() satisfies TurnAppletFactory {
    shared actual TurnApplet create(ITurnRunningModel model, ICLIHelper cli, IDRegistrar idf) =>
        GatherApplet(model, cli, idf);
}

class GatherApplet(ITurnRunningModel model, ICLIHelper cli, IDRegistrar idf)
        extends AbstractTurnApplet(model, cli, idf) {
    HuntingModel huntingModel = HuntingModel(model.map);
    ResourceAddingCLIHelper resourceAddingHelper = ResourceAddingCLIHelper(cli, idf);

    shared actual [String+] commands = ["gather"];

    shared actual String description = "gather vegetation from surrounding area";

    "If argument is a meadow, its status in the format used below; otherwise the empty
     string."
    String meadowStatus(Anything argument) {
        if (is Meadow argument) {
            return " (``argument.status``)";
        } else {
            return "";
        }
    }

    String toHours(Integer minutes) {
        if (minutes.negative) {
            return "negative " + toHours(minutes.negated);
        } else if (minutes.zero) {
            return "no time";
        } else if (minutes == 1) {
            return "1 minute";
        } else if (minutes < 60) {
            return minutes.string + " minutes";
        } else if (minutes == 60) {
            return "1 hour";
        } else if (minutes < 120) {
            return "1 hour, " + toHours(minutes.modulo(60));
        } else if (60.divides(minutes)) {
            return (minutes / 60).string + " hours";
        } else {
            return (minutes / 60).string + " hours, " + toHours(minutes.modulo(60));
        }
    }

    shared actual String? run() {
        StringBuilder buffer = StringBuilder();
        if (exists center = confirmPoint("Location to search around: "),
                exists startingTime = cli.inputNumber("Minutes to spend gathering: ")) {
            variable Integer time = startingTime;
            variable {<Point->Grove|Shrub|Meadow|HuntingModel.NothingFound>*} encounters =
                huntingModel.gather(center);
            variable Integer noResultsTime = 0;
            while (time > 0, exists loc->find = encounters.first) {
                encounters = encounters.rest;
                if (is HuntingModel.NothingFound find) {
                    noResultsTime += noResultCost;
                    time -= noResultCost;
                } else {
                    if (noResultsTime.positive) {
                        cli.println("Found nothing for the next ``toHours(noResultsTime)``"); // TODO: Add to results?
                        noResultsTime = 0;
                    }
                    switch (cli.inputBooleanInSeries(
                        "Gather from ``find.shortDescription````meadowStatus(find)``?",
                        find.kind))
                    case (true) {
                        if (exists unit = model.selectedUnit) {
                            cli.println(
                                "Enter details of harvest (any empty string aborts):");
                            while (exists resource =
                                    resourceAddingHelper.enterResource()) {
                                if (resource.kind == "food") {
                                    resource.created = model.map.currentTurn;
                                }
                                if (!model.addExistingResource(resource, unit.owner)) {
                                    cli.println("Failed to find a fortress to add to in any map");
                                }
                            }
                        }
                        Integer cost = cli.inputNumber("Time to gather: ")
                        else runtime.maxArraySize;
                        time -= cost;
                        // TODO: Once model supports remaining-quantity-in-fields data, offer to reduce it here
                        if (is Shrub find, find.population>0) {
                            switch (cli.inputBooleanInSeries(
                                "Reduce shrub population here?"))
                            case (true) {
                                reducePopulation(loc, find, "plants", true);
                                cli.print(inHours(time));
                                cli.println("remaining.");
                                continue;
                            }
                            case (false) {}
                            case (null) { return null; }
                        }
                        cli.print(inHours(time));
                        cli.println(" remaining.");
                    }
                    case (false) { time -= noResultCost; }
                    case (null) { return null; }
                    model.copyToSubMaps(loc, find, true);
                }
                if (exists addendum = cli.inputMultilineString(
                        "Add to results about that:")) {
                    buffer.append(addendum);
                } else {
                    return null;
                }
            }
            if (noResultsTime.positive) {
                cli.println("Found nothing for the next ``toHours(noResultsTime)``"); // TODO: Add to results?
            }
        }
        return buffer.string.trimmed;
    }
}
