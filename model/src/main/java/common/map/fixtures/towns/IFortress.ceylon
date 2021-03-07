import lovelace.util.common {
    todo
}

import strategicprimer.model.common.map {
    IFixture,
    Subsettable,
    HasName,
    HasImage
}
import strategicprimer.model.common.map.fixtures {
    FortressMember
}

"A fortress on the map. A player can only have one fortress per tile, but multiple players
  may have fortresses on the same tile."
todo("FIXME: We need something about buildings yet")
shared interface IFortress satisfies HasImage&ITownFixture&HasName&
        {FortressMember*}&IFixture&Subsettable<IFixture> {
    "Clone the fortress."
    shared actual formal IFortress copy(Boolean zero);

    shared actual Boolean equals(Object obj) {
        if (is IFortress obj) {
            return equalsIgnoringID(obj) && id == obj.id;
        } else {
            return false;
        }
    }

    shared actual Integer hash => id;

    "The filename of the image to use as an icon when no per-instance icon has been
     specified."
    todo("Should perhaps be more granular")
    shared actual String defaultImage => "fortress.png";

    "The status of the fortress."
    todo("Add support for having a different status? (but leave 'active' the default) Or
          maybe a non-'active' fortress is a Fortification, and an active fortification is
          a Fortress.")
    shared actual TownStatus status => TownStatus.active;

    shared actual String plural => "Fortresses";

    shared actual String shortDescription {
        if (owner.current) {
            return "a fortress, ``name``, owned by you";
        } else if (owner.independent) {
            return "an independent fortress, ``name``";
        } else {
            return "a fortress, ``name``, owned by ``owner.name``";
        }
    }

    shared actual String kind => "fortress";
}
