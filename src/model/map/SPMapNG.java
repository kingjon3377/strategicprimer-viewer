package model.map;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import model.map.fixtures.Ground;
import model.map.fixtures.terrain.Forest;
import model.viewer.PointIterator;

import org.eclipse.jdt.annotation.Nullable;

import util.ArraySet;
import util.EmptyIterator;
import util.IteratorWrapper;
import util.NullCleaner;
/**
 * A proper implementation of IMapNG.
 * @author Jonathan Lovelace
 */
public class SPMapNG implements IMutableMapNG {
	/**
	 * The set of mountainous places.
	 * TODO: Populate the set.
	 */
	private final Set<Point> mountains = new HashSet<>();
	/**
	 * The base terrain at points in the map.
	 * TODO: Populate the map.
	 */
	private final Map<Point, TileType> terrain = new HashMap<>();
	/**
	 * The players in the map.
	 */
	private final PlayerCollection playerCollection;
	/**
	 * The current turn.
	 */
	private int turn;
	/**
	 * The forests in the map. If there's more than one forest, only one goes
	 * here, and the rest go in the "miscellaneous fixtures" pile.
	 *
	 * TODO: populate the map.
	 */
	private final Map<Point, Forest> forests = new HashMap<>();
	/**
	 * Fixtures at various points, other than the main ground and forest. We
	 * specify Collection rather than Iterable because they need to be
	 * explicitly mutable for unit motion.
	 *
	 * TODO: populate the map
	 *
	 * TODO: Use a multimap once we add the Guava dependency.
	 */
	private final Map<Point, Collection<TileFixture>> fixtures = new HashMap<>();
	/**
	 * The dimensions of the map.
	 */
	private final MapDimensions dims;
	/**
	 * The ground under various locations. If there's more than one, others go
	 * in the "other fixtures" collection.
	 *
	 * TODO: Populate the map.
	 */
	private final Map<Point, Ground> ground = new HashMap<>();
	/**
	 * The rivers in the map.
	 * TODO: populate the map; remember to use EnumSets, not RiverFixtures.
	 */
	private final Map<Point, EnumSet<River>> rivers = new HashMap<>();
	/**
	 * @param obj another map
	 * @param ostream the stream to write verbose results to
	 * @param context
	 *            a string to print before every line of output, describing the
	 *            context
	 * @return whether the other map is a subset of this one
	 * @throws IOException on I/O error writing output to the stream
	 */
	@Override
	public boolean isSubset(final IMapNG obj, final Appendable ostream,
			final String context) throws IOException {
		if (dimensions().equals(obj.dimensions())) {
			// TODO: We should probably delegate this to the PlayerCollection.
			boolean retval = true;
			for (final Player player : obj.players()) {
				if (player != null && !playerCollection.contains(player)) {
					ostream.append(context);
					ostream.append("\tExtra player ");
					ostream.append(player.toString());
					ostream.append('\n');
					retval = false;
					// return false;
				}
			}
			for (final Point point : locations()) {
				final String ctxt =
						context + " At " + Objects.toString(point) + ':';
				if (point == null) {
					continue;
				} else if (!getBaseTerrain(point).equals(
						obj.getBaseTerrain(point))) {
					ostream.append(ctxt);
					ostream.append("\tBase terrain differs\n");
					retval = false;
					continue;
//					return false;
				}
				if (obj.isMountainous(point) && !isMountainous(point)) {
					ostream.append(ctxt);
					ostream.append("\tHas mountains we don't\n");
					retval = false;
					// return false;
				}
				if (!Objects.equals(getForest(point), obj.getForest(point))
						&& obj.getForest(point) != null) {
					// TODO: Shouldn't do getForest call twice
					ostream.append(ctxt);
					ostream.append("\tHas forest we don't, or different primary forest");
					retval = false;
					// return false;
				}
				if (!Objects.equals(getGround(point), obj.getGround(point))
						&& obj.getGround(point) != null) {
					// TODO: Shouldn't do getGround call twice
					ostream.append(ctxt);
					ostream.append("\tHas different primary ground, or ground we don't");
					retval = false;
					// return false;
				}
				final Collection<TileFixture> ourFixtures =
						(Collection<TileFixture>) getOtherFixtures(point);
				final Iterable<TileFixture> theirFixtures = obj
						.getOtherFixtures(point);
				for (final TileFixture fix : theirFixtures) {
					if (!ourFixtures.contains(fix)) {
						ostream.append(ctxt);
						ostream.append(" Extra fixture:\t");
						ostream.append(fix.toString());
						ostream.append('\n');
						retval = false;
						break;
						// return false;
					}
				}
				final Set<River> ourRivers = rivers.get(point);
				final Iterable<River> theirRivers = obj.getRivers(point);
				for (final River river : theirRivers) {
					if (river != null && !ourRivers.contains(river)) {
						ostream.append(ctxt);
						ostream.append("\tExtra river\n");
						retval = false;
						break;
						// return false;
					}
				}
			}
			return retval; // NOPMD
		} else {
			ostream.append(context);
			ostream.append("\tDimension mismatch\n");
			return false; // NOPMD
		}
	}
	/**
	 * @param other another map
	 * @return the result of a comparison between us and it.
	 */
	@Override
	public int compareTo(final IMapNG other) {
		if (equals(other)) {
			return 0; // NOPMD
		} else {
			return hashCode() - other.hashCode();
		}
	}
	/**
	 * Constructor.
	 * @param dimensions the dimensions of the map
	 * @param players the players in the map
	 * @param currentTurn the current turn
	 */
	public SPMapNG(final MapDimensions dimensions,
			final PlayerCollection players, final int currentTurn) {
		dims = dimensions;
		playerCollection = players;
		turn = currentTurn;
	}
	/**
	 * @return the map's dimensions
	 */
	@Override
	public MapDimensions dimensions() {
		return dims;
	}
	/**
	 * @return the players in the map
	 */
	@Override
	public Iterable<Player> players() {
		return playerCollection;
	}
	/**
	 * @return the locations in the map
	 */
	@Override
	public Iterable<Point> locations() {
		return new IteratorWrapper<>(new PointIterator(dimensions(), null,
				true, true));
	}
	/**
	 * @param location a location
	 * @return the base terrain at that location
	 */
	@Override
	public TileType getBaseTerrain(final Point location) {
		if (terrain.containsKey(location)) {
			return NullCleaner.assertNotNull(terrain.get(location)); // NOPMD
		} else {
			return TileType.NotVisible;
		}
	}
	/**
	 * @param location a location
	 * @return whether that location is mountainous
	 */
	@Override
	public boolean isMountainous(final Point location) {
		return mountains.contains(location);
	}
	/**
	 * @param location a location
	 * @return the rivers there
	 */
	@Override
	public Iterable<River> getRivers(final Point location) {
		if (rivers.containsKey(location)) {
			return NullCleaner.assertNotNull(rivers.get(location)); // NOPMD
		} else {
			return NullCleaner.assertNotNull(EnumSet.noneOf(River.class));
		}
	}

