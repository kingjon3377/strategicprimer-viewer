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
    """The "nothing" value we insert."""
    shared static String noResults = "Nothing ...";
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
    "Non-aquatic animals in the map."
    MutableMap<Point, MutableList<String>> animals =
            HashMap<Point, MutableList<String>>();
    "Aquatic animals in the map."
    MutableMap<Point, MutableList<String>> waterAnimals =
            HashMap<Point, MutableList<String>>();
    "Plants in the map."
    MutableMap<Point, MutableList<String>> plants = HashMap<Point, MutableList<String>>();
    for (point in map.locations) {
//        for (fixture in map.fixtures[point]) { // TODO: syntax sugar once compiler bug fixed
        for (fixture in map.fixtures.get(point)) {
            if (is Animal fixture, !fixture.talking, !fixture.traces) {
                String kind = fixture.kind;
                MutableList<String> list;
                if (fishKinds.contains(kind)) {
                    if (exists temp = waterAnimals[point]) {
                        list = temp;
                    } else {
                        list = ArrayList<String>();
                        waterAnimals[point] = list;
                    }
                } else if (exists temp = animals[point]) {
                    list = temp;
                } else {
                    list = ArrayList<String>();
                    animals[point] = list;
                }
                list.add(kind);
            } else if (is Grove|Meadow|Shrub fixture) {
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
    {String*} chooseFromMap(
            "Whereabouts to search"
            Point point,
            "Which map to look in"
            Map<Point, MutableList<String>> chosenMap) {
        variable {String*} choices = surroundingPointIterable(point, dimensions)
            .map((loc) => chosenMap[loc]).coalesced.flatMap(identity);
        choices = choices.chain({noResults}.repeat(choices.size));
        return DefaultRandom().elements(choices);
    }
    """Get a stream of hunting results from the area surrounding the given tile. About half
        will be "nothing". May be an infinite stream."""
    shared {String*} hunt(
            "Whereabouts to search"
            Point point) => chooseFromMap(point, animals);
    """Get a stream of fishing results from the area surrounding the given tile. About half
        will be "nothing". May be an infinite stream."""
    shared {String*} fish(
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
