import strategicprimer.drivers.common {
    SelectionChangeSource,
    IMultiMapModel
}
import strategicprimer.model.common.map {
    Point,
    Player,
    Direction,
    TileFixture
}
import strategicprimer.model.common.map.fixtures.mobile {
    IUnit
}

"A model for exploration apps."
shared interface IExplorationModel
        satisfies IMultiMapModel&SelectionChangeSource&MovementCostSource {
    "Players that are shared by all the maps."
    shared formal {Player*} playerChoices;

    "The given player's units in the main (master) map."
    shared formal {IUnit*} getUnits(Player player);

    "Move the currently selected unit from its current location one tile in the specified
     direction. Moves the unit in all maps where the unit *was* in that tile, copying
     terrain information if the tile didn't exist in a subordinate map. If movement in the
     specified direction is impossible, we update all subordinate maps with the terrain
     information showing that, then re-throw the exception; callers should deduct a
     minimal MP cost (though we notify listeners of that cost). We return the cost of the
     move in MP, which we also tell listeners about."
    throws(`class TraversalImpossibleException`,
        "if movement in the specified direction is impossible")
    shared formal Integer move(Direction direction, Speed speed);

    "Given a starting point and a direction, get the next point in that direction."
    shared formal Point getDestination(Point point, Direction direction);

    """Get the location of the first fixture that can be found that is "equal to" the
       given fixture, or "the invalid point" if not found."""
    shared formal Point find(TileFixture fixture);

    "The currently selected unit, if any."
    shared formal variable IUnit? selectedUnit;

    """The location of the currently selected unit, or "the invalid point" if none."""
    shared formal Point selectedUnitLocation;

    "If there is a currently selected unit, make any independent villages at its location
     change to be owned by the owner of the currently selected unit. This costs MP."
    shared formal void swearVillages();

    "If there is a currently selected unit, change one
     [[strategicprimer.model.common.map.fixtures::Ground]],
     [[strategicprimer.model.common.map.fixtures.resources::StoneDeposit]], or
     [[strategicprimer.model.common.map.fixtures.resources::MineralVein]] at the location
     of that unit from unexposed to exposed (and discover it). This costs MP."
    shared formal void dig();
}
