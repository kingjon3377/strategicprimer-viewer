import model.misc {
    IDriverModel
}
import java.lang {
    IllegalStateException
}
import ceylon.file {
    parsePath
}
import model.workermgmt {
    WorkerModel,
    IWorkerModel
}
import strategicprimer.viewer.drivers {
    DriverFailedException,
    ICLIHelper,
    DriverUsage,
    ParamCount,
    IDriverUsage,
    SPOptions,
    SimpleDriver
}
"A command-line program to export a proto-strategy for a player from orders in a map."
shared object strategyExportCLI satisfies SimpleDriver {
    shared actual IDriverUsage usage = DriverUsage {
        graphical = false;
        shortOption = "-w";
        longOption = "--worker";
        paramsWanted = ParamCount.one;
        shortDescription = "Export a proto-strategy";
        longDescription = "Create a proto-strategy using orders stored in the map";
        supportedOptionsTemp = [ "--current-turn=NN", "--print-empty", "--export=filename.txt" ];
    };
    shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        if (is IWorkerModel model) {
            if (options.hasOption("--export")) {
                StrategyExporter(model, options).writeStrategy(parsePath(
                    options.getArgument("--export")).resource, {});
            } else {
                throw DriverFailedException(
                    IllegalStateException("--export option is required"),
                    "--export option is required");
            }
        } else {
            startDriverOnModel(cli, options, WorkerModel(model));
        }
    }
}