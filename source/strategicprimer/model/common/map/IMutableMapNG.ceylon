import lovelace.util.common {
    NonNullCorrespondence,
    PathWrapper
}
import strategicprimer.model.common.map {
    TileFixture,
    Player,
    Point,
    River,
    TileType
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

    "Set the road level at a location for a direction."
    shared formal void setRoadLevel(Point location, Direction direction, Integer quality);

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

    "The file from which the map was loaded, or to which it should be saved, if known"
    shared actual formal variable PathWrapper? filename; // FIXME: Notify map metadata listeners when changed

    "Whether the map has been modified since it was last saved."
    shared actual formal variable Boolean modified; // FIXME: Notify map metadata listeners when changed

    "Add a bookmark."
    shared formal void addBookmark(
        "Where to place the bookmark" Point point,
        "The player to place the bookmark for" Player player = currentPlayer);

    "Remove a bookmark."
    shared formal void removeBookmark(
        "Where to remove the bookmark" Point point,
        "The player to remove the bookmark for" Player player = currentPlayer);

    "Replace [[an existing fixture|original]], if present, with [[a new
     one|replacement]]. If [[original]] was not present, add [[replacement]]
     anyway.  Order within the list of fixtures is of course not guaranteed,
     but subclass implementations are encouraged to use a replace-in-place
     operation to minimize churn in the XML serialized form."
     // TODO: return Boolean if the map was changed?
    shared default void replace(Point location, TileFixture original, TileFixture replacement) {
        removeFixture(location, original);
        addFixture(location, replacement);
    }
}
