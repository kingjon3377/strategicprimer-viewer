import strategicprimer.drivers.common {
    UtilityDriver,
    IDriverUsage,
    DriverUsage,
    ParamCount,
    SPOptions,
    DriverFactory,
    UtilityDriverFactory
}

import strategicprimer.drivers.common.cli {
    ICLIHelper
}

"A factory for a driver to create a spreadsheet model of a mine. Its parameters are the
 name of the file to write the CSV to and the value at the top center (as an index into
 the [[LodeStatus]] values array, or the String representation thereof)."
service(`interface DriverFactory`)
native("jvm") // TODO: remove once ceylon.file and ceylon.decimal are implemented for JS, eclipse/ceylon#2448 and eclipse/ceylon-sdk#239
shared class MiningCLIFactory satisfies UtilityDriverFactory {
    shared static IDriverUsage staticUsage = DriverUsage {
        graphical = false;
        invocation = "mining";
        paramsWanted = ParamCount.two;
        shortDescription = "Create a model of a mine";
        longDescription = "Create a CSV spreadsheet representing a mine's area";
        firstParamDescription = "output.csv";
        subsequentParamDescription = "status";
        includeInCLIList = true;
        includeInGUIList = false;
        supportedOptions = [ "--seed=NN", "--banded" ];
    };

    shared actual IDriverUsage usage => staticUsage;

    shared new () {}

    shared actual UtilityDriver createDriver(ICLIHelper cli, SPOptions options) =>
            MiningCLI(cli, options);
}
