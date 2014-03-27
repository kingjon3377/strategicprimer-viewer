package model.map;

import java.io.PrintWriter;
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

import util.EmptyIterator;
import util.IteratorWrapper;
/**
 * A proper implementation of IMapNG.
 * @author Jonathan Lovelace
 */
public class SPMapNG implements IMapNG {
	/**
	 * @param obj another map
	 * @param out the stream to write verbose results to
	 * @return whether the other map is a subset of this one
	 */
	@Override
	public boolean isSubset(final IMapNG obj, final PrintWriter out) {
		if (!dimensions().equals(obj.dimensions())) {
			out.println("Dimension mismatch");
			return false; // NOPMD
		} else {
			boolean retval = true;
			for (final Player player : obj.players()) {
				if (player != null && !playerCollection.contains(player)) {
					out.print("Extra player ");
					out.println(player);
					retval = false;
					// return false;
				}
			}
			for (final Point point : locations()) {
				if (point == null) {
					continue;
				} else if (!getBaseTerrain(point).equals(
						obj.getBaseTerrain(point))) {
					out.print("Base terrain differs at ");
					out.println(point);
					retval = false;
					continue;
//					return false;
				}
				if (obj.isMountainous(point) && !isMountainous(point)) {
					out.print("Has mountains we don't at ");
					out.println(point);
					retval = false;
					// return false;
				}
				if (!Objects.equals(getForest(point), obj.getForest(point))
						&& obj.getForest(point) != null) {
					// TODO: Shouldn't do getForest call twice
					out.print("Has forest we don't, or ");
					out.print("different primary forest, at ");
					out.println(point);
					retval = false;
					// return false;
				}
				if (!Objects.equals(getGround(point), obj.getGround(point))
						&& obj.getGround(point) != null) {
					out.print("Has different primary ground, ");
					out.print("or ground we don't, at ");
					out.println(point);
					retval = false;
					// return false;
				}
				final Collection<TileFixture> ourFixtures =
						(Collection<TileFixture>) getOtherFixtures(point);
				final Iterable<TileFixture> theirFixtures = obj
						.getOtherFixtures(point);
				for (final TileFixture fix : theirFixtures) {
					if (!ourFixtures.contains(fix)) {
						out.print("Extra fixture at ");
						out.print(point);
						out.print(": ");
						out.println(fix);
						retval = false;
						break;
						// return false;
					}
				}
				final Set<River> ourRivers = rivers.get(point);
				final Iterable<River> theirRivers = obj.getRivers(point);
				for (final River river : theirRivers) {
					if (river != null && !ourRivers.contains(river)) {
						out.print("Extra river at ");
						out.println(point);
						retval = false;
						break;
						// return false;
					}
				}
			}
			return retval;
		}
	}
	/**
	 * @param other another map
	 * @return the result of a comparison between us and it.
	 */
	@Override
	public int compareTo(final IMapNG other) {
		if (equals(other)) {
			return 0;
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
	 * The dimensions of the map.
	 */
	private final MapDimensions dims;
	/**
	 * @return the map's dimensions
	 */
	@Override
	public MapDimensions dimensions() {
		return dims;
	}
	/**
	 * The players in the map.
	 */
	private final PlayerCollection playerCollection;
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
	 * The base terrain at points in the map.
	 * TODO: Populate the map.
	 */
	private final Map<Point, TileType> terrain = new HashMap<>();
	/**
	 * @param location a location
	 * @return the base terrain at that location
	 */
	@Override
	public TileType getBaseTerrain(final Point location) {
		if (terrain.containsKey(location)) {
			final TileType retval = terrain.get(location);
			assert retval != null;
			return retval; // NOPMD
		} else {
			return TileType.NotVisible;
		}
	}
	/**
	 * The set of mountainous places.
	 * TODO: Populate the set.
	 */
	private final Set<Point> mountains = new HashSet<>();
	/**
	 * @param location a location
	 * @return whether that location is mountainous
	 */
	@Override
	public boolean isMountainous(final Point location) {
		return mountains.contains(location);
	}
	/**
	 * The rivers in the map.
	 * TODO: populate the map; remember to use EnumSets, not RiverFixtures.
	 */
	private final Map<Point, EnumSet<River>> rivers = new HashMap<>();
	/**
	 * @param location a location
	 * @return the rivers there
	 */
	@Override
	public Iterable<River> getRivers(final Point location) {
		// ESCA-JAVA0177:
		final Iterable<River> retval;
		if (rivers.containsKey(location)) {
			retval = rivers.get(location);
		} else {
			retval = EnumSet.noneOf(River.class);
		}
		assert retval != null;
		return retval;
	}

	/**
	 * The forests in the map. If there's more than one forest, only one goes
	 * here, and the rest go in the "miscellaneous fixtures" pile.
	 *
	 * TODO: populate the map.
	 */
	private final Map<Point, Forest> forests = new HashMap<>();
	/**
	 * @param location a location
	 * @return any forests there
	 */
	@Override
	@Nullable public Forest getForest(final Point location) {
		return forests.get(location);
	}

	/**
	 * The ground under various locations. If there's more than one, others go
	 * in the "other fixtures" collection.
	 *
	 * TODO: Populate the map.
	 */
	private final Map<Point, Ground> ground = new HashMap<>();
	/**
	 * @param location a location
	 * @return the ground there
	 */
	@Override
	@Nullable public Ground getGround(final Point location) {
		return ground.get(location);
	}

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
	 * @param location a location
	 * @return any other fixtures there
	 */
	@Override
	public Iterable<TileFixture> getOtherFixtures(final Point location) {
		if (fixtures.containsKey(location)) {
			final Iterable<TileFixture> retval = fixtures.get(location);
			assert retval != null;
			return retval; // NOPMD
		} else {
			return new IteratorWrapper<>(new EmptyIterator<TileFixture>());
		}
	}
	/**
	 * The current turn.
	 */
	private final int turn;
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
		return obj == this || (obj instanceof IMapNG && equalsImpl((IMapNG) obj));
	}
	/**
	 * @param obj another map
	 * @return whether it equals this one
	 */
	private boolean equalsImpl(final IMapNG obj) {
		if (!dimensions().equals(obj.dimensions())
				|| !iterablesEqual(players(), obj.players())
				|| getCurrentTurn() != obj.getCurrentTurn()
				|| !getCurrentPlayer().equals(obj.getCurrentPlayer())) {
			return false; // NOPMD
		} else {
			for (final Point point : locations()) {
				if (point == null) {
					continue;
				} else if (!getBaseTerrain(point).equals(obj.getBaseTerrain(point))
						|| isMountainous(point) != obj.isMountainous(point)
						|| !iterablesEqual(getRivers(point),
								obj.getRivers(point))
						|| !Objects.equals(getForest(point),
								obj.getForest(point))
						|| !Objects.equals(getGround(point),
								obj.getGround(point))
						|| !iterablesEqual(getOtherFixtures(point),
								obj.getOtherFixtures(point))) {
					return false;
				}
			}
			return true;
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
			for (final T item : second) {
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
}
