import lovelace.util.common {
    todo
}
import strategicprimer.model.common.map {
    HasPopulation,
    IFixture,
    HasKind,
    HasImage
}
import strategicprimer.model.common.map.fixtures {
    UnitMember
}
import strategicprimer.model.common.map.fixtures.mobile {
    MobileFixture
}
"An interface to cover animals and animal tracks, to try to work around
 eclipse/ceylon#7372."
todo("Remove once that fixed and the fix released")
shared interface AnimalOrTracks of Animal|AnimalTracks satisfies IFixture&UnitMember {}
"An animal or group of animals."
shared interface Animal
        satisfies AnimalOrTracks&Identifiable&MobileFixture&HasImage&HasKind&UnitMember
            &HasPopulation<Animal> {
    "Whether this is a talking animal."
    shared formal Boolean talking;
    "The domestication status of the animal."
    todo("Should this be an enumerated type?")
    shared formal String status;
    "The turn the animal was born, or -1 if it is an adult (or if this is traces ...)"
    shared formal Integer born;
    shared actual default String shortDescription {
        if (talking) {
            return "talking ``kind``";
        } else {
            String popString;
            if (population == 1) {
                popString = "";
            } else {
                popString = "``population`` ";
            }
            if (born >= 0) {
                return "``popString````kind`` (born ``born``)";
            } else {
                return "``popString````status`` ``kind``";
            }
        }
    }
    "Default image filename"
    todo("Should depend on the kind of animal")
    shared actual default String defaultImage => "animal.png";
    shared actual Integer hash => id;
    shared actual String string => shortDescription;
    shared actual String plural => "Animals";
    "Whether another animal is equal except its ID and population count."
    shared default Boolean equalExceptPopulation(Animal other) {
        return kind == other.kind && talking == other.talking &&
                status == other.status && born == other.born;
    }
    shared actual default Boolean equalsIgnoringID(IFixture fixture) {
        if (is Animal fixture) {
            return equalExceptPopulation(fixture) && population == fixture.population;
        } else {
            return false;
        }
    }
    "An object is equal if it is an animal with equal kind, either both or neither are
     talking, either both or neither are only traces, and if not traces if the IDs are
     equal."
    shared actual default Boolean equals(Object obj) {
        if (is Animal obj) {
            return obj.id == id && equalsIgnoringID(obj);
        } else {
            return false;
        }
    }
    shared actual formal Animal reduced(Integer newPopulation, Integer newId);
    shared actual formal Animal copy(Boolean zero);
    shared actual formal Animal combined(Animal addend);
    shared actual default Boolean isSubset(IFixture obj, Anything(String) report) {
        if (obj.id == id) {
            if (is Animal obj) {
                if (kind != obj.kind) {
                    report("Different kinds of animal for ID #``id``");
                    return false;
                } else if (!talking, obj.talking) {
                    report(
                        "In animal ID #``id``:\tSubmap's is talking and master's isn't");
                    return false;
                } else if (status != obj.status) {
                    report("Animal domestication status differs at ID #``id``");
                    return false;
                } else if (obj.population > population) {
                    report(
                        "In animal #``id``: Submap has greater population than master");
                    return false;
                } else if (obj.born < born) {
                    report("In animal #``id``: Submap has greater age than master");
                    return false;
                } else {
                    return true;
                }
            } else if (is AnimalTracks obj, kind == obj.kind) {
                return true;
            } else {
                report("For ID #``id``, different kinds of members");
                return false;
            }
        } else {
            report("Called with different IDs, #``id`` and #``obj.id``");
            return false;
        }
    }
}
