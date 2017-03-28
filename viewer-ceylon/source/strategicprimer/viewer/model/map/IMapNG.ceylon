import lovelace.util.common {
    todo
}

import model.map {
    Subsettable,
    MapDimensions,
    Player,
    TileType,
    River,
    TileFixture,
    Point
}
import model.map.fixtures {
    Ground,
    TextFixture
}
import strategicprimer.viewer.model.map.fixtures.mobile {
    Animal
}
import strategicprimer.viewer.model.map.fixtures.resources {
    CacheFixture
}
import strategicprimer.viewer.model.map.fixtures.terrain {
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
    todo("Drop `get` prefix?")
    shared formal TileType getBaseTerrain(Point location);
    "Whether the given location is mountainous."
    todo("Drop `is` prefix?")
    shared formal Boolean isMountainous(Point location);
    "The rivers, if any, at the given location."
    todo("Drop `get` prefix?")
    shared formal {River*} getRivers(Point location);
    "The forest (or first forest) at the given location."
    todo("Drop `get` prefix?")
    shared formal Forest? getForest(Point location);
    "The primary Ground at the given location"
    todo("Drop `get` prefix?")
    shared formal Ground? getGround(Point location);
    "Any fixtures, other than the main ground and forest, at the given location."
    todo("Drop `get` prefix?")
    shared formal {TileFixture*} getOtherFixtures(Point location);
    "All fixtures at the given location, including primary forest and ground."
    todo("Drop `get` prefix?")
    shared default {TileFixture*} getAllFixtures(Point location) =>
            {getGround(location), getForest(location), *getOtherFixtures(location)}
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
    todo("Drop `is` prefix?")
    shared default Boolean isLocationEmpty(Point location) =>
            TileType.notVisible == getBaseTerrain(location) && !isMountainous(location)
                && getRivers(location).empty && getAllFixtures(location).empty;
    "Strict-subset calculations should skip caches, text fixtures, and animal tracks."
    shared default Boolean shouldSkip(TileFixture fixture) {
        if (is Animal fixture) {
            return fixture.traces;
        } else {
            return fixture is CacheFixture|TextFixture;
        }
    }
}