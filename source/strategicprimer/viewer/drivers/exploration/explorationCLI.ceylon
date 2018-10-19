import ceylon.logging {
    logger,
    Logger
}
import strategicprimer.drivers.common {
    DriverUsage,
    ParamCount,
    IDriverUsage,
    SPOptions,
    IDriverModel,
    CLIDriver,
    DriverFactory,
    ModelDriverFactory,
    ModelDriver
}
import strategicprimer.drivers.common.cli {
    ICLIHelper
}
import strategicprimer.drivers.exploration.common {
    IExplorationModel,
    ExplorationModel
}
import lovelace.util.common {
    PathWrapper
}
import strategicprimer.model.common.map {
    IMutableMapNG
}

"A logger."
Logger log = logger(`module strategicprimer.viewer`);

"A factory for the CLI exploration app."
service(`interface DriverFactory`)
shared class ExplorationCLIFactory() satisfies ModelDriverFactory {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = false;
        invocations = ["-x", "--explore"];
        paramsWanted = ParamCount.atLeastOne;
        shortDescription = "Run exploration.";
        longDescription = "Move a unit around the map, updating the player's map with
                           what it sees.";
        includeInCLIList = true;
        includeInGUIList = false;
        supportedOptions = [ "--current-turn=NN" ];
    };

    shared actual ModelDriver createDriver(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        if (is IExplorationModel model) {
            return ExplorationCLI(cli, model);
        } else {
            return createDriver(cli, options, ExplorationModel.copyConstructor(model));
        }
    }

    shared actual IDriverModel createModel(IMutableMapNG map, PathWrapper? path) =>
            ExplorationModel(map, path);

}

"A CLI to help running exploration."
class ExplorationCLI(ICLIHelper cli, model) satisfies CLIDriver {
    shared actual IExplorationModel model;
    shared actual void startDriver() {
        ExplorationCLIHelper eCLI = ExplorationCLIHelper(model, cli);
        if (exists player = eCLI.choosePlayer(),
                exists unit = eCLI.chooseUnit(player)) {
            model.selectedUnit = unit;
            eCLI.moveUntilDone();
        }
    }
}
