import strategicprimer.drivers.common {
    IDriverModel
}

import strategicprimer.model.common.map {
    Point,
    TileFixture
}

shared interface IPopulatorDriverModel satisfies IDriverModel { // TODO: Extend IMultiMapDriverModel?
    "Add a fixture to the map."
    shared formal void addFixture(Point location, TileFixture fixture);
}
