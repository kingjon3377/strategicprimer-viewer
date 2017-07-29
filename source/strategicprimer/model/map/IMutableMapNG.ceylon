import lovelace.util.common {
    NonNullCorrespondence
}
"A mutable map."
shared interface IMutableMapNG satisfies IMapNG {
    "Add a player to the map."
    shared formal void addPlayer(Player player);
    "The base terrain at any given point."
    shared actual formal Correspondence<Point, TileType>&
        KeyedCorrespondenceMutator<Point, TileType?> baseTerrain;
    "Whether any given point is mountainous."
    shared actual formal NonNullCorrespondence<Point, Boolean>&
        KeyedCorrespondenceMutator<Point, Boolean> mountainous;
    "Add rivers at a location."
    shared formal void addRivers(Point location, River* addedRivers);
    "Remove rivers at a location."
    shared formal void removeRivers(Point location, River* removedRivers);
    "Add a tile fixture at the given location. Return whether the collection of fixtures
     has an additional member as a result of this operation; if the fixture was already
     present, or if it replaces one that was present, this returns false."
    shared formal Boolean addFixture(Point location, TileFixture fixture);
    "Remove a fixture from the given location."
    shared formal void removeFixture(Point location, TileFixture fixture);
    "The current player."
    shared actual formal variable Player currentPlayer;
    "The current turn."
    shared actual formal variable Integer currentTurn;
}