import controller.map.drivers {
    SimpleCLIDriver,
    DriverUsage,
    ParamCount,
    IDriverUsage,
    SPOptions,
    DriverFailedException
}
import controller.map.misc {
    ICLIHelper
}
import model.misc {
    IDriverModel
}
import model.exploration {
    IExplorationModel,
    ExplorationModel
}
import java.io {
    IOException
}
import view.exploration {
    ExplorationCLI
}
"A CLI to help running exploration."
object explorationCLI satisfies SimpleCLIDriver {
    DriverUsage usageObject = DriverUsage(false, "-x", "--explore", ParamCount.atLeastOne,
        "Run exploration.",
        "Move a unit around the map, updating the player's map with what it sees.");
    usageObject.addSupportedOption("--current-turn=NN");
    shared actual IDriverUsage usage() => usageObject;
    shared actual void startDriver(ICLIHelper cli, SPOptions options, IDriverModel model) {
        IExplorationModel explorationModel;
        if (is IExplorationModel model) {
            explorationModel = model;
        } else {
            explorationModel = ExplorationModel(model);
        }
        try {
            ExplorationCLI eCLI = ExplorationCLI(explorationModel, cli);
            if (exists player = eCLI.choosePlayer(), exists unit = eCLI.chooseUnit(player)) {
                explorationModel.selectUnit(unit);
                eCLI.moveUntilDone();
            }
        } catch (IOException except) {
            throw DriverFailedException("I/O error interacting with user", except);
        }
    }
}