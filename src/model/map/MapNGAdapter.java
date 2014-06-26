package model.map;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import model.map.fixtures.Ground;
import model.map.fixtures.terrain.Forest;
import model.map.fixtures.terrain.Mountain;

import org.eclipse.jdt.annotation.Nullable;

import util.NullCleaner;

/**
 * An implementation of IMapNG that is, under the hood, just a MapView.
 *
 * TODO: Write tests.
 *
 * TODO: Write proper toString based on the interface.
 *
 * @author Jonathan Lovelace
 *
 */
public class MapNGAdapter implements IMapNG { // $codepro.audit.disable
	/**
	 * The old-interface map we use for our state.
	 */
	private final MapView state;

	/**
	 * Constructor.
	 *
	 * @param wrapped the map to adapt to the new interface
	 */
	public MapNGAdapter(final MapView wrapped) {
		state = wrapped;
	}

	/**
	 * @param obj another map
	 * @param ostream the stream to write to
	 * @param context
	 *            a string to print before every line of output, describing the
	 *            context
	 * @return whether it is a strict subset of this map.
	 * @throws IOException on I/O error writing output to the stream
	 */
	@Override
	public boolean isSubset(final IMapNG obj, final Appendable ostream,
			final String context) throws IOException {
		if (dimensions().equals(obj.dimensions())) {
			boolean retval = true;
			for (final Player player : obj.players()) {
				if (player != null && !state.getPlayers().contains(player)) {
					// return false;
					retval = false;
					ostream.append(context);
					ostream.append("\tExtra player ");
					ostream.append(player.toString());
					ostream.append('\n');
				}
			}
			for (final Point point : obj.locations()) {
				if (point != null && !isTileSubset(obj, ostream, point, context)) {
					retval = false;
				}
			}
			return retval; // NOPMD
		} else {
			ostream.append("Sizes differ\n");
			return false;
		}
	}

	/**
	 * @param obj the map that might be a subset of us
	 * @param ostream the stream to write detailed results to
	 * @param loc the current location
	 * @param context
	 *            a string to print before every line of output, describing the
	 *            context
	 * @return whether that location fits the "subset" hypothesis.
	 * @throws IOException on I/O error writing output to the stream
	 */
	private boolean isTileSubset(final IMapNG obj, final Appendable ostream,
			final Point loc, final String context) throws IOException {
		boolean retval = true;
		final String ctxt = context + " At " + loc.toString() + ':';
		if (!getBaseTerrain(loc).equals(obj.getBaseTerrain(loc))) { // NOPMD
			// return false;
			retval = false;
			ostream.append(ctxt);
			ostream.append("\tTile types differ\n");
		} else if (isMountainous(loc) != obj.isMountainous(loc)) { // NOPMD
			// return false;
			retval = false;
			ostream.append(ctxt);
			ostream.append("\tReports of mountains differ\n");
		} else if (!areRiversSubset(getRivers(loc), obj.getRivers(loc))) { //NOPMD
			retval = false;
			ostream.append(ctxt);
			ostream.append("\tExtra rivers\n");
		} else if (!Objects.equals(getForest(loc), obj.getForest(loc))) { // NOPMD
			// return false;
			retval = false;
			ostream.append(ctxt);
			ostream.append("\tPrimary forests differ. Representation error?\n");
		} else if (!Objects.equals(getGround(loc), obj.getGround(loc))) { // NOPMD
			// return false;
			retval = false;
			ostream.append(ctxt);
			ostream.append("\tPrimary Ground differs. Representation error?\n");
		} else {
			// TODO: Use Guava collection-from-iterable to improve/simplify this
			final List<TileFixture> fixtures = new ArrayList<>();
			for (final TileFixture fix : state.getTile(loc)) {
				fixtures.add(fix);
			}
			for (final TileFixture fix : obj.getOtherFixtures(loc)) {
				if (fix != null && !fixtures.contains(fix)
						&& !Tile.shouldSkip(fix)) {
					// return false;
					retval = false;
					ostream.append(ctxt);
					ostream.append(" Extra fixture:\t");
					ostream.append(fix.toString());
					ostream.append('\n');
				}
			}
		}
		return retval;
	}

	/**
	 * @param ours Our rivers
	 * @param theirs Another map's rivers for the same location
	 * @return whether theirs are a subset of ours.
	 */
	private static boolean areRiversSubset(final Iterable<River> ours,
			final Iterable<River> theirs) {
		final Set<River> theirRivers = EnumSet.noneOf(River.class);
		for (final River river : theirs) {
			theirRivers.add(river);
		}
		for (final River river : ours) {
			theirRivers.remove(river);
		}
		return !theirRivers.isEmpty();
	}

