import ceylon.collection {
    MutableMap,
    HashMap,
    MutableSet,
    HashSet
}

import lovelace.util.common {
    todo
}
import strategicprimer.model.map.fixtures {
    ResourcePile
}
import strategicprimer.model.map {
    Subsettable
}
todo("Convert to an interface, or otherwise provide an alternative for non-active
      communities to suggest contents to be found there",
    "Allow towns to contain Workers, so inhabitants players know about can be
     represented")
shared class CommunityStats(populationCount) satisfies Subsettable<CommunityStats?> {
    "Approximately how many adults live in the community."
    variable Integer populationCount;
    "Population cannot be negative"
    assert (populationCount >= 0);
    "Approximately how many adults live in the community."
    shared Integer population => populationCount;
    assign population {
        "Population cannot be negative"
        assert (population >= 0);
        populationCount = population;
    }
    MutableMap<String, Integer> skillLevels = HashMap<String, Integer>();
    "The highest Job (skill) levels in the community."
    shared Map<String, Integer> highestSkillLevels => map { *skillLevels };
    "Set the highest level in the community for the given Job"
    shared void setSkillLevel(String skill, Integer level) {
        "Skill level cannot be negative; zero removes the skill entirely"
        assert (level >= 0);
        if (level == 0) {
            skillLevels.remove(skill);
        } else {
            skillLevels[skill] = level;
        }
    }
    "ID numbers of fields, orchards, and the like that this community cultivates. We don't
     have references to the
     [[strategicprimer.model.map.fixtures.resources::HarvestableFixture]] objects
     themselves, because that would require XML parsing to grow an additional pass, but
     every number here should be the ID number of a
     [[strategicprimer.model.map.fixtures.resources::HarvestableFixture]] that is not
     claimed by any other community."
    MutableSet<Integer> workedFieldIDs = HashSet<Integer>();
    "ID numbers of fields, orchards, and the like that this community cultivates. We don't
     have references to the
     [[strategicprimer.model.map.fixtures.resources::HarvestableFixture]] objects
     themselves, because that would require XML parsing to grow an additional pass, but
     every number here should be the ID number of a
      [[strategicprimer.model.map.fixtures.resources::HarvestableFixture]] that is not
      claimed by any other community."
    shared {Integer*} workedFields => {*workedFieldIDs};
    "Add a field (or orchard, or other harvestable resource source) (ID number) to the
     collection of worked fields."
    shared void addWorkedField(Integer fieldID) => workedFieldIDs.add(fieldID);
    "Remove a harvestable resource source (ID number) from the collection of such sources
      worked by this community"
    shared void removeWorkedField(Integer fieldID) => workedFieldIDs.remove(fieldID);
    "The set of resources produced each year."
    todo("Should we really expose this as a [[MutableSet]], instead of merely a [[Set]]
          modified by mutators on this class?")
    shared MutableSet<ResourcePile> yearlyProduction = HashSet<ResourcePile>();
    "The set of resources consumed each year. (Though substitutions of like resources are
     to be expected.)"
    todo("Should we really expose this as a [[MutableSet]], instead of merely a [[Set]]
          modified by mutators on this class?")
    shared MutableSet<ResourcePile> yearlyConsumption = HashSet<ResourcePile>();
    shared actual String string {
        StringBuilder builder = StringBuilder();
        builder.append("Community stats:");
        builder.appendNewline();
        builder.append("Population: ``population``");
        builder.appendNewline();
        builder.append("Skills: ");
        for (skill->level in skillLevels) {
            builder.append("- ``skill``: Level ``level``");
            builder.appendNewline();
        }
        builder.append("ID #s of worked fields: ");
        builder.append(", ".join(workedFieldIDs));
        builder.appendNewline();
        builder.append("Yearly Resource Production:");
        builder.appendNewline();
        for (resource in yearlyProduction) {
            builder.append("- ``resource``");
            builder.appendNewline();
        }
        builder.append("Yearly Resource Consumption:");
        builder.appendNewline();
        for (resource in yearlyConsumption) {
            builder.append("- ``resource``");
            builder.appendNewline();
        }
        return builder.string;
    }
    shared actual Boolean equals(Object that) {
        if (is CommunityStats that) {
            return population == that.population && skillLevels == that.skillLevels &&
                workedFieldIDs == that.workedFieldIDs &&
                yearlyProduction == that.yearlyProduction &&
                yearlyConsumption == that.yearlyConsumption;
        } else {
            return false;
        }
    }
    shared actual Boolean isSubset(CommunityStats? other, Anything(String) report) {
        if (exists other) {
            if (population < other.population) {
                report("Population is larger");
                return false;
            } else if (!workedFields.containsEvery(other.workedFields)) {
                report("Has worked fields we don't");
                return false;
            } else {
                for (resource in other.yearlyProduction) {
                    if (yearlyProduction.contains(resource) ||
                            yearlyProduction.any((ours) =>
                                ours.isSubset(resource, noop))) {
                        continue;
                    } else {
                        report("Produces a resource we don't, or more than we do: ``
                                resource``");
                        return false;
                    }
                }
                for (resource in other.yearlyConsumption) {
                    if (yearlyConsumption.contains(resource) ||
                            yearlyConsumption.any(
                                        (ours) => ours.isSubset(resource, noop))) {
                        continue;
                    } else {
                        report("Consumes a resource we don't, or more than we do: ``
                                resource``");
                        return false;
                    }
                }
                return true;
            }
        } else {
            return true;
        }
    }
}
