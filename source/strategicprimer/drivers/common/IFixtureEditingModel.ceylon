import strategicprimer.model.common.map {
    HasKind,
    HasMutableName,
    HasMutableOwner,
    Player
}

import strategicprimer.model.common.map.fixtures {
    UnitMember
}

import strategicprimer.model.common.map.fixtures.mobile {
    IUnit
}

shared interface IFixtureEditingModel satisfies IDriverModel {
    "Move a unit-member from one unit to another."
    shared formal void moveMember(UnitMember member, IUnit old, IUnit newOwner);

    """Remove the given unit from the map. It must be empty, and may be
       required to be owned by the current player. The operation will also fail
       if "matching" units differ in name or kind from the provided unit.
       Returns [[true]] if the preconditions were met and the unit was removed,
       and [[false]] otherwise."""
    shared formal Boolean removeUnit(IUnit unit);

    "Add a new member to a unit."
    shared formal void addUnitMember(
            "The unit that should own the member"
            IUnit unit,
            "The member to add to the unit"
            UnitMember member);

    "Change something's name. Returns [[true]] if we were able to find it and
     changed its name, [[false]] on failure."
    shared formal Boolean renameItem(HasMutableName item, String newName);

    "Change something's kind. Returns [[true]] if we were able to find it and
     changed its kind, [[false]] on failure."
    shared formal Boolean changeKind(HasKind item, String newKind);

    "Dismiss a unit member from a unit and from the player's service."
    shared formal void dismissUnitMember(UnitMember member);

    "Add a unit member to the unit that contains the given member in each map.
     Returns [[true]] if any of the maps had a unit containing the existing
     sibling, to which the new member was added, [[false]] otherwise."
    shared formal Boolean addSibling(
        "The member that is already in the tree."
        UnitMember base,
        "The member to add as its sibling."
        UnitMember sibling);

    "Change the owner of the given item in all maps. Returns [[true]] if this
     succeeded in any map, [[false]] otherwise."
    shared formal Boolean changeOwner(HasMutableOwner item, Player newOwner);

    "Add a unit in its owner's HQ."
    shared formal void addUnit("The unit to add" IUnit unit);

    "Sort the members of the given unit in all maps. Returns [[true]] if any
     map contained a matching unit, [[false]] otherwise."
    // TODO: Also support fortresses, and any other fixtures-containing-fixtures as they come to exist
    shared formal Boolean sortFixtureContents(IUnit fixture);

    "The unit members that have been dismissed during this session."
    shared formal {UnitMember*} dismissed;
}