	/**
	 * @param location a location
	 * @return any forests there
	 */
	@Override
	@Nullable public Forest getForest(final Point location) {
		return forests.get(location);
	}

	/**
	 * @param location a location
	 * @return the ground there
	 */
	@Override
	@Nullable public Ground getGround(final Point location) {
		return ground.get(location);
	}

	/**
	 * @param location a location
	 * @return any other fixtures there
	 */
	@Override
	public Iterable<TileFixture> getOtherFixtures(final Point location) {
		if (fixtures.containsKey(location)) {
			return NullCleaner.assertNotNull(fixtures.get(location)); // NOPMD
		} else {
			return new IteratorWrapper<>(new EmptyIterator<TileFixture>());
		}
	}
	/**
	 * @return the current turn
	 */
	@Override
	public int getCurrentTurn() {
		return turn;
	}
	/**
	 * @return the current player
	 */
	@Override
	public Player getCurrentPlayer() {
		return playerCollection.getCurrentPlayer();
	}
	/**
	 * @param obj an object
	 * @return whether it's a map equal to this one
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return obj == this || obj instanceof IMapNG && equalsImpl((IMapNG) obj);
	}
	/**
	 * @param obj another map
	 * @return whether it equals this one
	 */
	private boolean equalsImpl(final IMapNG obj) {
		if (dimensions().equals(obj.dimensions())
				&& iterablesEqual(players(), obj.players())
				&& getCurrentTurn() == obj.getCurrentTurn()
				&& getCurrentPlayer().equals(obj.getCurrentPlayer())) {
			for (final Point point : locations()) {
				if (point == null) {
					continue;
				} else if (!getBaseTerrain(point).equals(
						obj.getBaseTerrain(point))
						|| isMountainous(point) != obj.isMountainous(point)
						|| !iterablesEqual(getRivers(point),
								obj.getRivers(point))
						|| !Objects.equals(getForest(point),
								obj.getForest(point))
						|| !Objects.equals(getGround(point),
								obj.getGround(point))
						|| !iterablesEqual(getOtherFixtures(point),
								obj.getOtherFixtures(point))) {
					return false; // NOPMD
				}
			}
			return true; // NOPMD
		} else {
			return false; // NOPMD
		}
	}
	/**
	 * FIXME: This is probably very slow ...
	 * @param one one iterable
	 * @param two another
	 * @param <T> the type of thing they contain
	 * @return whether they contain the same elements.
	 */
	private static <T> boolean iterablesEqual(final Iterable<T> one,
			final Iterable<T> two) {
		final Collection<T> first;
		if (one instanceof Collection) {
			first = (Collection<T>) one;
		} else {
			first = new ArrayList<>();
			for (final T item : one) {
				first.add(item);
			}
		}
		final Collection<T> second;
		if (two instanceof Collection) {
			second = (Collection<T>) two;
		} else {
			second = new ArrayList<>();
			for (final T item : two) {
				second.add(item);
			}
		}
		return first.containsAll(second) && second.containsAll(first);
	}

