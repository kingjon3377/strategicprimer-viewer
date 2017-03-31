import lovelace.util.common {
    todo
}

import strategicprimer.viewer.model.map {
    HasMutableImage,
    IFixture
}
import strategicprimer.viewer.model.map.fixtures {
    IEvent
}
import model.map {
    Player
}
"An abstract superclass for towns etc."
shared abstract class AbstractTown(status, townSize, name, owner, dc) satisfies IEvent&HasMutableImage&ITownFixture {
    "The status of the town, fortification, or city"
    shared actual TownStatus status;
    "The size of the town, fortification, or city"
    shared actual TownSize townSize;
    "The name of the town, fortification, or city"
    todo("Should this really be variable?")
    shared actual variable String name;
    "The player that owns the town, fortification, or city"
    shared actual variable Player owner;
    "The DC to discover the town, fortification, or city"
    shared actual Integer dc;
    "The filename of an image to use as an icon for this instance."
    shared actual variable String image = "";
    "The filename of an image to use as a portrait."
    shared actual variable String portrait = "";
    "Exploration-result text for the town."
    shared actual String text => "There is a ``(townSize == TownSize.medium) then
            "medium-size" else townSize.string`` ``(status == TownStatus.burned) then
            "burned-out" else status.string`` ``kind````name.empty then "" else
            ", ``name``,"`` here.";
    "A helper method for equals() that checks everything except the type of the object."
    shared Boolean equalsContents(AbstractTown fixture) => fixture.townSize == townSize &&
            fixture.name == name && fixture.status == status &&
            fixture.owner == owner;
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
            return "A ``townSize`` ``status`` ``kind`` ``(name.empty) then "with no name" else
                "named ``name``"``, owned by ``(owner.current) then "you" else
                owner.name``";
        }
    }
}