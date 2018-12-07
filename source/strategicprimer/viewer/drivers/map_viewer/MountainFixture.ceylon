import strategicprimer.model.common.map {
    FakeFixture,
    TileFixture
}
"""A fake "TileFixture" to represent the mountain(s) on a mountainous tile, so it/they can
   appear in the list of the tile's contents."""
class MountainFixture() satisfies FakeFixture {
    "Clone the object."
    deprecated("This class should only ever be used in a FixtureListModel, and copying
                a tile's terrain type should be handled specially anyway, so this method
                should never be called.")
    shared actual MountainFixture copy(Boolean zero) {
        log.warn("TileTypeFixture.copy called", Exception("dummy"));
        return MountainFixture();
    }

    "The required Perception check for an explorer to find the fixture."
    shared actual Integer dc = 0;

    shared actual String defaultImage => "mountain.png";

    shared actual String shortDescription => "Mountainous terrain";
}
