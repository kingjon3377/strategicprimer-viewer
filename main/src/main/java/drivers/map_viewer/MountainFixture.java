package drivers.map_viewer;

import legacy.map.FakeFixture;
import lovelace.util.LovelaceLogger;

/**
 * A fake "TileFixture" to represent the mountain(s) on a mountainous tile, so
 * it/they can appear in the list of the tile's contents.
 */
/* package */ class MountainFixture implements FakeFixture {
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
	public MountainFixture copy(final CopyBehavior zero) {
		LovelaceLogger.warning(new Exception("dummy"), "TileTypeFixture.copy called");
		return new MountainFixture();
	}

	/**
	 * The required Perception check for an explorer to find the fixture.
	 */
	@Override
	public int getDC() {
		return 0;
	}

	@Override
	public String getDefaultImage() {
		return "mountain.png";
	}

	@Override
	public String getShortDescription() {
		return "Mountainous terrain";
	}

	@Override
	public boolean equals(final Object that) {
		return that instanceof MountainFixture;
	}

	@Override
	public int hashCode() {
		return MountainFixture.class.hashCode();
	}
}
