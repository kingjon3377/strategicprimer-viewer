package drivers.generators;

import legacy.map.IMapNG;
import legacy.map.Point;

import legacy.idreg.IDRegistrar;

/**
 * An interface for map-populating passes to implement. Create an object
 * satisfying this interface, and assign a reference to it to the designated
 * field in {@link MapPopulatorDriver}, and run the driver."
 /* package */
interface MapPopulator {
    /**
     * Whether a point is suitable for the kind of fixture we're creating.
     */
    boolean isSuitable(IMapNG map, Point location);

    /**
     * The probability of adding something to any given tile.
     */
    double getChance();

    /**
     * Add a fixture of the kind we're creating at the given location.
     */
    void create(Point location, IPopulatorDriverModel model, IDRegistrar idf);
}
