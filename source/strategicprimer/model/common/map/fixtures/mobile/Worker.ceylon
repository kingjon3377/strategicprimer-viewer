import ceylon.language {
    createMap=map
}

import lovelace.util.common {
    todo,
    ArraySet,
    anythingEqual,
    matchingValue,
    entryBy
}
import strategicprimer.model.common.map {
    IFixture,
    Player
}

import strategicprimer.model.common.map.fixtures.mobile.worker {
    WorkerStats,
    Job,
    IJob
}
import ceylon.collection {
    ArrayList,
    MutableList,
    MutableMap,
    HashMap,
    MutableSet
}

import strategicprimer.model.common.map.fixtures {
    Implement
}

"A worker (or soldier) in a unit. This is deliberately not a
 [[TileFixture|strategicprimer.model.common.map::TileFixture]]: these should only be part
 of a unit, not as a top-level tag."
todo("Convert some other [[MobileFixture]]s similarly?")
shared class Worker satisfies IMutableWorker {
    "Whether neither of two collections of Jobs contains a nonempty Job the other does
     not."
    static Boolean jobSetsEqual({IJob*} first, {IJob*} second) =>
            set(first.filter(not(IJob.emptyJob))) ==
            set(second.filter(not(IJob.emptyJob)));

    "The set of Jobs the worker is trained or experienced in."
    MutableSet<IJob> jobSet;

    "The notes players have associaed with this worker"
    MutableMap<Integer, String> notesImpl = HashMap<Integer, String>();

    MutableList<Implement> equipmentImpl = ArrayList<Implement>();

    "The worker's ID number."
    shared actual Integer id;

    "The worker's name."
    shared actual String name;

    "The worker's race (elf, dwarf, human, etc.)"
    shared actual String race;

    shared new (String name, String race, Integer id, IJob* jobs) {
        this.name = name;
        this.race = race;
        this.id = id;
        jobSet = ArraySet<IJob>(jobs);
    }

    "The worker's stats."
    shared actual variable WorkerStats? stats = null;

    "The filename of an image to use as an icon for this instance."
    shared actual variable String image = "";

    "The filename of an image to use as a portrait for the worker."
    shared actual variable String portrait = "";

    "Add a Job."
    shared actual Boolean addJob(IJob job) {
        Integer size = jobSet.size;
        jobSet.add(job);
        return size != jobSet.size;
    }

    "An iterator over the worker's Jobs."
    shared actual Iterator<IJob> iterator() => jobSet.iterator();

    shared actual variable Animal? mount = null;

    shared actual Boolean equalsIgnoringID(IFixture fixture) {
        if (is IWorker fixture) {
            return fixture.name == name && jobSetsEqual(jobSet, fixture)
                && fixture.race == race && anythingEqual(stats, fixture.stats)
                && set(fixture.equipment) == set(equipmentImpl)
                && anythingEqual(mount, fixture.mount);
        } else {
            return false;
        }
    }

    shared actual Boolean equals(Object obj) {
        if (is IWorker obj) {
            return obj.id == id && equalsIgnoringID(obj);
        } else {
            return false;
        }
    }

    shared actual Integer hash => id;

    "We only use the worker's name and race for `string`."
    shared actual String string =>
            ("human" == race) then name else "``name``, a ``race``";

    "The filename of the icon to use by default. This is just for icons in lists and such,
     not the map, since this isn't a
     [[TileFixture|strategicprimer.model.common.map::TileFixture]]."
    shared actual String defaultImage = "worker.png";

    "A fixture is a subset if it is a worker with the same ID, name, race, and stats, and
     no Jobs we don't have, and its Jobs are subsets of our corresponding Jobs."
    shared actual Boolean isSubset(IFixture obj, Anything(String) report) {
        if (obj.id == id) {
            if (is IWorker obj) {
                Anything(String) localReport =
                        compose(report, "In worker ``name`` (ID #``id``):\t".plus);
                if (name != obj.name) {
                    localReport("Names differ");
                    return false;
                } else if (race != obj.race) {
                    localReport("Races differ");
                    return false;
                } else if (!anythingEqual(stats, obj.stats)) {
                    localReport("Stats differ");
                    return false;
                }
                Map<String, IJob> ours =
                        createMap(map(entryBy(IJob.name, identity<IJob>)));
                variable Boolean retval = true;
                for (job in obj) {
                    if (exists corresponding = ours[job.name]) {
                        if (!corresponding.isSubset(job, localReport)) {
                            retval = false;
                        }
                    } else if (!job.empty) {
                        localReport("Extra Job: ``job.name``");
                        retval = false;
                    }
                }
                if (exists theirMount = obj.mount) {
                    if (exists ourMount = mount, ourMount != theirMount) {
                        localReport("Mounts differ");
                        retval = false;
                    } else if (!mount exists) {
                        localReport("Has mount we don't");
                        retval = false;
                    }
                }
                for (item in obj.equipment) {
                    if (!item in equipment) {
                        localReport("Extra equipment: ``item``");
                        retval = false;
                    }
                }
                return retval;
            } else {
                report("For ID #``id``, different kinds of members");
                return false;
            }
        } else {
            report("Called with different IDs, #``id`` and #``obj.id``");
            return false;
        }
    }

    "Clone the object."
    shared actual Worker copy(Boolean zero) {
        Worker retval = Worker(name, race, id);
        retval.image = image;
        if (!zero) {
            if (exists localStats = stats) {
                retval.stats = localStats.copy();
            }
            for (job in this) {
                if (!job.emptyJob) {
                    retval.addJob(job.copy());
                }
            }
            if (exists temp = mount) {
                retval.mount = temp.copy(zero);
            }
            for (item in equipment) {
                retval.addEquipment(item.copy(zero));
            }
        }
        return retval;
    }

    "Get a Job by name: the Job by that name the worker has, or a newly-constructed one if
     it didn't have one."
    shared actual IJob getJob(String name) {
        if (exists retval = find(matchingValue(name, IJob.name))) {
            return retval;
        } else {
            IJob retval = Job(name, 0);
            jobSet.add(retval);
            return retval;
        }
    }

    shared actual object notes satisfies Correspondence<Player|Integer, String>&KeyedCorrespondenceMutator<Player, String> {
        shared actual Boolean defines(Player|Integer key) {
            switch (key)
            case (is Player) { return notesImpl.defines(key.playerId); }
            case (is Integer) { return notesImpl.defines(key); }
        }

        shared actual String? get(Player|Integer key) {
            switch (key)
            case (is Player) { return notesImpl[key.playerId]; }
            case (is Integer) { return notesImpl[key]; }
        }

        shared actual void put(Player key, String item) {
            if (item.empty) {
                notesImpl.remove(key.playerId);
            } else {
                notesImpl[key.playerId] = item;
            }
        }
    }

    shared actual {Integer*} notesPlayers => notesImpl.keys;

    shared actual {Implement*} equipment => equipmentImpl;

    shared actual void addEquipment(Implement item) => equipmentImpl.add(item);

    shared actual void removeEquipment(Implement item) => equipmentImpl.remove(item);
}
