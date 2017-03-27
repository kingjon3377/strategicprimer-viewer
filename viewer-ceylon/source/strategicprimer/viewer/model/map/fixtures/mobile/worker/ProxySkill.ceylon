import ceylon.collection {
    MutableList,
    ArrayList
}
import ceylon.interop.java {
    CeylonIterable,
    JavaIterable
}

import java.util {
    JRandom=Random,
    Formatter
}
import java.lang {
    JIterable=Iterable
}
import lovelace.util.common {
    todo
}

import model.map.fixtures.mobile {
    ProxyFor
}
import model.map.fixtures.mobile.worker {
    ISkill,
    IJob,
    Skill
}
"An implementation of ISkill whose operations act on multiple workers at once."
todo("Figure out how we can make this satisfy ProxyFor<ISkill>?")
class ProxySkill(name, parallel, IJob* proxiedJobsStream) satisfies ISkill&ProxyFor<IJob> {
    """If false, the worker containing this is representing all the workers in a single
       unit; if true, it is representing corresponding workers in corresponding units in
       different maps. Thus, if true, we should use the same "random" seed repeatedly in
       any given adding-hours operation, and not if false."""
    todo("We should always probably return true in the API, since this is a
          [[ProxyFor<IJob>]], and use a private variable for the
          parallel-or-corresponding-Worker question.")
    shared actual Boolean parallel;
    MutableList<IJob> proxiedJobs = ArrayList<IJob> { *proxiedJobsStream };
    "The name of the skill we're proxying across workers."
    shared actual String name;
    "Clone this object."
    shared actual ISkill copy() =>
            ProxySkill(name, parallel, *proxiedJobs.map((job) => job.copy()));
    "The lowest level that any proxied Job has in the skill."
    shared actual Integer level {
        variable Integer? retval = null;
        for (job in proxiedJobs) {
            for (skill in CeylonIterable(job).filter(notThis)) {
                if (skill.name == name,
                        skill.level < (retval else runtime.maxIntegerValue)) {
                    retval = skill.level;
                }
            }
        }
        return retval else 0;
    }
    Boolean notThis(Anything obj) {
        if (is Identifiable obj) {
            return !(obj === this);
        } else {
            return true;
        }
    }
    "The most hours any of the proxied Jobs has for the skill."
    shared actual Integer hours {
        return Integer.max(proxiedJobs.flatMap(CeylonIterable<ISkill>).filter(notThis)
            .map(ISkill.hours)) else 0;
    }
    "Add hours to the proxied skills."
    shared actual void addHours(Integer hours, Integer condition) {
        if (parallel) {
            for (job in proxiedJobs) {
                variable Boolean unmodified = true;
                for (skill in CeylonIterable(job).filter(notThis)) {
                    if (skill.name == name) {
                        skill.addHours(hours, condition);
                        unmodified = false;
                    }
                }
                if (unmodified) {
                    ISkill skill = Skill(name, 0, 0);
                    skill.addHours(hours, condition);
                    job.addSkill(skill);
                }
            }
        } else {
            // TODO: figure out how to use "Ceylon-native" RNG interfaces
            JRandom random = JRandom(condition);
            for (job in proxied) {
                variable Boolean unmodified = true;
                for (skill in CeylonIterable(job).filter(notThis)) {
                    if (skill.name == name) {
                        skill.addHours(hours, random.nextInt(100));
                        unmodified = false;
                    }
                }
                if (unmodified) {
                    ISkill skill = Skill(name, 0, 0);
                    job.addSkill(skill);
                    (CeylonIterable(job).find((temp) => temp.name == name) else skill)
                        .addHours(hours, random.nextInt(100));
                }
            }
        }
    }
    "The name of the skills."
    todo("Indicate this is a proxy?")
    shared actual String string => name;
    "Add a Job to the list of Jobs we're proxying a skill for."
    shared actual void addProxied(IJob item) => proxiedJobs.add(item);
    "Note that this is the *one* place where [[ProxySkill]] should be a
     [[ProxyFor<ISkill>]] instead of a [[ProxyFor<IJob>]]."
    shared actual JIterable<IJob> proxied => JavaIterable(proxiedJobs);
    """Whether every proxied Skill is "empty"."""
    shared actual Boolean empty =>
            proxiedJobs.flatMap(CeylonIterable<ISkill>).filter(notThis)
                .filter((skill) => skill.name == name).any((skill) => !skill.empty);
    shared actual Boolean isSubset(ISkill obj, Formatter ostream, String context) {
        ostream.format("%s\tisSubset called on ProxySkill%n", context);
        return false;
    }
}