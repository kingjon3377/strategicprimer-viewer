package drivers.map_viewer;

import common.map.River;
import common.map.FakeFixture;

import java.util.Arrays;
import java.util.logging.Level;

import java.util.Set;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.stream.Collectors;

/**
 * A fake "TileFixture" to represent the rivers on a tile, so they can appear
 * in the list of the tile's contents.
 */
/* package */ class RiverFixture implements FakeFixture {
	@Override
	public String getDefaultImage() {
		return "river.png";
	}

	private final Set<River> rivers;

	public Collection<River> getRivers() {
		return rivers;
	}

	public RiverFixture(final River... rivers) {
		if (rivers.length == 0) {
			this.rivers = Collections.emptySet();
		} else {
			this.rivers = EnumSet.copyOf(Arrays.asList(rivers));
		}
	}

	/**
	 * Clone the object.
	 *
	 * @deprecated This class should only ever be used in a
	 * FixtureListModel, and copying a tile's rivers should be handled
	 * specially anyway, so this method should never be called.
	 * @param zero
	 */
	@Deprecated
	@Override
	public RiverFixture copy(final CopyBehavior zero) {
		LOGGER.log(Level.WARNING, "TileTypeFixture.copy called", new Exception("dummy"));
		return new RiverFixture(rivers.toArray(new River[0]));
	}

	@Override
	public boolean equals(final Object obj) {
		return obj instanceof RiverFixture && rivers.containsAll(((RiverFixture) obj).rivers) &&
			((RiverFixture) obj).rivers.containsAll(rivers);
	}

	@Override
	public int hashCode() {
		return rivers.hashCode();
	}

	@Override
	public String toString() {
		return rivers.stream().map(River::toString)
			.collect(Collectors.joining(", ", "Rivers: ", ""));
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
