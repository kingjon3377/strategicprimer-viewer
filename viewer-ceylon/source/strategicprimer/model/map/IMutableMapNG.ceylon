import strategicprimer.model.map {
    TileFixture,
    River
}
import strategicprimer.model.map.fixtures {
    Ground
}
import strategicprimer.model.map.fixtures.terrain {
    Forest
}
"A mutable map."
shared interface IMutableMapNG satisfies IMapNG {
    "Add a player to the map."
    shared formal void addPlayer(Player player);
    "Set the base terrain at a location."
    shared formal void setBaseTerrain(Point location, TileType terrainType);
    "Set whether a location is mountainous."
    shared formal void setMountainous(Point location, Boolean mountainous);
    "Add rivers at a location."
    shared formal void addRivers(Point location, River* addedRivers);
    "Remove rivers at a location."
    shared formal void removeRivers(Point location, River* removedRivers);
    "Set the primary forest, if any, at a location."
    shared formal void setForest(Point location, Forest? forest);
    "Set the primary ground, if any, at a location."
    shared formal void setGround(Point location, Ground? newGround);
    """Add a fixture to the "other fixtures" collection at a location. (Note that this may
        or may not be related to the other querying methods; it is possible for the
        "primary Ground" to be null but for there to be a Ground here.) Return whether the
         "all fixtures at this point" has an additional member as a result of this
         operation."""
    shared formal Boolean addFixture(Point location, TileFixture fixture);
    """Remove a fixture from the "other fixtures" collection at a location. This may or may
       not remove it from the other "buckets"."""
    shared formal void removeFixture(Point location, TileFixture fixture);
    "The current player."
    shared actual formal variable Player currentPlayer;
    "The current turn."
    shared actual formal variable Integer currentTurn;
}