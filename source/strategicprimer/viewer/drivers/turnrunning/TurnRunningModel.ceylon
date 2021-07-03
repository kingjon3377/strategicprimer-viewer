import strategicprimer.model.common.map {
    HasExtent,
    HasName,
    HasOwner,
    HasPopulation,
    IFixture,
    IMutableMapNG,
    Player,
    Point,
    TileFixture
}

import strategicprimer.drivers.exploration.common {
    ExplorationModel
}

import strategicprimer.drivers.common {
    IDriverModel
}

import ceylon.decimal {
    Decimal
}

import lovelace.util.jvm {
    decimalize
}

import strategicprimer.model.common.map.fixtures.mobile {
    AnimalImpl,
    IMutableWorker,
    IMutableUnit,
    IUnit,
    IWorker
}

import lovelace.util.common {
    matchingValue
}

import strategicprimer.model.common.map.fixtures.mobile.worker {
    IJob,
    IMutableJob,
    IMutableSkill,
    ISkill,
    Job,
    Skill
}

import strategicprimer.model.common.map.fixtures.towns {
    IFortress,
    IMutableFortress
}

import ceylon.logging {
    Logger,
    logger
}

import strategicprimer.model.common.map.fixtures {
    FortressMember,
    IMutableResourcePile,
    IResourcePile,
    Quantity,
    ResourcePileImpl
}

import ceylon.random {
    DefaultRandom,
    Random
}

"Logger."
Logger log = logger(`module strategicprimer.viewer`);

