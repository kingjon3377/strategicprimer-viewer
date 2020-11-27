import strategicprimer.model.common.map {
    IMapNG,
    Point
}

import strategicprimer.model.common.idreg {
    IDRegistrar
}

"An interface for map-populating passes to implement. Create an object satisfying this
 interface, and assign a reference to it to the designated field in
 [[MapPopulatorDriver]], and run the driver."
interface MapPopulator {
    "Whether a point is suitable for the kind of fixture we're creating."
    shared formal Boolean isSuitable(IMapNG map, Point location);

    "The probability of adding something to any given tile."
    shared formal Float chance;

    "Add a fixture of the kind we're creating at the given location."
    shared formal void create(Point location, IPopulatorDriverModel model, IDRegistrar idf);
}
