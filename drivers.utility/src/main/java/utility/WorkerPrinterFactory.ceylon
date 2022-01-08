import strategicprimer.drivers.common {
    IDriverUsage,
    DriverUsage,
    ParamCount,
    SPOptions,
    IDriverModel,
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

import strategicprimer.model.common.map {
    IMutableMapNG
}

"A factory for the driver to print a mini-report on workers, suitable for inclusion in a
 player's results."
service(`interface DriverFactory`)
shared class WorkerPrinterFactory() satisfies ModelDriverFactory {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = false;
        invocation = "print-stats";
        paramsWanted = ParamCount.one;
        shortDescription = "Print stats of workers";
        longDescription = "Print stats of workers in a unit in a brief list.";
        includeInCLIList = true;
        includeInGUIList = false;
    };

    shared actual ModelDriver createDriver(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        if (is IExplorationModel model) {
            return WorkerPrintCLI(cli, model);
        } else {
            return createDriver(cli, options, ExplorationModel.copyConstructor(model));
        }
    }

    shared actual IDriverModel createModel(IMutableMapNG map) =>
            ExplorationModel(map);
}
