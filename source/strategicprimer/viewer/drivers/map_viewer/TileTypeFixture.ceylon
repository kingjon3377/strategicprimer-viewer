import strategicprimer.model.common.map {
    TileType,
    FakeFixture
}

"""A fake "TileFixture" to represent the tile's terrain type, so it can be copied via
   drag-and-drop like a fixture."""
class TileTypeFixture(shared TileType tileType) satisfies FakeFixture {
    "Clone the object."
    deprecated("This class should only ever be used in a FixtureListModel, and copying
                a tile's terrain type should be handled specially anyway, so this method
                should never be called.")
    shared actual TileTypeFixture copy(Boolean zero) {
        log.warn("TileTypeFixture.copy called", Exception("dummy"));
        return TileTypeFixture(tileType);
    }

    shared actual Boolean equals(Object obj) {
        if (is TileTypeFixture obj) {
            return tileType == obj.tileType;
        } else {
            return false;
        }
    }

    shared actual Integer hash => tileType.hash;
    shared actual String string => "Terrain: " + tileType.string;

    "The filename of an image to represent the object.

     There are now actually images in the repository for each tile type; they are not
     suitable for using as tile images, but are suitable for use in fixture lists. They
     are all public domain, found on either OpenClipArt or Pixabay and then adjusted to a
     square aspect ratio. (Except for 'mountain', which has been in the repository for a
     long time because it was used by the Mountain tile fixture."
    shared actual String defaultImage => "``tileType.xml``.png";

    shared actual String shortDescription => string;

    "The required Perception check for an explorer to find the fixture."
    shared actual Integer dc = 0;
}
