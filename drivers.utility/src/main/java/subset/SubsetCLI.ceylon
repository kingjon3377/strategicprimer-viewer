import strategicprimer.drivers.common {
    IMultiMapModel,
    ReadOnlyDriver,
    emptyOptions,
    SPOptions
}

import strategicprimer.drivers.common.cli {
    ICLIHelper
}

"A driver to check whether player maps are subsets of the main map."
shared class SubsetCLI(ICLIHelper cli, model) satisfies ReadOnlyDriver {
    shared actual IMultiMapModel model;
    shared actual SPOptions options = emptyOptions;

    void report(String filename)(String string) =>
            cli.println("In ``filename``: ``string``");

    shared actual void startDriver() {
        for (map in model.subordinateMaps) {
            String filename = map.filename?.string else "map without a filename";
            cli.print(filename, "\t...\t\t");
            if (model.map.isSubset(map, report(filename))) {
                cli.println("OK");
            } else {
                cli.println("WARN");
            }
        }
    }
}
