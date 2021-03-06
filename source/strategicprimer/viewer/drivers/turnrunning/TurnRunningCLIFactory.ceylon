import strategicprimer.drivers.common {
    DriverFactory,
    ModelDriverFactory,
    DriverUsage,
    IDriverUsage,
    ParamCount,
    ModelDriver,
    SPOptions,
    IDriverModel
}
import strategicprimer.drivers.common.cli {
    ICLIHelper
}
import strategicprimer.model.common.map {
    IMutableMapNG
}

"A factory for the turn-running CLI app."
service(`interface DriverFactory`)
shared class TurnRunningCLIFactory() satisfies ModelDriverFactory {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = false;
        invocation = "run-turn";
        paramsWanted = ParamCount.atLeastTwo;
        shortDescription = "Run a turn's orders and enter results";
        longDescription = "Run a player's orders for a turn and enter results.";
        includeInCLIList = true;
        includeInGUIList = false;
        supportedOptions = ["--current-turn=NN"];
    };

    shared actual ModelDriver createDriver(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        if (is ITurnRunningModel model) {
            return TurnRunningCLI(cli, model);
        } else {
            return createDriver(cli, options, TurnRunningModel.copyConstructor(model));
        }
    }

    shared actual IDriverModel createModel(IMutableMapNG map) => TurnRunningModel(map);
}
