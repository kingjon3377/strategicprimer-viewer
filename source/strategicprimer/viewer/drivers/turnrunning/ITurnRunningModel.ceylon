import strategicprimer.model.common.map {
    HasExtent,
    HasPopulation,
    Point,
    TileFixture
}

import strategicprimer.drivers.exploration.common {
    IExplorationModel
}

import ceylon.decimal {
    Decimal
}

import strategicprimer.drivers.common {
    IAdvancementModel
}

"A model for turn-running apps."
shared interface ITurnRunningModel satisfies IExplorationModel&IAdvancementModel {
    "Add a copy of the given fixture to all submaps at the given location iff no fixture
     with the same ID is already there."
    shared formal void addToSubMaps(Point location, TileFixture fixture, Boolean zero);

    "Reduce the population of a group of plants, animals, etc., and copy the reduced form
     into all subordinate maps."
    shared formal void reducePopulation(Point location, HasPopulation<out TileFixture>&TileFixture fixture,
            Boolean zero, Integer reduction);

    "Reduce the acreage of a fixture, and copy the reduced form into all subordinate maps."
    shared formal void reduceExtent(Point location,
            HasExtent<out HasExtent<out Anything>&TileFixture>&TileFixture fixture,
            Boolean zero, Decimal reduction);
}
