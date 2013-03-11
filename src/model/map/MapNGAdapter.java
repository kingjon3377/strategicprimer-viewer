package model.map;

import java.io.PrintWriter;
import java.util.EnumSet;

import model.map.fixtures.Ground;
import model.map.fixtures.terrain.Forest;
import model.map.fixtures.terrain.Mountain;
/**
 * An implementation of IMapNG that is, under the hood, just a MapView.
 * @author Jonathan Lovelace
 *
 */
public class MapNGAdapter implements IMapNG {
	/**
	 * The old-interface map we use for our state.
	 */
	private final MapView state;
	/**
	 * Constructor.
	 * @param wrapped the map to adapt to the new interface
	 */
	public MapNGAdapter(final MapView wrapped) {
		state = wrapped;
	}
	/**
	 * @param obj another map
	 * @param out the stream to write to
	 * @return whether it is a strict subset of this map.
	 */
	@Override
	public boolean isSubset(final IMapNG obj, final PrintWriter out) {
		if (dimensions().equals(obj.dimensions())) {
			boolean retval = true;
			for (Player player : obj.players()) {
				if (!state.getPlayers().contains(player)) {
					// return false;
					retval = false;
					out.println("Extra player " + player);
				}
			}
			for (Point point : obj.locations()) {
				if (!isTileSubset(obj, out, point)) {
					retval = false;
				}
			}
			return retval; // NOPMD
		} else {
			out.println("Sizes differ");
			return false;
		}
	}
	/**
	 * @param obj the map that might be a subset of us
	 * @param out the stream to write detailed results to
	 * @param point the current location
	 * @return whether that location fits the "subset" hypothesis.
	 */
	private boolean isTileSubset(final IMapNG obj, final PrintWriter out,
			final Point point) {
		boolean retval = true;
		if (!getBaseTerrain(point).equals(obj.getBaseTerrain(point))) { // NOPMD
			// return false;
			retval = false;
			out.println("Tile types differ at " + point);
		} else if (isMountainous(point) != obj.isMountainous(point)) { // NOPMD
			// return false;
			retval = false;
			out.println("Reports of mountains differ at " + point);
		} else if (!getRivers(point).equals(obj.getRivers(point))) { // NOPMD
			// FIXME: Shouldn't rely on getRivers() returning a RiverFixture ...
			// return false;
			retval = false;
			out.println("Rivers differ at " + point);
		} else if (!getForest(point).equals(obj.getForest(point))) { // NOPMD
			// return false;
			retval = false;
			out.println("Primary forests differ at " + point + ", may be representation error");
		} else if (!getGround(point).equals(obj.getGround(point))) { // NOPMD
			// return false;
			retval = false;
			out.println("Primary Ground differs at " + point + ", may be representation error");
		} else {
			for (TileFixture fix : obj.getOtherFixtures(point)) {
				if (!state.getTile(point).getContents().contains(fix) && !Tile.shouldSkip(fix)) {
					// return false;
					retval = false;
					out.println("Extra fixture " + fix + " at " + point);
				}
			}
		}
		return retval;
	}

	/**
	 * Compare to another map. This method is needed so the class can be put in a Pair.
	 *
	 * @param other the other map
	 * @return the result of the comparison
	 */
	@Override
	public int compareTo(final IMapNG other) {
		return equals(other) ? 0 : hashCode() - other.hashCode();
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
		if (dimensions().version < 2 && TileType.Mountain.equals(getBaseTerrain(location))) {
			return true; // NOPMD
		}
		for (TileFixture fix : state.getTile(location)) {
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
		return state.getTile(location).hasRiver() ? state.getTile(location)
				.getRivers() : EnumSet.noneOf(River.class);
	}

	/**
	 * Implementations should aim to have only the "main" forest here, and any
	 * "extra" forest Fixtures in the "et cetera" collection.
	 *
	 * @param location a location
	 * @return the forest (if any) at that location
	 */
	@Override
	public Forest getForest(final Point location) {
		Forest retval = null;
		for (TileFixture fix : state.getTile(location)) {
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
	 * We actually include the main Ground and Forest too; there's no easy way around that ...
	 * @param location a location
	 * @return a view of any fixtures on the map that aren't covered in the other querying methods.
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
	 * @param location a location
	 * @return the Ground at that location
	 */
	@Override
	public Ground getGround(final Point location) {
		Ground retval = null;
		for (TileFixture fix : state.getTile(location)) {
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
	 * FIXME: This should, like Set equality, ignore implementation details, but at present does not.
	 * @param obj an object
	 * @return whether it's the same as us
	 */
	@Override
	public boolean equals(final Object obj) {
		return this == obj
				|| (obj instanceof MapNGAdapter && ((MapNGAdapter) obj).state
						.equals(state));
	}
	/**
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return state.hashCode();
	}
	/**
	 * @return the map's dimensions and version
	 */
	@Override
	public MapDimensions dimensions() {
		return state.getDimensions();
	}
}
