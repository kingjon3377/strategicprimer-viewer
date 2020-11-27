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

    shared actual String? run() {
        StringBuilder buffer = StringBuilder();
        if (exists center = confirmPoint("Location to search around: "),
            exists startingTime = cli.inputNumber("Minutes to spend gathering: ")) {
            variable Integer time = startingTime;
            variable {<Point->Grove|Shrub|Meadow|HuntingModel.NothingFound>*} encounters =
                huntingModel.gather(center);
            while (time > 0, exists loc->find = encounters.first) {
                encounters = encounters.rest;
                if (is HuntingModel.NothingFound find) { // TODO: We'd like to combine consecutive 'no result' results before telling the user about them.
                    cli.println("Found nothing for the next ``noResultCost`` minutes.");
                    time -= noResultCost;
                } else {
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
                                addResourceToMaps(resource, unit.owner);
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
        }
        return buffer.string.trimmed;
    }
}
