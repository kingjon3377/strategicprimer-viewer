import java.util {
    Formatter
}

import lovelace.util.common {
    todo
}

import model.map {
    HasMutableImage,
    SubsettableFixture,
    Player,
    IFixture
}
import model.map.fixtures.towns {
    TownStatus,
    TownSize
}
"A village in the map."
shared class Village(status, name, id, tempOwner, race)
        satisfies ITownFixture&HasMutableImage&SubsettableFixture {
    "The status of the village."
    shared actual TownStatus status;
    "The name of the village."
    shared actual String name;
    "The ID number."
    shared actual Integer id;
    variable Player tempOwner;
    "The dominant race of the village."
    shared variable String race;
    "The player the village has pledged to serve and support, if any."
    shared actual Player owner => tempOwner;
    variable String tempImage = "";
    "The per-instance icon filename."
    shared actual String image => tempImage;
    "The default-icon filename."
    shared actual String defaultImage = "village.png";
    "A filename of an image to use as a portrait of the village."
    shared actual variable String portrait = "";
    "A short description of the village."
    shared actual String shortDesc() {
        StringBuilder builder = StringBuilder();
        if (owner.independent) {
            builder.append("Independent ");
        }
        builder.append("``status.string`` village");
        if (!name.empty) {
            builder.append(" named ``name``");
        }
        if (owner.current) {
            builder.append(", owned by you");
        } else if (!owner.independent) {
            builder.append(", owned by ``owner.name``");
        }
        return builder.string;
    }
    shared actual String string => shortDesc();
    "An object is equal if it is a Village with the same status, ID, name, race, and
     owner."
    shared actual Boolean equals(Object obj) {
        if (is Village obj) {
            return status == obj.status && name == obj.name && id == obj.id &&
                owner == obj.owner && race == obj.race;
        } else {
            return false;
        }
    }
    "Use the ID number for hashing."
    shared actual Integer hash => id;
    "If we ignore ID, a fixture is equal iff it is a Village with the same status, owner,
     and race."
    shared actual Boolean equalsIgnoringID(IFixture fixture) {
        if (is Village fixture) {
            return status == fixture.status && name == fixture.name &&
                owner == fixture.owner;
        } else {
            return false;
        }
    }
    "All villages are small."
    shared actual TownSize size => TownSize.small;
    shared actual String plural() => "Villages";
    """A village is a "subset" of another if they are identical, or if the only difference
        is that the "subset" is independent and the "superset" owes allegiance to some
        player."""
    shared actual Boolean isSubset(IFixture obj, Formatter ostream, String context) {
        if (is Village obj) {
            if (id != obj.id) {
                ostream.format("%s\tIds differ%n", context);
            } else if (status != obj.status) {
                ostream.format("%s In village (ID #``id``):\tVillage status differs%n",
                    context);
            } else if (name != obj.name) {
                ostream.format("%s In village (ID #``id``):\tVillage name differs%n",
                    context);
            } else if (race != obj.race) {
                ostream.format("%s In village %s (ID #``id``):\tDominant race differs%n",
                    context, name);
            } else if (owner.playerId != obj.owner.playerId, !obj.owner.independent) {
                ostream.format("%s In village %s (ID #``id``):\tOwners differ%n", context,
                    name);
            } else {
                return true;
            }
            return false;
        } else {
            ostream.format("%sIncompatible type to Village%n", context);
            return false;
        }
    }
    shared actual String kind => "village";
    "The required Perception check to find the village."
    shared actual Integer dc => (TownStatus.active == status) then 15 else 30;
    "Clone the object."
    todo("Omit portrait if `zero`?")
    shared actual Village copy(Boolean zero) {
        Village retval = Village(status, name, id, owner, race);
        retval.setImage(image);
        retval.portrait = portrait;
        return retval;
    }
    // Until the 'mutable' interfaces are ported ...
    shared actual void setImage(String img) => tempImage = img;
    shared actual void setOwner(Player player) => tempOwner = player;
}