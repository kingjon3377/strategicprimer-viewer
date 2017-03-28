import ceylon.collection {
    MutableList,
    ArrayList
}
import ceylon.interop.java {
    JavaIterator,
    CeylonIterable
}
import ceylon.language {
    createMap=map
}

import java.util {
    JIterator=Iterator,
    Formatter
}

import lovelace.util.common {
    todo
}

import model.map {
    HasPortrait,
    IFixture
}
import strategicprimer.viewer.model.map.fixtures.mobile.worker {
    WorkerStats,
    Job,
    IJob
}
"Whether neither of two collections of Jobs contains a nonempty Job the other does not."
todo("Make sure to change `.empty` once [[IJob]] is ported.")
Boolean jobSetsEqual({IJob*} first, {IJob*} second) =>
        set { *first.filter((job) => !job.emptyJob) } == set { *second.filter((job) => !job.emptyJob) };
"Whether two nullable values are either neither present or both present and equal."
Boolean nullablesEqual(Anything one, Anything two) {
    if (exists one) {
        if (exists two) {
            return one == two;
        }
        return false;
    }
    return !two exists;
}
"A worker (or soldier) in a unit. This is deliberately not a [[TileFixture]]: these should
 only be part of a unit, not as a top-level tag."
todo("Convert some other [[MobileFixture]]s similarly?")
shared class Worker(name, race, id, IJob* jobs) satisfies IWorker&HasPortrait {
    //MutableSet<IJob> jobSet = ArraySet<IJob> { *jobs };
    "The set of Jobs the worker is trained or experienced in."
    todo("Convert back to Set once ArraySet is ported.")
    MutableList<IJob> jobSet = ArrayList { *jobs };
    "The worker's ID number."
    shared actual Integer id;
    "The worker's name."
    shared actual String name;
    "The worker's race (elf, dwarf, human, etc.)"
    shared actual String race;
    "The worker's stats."
    shared actual variable WorkerStats? stats = null;
    "The filename of an image to use as an icon for this instance."
    variable String imageFilename = "";
    shared actual String image => imageFilename;
    shared actual void setImage(String image) => imageFilename = image;
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
                && fixture.race == race && nullablesEqual(stats, fixture.stats);
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
    shared actual String kind => race;
    "The filename of the icon to use by default. This is just for icons in lists and such,
     not the map, since this isn't a [[TileFixture]]."
    shared actual String defaultImage = "worker.png";
    "A fixture is a subset if it is a worker with the same ID, name, race, and stats, and
     no Jobs we don't have, and its Jobs are subsets of our corresponding Jobs."
    shared actual Boolean isSubset(IFixture obj, Formatter ostream, String context) {
        if (obj.id == id) {
            if (is IWorker obj) {
                if (name != obj.name) {
                    ostream.format("%s In worker %s (ID #%d):\tNames differ%n", context,
                        name, id);
                    return false;
                } else if (race != obj.race) {
                    ostream.format("%s In worker %s (ID %d):\tRaces differ%n", context,
                        name, id);
                    return false;
                } else if (!nullablesEqual(stats, obj.stats)) {
                    ostream.format("%s In worker %s (ID %d):\tStats differ%n", context,
                        name, id);
                    return false;
                }
                Map<String, IJob> ours = createMap { *map((job) => job.name->job) };
                variable Boolean retval = true;
                for (job in obj) {
                    if (exists corresponding = ours.get(job.name)) {
                        if (!corresponding.isSubset(job, ostream,
                                "``context`` In worker ``name`` (ID #``id``):")) {
                            retval = false;
                        }
                    } else {
                        // TODO: skip empty Jobs?
                        ostream.format("%s In worker %s (ID %d):\tExtra Job: %s%n",
                            context, name, id, job.name);
                        retval = false;
                    }
                }
                return retval;
            } else {
                ostream.format("%sFor ID #%d, different kinds of members%n", context, id);
                return false;
            }
        } else {
            ostream.format("%sCalled with different IDs, #%d and #%d%n", context, id,
                obj.id);
            return false;
        }
    }
    "Clone the object."
    shared actual Worker copy(Boolean zero) {
        Worker retval = Worker(name, race, id);
        retval.setImage(image);
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
        if (exists retval = find((job) => job.name == name)) {
            return retval;
        } else {
            IJob retval = Job(name, 0);
            jobSet.add(retval);
            return retval;
        }
    }
}