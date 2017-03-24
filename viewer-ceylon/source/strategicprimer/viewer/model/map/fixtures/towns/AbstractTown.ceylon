import lovelace.util.common {
    todo
}

import model.map {
    IEvent,
    HasMutableImage,
    Player,
    IFixture
}
"An abstract superclass for towns etc."
shared abstract class AbstractTown(status, size, name, townOwner, dc) satisfies IEvent&HasMutableImage&ITownFixture {
    "The status of the town, fortification, or city"
    shared actual TownStatus status;
    "The size of the town, fortification, or city"
    shared actual TownSize size;
    "The name of the town, fortification, or city"
    todo("Should this really be variable?")
    shared actual variable String name;
    "The player that owns the town, fortification, or city"
    variable Player townOwner;
    "The DC to discover the town, fortification, or city"
    shared actual Integer dc;
    variable String imageFilename = "";
    "The filename of an image to use for this instance."
    shared actual String image => imageFilename;
    shared actual void setImage(String image) => imageFilename = image;
    "The filename of an image to use as a portrait."
    shared actual variable String portrait = "";
    shared actual Player owner => townOwner;
    shared actual void setOwner(Player owner) => townOwner = owner;
    "Exploration-result text for the town."
    shared actual String text => "There is a ``(size == TownSize.medium) then
            "medium-size" else size.string`` ``(status == TownStatus.burned) then
            "burned-out" else status.string`` ``kind````name.empty then "" else
            ", ``name``,"`` here.";
    "A helper method for equals() that checks everything except the type of the object."
    shared Boolean equalsContents(AbstractTown fixture) => fixture.size == size &&
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
            return "An independent ``size`` ``status`` ``kind`` of DC ``dc`` ``
                (name.empty) then "with no name" else "named ``name``"``";
        } else {
            return "A ``size`` ``status`` ``kind`` of DC ``dc`` ``(name.empty) then
                "with no name" else "named ``name``"``, owned by ``(owner.current) then
                "you" else owner.name``";
        }
    }
    shared actual formal String defaultImage;
    shared actual String shortDesc() {
        if (owner.independent) {
            return "An independent ``size`` ``status`` ``kind`` ``(name.empty) then
                "with no name" else "named ``name``"``";
        } else {
            return "A ``size`` ``status`` ``kind`` ``(name.empty) then "with no name" else
                "named ``name``"``, owned by ``(owner.current) then "you" else
                owner.name``";
        }
    }
}