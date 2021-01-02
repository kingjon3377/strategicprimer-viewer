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
    Fortress
}

import ceylon.logging {
    Logger,
    logger
}

import strategicprimer.model.common.map.fixtures {
    IMutableResourcePile,
    IResourcePile,
    Quantity,
    ResourcePileImpl
}

"Logger."
Logger log = logger(`module strategicprimer.viewer`);

shared class TurnRunningModel extends ExplorationModel satisfies ITurnRunningModel {
    "If [[fixture]] is a [[Fortress]], return it; otherwise, return a Singleton
     containing it. This is intended to be used in [[Iterable.flatMap]]."
    static {IFixture*} unflattenNonFortresses(TileFixture fixture) {
        if (is Fortress fixture) {
            return fixture;
        } else {
            return Singleton(fixture);
        }
    }

    "If [[fixture]] is a fortress, return a stream of it and its contents; otherwise,
     return a singleton containing the fixture. This is intended to be used in [[Iterable.flatMap]]."
    static {IFixture*} partiallyFlattenFortresses(TileFixture fixture) {
        if (is Fortress fixture) {
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
                    .filter(matchingValue(map.currentPlayer, IUnit.owner))
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
                    .filter(matchingValue(map.currentPlayer, IUnit.owner))
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
                    .filter(matchingValue(map.currentPlayer, IUnit.owner))
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
     [[fortress|Fortress]] owned by [[the specified player|owner]], by [[the
     specified amount|amount]]. Returns [[true]] if any (mutable) resource
     piles matched in any of the maps, [[false]] otherwise."
    shared actual Boolean reduceResourceBy(IResourcePile resource, Decimal amount, Player owner) {
        variable Boolean any = false;
        for (map in restrictedAllMaps) {
            for (container in map.fixtures.items.flatMap(partiallyFlattenFortresses)
                    .narrow<IMutableUnit|Fortress>().filter(matchingValue(owner, HasOwner.owner))) {
                variable Boolean found = false;
                for (item in container.narrow<IMutableResourcePile>()) {
                    if (resource.isSubset(item, noop)) { // TODO: is that the right way around?
                        if (decimalize(item.quantity.number) <= amount) {
                            switch (container)
                            case (is IMutableUnit) { container.removeMember(item); }
                            else case (is Fortress) { container.removeMember(item); }
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
     [[fortress|strategicprimer.model.common.map.fixtures.towns::Fortress]]
     owned by [[the specified player|owner]] in all maps. Returns [[true]] if
     any matched in any of the maps, [[false]] otherwise."
    deprecated("Use [[reduceResourceBy]] when possible instead.")
    shared actual Boolean removeResource(IResourcePile resource, Player owner) {
        variable Boolean any = false;
        for (map in restrictedAllMaps) {
            for (container in map.fixtures.items.flatMap(partiallyFlattenFortresses)
                    .narrow<IMutableUnit|Fortress>().filter(matchingValue(owner, HasOwner.owner))) {
                variable Boolean found = false;
                for (item in container.narrow<IResourcePile>()) {
                    if (resource.isSubset(item, noop)) { // TODO: is that the right way around?
                        switch (container)
                        case (is IMutableUnit) { container.removeMember(item); }
                        else case (is Fortress) { container.removeMember(item); }
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
    shared actual Boolean addResource(IUnit|Fortress container, Integer id, String kind, String contents,
            Quantity quantity, Integer? createdDate) {
        variable Boolean any = false;
        IMutableResourcePile resource = ResourcePileImpl(id, kind, contents, quantity);
        if (exists createdDate) {
            resource.created = createdDate;
        }
        for (map in restrictedAllMaps) {
            if (exists matching = map.fixtures.items.flatMap(partiallyFlattenFortresses)
                    .narrow<IMutableUnit|Fortress>()
                    .filter(matchingValue(container.name, HasName.name))
                    .find(matchingValue(container.id, TileFixture.id))) {
                if (is Fortress matching) {
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
}
