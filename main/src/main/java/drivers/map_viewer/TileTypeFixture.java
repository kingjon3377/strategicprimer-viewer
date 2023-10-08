package drivers.map_viewer;

import common.map.TileType;
import common.map.FakeFixture;
import lovelace.util.LovelaceLogger;

/**
 * A fake "TileFixture" to represent the tile's terrain type, so it can be
 * copied via drag-and-drop like a fixture.
 */
/* package */ record TileTypeFixture(TileType tileType) implements FakeFixture {


    /**
     * Clone the object.
     *
     * @param zero
     * @deprecated This class should only ever be used in a
     * FixtureListModel, and copying a tile's terrain type should be
     * handled specially anyway, so this method should never be called.
     */
    @Deprecated
    @Override
    public TileTypeFixture copy(final CopyBehavior zero) {
        LovelaceLogger.warning(new Exception("dummy"), "TileTypeFixture.copy called");
        return new TileTypeFixture(tileType);
    }

    @Override
    public String toString() {
        return "Terrain: " + tileType;
    }

    /**
     * The filename of an image to represent the object.
     * <p>
     * There are now actually images in the repository for each tile type;
     * they are not suitable for using as tile images, but are suitable for
     * use in fixture lists. They are all public domain, found on either
     * OpenClipArt or Pixabay and then adjusted to a square aspect ratio.
     * (Except for 'mountain', which has been in the repository for a long
     * time because it was used by the Mountain tile fixture.
     */
    @Override
    public String getDefaultImage() {
        return tileType.getXml() + ".png";
    }

    @Override
    public String getShortDescription() {
        return toString();
    }

    /**
     * The required Perception check for an explorer to find the fixture.
     */
    @Override
    public int getDC() {
        return 0;
    }
}
