package model.exploration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import model.listeners.MovementCostListener;
import model.listeners.SelectionChangeListener;
import model.map.FixtureIterable;
import model.map.HasOwner;
import model.map.IFixture;
import model.map.IMapNG;
import model.map.IMutableMapNG;
import model.map.MapDimensions;
import model.map.Player;
import model.map.Point;
import model.map.PointFactory;
import model.map.TileFixture;
import model.map.TileType;
import model.map.fixtures.FortressMember;
import model.map.fixtures.Ground;
import model.map.fixtures.mobile.IUnit;
import model.map.fixtures.mobile.SimpleMovement;
import model.map.fixtures.mobile.SimpleMovement.TraversalImpossibleException;
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.terrain.Forest;
import model.map.fixtures.terrain.Mountain;
import model.map.fixtures.towns.Fortress;
import model.misc.IDriverModel;
import model.misc.SimpleMultiMapModel;
import util.Pair;
import view.util.SystemOut;

/**
 * A model for exploration drivers.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2013-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of version 3 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 *
 */
public final class ExplorationModel extends SimpleMultiMapModel implements
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
	public ExplorationModel(final IMutableMapNG map, final File file) {
		super(map, file);
	}
	/**
	 * Copy constructor.
	 * @param model a driver model
	 */
	public ExplorationModel(final IDriverModel model) {
		super(model);
	}
	/**
	 * @return all the players shared by all the maps
	 */
	@Override
	public List<Player> getPlayerChoices() {
		final List<Player> retval = new ArrayList<>();
		for (final Player player : getMap().players()) {
			retval.add(player);
		}
		final List<Player> temp = new ArrayList<>();
		for (final Pair<IMutableMapNG, File> pair : getSubordinateMaps()) {
			final IMapNG map = pair.first();
			temp.clear();
			for (final Player player : map.players()) {
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
		for (final Point point : getMap().locations()) {
			retval.addAll(getUnits(getMap().getOtherFixtures(point), player));
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
		IMutableMapNG map = getMap();
		final Point point = selUnitLoc;
		final Point dest = getDestination(point, direction);
		// ESCA-JAVA0177:
		if (SimpleMovement.isLandMovementPossible(map.getBaseTerrain(dest))) {
			final int retval; // NOPMD
			if (dest.equals(point)) {
				retval = 1;
			} else {
				retval =
						SimpleMovement.getMovementCost(
								map.getBaseTerrain(dest),
								map.getForest(dest) != null,
								map.isMountainous(dest), map.getRivers(dest)
								.iterator().hasNext(),
								map.getOtherFixtures(dest));
			}
			removeImpl(map, point, unit);
			map.addFixture(dest, unit);
			for (final Pair<IMutableMapNG, File> pair : getSubordinateMaps()) {
				final IMutableMapNG subMap = pair.first();
				if (!locationHasFixture(subMap, point, unit)) {
					continue;
				}
				ensureTerrain(subMap, dest, map.getBaseTerrain(dest));
				removeImpl(subMap, point, unit);
				subMap.addFixture(dest, unit);
			}
			selUnitLoc = dest;
			fireSelectionChange(point, dest);
			fireMovementCost(retval);
			checkNearbyWatchers(getMap(), unit, dest);
			return retval;
		} else {
			for (final Pair<IMutableMapNG, File> pair : getSubordinateMaps()) {
				final IMutableMapNG subMap = pair.first();
				ensureTerrain(subMap, dest, map.getBaseTerrain(dest));
			}
			fireMovementCost(1);
			throw new TraversalImpossibleException();
		}
	}

	/**
	 * If a unit's motion could be observed by someone allied to another
	 * (non-independent) player (which at present means the unit is moving *to*
	 * a tile two or fewer tiles away from the watcher), print a message saying
	 * so to stdout.
	 *
	 * @param map
	 *            the main map.
	 * @param unit
	 *            the mover
	 * @param dest
	 *            the unit's new location
	 */
	private static void checkNearbyWatchers(final IMapNG map, final IUnit unit,
			final Point dest) {
		MapDimensions dims = map.dimensions();
		final Set<Point> done = new HashSet<>(25);
		for (final Point point : new SurroundingPointIterable(dest, dims)) {
			if (done.contains(point)) {
				continue;
			} else {
				done.add(point);
				checkNearbyWatcher(map.getOtherFixtures(point), point, unit,
						dest);
			}
		}
	}

	/**
	 * If a unit's motion to a new tile could be observed by a watcher on a
	 * specified nearby tile, print a message to stdout saying so.
	 * @param fixtures a collection of fixtures in the location being considered
	 * @param point its location
	 * @param unit the mover
	 * @param dest where the mover moved to
	 */
	private static void checkNearbyWatcher(final Iterable<TileFixture> fixtures,
			final Point point, final IUnit unit, final Point dest) {
		for (final TileFixture fix : fixtures) {
			if (fix instanceof HasOwner
					&& !((HasOwner) fix).getOwner().isIndependent()
					&& !((HasOwner) fix).getOwner().equals(unit.getOwner())) {
				SystemOut.SYS_OUT.printf(
						"Unit's motion to %s could be observed by %s at %s%n",
						dest.toString(), fix.shortDesc(), point.toString());
			}
		}
	}
	/**
	 * @param map the map we're dealing with
	 * @param point the location where the unit is
	 * @param unit a unit to remove from that location, even if it's in a fortress
	 */
	private static void removeImpl(final IMutableMapNG map, final Point point,
			final IUnit unit) {
		boolean outside = false;
		for (final TileFixture fix : map.getOtherFixtures(point)) {
			if (unit.equals(fix)) {
				outside = true;
				break;
			} else if (fix instanceof Fortress) {
				for (final FortressMember item : (Fortress) fix) {
					if (unit.equals(item)) {
						((Fortress) fix).removeMember(unit);
						return;
					}
				}
			}
		}
		if (outside) {
			map.removeFixture(point, unit);
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
	 * Ensure that a given map has at least terrain information
	 * for the specified location.
	 *
	 * @param map the map we're operating on
	 * @param point the location to look at
	 * @param terrain the terrain type it should be
	 */
	private static void ensureTerrain(final IMutableMapNG map,
			final Point point, final TileType terrain) {
		if (TileType.NotVisible.equals(map.getBaseTerrain(point))) {
			map.setBaseTerrain(point, terrain);
		}
	}

	/**
	 * @param map a map
	 * @param point a location in that map
	 * @param fix a fixture
	 * @return whether the map contains that fixture at that location
	 */
	private static boolean locationHasFixture(final IMapNG map, final Point point,
			final TileFixture fix) {
		if ((fix instanceof Forest && fix.equals(map.getForest(point)))
				|| (fix instanceof Ground && fix.equals(map.getGround(point)))
				|| (fix instanceof Mountain && map.isMountainous(point))) {
			return true;
		}
		for (final TileFixture fixture : map.getOtherFixtures(point)) {
			if (fixture.equals(fix)) {
				return true; // NOPMD
			} else if (fixture instanceof FixtureIterable) {
				for (final IFixture inner : (FixtureIterable<@NonNull ?>) fixture) {
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
		final IMapNG source = getMap();
		for (final Point point : source.locations()) {
			if ((fix instanceof Mountain && source.isMountainous(point))
					|| (fix instanceof Forest && fix.equals(source
							.getForest(point)))
					|| (fix instanceof Ground && fix.equals(source
							.getGround(point)))) {
				return point;
			}
			for (final TileFixture item : source.getOtherFixtures(point)) {
				if (fix.equals(item)) {
					return point; // NOPMD
				} else if (item instanceof FixtureIterable) {
					for (final IFixture inner : (FixtureIterable<@NonNull ?>) item) {
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
	public void selectUnit(@Nullable final IUnit unit) {
		final Point oldLoc = selUnitLoc;
		selUnit = unit;
		if (unit == null) {
			selUnitLoc = PointFactory.point(-1, -1);
		} else {
			selUnitLoc = find(unit);
		}
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
