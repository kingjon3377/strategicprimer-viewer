import ceylon.collection {
    MutableList,
    ArrayList
}
import ceylon.interop.java {
    JavaIterator
}

import java.lang {
    JInteger=Integer
}
import java.util {
    JIterator=Iterator,
    Formatter
}

import lovelace.util.common {
    todo
}

import model.map {
    HasMutableImage,
    HasMutableName,
    FixtureIterable,
    SubsettableFixture,
    Player,
    IFixture,
    TileFixture
}
import model.map.fixtures {
    FortressMember
}
import model.map.fixtures.mobile {
    IUnit
}
import model.map.fixtures.towns {
    TownSize,
    ITownFixture,
    TownStatus
}
"A fortress on the map. A player can only have one fortress per tile, but multiple players
  may have fortresses on the same tile."
todo("Enforce that only-one-per-player-per-tile restriction",
    "FIXME: We need something about buildings yet")
shared class Fortress(fortOwner, fortName, id, fortSize = TownSize.small)
        satisfies HasMutableImage&ITownFixture&HasMutableName&
            FixtureIterable<FortressMember>&SubsettableFixture {
    "The player who owns the fortress."
    variable Player fortOwner;
    "The name of the fortress."
    variable String fortName;
    "The ID number."
    shared actual Integer id;
    "The size of the fortress."
    TownSize fortSize;
    "The name of the fortress."
    shared actual String name => fortName;
    "Set the name of the fortress."
    shared actual void setName(String name) => fortName = name;
    "The members of the fortress."
    todo("Should this perhaps be a Set?")
    MutableList<FortressMember> members = ArrayList<FortressMember>();
    "The name of an image to use as an icon for this particular instance."
    variable String imageFilename = "";
    "The name of an image to use as an icon for this particular instance."
    shared actual String image => imageFilename;
    "Set the per-instance icon filename."
    shared actual void setImage(String image) => imageFilename = image;
    "The name of an image to use as a portrait."
    shared actual variable String portrait = "";
    "Add a member to the fortress."
    shared void addMember(FortressMember member) => members.add(member);
    "Remove a member from the fortress."
    shared void removeMember(FortressMember member) => members.remove(member);
    "The owner of the fortress."
    shared actual Player owner => fortOwner;
    "Set the owner of the fortress."
    shared actual void setOwner(Player owner) => fortOwner = owner;
    "Clone the fortress."
    shared actual Fortress copy(Boolean zero) {
        Fortress retval;
        if (zero) {
            retval = Fortress(fortOwner, "unknown", id, fortSize);
        } else {
            retval = Fortress(fortOwner, fortName, id, fortSize);
            for (member in members) {
                retval.addMember(member.copy(false));
            }
        }
        retval.setImage(image);
        return retval;
    }
    "An iterator over the members of the fortress."
    shared actual JIterator<FortressMember> iterator() =>
            JavaIterator(members.iterator());
    shared actual Boolean equals(Object obj) {
        if (is Fortress obj) {
            return name == obj.name && owner.playerId == obj.owner.playerId &&
                set { *members } == set { *obj.members } && id == obj.id;
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
    shared actual Boolean isSubset(IFixture obj, Formatter ostream, String context) {
        if (!obj is Fortress) {
            ostream.format("%sIncompatible type to Fortress%n", context);
            return false;
        }
        assert (is Fortress obj);
        if (obj.id != id) {
            ostream.format("%sID mismatch between Fortresses%n", context);
            return false;
        }
        if ({name, "unknown"}.contains(obj.name), obj.owner.playerId == owner.playerId) {
            Map<Integer, FortressMember> ours = map {
                *members.map((member) => member.id->member)
            };
            variable Boolean retval = true;
            for (member in obj) {
                Integer memberID = member.id;
                if (exists corresponding = ours.get(memberID)) {
                    if (!corresponding.isSubset(member, ostream,
                            "``context`` In fortress ``name`` (ID #``id``")) {
                        retval = false;
                    }
                } else {
                    ostream.format(
                        "%s In fortress %s (ID #%d): Extra member:\t%s, ID #%d%n",
                        context, name, JInteger(id), member.string, JInteger(memberID));
                    retval = false;
                }
            }
            return retval;
        } else {
            ostream.format("%s In fortress (ID #%d): Names don't match%n", context,
                JInteger(id));
            return false;
        }
    }
    shared actual Boolean equalsIgnoringID(IFixture fixture) {
        if (is Fortress fixture) {
            return name == fixture.name && owner.playerId == fixture.owner.playerId &&
                set { *members } == set { *fixture.members };
        } else {
            return false;
        }
    }
    "The status of the fortress."
    todo("Add support for having a different status? (but leave 'active' the default) Or
          maybe a non-'active' fortress is a Fortification, and an active fortification is
          a Fortress.")
    shared actual TownStatus status() => TownStatus.active;
    "The size of the fortress."
    shared actual TownSize size() => fortSize;
    shared actual String plural() => "Fortresses";
    shared actual String shortDesc() {
        if (owner.current) {
            return "a fortress, ``name``, owned by you";
        } else if (owner.independent) {
            return "an independent fortress, ``name``";
        } else {
            return "a fortress, ``name``, owned by ``owner.name``";
        }
    }
    shared actual String kind() => "fortress";
    "The required Perception check for an explorer to find the fortress."
    todo("Should depend on size")
    shared actual Integer dc => min { *members.narrow<TileFixture>().map(TileFixture.dc) }
            else 20 - members.size;
}