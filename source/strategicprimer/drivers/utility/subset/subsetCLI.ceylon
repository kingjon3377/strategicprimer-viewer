import ceylon.logging {
    logger,
    Logger
}
import strategicprimer.drivers.common {
    DriverUsage,
    ParamCount,
    IDriverUsage,
    SPOptions,
    SimpleMultiMapModel,
    IMultiMapModel,
    IDriverModel,
    ISPDriver,
    ReadOnlyDriver
}
import strategicprimer.drivers.common.cli {
    ICLIHelper
}

"A logger."
Logger log = logger(`module strategicprimer.drivers.utility`);
"A driver to check whether player maps are subsets of the main map."
service(`interface ISPDriver`)
shared class SubsetCLI() satisfies ReadOnlyDriver {
    shared actual IDriverUsage usage = DriverUsage(false, ["-s", "--subset"],
        ParamCount.atLeastTwo, "Check players' maps against master",
        "Check that subordinate maps are subsets of the main map, containing nothing that
         it does not contain in the same place.", true, false);
    shared actual void startDriverOnModel(ICLIHelper cli, SPOptions options,
            IDriverModel model) {
        if (is IMultiMapModel model) {
            for (map->[file, _] in model.subordinateMaps) {
                String filename = file?.string else "map without a filename";
                cli.print("``filename``\t...\t\t");
                if (model.map.isSubset(map,
                            (String string) =>
                                cli.println("In ``filename``: ``string``"))) {
                    cli.println("OK");
                } else {
                    cli.println("WARN");
                }
            }
        } else {
            log.warn("Subset checking does nothing with no subordinate maps");
            startDriverOnModel(cli, options, SimpleMultiMapModel.copyConstructor(model));
        }
    }
}
