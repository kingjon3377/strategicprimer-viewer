import strategicprimer.drivers.common {
    IDriverModel,
    SimpleDriverModel
}

import strategicprimer.model.common.map {
    IMutableMapNG,
    Point,
    TileFixture
}

import lovelace.util.common {
    matchingValue,
    PathWrapper
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

shared class PopulationGeneratingModel extends SimpleDriverModel { // TODO: extend to multi-map operation
    shared new (IMutableMapNG map, PathWrapper? path) extends SimpleDriverModel(map, path) {}

    shared new copyConstructor(IDriverModel model) extends SimpleDriverModel(model.map, model.mapFile) {}

    "Replace an existing fixture with a replacement." // TODO: Should this be provided by IMutableMapNG instead?
    void replace<Type>(Point location, Type original, Type replacement) given Type satisfies TileFixture {
        map.removeFixture(location, original); // TODO: If these methods return Boolean, make this return Boolean too based on their return values
        map.addFixture(location, replacement);
    }

    "Set the population of [[kind]] animals (talking or not per [[talking]]) at
     [[location]] to [[population]]. Returns [[true]] if a population was in
     fact set (i.e. if there was a matching object there), [[false]] otherwise."
    shared Boolean setAnimalPopulation(Point location, Boolean talking, String kind, Integer population) {
        //if (exists animal = map.fixtures[location].narrow<Animal>() // TODO: syntax sugar
        if (exists animal = map.fixtures.get(location).narrow<Animal>()
                .filter(matchingValue(talking, Animal.talking))
                .find(matchingValue(kind, Animal.kind))) {
            Animal replacement = animal.reduced(population);
            replace(location, animal, replacement);
            return true;
        } else {
            return false;
        }
    }

    "Set the population of [[kind]] trees (in a grove or orchard) at
     [[location]] to [[population]]. Returns [[true]] if a population was in
     fact set (i.e. if there was a matching grove or orchard there), [[false]]
     otherwise."
    shared Boolean setGrovePopulation(Point location, String kind, Integer population) {
        //if (exists grove = map.fixtures[location].narrow<Grove>() // TODO: syntax sugar
        if (exists grove = map.fixtures.get(location).narrow<Grove>()
                .find(matchingValue(kind, Grove.kind))) {
            Grove replacement = grove.reduced(population);
            replace(location, grove, replacement);
            return true;
        } else {
            return false;
        }
    }

    "Set the population of [[kind]] shrubs at [[location]] to [[population]].
     Returns [[true]] if a population was in fact set (i.e. there were in fact
     matching shrubs there), [[false]]."
    shared Boolean setShrubPopulation(Point location, String kind, Integer population) {
        //if (exists grove = map.fixtures[location].narrow<Shrub>() // TODO: syntax sugar
        if (exists shrub = map.fixtures.get(location).narrow<Shrub>()
                .find(matchingValue(kind, Shrub.kind))) {
            Shrub replacement = shrub.reduced(population);
            replace(location, shrub, replacement);
            return true;
        } else {
            return false;
        }
    }

    "Set the extent of the given [[field or meadow|field]] at [[location]] to [[acres]]."
    shared void setFieldExtent(Point location, Meadow field, Float acres) {
        Meadow replacement = Meadow(field.kind, field.field, field.cultivated,
            field.id, field.status, acres);
        replace(location, field, replacement);
    }

    "Set the extent of the given [[forest]] at [[location]] to [[acres]]."
    shared void setForestExtent(Point location, Forest forest, Number<out Anything> acres) {
        Forest replacement = Forest(forest.kind, forest.rows, forest.id, acres);
        replace(location, forest, replacement);
    }
}
