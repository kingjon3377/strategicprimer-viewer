package model.map;

import static model.map.TileType.NotVisible;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import model.map.fixtures.Ground;
import model.map.fixtures.RiverFixture;

import org.eclipse.jdt.annotation.Nullable;
/**
 * An IMap implementation that uses an IMapNG for its backend.
 * @author Jonathan Lovelace
 *
 */
public class MapNGReverseAdapter implements IMap {
	/**
	 * The new-interface map we wrap.
	 */
	private final IMapNG impl;
	/**
	 * @param wrapped the new-interface map to wrap
	 */
	public MapNGReverseAdapter(final IMapNG wrapped) {
		impl = wrapped;
	}
	/**
	 *
	 * @param obj another map
	 * @param out a stream to write comments to
	 * @return whether it's a subset of this one
	 */
	@Override
	public boolean isSubset(@Nullable final IMap obj, final PrintWriter out) {
		throw new IllegalStateException("Not yet implemented");
	}
	/**
	 * @param obj a map to compare to
	 * @return the result of the comparison
	 */
	@Override
	public int compareTo(final IMap obj) {
		throw new IllegalStateException("Not yet implemented");
	}
	/**
	 * @return the dimensions of the map
	 */
	@Override
	public MapDimensions getDimensions() {
		return impl.dimensions();
	}
	/**
	 * @param point a point on the map
	 * @return what's there
	 */
	@Override
	public ITile getTile(final Point point) {
		return new TileAdapter(impl, point);
	}

	@Override
	public PlayerCollection getPlayers() {
		throw new IllegalStateException("Not yet implemented");
	}

	@Override
	public TileCollection getTiles() {
		throw new IllegalStateException("Not yet implemented");
	}
	/**
	 * A "Tile" that looks to the IMapNG.
	 */
	private static class TileAdapter implements ITile {
		/**
		 * Where this tile is.
		 */
		protected final Point loc;
		/**
		 * Constructor.
		 * @param outer the map to refer to
		 * @param location where this tile is
		 */
		protected TileAdapter(final IMapNG outer, final Point location) {
			map = outer;
			loc = location;
		}
		/**
		 * The map to refer to.
		 */
		protected final IMapNG map;
		/**
		 * @param obj another tile
		 * @param out a stream to write comments to
		 * @return whether it's a strict subset of this tile or not
		 */
		@Override
		public boolean isSubset(final ITile obj, final PrintWriter out) {
			throw new IllegalStateException("Not yet implemented");
		}

		/**
		 * FIXME: Instead of constructing a new list every time, make a new
		 * Iterator implementation for this class that keeps track of where the
		 * reader is in the progression.
		 *
		 * @return an iterator over the tile's fixtures
		 */
		@Override
		public Iterator<TileFixture> iterator() {
			final List<TileFixture> list = new ArrayList<>();
			maybeAdd(list, map.getGround(loc));
			maybeAdd(list, map.getForest(loc));
			for (final TileFixture fix : map.getOtherFixtures(loc)) {
				maybeAdd(list, fix);
			}
			if (hasRiver()) {
				final RiverFixture rivers = new RiverFixture();
				for (final River river : getRivers()) {
					if (river != null) {
						rivers.addRiver(river);
					}
				}
				list.add(rivers);
			}
			final Iterator<TileFixture> retval = list.iterator();
			assert retval != null;
			return retval;
		}
		/**
		 * Does nothing if fix is null, which is the point.
		 * @param list a list of fixtures
		 * @param fix a fixture to add to it, or null.
		 */
		private static void maybeAdd(final List<TileFixture> list, @Nullable final TileFixture fix) {
			if (fix != null) {
				list.add(fix);
			}
		}
		/**
		 * @return false if there is anything here
		 */
		@Override
		public boolean isEmpty() {
			return !NotVisible.equals(map.getBaseTerrain(loc))
					&& !map.getRivers(loc).iterator().hasNext()
					&& map.getGround(loc) == null && map.getForest(loc) == null
					&& !map.getOtherFixtures(loc).iterator().hasNext();
		}
		/**
		 * @return whether there are any rivers here
		 */
		@Override
		public boolean hasRiver() {
			return map.getRivers(loc).iterator().hasNext();
		}
		/**
		 * @return the rivers here, if any
		 */
		@Override
		public Iterable<River> getRivers() {
			return map.getRivers(loc);
		}
		/**
		 * @return the terrain at this location
		 */
		@Override
		public TileType getTerrain() {
			return map.getBaseTerrain(loc);
		}
	}
}
