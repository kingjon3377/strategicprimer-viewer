import strategicprimer.drivers.common {
    IDriverModel,
    DriverUsage,
    ParamCount,
    IDriverUsage,
    SPOptions,
    CLIDriver,
    ModelDriver,
    DriverFactory,
    ModelDriverFactory,
    IWorkerModel
}
import strategicprimer.drivers.common.cli {
    ICLIHelper
}
import strategicprimer.model.common.map {
    Player,
    IMutableMapNG
}
import strategicprimer.drivers.worker.common {
    WorkerModel
}
import ceylon.logging {
    logger,
    Logger
}
import strategicprimer.model.common.map.fixtures.mobile {
    IWorker,
    IUnit
}
import ceylon.collection {
    ArrayList,
    MutableList
}
import lovelace.util.common {
    PathWrapper
}

"A logger."
Logger log = logger(`module strategicprimer.viewer`);

"A factory for the worker-advancement CLI driver."
service(`interface DriverFactory`)
shared class AdvancementCLIFactory() satisfies ModelDriverFactory {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = false;
        invocation = "advance";
        paramsWanted = ParamCount.atLeastOne;
        shortDescription = "View a player's workers and manage their advancement";
        longDescription = """View a player's units, the workers in those units, each
                             worker's Jobs, and his or her level in each Skill in each
                             Job.""";
        includeInCLIList = true;
        includeInGUIList = false;
        supportedOptions = [ "--current-turn=NN", "--allow-expert-mentoring" ];
    };

    shared actual ModelDriver createDriver(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        if (is IWorkerModel model) {
            return AdvancementCLI(cli, options, model);
        } else {
            return createDriver(cli, options, WorkerModel.copyConstructor(model));
        }
    }

    shared actual IDriverModel createModel(IMutableMapNG map, PathWrapper? path) =>
            WorkerModel(map, path);
}

"The worker-advancement CLI driver."
shared class AdvancementCLI(ICLIHelper cli, SPOptions options, model)
        satisfies CLIDriver {
    shared actual IWorkerModel model;

    AdvancementCLIHelper helper = AdvancementCLIHelper(cli);

    "Let the user add experience to a player's workers."
    void advanceWorkers(IWorkerModel model, Player player, Boolean allowExpertMentoring) {
        MutableList<IUnit> units = ArrayList {
            elements = model.getUnits(player).filter(
                        (unit) => !unit.narrow<IWorker>().empty); };
        while (!units.empty, exists chosen = cli.chooseFromList(units,
                "``player.name``'s units:", "No unadvanced units remain.", "Chosen unit:",
                false).item) {
            units.remove(chosen);
            helper.advanceWorkersInUnit(chosen, allowExpertMentoring);
            if (exists continuation = cli.inputBoolean("Choose another unit?"),
                    continuation) {
                // continue;
            } else {
                break;
            }
        }
        for (map->_ in model.allMaps) {
            model.setModifiedFlag(map, true);
        }
    }

    "Let the user choose a player to run worker advancement for."
    shared actual void startDriver() {
        MutableList<Player> playerList = ArrayList { elements = model.players; };
        while (!playerList.empty, exists chosen = cli.chooseFromList(playerList,
                "Available players:", "No players found.", "Chosen player:",
                false).item) {
            playerList.remove(chosen);
            advanceWorkers(model, chosen,
                options.hasOption("--allow-expert-mentoring"));
            if (exists continuation = cli.inputBoolean("Select another player?"),
                    continuation) {
                // continue;
            } else {
                break;
            }
        }
    }
}