	/**
	 * Compare to another map. This method is needed so the class can be put in
	 * a Pair.
	 *
	 * @param other the other map
	 * @return the result of the comparison
	 */
	@Override
	public int compareTo(@Nullable final IMapNG other) {
		if (other == null) {
			throw new IllegalArgumentException("Compared to null map");
		} else if (equals(other)) {
			return 0; // NOPMD
		} else {
			return hashCode() - other.hashCode();
		}
	}

	/**
	 * @return a view of the players in the map.
	 */
	@Override
	public Iterable<Player> players() {
		return state.getPlayers();
	}

	/**
	 * @return a view of the locations on the map
	 */
	@Override
	public Iterable<Point> locations() {
		return state.getTiles();
	}

	/**
	 * @param location a location
	 * @return the "base terrain" at that location
	 */
	@Override
	public TileType getBaseTerrain(final Point location) {
		return state.getTile(location).getTerrain();
	}

	/**
	 * @param location a location
	 * @return whether that location is mountainous
	 */
	@SuppressWarnings("deprecation")
	@Override
	public boolean isMountainous(final Point location) {
		if (dimensions().version < 2
				&& TileType.Mountain.equals(getBaseTerrain(location))) {
			return true; // NOPMD
		}
		for (final TileFixture fix : state.getTile(location)) {
			if (fix instanceof Mountain) {
				return true; // NOPMD
			}
		}
		return false;
	}

	/**
	 * @param location a location
	 * @return a view of the river directions, if any, at that location
	 */
	@Override
	public Iterable<River> getRivers(final Point location) {
		if (state.getTile(location).hasRiver()) {
			return state.getTile(location).getRivers(); // NOPMD
		} else {
			return NullCleaner.assertNotNull(EnumSet.noneOf(River.class));
		}
	}

	/**
	 * Implementations should aim to have only the "main" forest here, and any
	 * "extra" forest Fixtures in the "et cetera" collection.
	 *
	 * @param location a location
	 * @return the forest (if any) at that location; null if there is none
	 */
	@Override
	@Nullable
	public Forest getForest(final Point location) {
		Forest retval = null;
		for (final TileFixture fix : state.getTile(location)) {
			if (fix instanceof Forest) {
				if (retval == null || retval.isRows()) {
					retval = (Forest) fix;
				} else {
					break;
				}
			}
		}
		return retval;
	}

	/**
	 * We actually include the main Ground and Forest too; there's no easy way
	 * around that ...
	 *
	 * @param location a location
	 * @return a view of any fixtures on the map that aren't covered in the
	 *         other querying methods.
	 */
	@Override
	public Iterable<TileFixture> getOtherFixtures(final Point location) {
		return state.getTile(location);
	}

	/**
	 * @return the current turn
	 */
	@Override
	public int getCurrentTurn() {
		return state.getCurrentTurn();
	}

	/**
	 * @return the current player
	 */
	@Override
	public Player getCurrentPlayer() {
		return state.getPlayers().getCurrentPlayer();
	}

	/**
	 * Implementations should aim to have only the "main" Ground here, and any
	 * exposed or otherwise "extra" Fixtures in the "et cetera" collection.
	 *
	 * @param location a location
	 * @return the Ground at that location
	 */
	@Override
	@Nullable
	public Ground getGround(final Point location) {
		Ground retval = null;
		for (final TileFixture fix : state.getTile(location)) {
			if (fix instanceof Ground) {
				if (retval == null || retval.isExposed()) {
					retval = (Ground) fix;
				} else {
					break;
				}
			}
		}
		return retval;
	}

	/**
	 * @param obj an object
	 * @return whether it's the same as us---we're a subset of it and it's a
	 *         subset of us
	 */
	@Override
	public boolean equals(@Nullable final Object obj) {
		return this == obj || obj instanceof IMapNG && equalsImpl((IMapNG) obj);
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
		// ESCA-JAVA0177:
		final Collection<T> first;
		if (one instanceof Collection) {
			first = (Collection<T>) one;
		} else {
			first = new ArrayList<>();
			for (final T item : one) {
				first.add(item);
			}
		}
		// ESCA-JAVA0177:
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
	 * @return the map's dimensions and version
	 */
	@Override
	public MapDimensions dimensions() {
		return state.getDimensions();
	}
}
