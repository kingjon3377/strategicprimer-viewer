import model.misc {
    IDriverModel
}
import controller.map.misc {
    ICLIHelper
}
import java.io {
    IOException
}
import strategicprimer.viewer.drivers {
    DriverFailedException,
    DriverUsage,
    ParamCount,
    SimpleCLIDriver,
    IDriverUsage,
    SPOptions
}
import model.exploration {
    IExplorationModel,
    ExplorationModel
}
import ceylon.logging {
    logger,
    Logger
}
"A logger."
Logger log = logger(`module strategicprimer.viewer`);
"A CLI to help running exploration."
shared object explorationCLI satisfies SimpleCLIDriver {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = false;
        shortOption = "-x";
        longOption = "--explore";
        paramsWanted = ParamCount.atLeastOne;
        shortDescription = "Run exploration.";
        longDescription = "Move a unit around the map, updating the player's map with what it sees.";
        supportedOptionsTemp = [ "--current-turn=NN" ];
    };
    shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options, IDriverModel model) {
        IExplorationModel explorationModel;
        if (is IExplorationModel model) {
            explorationModel = model;
        } else {
            explorationModel = ExplorationModel(model);
        }
        try {
            ExplorationCLIHelper eCLI = ExplorationCLIHelper(explorationModel, cli);
            if (exists player = eCLI.choosePlayer(), exists unit = eCLI.chooseUnit(player)) {
                explorationModel.selectUnit(unit);
                eCLI.moveUntilDone();
            }
        } catch (IOException except) {
            throw DriverFailedException(except, "I/O error interacting with user");
        }
    }
}
