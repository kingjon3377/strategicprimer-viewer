import strategicprimer.model.common.map.fixtures {
    UnitMember
}

import strategicprimer.model.common.map.fixtures.mobile {
    IUnit
}

import strategicprimer.model.common.map {
    Player,
    HasMutableName,
    HasMutableOwner,
    HasKind
}

import strategicprimer.drivers.worker.common {
    IFixtureEditHelper
}

import strategicprimer.drivers.common {
    IFixtureEditingModel
}

shared class FixtureEditHelper(IFixtureEditingModel model) satisfies IFixtureEditHelper {
    "Move a member between units."
    shared actual void moveMember(UnitMember member, IUnit old, IUnit newOwner) =>
            model.moveMember(member, old, newOwner);

    "Add a new unit, and also handle adding it to the map (via the driver model)."
    shared actual void addUnit(IUnit unit) => model.addUnit(unit);

    "(Try to) remove a unit (from the map, via the driver model)."
    shared actual void removeUnit(IUnit unit) => model.removeUnit(unit);

    "Add a new member to a unit."
    shared actual void addUnitMember(IUnit unit, UnitMember member) =>
            model.addUnitMember(unit, member);

    "Change something's name."
    shared actual void renameItem(HasMutableName item, String newName) =>
            model.renameItem(item, newName);

    "Change something's kind; in the worker mgmt GUI, if it's a unit, this
     means moving it in the tree, since units' kinds are currently their parent
     nodes."
    shared actual void changeKind(HasKind item, String newKind) =>
            model.changeKind(item, newKind);

    "Dismiss a unit member from a unit and from the player's service."
    shared actual void dismissUnitMember(UnitMember member) =>
            model.dismissUnitMember(member);

    "Add a unit member to the unit that contains the given member. If the base is not in
     the tree, the model is likely to simply ignore the call, but the behavior is
     undefined."
    shared actual void addSibling(UnitMember base, UnitMember sibling) =>
            model.addSibling(base, sibling);

    "Change the owner of the given item."
    shared actual void changeOwner(HasMutableOwner item, Player newOwner) =>
            model.changeOwner(item, newOwner);

    "Sort the contents of the given unit."
    shared actual void sortMembers(IUnit fixture) => model.sortFixtureContents(fixture);

    shared actual void mapChanged() {} // TODO: Do we need to implement this?

    shared actual void mapMetadataChanged() {}

    "Add a new unit."
    shared actual void addNewUnit(IUnit unit) => addUnit(unit);

    shared actual void playerChanged(Player? previousCurrent, Player newCurrent) {} // TODO: Do we need to implement this?
}
