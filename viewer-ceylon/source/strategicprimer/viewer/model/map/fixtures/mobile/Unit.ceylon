/*"A unit in the map."
todo("FIXME: we need more members: something about stats; what else?")
class Unit(unitOwner, unitKind, unitName, id) satisfies IUnit&HasMutableKind&
        HasMutableName&HasMutableImage&HasMutableOwner&HasPortrait {
    "The unit's orders. This is serialized to and from XML, but does not affect equality
     or hashing, and is not printed in [[string]]."
    MutableMap<Integer, String> orders = naturalOrderTreeMap<Integer, String>();
    "The unit's results. This is serialized to and from XML, but does not affect equality
     or hashing, and is not printed in [[string]]."
    MutableMap<Integer, String> results = naturalOrderTreeMap<Integer, String>();
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
    variable Player owningPlayer;
    shared actual Player owner => owningPlayer;
    shared actual void setOwner(Player owner) => owningPlayer = owner;
    "What kind of unit this is."
    variable String unitKind;
    shared actual String kind => unitKind;
    shared actual void setKind(String kind) => unitKind = kind;
    "The name of this unit."
    variable String unitName;
    shared actual String name => unitName;
    shared actual void setName(String name) => unitName = name;
    "The filename of an image to use as a portrait for the unit."
    shared actual String portrait = "";
}*/