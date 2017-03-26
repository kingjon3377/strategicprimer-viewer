import ceylon.collection {
    MutableMap,
    naturalOrderTreeMap,
    MutableList,
    SortedMap,
    ArrayList
}
import ceylon.interop.java {
    JavaIterable,
    CeylonIterable
}

import java.lang {
    IllegalStateException
}
import java.util {
    JIterator=Iterator
}

import lovelace.util.common {
    todo
}

import model.map {
    HasMutableKind,
    HasMutableName,
    HasMutableImage,
    HasMutableOwner,
    HasPortrait,
    Player,
    IFixture,
    TileFixture
}
import model.map.fixtures {
    UnitMember
}
import model.map.fixtures.mobile {
    ProxyFor
}
"A unit in the map."
todo("FIXME: we need more members: something about stats; what else?")
shared class Unit(unitOwner, unitKind, unitName, id) satisfies IUnit&HasMutableKind&
        HasMutableName&HasMutableImage&HasMutableOwner&HasPortrait {
    "The unit's orders. This is serialized to and from XML, but does not affect equality
     or hashing, and is not printed in [[string]]."
    SortedMap<Integer, String>&MutableMap<Integer, String> orders =
            naturalOrderTreeMap<Integer, String>({});
    "The unit's results. This is serialized to and from XML, but does not affect equality
     or hashing, and is not printed in [[string]]."
    SortedMap<Integer, String>&MutableMap<Integer, String> results =
            naturalOrderTreeMap<Integer, String>({});
    "The members of the unit."
    todo("Use [[MutableSet]]/[[ArraySet]] once the latter is ported: we want the
          uniqueness guarantee")
    MutableList<UnitMember> members = ArrayList<UnitMember>();
    "The ID number."
    shared actual Integer id;
    "The filename of an image to use as an icon for this instance."
    variable String imageFilename = "";
    shared actual String image => imageFilename;
    shared actual void setImage(String image) => imageFilename = image;
    "The player that owns the unit."
    variable Player unitOwner;
    shared actual Player owner => unitOwner;
    shared actual void setOwner(Player owner) => unitOwner = owner;
    """What kind of unit this is. For player-owned units this is usually their "category"
       (e.g. "agriculture"); for independent units it's more descriptive."""
    variable String unitKind;
    shared actual String kind => unitKind;
    shared actual void setKind(String kind) => unitKind = kind;
    """The name of this unit. For independent this is often something like "party from the
       village"."""
    variable String unitName;
    shared actual String name => unitName;
    shared actual void setName(String name) => unitName = name;
    "The filename of an image to use as a portrait for the unit."
    shared actual variable String portrait = "";
    "The unit's orders for all turns."
    todo("Wrap somehow so callers can't cast to `MutableMap`.")
    shared actual SortedMap<Integer, String> allOrders => orders;
    "The unit's results for all turns."
    todo("Wrap somehow so callers can't cast to `MutableMap`.")
    shared actual SortedMap<Integer, String> allResults => results;
    "Clone the unit."
    todo("There should be some way to convey the unit's *size* without the *details* of
          its contents. Or maybe we should give the contents but not *their* details?")
    shared actual Unit copy(Boolean zero) {
        Unit retval = Unit(owner, kind, name, id);
        if (!zero) {
            retval.orders.putAll(orders);
            retval.results.putAll(results);
            for (member in members) {
                retval.addMember(member.copy(false));
            }
        }
        retval.setImage(image);
        return retval;
    }
    "Add a member."
    shared actual void addMember(UnitMember member) {
        if (is ProxyFor<out Anything> member) {
            log.error("ProxyWorker added to Unit",
                IllegalStateException("ProxyWorker added to Unit"));
        }
        members.add(member);
    }
    "Remove a member."
    shared actual void removeMember(UnitMember member) => members.remove(member);
    "An iterator over the unit's members."
    shared actual JIterator<UnitMember> iterator() => JavaIterable(members).iterator();
    "An object is equal iff it is a IUnit owned by the same player, with the same kind,
     ID, and name, and with equal members."
    shared actual Boolean equals(Object obj) {
        if (is IUnit obj) {
            return obj.owner.playerId == owner.playerId && obj.kind == kind &&
                obj.name == name && obj.id == id &&
                members.containsEvery(CeylonIterable(obj)) &&
                CeylonIterable(obj).containsEvery(members);
        } else {
            return false;
        }
    }
    shared actual Integer hash => id;
    shared actual String string {
        if (owner.independent) {
            return "Independent unit of type ``kind``, named ``name``";
        } else {
            return "Unit of type ``kind``, belonging to ``owner``, named ``name``";
        }
    }
    shared actual String verbose =>
            "``string``, consisting of:
             ``operatingSystem.newline.join(members)``";
    "An icon to represent units by default."
    by("[jreijonen](http://opengameart.org/content/faction-symbols-allies-axis)")
    shared actual String defaultImage = "unit.png";
    "If we ignore ID (and members, which we probably shouldn't), a fixture is equal iff it
     is an IUnit owned by the same player with the same kind and name."
    todo("Should we look at unit members?") // FIXME
    shared actual Boolean equalsIgnoringID(IFixture fixture) {
        if (is IUnit fixture) {
            return fixture.owner.playerId == owner.playerId && fixture.kind == kind &&
                fixture.name == name;
        } else {
            return false;
        }
    }
    "Set orders for a turn."
    shared actual void setOrders(Integer turn, String newOrders) =>
            orders.put(turn, newOrders);
    "Get orders for a turn."
    shared actual String getOrders(Integer turn) {
        if (exists retval = orders.get(turn)) {
            return retval;
        } else if (turn < 0, exists retval = orders.get(-1)) {
            return retval;
        } else {
            return "";
        }
    }
    "Set results for a turn."
    shared actual void setResults(Integer turn, String newResults) =>
            results.put(turn, newResults);
    "Get results for a turn."
    shared actual String getResults(Integer turn) {
        if (exists retval = results.get(turn)) {
            return retval;
        } else if (turn < 0, exists retval = results.get(-1)) {
            return retval;
        } else {
            return "";
        }
    }
    "A short description of the fixture, giving its kind and owner but not its name."
    todo("Should probably give name for independent units.")
    shared actual String shortDesc() {
        if (owner.current) {
            return "a(n) ``kind`` unit belonging to you";
        } else {
            return "(a(n) ``kind`` unit belonging to ``owner.name``";
        }
    }
    "The required Perception check result for an explorer to notice the unit."
    shared actual Integer dc =>
            Integer.min({25 - members.size,
                *members.narrow<TileFixture>().map(TileFixture.dc)}) else 100;
}