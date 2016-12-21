package model.exploration;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import model.listeners.MovementCostListener;
import model.listeners.SelectionChangeListener;
import model.map.FixtureIterable;
import model.map.HasMutableOwner;
import model.map.HasOwner;
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
import model.map.fixtures.mobile.Unit;
import model.map.fixtures.resources.MineralVein;
import model.map.fixtures.resources.StoneDeposit;
import model.map.fixtures.terrain.Forest;
import model.map.fixtures.terrain.Mountain;
import model.map.fixtures.towns.Fortress;
import model.map.fixtures.towns.Village;
import model.misc.IDriverModel;
import model.misc.SimpleMultiMapModel;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import util.Pair;
import view.util.SystemOut;

/**
 * A model for exploration drivers.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class ExplorationModel extends SimpleMultiMapModel implements
		IExplorationModel {
	/**
	 * The list of movement-cost listeners.
	 */
	private final Collection<MovementCostListener> mcListeners = new ArrayList<>();
	/**
	 * The list of selection-change-listeners to notify when the unit moves.
	 */
	private final Collection<SelectionChangeListener> scListeners = new ArrayList<>();
	/**
	 * The currently selected unit and its location.
	 */
	private Pair<Point, Optional<IUnit>> selection =
			Pair.of(PointFactory.point(-1, -1), Optional.empty());

	/**
	 * Constructor.
	 *
	 * @param map  the starting main map
	 * @param file the name it was loaded from
	 */
	public ExplorationModel(final IMutableMapNG map, final Optional<Path> file) {
		super(map, file);
	}

	/**
	 * Copy constructor.
	 *
	 * @param model a driver model
	 */
	public ExplorationModel(final IDriverModel model) {
		super(model);
	}

	/**
	 * @param stream a sequence of members of that type
	 * @param player a player
	 * @return a list of the members of the sequence that are units owned by the player
	 */
	private static Stream<IUnit> getUnits(final Stream<@NonNull ? super Unit> stream,
										  final Player player) {
		return stream.flatMap(obj -> {
			if (obj instanceof Fortress) {
				return ((Fortress) obj).stream();
			} else {
				return Stream.of(obj);
			}
		}).filter(IUnit.class::isInstance).map(IUnit.class::cast)
												 .filter(unit -> player.equals(
														 unit.getOwner()));
	}

	/**
	 * If a unit's motion could be observed by someone allied to another
	 * (non-independent)
	 * player (which at present means the unit is moving *to* a tile two or fewer tiles
	 * away from the watcher), print a message saying so to stdout.
	 *
	 * @param map  the main map.
	 * @param unit the mover
	 * @param dest the unit's new location
	 */
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	private static void checkAllNearbyWatchers(final IMapNG map, final HasOwner unit,
											   final Point dest) {
		final MapDimensions dims = map.dimensions();
		final Collection<Point> done = new HashSet<>(25);
		for (final Point point : new SurroundingPointIterable(dest, dims)) {
			if (done.contains(point)) {
				continue;
			} else {
				done.add(point);
				checkNearbyWatcher(map.streamOtherFixtures(point), point, unit,
						dest);
			}
		}
	}

	/**
	 * If a unit's motion to a new tile could be observed by a watcher on a specified
	 * nearby tile, print a message to stdout saying so.
	 *
	 * @param fixtures a collection of fixtures in the location being considered
	 * @param point    its location
	 * @param unit     the mover
	 * @param dest     where the mover moved to
	 */
	@SuppressWarnings({"NonBooleanMethodNameMayNotStartWithQuestion", "resource"})
	private static void checkNearbyWatcher(final Stream<TileFixture> fixtures,
										   final Point point, final HasOwner unit,
										   final Point dest) {
		fixtures.filter(HasOwner.class::isInstance).map(HasOwner.class::cast)
				.filter(fix -> !fix.getOwner().isIndependent() &&
									   !fix.getOwner().equals(unit.getOwner())).forEach(
				fix -> SystemOut.SYS_OUT
							   .printf("Motion to %s could be observed by %s at %s%n",
									   dest.toString(), ((TileFixture) fix).shortDesc(),
									   point.toString()));
	}

	/**
	 * @param map   the map we're dealing with
	 * @param point the location where the unit is
	 * @param unit  a unit to remove from that location, even if it's in a fortress
	 */
	@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
	private static void removeImpl(final IMutableMapNG map, final Point point,
								   final IUnit unit) {
		boolean outside = false;
		for (final TileFixture fix : map.getOtherFixtures(point)) {
			if (unit.equals(fix)) {
				outside = true;
				break;
			} else if (fix instanceof Fortress) {
				final Fortress fortress = (Fortress) fix;
				for (final FortressMember item : fortress) {
					if (unit.equals(item)) {
						fortress.removeMember(unit);
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
	 * Ensure that a given map has at least terrain information for the specified
	 * location.
	 *
	 * @param map     the map we're operating on
	 * @param point   the location to look at
	 * @param terrain the terrain type it should be
	 */
	private static void ensureTerrain(final IMutableMapNG map,
									  final Point point, final TileType terrain) {
		if (TileType.NotVisible == map.getBaseTerrain(point)) {
			map.setBaseTerrain(point, terrain);
		}
	}

	/**
	 * @param map   a map
	 * @param point a location in that map
	 * @param fix   a fixture
	 * @return whether the map contains that fixture at that location
	 */
	private static boolean doesLocationHaveFixture(final IMapNG map, final Point point,
												   final TileFixture fix) {
		if (((fix instanceof Forest) && fix.equals(map.getForest(point)))
					|| ((fix instanceof Ground) && fix.equals(map.getGround(point)))
					|| ((fix instanceof Mountain) && map.isMountainous(point))) {
			return true;
		}
		return map.streamOtherFixtures(point).flatMap(fixture -> {
			if (fixture instanceof FixtureIterable) {
				return Stream.concat(Stream.of(fixture),
						((FixtureIterable<@NonNull ?>) fixture).stream());
			} else {
				return Stream.of(fixture);
			}
		}).anyMatch(fix::equals);
	}

	/**
	 * A "plus one" method with a configurable, low "overflow".
	 *
	 * @param num the number to increment
	 * @param max the maximum number we want to return
	 * @return either num + 1, if max or lower, or 0.
	 */
	private static int increment(final int num, final int max) {
		if (num >= max) {
			return 0;
		} else {
			return num + 1;
		}
	}

	/**
	 * A "minus one" method that "underflows" after 0 to a configurable, low value.
	 *
	 * @param num the number to decrement.
	 * @param max the number to "underflow" to.
	 * @return either num - 1, if 1 or higher, or max.
	 */
	private static int decrement(final int num, final int max) {
		if (num == 0) {
			return max;
		} else {
			return num - 1;
		}
	}

	/**
	 * @return all the players shared by all the maps
	 */
	@Override
	public List<Player> getPlayerChoices() {
		final List<Player> retval =
				getMap().streamPlayers().collect(Collectors.toList());
		streamSubordinateMaps().map(Pair::first)
				.map(map -> map.streamPlayers().collect(Collectors.toList()))
				.forEach(retval::retainAll);
		return retval;
	}

	/**
	 * @param player a player
	 * @return all that player's units in the main map
	 */
	@Override
	public List<IUnit> getUnits(final Player player) {
		return getMap().locationStream().flatMap(
				point -> getUnits(getMap().streamOtherFixtures(point), player))
					   .collect(Collectors.toList());
	}

	/**
	 * Move the currently selected unit from its current location one tile in the
	 * specified direction. Moves the unit in all maps where the unit *was* in the
	 * specified tile, copying terrain information if the tile didn't exist in a
	 * subordinate map. If movement in the specified direction is impossible, we update
	 * all subordinate maps with the terrain information showing that, then re-throw the
	 * exception; callers should deduct a minimal MP cost.
	 *
	 * @param direction the direction to move
	 * @param speed the speed the explorer is moving
	 * @return the movement cost
	 * @throws SimpleMovement.TraversalImpossibleException if movement in that direction
	 *                                                     is impossible
	 */
	@Override
	public int move(final Direction direction, final Speed speed)
			throws SimpleMovement.TraversalImpossibleException {
		final Pair<Point, Optional<IUnit>> local = selection;
		final IUnit unit = local.second().orElseThrow(
				() -> new IllegalStateException("move() called when no unit selected"));
		final IMutableMapNG map = getMap();
		final Point point = local.first();
		final Point dest = getDestination(point, direction);
		if (SimpleMovement.isLandMovementPossible(map.getBaseTerrain(dest))) {
			final int base;
			if (dest.equals(point)) {
				base = 1;
			} else {
				base = SimpleMovement.getMovementCost(map.getBaseTerrain(dest),
						map.getForest(dest) != null, map.isMountainous(dest),
						SimpleMovement.doRiversApply(direction, map.getRivers(point),
								map.getRivers(dest)),
						() -> map.streamOtherFixtures(dest));
			}
			final int retval = (int) (Math.ceil(base * speed.getMpMultiplier()) + 0.1);
			removeImpl(map, point, unit);
			map.addFixture(dest, unit);
			for (final Pair<IMutableMapNG, Optional<Path>> pair : getSubordinateMaps()) {
				final IMutableMapNG subMap = pair.first();
				if (!doesLocationHaveFixture(subMap, point, unit)) {
					continue;
				}
				ensureTerrain(subMap, dest, map.getBaseTerrain(dest));
				removeImpl(subMap, point, unit);
				subMap.addFixture(dest, unit);
			}
			selection = Pair.of(dest, Optional.of(unit));
			fireSelectionChange(point, dest);
			fireMovementCost(retval);
			checkAllNearbyWatchers(getMap(), unit, dest);
			return retval;
		} else {
			for (final Pair<IMutableMapNG, Optional<Path>> pair : getSubordinateMaps()) {
				final IMutableMapNG subMap = pair.first();
				ensureTerrain(subMap, dest, map.getBaseTerrain(dest));
			}
			fireMovementCost(1);
			throw new SimpleMovement.TraversalImpossibleException();
		}
	}

	/**
	 * Tell listeners that the selected point changed.
	 *
	 * @param old    the previous selection
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
	 * @param point     a point
	 * @param direction a direction
	 * @return the point one tile in that direction.
	 */
	@Override
	public Point getDestination(final Point point, final Direction direction) {
		final MapDimensions dims = getMapDimensions();
		switch (direction) {
		case East:
			return PointFactory.point(point.getRow(),
					increment(point.getCol(), dims.cols - 1));
		case North:
			return PointFactory.point(decrement(point.getRow(), dims.rows - 1),
					point.getCol());
		case Northeast:
			return PointFactory.point(decrement(point.getRow(), dims.rows - 1),
					increment(point.getCol(), dims.cols - 1));
		case Northwest:
			return PointFactory.point(decrement(point.getRow(), dims.rows - 1),
					decrement(point.getCol(), dims.cols - 1));
		case South:
			return PointFactory.point(increment(point.getRow(), dims.rows - 1),
					point.getCol());
		case Southeast:
			return PointFactory.point(increment(point.getRow(), dims.rows - 1),
					increment(point.getCol(), dims.cols - 1));
		case Southwest:
			return PointFactory.point(increment(point.getRow(), dims.rows - 1),
					decrement(point.getCol(), dims.cols - 1));
		case West:
			return PointFactory.point(point.getRow(),
					decrement(point.getCol(), dims.cols - 1));
		case Nowhere:
			return point;
		default:
			throw new IllegalStateException("Unhandled case");
		}
	}

	/**
	 * @param fix a fixture
	 * @return the first location found (search order is not defined) containing a
	 * fixture
	 * "equal to" the specified one. (Using it on mountains, e.g., will *not* do what you
	 * want ...)
	 */
	@Override
	public Point find(final TileFixture fix) {
		final IMapNG source = getMap();
		for (final Point point : source.locations()) {
			if (((fix instanceof Mountain) && source.isMountainous(point)) ||
						((fix instanceof Forest) &&
								 fix.equals(source.getForest(point))) ||
						((fix instanceof Ground) &&
								 fix.equals(source.getGround(point)))) {
				return point;
			}
			if (source.streamOtherFixtures(point).flatMap(item -> {
				if (item instanceof FixtureIterable) {
					return Stream.concat(Stream.of(item),
							((FixtureIterable<@NonNull ?>) item).stream());
				} else {
					return Stream.of(item);
				}
			}).anyMatch(fix::equals)) {
				return point;
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
		return selection.second().orElse(null);
	}

	/**
	 * @param unit the new selected unit
	 */
	@Override
	public void selectUnit(@Nullable final IUnit unit) {
		final Point oldLoc = selection.first();
		final Point loc;
		if (unit == null) {
			loc = PointFactory.point(-1, -1);
		} else {
			loc = find(unit);
		}
		selection = Pair.of(loc, Optional.ofNullable(unit));
		fireSelectionChange(oldLoc, loc);
	}

	/**
	 * @return the location of the currently selected unit.
	 */
	@Override
	public Point getSelectedUnitLocation() {
		return selection.first();
	}

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
	 * @param listener the listener to add
	 */
	@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
	@Override
	public void addMovementCostListener(final MovementCostListener listener) {
		mcListeners.add(listener);
	}

	/**
	 * @param listener the listener to remove
	 */
	@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
	@Override
	public void removeMovementCostListener(final MovementCostListener listener) {
		mcListeners.remove(listener);
	}

	/**
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "ExplorationModel";
	}

	/**
	 * If there is a currently selected unit, make any independent villages at its
	 * location change to be owned by the owner of the currently selected unit. This
	 * costs
	 * MP.
	 */
	@Override
	public void swearVillages() {
		final Pair<Point, Optional<IUnit>> localSelection = selection;
		final Point currPoint = localSelection.first();
		final Optional<IUnit> temp = localSelection.second();
		if (temp.isPresent()) {
			final IUnit mover = temp.get();
			final Player owner = mover.getOwner();
			streamAllMaps().map(Pair::first)
					.flatMap(map -> map.streamOtherFixtures(currPoint))
					.filter(Village.class::isInstance).map(HasMutableOwner.class::cast)
					.forEach(fix -> fix.setOwner(owner));
			fireMovementCost(5);
		}
	}
	/**
	 * @param fix a TileFixture
	 * @return whether it is "diggable"
	 */
	private static boolean isDiggable(final TileFixture fix) {
		return (fix instanceof Ground) || (fix instanceof StoneDeposit) ||
					   (fix instanceof MineralVein);
	}
	/**
	 * In StoneDeposit and MineralVein, equals() is false if DCs are not equal, so this
	 * method uses copy() to get a clone with that zeroed out.
	 * @param firstFix one fixture
	 * @param secondFix another fixture
	 * @return whether they're "equal enough" for the purposes of updating a map after
	 * digging
	 */
	private static boolean areDiggablesEqual(final TileFixture firstFix, final TileFixture secondFix) {

		if (firstFix.equals(secondFix)) {
			return true;
		} else if ((firstFix instanceof StoneDeposit) ||
						   (firstFix instanceof MineralVein)) {
			return firstFix.copy(true).equals(secondFix.copy(true));
		} else {
			return false;
		}
	}
	/**
	 * If there is a currently selected unit, change one Ground, StoneDeposit, or
	 * MineralVein at the location of that unit from unexposed to exposed (and discover
	 * it). This costs MP.
	 */
	@Override
	public void dig() {
		final Point currPoint = selection.first();
		if (currPoint.getRow() >= 0) {
			final IMutableMapNG mainMap = getMap();
			@Nullable final Ground ground = mainMap.getGround(currPoint);
			final List<TileFixture> diggables = Stream.concat(Stream.of(ground),
					mainMap.streamOtherFixtures(currPoint))
														.filter(ExplorationModel::isDiggable)
														.collect(Collectors.toList());
			if (diggables.isEmpty()) {
				return;
			}
			int i = 0;
			boolean first = true;
			while (first || ((i < 4) && !(diggables.get(0) instanceof Ground))) {
				Collections.shuffle(diggables);
				first = false;
				i++;
			}
			final TileFixture oldFix = diggables.get(0);
			final TileFixture newFix = oldFix.copy(false);
			if (newFix instanceof Ground) {
				((Ground) newFix).setExposed(true);
			} else if (newFix instanceof MineralVein) {
				((MineralVein) newFix).setExposed(true);
			}
			final BiConsumer<IMutableMapNG, Boolean> addToMap = (map, condition) -> {
				final Ground locGround = map.getGround(currPoint);
				if ((locGround == null) || locGround.equals(ground)) {
					map.setGround(currPoint,
							(Ground) newFix.copy(condition.booleanValue()));
					return;
				} else if (map.streamOtherFixtures(currPoint)
								   .anyMatch(fix -> areDiggablesEqual(fix, oldFix))) {
					map.removeFixture(currPoint, oldFix);
				}
				map.addFixture(currPoint, newFix.copy(condition.booleanValue()));
			};
			// TODO: When Ground gets unique IDs, check it instead of using ==
			//noinspection ObjectEquality
			if (ground == oldFix) {
				streamAllMaps().map(Pair::first)
						.forEach(map -> addToMap.accept(map, Boolean.FALSE));
			} else {
				boolean subsequent = false;
				for (final Pair<IMutableMapNG, Optional<Path>> pair : getAllMaps()) {
					addToMap.accept(pair.first(), Boolean.valueOf(subsequent));
					subsequent = true;
				}
			}
			fireMovementCost(4);
		}
	}
}
