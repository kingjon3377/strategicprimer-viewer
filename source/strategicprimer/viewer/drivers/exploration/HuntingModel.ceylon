import strategicprimer.drivers.exploration.common {
    surroundingPointIterable
}
import strategicprimer.model.common.map {
    TileType,
    MapDimensions,
    Point,
    IMapNG
}
import strategicprimer.model.common.map.fixtures.mobile {
    Animal,
    AnimalTracks
}
import strategicprimer.model.common.map.fixtures.resources {
    Grove,
    Shrub,
    Meadow
}
import lovelace.util.common {
    matchingPredicate,
    matchingValue,
    singletonRandom
}

"A class to facilitate a better hunting/fishing driver."
shared class HuntingModel {
    """A class and object for "nothing found"."""
    shared static class NothingFound of nothingFound {
        shared new nothingFound {}
    }

    """The "nothing" value we insert."""
    shared static String noResults = "Nothing ...";

    "How long it should take, in man-hours, to process a carcass of the specified mass,
     in pounds. Calculated using quadratic regression on a set of nine data-points drawn
     from what I could find in online research, plus the origin twice. The quadratic
     trend curve fit better than the linear trendline for all but three of the points,
     and better than a cubic trend curve for all but the origin."
    shared static Float processingTime(Integer weight) =>
            0.855 + 0.0239 * weight - 0.000000872 * weight * weight;

    "The map to hunt in" IMapNG map;
    shared new (IMapNG map) {
        this.map = map;
    }

    MapDimensions dimensions = map.dimensions;

    {String*} fishKinds = map.locations
        .filter(matchingValue(TileType.ocean, map.baseTerrain.get))
            .flatMap(map.fixtures.get).narrow<Animal>().map(Animal.kind).distinct;

    "Animals (outside fortresses and units), both aquatic and non-aquatic, at the given
     location in the map."
    {Animal*} baseAnimals(Point point) =>
            //map.fixtures[point].narrow<Animal>().filter(not(animal.talking)); // TODO: syntax sugar once compiler bug fixed
            map.fixtures.get(point).narrow<Animal>().filter(not(Animal.talking));

    "Non-aquatic animals (outside fortresses and units) at the given location in the map."
    {Animal*} animals(Point point) => baseAnimals(point)
        .filter(not(matchingPredicate(fishKinds.contains, Animal.kind)));

    "Aquatic animals (outside fortresses and units) at the given location in the map."
    {Animal*} waterAnimals(Point point) =>
            baseAnimals(point).filter(matchingPredicate(fishKinds.contains, Animal.kind));

    """Plant-type harvestable fixtures in the map, followed by a number of "nothing found"
       sufficient to give the proportion we want for that tile type."""
    {Grove|Meadow|Shrub|NothingFound*} plants(Point point) {
        value retval = map.fixtures.get(point).narrow<Grove|Meadow|Shrub>();
        Integer length = retval.size - 1;
        Integer nothings;
        switch (tileType = map.baseTerrain[point])
        case (TileType.desert|TileType.tundra) { nothings = length * 3; }
        case (TileType.jungle) { nothings = length / 2; }
        else { nothings = length; }
        return retval.chain(Singleton(NothingFound.nothingFound).repeat(nothings));
    }

    "A helper method for hunting or fishing."
    {<Point->Type|NothingFound>*} chooseFromMap<out Type>(
            "Whereabouts to search"
            Point point,
            "Filter/provider to use to find the animals."
            {Type|NothingFound*}(Point) chosenMap) given Type satisfies Object {
        variable {<Point->Type|NothingFound>*} choices =
                surroundingPointIterable(point, dimensions)
                    .map((loc) => chosenMap(loc).map(
                        curry(Entry<Point, Type|NothingFound>)(loc)))
                    .coalesced.flatMap(identity);
        choices = choices.chain(Singleton(point->NothingFound.nothingFound)
            .repeat(choices.size));
        return singletonRandom.elements(choices);
    }

    """Get a stream of hunting results from the area surrounding the given tile. About
       half will be "nothing". May be an infinite stream."""
    // TODO: We'd like to allow callers(?) to specify a proportion that *should* be tracks, perhaps replacing some of the NothingFound
    shared {<Point->Animal|AnimalTracks|NothingFound>*} hunt(
            "Whereabouts to search"
            Point point) => chooseFromMap(point, animals);

    """Get a stream of fishing results from the area surrounding the given tile. About
       half will be "nothing". May be an infinite stream."""
    shared {<Point->Animal|AnimalTracks|NothingFound>*} fish(
            "Whereabouts to search"
            Point point) => chooseFromMap(point, waterAnimals);

    """Get a stream of gathering results from the area surrounding the given tile. Many
       will be "nothing," especially from desert and tundra tiles and less from jungle
       tiles. This may be an infinite stream."""
    shared {<Point->Grove|Meadow|Shrub|NothingFound>*} gather(
            "Whereabouts to search"
            Point point) {
        {<Point->Grove|Meadow|Shrub|NothingFound>*} retval = // TODO: Inline; make method use =>
                surroundingPointIterable(point, dimensions)
                    .map((loc) => plants(loc)
                        .map(curry(Entry<Point, Grove|Meadow|Shrub|NothingFound>)(loc)))
                .flatMap(identity);
        return singletonRandom.elements(retval);
    }
}
