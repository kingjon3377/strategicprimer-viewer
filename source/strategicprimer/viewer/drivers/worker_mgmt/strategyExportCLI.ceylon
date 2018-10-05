import ceylon.file {
    parsePath
}
import strategicprimer.drivers.common {
    DriverFailedException,
    IDriverModel,
    DriverUsage,
    ParamCount,
    IDriverUsage,
    SPOptions,
    ReadOnlyDriver,
    ModelDriverFactory,
    DriverFactory,
    ModelDriver
}
import strategicprimer.drivers.common.cli {
    ICLIHelper
}
import strategicprimer.drivers.worker.common {
    WorkerModel,
    IWorkerModel
}
import lovelace.util.common {
    PathWrapper
}
import strategicprimer.model.common.map {
    IMutableMapNG
}

"A factory for a command-line program to export a proto-strategy for a player from orders
 in a map."
service(`interface DriverFactory`)
shared class StrategyExportFactory() satisfies ModelDriverFactory {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = false;
        invocations = ["-w", "--worker", "--export-strategy"];
        paramsWanted = ParamCount.one;
        shortDescription = "Export a proto-strategy";
        longDescription = "Create a proto-strategy using orders stored in the map";
        includeInCLIList = true;
        includeInGUIList = false;
        supportedOptions = [ "--current-turn=NN", "--print-empty",
            "--export=filename.txt", "--include-unleveled-jobs",
            "--summarize-large-units" ];
    };
    shared actual ModelDriver createDriver(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        if (is IWorkerModel model) {
            return StrategyExportCLI(options, model);
        } else {
            return createDriver(cli, options, WorkerModel.copyConstructor(model));
        }
    }
    shared actual IDriverModel createModel(IMutableMapNG map, PathWrapper? path) =>
            WorkerModel(map, path);
}
"A command-line program to export a proto-strategy for a player from orders in a map."
shared class StrategyExportCLI(SPOptions options, model) satisfies ReadOnlyDriver {
    shared actual IWorkerModel model;
    shared actual void startDriver() {
        if (options.hasOption("--export")) {
            StrategyExporter(model, options).writeStrategy(parsePath(
                options.getArgument("--export")).resource, []);
        } else {
            throw DriverFailedException.illegalState("--export option is required");
        }
    }
}
