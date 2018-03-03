import ceylon.collection {
    MutableList,
    MutableMap,
    HashMap,
    ArrayList
}

import strategicprimer.drivers.exploration.common {
    surroundingPointIterable
}
import strategicprimer.model.map {
    MapDimensions,
    TileType,
    IMapNG,
    Point
}
import strategicprimer.model.map.fixtures.mobile {
    Animal
}
import strategicprimer.model.map.fixtures.resources {
    Grove,
    Shrub,
    Meadow
}
import ceylon.random {
    DefaultRandom
}
"A class to facilitate a better hunting/fishing driver."
shared class HuntingModel {
	"""A class and object for "nothing found"."""
	shared static class NothingFound of nothingFound {
		shared new nothingFound {}
	}
    """The "nothing" value we insert."""
    shared static String noResults = "Nothing ...";
    "How long it should take, in man-hours, to process a carcass of the specified mass, in pounds.
     Calculated using quadratic regression on a set of nine data-points drawn from what I could find
     in online research, plus the origin twice. The quadratic trend curve fit better than the linear
     trendline for all but three of the points, and better than a cubic trend curve for all but the
     origin."
    shared static Float processingTime(Integer weight) =>
            0.855 + 0.0239 * weight - 0.000000872 * weight * weight;
    "The map to hunt in" IMapNG map;
    shared new (IMapNG map) {
        this.map = map;
    }
    MapDimensions dimensions = map.dimensions;
    {String*} fishKinds = set {
        for (point in map.locations)
            if (exists terrain = map.baseTerrain[point], terrain == TileType.ocean)
//                for (fixture in map.fixtures[point]) // TODO: syntax sugar once compiler bug fixed
                for (fixture in map.fixtures.get(point))
                    if (is Animal fixture)
                        fixture.kind
    };
    "Animals (outside fortresses and units), both aquatic and non-aquatic, at the given location in the map."
    {Animal*} baseAnimals(Point point) =>
            //map.fixtures[point].narrow<Animal>().filter((animal) => !animal.talking && !animal.traces); // TODO: syntax sugar once compiler bug fixed
            map.fixtures.get(point).narrow<Animal>().filter((animal) => !animal.talking && !animal.traces);
    "Non-aquatic animals (outside fortresses and units) at the given location in the map."
    {Animal*} animals(Point point) => baseAnimals(point).filter((animal) => !fishKinds.contains(animal.kind));
    "Aquatic animals (outside fortresses and units) at the given location in the map."
    {Animal*} waterAnimals(Point point) => baseAnimals(point).filter((animal) => fishKinds.contains(animal.kind));
    "Plants in the map."
    MutableMap<Point, MutableList<String>> plants = HashMap<Point, MutableList<String>>();
    for (point in map.locations) {
        for (fixture in map.fixtures.get(point)) {
            if (is Grove|Meadow|Shrub fixture) {
                if (exists list = plants[point]) {
                    list.add(fixture.string);
                } else {
                    MutableList<String> list = ArrayList<String>();
                    plants[point] = list;
                    list.add(fixture.string);
                }
            }
        }
        if (exists plantList = plants[point]) {
            Integer length = plantList.size - 1;
            TileType? tileType = map.baseTerrain[point];
            Integer nothings;
            switch (tileType)
            case (TileType.desert|TileType.tundra) { nothings = length * 3; }
            case (TileType.jungle) { nothings = length / 2; }
            else { nothings = length; }
            plantList.addAll({noResults}.repeat(nothings));
        }
    }
    "A helper method for hunting or fishing."
    {<Point->Type|NothingFound>*} chooseFromMap<out Type>(
            "Whereabouts to search"
            Point point,
            "Filter/provider to use to find the animals."
            {Type|NothingFound*}(Point) chosenMap) given Type satisfies Object {
        variable {<Point->Type|NothingFound>*} choices = surroundingPointIterable(point, dimensions)
            .map((loc) => chosenMap(loc).map((item) => loc->item)).coalesced.flatMap(identity);
        choices = choices.chain({point->NothingFound.nothingFound}.repeat(choices.size));
        return DefaultRandom().elements(choices);
    }
    """Get a stream of hunting results from the area surrounding the given tile. About half
        will be "nothing". May be an infinite stream."""
    shared {<Point->Animal|NothingFound>*} hunt(
            "Whereabouts to search"
            Point point) => chooseFromMap(point, animals);
    """Get a stream of fishing results from the area surrounding the given tile. About half
        will be "nothing". May be an infinite stream."""
    shared {<Point->Animal|NothingFound>*} fish(
            "Whereabouts to search"
            Point point) => chooseFromMap(point, waterAnimals);
    """Get a stream of gathering results from the area surrounding the given tile. Many will
        be "nothing," especially from desert and tundra tiles and less from jungle
        tiles. This may be an infinite stream."""
    shared {String*} gather(
            "Whereabouts to search"
            Point point) {
        variable {String*} choices = {
            for (loc in surroundingPointIterable(point, dimensions))
                if (exists list = plants[loc])
                    for (plant in list)
                        plant
        };
        return DefaultRandom().elements(choices);
    }
}
