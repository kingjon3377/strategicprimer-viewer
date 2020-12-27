import strategicprimer.model.common.map {
    Player
}

import strategicprimer.model.common.map.fixtures {
    UnitMember
}

import strategicprimer.model.common.map.fixtures.mobile {
    IUnit
}
import strategicprimer.drivers.common {
    IMultiMapModel
}
import strategicprimer.model.common.map.fixtures.towns {
    Fortress
}

"A driver model for the worker management app and the advancement app, aiming
 to provide a hierarchical view of the units in the map and their members,
 regardless of their location."
shared interface IWorkerModel satisfies IMultiMapModel {
    "All the players in all the maps."
    shared formal {Player*} players;

    "The units in the map belonging to the given player."
    shared formal {IUnit*} getUnits(
            "The player whose units we want"
            Player player,
            """Which "kind" to restrict to, if any"""
            String? kind = null);

    """The "kinds" of units that the given player has."""
    shared formal {String*} getUnitKinds(
            "The player whose unit kinds we want"
            Player player);

    "Add a unit in its owner's HQ."
    shared formal void addUnit("The unit to add" IUnit unit);

    "Get a unit by ID number."
    shared formal IUnit? getUnitByID(Player owner, Integer id);

    "The player that the UI seems to be concerned with."
    shared formal variable Player currentPlayer;

    "The fortresses belonging to the current player."
    // TODO: Return their positions with them?
    shared formal {Fortress*} getFortresses(
            "The player whose fortresses we want" Player player);

    """Remove the given unit from the map. It must be empty, and may be
       required to be owned by the current player. The operation will also fail
       if "matching" units differ in name or kind from the provided unit.
       Returns [[true]] if the preconditions were met and the unit was removed,
       and [[false]] otherwise."""
    shared formal Boolean removeUnit(IUnit unit);

    "Move a unit-member from one unit to another."
    shared formal void moveMember(UnitMember member, IUnit old, IUnit newOwner);
}
