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
    IFortress
}

"A driver model for the worker management app and the advancement app, aiming
 to provide a hierarchical view of the units in the map and their members,
 regardless of their location."
shared interface IWorkerModel satisfies IMultiMapModel&IAdvancementModel&IFixtureEditingModel {
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

    "Get a unit by ID number."
    shared formal IUnit? getUnitByID(Player owner, Integer id);

    "The player that the UI seems to be concerned with."
    shared formal variable Player currentPlayer;

    "The fortresses belonging to the current player."
    // TODO: Return their positions with them?
    shared formal {IFortress*} getFortresses(
            "The player whose fortresses we want" Player player);

    "The unit members that have been dismissed during this session."
    // TODO: Move to IFixtureEditingModel, since dismissUnitMember() got moved there?
    shared formal {UnitMember*} dismissed;
}
