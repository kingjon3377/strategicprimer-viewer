import lovelace.util.common {
    todo
}
import strategicprimer.model.common.map {
    IMutableMapNG
}

"An interface to represent a set of changes that can be made to a map (TODO: or to what?).
 It'll be used to represent the differences between an earlier and a later map."
todo("Tests", "Think of how to implement this")
shared interface Changeset {
    "The number of the turn before the changeset is applied."
    shared formal Integer from;

    "The number of the turn after the changeset is applied."
    shared formal Integer to;

    "The inverse of this set of operations."
    shared formal Changeset invert();

    "Apply the changeset to a map."
    todo("Should this possibly take different arguments?",
        "Should this possibly take [[strategicprimer.model.common.map::IMapNG]] and return the
         modified map, instead of modifying an [[IMutableMapNG]] in place?")
    shared formal void apply(IMutableMapNG map);
}
