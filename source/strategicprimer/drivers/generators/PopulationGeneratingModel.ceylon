import strategicprimer.drivers.common {
    IDriverModel,
    SimpleMultiMapModel
}

import strategicprimer.model.common.map {
    IMutableMapNG,
    Point
}

import lovelace.util.common {
    matchingValue
}

import strategicprimer.model.common.map.fixtures.mobile {
    Animal
}

import strategicprimer.model.common.map.fixtures.terrain {
    Forest
}

import strategicprimer.model.common.map.fixtures.resources {
    Grove,
    Meadow,
    Shrub
}

import strategicprimer.model.common.map.fixtures.towns {
    AbstractTown,
    CommunityStats,
    Village
}

shared class PopulationGeneratingModel extends SimpleMultiMapModel {
    "Fortresses' [[population]] field cannot be set."
    static alias ModifiableTown=>AbstractTown|Village;

    shared new (IMutableMapNG map) extends SimpleMultiMapModel(map) {}

    shared new copyConstructor(IDriverModel model) extends SimpleMultiMapModel(model.restrictedMap) {}

    "Set the population of [[kind]] animals (talking or not per [[talking]]) at
     [[location]] to [[population]]. Returns [[true]] if a population was in
     fact set (i.e. if there was a matching object there in any of the maps),
     [[false]] otherwise."
    shared Boolean setAnimalPopulation(Point location, Boolean talking, String kind, Integer population) {
        variable Boolean retval = false;
        for (map in restrictedAllMaps) { // TODO: Should submaps really all get this information?
            //if (exists animal = map.fixtures[location].narrow<Animal>() // TODO: syntax sugar
            if (exists animal = map.fixtures.get(location).narrow<Animal>()
                    .filter(matchingValue(talking, Animal.talking))
                    .find(matchingValue(kind, Animal.kind))) { // TODO: Only match those without an existing population?
                Animal replacement = animal.reduced(population);
                map.replace(location, animal, replacement);
                retval = true;
            }
        }
        return retval;
    }

    "Set the population of [[kind]] trees (in a grove or orchard) at
     [[location]] to [[population]]. Returns [[true]] if a population was in
     fact set (i.e. if there was a matching grove or orchard there in any of
     the maps), [[false]] otherwise."
    shared Boolean setGrovePopulation(Point location, String kind, Integer population) {
        variable Boolean retval = false;
        for (map in restrictedAllMaps) { // TODO: Should submaps really all get this information?
            //if (exists grove = map.fixtures[location].narrow<Grove>() // TODO: syntax sugar
            if (exists grove = map.fixtures.get(location).narrow<Grove>()
                    .find(matchingValue(kind, Grove.kind))) { // TODO: Only match without an existing count?
                Grove replacement = grove.reduced(population);
                map.replace(location, grove, replacement);
                retval = true;
            }
        }
        return retval;
    }

    "Set the population of [[kind]] shrubs at [[location]] to [[population]].
     Returns [[true]] if a population was in fact set (i.e. there were in fact
     matching shrubs there in any of the maps), [[false]]."
    shared Boolean setShrubPopulation(Point location, String kind, Integer population) {
        variable Boolean retval = false;
        for (map in restrictedAllMaps) { // TODO: Should submaps really all get this information?
            //if (exists grove = map.fixtures[location].narrow<Shrub>() // TODO: syntax sugar
            if (exists shrub = map.fixtures.get(location).narrow<Shrub>()
                    .find(matchingValue(kind, Shrub.kind))) { // TODO: Only match without an existing count?
                Shrub replacement = shrub.reduced(population);
                map.replace(location, shrub, replacement);
                retval = true;
            }
        }
        return retval;
    }

    "Set the extent of the given [[field or meadow|field]] at [[location]] to
     [[acres]] in maps where that field or meadow is known. Returns [[true]] if
     any of the maps had it at that location, [[false]] otherwise."
    shared Boolean setFieldExtent(Point location, Meadow field, Float acres) {
        variable Boolean retval = false;
        for (map in restrictedAllMaps) { // TODO: Should submaps really all get this information?
            if (exists existing = map.fixtures.get(location).narrow<Meadow>() // TODO: syntax sugar
                    .filter(matchingValue(field.kind, Meadow.kind))
                    .filter(matchingValue(field.field, Meadow.field))
                    .filter(matchingValue(field.cultivated, Meadow.cultivated))
                    .filter(matchingValue(field.status, Meadow.status))
                    .find(matchingValue(field.id, Meadow.id))) { // TODO: only match without an existing extent?
                Meadow replacement = Meadow(field.kind, field.field, field.cultivated,
                    field.id, field.status, acres);
                map.replace(location, existing, replacement);
                retval = true;
            }
        }
        return retval;
    }

    "Set the extent of the given [[forest]] at [[location]] to [[acres]] in
     maps where that forest is known. Returns [[true]] if any of the maps had
     it at that location, [[false]] otherwise."
    shared Boolean setForestExtent(Point location, Forest forest, Number<out Anything> acres) {
        variable Boolean retval = false;
        for (map in restrictedAllMaps) { // TODO: Should submaps really all get this information?
            if (exists existing = map.fixtures.get(location).narrow<Forest>() // TODO: syntax sugar
                    .filter(matchingValue(forest.kind, Forest.kind))
                    .filter(matchingValue(forest.rows, Forest.rows))
                    .find(matchingValue(forest.id, Forest.id))) {
                Forest replacement = Forest(forest.kind, forest.rows, forest.id, acres);
                restrictedMap.replace(location, existing, replacement);
                retval = true;
            }
        }
        return retval;
    }

    "Assign the given [[population details object|stats]] to the town
     identified by the given [[ID number|townId]] and [[name]] at the given
     [[location]] in each map. Returns [[true]] if such a town was in any of
     the maps, [[false]] otherwise." // TODO: Should use a copy of the stats object, not it itself
    shared Boolean assignTownStats(Point location, Integer townId, String name,
            CommunityStats stats) {
        variable Boolean retval = false;
        for (map in restrictedAllMaps) { // TODO: Should submaps really all get this information?
            if (exists town = map.fixtures.get(location) // TODO: syntax sugar
                    .narrow<ModifiableTown>()
                    .filter(matchingValue(townId, ModifiableTown.id))
                    .find(matchingValue(name, ModifiableTown.name))) {
                if (is AbstractTown town) {
                    town.population = stats;
                } else {
                    town.population = stats;
                }
                retval = true;
                map.modified = true;
            }
        }
        return retval;
    }
}
