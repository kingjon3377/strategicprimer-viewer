package model.exploration;

import java.util.ArrayList;
import java.util.List;

import model.listeners.MovementCostListener;
import model.listeners.SelectionChangeListener;
import model.map.IMap;
import model.map.MapDimensions;
import model.map.MapView;
import model.map.Player;
import model.map.Point;
import model.map.PointFactory;
import model.map.Tile;
import model.map.TileCollection;
import model.map.TileFixture;
import model.map.TileType;
import model.map.fixtures.mobile.SimpleMovement;
import model.map.fixtures.mobile.SimpleMovement.TraversalImpossibleException;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.towns.Fortress;
import model.misc.AbstractMultiMapModel;

import org.eclipse.jdt.annotation.Nullable;

import util.Pair;

/**
 * A model for exploration drivers.
 *
 * @author Jonathan Lovelace
 *
 */
public class ExplorationModel extends AbstractMultiMapModel implements
		IExplorationModel {
	/**
	 * Constructor.
	 *
	 * @param map the starting main map
	 * @param filename the name it was loaded from
	 */
	public ExplorationModel(final MapView map, final String filename) {
		setMap(map, filename);
	}

	/**
	 * @return all the players shared by all the maps
	 */
	@Override
	public List<Player> getPlayerChoices() {
		final List<Player> retval = new ArrayList<>();
		for (final Player player : getMap().getPlayers()) {
			retval.add(player);
		}
		final List<Player> temp = new ArrayList<>();
		for (final Pair<IMap, String> pair : getSubordinateMaps()) {
			final IMap map = pair.first();
			temp.clear();
			for (final Player player : map.getPlayers()) {
				temp.add(player);
			}
			retval.retainAll(temp);
		}
		return retval;
	}

	/**
	 * @param player a player
	 * @return all that player's units in the main map
	 */
	@Override
	public List<Unit> getUnits(final Player player) {
		final List<Unit> retval = new ArrayList<>();
		final TileCollection tiles = getMap().getTiles();
		for (final Point point : tiles) {
			if (point != null) {
				final Tile tile = tiles.getTile(point);
				retval.addAll(getUnits(tile, player));
			}
		}
		return retval;
	}

	/**
	 * @param iter a sequence of members of that type
	 * @param player a player
	 * @return a list of the members of the sequence that are units owned by the
	 *         player
	 */
	private static List<Unit> getUnits(final Iterable<? super Unit> iter,
			final Player player) {
		final List<Unit> retval = new ArrayList<>();
		for (final Object obj : iter) {
			if (obj instanceof Unit && ((Unit) obj).getOwner().equals(player)) {
				retval.add((Unit) obj);
			} else if (obj instanceof Fortress) {
				retval.addAll(getUnits((Fortress) obj, player));
			}
		}
		return retval;
	}

	/**
	 * Move the currently selected unit from its current location one tile in
	 * the specified direction. Moves the unit in all maps where the unit *was*
	 * in the specified tile, copying terrain information if the tile didn't
	 * exist in a subordinate map. If movement in the specified direction is
	 * impossible, we update all subordinate maps with the terrain information
	 * showing that, then re-throw the exception; callers should deduct a
	 * minimal MP cost.
	 *
	 * @param direction the direction to move
	 * @return the movement cost
	 * @throws TraversalImpossibleException if movement in that direction is
	 *         impossible
	 */
	@Override
	public int move(final Direction direction)
			throws TraversalImpossibleException {
		final Unit unit = selUnit;
		if (unit == null) {
			throw new IllegalStateException(
					"move() called when no unit selected");
		}
		final Point point = selUnitLoc;
		final Point dest = getDestination(point, direction);
		// ESCA-JAVA0177:
		final Tile destTile = getMap().getTile(dest);
		if (SimpleMovement.isLandMovementPossible(destTile)) {
			final int retval = dest.equals(point) ? 1 : SimpleMovement
					.getMovementCost(destTile);
			getMap().getTile(point).removeFixture(unit);
			destTile.addFixture(unit);
			for (final Pair<IMap, String> pair : getSubordinateMaps()) {
				final TileCollection mapTiles = pair.first().getTiles();
				final Tile stile = mapTiles.getTile(point);
				if (!tileHasFixture(stile, unit)) {
					continue;
				}
				ensureTerrain(mapTiles, dest, destTile.getTerrain());
				stile.removeFixture(unit);
				mapTiles.getTile(dest).addFixture(unit);
			}
			selUnitLoc = dest;
			for (final SelectionChangeListener list : scListeners) {
				list.selectedPointChanged(point, dest);
			}
			fireMovementCost(retval);
			return retval;
		} else {
			for (final Pair<IMap, String> pair : getSubordinateMaps()) {
				ensureTerrain(pair.first().getTiles(), dest,
						destTile.getTerrain());
			}
			fireMovementCost(1);
			throw new TraversalImpossibleException();
		}
	}

	/**
	 * Tell listeners of a movement cost.
	 *
	 * @param cost how much the move cost
	 */
	private void fireMovementCost(final int cost) {
		for (final MovementCostListener list : mcListeners) {
			list.deduct(cost);
		}
	}

	/**
	 * Ensure that a given collection of tiles has at least terrain information
	 * for the specified location.
	 *
	 * @param tiles the collection we're operating on
	 * @param point the location to look at
	 * @param terrain the terrain type it should be
	 */
	private static void ensureTerrain(final TileCollection tiles,
			final Point point, final TileType terrain) {
		if (!tiles.hasTile(point)) {
			tiles.addTile(point, new Tile(terrain));
		}
	}

	/**
	 * @param tile a tile
	 * @param fix a fixture
	 * @return whether the tile contains it
	 */
	private static boolean tileHasFixture(final Tile tile, final TileFixture fix) {
		for (final TileFixture fixture : tile) {
			if (fixture.equals(fix)) {
				return true; // NOPMD
			}
		}
		return false;
	}

	/**
	 * @param point a point
	 * @param direction a direction
	 * @return the point one tile in that direction.
	 */
	@Override
	public Point getDestination(final Point point, final Direction direction) { // NOPMD
		final MapDimensions dims = getMapDimensions();
		switch (direction) {
		case East:
			return PointFactory.point(point.row, // NOPMD
					increment(point.col, dims.cols - 1));
		case North:
			return PointFactory.point(decrement(point.row, dims.rows - 1), // NOPMD
					point.col);
		case Northeast:
			return PointFactory.point(decrement(point.row, dims.rows - 1), // NOPMD
					increment(point.col, dims.rows - 1));
		case Northwest:
			return PointFactory.point(decrement(point.row, dims.rows - 1), // NOPMD
					decrement(point.col, dims.cols - 1));
		case South:
			return PointFactory.point(increment(point.row, dims.rows - 1), // NOPMD
					point.col);
		case Southeast:
			return PointFactory.point(increment(point.row, dims.rows - 1), // NOPMD
					increment(point.col, dims.cols - 1));
		case Southwest:
			return PointFactory.point(increment(point.row, dims.rows - 1), // NOPMD
					decrement(point.col, dims.cols - 1));
		case West:
			return PointFactory.point(point.row, // NOPMD
					decrement(point.col, dims.cols - 1));
		case Nowhere:
			return point; // NOPMD
		default:
			throw new IllegalStateException("Unhandled case");
		}
	}

	/**
	 * A "plus one" method with a configurable, low "overflow".
	 *
	 * @param num the number to increment
	 * @param max the maximum number we want to return
	 * @return either num + 1, if max or lower, or 0.
	 */
	public static int increment(final int num, final int max) {
		return num >= max - 1 ? 0 : num + 1;
	}

	/**
	 * A "minus one" method that "underflows" after 0 to a configurable, low
	 * value.
	 *
	 * @param num the number to decrement.
	 * @param max the number to "underflow" to.
	 * @return either num - 1, if 1 or higher, or max.
	 */
	public static int decrement(final int num, final int max) {
		return num == 0 ? max : num - 1;
	}

	/**
	 * @param fix a fixture
	 * @return the first location found (search order is not defined) containing
	 *         a fixture "equal to" the specified one. (Using it on mountains,
	 *         e.g., will *not* do what you want ...)
	 */
	@Override
	public Point find(final TileFixture fix) {
		final IMap source = getMap();
		for (final Point point : source.getTiles()) {
			if (point == null) {
				continue;
			}
			for (final TileFixture item : source.getTile(point)) {
				if (fix.equals(item)) {
					return point; // NOPMD
				}
			}
		}
		return PointFactory.point(-1, -1);
	}

	/**
	 * The currently selected unit.
	 */
	@Nullable
	private Unit selUnit = null;
	/**
	 * Its location.
	 */
	private Point selUnitLoc = PointFactory.point(-1, -1);

	/**
	 * @return the currently selected unit
	 */
	@Override
	@Nullable
	public Unit getSelectedUnit() {
		return selUnit;
	}

	/**
	 * @param unit the new selected unit
	 */
	public void selectUnit(final Unit unit) {
		final Point oldLoc = selUnitLoc;
		selUnit = unit;
		selUnitLoc = find(unit);
		for (final SelectionChangeListener list : scListeners) {
			list.selectedPointChanged(oldLoc, selUnitLoc);
		}
	}

	/**
	 * @return the location of the currently selected unit.
	 */
	@Override
	public Point getSelectedUnitLocation() {
		return selUnitLoc;
	}

	/**
	 * The list of selection-change-listeners to notify when the unit moves.
	 */
	private final List<SelectionChangeListener> scListeners = new ArrayList<>();

	/**
	 * @param list a listener to add
	 */
	@Override
	public void addSelectionChangeListener(final SelectionChangeListener list) {
		scListeners.add(list);
	}

	/**
	 * @param list a listener to remove
	 */
	@Override
	public void removeSelectionChangeListener(final SelectionChangeListener list) {
		scListeners.remove(list);
	}

	/**
	 * The list of movement-cost listeners.
	 */
	private final List<MovementCostListener> mcListeners = new ArrayList<>();

	/**
	 * @param listener the listener to add
	 */
	@Override
	public void addMovementCostListener(final MovementCostListener listener) {
		mcListeners.add(listener);
	}

	/**
	 * @param listener the listener to remove
	 */
	@Override
	public void removeMovementCostListener(final MovementCostListener listener) {
		mcListeners.remove(listener);
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "ExplorationModel";
	}
}
