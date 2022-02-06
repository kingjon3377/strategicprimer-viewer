package drivers.map_viewer;

import common.map.TileType;
import common.map.FakeFixture;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * A fake "TileFixture" to represent the tile's terrain type, so it can be
 * copied via drag-and-drop like a fixture.
 */
/* package */ class TileTypeFixture implements FakeFixture {
	private static final Logger LOGGER = Logger.getLogger(TileTypeFixture.class.getName());
	private final TileType tileType;

	public TileType getTileType() {
		return tileType;
	}

	public TileTypeFixture(final TileType tileType) {
		this.tileType = tileType;
	}

	/**
	 * Clone the object.
	 *
	 * @deprecated This class should only ever be used in a
	 * FixtureListModel, and copying a tile's terrain type should be
	 * handled specially anyway, so this method should never be called.
	 */
	@Deprecated
	@Override
	public TileTypeFixture copy(final boolean zero) {
		LOGGER.log(Level.WARNING, "TileTypeFixture.copy called", new Exception("dummy"));
		return new TileTypeFixture(tileType);
	}

	@Override
	public boolean equals(final Object obj) {
		return obj instanceof TileTypeFixture && ((TileTypeFixture) obj).tileType == tileType;
	}

	@Override
	public int hashCode() {
		return tileType.hashCode();
	}

	@Override
	public String toString() {
		return "Terrain: " + tileType;
	}

	/**
	 * The filename of an image to represent the object.
	 *
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
