import ceylon.collection {
    ArrayList,
    MutableList,
    MutableSet,
    HashSet
}

import java.util {
    Formatter
}

import lovelace.util.common {
    todo
}

import strategicprimer.viewer.model.map.fixtures.mobile {
    IWorker,
    ProxyFor
}
import model.map.fixtures.mobile.worker {
    ISkill
}
"An IJob implementation to let the Job tree operate on a whole unit at once."
shared class ProxyJob(name, parallel, IWorker* proxiedWorkers) satisfies IJob&ProxyFor<IJob> {
    """If false, the worker containing this is representing all the workers in a single
       unit; if true, it is representing corresponding workers in corresponding units in
       different maps. Thus, if true, we should use the same "random" seed repeatedly in
       any given adding-hours operation, and not if false."""
    shared actual Boolean parallel;
    "Jobs we're proxying."
    MutableList<IJob> proxiedJobs = ArrayList<IJob>();
    "The names of skills we're proxying."
    MutableSet<String> skillNames = HashSet<String>();
    "The name of the Job."
    shared actual String name;
    for (worker in proxiedWorkers) {
        variable Boolean unmodified = true;
        for (job in worker) {
            if (name == job.name) {
                proxiedJobs.add(job);
                skillNames.addAll(job.map(ISkill.name));
                unmodified = false;
            }
        }
        if (unmodified) {
            IJob job = Job(name, 0);
            worker.addJob(job);
            proxiedJobs.add(worker.find((temp) => temp.name == name) else job);
        }
    }
    "Proxy-skills."
    MutableList<ISkill&ProxyFor<IJob>> proxiedSkills =
            ArrayList<ISkill&ProxyFor<IJob>> { *skillNames
                .map((skill) => ProxySkill(skill, parallel, *proxiedJobs)) };
    shared actual IJob copy() {
        ProxyJob retval = ProxyJob(name, parallel);
        for (job in proxiedJobs) {
            retval.addProxied(job.copy());
        }
        return retval;
    }
    shared actual Iterator<ISkill> iterator() => proxiedSkills.iterator();
    "Add a skill; returns false if we were already proxying a skill by that name, and true
     otherwise."
    shared actual Boolean addSkill(ISkill skill) {
        if (proxiedSkills.map(ISkill.name).any(skill.name.equals)) {
            return false;
        } else {
            proxiedSkills.add(ProxySkill(skill.name, parallel, *proxiedJobs));
            return true;
        }
    }
    "The lowest level among proxied jobs."
    shared actual Integer level => Integer.min(proxiedJobs.map(IJob.level)) else 0;
    "Set the proxied jobs' level."
    assign level {
        log.warn("ProxyJob.level assigned");
        for (job in proxiedJobs) {
            job.level = level;
        }
    }
    "Delegates to [[name]]."
    todo("Indicate we're a proxy?")
    shared actual String string => name;
    shared actual Boolean isSubset(IJob obj, Formatter ostream, String context) {
        ostream.format("%s\tisSubset called on ProxyJob%n", context);
        return false;
    }
    "Proxy an additional Job."
    shared actual void addProxied(IJob item) {
        if (is Identifiable item, item === this) {
            return;
        } else if (name != item.name) {
            return;
        }
        proxiedJobs.add(item);
        for (skill in proxiedSkills) {
            skill.addProxied(item);
        }
        for (skill in item) {
            if (!skillNames.contains(skill.name)) {
                proxiedSkills.add(ProxySkill(skill.name, parallel, *proxiedJobs));
            }
        }
    }
    "A view of the proxied Jobs."
    shared actual Iterable<IJob> proxied => {*proxiedJobs};
    """Whether all of the Jobs this is a proxy for are "empty," i.e. having no levels and
       containing no Skills that report either levels or hours of experience."""
    todo("When porting IJob, make sure to rename this")
    shared actual Boolean emptyJob => proxiedJobs.every(IJob.emptyJob);
    "Get a Skill by name."
    shared actual ISkill getSkill(String skillName) {
        if (exists retval = proxiedSkills.find((skill) => skill.name == skillName)) {
            return retval;
        }
        value retval = ProxySkill(skillName, parallel, *proxiedJobs);
        proxiedSkills.add(retval);
        return retval;
    }
}