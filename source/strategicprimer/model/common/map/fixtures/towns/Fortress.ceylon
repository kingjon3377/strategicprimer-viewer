import ceylon.collection {
    MutableList,
    ArrayList
}


import lovelace.util.common {
    todo,
    entryBy
}
import ceylon.language {
    createMap=map
}
import strategicprimer.model.common.map {
    IFixture,
    Subsettable,
    TileFixture,
    HasMutableName,
    HasMutableImage,
    Player
}
import strategicprimer.model.common.map.fixtures {
    FortressMember
}
import strategicprimer.model.common.map.fixtures.mobile {
    IUnit
}

"A fortress on the map. A player can only have one fortress per tile, but multiple players
  may have fortresses on the same tile."
todo("FIXME: We need something about buildings yet")
shared class Fortress(owner, name, id, townSize = TownSize.small)
        satisfies HasMutableImage&ITownFixture&HasMutableName&
        {FortressMember*}&IFixture&Subsettable<IFixture> {
    "The player who owns the fortress."
    shared actual variable Player owner;

    "The name of the fortress."
    shared actual variable String name;

    "The ID number."
    shared actual Integer id;

    "The size of the fortress."
    shared actual TownSize townSize;

    "The members of the fortress."
    todo("Should this perhaps be a Set?")
    MutableList<FortressMember> members = ArrayList<FortressMember>();

    "A Fortress's contents aren't handled like those of other towns."
    todo("OTOH, should they be?")
    shared actual CommunityStats? population = null;

    "The filename of an image to use as an icon for this instance."
    shared actual variable String image = "";

    "The name of an image to use as a portrait."
    shared actual variable String portrait = "";

    "Add a member to the fortress."
    shared void addMember(FortressMember member) => members.add(member);

    "Remove a member from the fortress."
    shared void removeMember(FortressMember member) => members.remove(member);

    "Clone the fortress."
    shared actual Fortress copy(Boolean zero) {
        Fortress retval;
        if (zero) {
            retval = Fortress(owner, "unknown", id, townSize);
        } else {
            retval = Fortress(owner, name, id, townSize);
            for (member in members) {
                retval.addMember(member.copy(false));
            }
        }
        retval.image = image;
        return retval;
    }

    "An iterator over the members of the fortress."
    shared actual Iterator<FortressMember> iterator() => members.iterator();

    shared actual Boolean equalsIgnoringID(IFixture fixture) {
        if (is Fortress fixture) {
            return name == fixture.name && owner.playerId == fixture.owner.playerId &&
                    set(members) == set(fixture.members);
        } else {
            return false;
        }
    }

    shared actual Boolean equals(Object obj) {
        if (is Fortress obj) {
            return equalsIgnoringID(obj) && id == obj.id;
        } else {
            return false;
        }
    }

    shared actual Integer hash => id;

    shared actual String string {
        StringBuilder builder = StringBuilder();
        builder.append("Fortress ``name``, owned by player ``owner``. Members:");
        variable Integer count = 0;
        for (member in members) {
            builder.appendNewline();
            builder.append("\t\t\t");
            if (is IUnit member) {
                builder.append(member.name);
                if (member.owner == owner) {
                    builder.append(" (``member.kind``)");
                } else if (member.owner.independent) {
                    builder.append(", an independent ``member.kind``");
                } else {
                    builder.append(" (``member.kind``), belonging to ``member.owner``");
                }
            } else {
                builder.append(member.string);
            }
            count++;
            if (count < members.size - 1) {
                builder.append(";");
            }
        }
        return builder.string;
    }

    "The filename of the image to use as an icon when no per-instance icon has been
     specified."
    todo("Should perhaps be more granular")
    shared actual String defaultImage => "fortress.png";

    """A fixture is a subset if it is a Fortress with the same ID, owner, and name (or it
       has the name "unknown") and every member it has is equal to, or a subset of, one of
       our members."""
    shared actual Boolean isSubset(IFixture obj, Anything(String) report) {
        if (!obj is Fortress) {
            report("Incompatible type to Fortress");
            return false;
        }
        assert (is Fortress obj);
        if (obj.id != id) {
            report("ID mismatch between Fortresses");
            return false;
        }
        if (townSize != obj.townSize) {
            report("Size mismatch between Fortresses");
            return false;
        }
        if ([name, "unknown"].contains(obj.name), obj.owner.playerId == owner.playerId) {
            Map<Integer, FortressMember> ours =
                    createMap(members.map(entryBy(FortressMember.id,
                        identity<FortressMember>)));
            variable Boolean retval = true;
            Anything(String) localFormat =
                    compose(report, "In fortress ``name`` (ID #``id``):\t".plus);
            for (member in obj) {
                if (exists corresponding = ours[member.id]) {
                    if (!corresponding.isSubset(member, localFormat)) {
                        retval = false;
                    }
                } else {
                    localFormat("Extra member:\t``member``, ID #``member.id``");
                    retval = false;
                }
            }
            return retval;
        } else {
            report("In fortress (ID #``id``): Names don't match");
            return false;
        }
    }

    "The status of the fortress."
    todo("Add support for having a different status? (but leave 'active' the default) Or
          maybe a non-'active' fortress is a Fortification, and an active fortification is
          a Fortress.")
    shared actual TownStatus status = TownStatus.active;

    shared actual String plural = "Fortresses";

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

    "The required Perception check for an explorer to find the fortress."
    shared actual Integer dc => min(members.narrow<TileFixture>().map(TileFixture.dc))
            else 20 - members.size - townSize.ordinal * 2;
}
