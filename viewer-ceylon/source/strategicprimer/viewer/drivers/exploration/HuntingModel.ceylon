import ceylon.collection {
    MutableList,
    MutableMap,
    HashMap,
    ArrayList
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
import lovelace.util.jvm { shuffle }
import strategicprimer.drivers.exploration.common {
    surroundingPointIterable
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
            if (map.getBaseTerrain(point) == TileType.ocean)
                for (fixture in map.getOtherFixtures(point))
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
        for (fixture in map.getOtherFixtures(point)) {
            if (is Animal fixture, !fixture.talking, !fixture.traces) {
                String kind = fixture.kind;
                MutableList<String> list;
                if (fishKinds.contains(kind)) {
                    if (exists temp = waterAnimals.get(point)) {
                        list = temp;
                    } else {
                        list = ArrayList<String>();
                        waterAnimals.put(point, list);
                    }
                } else if (exists temp = animals.get(point)) {
                    list = temp;
                } else {
                    list = ArrayList<String>();
                    animals.put(point, list);
                }
                list.add(kind);
            } else if (is Grove|Meadow|Shrub fixture) {
                if (exists list = plants.get(point)) {
                    list.add(fixture.string);
                } else {
                    MutableList<String> list = ArrayList<String>();
                    plants.put(point, list);
                    list.add(fixture.string);
                }
            }
        }
        if (exists plantList = plants.get(point)) {
            Integer length = plantList.size - 1;
            TileType tileType = map.getBaseTerrain(point);
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
            "How many items to limit the list to"
            Integer items,
            "Which map to look in"
            Map<Point, MutableList<String>> chosenMap) {
        variable {String*} choices = {
            for (loc in surroundingPointIterable(point, dimensions))
                if (exists list = chosenMap.get(loc))
                    for (item in list)
                        item
        };
        choices = choices.chain({noResults}.repeat(choices.size));
        MutableList<String> retval = ArrayList<String>();
        for (i in 0..items) {
            choices = shuffle(choices);
            if (exists first = choices.first) {
                retval.add(first);
            }
        }
        return { *retval };
    }
    """Get a list of hunting results from the area surrounding the given tile. About half
        will be "nothing"."""
    shared {String*} hunt(
            "Whereabouts to search"
            Point point,
            "How many items to limit the list to"
            Integer items) => chooseFromMap(point, items, animals);
    """Get a list of fishing results from the area surrounding the given tile. About half
        will be "nothing"."""
    shared {String*} fish(
            "Whereabouts to search"
            Point point,
            "How many items to limit the list to"
            Integer items) => chooseFromMap(point, items, waterAnimals);
    """Get a list of gathering results from the area surrounding the given tile. Many will
        be "nothing," especially from desert and tundra tiles and less from jungle
        tiles."""
    shared {String*} gather(
            "Whereabouts to search"
            Point point,
            "How many items to limit the list to"
            Integer items) {
        variable {String*} choices = {
            for (loc in surroundingPointIterable(point, dimensions))
                if (exists list = plants.get(loc))
                    for (plant in list)
                        plant
        };
        MutableList<String> retval = ArrayList<String>();
        for (i in 0..items) {
            choices = shuffle(choices);
            if (exists first = choices.first) {
                retval.add(first);
            }
        }
        return { *retval };
    }
}