import lovelace.util.common {
    PathWrapper
}

import strategicprimer.drivers.common {
    IDriverModel,
    SimpleDriverModel
}

import strategicprimer.model.common.map {
    IMutableMapNG,
    Point,
    TileFixture
}

shared class PopulatorDriverModel extends SimpleDriverModel satisfies IPopulatorDriverModel {
    shared new (IMutableMapNG map, PathWrapper? path, Boolean modified = false)
        extends SimpleDriverModel(map, path, modified) {}

    shared new copyConstructor(IDriverModel model)
        extends SimpleDriverModel(model.restrictedMap, model.mapFile, model.mapModified) {}

    shared actual void addFixture(Point location, TileFixture fixture) {
        if (restrictedMap.addFixture(location, fixture)) {
            mapModified = true;
        }
    }
}

