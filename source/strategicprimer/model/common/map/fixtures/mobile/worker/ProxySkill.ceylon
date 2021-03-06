import ceylon.collection {
    MutableList,
    ArrayList
}
import lovelace.util.common {
    todo,
    matchingValue
}

import strategicprimer.model.common.map.fixtures.mobile {
    ProxyFor
}

"An implementation of ISkill whose operations act on multiple workers at once."
todo("Figure out how we can make this satisfy ProxyFor<ISkill>?")
class ProxySkill(name, parallel, IJob* proxiedJobsStream)
        satisfies ISkill&ProxyFor<IJob> {
    """If false, the worker containing this is representing all the workers in a single
       unit; if true, it is representing corresponding workers in corresponding units in
       different maps. Thus, if true, we should use the same "random" seed repeatedly in
       any given adding-hours operation, and not if false."""
    todo("We should always probably return true in the API, since this is a
          [[ProxyFor<IJob>]], and use a private variable for the
          parallel-or-corresponding-Worker question.")
    shared actual Boolean parallel;

    MutableList<IJob> proxiedJobs = ArrayList<IJob> { elements = proxiedJobsStream; };

    "The name of the skill we're proxying across workers."
    shared actual String name;

    IJob copyJob(IJob job) => job.copy();

    "Clone this object."
    shared actual ISkill copy() =>
            ProxySkill(name, parallel, *proxiedJobs.map(copyJob));

    "The lowest level that any proxied Job has in the skill."
    shared actual Integer level =>
            Integer.min(proxiedJobs.flatMap(identity).filter(notThis)
                .filter(matchingValue(name, ISkill.name)).map(ISkill.level)) else 0;

    Boolean notThis(Anything obj) {
        if (is Identifiable obj) {
            return !(obj === this);
        } else {
            return true;
        }
    }

    "The most hours any of the proxied Jobs has for the skill."
    shared actual Integer hours {
        return Integer.max(proxiedJobs.flatMap(identity).filter(notThis)
            .filter(matchingValue(name, ISkill.name)).map(ISkill.hours)) else 0;
    }

    "The name of the skills."
    todo("Indicate this is a proxy?")
    shared actual String string => name;

    "Add a Job to the list of Jobs we're proxying a skill for."
    shared actual void addProxied(IJob item) => proxiedJobs.add(item);

    "Note that this is the *one* place where [[ProxySkill]] should be a
     [[ProxyFor<ISkill>]] instead of a [[ProxyFor<IJob>]]."
    shared actual {IJob*} proxied => proxiedJobs.sequence();

    """Whether every proxied Skill is "empty"."""
    shared actual Boolean empty =>
            proxiedJobs.flatMap(identity).filter(notThis)
                .filter(matchingValue(name, ISkill.name)).every(ISkill.empty);

    shared actual Boolean isSubset(ISkill obj, Anything(String) report) {
        report("isSubset called on ProxySkill");
        return false;
    }

    shared actual Boolean equals(Object that) {
        if (is ISkill that) {
            return that.name == name && that.level == level && that.hours == hours;
        } else {
            return false;
        }
    }

    shared actual Integer hash => name.hash;
}
