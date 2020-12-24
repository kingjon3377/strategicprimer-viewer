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
    shared new (IMutableMapNG map) extends SimpleDriverModel(map) {}

    shared new copyConstructor(IDriverModel model)
        extends SimpleDriverModel(model.restrictedMap) {}

    shared actual void addFixture(Point location, TileFixture fixture) {
        if (restrictedMap.addFixture(location, fixture)) {
            mapModified = true;
        }
    }
}

