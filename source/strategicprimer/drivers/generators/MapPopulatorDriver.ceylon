import strategicprimer.model.common.idreg {
    createIDFactory,
    IDRegistrar
}

import strategicprimer.drivers.common {
    CLIDriver,
    SPOptions
}

import strategicprimer.drivers.common.cli {
    ICLIHelper
}

import ceylon.random {
    randomize
}

import lovelace.util.common {
    singletonRandom
}

"""A driver to add some kind of fixture to suitable tiles throughout the map. Customize
   the [[populator]] field before each use."""
// TODO: Write GUI equivalent of Map Populator Driver
shared class MapPopulatorDriver(ICLIHelper cli, options, model) satisfies CLIDriver {
    shared actual SPOptions options;
    shared actual IPopulatorDriverModel model;

    "The object that does the heavy lifting of populating the map. This is the one field
     that should be changed before each populating pass."
    MapPopulator populator = sampleMapPopulator;

    variable Integer suitableCount = 0;

    variable Integer changedCount = 0;

    "Populate the map. You shouldn't need to customize this."
    void populate(IPopulatorDriverModel model) {
        IDRegistrar idf = createIDFactory(model.map);
        for (location in randomize(model.map.locations)) {
            if (populator.isSuitable(model.map, location)) {
                suitableCount++;
                if (singletonRandom.nextFloat() < populator.chance) {
                    changedCount++;
                    populator.create(location, model, idf);
                }
            }
        }
    }

    shared actual void startDriver() {
        populate(model);
        cli.println("``changedCount``/``suitableCount`` suitable locations were changed");
        if (changedCount > 0) {
            model.mapModified = true;
        }
    }
}
