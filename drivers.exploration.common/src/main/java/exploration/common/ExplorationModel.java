package exploration.common;

import legacy.map.HasName;
import legacy.map.fixtures.resources.ExposureStatus;
import legacy.map.fixtures.resources.MineralVein;
import legacy.map.fixtures.resources.StoneDeposit;
import lovelace.util.LovelaceLogger;
import static lovelace.util.MatchingValue.matchingValues;
import org.javatuples.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.Collections;
import java.util.stream.StreamSupport;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Optional;
import java.util.Objects;
import java.util.HashSet;
import java.util.EnumSet;

import drivers.common.SimpleMultiMapModel;
import drivers.common.IDriverModel;
import drivers.common.SelectionChangeListener;
import legacy.map.fixtures.FixtureIterable;
import legacy.map.fixtures.FortressMember;
import legacy.map.FakeFixture;
import legacy.map.IFixture;
import legacy.map.Player;
import legacy.map.HasKind;
import legacy.map.HasMutableKind;
import legacy.map.HasMutableName;
import legacy.map.HasMutableOwner;
import legacy.map.HasOwner;
import legacy.map.MapDimensions;
import legacy.map.Point;
import legacy.map.River;
import legacy.map.TileFixture;
import legacy.map.TileType;
import legacy.map.IMutableLegacyMap;
import legacy.map.Direction;
import legacy.map.ILegacyMap;
import legacy.map.fixtures.Ground;
import legacy.map.fixtures.MineralFixture;
import legacy.map.fixtures.UnitMember;
import legacy.map.fixtures.mobile.Animal;
import legacy.map.fixtures.mobile.AnimalTracks;
import legacy.map.fixtures.mobile.IMutableUnit;
import legacy.map.fixtures.mobile.IUnit;
import legacy.map.fixtures.mobile.ProxyUnit;
import legacy.map.fixtures.mobile.MobileFixture;
import legacy.map.fixtures.resources.CacheFixture;
import legacy.map.fixtures.resources.Grove;
import legacy.map.fixtures.resources.Meadow;
import legacy.map.fixtures.resources.Mine;
import legacy.map.fixtures.terrain.Forest;
import legacy.map.fixtures.towns.IMutableFortress;
import legacy.map.fixtures.towns.Village;
import legacy.map.fixtures.towns.IFortress;

import static java.util.function.Predicate.not;
import static lovelace.util.MatchingValue.matchingValue;

// TODO: Make sure all methods are still used; at least one driver now uses a different model interface.

/**
 * A model for exploration apps.
 */
public class ExplorationModel extends SimpleMultiMapModel implements IExplorationModel {
	/**
	 * A fixture is "diggable" if it is a {@link MineralFixture} or a {@link Mine}.
	 */
	private static boolean isDiggable(final TileFixture fixture) {
		return fixture instanceof MineralVein || fixture instanceof Mine;
	}

	private static Stream<IFixture> flattenIncluding(final IFixture fixture) {
		if (fixture instanceof final FixtureIterable<?> iter) {
			return Stream.concat(Stream.of(fixture), iter.stream());
		} else {
			return Stream.of(fixture);
		}
	}

	/**
	 * If the item in the entry is a {@link IFortress fortress}, return a
	 * stream of its contents paired with its location; otherwise, return a
	 * {@link Collections#singleton singleton} of the argument.
	 */
	private static Iterable<Pair<Point, IFixture>> flattenEntries(final Pair<Point, IFixture> entry) {
		if (entry.getValue1() instanceof final IFortress fort) {
			return fort.stream().map(IFixture.class::cast)
					.map(each -> Pair.with(entry.getValue0(), each))
					.collect(Collectors.toList());
		} else {
			return Collections.singleton(entry);
		}
	}

	/**
	 * Check whether two fixtures are "equal enough" for the purposes of
	 * updating a map after digging. This method is needed because equals()
	 * in {@link StoneDeposit} and {@link
	 * MineralVein} compares DCs.
	 */
	private static boolean areDiggablesEqual(final IFixture firstFixture, final IFixture secondFixture) {
		return Objects.equals(firstFixture, secondFixture) ||
				Objects.equals(firstFixture.copy(IFixture.CopyBehavior.ZERO),
						secondFixture.copy(IFixture.CopyBehavior.ZERO));
	}

	/**
	 * If a unit's motion could be observed by someone allied to another
	 * (non-independent) player (which at present means the unit is moving
	 * <em>to</em> a tile two or fewer tiles away from the watcher), print a message saying so to stdout.
	 */
	private static void checkAllNearbyWatchers(final ILegacyMap map, final IUnit unit, final Point dest) {
		final MapDimensions dimensions = map.getDimensions();
		final String description;
		if (unit.owner().isIndependent()) {
			description = "%s (ID #%d)".formatted(unit.getShortDescription(), unit.getId());
		} else {
			description = unit.getShortDescription();
		}
		new SurroundingPointIterable(dest, dimensions).stream()
				.flatMap(point -> map.getFixtures(point).stream().filter(HasOwner.class::isInstance)
						.map(HasOwner.class::cast).filter(owned -> !owned.owner().isIndependent())
						.filter(not(matchingValue(unit, HasOwner::owner)))
						.map(TileFixture.class::cast).map(fix -> Pair.with(point, fix.getShortDescription())))
				.forEach(pair ->
						System.out.printf("Motion of %s to %s could be observed by %s at %s%n", description, dest,
								pair.getValue1(), pair.getValue0()));
	}

	/**
	 * Remove a unit from a location, even if it's in a fortress.
	 */
	private static void removeImpl(final IMutableLegacyMap map, final Point point, final TileFixture unit) {
		boolean outside = false;
		for (final TileFixture fixture : map.getFixtures(point)) {
			if (Objects.equals(unit, fixture)) {
				outside = true;
				break;
			} else if (fixture instanceof final IMutableFortress fort) {
				final Optional<FortressMember> item = fort.stream()
						.filter(Predicate.isEqual(unit)).findAny();
				if (item.isPresent()) {
					((IMutableFortress) fixture).removeMember(item.get());
					return;
				}
			}
		}
		if (outside) {
			map.removeFixture(point, unit);
		}
	}

