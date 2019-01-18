import strategicprimer.drivers.common.cli {
    ICLIHelper
}
import lovelace.util.common {
    todo
}
import strategicprimer.model.common.idreg {
    IDRegistrar,
    createIDFactory
}
import strategicprimer.model.common.map {
    Player
}
import strategicprimer.model.common.map.fixtures {
    Quantity,
    ResourcePile,
    Implement
}
import strategicprimer.drivers.common {
    SPOptions,
    CLIDriver
}
import com.vasileff.ceylon.structures {
    MutableMultimap,
    HashMultimap
}
import ceylon.collection {
    MutableSet,
    ArrayList,
    MutableMap,
    MutableList,
    HashSet,
    HashMap
}

"A driver to let the user enter a player's resources and equipment."
class ResourceAddingCLI(ICLIHelper cli, SPOptions options, model) satisfies CLIDriver {
    shared actual ResourceManagementDriverModel model;

    MutableSet<String> resourceKinds = HashSet<String>();
    MutableMultimap<String, String> resourceContents =
        HashMultimap<String, String>();
    MutableMap<String, String> resourceUnits = HashMap<String, String>();

    "Ask the user to choose or enter a resource kind. Returns [[null]] on EOF."
    String? getResourceKind() {
        String[] list = resourceKinds.sequence();
        value choice = cli.chooseStringFromList(list, "Possible kinds of resources:",
            "No resource kinds entered yet", "Chosen kind: ", false);
        if (exists retval = choice.item) {
            return retval;
        } else if (exists retval = cli.inputString("Resource kind to use: ")) {
            resourceKinds.add(retval);
            return retval;
        } else {
            return null;
        }
    }

    "Ask the user to choose or enter a resource-content-type for a given resource kind.
     Returns [[null]] on EOF."
    String? getResourceContents(String kind) {
        String[] list = resourceContents.get(kind).sequence();
        value num->item = cli.chooseStringFromList(list,
            "Possible resources in the ``kind`` category`", "No resources entered yet",
            "Choose resource: ", false);
        if (exists item) {
            return item;
        } else if (exists retval = cli.inputString("Resource to use: ")) {
            resourceContents.put(kind, retval);
            return retval;
        } else {
            return null;
        }
    }

    "Ask the user to choose units for a type of resource. Returns [[null]] on EOF."
    String? getResourceUnits(String resource) {
        if (exists unit = resourceUnits[resource]) {
            switch (cli.inputBooleanInSeries(
                "Is ``unit`` the correct unit for ``resource``? ",
                "correct;``unit``;``resource``"))
            case (true) { return unit; }
            case (null) { return null; }
            case (false) {}
        }
        if (exists retval = cli.inputString("Unit to use for ``resource``: ")) {
            resourceUnits[resource] = retval;
            return retval;
        } else {
            return null;
        }
    }

    "Ask the user to enter a resource."
    void enterResource(IDRegistrar idf, Player player) {
        if (exists kind = getResourceKind(),
            exists origContents = getResourceContents(kind),
            exists units = getResourceUnits(origContents),
            exists usePrefix = cli.inputBooleanInSeries(
                "Qualify the particular resource with a prefix?",
                "prefix " + origContents)) {
            String contents;
            if (usePrefix) {
                if (exists prefix = cli.inputString("Prefix to use: ")) {
                    contents = prefix + " " + origContents;
                } else {
                    return;
                }
            } else {
                contents = origContents;
            }
            if (exists quantity = cli.inputDecimal("Quantity in ``units``?")) {
                model.addResource(ResourcePile(idf.createID(), kind, contents, Quantity(
                    quantity, units)), player);
            }
        }
    }

    "Ask the user to enter an Implement (a piece of equipment)"
    void enterImplement(IDRegistrar idf, Player player) {
        if (exists kind = cli.inputString("Kind of equipment: "),
            exists multiple = cli.inputBooleanInSeries("Add more than one? ")) {
            Integer count;
            if (multiple) {
                if (exists temp = cli.inputNumber("Number to add: ")) {
                    count = temp;
                } else {
                    return;
                }
            } else {
                count = 1;
            }
            model.addResource(Implement(kind, idf.createID(), count), player);
        }
    }

    todo("Add a loopOnPlayers() helper method to CLIDriver interface, since there are
          several disparate CLI drivers that do that.")
    shared actual void startDriver() {
        MutableList<Player> players = ArrayList { elements = model.players; };
        IDRegistrar idf = createIDFactory(model.allMaps.map(Entry.key));
        while (!players.empty, exists chosen = cli.chooseFromList(players,
            "Players in the maps:", "No players found.",
            "Player to add resources for: ", false).item) {
            players.remove(chosen);
            while (true) {
                switch (cli.inputBoolean("Keep going? "))
                case (true) {
                    switch (cli.inputBooleanInSeries(
                        "Enter a (quantified) resource? "))
                    case (true) {
                        enterResource(idf, chosen);
                        continue;
                    }
                    case (false) {}
                    case (null) { return; }
                    switch (cli.inputBooleanInSeries(
                        "Enter equipment etc.? "))
                    case (true) {
                        enterImplement(idf, chosen);
                    }
                    case (false) {}
                    case (null) { return; }
                }
                case (null) {
                    return;
                }
                case (false) {
                    break;
                }
            }
            Boolean? continuation = cli.inputBoolean("Choose another player?");
            if (exists continuation, continuation) {
                // continue;
            } else {
                break;
            }
        }
    }
}
