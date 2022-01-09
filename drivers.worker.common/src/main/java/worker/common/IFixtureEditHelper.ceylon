import strategicprimer.model.common.map.fixtures {
    UnitMember
}

import strategicprimer.drivers.common {
    PlayerChangeListener,
    MapChangeListener
}

import strategicprimer.model.common.map.fixtures.mobile {
    IUnit
}

import strategicprimer.model.common.map {
    HasMutableName,
    HasMutableOwner,
    HasKind,
    Player
}

"An interface for helpers to allow the worker tree component, the fixture list
 in the map viewer, etc., to allow the user to edit fixtures."
shared interface IFixtureEditHelper satisfies NewUnitListener&PlayerChangeListener&MapChangeListener {
    "Move a member between units."
    shared formal void moveMember(
            "The member to move."
            UnitMember member,
            "Its prior owner"
            IUnit old,
            "Its new owner"
            IUnit newOwner);

    "Add a new unit, and also handle adding it to the map (via the driver model)."
    shared formal void addUnit(
            "The unit to add"
            IUnit unit);

    "(Try to) remove a unit (from the map, via the driver model)."
    shared formal void removeUnit("The unit to remove" IUnit unit);

    "Add a new member to a unit."
    shared formal void addUnitMember(
            "The unit that should own the member"
            IUnit unit,
            "The member to add to the unit"
            UnitMember member);

    "Change something's name." // TODO: Just take HasName, since it might be a proxy for things with mutable names
    shared formal void renameItem(HasMutableName item, String newName);

    "Change something's kind; in the worker mgmt GUI, if it's a unit, this
     means moving it in the tree, since units' kinds are currently their parent
     nodes."
    shared formal void changeKind(HasKind item, String newKind);

    "Dismiss a unit member from a unit and from the player's service."
    shared formal void dismissUnitMember(UnitMember member);

    "Add a unit member to the unit that contains the given member. If the base is not in
     the tree, the model is likely to simply ignore the call, but the behavior is
     undefined."
    shared formal void addSibling(
        "The member that is already in the tree."
        UnitMember base,
        "The member to add as its sibling."
        UnitMember sibling);

    "Change the owner of the given item." // TODO: Just take HasOwner, since it might be a proxy for things with mutable owners
    shared formal void changeOwner(HasMutableOwner item, Player newOwner);

    "Sort the contents of the given unit."
    shared formal void sortMembers(IUnit fixture);
}
