import strategicprimer.drivers.common {
    CLIDriver,
    SPOptions
}

import strategicprimer.drivers.common.cli {
    ICLIHelper
}

"A driver to fix ID mismatches between forests and Ground in the main and player maps."
shared class ForestFixerDriver(ICLIHelper cli, options, model) satisfies CLIDriver {
    shared actual SPOptions options;
    shared actual UtilityDriverModel model;

    shared actual void startDriver() => model.fixForestsAndGround(cli.println);
}
