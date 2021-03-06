import lovelace.util.common {
    todo,
    anythingEqual
}

import strategicprimer.model.common.map {
    IFixture,
    Subsettable,
    HasMutableImage,
    Player
}

"An abstract superclass for towns etc."
shared abstract class AbstractTown(status, townSize, name, owner, dc)
        satisfies HasMutableImage&IMutableTownFixture&Subsettable<AbstractTown> {
    "The status of the town, fortification, or city"
    shared actual TownStatus status;

    "The size of the town, fortification, or city"
    shared actual TownSize townSize;

    "The name of the town, fortification, or city"
    shared actual String name;

    "The player that owns the town, fortification, or city"
    shared actual variable Player owner;

    "The DC to discover the town, fortification, or city"
    todo("Provide reasonable default, depending on [[population]] members as well as
          [[status]] and [[townSize]]")
    shared actual Integer dc;

    "The filename of an image to use as an icon for this instance."
    shared actual variable String image = "";

    "The filename of an image to use as a portrait."
    shared actual variable String portrait = "";

    "The contents of the town."
    shared actual variable CommunityStats? population = null;

    shared actual Boolean isSubset(AbstractTown other, Anything(String) report) {
        if (id != other.id) {
            report("Fixtures' ID #s differ");
            return false;
        } else if (name != other.name, other.name != "unknown") {
            report("Town name differs");
            return false;
        } else if (kind != other.kind) {
            report("In ``name``, ID #``id``:        Town kind differs");
            return false;
        }
        Anything(String) localReport =
                compose(report, "In ``kind`` ``name``, ID #``id``:\t".plus);
        variable Boolean retval = true;
        if (status != other.status) {
            localReport("Town status differs");
            retval = false;
        }
        if (townSize != other.townSize) {
            localReport("Town size differs");
            retval = false;
        }
        if (other.population exists, !population exists) {
            localReport("Has contents details we don't");
            retval = false;
        }
        if (exists ours = population, !ours.isSubset(other.population, localReport)) {
            localReport("Has different population details");
            retval = false;
        }
        if (owner != other.owner, !other.owner.independent) {
            localReport("Has different owner");
            retval = false;
        }
        return retval;
    }

    "A helper method for equals() that checks everything except the type of the object."
    shared Boolean equalsContents(AbstractTown fixture) => fixture.townSize == townSize &&
                fixture.name == name && fixture.status == status &&
                fixture.owner == owner && anythingEqual(population, fixture.population);

    shared actual default Boolean equals(Object obj) {
        if (is AbstractTown obj) {
            return id == obj.id && equalsContents(obj);
        } else {
            return false;
        }
    }

    shared actual Boolean equalsIgnoringID(IFixture fixture) {
        if (is AbstractTown fixture) {
            return equalsContents(fixture);
        } else {
            return false;
        }
    }

    shared actual Integer hash => id;

    shared actual String string {
        if (owner.independent) {
            return "An independent ``townSize`` ``status`` ``kind`` of DC ``dc`` ``
                (name.empty) then "with no name" else "named ``name``"``";
        } else {
            return "A ``townSize`` ``status`` ``kind`` of DC ``dc`` ``(name.empty) then
                "with no name" else "named ``name``"``, owned by ``(owner.current) then
                "you" else owner.name``";
        }
    }

    shared actual formal String defaultImage;

    shared actual String shortDescription {
        if (owner.independent) {
            return "An independent ``townSize`` ``status`` ``kind`` ``(name.empty) then
                "with no name" else "named ``name``"``";
        } else {
            return "A ``townSize`` ``status`` ``kind`` ``(name.empty) then "with no name"
                else "named ``name``"``, owned by ``(owner.current) then "you" else
                owner.name``";
        }
    }
}
