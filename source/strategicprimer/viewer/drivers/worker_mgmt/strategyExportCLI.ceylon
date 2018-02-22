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
    SimpleDriver
}
import strategicprimer.drivers.common.cli {
    ICLIHelper
}
import java.nio.file {
    JPath=Path
}
import strategicprimer.drivers.worker.common {
    WorkerModel,
    IWorkerModel
}
"A command-line program to export a proto-strategy for a player from orders in a map."
shared object strategyExportCLI satisfies SimpleDriver {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = false;
        invocations = ["-w", "--worker"];
        paramsWanted = ParamCount.one;
        shortDescription = "Export a proto-strategy";
        longDescription = "Create a proto-strategy using orders stored in the map";
        supportedOptionsTemp = [ "--current-turn=NN", "--print-empty",
            "--export=filename.txt", "--include-unleveled-jobs" ];
    };
    shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        if (is IWorkerModel model) {
            if (options.hasOption("--export")) {
                StrategyExporter(model, options).writeStrategy(parsePath(
                    options.getArgument("--export")).resource, {});
            } else {
                throw DriverFailedException.illegalState("--export option is required");
            }
        } else {
            startDriverOnModel(cli, options, WorkerModel.copyConstructor(model));
        }
    }
    "This is a CLI driver, so we can't show a file-chooser dialog."
    shared actual {JPath*} askUserForFiles() => {};
}
