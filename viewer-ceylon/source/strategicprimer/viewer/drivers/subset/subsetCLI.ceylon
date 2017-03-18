import java.nio.file {
    JPath=Path
}
import java.util {
    Formatter
}

import lovelace.util.jvm {
    AppendableHelper
}

import model.misc {
    SimpleMultiMapModel,
    IDriverModel,
    IMultiMapModel
}

import strategicprimer.viewer.drivers {
    ICLIHelper,
    DriverUsage,
    ParamCount,
    IDriverUsage,
    SPOptions,
    SimpleDriver
}
import ceylon.logging {
    logger,
    Logger
}
"A logger."
Logger log = logger(`module strategicprimer.viewer`);
"A driver to check whether player maps are subsets of the main map."
shared object subsetCLI satisfies SimpleDriver {
    shared actual IDriverUsage usage = DriverUsage(false, "-s", "--subset", ParamCount.atLeastTwo,
        "Check players' maps against master",
        "Check that subordinate maps are subsets of the main map, containing nothing that
         it does not contain in the same place.");
    shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        if (is IMultiMapModel model) {
            for (pair in model.subordinateMaps) {
                String filename = pair.second().map(JPath.string)
                    .orElse("map without a filename");
                cli.print("``filename``\t...\t\t");
                if (model.map.isSubset(pair.first(),
                    Formatter(AppendableHelper(cli.print)), "In ``filename``:")) {
                    cli.println("OK");
                } else {
                    cli.println("WARN");
                }
            }
        } else {
            log.warn("Subset checking does nothing with no subordinate maps");
            startDriverOnModel(cli, options, SimpleMultiMapModel(model));
        }
    }
}
