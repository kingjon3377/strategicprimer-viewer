import strategicprimer.drivers.common {
    IFixtureEditingModel,
    SelectionChangeSource,
    IMultiMapModel
}
import strategicprimer.model.common.map {
    Point,
    Player,
    Direction,
    River,
    TileFixture,
    TileType
}
import strategicprimer.model.common.map.fixtures.mobile {
    IUnit
}

"A model for exploration apps."
shared interface IExplorationModel
        satisfies IMultiMapModel&SelectionChangeSource&MovementCostSource&IFixtureEditingModel {
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

    "Add the given [[unit]] at the given [[location]]."
    shared formal void addUnitAtLocation(IUnit unit, Point location);

    "Copy the given fixture from the main map to subordinate maps. (It is found
     in the main map by ID, rather than trusting the input, unless it is animal
     tracks.) If it is a cache, remove it from the main map. If [[zero]],
     remove sensitive information from the copies. Returns true if we think
     this changed anything in any of the sub-maps."
    shared formal Boolean copyToSubMaps(Point location, TileFixture fixture, Boolean zero = true);

    "Copy terrain, including any mountain, rivers, and roads, from the main map
     to subordinate maps." // FIXME: Is this really necessary?
    shared formal void copyTerrainToSubMaps(Point location);

    "Set sub-map terrain at the given location to the given type."
    deprecated("Can we redesign the fixture list to not need this for the exploration GUI?")
    shared formal void setSubMapTerrain(Point location, TileType? terrain);

    "Copy the given rivers to sub-maps, if they are present in the main map."
    shared formal void copyRiversToSubMaps(Point location, River* rivers);

    "Remove the given rivers from sub-maps."
    deprecated("Can we redesign the fixture list to not need this for the exploration GUI?")
    shared formal void removeRiversFromSubMaps(Point location, River* rivers);

    "Remove the given fixture from sub-maps."
    deprecated("Can we redesign the fixture list to not need this for the exploration GUI?")
    shared formal void removeFixtureFromSubMaps(Point location, TileFixture fixture);

    "Set whether sub-maps have a mountain at the given location."
    deprecated("Can we redesign the fixture list to not need this for the exploration GUI?")
    shared formal void setMountainousInSubMap(Point location, Boolean mountainous);
}
