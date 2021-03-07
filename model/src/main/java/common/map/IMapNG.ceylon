import lovelace.util.common {
    NonNullCorrespondence,
    PathWrapper
}
import com.vasileff.ceylon.structures {
    Multimap
}
import strategicprimer.model.common.map {
    Subsettable,
    TileFixture,
    Player,
    MapDimensions,
    Point,
    River,
    TileType,
    IPlayerCollection
}

"""An interface for game-world maps.

   This is the third-generation interface:

   - The first map implementation modeled a map as a collection of Tile objects, and
     exposed this in the interface that was extracted from it. That approach proved
     expensive from a performance perspective, but changing it required redesigning the
     interface.
   - The second-generation interface was designed so that an implementer *could* use Tile
     objects, but callers whould be oblivious to that detail. Instead, callers askeded for
     the tile type, rivers, forest, mountain, fixtures, etc., mapped to a given Point. The
     interface also included several of the features that, in the first implementation,
     were in a MapView class that wrapped the SPMap class.
   - This third generation is built using the same principles as the second, but is
     adapted to use interfaces for which Ceylon provides "syntax sugar".

   Mutator methods, including those used in constructing the map object, are out of the
   scope of this interface."""
shared interface IMapNG satisfies Subsettable<IMapNG>&Identifiable {
    "The dimensions (and version) of the map."
    shared formal MapDimensions dimensions;

    "(A view of) the players in the map."
    shared formal IPlayerCollection players;

    """The locations in the map. This should *not* include locations outside the
       dimensions of the map even if callers have modified them, but *should* include all
       points within the dimensions of the map even if they are "empty"."""
    shared formal {Point*} locations;

    "The base terrain at any given point."
    shared formal Correspondence<Point, TileType> baseTerrain;

    "Whether any given point is mountainous."
    shared formal NonNullCorrespondence<Point, Boolean> mountainous;

    "The rivers in the map."
    shared formal Multimap<Point, River> rivers;

    "Roads in the map."
    // TODO: Should we have Road objects instead?
    // TODO: Make a RoadQuality enum
    shared formal Map<Point, Map<Direction, Integer>> roads;

    "The tile-fixtures at the various locations."
    shared formal Multimap<Point, TileFixture> fixtures;

    "The current turn."
    shared formal Integer currentTurn;

    "The current player."
    shared formal Player currentPlayer;

    "The current player's bookmarks."
    shared formal Set<Point> bookmarks;

    "Bookmarks for another player."
    shared formal Set<Point> bookmarksFor(Player player);

    "All bookmarks."
    shared formal Multimap<Point, Player> allBookmarks;

    "Clone the map." // TODO: What should this do with the filename? Take a filename parameter?
    shared formal IMapNG copy(
            """Whether to "zero" sensitive data"""
            Boolean zero,
            "The player for whom the copied map is being prepared, if any."
            Player? player);

    "The file from which the map was loaded, or to which it should be saved, if known"
    shared formal PathWrapper? filename;

    "Whether the map has been modified since it was last saved."
    shared formal Boolean modified;

    "A location is empty if it has no terrain, no Ground, no Forest, no rivers, no roads, no
     bookmarks, and no other fixtures"
    shared default Boolean locationEmpty(Point location) {
        if (exists terrain = baseTerrain[location]) {
            return false;
        } else if (exists mountain = mountainous[location], mountain) {
            return false;
        } else if (exists riverList = rivers[location], !riverList.empty) {
            return false;
        } else if (exists tileRoads = roads[location], !tileRoads.empty) {
            return false;
        } else if (exists fixtureList = fixtures[location], !fixtureList.empty) {
            return false;
        } else if (!allBookmarks.get(location).empty) {
            return false;
        } else {
            return true;
        }
    }
}
