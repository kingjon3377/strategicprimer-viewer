import strategicprimer.model.impl.map {
    Player
}
import strategicprimer.model.impl.map.fixtures.mobile {
    IUnit
}
import strategicprimer.drivers.common {
    IMultiMapModel
}
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
}
