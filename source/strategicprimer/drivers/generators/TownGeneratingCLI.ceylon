import strategicprimer.drivers.common {
    IDriverModel,
    IMultiMapModel,
    CLIDriver
}

import strategicprimer.drivers.common.cli {
    ICLIHelper
}

import strategicprimer.model.common.idreg {
    IDRegistrar,
    createIDFactory
}

"A driver to let the user enter or generate 'stats' for towns."
// TODO: Write GUI to allow user to generate or enter town contents
shared class TownGeneratingCLI(ICLIHelper cli, model) satisfies CLIDriver {
    shared actual IDriverModel model;

    shared actual void startDriver() {
        TownGenerator generator = TownGenerator(cli); // TODO: Consider combining that with this class again.
        IDRegistrar idf;
        if (is IMultiMapModel model) {
            idf = createIDFactory(model.allMaps.map(Entry.key));
        } else {
            idf = createIDFactory(model.map);
        }
        if (exists specific =
                cli.inputBoolean("Enter or generate stats for just specific towns? ")) {
            if (specific) {
                generator.generateSpecificTowns(idf, model);
            } else {
                generator.generateAllTowns(idf, model);
            }
        }
    }
}
