package model.exploration;

import java.util.ArrayList;
import java.util.List;

import model.map.IMap;
import model.map.MapDimensions;
import model.map.MapView;
import model.map.Player;
import model.map.Point;
import model.map.PointFactory;
import model.map.Tile;
import model.map.TileCollection;
import model.map.TileFixture;
import model.map.fixtures.mobile.SimpleMovement;
import model.map.fixtures.mobile.SimpleMovement.TraversalImpossibleException;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.towns.Fortress;
import model.misc.AbstractMultiMapModel;
import util.Pair;
/**
 * A model for exploration drivers.
 * @author Jonathan Lovelace
 *
 */
public class ExplorationModel extends AbstractMultiMapModel implements
		IExplorationModel {
	/**
	 * Constructor.
	 * @param map the starting main map
	 * @param filename the name it was loaded from
	 */
	public ExplorationModel(final MapView map, final String filename) {
		setMap(map, filename);
	}
	/**
	 * Version UID for serialization.
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * @return all the players shared by all the maps
	 */
	@Override
	public List<Player> getPlayerChoices() {
		final List<Player> retval = new ArrayList<Player>();
		for (Player player : getMap().getPlayers()) {
			retval.add(player);
		}
		final List<Player> temp = new ArrayList<Player>();
		for (Pair<IMap, String> pair : getSubordinateMaps()) {
			final IMap map = pair.first();
			temp.clear();
			for (Player player : map.getPlayers()) {
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
		final List<Unit> retval = new ArrayList<Unit>();
		final TileCollection tiles = getMap().getTiles();
		for (final Point point : tiles) {
			final Tile tile = tiles.getTile(point);
			retval.addAll(getUnits(tile, player));
		}
		return retval;
	}
	/**
	 * @param iter a sequence of members of that type
	 * @param player a player
	 * @return a list of the members of the sequence that are units owned by the player
	 */
	private static List<Unit> getUnits(final Iterable<? super Unit> iter, final Player player) {
		final List<Unit> retval = new ArrayList<Unit>();
		for (Object obj : iter) {
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
			throw new IllegalStateException("move() called when no unit selected");
		}
		final Point point = selUnitLoc;
		final Point dest = getDestination(point, direction);
		// ESCA-JAVA0177:
		final int retval; //NOPMD
		final Tile destTile = getMap().getTile(dest);
		try {
			retval = SimpleMovement.getMovementCost(destTile);
		} catch (final TraversalImpossibleException except) {
			for (Pair<IMap, String> pair : getSubordinateMaps()) {
				final IMap map = pair.first();
				if (map.getTile(dest).isEmpty()) {
					map.getTiles().addTile(dest,
							new Tile(destTile.getTerrain())); // NOPMD
				}
			}
			throw except;
		}
		getMap().getTile(point).removeFixture(unit);
		destTile.addFixture(unit);
		for (Pair<IMap, String> pair : getSubordinateMaps()) {
			final IMap map = pair.first();
			final Tile stile = map.getTile(point);
			boolean hasUnit = false;
			for (final TileFixture fix : stile) {
				if (fix.equals(unit)) {
					hasUnit = true;
					break;
				}
			}
			if (!hasUnit) {
				continue;
			}
			Tile dtile = map.getTile(dest);
			if (dtile.isEmpty()) {
				dtile = new Tile(destTile.getTerrain()); // NOPMD
				map.getTiles().addTile(dest, dtile);
			}
			stile.removeFixture(unit);
			dtile.addFixture(unit);
		}
		selUnitLoc = dest;
		return retval;
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
		default:
			throw new IllegalStateException("Unhandled case");
		}
	}
	/**
	 * A "plus one" method with a configurable, low "overflow".
	 * @param num the number to increment
	 * @param max the maximum number we want to return
	 * @return either num + 1, if max or lower, or 0.
	 */
	public static int increment(final int num, final int max) {
		return num >= max - 1 ? 0 : num + 1;
	}
	/**
	 * A "minus one" method that "underflows" after 0 to a configurable, low value.
	 * @param num the number to decrement.
	 * @param max the number to "underflow" to.
	 * @return either num - 1, if 1 or higher, or max.
	 */
	public static int decrement(final int num, final int max) {
		return num == 0 ? max : num - 1;
	}
	/**
	 * @param fix a fixture
	 * @return the first location found (search order is not defined) containing a
	 *         fixture "equal to" the specified one. (Using it on mountains,
	 *         e.g., will *not* do what you want ...)
	 */
	@Override
	public Point find(final TileFixture fix) {
		final IMap source = getMap();
		for (Point point : source.getTiles()) {
			for (TileFixture item : source.getTile(point)) {
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
	private Unit selUnit = null;
	/**
	 * Its location.
	 */
	private Point selUnitLoc = PointFactory.point(-1, -1);
	/**
	 * @return the currently selected unit
	 */
	@Override
	public Unit getSelectedUnit() {
		return selUnit;
	}
	/**
	 * @param unit the new selected unit
	 */
	public void selectUnit(final Unit unit) {
		final Unit old = selUnit;
		selUnit = unit;
		selUnitLoc = find(unit);
		firePropertyChange("selected-unit", old, unit);
	}
	/**
	 * @return the location of the currently selected unit.
	 */
	@Override
	public Point getSelectedUnitLocation() {
		return selUnitLoc;
	}
}
