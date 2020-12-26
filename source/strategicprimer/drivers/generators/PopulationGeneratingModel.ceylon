import strategicprimer.drivers.common {
    IDriverModel,
    SimpleMultiMapModel
}

import strategicprimer.model.common.map {
    HasOwner,
    IFixture,
    IMapNG,
    IMutableMapNG,
    Player,
    Point
}

import lovelace.util.common {
    matchingValue
}

import strategicprimer.model.common.map.fixtures.mobile {
    Animal,
    IUnit,
    IWorker
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
    Fortress,
    Village
}

shared class PopulationGeneratingModel extends SimpleMultiMapModel { // TODO: Extract interface
    "Fortresses' [[population]] field cannot be set."
    static alias ModifiableTown=>AbstractTown|Village;

    "The intersection of two sets; here so it can be passed as a method reference rather
     than a lambda in [[playerChoices]]." // TODO: Move to lovelace.util? Or is there some equivalent method-reference logic with curry() or uncurry() or some such?
    static Set<T> intersection<T>(Set<T> one, Set<T> two) given T satisfies Object =>
            one.intersection(two);

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

    "If the fixture is a fortress, return it; otherwise return a Singleton of
     it. This flattens a fortress's contents into a stream of tile fixtures."
    {IFixture*} flattenFortresses(IFixture fixture) {
        if (is Fortress fixture) {
            return fixture;
        } else {
            return Singleton(fixture);
        }
    }

    "Add a (copy of a) worker to a unit in all maps where that unit exists."
    shared void addWorkerToUnit(IUnit unit, IWorker worker) {
        String existingNote;
        if (exists temp = worker.notes.get(unit.owner), !temp.empty) {
            existingNote = temp + " ";
        } else {
            existingNote = "";
        }
        for (map in restrictedAllMaps) {
            if (exists localUnit = map.locations.flatMap(map.fixtures.get)
                    .flatMap(flattenFortresses).narrow<IUnit>()
                    .filter(matchingValue(unit.owner, IUnit.owner))
                    .filter(matchingValue(unit.kind, IUnit.kind))
                    .filter(matchingValue(unit.name, IUnit.name))
                    .find(matchingValue(unit.id, IUnit.id))) {
                Integer turn = map.currentTurn;
                value addend = worker.copy(false);
                if (!turn.negative) {
                    addend.notes[localUnit.owner] = existingNote + "Newcomer in turn #``turn``.";
                }
                localUnit.addMember(addend);
                if (localUnit.getOrders(turn).empty) {
                    localUnit.setOrders(turn, "TODO: assign");
                }
                map.modified = true;
            }
        }
    }

    "All the units in the main map belonging to the specified player."
    shared {IUnit*} getUnits(Player player) =>
            map.fixtures.items.flatMap(flattenFortresses)
                .narrow<IUnit>().filter(matchingValue(player, HasOwner.owner));

    "All the players shared by all the maps." // TODO: Move to IMultiMapModel?
    shared {Player*} playerChoices => allMaps.map(IMapNG.players)
        .map(set).fold(set(map.players))(intersection);

    "Add the given [[unit]] at the given [[location]]."
    shared void addUnitAtLocation(IUnit unit, Point location) { // TODO: If more than one map, return a proxy for the units; otherwise, return the unit
        for (indivMap in restrictedAllMaps) {
            indivMap.addFixture(location, unit); // FIXME: Check for existing matching unit there already
            indivMap.modified = true;
        }
    }
}
