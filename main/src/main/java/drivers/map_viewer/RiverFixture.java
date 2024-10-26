package drivers.map_viewer;

import legacy.map.River;
import legacy.map.FakeFixture;

import java.util.Arrays;

import java.util.Set;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.stream.Collectors;

import lovelace.util.LovelaceLogger;

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
		return Collections.unmodifiableSet(rivers);
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
	 * @param zero
	 * @deprecated This class should only ever be used in a
	 * FixtureListModel, and copying a tile's rivers should be handled
	 * specially anyway, so this method should never be called.
	 */
	@Deprecated
	@Override
	public RiverFixture copy(final CopyBehavior zero) {
		LovelaceLogger.warning(new Exception("dummy"), "TileTypeFixture.copy called");
		return new RiverFixture(rivers.toArray(River[]::new));
	}

	@Override
	public boolean equals(final Object obj) {
		return obj instanceof final RiverFixture rf && rivers.containsAll(rf.rivers) && rf.rivers.containsAll(rivers);
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
