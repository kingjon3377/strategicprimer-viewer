package model.exploration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import model.listeners.MovementCostListener;
import model.listeners.SelectionChangeListener;
import model.map.FixtureIterable;
import model.map.IFixture;
import model.map.IMap;
import model.map.IMutableTile;
import model.map.IMutableTileCollection;
import model.map.ITile;
import model.map.ITileCollection;
import model.map.MapDimensions;
import model.map.MapView;
import model.map.Player;
import model.map.Point;
import model.map.PointFactory;
import model.map.Tile;
import model.map.TileFixture;
import model.map.TileType;
import model.map.fixtures.mobile.IUnit;
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
	 * The currently selected unit.
	 */
	@Nullable
	private IUnit selUnit = null;
	/**
	 * Its location.
	 */
	private Point selUnitLoc = PointFactory.point(-1, -1);
	/**
	 * The list of movement-cost listeners.
	 */
	private final List<MovementCostListener> mcListeners = new ArrayList<>();

	/**
	 * The list of selection-change-listeners to notify when the unit moves.
	 */
	private final List<SelectionChangeListener> scListeners = new ArrayList<>();

	/**
	 * Constructor.
	 *
	 * @param map the starting main map
	 * @param file the name it was loaded from
	 */
	public ExplorationModel(final MapView map, final File file) {
		setMap(map, file);
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
		for (final Pair<IMap, File> pair : getSubordinateMaps()) {
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
	public List<IUnit> getUnits(final Player player) {
		final List<IUnit> retval = new ArrayList<>();
		final ITileCollection tiles = getMap().getTiles();
		for (final Point point : tiles) {
			if (point != null) {
				final ITile tile = tiles.getTile(point);
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
	private static List<IUnit> getUnits(final Iterable<? super Unit> iter,
			final Player player) {
		final List<IUnit> retval = new ArrayList<>();
		for (final Object obj : iter) {
			if (obj instanceof IUnit && ((IUnit) obj).getOwner().equals(player)) {
				retval.add((IUnit) obj);
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
		final IUnit unit = selUnit;
		if (unit == null) {
			throw new IllegalStateException(
					"move() called when no unit selected");
		}
		final Point point = selUnitLoc;
		final Point dest = getDestination(point, direction);
		final ITile sourceTile = getMap().getTile(point);
		// ESCA-JAVA0177:
		final ITile destTile = getMap().getTile(dest);
		if (sourceTile instanceof IMutableTile && destTile instanceof IMutableTile
				&& SimpleMovement.isLandMovementPossible(destTile)) {
			final int retval; // NOPMD
			if (dest.equals(point)) {
				retval = 1;
			} else {
				retval = SimpleMovement.getMovementCost(destTile);
			}
			removeImpl((IMutableTile) sourceTile, unit);
			((IMutableTile) destTile).addFixture(unit);
			for (final Pair<IMap, File> pair : getSubordinateMaps()) {
				final ITileCollection mapTilesPre = pair.first().getTiles();
				if (!(mapTilesPre instanceof IMutableTileCollection)) {
					throw new IllegalStateException("Immutable tile collection");
				}
				final IMutableTileCollection mapTiles =
						(IMutableTileCollection) mapTilesPre;
				final IMutableTile stile = mapTiles.getTile(point);
				if (!tileHasFixture(stile, unit)) {
					continue;
				}
				ensureTerrain(mapTiles, dest, destTile.getTerrain());
				final IMutableTile dtile = mapTiles.getTile(dest);
				removeImpl(stile, unit);
				dtile.addFixture(unit);
			}
			selUnitLoc = dest;
			fireSelectionChange(point, dest);
			fireMovementCost(retval);
			return retval;
		} else {
			for (final Pair<IMap, File> pair : getSubordinateMaps()) {
				final ITileCollection tiles = pair.first().getTiles();
				if (!(tiles instanceof IMutableTileCollection)) {
					throw new IllegalStateException("Immutable collection of tiles");
				}
				ensureTerrain((IMutableTileCollection) tiles, dest,
						destTile.getTerrain());
			}
			fireMovementCost(1);
			throw new TraversalImpossibleException();
		}
	}
	/**
	 * @param sourceTile a tile
	 * @param unit a unit to remove from that tile, even if it's in a fortress
	 */
	private static void removeImpl(final IMutableTile sourceTile, final IUnit unit) {
		for (final TileFixture fix : sourceTile) {
			if (unit.equals(fix)) {
				sourceTile.removeFixture(unit);
				return; // NOPMD
			} else if (fix instanceof Fortress) {
				for (final IUnit item : (Fortress) fix) {
					if (unit.equals(item)) {
						((Fortress) fix).removeUnit(unit);
						return;
					}
				}
			}
		}
	}

	/**
	 * Tell listeners that the selected point changed.
	 * @param old the previous selection
	 * @param newSel the new selection
	 */
	private void fireSelectionChange(final Point old, final Point newSel) {
		for (final SelectionChangeListener list : scListeners) {
			list.selectedPointChanged(old, newSel);
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
	private static void ensureTerrain(final IMutableTileCollection tiles,
			final Point point, final TileType terrain) {
		if (!tiles.hasTile(point)) {
			tiles.addTile(point, new Tile(terrain));
		} else if (TileType.NotVisible.equals(tiles.getTile(point).getTerrain())) {
			tiles.getTile(point).setTerrain(terrain);
		}
	}

	/**
	 * @param tile a tile
	 * @param fix a fixture
	 * @return whether the tile contains it
	 */
	private static boolean tileHasFixture(final ITile tile, final TileFixture fix) {
		for (final TileFixture fixture : tile) {
			if (fixture.equals(fix)) {
				return true; // NOPMD
			} else if (fixture instanceof FixtureIterable) {
				for (final IFixture inner : (FixtureIterable<?>) fixture) {
					if (fix.equals(inner)) {
						return true; // NOPMD
					}
				}
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
	public Point getDestination(final Point point, final Direction direction) {
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
					increment(point.col, dims.cols - 1));
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
		if (num >= max - 1) {
			return 0; // NOPMD
		} else {
			return num + 1;
		}
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
		if (num == 0) {
			return max; // NOPMD
		} else {
			return num - 1;
		}
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
				} else if (item instanceof FixtureIterable) {
					for (final IFixture inner : (FixtureIterable<?>) item) {
						if (fix.equals(inner)) {
							return point; // NOPMD
						}
					}
				}
			}
		}
		return PointFactory.point(-1, -1);
	}


	/**
	 * @return the currently selected unit
	 */
	@Override
	@Nullable
	public IUnit getSelectedUnit() {
		return selUnit;
	}

	/**
	 * @param unit the new selected unit
	 */
	public void selectUnit(final IUnit unit) {
		final Point oldLoc = selUnitLoc;
		selUnit = unit;
		selUnitLoc = find(unit);
		fireSelectionChange(oldLoc, selUnitLoc);
	}

	/**
	 * @return the location of the currently selected unit.
	 */
	@Override
	public Point getSelectedUnitLocation() {
		return selUnitLoc;
	}

	/**
	 * @param list a listener to add
	 */
	@Override
	public final void addSelectionChangeListener(
			final SelectionChangeListener list) {
		scListeners.add(list);
	}

	/**
	 * @param list a listener to remove
	 */
	@Override
	public final void removeSelectionChangeListener(
			final SelectionChangeListener list) {
		scListeners.remove(list);
	}

	/**
	 * @param listener the listener to add
	 */
	@Override
	public final void addMovementCostListener(final MovementCostListener listener) {
		mcListeners.add(listener);
	}

	/**
	 * @param listener the listener to remove
	 */
	@Override
	public final void removeMovementCostListener(
			final MovementCostListener listener) {
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