shared class TurnRunningModel extends ExplorationModel satisfies ITurnRunningModel {
    "If [[fixture]] is a [[fortress|IFortress]], return it; otherwise, return a Singleton
     containing it. This is intended to be used in [[Iterable.flatMap]]."
    static {IFixture*} unflattenNonFortresses(TileFixture fixture) {
        if (is IFortress fixture) {
            return fixture;
        } else {
            return Singleton(fixture);
        }
    }

    "If [[fixture]] is a fortress, return a stream of it and its contents; otherwise,
     return a singleton containing the fixture. This is intended to be used in [[Iterable.flatMap]]."
    static {IFixture*} partiallyFlattenFortresses(TileFixture fixture) {
        if (is IFortress fixture) {
            return fixture.follow(fixture);
        } else {
            return Singleton(fixture);
        }
    }

    shared new (IMutableMapNG map) extends ExplorationModel(map) {}
    shared new copyConstructor(IDriverModel model)
        extends ExplorationModel.copyConstructor(model) {}

    "Add a copy of the given fixture to all submaps at the given location iff no fixture
     with the same ID is already there."
    shared actual void addToSubMaps(Point point, TileFixture fixture, Boolean zero) {
        for (map in restrictedSubordinateMaps) {
            if (!map.fixtures.get(point).map(TileFixture.id).any(fixture.id.equals)) {
                map.addFixture(point, fixture.copy(zero));
            }
        }
    }

    "Reduce the population of a group of plants, animals, etc., and copy the reduced form
     into all subordinate maps."
    shared actual void reducePopulation(Point location, HasPopulation<out TileFixture>&TileFixture fixture,
            Boolean zero, Integer reduction) {
        if (reduction.positive) {
            variable Boolean first = false;
            variable Boolean all = false;
            for (map in restrictedAllMaps) {
                if (exists matching = map.fixtures.get(location).narrow<HasPopulation<out TileFixture>>()
                        .find(shuffle(curry(fixture.isSubset))(noop))) {
                    if (all) {
                        map.removeFixture(location, matching);
                    } else if (matching.population.positive) {
                        Integer remaining = matching.population - reduction;
                        if (remaining.positive) {
                            value addend = matching.reduced(remaining);
                            map.replace(location, matching, addend.copy(first || zero));
                            continue;
                        } else if (first) {
                            all = true;
                        }
                        map.removeFixture(location, matching);
                    } else if (first) {
                        break;
                    } else {
                        map.addFixture(location, fixture.copy(zero));
                    }
                } else if (first) {
                    break;
                }
                first = false;
            }
        }
    }

    "Reduce the acreage of a fixture, and copy the reduced form into all subordinate maps."
    shared actual void reduceExtent(Point location,
            HasExtent<out HasExtent<out Anything>&TileFixture>&TileFixture fixture,
            Boolean zero, Decimal reduction) {
        if (reduction.positive) {
            variable Boolean first = false;
            variable Boolean all = false;
            for (map in restrictedAllMaps) {
                if (exists matching = map.fixtures.get(location).narrow<HasExtent<out TileFixture>>()
                        .find(shuffle(curry(fixture.isSubset))(noop))) {
                    if (all) {
                        map.removeFixture(location, matching);
                    } else if (matching.acres.positive) {
                        if (decimalize(matching.acres) > reduction) {
                            value addend = matching.reduced(reduction).copy(first || zero);
                            map.replace(location, matching, addend);
                            continue;
                        } else if (first) {
                            all = true;
                        }
                        map.removeFixture(location, matching);
                    } else if (first) {
                        break;
                    } else {
                        map.addFixture(location, fixture.copy(zero));
                    }
                } else if (first) {
                    break;
                }
                first = false;
            }
        }
    }

    "Add a Job to the matching worker in all maps. Returns [[true]] if a
     matching worker was found in at least one map, [[false]] otherwise. If
     an existing Job by that name already existed, it is left alone."
    shared actual Boolean addJobToWorker(IWorker worker, String jobName) {
        variable Boolean any = false;
        for (map in restrictedAllMaps) {
            if (exists matching = map.fixtures.items.flatMap(unflattenNonFortresses).narrow<IUnit>()
                    .flatMap(identity).narrow<IMutableWorker>()
                    .filter(matchingValue(worker.race, IWorker.race))
                    .filter(matchingValue(worker.name, IWorker.name))
                    .find(matchingValue(worker.id, IWorker.id))) {
                if (!matching.any(matchingValue(jobName, IJob.name))) {
                    map.modified = true;
                    matching.addJob(Job(jobName, 0));
                }
                any = true;
            }
        }
        return any;
    }

    "Add a skill, without any hours in it, to the specified worker in the
     specified Job in all maps. Returns [[true]] if a matching worker was found
     in at least one map, [[false]] otherwise. If no existing Job by that name
     already exists, a zero-level Job with that name is added first. If a Skill
     by that name already exists in the corresponding Job, it is left alone."
    shared actual Boolean addSkillToWorker(IWorker worker, String jobName, String skillName) {
        variable Boolean any = false;
        for (map in restrictedAllMaps) {
            if (exists matching = map.fixtures.items.flatMap(unflattenNonFortresses).narrow<IUnit>()
                    .flatMap(identity).narrow<IMutableWorker>()
                    .filter(matchingValue(worker.race, IWorker.race))
                    .filter(matchingValue(worker.name, IWorker.name))
                    .find(matchingValue(worker.id, IWorker.id))) {
                if (exists job = matching.narrow<IMutableJob>()
                        .find(matchingValue(jobName, IJob.name))) {
                    if (!job.any(matchingValue(skillName, ISkill.name))) {
                        map.modified = true;
                        job.addSkill(Skill(skillName, 0, 0));
                    }
                } else {
                    map.modified = true;
                    Job job = Job(jobName, 0);
                    job.addSkill(Skill(skillName, 0, 0));
                    matching.addJob(job);
                }
                any = true;
            }
        }
        return any;
    }

    "Add a skill, without any hours in it, to all workers in the specified Job
     in all maps. Returns [[true]] if at least one matching worker was found in
     at least one map, [[false]] otherwise. If a worker is in a different unit
     in some map, the Skill is still added to it. If no existing Job by that
     name already exists, a zero-level Job with that name is added first. If a
     Skill by that name already exists in the corresponding Job, it is left
     alone."
    shared actual Boolean addSkillToAllWorkers(IUnit unit, String jobName, String skillName) {
        variable Boolean any = false;
        for (worker in unit.narrow<IWorker>()) {
            if (addSkillToWorker(worker, jobName, skillName)) {
                any = true;
            }
        }
        return any;
    }

    "Add hours to a Skill to the specified Job in all workers in the given unit
     in all maps. (If a worker is in a different unit in some maps, that worker
     will still receive the hours.) Returns [[true]] if at least one worker
     received hours, [[false]] otherwise. If a worker doesn't have that skill
     in that Job, it is added first; if it doesn't have that Job, it is added
     first as in [[addJobToWorker]], then the skill is added to it. The
     [[contextValue]] is used to calculate a new value passed to
     [[strategicprimer.model.common.map.fixtures.mobile.worker::IMutableSkill.addHours]]
     for each worker."
    shared actual Boolean addHoursToSkillInAll(IUnit unit, String jobName, String skillName,
            Integer hours, Integer contextValue) {
        variable Boolean any = false;
        Random rng = DefaultRandom(contextValue);
        for (worker in unit.narrow<IWorker>()) {
            if (addHoursToSkill(worker, jobName, skillName, hours, rng.nextInteger(100))) {
                any = true;
            }
        }
        return any;
    }

    "Add hours to a Skill to the specified Job in the matching worker in all
     maps.  Returns [[true]] if a matching worker was found in at least one
     map, [[false]] otherwise. If the worker doesn't have that Skill in that
     Job, it is added first; if the worker doesn't have that Job, it is added
     first as in [[addJobToWorker]], then the skill is added to it. The
     [[contextValue]] is passed to
     [[strategicprimer.model.common.map.fixtures.mobile.worker::IMutableSkill.addHours]];
     it should be a random number between 0 and 99."
    shared actual Boolean addHoursToSkill(IWorker worker, String jobName, String skillName,
            Integer hours, Integer contextValue) {
        variable Boolean any = false;
        for (map in restrictedAllMaps) {
            if (exists matching = map.fixtures.items.flatMap(unflattenNonFortresses).narrow<IUnit>()
                    .flatMap(identity).narrow<IMutableWorker>()
                    .filter(matchingValue(worker.race, IWorker.race))
                    .filter(matchingValue(worker.name, IWorker.name))
                    .find(matchingValue(worker.id, IWorker.id))) {
                map.modified = true;
                any = true;
                IMutableJob job;
                if (exists temp = matching.narrow<IMutableJob>()
                        .find(matchingValue(jobName, IJob.name))) {
                    job = temp;
                } else {
                    job = Job(jobName, 0);
                    matching.addJob(job);
                }
                IMutableSkill skill;
                if (exists temp = job.narrow<IMutableSkill>()
                        .find(matchingValue(skillName, ISkill.name))) {
                    skill = temp;
                } else {
                    skill = Skill(skillName, 0, 0);
                    job.addSkill(skill);
                }
                skill.addHours(hours, contextValue);
            }
        }
        return any;
    }

    "Replace [[one skill|delenda]] with [[another|replacement]] in the specified job
     in the specified worker in all maps. Unlike [[addHoursToSkill]], if a map does
     not have an *equal* Job in the matching worker, that map is completely
     skipped.  If the replacement is already present, just remove the first
     skill. Returns [[true]] if the operation was carried out in any of the
     maps, [[false]] otherwise."
    shared actual Boolean replaceSkillInJob(IWorker worker, String jobName, ISkill delenda,
            ISkill replacement) {
        variable Boolean any = false;
        for (map in restrictedAllMaps) {
            if (exists matchingWorker = map.fixtures.items.flatMap(unflattenNonFortresses).narrow<IUnit>()
                    .flatMap(identity).narrow<IMutableWorker>()
                    .filter(matchingValue(worker.race, IWorker.race))
                    .filter(matchingValue(worker.name, IWorker.name))
                    .find(matchingValue(worker.id, IWorker.id))) {
                if (exists matchingJob = matchingWorker.narrow<IMutableJob>()
                            .find(matchingValue(jobName, IJob.name)),
                        exists matchingSkill = matchingJob.find(delenda.equals)) {
                    map.modified = true;
                    any = true;
                    matchingJob.removeSkill(matchingSkill);
                    matchingJob.addSkill(replacement.copy());
                } else {
                    log.warn("No matching skill in matching worker");
                }
            }
        }
        return any;
    }

    "Reduce the matching [[resource|IResourcePile]], in a
     [[unit|strategicprimer.model.common.map.fixtures.mobile::IUnit]] or
     [[fortress|IFortress]] owned by [[the specified player|owner]], by [[the
     specified amount|amount]]. Returns [[true]] if any (mutable) resource
     piles matched in any of the maps, [[false]] otherwise."
    shared actual Boolean reduceResourceBy(IResourcePile resource, Decimal amount, Player owner) {
        variable Boolean any = false;
        for (map in restrictedAllMaps) {
            for (container in map.fixtures.items.flatMap(partiallyFlattenFortresses)
                    .narrow<IMutableUnit|IMutableFortress>().filter(matchingValue(owner, HasOwner.owner))) {
                variable Boolean found = false;
                for (item in container.narrow<IMutableResourcePile>()) {
                    if (resource.isSubset(item, noop) ||
                            (resource.kind == item.kind && resource.contents == item.contents
                                && resource.id == item.id)) {
                        if (decimalize(item.quantity.number) <= amount) {
                            switch (container)
                            case (is IMutableUnit) { container.removeMember(item); }
                            else case (is IMutableFortress) { container.removeMember(item); }
                        } else {
                            item.quantity = Quantity(decimalize(item.quantity.number) - amount, resource.quantity.units);
                        }
                        map.modified = true;
                        any = true;
                        found = true;
                        break;
                    }
                }
                if (found) {
                    break;
                }
            }
        }
        return any;
    }

    "Remove the given [[resource|IResourcePile]] from a
     [[unit|strategicprimer.model.common.map.fixtures.mobile::IUnit]] or
     [[fortress|strategicprimer.model.common.map.fixtures.towns::IFortress]]
     owned by [[the specified player|owner]] in all maps. Returns [[true]] if
     any matched in any of the maps, [[false]] otherwise."
    deprecated("Use [[reduceResourceBy]] when possible instead.")
    shared actual Boolean removeResource(IResourcePile resource, Player owner) {
        variable Boolean any = false;
        for (map in restrictedAllMaps) {
            for (container in map.fixtures.items.flatMap(partiallyFlattenFortresses)
                    .narrow<IMutableUnit|IMutableFortress>().filter(matchingValue(owner, HasOwner.owner))) {
                variable Boolean found = false;
                for (item in container.narrow<IResourcePile>()) {
                    if (resource.isSubset(item, noop)) { // TODO: is that the right way around?
                        switch (container)
                        case (is IMutableUnit) { container.removeMember(item); }
                        else case (is IMutableFortress) { container.removeMember(item); }
                        map.modified = true;
                        any = true;
                        found = true;
                        break;
                    }
                }
                if (found) {
                    break;
                }
            }
        }
        return any;
    }

    "Set the given unit's orders for the given turn to the given text. Returns
     [[true]] if a matching (and mutable) unit was found in at least one map,
     [[false]] otherwise."
    shared actual Boolean setUnitOrders(IUnit unit, Integer turn, String results) {
        variable Boolean any = false;
        for (map in restrictedAllMaps) {
            if (exists matching = map.fixtures.items.flatMap(unflattenNonFortresses)
                    .narrow<IMutableUnit>().filter(matchingValue(unit.owner, IUnit.owner))
                    .filter(matchingValue(unit.kind, IUnit.kind))
                    .filter(matchingValue(unit.name, IUnit.name))
                    .find(matchingValue(unit.id, IUnit.id))) {
                matching.setOrders(turn, results);
                map.modified = true;
                any = true;
            }
        }
        return any;
    }

    "Set the given unit's results for the given turn to the given text. Returns
     [[true]] if a matching (and mutable) unit was found in at least one map,
     [[false]] otherwise."
    shared actual Boolean setUnitResults(IUnit unit, Integer turn, String results) {
        variable Boolean any = false;
        for (map in restrictedAllMaps) {
            if (exists matching = map.fixtures.items.flatMap(unflattenNonFortresses)
                    .narrow<IMutableUnit>().filter(matchingValue(unit.owner, IUnit.owner))
                    .filter(matchingValue(unit.kind, IUnit.kind))
                    .filter(matchingValue(unit.name, IUnit.name))
                    .find(matchingValue(unit.id, IUnit.id))) {
                matching.setResults(turn, results);
                map.modified = true;
                any = true;
            }
        }
        return any;
    }

    "Add a resource with the given ID, kind, contents, quantity, and (if
     provided) created date in the given unit or fortress in all maps.
     Returns [[true]] if a matching (and mutable) unit or fortress was found in
     at least one map, [[false]] otherwise."
    shared actual Boolean addResource(IUnit|IFortress container, Integer id, String kind, String contents,
            Quantity quantity, Integer? createdDate) {
        variable Boolean any = false;
        IMutableResourcePile resource = ResourcePileImpl(id, kind, contents, quantity);
        if (exists createdDate) {
            resource.created = createdDate;
        }
        for (map in restrictedAllMaps) {
            if (exists matching = map.fixtures.items.flatMap(partiallyFlattenFortresses)
                    .narrow<IMutableUnit|IMutableFortress>()
                    .filter(matchingValue(container.name, HasName.name))
                    .find(matchingValue(container.id, TileFixture.id))) {
                if (is IMutableFortress matching) {
                    matching.addMember(resource.copy(false));
                } else {
                    matching.addMember(resource.copy(false));
                }
                map.modified = true;
                any = true;
            }
        }
        return any;
    }

    "Add a non-talking animal population to the given unit in all maps. Returns
     [[true]] if the input makes sense and a matching (and mutable) unit was
     found in at least one map, [[false]] otherwise.

     Note the last two parameters are *reversed* from the
      [[strategicprimer.model.common.map.fixtures.mobile::AnimalImpl]]
      constructor, to better fit the needs of *our* callers."
    shared actual Boolean addAnimal(IUnit container, String kind, String status, Integer id,
            Integer population, Integer born) {
        if (!population.positive) {
            return false;
        }
        value animal = AnimalImpl(kind, false, status, id, born, population);
        variable Boolean any = false;
        for (map in restrictedAllMaps) {
            if (exists matching = map.fixtures.items.flatMap(unflattenNonFortresses)
                    .narrow<IMutableUnit>().filter(matchingValue(container.owner, IUnit.owner))
                    .filter(matchingValue(container.kind, IUnit.kind))
                    .filter(matchingValue(container.name, IUnit.name))
                    .find(matchingValue(container.id, IUnit.id))) {
                matching.addMember(animal.copy(false));
                any = true;
                map.modified = true;
            }
        }
        return any;
    }

    "Transfer [[quantity]] units from [[a resource|from]] to (if not all of it)
     another resource in [[a unit or fortress|to]] in all maps. If this leaves
     any behind in any map, [[id]] will be called exactly once to generate the
     ID number for the resource in the destination in maps where that is the
     case. Returns [[true]] if a matching (mutable) resource and destination
     are found (and the transfer occurs) in any map, [[false]] otherwise."
    shared actual Boolean transferResource(IResourcePile from, IUnit|IFortress to,
            Decimal quantity, Integer() idFactory) {
        variable Boolean any = false;
        variable Integer? generatedId = null;
        Integer id() {
            if (exists temp = generatedId) {
                return temp;
            } else {
                Integer temp = idFactory();
                generatedId = temp;
                return temp;
            }
        }
        for (map in restrictedAllMaps) {
            for (container in map.fixtures.items.flatMap(partiallyFlattenFortresses)
                    .narrow<IMutableUnit|IMutableFortress>()
		    .filter(matchingValue(to.owner, HasOwner.owner))) {
                if (exists matching = container.narrow<IMutableResourcePile>()
                            .filter(matchingValue(from.kind, IResourcePile.kind))
                            .filter(matchingValue(from.contents, IResourcePile.contents))
                            .filter(matchingValue(from.created, IResourcePile.created))
                            .filter(matchingValue(from.quantity.units, compose(Quantity.units,
                                IResourcePile.quantity)))
                            .find(matchingValue(from.id, IResourcePile.id)),
                        exists destination = map.fixtures.items.flatMap(partiallyFlattenFortresses)
                            .narrow<IMutableUnit|IMutableFortress>()
                            .filter(matchingValue(to.name, HasName.name))
                            .find(matchingValue(to.id, TileFixture.id))) {
                    map.modified = true;
                    if (quantity >= decimalize(matching.quantity.number)) {
                        if (is IMutableFortress container) { // TODO: Combine with other block when a supertype is added for this method
                            container.removeMember(matching);
                        } else {
                            container.removeMember(matching);
                        }
                        if (is IMutableFortress destination) {
                            destination.addMember(matching);
                        } else {
                            destination.addMember(matching);
                        }
                    } else {
                        IMutableResourcePile split = ResourcePileImpl(id(), matching.kind, matching.contents,
                            Quantity(quantity, matching.quantity.units));
                        split.created = matching.created;
                        matching.quantity = Quantity(decimalize(matching.quantity.number) - quantity,
                            matching.quantity.units);
                    }
                    any = true;
                    break;
                }
            }
        }
        return any;
    }

    "Add (a copy of) an existing resource to the fortress belonging to the
     given player with the given name, or failing that to any fortress
     belonging to the given player, in all maps. Returns [[true]] if a matching
     (and mutable) fortress ws found in at least one map, [[false]] otherwise."
    // TODO: Make a way to add to units
    shared actual Boolean addExistingResource(FortressMember resource, Player owner, String fortName) {
        variable Boolean any = false;
        for (map in restrictedAllMaps) {
            IMutableFortress result;
            variable IMutableFortress? temp = null;
            for (item in map.fixtures.items.narrow<IMutableFortress>()
                    .filter(matchingValue(owner, IMutableFortress.owner))) {
                if (item.name == fortName) {
                    result = item;
                    break;
                } else if (!temp exists) {
                    temp = item;
                }
            } else {
                if (exists fortress = temp) {
                    result = fortress;
                } else {
                    continue;
                }
            }
            any = true;
            map.modified = true;
            result.addMember(resource.copy(false));
        }
        return any;
    }
}