	/**
	 * Ensure that a given map has at least terrain information for the specified location.
	 */
	private static void ensureTerrain(final ILegacyMap mainMap, final IMutableLegacyMap map, final Point point) {
		if (Objects.isNull(map.getBaseTerrain(point))) {
			map.setBaseTerrain(point, mainMap.getBaseTerrain(point));
		}
		if (mainMap.isMountainous(point)) {
			map.setMountainous(point, true);
		}
		map.addRivers(point, mainMap.getRivers(point).toArray(River[]::new));
		// TODO: Should we copy roads here?
	}

	/**
	 * Whether the given fixture is contained in the given stream.
	 */
	private static boolean doesStreamContainFixture(final Iterable<? extends IFixture> stream, final IFixture fixture) {
		for (final IFixture member : stream) {
			if (Objects.equals(member, fixture)) {
				return true;
			} else if (member instanceof final FixtureIterable<?> iter &&
					doesStreamContainFixture(iter, fixture)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Specialization of {@link #doesStreamContainFixture} for units.
	 */
	private static boolean doesStreamContainUnit(final Iterable<? extends IFixture> stream, final IUnit unit) {
		for (final IFixture member : stream) {
			switch (member) {
				case final IUnit memUnit when memUnit.getId() == unit.getId() &&
						memUnit.owner().equals(unit.owner()) && memUnit.getKind().equals(unit.getKind()) &&
						memUnit.getName().equals(unit.getName()) -> {
					return true;
				}
				case final FixtureIterable<?> iter when doesStreamContainUnit(iter, unit) -> {
					return true;
				}
				default -> {
				}
			}
		}
		return false;
	}

	/**
	 * Specialization of {@link #doesStreamContainFixture} for fortresses.
	 */
	private static boolean doesStreamContainFortress(final Iterable<? extends IFixture> stream, final IFortress fort) {
		for (final IFixture member : stream) {
			switch (member) {
				case final IFortress memFort when memFort.getId() == fort.getId() &&
						memFort.owner().equals(fort.owner()) && memFort.getName().equals(fort.getName()) -> {
					return true;
				}
				case final FixtureIterable<?> iter when doesStreamContainFortress(iter, fort) -> {
					return true;
				}
				default -> {
				}
			}
		}
		return false;
	}

	/**
	 * Whether the given fixture is at the given location in the given map.
	 */
	private static boolean doesLocationHaveFixture(final ILegacyMap map, final Point point, final TileFixture fixture) {
		return switch (fixture) {
			case final IUnit unit -> doesStreamContainUnit(map.getFixtures(point), unit);
			case final IFortress fort -> doesStreamContainFortress(map.getFixtures(point), fort);
			default -> doesStreamContainFixture(map.getFixtures(point), fixture);
		};
	}

	/**
	 * A "plus one" method with a configurable, low "overflow".
	 *
	 * @param number The number to increment
	 * @param max    The maximum number we want to return
	 */
	private static int increment(final int number, final int max) {
		return (number >= max) ? 0 : (number + 1);
	}

	/**
	 * A "minus one" method that "underflows" after 0 to a configurable, low value.
	 *
	 * @param number The number to decrement
	 * @param max    The number to "underflow" to
	 */
	private static int decrement(final int number, final int max) {
		return (number <= 0) ? max : (number - 1);
	}

	/**
	 * The intersection of two sets; here so it can be passed as a method
	 * reference rather than a lambda in {@link #getPlayerChoices}.
	 */
	private static <T> Set<T> intersection(final Set<T> one, final Collection<T> two) {
		final Set<T> retval = new HashSet<>(one);
		retval.retainAll(two);
		return retval;
	}

	/**
	 * If the fixture is a {@link IFortress fortress}, return a stream of its contents;
	 * otherwise, return a stream containing only it. This is intended to
	 * be used in {@link Stream#flatMap}.
	 */
	private static Stream<IFixture> unflattenNonFortresses(final IFixture fixture) {
		if (fixture instanceof final IFortress fort) {
			return fort.stream().map(IFixture.class::cast);
		} else {
			return Stream.of(fixture);
		}
	}

	private final Set<UnitMember> dismissedMembers = new HashSet<>();
	private final Collection<MovementCostListener> mcListeners = new ArrayList<>();
	private final Collection<SelectionChangeListener> scListeners = new ArrayList<>();

	/**
	 * The currently selected unit and its location.
	 */
	private Pair<Point, @Nullable IUnit> selection = Pair.with(Point.INVALID_POINT, null);

	public ExplorationModel(final IMutableLegacyMap map) {
		super(map);
	}

	public ExplorationModel(final IDriverModel model) {
		super(model);
	}

	/**
	 * All the players shared by all the maps.
	 * TODO: Move to IMultiMapModel?
	 */
	@Override
	public final Collection<Player> getPlayerChoices() {
		// TODO: Port this stream-based algorithm to Java
//		return allMaps.map(IMapNG.players).map(set).fold(set(map.players))(intersection);
		final Set<Player> retval = StreamSupport.stream(getMap().getPlayers().spliterator(), true)
				.collect(Collectors.toSet());
		for (final ILegacyMap map : getSubordinateMaps()) {
			retval.retainAll(StreamSupport.stream(map.getPlayers().spliterator(), true)
					.collect(Collectors.toSet()));
		}
		return Collections.unmodifiableSet(retval);
	}

	/**
	 * Collect all the units in the main map belonging to the specified player.
	 */
	@Override
	public final List<IUnit> getUnits(final Player player) {
		return getMap().streamAllFixtures()
				.flatMap(ExplorationModel::unflattenNonFortresses)
				.filter(IUnit.class::isInstance)
				.map(IUnit.class::cast)
				.filter(u -> u.owner().equals(player))
				.collect(Collectors.toList());
	}

	/**
	 * Tell listeners that the selected point changed.
	 */
	private void fireSelectionChange(final Point old, final Point newSelection) {
		for (final SelectionChangeListener listener : scListeners) {
			LovelaceLogger.debug("Notifying a listener of selected-point change");
			listener.selectedPointChanged(old, newSelection);
		}
	}

	/**
	 * Tell listeners that the selected unit changed.
	 */
	private void fireSelectedUnitChange(final @Nullable IUnit old, final @Nullable IUnit newSelection) {
		for (final SelectionChangeListener listener : scListeners) {
			LovelaceLogger.debug("Notifying a listener of selected-unit change");
			listener.selectedUnitChanged(old, newSelection);
		}
	}

	/**
	 * Tell listeners to deduct a cost from their movement-point totals.
	 */
	private void fireMovementCost(final Number cost) {
		for (final MovementCostListener listener : mcListeners) {
			listener.deduct(cost);
		}
	}

	/**
	 * Get the location one tile in the given direction from the given point.
	 */
	@Override
	public final Point getDestination(final Point point, final Direction direction) {
		final MapDimensions dims = getMapDimensions();
		final int maxColumn = dims.columns() - 1;
		final int maxRow = dims.rows() - 1;
		final int row = point.row();
		final int column = point.column();
		return switch (direction) {
			case East -> new Point(row, increment(column, maxColumn));
			case North -> new Point(decrement(row, maxRow), column);
			case Northeast -> new Point(decrement(row, maxRow), increment(column, maxColumn));
			case Northwest -> new Point(decrement(row, maxRow), decrement(column, maxColumn));
			case South -> new Point(increment(row, maxRow), column);
			case Southeast -> new Point(increment(row, maxRow), increment(column, maxColumn));
			case Southwest -> new Point(increment(row, maxRow), decrement(column, maxColumn));
			case West -> new Point(row, decrement(column, maxColumn));
			case Nowhere -> point;
		};
	}

	private void fixMovedUnits(final Point base) {
		final BiFunction<ILegacyMap, TileFixture, Iterable<Pair<Point, TileFixture>>> localFind =
				(mapParam, target) -> mapParam.streamLocations()
						.flatMap(l -> mapParam.getFixtures(l).stream().map(f -> Pair.with(l, f)))
						.filter(p -> target.equals(p.getValue1())) // TODO: Filter should come earlier
						.collect(Collectors.toList());
		// TODO: Unit vision range
		final Iterable<Point> points = new SurroundingPointIterable(base, getMap().getDimensions(), 2);
		for (final IMutableLegacyMap submap : getRestrictedSubordinateMaps()) {
			// TODO: Can we limit use of mutability to a narrower critical section?
			for (final Point point : points) {
				for (final TileFixture fixture : submap.getFixtures(point)) {
					if (fixture instanceof MobileFixture) {
						for (final Pair<Point, TileFixture> pair :
								localFind.apply(submap, fixture)) {
							final Point innerPoint = pair.getValue0();
							final TileFixture match = pair.getValue1();
							if (!innerPoint.equals(point) &&
									!getMap().getFixtures(innerPoint)
											.contains(match)) {
								submap.removeFixture(innerPoint, match);
								submap.setModified(true);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Move the currently selected unit from its current location one tile
	 * in the specified direction. Moves the unit in all maps where the
	 * unit <em>was</em> in that tile, copying terrain information if the
	 * tile didn't exist in a subordinate map. If movement in the specified
	 * direction is impossible, we update all subordinate maps with the
	 * terrain information showing that, then re-throw the exception;
	 * callers should deduct a minimal MP cost (though we notify listeners
	 * of that cost). We return the cost of the move in MP, which we also
	 * tell listeners about.
	 *
	 * @param direction The direction to move
	 * @param speed     How hastily the explorer is moving
	 * @throws TraversalImpossibleException if movement in the specified direction is impossible
	 */
	@Override
	public final Number move(final Direction direction, final Speed speed) throws TraversalImpossibleException {
		final Pair<Point, @Nullable IUnit> local = selection;
		final Point point = local.getValue0();
		final IUnit unit = local.getValue1();
		if (Objects.isNull(unit)) {
			throw new IllegalStateException("No mover selected");
		}
		final Point dest = getDestination(point, direction);
		final TileType terrain = getMap().getBaseTerrain(dest);
		final TileType startingTerrain = getMap().getBaseTerrain(point);
		if (Objects.nonNull(terrain) && Objects.nonNull(startingTerrain) &&
				((SimpleMovementModel.landMovementPossible(terrain) &&
						TileType.Ocean != startingTerrain) ||
						(TileType.Ocean == startingTerrain &&
								terrain == TileType.Ocean))) {
			final int base;
			if (dest.equals(point)) {
				base = 1;
			} else {
				final Iterable<TileFixture> fixtures = getMap().getFixtures(dest);
				base = SimpleMovementModel.movementCost(getMap().getBaseTerrain(dest),
						getMap().getFixtures(dest).stream()
								.anyMatch(Forest.class::isInstance),
						getMap().isMountainous(dest),
						SimpleMovementModel.riversSpeedTravel(direction,
								getMap().getRivers(point),
								getMap().getRivers(dest)), fixtures);
			}
			final double retval = base * speed.getMpMultiplier();
			removeImpl(getRestrictedMap(), point, unit);
			getRestrictedMap().addFixture(dest, unit);
			setMapModified(true);
			for (final IMutableLegacyMap subMap : getRestrictedSubordinateMaps()) {
				if (doesLocationHaveFixture(subMap, point, unit)) {
					ensureTerrain(getMap(), subMap, dest);
					removeImpl(subMap, point, unit);
					subMap.addFixture(dest, unit);
					subMap.setModified(true);
				}
			}
			selection = Pair.with(dest, unit);
			fireSelectionChange(point, dest);
			fireMovementCost(retval);
			checkAllNearbyWatchers(getMap(), unit, dest);
			fixMovedUnits(dest);
			return retval;
		} else {
			if (Objects.isNull(getMap().getBaseTerrain(point))) {
				LovelaceLogger.debug("Started outside explored territory in main map");
			} else if (Objects.isNull(getMap().getBaseTerrain(dest))) {
				LovelaceLogger.debug("Main map doesn't have terrain for destination");
			} else {
				if (SimpleMovementModel.landMovementPossible(terrain) &&
						TileType.Ocean == startingTerrain) {
					LovelaceLogger.debug("Starting in ocean, trying to get to %s", Objects.requireNonNull(terrain));
				} else if (TileType.Ocean == startingTerrain && TileType.Ocean != terrain) {
					LovelaceLogger.debug("Land movement not possible from ocean to %s",
							Objects.requireNonNullElse(terrain, "unexplored"));
				} else if (TileType.Ocean != startingTerrain &&
						TileType.Ocean == terrain) {
					LovelaceLogger.debug("Starting in %s, trying to get to ocean",
							Objects.requireNonNullElse(startingTerrain, "unexplored"));
				} else {
					LovelaceLogger.debug("Unknown reason for movement-impossible condition");
				}
			}
			for (final IMutableLegacyMap subMap : getRestrictedSubordinateMaps()) {
				ensureTerrain(getMap(), subMap, dest);
				subMap.setModified(true);
			}
			fireMovementCost(1);
			throw new TraversalImpossibleException();
		}
	}

	/**
	 * Search the main map for the given fixture. Returns the first
	 * location found (search order is not defined) containing a fixture
	 * "equal to" the specified one.
	 */
	@Override
	public final Point find(final TileFixture fixture) {
		for (final Point point : getMap().getLocations()) {
			if (doesLocationHaveFixture(getMap(), point, fixture)) {
				return point;
			}
		}
		return Point.INVALID_POINT;
	}

	private boolean mapsAgreeOnLocation(final TileFixture unit) {
		if (unit instanceof final ProxyUnit proxy) {
			if (proxy.getProxied().isEmpty()) {
				return false;
			} else {
				return mapsAgreeOnLocation(proxy.getProxied().iterator().next());
			}
		}
		final Point mainLoc = find(unit);
		if (!mainLoc.isValid()) {
			return false;
		}
		for (final ILegacyMap subMap : getSubordinateMaps()) {
			for (final Point point : subMap.getLocations()) {
				if (doesLocationHaveFixture(subMap, point, unit)) {
					if (point.equals(mainLoc)) {
						break;
					} else {
						return false;
					}
				}
			}
		}
		return true;
	}

	/**
	 * The currently selected unit.
	 */
	@Override
	public final @Nullable IUnit getSelectedUnit() {
		return selection.getValue1();
	}

	/**
	 * Unselect the currently selected unit.
	 */
	@Override
	public final void clearSelectedUnit() {
		LovelaceLogger.debug("Unsetting currently-selected-unit property");
		selection = Pair.with(Point.INVALID_POINT, null);
	}

	/**
	 * Select the given unit.
	 */
	@Override
	public final void setSelectedUnit(final IUnit selectedUnit) {
		final Point oldLoc = selection.getValue0();
		final IUnit oldSelection = selection.getValue1();
		if (!mapsAgreeOnLocation(selectedUnit)) {
			LovelaceLogger.warning("Maps containing that unit don't all agree on its location");
		}
		LovelaceLogger.debug("Setting a newly selected unit");
		final Point loc = find(selectedUnit);
		if (loc.isValid()) {
			LovelaceLogger.debug("Found at %s", loc);
		} else {
			LovelaceLogger.debug("Not found using our 'find' method");
		}
		selection = Pair.with(loc, selectedUnit);
		fireSelectionChange(oldLoc, loc);
		fireSelectedUnitChange(oldSelection, selectedUnit);
	}

	/**
	 * The location of the currently selected unit.
	 */
	@Override
	public final Point getSelectedUnitLocation() {
		return selection.getValue0();
	}

	/**
	 * Add a selection-change listener.
	 */
	@Override
	public final void addSelectionChangeListener(final SelectionChangeListener listener) {
		scListeners.add(listener);
	}

	/**
	 * Remove a selection-change listener.
	 */
	@Override
	public final void removeSelectionChangeListener(final SelectionChangeListener listener) {
		scListeners.remove(listener);
	}

	/**
	 * Add a movement-cost listener.
	 */
	@Override
	public final void addMovementCostListener(final MovementCostListener listener) {
		mcListeners.add(listener);
	}

	/**
	 * Remove a movement-cost listener.
	 */
	@Override
	public final void removeMovementCostListener(final MovementCostListener listener) {
		mcListeners.remove(listener);
	}

	/**
	 * If there is a currently selected unit, make any independent villages
	 * at its location change to be owned by the owner of the currently
	 * selected unit. This costs MP.
	 */
	@Override
	public final void swearVillages() {
		final Pair<Point, @Nullable IUnit> localSelection = selection;
		final Point currentPoint = localSelection.getValue0();
		final IUnit unit = localSelection.getValue1();
		if (Objects.nonNull(unit)) {
			final Player owner = unit.owner();
			final List<Village> villages = streamAllMaps().flatMap(m -> m.getFixtures(currentPoint).stream())
					.filter(Village.class::isInstance)
					.map(Village.class::cast)
					.filter(v -> v.owner().isIndependent()).toList();
			if (!villages.isEmpty()) {
				IFixture.CopyBehavior subordinate = IFixture.CopyBehavior.KEEP;
				for (final Village village : villages) {
					village.setOwner(owner);
					for (final IMutableLegacyMap subMap : getRestrictedAllMaps()) {
						subMap.addFixture(currentPoint, village.copy(subordinate));
						subordinate = IFixture.CopyBehavior.ZERO;
						subMap.setModified(true);
					}
				}
				final ILegacyMap mainMap = getMap();
				final Iterable<Point> surroundingPoints =
						new SurroundingPointIterable(currentPoint, getMapDimensions(), 1);
				final Predicate<Object> isForest = Forest.class::isInstance;
				final Function<Object, Forest> forestCast = Forest.class::cast;
				for (final Point point : surroundingPoints) {
					for (final IMutableLegacyMap subMap : getRestrictedSubordinateMaps()) {
						ensureTerrain(mainMap, subMap, point);
						final Optional<Forest> subForest = subMap.getFixtures(point)
								.stream().filter(isForest)
								.map(forestCast).findFirst();
						final Optional<Forest> forest = getMap().getFixtures(point).stream()
								.filter(isForest)
								.map(forestCast).findFirst();
						if (forest.isPresent() && subForest.isEmpty()) {
							subMap.addFixture(point, forest.get());
						}
					}
				}
				final Iterable<Pair<Point, TileFixture>> surroundingFixtures =
						StreamSupport.stream(surroundingPoints.spliterator(), true)
								.flatMap(point -> mainMap.getFixtures(point).stream()
										.map(fixture -> Pair.with(point, fixture)))
								.collect(Collectors.toList());
				final Optional<Pair<Point, TileFixture>> vegetation =
						StreamSupport.stream(surroundingFixtures.spliterator(), false)
								.filter(p -> p.getValue1() instanceof Meadow ||
										p.getValue1() instanceof Grove).findFirst();
				final Optional<Pair<Point, TileFixture>> animal =
						StreamSupport.stream(surroundingFixtures.spliterator(), false)
								.filter(p -> p.getValue1() instanceof Animal).findFirst();
				for (final IMutableLegacyMap subMap : getRestrictedSubordinateMaps()) {
					vegetation.ifPresent(objects -> subMap.addFixture(objects.getValue0(),
							objects.getValue1().copy(IFixture.CopyBehavior.ZERO)));
					animal.ifPresent(objects -> subMap.addFixture(objects.getValue0(),
							objects.getValue1().copy(IFixture.CopyBehavior.ZERO)));
				}
			}
			fireMovementCost(5);
		}
	}

	/**
	 * If there is a currently selected unit, change one {@link Ground},
	 * {@link common.map.fixtures.resources::StoneDeposit}, or {@link
	 * MineralVein} at the location of that unit from unexposed to exposed
	 * (and discover it). This costs MP.
	 */
	@Override
	public final void dig() {
		final Point currentPoint = selection.getValue0();
		if (currentPoint.isValid()) {
			final IMutableLegacyMap mainMap = getRestrictedMap();
			final List<TileFixture> diggables = mainMap.getFixtures(currentPoint)
					.stream().filter(ExplorationModel::isDiggable).collect(Collectors.toList());
			if (diggables.isEmpty()) {
				return;
			}
			int i = 0;
			boolean first = true;
			while (first || (i < 4 && !(diggables.getFirst() instanceof Ground))) {
				Collections.shuffle(diggables);
				first = false;
				i++;
			}
			final TileFixture oldFixture = diggables.getFirst();
			final TileFixture newFixture = oldFixture.copy(IFixture.CopyBehavior.KEEP);
			// TODO: Extract an interface for 'exposed' so we only have to do one test
			switch (newFixture) {
				case final Ground g -> g.setExposure(ExposureStatus.EXPOSED);
				case final MineralVein mv -> mv.setExposure(ExposureStatus.EXPOSED);
				default -> {
				}
			}
			final BiConsumer<IMutableLegacyMap, IFixture.CopyBehavior> addToMap = (map, condition) -> {
				if (map.getFixtures(currentPoint).stream()
						.anyMatch(f -> areDiggablesEqual(oldFixture, f))) {
					map.replace(currentPoint, oldFixture, newFixture.copy(condition));
				} else {
					map.addFixture(currentPoint, newFixture.copy(condition));
				}
			};
			IFixture.CopyBehavior subsequent = IFixture.CopyBehavior.KEEP;
			for (final IMutableLegacyMap subMap : getRestrictedAllMaps()) {
				addToMap.accept(subMap, subsequent);
				subsequent = IFixture.CopyBehavior.ZERO;
				subMap.setModified(true);
			}
			fireMovementCost(4);
		}
	}

	/**
	 * Add the given unit at the given location.
	 */
	@Override
	// TODO: If more than one map, return a proxy for the units; otherwise, return the unit
	public final void addUnitAtLocation(final IUnit unit, final Point location) {
		for (final IMutableLegacyMap indivMap : getRestrictedAllMaps()) {
			indivMap.addFixture(location, unit); // FIXME: Check for existing matching unit there already
			indivMap.setModified(true);
		}
	}

	/**
	 * Copy the given fixture from the main map to subordinate maps. (It is
	 * found in the main map by ID, rather than trusting the input.) If it
	 * is a cache, remove it from the main map. If "zero" is {@link IFixture.CopyBehavior#ZERO} remove
	 * sensitive information from the copies.
	 */
	@Override
	public final boolean copyToSubMaps(final Point location, final TileFixture fixture,
	                                   final IFixture.CopyBehavior zero) {
		final @Nullable TileFixture matching;
		boolean retval = false;
		switch (fixture) {
			case final FakeFixture fake -> { // must be a block, won't compile if inlined
				return false; // Skip it! It'll corrupt the output XML!
			}
			case final AnimalTracks tracks -> matching = fixture;
			default -> matching = getMap().getFixtures(location).stream()
					.filter(f -> f.getId() == fixture.getId()).findAny().orElse(null);
		}
		if (Objects.isNull(matching)) {
			LovelaceLogger.warning("Skipping because not in the main map");
		} else {
			for (final IMutableLegacyMap subMap : getRestrictedSubordinateMaps()) {
				retval = subMap.addFixture(location, matching.copy(zero)) || retval;
				// We do *not* use the return value because it returns false if an existing fixture was *replaced*
				subMap.setModified(true);
			}

			if (matching instanceof CacheFixture) {
				// TODO: make removeFixture() return Boolean, true if anything was removed
				getRestrictedMap().removeFixture(location, matching);
				retval = true;
				// TODO: Here and elsewhere, don't bother to setModified() if an earlier mutator method already does so
				getRestrictedMap().setModified(true);
			}
		}
		return retval;
	}

	/**
	 * Copy any terrain, mountain, rivers, and roads from the main map to subordinate maps.
	 */
	@Override
	public final void copyTerrainToSubMaps(final Point location) {
		for (final IMutableLegacyMap subMap : getRestrictedSubordinateMaps()) {
			if (getMap().isMountainous(location) && !subMap.isMountainous(location)) {
				subMap.setMountainous(location, true);
				subMap.setModified(true);
			}
			final TileType terrain = getMap().getBaseTerrain(location);
			if (Objects.nonNull(terrain) &&
					terrain != subMap.getBaseTerrain(location)) {
				subMap.setBaseTerrain(location, terrain);
				subMap.setModified(true);
			}
			if (!getMap().getRivers(location).containsAll(subMap.getRivers(location))) {
				subMap.addRivers(location, getMap().getRivers(location).toArray(River[]::new));
				subMap.setModified(true);
			}
			final Map<Direction, Integer> subRoads = subMap.getRoads(location);
			if (!getMap().getRoads(location).isEmpty()) { // TODO: Just omit this check?
				for (final Map.Entry<Direction, Integer> entry :
						getMap().getRoads(location).entrySet()) {
					if (subRoads.getOrDefault(entry.getKey(), -1) < entry.getValue()) {
						subMap.setRoadLevel(location, entry.getKey(),
								entry.getValue());
						subMap.setModified(true);
					}
				}
			}
		}
	}

	/**
	 * Set sub-map terrain at the given location to the given type.
	 *
	 * @deprecated Can we redesign the fixture list to not need this for the exploration GUI?
	 */
	@Deprecated
	@Override
	public final void setSubMapTerrain(final Point location, final @Nullable TileType terrain) {
		for (final IMutableLegacyMap subMap : getRestrictedSubordinateMaps()) {
			subMap.setBaseTerrain(location, terrain);
			subMap.setModified(true);
		}
	}

	/**
	 * Copy the given rivers to sub-maps, if they are present in the main map.
	 */
	@Override
	public final void copyRiversToSubMaps(final Point location, final River... rivers) {
		final Collection<River> actualRivers = EnumSet.copyOf(Stream.of(rivers)
				.collect(Collectors.toList()));
		actualRivers.retainAll(getMap().getRivers(location));
		for (final IMutableLegacyMap subMap : getRestrictedSubordinateMaps()) {
			// TODO: Make addRivers() return Boolean if this was a change, and only set modified flag in that case
			subMap.addRivers(location, actualRivers.toArray(River[]::new));
			subMap.setModified(true);
		}
	}

	/**
	 * Remove the given rivers from sub-maps.
	 *
	 * @deprecated Can we redesign the fixture list to not need this for the exploration GUI?
	 */
	@Deprecated
	@Override
	public final void removeRiversFromSubMaps(final Point location, final River... rivers) {
		for (final IMutableLegacyMap subMap : getRestrictedSubordinateMaps()) {
			// TODO: Make removeRivers() return Boolean if this was a change, and only set modified flag in that case
			subMap.removeRivers(location, rivers);
			subMap.setModified(true);
		}
	}

	/**
	 * Remove the given fixture from sub-maps.
	 *
	 * @deprecated Can we redesign the fixture list to not need this for the exploration GUI?
	 */
	@Deprecated
	@Override
	public final void removeFixtureFromSubMaps(final Point location, final TileFixture fixture) {
		for (final IMutableLegacyMap subMap : getRestrictedSubordinateMaps()) {
			// TODO: Make removeFixture() return Boolean if this was a change, and only set modified flag in that case
			subMap.removeFixture(location, fixture);
			subMap.setModified(true);
		}
	}

	/**
	 * Set whether sub-maps have a mountain at the given location.
	 *
	 * @deprecated Can we redesign the fixture list to not need this for the exploration GUI?
	 */
	@Deprecated
	@Override
	public final void setMountainousInSubMap(final Point location, final boolean mountainous) {
		for (final IMutableLegacyMap subMap : getRestrictedSubordinateMaps()) {
			if (subMap.isMountainous(location) != mountainous) {
				subMap.setMountainous(location, mountainous);
				subMap.setModified(true);
			}
		}
	}

	/**
	 * Move a unit-member from one unit to another.
	 */
	@Override
	public final void moveMember(final UnitMember member, final IUnit old, final IUnit newOwner) {
		final Predicate<Object> isMutableUnit = IMutableUnit.class::isInstance;
		final Function<Object, IMutableUnit> mutableUnitCast = IMutableUnit.class::cast;
		final Predicate<IUnit> matchingOldUnit = matchingValues(old, IUnit::owner, IUnit::getKind, IUnit::getName,
				IUnit::getId);
		final Predicate<IUnit> matchingNewUnit = matchingValues(newOwner, IUnit::owner, IUnit::getKind, IUnit::getName,
				IUnit::getId);
		// TODO: equals() isn't ideal for finding a matching member ...
		final Function<IUnit, Optional<UnitMember>> searchUnit =
				u -> u.stream().filter(Predicate.isEqual(member)).findAny();
		for (final IMutableLegacyMap map : getRestrictedSubordinateMaps()) {
			final Optional<IMutableUnit> matchingOld = map.streamAllFixtures()
					.flatMap(ExplorationModel::unflattenNonFortresses)
					.filter(isMutableUnit).map(mutableUnitCast)
					.filter(matchingOldUnit)
					.findAny();
			final Optional<UnitMember> matchingMember =
					matchingOld.flatMap(searchUnit);
			final Optional<IMutableUnit> matchingNew = map.streamAllFixtures()
					.flatMap(ExplorationModel::unflattenNonFortresses)
					.filter(isMutableUnit).map(mutableUnitCast)
					.filter(matchingNewUnit)
					.findAny();
			if (matchingOld.isPresent() && matchingMember.isPresent() &&
					matchingNew.isPresent()) {
				matchingOld.get().removeMember(matchingMember.get());
				matchingNew.get().addMember(matchingMember.get());
				map.setModified(true);
			}
		}
	}

	private static Predicate<Pair<Point, TileFixture>> unitMatching(final IUnit unit) {
		return (pair) -> {
			final Point location = pair.getValue0();
			final IFixture fixture = pair.getValue1();
			return fixture instanceof final IUnit matching && fixture.getId() == unit.getId() &&
					matching.owner().equals(unit.owner());
		};
	}

	/**
	 * Remove the given unit from the map. It must be empty, and may be
	 * required to be owned by the current player. The operation will also
	 * fail if "matching" units differ in name or kind from the provided
	 * unit.  Returns true if the preconditions were met and the unit was
	 * removed, and false otherwise. To make an edge case explicit, if
	 * there are no matching units in any map the method returns false.
	 *
	 * FIXME: Should also support removing a unit that's in a fortress
	 */
	@Override
	public final boolean removeUnit(final IUnit unit) {
		LovelaceLogger.debug("In ExplorationModel.removeUnit()");
		final Collection<Pair<IMutableLegacyMap, Pair<Point, IUnit>>> delenda = new ArrayList<>();
		for (final IMutableLegacyMap map : getRestrictedAllMaps()) {
			final Optional<Pair<Point, TileFixture>> pair = map.streamLocations()
					.flatMap(l -> map.getFixtures(l).stream().map(f -> Pair.with(l, f)))
					.filter(unitMatching(unit)).findAny();
			if (pair.isPresent()) {
				LovelaceLogger.debug("Map has matching unit");
				final IUnit fixture = (IUnit) pair.get().getValue1();
				if (fixture.getKind().equals(unit.getKind()) &&
						fixture.getName().equals(unit.getName()) &&
						!fixture.iterator().hasNext()) {
					LovelaceLogger.debug("Matching unit meets preconditions");
					delenda.add(Pair.with(map,
							Pair.with(pair.get().getValue0(), fixture)));
				} else {
					LovelaceLogger.warning(
							"Matching unit in %s fails preconditions for removal",
							Optional.ofNullable(map.getFilename())
									.map(Object::toString)
									.filter(f -> !f.isEmpty())
									.orElse("an unsaved map"));
					return false;
				}
			}
		}
		if (delenda.isEmpty()) {
			LovelaceLogger.debug("No matching units");
			return false;
		}
		for (final Pair<IMutableLegacyMap, Pair<Point, IUnit>> entry : delenda) {
			final IMutableLegacyMap map = entry.getValue0();
			final Point location = entry.getValue1().getValue0();
			final TileFixture fixture = entry.getValue1().getValue1();
			if (map.getFixtures(location).contains(fixture)) {
				map.removeFixture(location, fixture);
			} else {
				boolean any = false;
				for (final IMutableFortress fort : map.getFixtures(location).stream()
						.filter(IMutableFortress.class::isInstance)
						.map(IMutableFortress.class::cast).toList()) {
					if (fort.stream().anyMatch(fixture::equals)) {
						fort.removeMember((FortressMember) fixture);
						any = true;
						break;
					}
				}
				if (!any) {
					LovelaceLogger.warning("Failed to find unit to remove that we thought might be in a fortress");
				}
			}
		}
		LovelaceLogger.debug("Finished removing matching unit(s) from map(s)");
		return true;
	}

	@Override
	public final void addUnitMember(final IUnit unit, final UnitMember member) {
		final Predicate<Object> isMutableUnit = IMutableUnit.class::isInstance;
		final Function<Object, IMutableUnit> mutableUnitCast = IMutableUnit.class::cast;
		final Predicate<IUnit> isMatchingUnit = matchingValues(unit, IUnit::owner, IUnit::getName, IUnit::getKind,
				IUnit::getId);
		for (final IMutableLegacyMap map : getRestrictedAllMaps()) {
			final Optional<IMutableUnit> matching = map.streamAllFixtures()
					.flatMap(ExplorationModel::unflattenNonFortresses)
					.filter(isMutableUnit)
					.map(mutableUnitCast)
					.filter(isMatchingUnit)
					.findAny();
			if (matching.isPresent()) {
				matching.get().addMember(member.copy(IFixture.CopyBehavior.KEEP));
				map.setModified(true);
			}
		}
	}

	private boolean matchingPlayer(final HasOwner fixture) {
		return Optional.ofNullable(selection.getValue1()).map(IUnit::owner)
				.filter(fixture.owner()::equals).isPresent();
	}

	@Override
	public final boolean renameItem(final HasName item, final String newName) {
		boolean any = false;
		final Predicate<Object> isUnit = IUnit.class::isInstance;
		final Predicate<Object> hasMutableName = HasMutableName.class::isInstance;
		final Function<Object, IUnit> unitCast = IUnit.class::cast;
		final Function<Object, HasMutableName> hmnCast = HasMutableName.class::cast;
		switch (item) {
			case final IUnit unit -> {
				final Predicate<IUnit> matchingUnit = matchingValues(unit, IUnit::owner, IUnit::getName, IUnit::getKind,
						IUnit::getId);
				for (final IMutableLegacyMap map : getRestrictedAllMaps()) {
					final Optional<HasMutableName> matching = map.streamAllFixtures()
							.flatMap(ExplorationModel::unflattenNonFortresses)
							.filter(isUnit)
							.filter(hasMutableName)
							.map(unitCast)
							.filter(matchingUnit)
							.map(hmnCast).findAny();
					if (matching.isPresent()) {
						any = true;
						matching.get().setName(newName);
						map.setModified(true);
					}
				}
				if (!any) {
					LovelaceLogger.warning("Unable to find unit to rename");
				}
				return any;
			}
			case final UnitMember member -> {
				final Predicate<HasName> matchingName = matchingValue(item, HasName::getName);
				final Predicate<IFixture> matchingId = matchingValue(member, IFixture::getId);
				for (final IMutableLegacyMap map : getRestrictedAllMaps()) {
					final Optional<HasMutableName> matching = map.streamAllFixtures()
							.flatMap(ExplorationModel::unflattenNonFortresses)
							.filter(isUnit).map(unitCast)
							.filter(this::matchingPlayer).flatMap(FixtureIterable::stream)
							.filter(matchingId)
							.filter(hasMutableName)
							.map(hmnCast)
							.filter(matchingName)
							.findAny();
					if (matching.isPresent()) {
						any = true;
						matching.get().setName(newName);
						map.setModified(true);
					}
				}
				if (!any) {
					LovelaceLogger.warning("Unable to find unit member to rename");
				}
				return any;
			}
			default -> {
				LovelaceLogger.warning("Unable to find item to rename");
				return false;
			}
		}
	}

	@Override
	public final boolean changeKind(final HasKind item, final String newKind) {
		boolean any = false;
		final Predicate<Object> isUnit = IUnit.class::isInstance;
		final Function<Object, IUnit> unitCast = IUnit.class::cast;
		final Predicate<Object> hasMutableKind = HasMutableKind.class::isInstance;
		final Function<Object, HasMutableKind> hmkCast = HasMutableKind.class::cast;
		switch (item) {
			case final IUnit unit -> {
				final Predicate<IUnit> matchingUnit = matchingValues(unit, IUnit::owner, IUnit::getName,
						IUnit::getKind, IUnit::getId);
				for (final IMutableLegacyMap map : getRestrictedAllMaps()) {
					final Optional<HasMutableKind> matching = map.streamAllFixtures()
							.flatMap(ExplorationModel::unflattenNonFortresses)
							.filter(isUnit).map(unitCast)
							.filter(matchingUnit)
							.map(hmkCast).findAny();
					if (matching.isPresent()) {
						any = true;
						matching.get().setKind(newKind);
						map.setModified(true);
					}
				}
				if (!any) {
					LovelaceLogger.warning("Unable to find unit to change kind");
				}
				return any;
			}
			case final UnitMember member -> {
				final Predicate<HasKind> matchingKind = matchingValue(item, HasKind::getKind);
				final Predicate<HasMutableKind> matchingId = m -> ((IFixture) m).getId() == member.getId();
				for (final IMutableLegacyMap map : getRestrictedAllMaps()) {
					final Optional<HasMutableKind> matching = map.streamAllFixtures()
							.flatMap(ExplorationModel::unflattenNonFortresses)
							.filter(isUnit).map(unitCast)
							.filter(this::matchingPlayer)
							.flatMap(FixtureIterable::stream)
							.filter(hasMutableKind)
							.map(hmkCast)
							.filter(matchingKind)
							.filter(matchingId) // TODO: move above cast so we can drop cast in lambda
							.findAny();
					if (matching.isPresent()) {
						any = true;
						matching.get().setKind(newKind);
						map.setModified(true);
					}
				}
				if (!any) {
					LovelaceLogger.warning("Unable to find unit member to change kind");
				}
				return any;
			}
			default -> {
				LovelaceLogger.warning("Unable to find item to change kind");
				return false;
			}
		}
	}

	// TODO: Keep a list of dismissed members
	@Override
	public final void dismissUnitMember(final UnitMember member) {
		for (final IMutableLegacyMap map : getRestrictedAllMaps()) {
			for (final IMutableUnit unit : map.streamAllFixtures()
					.flatMap(ExplorationModel::unflattenNonFortresses)
					.filter(IMutableUnit.class::isInstance)
					.map(IMutableUnit.class::cast)
					.filter(this::matchingPlayer).toList()) {
				final Optional<UnitMember> matching = unit.stream().filter(Predicate.isEqual(member)).findAny();
				if (matching.isPresent()) { // FIXME: equals() will really not do here ...
					unit.removeMember(matching.get());
					dismissedMembers.add(member);
					map.setModified(true);
					break;
				}
			}
		}
	}

	@Override
	public final Iterable<UnitMember> getDismissed() {
		return Collections.unmodifiableSet(dismissedMembers);
	}

	@Override
	public final boolean addSibling(final UnitMember existing, final UnitMember sibling) {
		boolean any = false;
		for (final IMutableLegacyMap map : getRestrictedAllMaps()) {
			for (final IMutableUnit unit : map.streamAllFixtures()
					.flatMap(ExplorationModel::unflattenNonFortresses)
					.filter(IMutableUnit.class::isInstance)
					.map(IMutableUnit.class::cast)
					.filter(this::matchingPlayer).toList()) {
				// TODO: look beyond equals() for matching-in-existing?
				if (unit.stream().anyMatch(Predicate.isEqual(existing))) {
					unit.addMember(sibling.copy(IFixture.CopyBehavior.KEEP));
					any = true;
					map.setModified(true);
					break;
				}
			}
		}
		return any;
	}

	/**
	 * Change the owner of the given item in all maps. Returns true if this
	 * succeeded in any map, false otherwise.
	 */
	@Override
	public final boolean changeOwner(final HasOwner item, final Player newOwner) {
		boolean any = false;
		final Predicate<Object> isOwned = HasMutableOwner.class::isInstance;
		final Function<Object, HasMutableOwner> hmoCast = HasMutableOwner.class::cast;
		for (final IMutableLegacyMap map : getRestrictedAllMaps()) {
			final Optional<HasMutableOwner> matching = map.streamAllFixtures()
					.flatMap(ExplorationModel::flattenIncluding).flatMap(ExplorationModel::flattenIncluding)
					.filter(isOwned)
					.map(hmoCast)
					.filter(Predicate.isEqual(item)) // TODO: equals() is not the best way to find it ...
					.findAny();
			if (matching.isPresent()) {
				// FIXME: Add contains() method to IPlayerCollection and use that instead
				if (StreamSupport.stream(map.getPlayers().spliterator(), true).noneMatch(Predicate.isEqual(newOwner))) {
					map.addPlayer(newOwner);
				}
				matching.get().setOwner(map.getPlayers().getPlayer(newOwner.getPlayerId()));
				map.setModified(true);
				any = true;
			}
		}
		return any;
	}

	@Override
	public final boolean sortFixtureContents(final IUnit fixture) {
		boolean any = false;
		final Predicate<Object> isUnit = IMutableUnit.class::isInstance;
		final Function<Object, IMutableUnit> unitCast = IMutableUnit.class::cast;
		final Predicate<IUnit> matchingUnit = matchingValues(fixture, IUnit::getName, IUnit::getKind, IUnit::getId);
		for (final IMutableLegacyMap map : getRestrictedAllMaps()) {
			final Optional<IMutableUnit> matching = map.streamAllFixtures()
					.flatMap(ExplorationModel::unflattenNonFortresses)
					.filter(isUnit)
					.map(unitCast)
					.filter(this::matchingPlayer)
					.filter(matchingUnit)
					.findAny();
			if (matching.isPresent()) {
				matching.get().sortMembers();
				map.setModified(true);
				any = true;
			}
		}
		return any;
	}

	@Override
	public final void addUnit(final IUnit unit) {
		Point hqLoc = Point.INVALID_POINT;
		final Predicate<Object> isFortress = IFortress.class::isInstance;
		final Function<Object, IFortress> fortressCast = IFortress.class::cast;
		final Predicate<IFortress> matchingPlayer = f -> f.owner().equals(unit.owner());
		for (final Point location : getMap().getLocations()) {
			final Optional<IFortress> fortress = getMap().getFixtures(location).stream()
					.filter(isFortress).map(fortressCast)
					.filter(matchingPlayer).findAny();
			if (fortress.isPresent()) {
				if ("HQ".equals(fortress.get().getName())) {
					hqLoc = location;
					break;
				} else if (!hqLoc.isValid()) {
					hqLoc = location;
				}
			}
		}
		addUnitAtLocation(unit, hqLoc);
	}
}
