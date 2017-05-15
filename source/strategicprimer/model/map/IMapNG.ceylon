import lovelace.util.common {
    todo
}
import strategicprimer.model.map {
    River,
    Subsettable,
    TileFixture
}
import strategicprimer.model.map.fixtures {
    TextFixture,
    Ground
}
import strategicprimer.model.map.fixtures.mobile {
    Animal
}
import strategicprimer.model.map.fixtures.resources {
    CacheFixture
}
import strategicprimer.model.map.fixtures.terrain {
    Forest
}
"An interface for game-world maps.

 This is the second-generation interface. The first map implementation modeled a map as a
 collection of Tile objects, and exposed this in the interface that was extracted from it.
 That approach proved expensive from a performance perspective, but changing it required
 redesigning the interface. This interface is designed so that an implementer *could* use
 Tile objects, but callers should be oblivious to that detail. Instead, callers ask for
 the tile type, rivers, forest, mountain, fixtures, etc., mapped to a given Point. Mutator
 methods, including those used in constructing the map object, are out of the scope
 of this interface.

 We also include several of the features that, in the first implementation, were in a
 MapView class that wrapped the SPMap class."
todo("Possibly renaming this and subtypes to drop NG suffix first, redesign interface to
      take advantage of [[Correspondence]] interface's syntax sugar.")
shared interface IMapNG satisfies Subsettable<IMapNG>&Comparable<IMapNG> {
    "The dimensions (and version) of the map."
    shared formal MapDimensions dimensions;
    "(A view of) the players in the map."
    shared formal {Player*} players;
    "The locations in the map."
    todo("Define whether this should, or should not, include locations outside the
          dimensions that callers have modified anyway")
    shared formal {Point*} locations;
    "The base terrain at the given point."
    shared formal TileType baseTerrain(Point location);
    "Whether the given location is mountainous."
    shared formal Boolean mountainous(Point location);
    "The rivers, if any, at the given location."
    shared formal {River*} rivers(Point location);
    "The forest (or first forest) at the given location."
    shared formal Forest? forest(Point location);
    "The primary Ground at the given location"
    shared formal Ground? ground(Point location);
    "Any fixtures, other than the main ground and forest, at the given location."
    shared formal {TileFixture*} otherFixtures(Point location);
    "All fixtures at the given location, including primary forest and ground."
    shared default {TileFixture*} allFixtures(Point location) =>
            { ground(location), forest(location), *otherFixtures(location)}
                .coalesced;
    "The current turn."
    shared formal Integer currentTurn;
    "The current player."
    shared formal Player currentPlayer;
    "Clone the map."
    shared formal IMapNG copy(
            """Whether to "zero" sensitive data"""
            Boolean zero,
            "The player for whom the copied map is being prepared, if any."
            Player? player);
    "A location is empty if it has no terrain, no Ground, no Forest, no rivers, and no
     other fixtures"
    shared default Boolean locationEmpty(Point location) =>
            TileType.notVisible == baseTerrain(location) && !mountainous(location)
                && rivers(location).empty && allFixtures(location).empty;
    "Strict-subset calculations should skip caches, text fixtures, and animal tracks."
    shared default Boolean shouldSkip(TileFixture fixture) {
        if (is Animal fixture) {
            return fixture.traces;
        } else {
            return fixture is CacheFixture|TextFixture;
        }
    }
}