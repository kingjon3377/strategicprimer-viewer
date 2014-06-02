package model.map;

import static model.map.TileType.NotVisible;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import model.map.fixtures.RiverFixture;

import org.eclipse.jdt.annotation.Nullable;

import util.NullCleaner;
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
	 * @param ostream a stream to write comments to
	 * @param context
	 *            a string to print before every line of output, describing the
	 *            context
	 * @return whether it's a subset of this one
	 * @throws IOException on I/O error writing output to the stream
	 */
	@Override
	public boolean isSubset(@Nullable final IMap obj, final Appendable ostream,
			final String context) throws IOException {
		throw new IllegalStateException("Not yet implemented");
	}
	/**
	 * @param obj a map to compare to
	 * @return the result of the comparison
	 */
	@Override
	public int compareTo(final IMap obj) {
		if (equals(obj)) {
			return 0; // NOPMD
		} else {
			return hashCode() - obj.hashCode();
		}
	}
	/**
	 * @param obj an object
	 * @return whether it's a map equal to this one
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return this == obj || obj instanceof IMap
				&& getDimensions().equals(((IMap) obj).getDimensions())
				&& getPlayers().equals(((IMap) obj).getPlayers())
				&& getTiles().equals(((IMap) obj).getTiles());
	}
	/**
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return impl.hashCode();
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
	/**
	 * @return the players in the map
	 */
	@Override
	public IPlayerCollection getPlayers() {
		return new PlayerCollectionAdapter(impl);
	}
	/**
	 * @return the locations in the map
	 */
	@Override
	public ITileCollection getTiles() {
		return new TileCollectionAdapter(impl);
	}
	/**
	 * An adaptor to the ITileCollection interface.
	 * @author Jonathan Lovelace
	 */
	private static final class TileCollectionAdapter implements ITileCollection {
		/**
		 * The map we're adapting.
		 */
		private final IMapNG outer;
		/**
		 * Constructor.
		 * @param map the map we're adapting.
		 */
		protected TileCollectionAdapter(final IMapNG map) {
			outer = map;
		}
		/**
		 * @return an iterator over the points in the map
		 */
		@Override
		public Iterator<Point> iterator() {
			return NullCleaner.assertNotNull(outer.locations().iterator());
		}

		/**
		 * @param obj
		 *            another collection of tiles
		 * @param ostream
		 *            a stream to (TODO) write results on
		 * @param context
		 *            a string to print before every line of output, describing
		 *            the context
		 * @return whether the other collection is a subset of this one
		 * @throws IOException
		 *             on I/O error writing output to the stream
		 */
		@Override
		public boolean isSubset(final ITileCollection obj,
				final Appendable ostream, final String context)
				throws IOException {
			throw new IllegalStateException("FIXME: Not implemented yet");
		}
		/**
		 * @param point a location on the map
		 * @return the tile there
		 */
		@Override
		public ITile getTile(final Point point) {
			return new TileAdapter(outer, point);
		}
		/**
		 * @param point a location
		 * @return whether there is anything there
		 */
		@Override
		public boolean hasTile(final Point point) {
			return !NotVisible.equals(outer.getBaseTerrain(point))
					&& !outer.getRivers(point).iterator().hasNext()
					&& outer.getGround(point) == null
					&& outer.getForest(point) == null
					&& !outer.getOtherFixtures(point).iterator().hasNext();
		}
		/**
		 * @return a String representation of the object
		 */
		@Override
		public String toString() {
			return "ITileCollection view of a MapNG.";
		}
	}
	/**
	 * An adapter to the IPlayerCollection interface.
	 * @author Jonathan Lovelace
	 */
	private static final class PlayerCollectionAdapter implements
			IPlayerCollection {
		/**
		 * The new-interface map we're adapting.
		 */
		private final IMapNG outer;
		/**
		 * @param map the map to get the players from
		 */
		protected PlayerCollectionAdapter(final IMapNG map) {
			outer = map;
		}

		/**
		 * TODO: Write extra players to the stream.
		 *
		 * @param obj
		 *            another collection
		 * @param ostream
		 *            a stream to write to---ignored for now
		 * @param context
		 *            a string to print before every line of output, describing
		 *            the context
		 * @return whether the collection is a subset of this.
		 * @throws IOException
		 *             on I/O error writing output to the stream
		 *
		 * @see model.map.Subsettable#isSubset(java.lang.Object, Appendable)
		 */
		@Override
		public boolean isSubset(final IPlayerCollection obj,
				final Appendable ostream, final String context)
				throws IOException {
			for (final Player player : obj) {
				if (player != null && !contains(player)) {
					return false; // NOPMD
				}
			}
			return true;
		}
		/**
		 * @return an iterator over the players
		 */
		@Override
		public Iterator<Player> iterator() {
			return NullCleaner.assertNotNull(outer.players().iterator());
		}
		/**
		 * @param player a player number
		 * @return the corresponding player
		 */
		@Override
		public Player getPlayer(final int player) {
			for (final Player test : outer.players()) {
				if (test.getPlayerId() == player) {
					return test; // NOPMD
				}
			}
			return new Player(-1, "");
		}
		/**
		 * @return a player to own independent things
		 */
		@Override
		public Player getIndependent() {
			throw new IllegalStateException("Not implemented yet");
		}
		/**
		 * @return the current player
		 */
		@Override
		public Player getCurrentPlayer() {
			return outer.getCurrentPlayer();
		}
		/**
		 * @param obj a player
		 * @return whether we contain it
		 */
		@Override
		public boolean contains(final Player obj) {
			for (final Player player : this) {
				if (obj.equals(player)) {
					return true; // NOPMD
				}
			}
			return false;
		}
		/**
		 * @return a String representation of the object
		 */
		@Override
		public String toString() {
			return "A IPlayerCollection view of a MapNG.";
		}
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
		 * The map to refer to.
		 */
		protected final IMapNG map;
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
		 * @param obj
		 *            another tile
		 * @param ostream
		 *            a stream to write comments to
		 * @param context
		 *            a string to print before every line of output, describing
		 *            the context
		 * @return whether it's a strict subset of this tile or not
		 * @throws IOException
		 *             on I/O error writing output to the stream
		 */
		@Override
		public boolean isSubset(final ITile obj, final Appendable ostream,
				final String context) throws IOException {
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
			return NullCleaner.assertNotNull(list.iterator());
		}
		/**
		 * Does nothing if fix is null, which is the point.
		 * @param list a list of fixtures
		 * @param fix a fixture to add to it, or null.
		 */
		private static void maybeAdd(final List<TileFixture> list,
				@Nullable final TileFixture fix) {
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
		/**
		 * @return a String representation of the object
		 */
		@Override
		public String toString() {
			return "Location " + loc + " in a MapNG.";
		}
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "IMap adapter around the following MapNG instance:\n"
				+ impl.toString();
	}
}