	/**
	 * The hash code is based on the dimensions, the current turn, and the
	 * current player; basing it on anything else would certainly break any code
	 * that placed an IMapNG into a hash-table.
	 *
	 * @return a hash code for the object
	 */
	@Override
	public int hashCode() {
		return dimensions().hashCode() + getCurrentTurn() << 3 + getCurrentPlayer()
				.hashCode() << 5;
	}

	/**
	 * FIXME: Implement properly.
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "SPMapNG: FIXME: Implement properly";
	}
	/**
	 * @param player the player to add
	 */
	@Override
	public void addPlayer(final Player player) {
		playerCollection.add(player);
	}
	/**
	 * @param location a location
	 * @param ttype the terrain there
	 */
	@Override
	public void setBaseTerrain(final Point location, final TileType ttype) {
		terrain.put(location, ttype);
	}
	/**
	 * @param location a location
	 * @param mtn whether it's mountainous there
	 */
	@Override
	public void setMountainous(final Point location, final boolean mtn) {
		if (mtn) {
			mountains.add(location);
		} else {
			mountains.remove(location);
		}
	}
	/**
	 * @param location a location
	 * @param rvrs rivers to add to that location
	 */
	@Override
	public void addRivers(final Point location, final River... rvrs) {
		final EnumSet<River> localRivers;
		final EnumSet<River> temp = rivers.get(location);
		if (temp == null) {
			localRivers = EnumSet.noneOf(River.class);
			rivers.put(location, localRivers);
		} else {
			localRivers = temp;
		}
		for (River river : rvrs) {
			localRivers.add(river);
		}
	}
	/**
	 * @param location a location
	 * @param rvrs rivers to remove from it
	 */
	@Override
	public void removeRivers(final Point location, final River... rvrs) {
		final Set<River> localRivers = rivers.get(location);
		if (localRivers != null) {
			for (River river : rvrs) {
				localRivers.remove(river);
			}
		}
	}
	/**
	 * @param location a location
	 * @param forest what should be the primary forest there, if any
	 */
	@Override
	public void setForest(final Point location, @Nullable final Forest forest) {
		if (forest == null) {
			forests.remove(location);
		} else {
			forests.put(location, forest);
		}
	}
	/**
	 * @param location a location
	 * @param grnd what the ground there should be, if any
	 */
	@Override
	public void setGround(final Point location, @Nullable final Ground grnd) {
		if (grnd == null) {
			ground.remove(location);
		} else {
			ground.put(location, grnd);
		}
	}
	/**
	 * @param location a location
	 * @param fix a fixture to add there
	 */
	@Override
	public void addFixture(final Point location, final TileFixture fix) {
		final Collection<TileFixture> temp = fixtures.get(location);
		final Collection<TileFixture> local;
		if (temp == null) {
			local = new ArraySet<>();
			fixtures.put(location, local);
		} else {
			local = temp;
		}
		local.add(fix);
	}
	/**
	 * @param location a location
	 * @param fix a fixture to remove from that locaation
	 */
	@Override
	public void removeFixture(final Point location, final TileFixture fix) {
		final Collection<TileFixture> local = fixtures.get(location);
		if (local != null) {
			local.remove(fix);
		}
	}
	/**
	 * @param player the new current player
	 */
	@Override
	public void setCurrentPlayer(final Player player) {
		playerCollection.getCurrentPlayer().setCurrent(false);
		playerCollection.getPlayer(player.getPlayerId()).setCurrent(true);
	}
	/**
	 * @param curr the new current turn
	 */
	@Override
	public void setTurn(final int curr) {
		turn = curr;
	}
}
