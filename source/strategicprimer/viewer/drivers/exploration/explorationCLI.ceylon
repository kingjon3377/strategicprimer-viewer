import java.io {
    IOException
}
import ceylon.logging {
    logger,
    Logger
}
import strategicprimer.drivers.common {
    DriverFailedException,
    DriverUsage,
    ParamCount,
    SimpleCLIDriver,
    IDriverUsage,
    SPOptions,
    IDriverModel,
	ISPDriver
}
import strategicprimer.drivers.common.cli {
    ICLIHelper
}
import strategicprimer.drivers.exploration.common {
    IExplorationModel,
    ExplorationModel
}
"A logger."
Logger log = logger(`module strategicprimer.viewer`);
"A CLI to help running exploration."
service(`interface ISPDriver`)
shared class ExplorationCLI() satisfies SimpleCLIDriver {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = false;
        invocations = ["-x", "--explore"];
        paramsWanted = ParamCount.atLeastOne;
        shortDescription = "Run exploration.";
        longDescription = "Move a unit around the map, updating the player's map with
                           what it sees.";
        supportedOptionsTemp = [ "--current-turn=NN" ];
    };
    shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        IExplorationModel explorationModel;
        if (is IExplorationModel model) {
            explorationModel = model;
        } else {
            explorationModel = ExplorationModel.copyConstructor(model);
        }
        try {
            ExplorationCLIHelper eCLI = ExplorationCLIHelper(explorationModel, cli);
            if (exists player = eCLI.choosePlayer(),
                    exists unit = eCLI.chooseUnit(player)) {
                explorationModel.selectedUnit = unit;
                eCLI.moveUntilDone();
            }
        } catch (IOException except) {
            throw DriverFailedException(except, "I/O error interacting with user");
        }
    }
}
