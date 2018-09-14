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
import strategicprimer.model.map {
    IFixture,
    HasPortrait
}

import strategicprimer.model.map.fixtures.mobile.worker {
    WorkerStats,
    Job,
    IJob
}
import ceylon.collection {
    MutableSet
}
"A worker (or soldier) in a unit. This is deliberately not a
 [[TileFixture|strategicprimer.model.map::TileFixture]]: these should only be part of a
 unit, not as a top-level tag."
todo("Convert some other [[MobileFixture]]s similarly?")
shared class Worker satisfies IWorker&HasPortrait {
    "Whether neither of two collections of Jobs contains a nonempty Job the other does not."
    static Boolean jobSetsEqual({IJob*} first, {IJob*} second) =>
            set(first.filter(matchingValue(false, IJob.emptyJob))) ==
            set(second.filter(matchingValue(false, IJob.emptyJob)));
    "The set of Jobs the worker is trained or experienced in."
    MutableSet<IJob> jobSet;
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
    shared actual Boolean equalsIgnoringID(IFixture fixture) {
        if (is IWorker fixture) {
            return fixture.name == name && jobSetsEqual(jobSet, fixture)
                && fixture.race == race && anythingEqual(stats, fixture.stats);
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
     [[TileFixture|strategicprimer.model.map::TileFixture]]."
    shared actual String defaultImage = "worker.png";
    "A fixture is a subset if it is a worker with the same ID, name, race, and stats, and
     no Jobs we don't have, and its Jobs are subsets of our corresponding Jobs."
    shared actual Boolean isSubset(IFixture obj, Anything(String) report) {
        if (obj.id == id) {
            if (is IWorker obj) {
                void localReport(String string) =>
                        report("In worker ``name`` (ID #``id``):\t``string``)`");
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
}
