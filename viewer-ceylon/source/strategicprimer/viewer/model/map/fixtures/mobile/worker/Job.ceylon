import ceylon.collection {
    MutableMap,
    HashMap
}

import java.lang {
    IllegalArgumentException
}
import java.util {
    Formatter
}

import lovelace.util.common {
    todo
}

"A Job a worker can work at or have training or experience in."
shared class Job(name, levelNum, ISkill* skills) satisfies IJob {
    "The worker's level in various skills associated with the Job."
    MutableMap<String, ISkill> skillSet = HashMap<String, ISkill>();
    "The name of the Job."
    shared actual String name;
    "How many levels the worker has in the Job."
    variable Integer levelNum;
    "How many levels the worker has in the Job."
    shared actual Integer level => levelNum;
    assign level {
        if (level < 0) {
            throw IllegalArgumentException("Job level cannot be negative");
        }
        levelNum = level;
    }
    "Add a skill. Returns false if an equal skill was already in the collection, and true
     otherwise."
    todo("Is the return value really needed?")
    shared actual Boolean addSkill(ISkill skill) {
        if (exists existing = skillSet.get(skill.name), existing == skill) {
                return false;
        } else {
            skillSet.put(skill.name, skill);
            return true;
        }
    }
    for (skill in skills) {
        addSkill(skill);
    }
    "Clone the Job."
    shared actual IJob copy() => Job(name, level, *skillSet.items);
    "An iterator over (the worker's level in) the Skills in this Job."
    shared actual Iterator<ISkill> iterator() => skillSet.items.iterator();
    "A Job is equal to another object iff it is a Job with the same name and level and
     identical skills."
    todo("Specify IJob instead of the Job class?")
    shared actual Boolean equals(Object obj) {
        if (is Job obj) {
            return name == obj.name && level == obj.level &&
                skillSet.items.containsEvery(obj) &&
                obj.containsEvery(skillSet.items);
        } else {
            return false;
        }
    }
    """A Job is a "subset" if it has the same name, equal or lower level, and no extra or
       higher-level or extra-experienced Skills."""
    todo("Perhaps a lower-level Job with extra skills should still be a subset?")
    shared actual Boolean isSubset(IJob obj, Formatter ostream, String context) {
        if (name != obj.name) {
            ostream.format("%s\tPassed Jobs with different names%n", context);
            return false;
        } else if (obj.level > level) {
            ostream.format("%s\tSubmap has higher level for Job %s%n", context, name);
            return false;
        } else {
            variable Boolean retval = true;
            for (skill in obj) {
                if (exists ours = skillSet.get(skill.name)) {
                    retval = retval && ours.isSubset(skill, ostream,
                        "``context`` In Job ``name``:");
                } else {
                    ostream.format("%s In Job %s:\tExtra skill %s%n", context, name,
                        skill.name);
                    retval = false;
                }
            }
            return retval;
        }
    }
    "For stability, only the name is used to compute the hash value."
    shared actual Integer hash => name.hash;
    shared actual String string => "``name`` (``level``)";
    """A Job is "empty" if the worker has no levels in it and no experience in the skills
       it contains."""
    shared actual Boolean emptyJob => level == 0 && skillSet.items.every(ISkill.empty);
    "Get a Skill by name, or a newly-constructed empty one if we didn't have one."
    shared actual ISkill getSkill(String skillName) {
        if (exists skill = skillSet.get(skillName)) {
            return skill;
        } else {
            ISkill skill = Skill(skillName, 0, 0);
            skillSet.put(skillName, skill);
            return skill;
        }
    }
}