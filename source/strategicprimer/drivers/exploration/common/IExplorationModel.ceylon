import strategicprimer.drivers.common {
    SelectionChangeSource,
    IMultiMapModel
}
import strategicprimer.model.map {
    Player,
    TileFixture,
    HasName,
    Point
}
import strategicprimer.model.map.fixtures.mobile {
    IUnit
}
"An enumeration of directions of possible travel."
shared class Direction
        of north|northeast|east|southeast|south|southwest|west|northwest|nowhere {
    "North."
    shared new north {}
    "Northeast."
    shared new northeast {}
    "East."
    shared new east {}
    "Southeast."
    shared new southeast {}
    "South."
    shared new south {}
    "Southwest."
    shared new southwest {}
    "West."
    shared new west {}
    "Northwest."
    shared new northwest {}
    "Stand still."
    shared new nowhere {}
}
"An enumeration of possible movement speeds, joining their effects on MP costs and
 Perception. Traveling to [[Direction.nowhere]] should give an additional bonus (+2?) to
 Perception."
shared class Speed of hurried|normal|observant|careful|meticulous satisfies HasName {
    "The multiplicative modifier to apply to movement costs."
    shared Float mpMultiplier;
    "The modifier to add to Perception checks."
    shared Integer perceptionModifier;
    "A description to use in menus."
    shared actual String name;
    abstract new delegate(Float multMod, Integer addMod, String desc) {
        mpMultiplier = multMod;
        perceptionModifier = addMod;
        String perceptionString = (addMod >= 0) then "+``addMod``" else addMod.string;
        name = "``desc``: x``Float.format(multMod, 0,
            1)`` MP costs, ``perceptionString`` Perception";
    }
    "Traveling as quickly as possible."
    shared new hurried extends delegate(0.66, -6, "Hurried") {}
    "Normal speed."
    shared new normal extends delegate(1.0, -4, "Normal") {}
    "Moving slowly enough to notice one's surroundings."
    shared new observant extends delegate(1.5, -2, "Observant") {}
    "Looking carefully at one's surroundings to try not to miss anything important."
    shared new careful extends delegate(2.0, 0, "Careful") {}
    "Painstaking searches."
    shared new meticulous extends delegate(2.5, 2, "Meticulous") {}
    "A description to use in GUI menus."
    shared actual String string => name;
}
"A model for exploration drivers."
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
     [[strategicprimer.model.map.fixtures::Ground]],
     [[strategicprimer.model.map.fixtures.resources::StoneDeposit]], or
     [[strategicprimer.model.map.fixtures.resources::MineralVein]] at the location of that
     unit from unexposed to exposed (and discover it). This costs MP."
    shared formal void dig();
}