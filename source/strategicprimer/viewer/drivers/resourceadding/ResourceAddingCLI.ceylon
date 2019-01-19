import strategicprimer.drivers.common.cli {
    ICLIHelper
}
import lovelace.util.common {
    todo
}
import strategicprimer.model.common.idreg {
    createIDFactory
}
import strategicprimer.model.common.map {
    Player
}
import strategicprimer.drivers.common {
    SPOptions,
    CLIDriver
}
import ceylon.collection {
    ArrayList,
    MutableList
}

"A driver to let the user enter a player's resources and equipment."
class ResourceAddingCLI(ICLIHelper cli, SPOptions options, model) satisfies CLIDriver {
    shared actual ResourceManagementDriverModel model;

    ResourceAddingCLIHelper helper = ResourceAddingCLIHelper(cli,
        createIDFactory(model.allMaps.map(Entry.key)));
    todo("Add a loopOnPlayers() helper method to CLIDriver interface, since there are
          several disparate CLI drivers that do that.")
    shared actual void startDriver() {
        MutableList<Player> players = ArrayList { elements = model.players; };
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
                        if (exists resource = helper.enterResource()) {
                            model.addResource(resource, chosen);
                            continue;
                        } else {
                            return;
                        }
                    }
                    case (false) {}
                    case (null) { return; }
                    switch (cli.inputBooleanInSeries(
                        "Enter equipment etc.? "))
                    case (true) {
                        if (exists implement = helper.enterImplement()) {
                            model.addResource(implement, chosen);
                        } else {
                            return;
                        }
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
