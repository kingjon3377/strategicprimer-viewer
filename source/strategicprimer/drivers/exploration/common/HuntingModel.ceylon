import strategicprimer.drivers.exploration.common {
    surroundingPointIterable
}
import strategicprimer.model.common.map {
    TileType,
    MapDimensions,
    Point,
    IMapNG,
    TileFixture
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
    matchingValue,
    singletonRandom,
    todo
}

"A class to facilitate a better hunting/fishing driver."
shared class HuntingModel {
    """A class and object for "nothing found"."""
    shared static class NothingFound of nothingFound {
        shared new nothingFound {}
    }

    // TODO: Remove once Ceylon 1.4 (or even 1.3.4) is released
    // (workaround for eclipse/ceylon#7376)
    shared static NothingFound nothingFound => NothingFound.nothingFound;

    """The "nothing" value we insert."""
    shared static String noResults = "Nothing ...";

    "How long it should take, in man-hours, to process a carcass of the specified mass,
     in pounds. Calculated using quadratic regression on a set of nine data-points drawn
     from what I could find in online research, plus the origin twice. The quadratic
     trend curve fit better than the linear trendline for all but three of the points,
     and better than a cubic trend curve for all but the origin."
    shared static Float processingTime(Integer weight) =>
            0.855 + 0.0239 * weight - 0.000000872 * weight * weight;

    static Integer dcIfFound(TileFixture|NothingFound item) {
        switch (item)
        case (is TileFixture) {
            return item.dc;
        }
        case (is NothingFound) {
            return 60;
        }
    }

    "An infinite iterator consisting of items taken randomly, but in proportions such that
     lower-discovery-DC items are found more often, from a given stream with
     [[NothingFound]] instances interspersed in a given percentage."
    static class ResultIterator<Type, Absent=NothingFound>({Type*} stream,
            nothingProportion, Absent nothingValue, Integer(Type) dc)
            satisfies Iterator<Type|Absent> given Type satisfies Object {
        Float nothingProportion;
        shared actual Type|Absent next() {
            if (singletonRandom.nextFloat() < nothingProportion) {
                return nothingValue;
            }
            for (item in singletonRandom.elements(stream)) {
                if (15 + singletonRandom.nextInteger(20) >= dc(item)) {
                    return item;
                }
            } else {
                log.warn("Ran out of items to encounter");
                return nothingValue;
            }
        }
    }

    static class ResultStream<Type, Absent=NothingFound>({Type*} stream,
            Float nothingProportion, Absent nothingValue, Integer(Type) dc)
            satisfies {<Type|Absent>+} given Type satisfies Object {
        shared actual Iterator<Type|Absent> iterator() =>
            ResultIterator(stream, nothingProportion, nothingValue, dc);
    }

    "A *non-infinite* iterator that returns 'nothing found' values in the desired
     proportion, but should be more efficient than appending a Singleton cycled to the
     desired length."
    static class FiniteResultIterator<Type, Absent=NothingFound>(stream,
            nothingProportion, Absent nothingValue)
            satisfies Iterator<Type|Absent> given Type satisfies Object {
        {Type*} stream;
        Iterator<Type> wrapped = stream.iterator();
        Float nothingProportion;
        variable Integer counter = 0;
        variable Boolean switched = false;
        shared actual Type|Absent|Finished next() {
            if (switched) {
                if (counter <= 0) {
                    return finished;
                } else {
                    counter--;
                    return nothingValue;
                }
            }
            value retval = wrapped.next();
            if (retval is Finished) {
                switched = true;
                counter = (counter * nothingProportion).integer;
                return nothingValue;
            } else {
                return retval;
            }
        }
    }

    static class FiniteResultStream<Type, Absent=NothingFound>({Type*} stream,
            Float nothingProportion, Absent nothingValue)
            satisfies {<Type|Absent>*} given Type satisfies Object {
        shared actual Iterator<Type|Absent> iterator() => FiniteResultIterator(stream,
            nothingProportion, nothingValue);
    }

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
        .filter(not(compose(fishKinds.contains, Animal.kind)));

    "Aquatic animals (outside fortresses and units) at the given location in the map."
    {Animal*} waterAnimals(Point point) =>
            baseAnimals(point).filter(compose(fishKinds.contains, Animal.kind));

    """Plant-type harvestable fixtures in the map, followed by a number of "nothing found"
       sufficient to give the proportion we want for that tile type."""
    {Grove|Meadow|Shrub|NothingFound*} plants(Point point) {
        value retval = map.fixtures.get(point).narrow<Grove|Meadow|Shrub>();
        Float nothingProportion;
        switch (tileType = map.baseTerrain[point])
        case (TileType.desert|TileType.tundra) { nothingProportion = 0.75; }
        case (TileType.jungle) { nothingProportion = 1.0 / 3.0; }
        else { nothingProportion = 0.5; }
        return FiniteResultStream(retval, nothingProportion, NothingFound.nothingFound);
    }

    "A helper method for the helper method for hunting, fishing, etc."
    {<Point->Type|HuntingModel.NothingFound>*} chooseFromMapImpl<out Type>(
        {Type|NothingFound*}(Point) chosenMap)(Point loc)
            given Type satisfies Object => chosenMap(loc).map(curry(Entry<Point, Type|NothingFound>)(loc));

    "A helper method for hunting or fishing."
    {<Point->Type|NothingFound>+} chooseFromMap<out Type>(
            "Whereabouts to search"
            Point point,
            "Filter/provider to use to find the animals."
            {Type|NothingFound*}(Point) chosenMap)
        given Type satisfies Object&TileFixture => ResultStream(
            surroundingPointIterable(point, dimensions)
                .map(chooseFromMapImpl(chosenMap)).coalesced.flatMap(identity), 0.5,
            point->NothingFound.nothingFound,
                compose(dcIfFound, Entry<Point, TileFixture|NothingFound>.item));

    """Get a stream of hunting results from the area surrounding the given tile. About
       half will be "nothing". May be an infinite stream."""
    todo("We'd like to allow callers(?) to specify a proportion that *should* be tracks,
          perhaps replacing some of the NothingFound")
    shared {<Point->Animal|AnimalTracks|NothingFound>+} hunt(
            "Whereabouts to search"
            Point point) => chooseFromMap(point, animals);

    """Get a stream of fishing results from the area surrounding the given tile. About
       half will be "nothing". May be an infinite stream."""
    shared {<Point->Animal|AnimalTracks|NothingFound>+} fish(
            "Whereabouts to search"
            Point point) => chooseFromMap(point, waterAnimals);

    "Given a location, return the stream of gathering results from just that
     tile."
    {<Point->Grove|Meadow|Shrub|NothingFound>*} gatherImpl(Point point) =>
            plants(point)
                .map(curry(Entry<Point, Grove|Meadow|Shrub|NothingFound>)(point));

    """Get a stream of gathering results from the area surrounding the given tile. Many
       will be "nothing," especially from desert and tundra tiles and less from jungle
       tiles. This may be an infinite stream."""
    shared {<Point->Grove|Meadow|Shrub|NothingFound>*} gather(
            "Whereabouts to search"
            Point point) =>
        singletonRandom.elements(surroundingPointIterable(point, dimensions)
                .map(gatherImpl).flatMap(identity));
}
