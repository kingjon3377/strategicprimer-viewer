package exploration.common;

import common.map.HasName;
import org.javatuples.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
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
import java.util.logging.Logger;

import drivers.common.SimpleMultiMapModel;
import drivers.common.IDriverModel;
import drivers.common.SelectionChangeListener;
import common.map.fixtures.FixtureIterable;
import common.map.fixtures.FortressMember;
import common.map.FakeFixture;
import common.map.IFixture;
import common.map.Player;
import common.map.HasKind;
import common.map.HasMutableKind;
import common.map.HasMutableName;
import common.map.HasMutableOwner;
import common.map.HasOwner;
import common.map.MapDimensions;
import common.map.Point;
import common.map.River;
import common.map.TileFixture;
import common.map.TileType;
import common.map.IMutableMapNG;
import common.map.Direction;
import common.map.IMapNG;
import common.map.fixtures.Ground;
import common.map.fixtures.MineralFixture;
import common.map.fixtures.UnitMember;
import common.map.fixtures.mobile.Animal;
import common.map.fixtures.mobile.AnimalTracks;
import common.map.fixtures.mobile.IMutableUnit;
import common.map.fixtures.mobile.IUnit;
import common.map.fixtures.mobile.ProxyUnit;
import common.map.fixtures.mobile.MobileFixture;
import common.map.fixtures.resources.CacheFixture;
import common.map.fixtures.resources.Grove;
import common.map.fixtures.resources.Meadow;
import common.map.fixtures.resources.Mine;
import common.map.fixtures.resources.MineralVein;
import common.map.fixtures.terrain.Forest;
import common.map.fixtures.towns.IMutableFortress;
import common.map.fixtures.towns.Village;
import common.map.fixtures.towns.IFortress;

// TODO: Make sure all methods are still used; at least one driver now uses a different model interface.
/**
 * A model for exploration apps.
 */
public class ExplorationModel extends SimpleMultiMapModel implements IExplorationModel {
	private static final Logger LOGGER = Logger.getLogger(ExplorationModel.class.getName());
	/**
	 * A fixture is "diggable" if it is a {@link MineralFixture} or a {@link Mine}.
	 */
	private static boolean isDiggable(final TileFixture fixture) {
		return fixture instanceof MineralVein || fixture instanceof Mine;
	}

	private static Stream<IFixture> flattenIncluding(final IFixture fixture) {
		if (fixture instanceof FixtureIterable) {
			return Stream.concat(Stream.of(fixture), ((FixtureIterable<?>) fixture).stream());
		} else {
			return Stream.of(fixture);
		}
	}

	/**
	 * If the item in the entry is a {@link IFortress fortress}, return a
	 * stream of its contents paired with its location; otherwise, return a
	 * {@link Singleton} of the argument.
	 */
	private static Iterable<Pair<Point, IFixture>> flattenEntries(final Pair<Point, IFixture> entry) {
		if (entry.getValue1() instanceof IFortress) {
			return ((IFortress) entry.getValue1()).stream().map(IFixture.class::cast)
					.map(each -> Pair.with(entry.getValue0(), each))
				.collect(Collectors.toList());
		} else {
			return Collections.singleton(entry);
		}
	}

	/**
	 * Check whether two fixtures are "equal enough" for the purposes of
	 * updating a map after digging. This method is needed because equals()
	 * in {@link common.map.fixtures.resources.StoneDeposit} and {@link
	 * common.map.fixtures.resources.MineralVein} compares DCs.
	 */
	private static boolean areDiggablesEqual(final IFixture firstFixture, final IFixture secondFixture) {
		return Objects.equals(firstFixture, secondFixture) ||
			Objects.equals(firstFixture.copy(true), secondFixture.copy(true));
	}

	/**
	 * If a unit's motion could be observed by someone allied to another
	 * (non-independent) player (which at present means the unit is moving
	 * <em>to</em> a tile two or fewer tiles away from the watcher), print a message saying so to stdout.
	 */
	private static void checkAllNearbyWatchers(final IMapNG map, final IUnit unit, final Point dest) {
		final MapDimensions dimensions = map.getDimensions();
		final String description;
		if (unit.getOwner().isIndependent()) {
			description = String.format("%s (ID #%d)", unit.getShortDescription(), unit.getId());
		} else {
			description = unit.getShortDescription();
		}
		for (final Point point : new SurroundingPointIterable(dest, dimensions).stream().distinct()
				.collect(Collectors.toList())) {
			for (final TileFixture fixture : map.getFixtures(point)) {
				if (fixture instanceof HasOwner &&
						!((HasOwner) fixture).getOwner().isIndependent() &&
						!((HasOwner) fixture).getOwner().equals(unit.getOwner())) {
					System.out.printf( // FIXME: Make a new interface for reporting this, and write to UI in a listener
						"Motion of %s to %s could be observed by %s at %s",
						description, dest, fixture.getShortDescription(), point);
				}
			}
		}
	}

	/**
	 * Remove a unit from a location, even if it's in a fortress.
	 */
	private static void removeImpl(final IMutableMapNG map, final Point point, final IUnit unit) {
		boolean outside = false;
		for (final TileFixture fixture : map.getFixtures(point)) {
			if (Objects.equals(unit, fixture)) {
				outside = true;
				break;
			} else if (fixture instanceof IMutableFortress) {
				final Optional<FortressMember> item = ((IMutableFortress) fixture).stream()
					.filter(unit::equals).findAny();
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
	private static void ensureTerrain(final IMapNG mainMap, final IMutableMapNG map, final Point point) {
		if (map.getBaseTerrain(point) == null) {
			map.setBaseTerrain(point, mainMap.getBaseTerrain(point));
		}
		if (mainMap.isMountainous(point)) {
			map.setMountainous(point, true);
		}
		map.addRivers(point, mainMap.getRivers(point).toArray(new River[0]));
		// TODO: Should we copy roads here?
	}

	/**
	 * Whether the given fixture is contained in the given stream.
	 */
	private static boolean doesStreamContainFixture(final Iterable<? extends IFixture> stream, final IFixture fixture) {
		for (final IFixture member : stream) {
			if (Objects.equals(member, fixture)) {
				return true;
			} else if (member instanceof FixtureIterable &&
					doesStreamContainFixture((FixtureIterable<?>) member, fixture)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Whether the given fixture is at the given location in the given map.
	 */
	private static boolean doesLocationHaveFixture(final IMapNG map, final Point point, final TileFixture fixture) {
		return doesStreamContainFixture(map.getFixtures(point), fixture);
	}

	/**
	 * A "plus one" method with a configurable, low "overflow".
	 * @param number The number to increment
	 * @param max The maximum number we want to return
	 */
	private static int increment(final int number, final int max) {
		return (number >= max) ? 0 : (number + 1);
	}

	/**
	 * A "minus one" method that "underflows" after 0 to a configurable, low value.
	 *
	 * @param number The number to decrement
	 * @param max The number to "underflow" to
	 */
	private static int decrement(final int number, final int max) {
		return (number <= 0) ? max : (number - 1);
	}

	/**
	 * The intersection of two sets; here so it can be passed as a method
	 * reference rather than a lambda in {@link playerChoices}.
	 */
	private static <T> Set<T> intersection(final Set<T> one, final Set<T> two) {
		final Set<T> retval = new HashSet<>(one);
		retval.retainAll(two);
		return retval;
	}

	/**
	 * If {@link fixture} is a {@link IFortress fortress}, return a stream of its contents;
	 * otherwise, return a stream containing only it. This is intended to
	 * be used in {@link Stream#flatMap}.
	 */
	private static Stream<IFixture> unflattenNonFortresses(final TileFixture fixture) {
		if (fixture instanceof IFortress) {
			return ((IFortress) fixture).stream().map(IFixture.class::cast);
		} else {
			return Stream.of(fixture);
		}
	}

	private final List<MovementCostListener> mcListeners = new ArrayList<>();
	private final List<SelectionChangeListener> scListeners = new ArrayList<>();

	/**
	 * The currently selected unit and its location.
	 */
	private Pair<Point, @Nullable IUnit> selection = Pair.with(Point.INVALID_POINT, null);

	public ExplorationModel(final IMutableMapNG map) {
		super(map);
	}

	// TODO: Make private and provide static copyConstructor method instead of making this public?
	public ExplorationModel(final IDriverModel model) {
		super(model);
	}

	/**
	 * All the players shared by all the maps.
	 * TODO: Move to IMultiMapModel?
	 */
	@Override
	public Collection<Player> getPlayerChoices() {
		// TODO: Port this stream-based algorithm to Java
//		return allMaps.map(IMapNG.players).map(set).fold(set(map.players))(intersection);
		final Set<Player> retval = StreamSupport.stream(getMap().getPlayers().spliterator(), true)
			.collect(Collectors.toSet());
		for (final IMapNG map : getSubordinateMaps()) {
			retval.retainAll(StreamSupport.stream(map.getPlayers().spliterator(), true)
				.collect(Collectors.toSet()));
		}
		return Collections.unmodifiableSet(retval);
	}

	/**
	 * Collect all the units in the main map belonging to the specified player.
	 */
	@Override
	public List<IUnit> getUnits(final Player player) {
		return getMap().streamAllFixtures()
			.flatMap(ExplorationModel::unflattenNonFortresses)
			.filter(IUnit.class::isInstance)
			.map(IUnit.class::cast)
			.filter(u -> u.getOwner().equals(player))
			.collect(Collectors.toList());
	}

	/**
	 * Tell listeners that the selected point changed.
	 */
	private void fireSelectionChange(final Point old, final Point newSelection) {
		for (final SelectionChangeListener listener : scListeners) {
			LOGGER.fine("Notifying a listener of selected-point change");
			listener.selectedPointChanged(old, newSelection);
		}
	}

	/**
	 * Tell listeners that the selected unit changed.
	 */
	private void fireSelectedUnitChange(@Nullable final IUnit old, @Nullable final IUnit newSelection) {
		for (final SelectionChangeListener listener : scListeners) {
			LOGGER.fine("Notifying a listener of selected-unit change");
			listener.selectedUnitChanged(old, newSelection);
		}
	}

	/**
	 * Tell listeners to deduct a cost from their movement-point totals.
	 */
	private void fireMovementCost(final int cost) {
		for (final MovementCostListener listener : mcListeners) {
			listener.deduct(cost);
		}
	}

	/**
	 * Get the location one tile in the given direction from the given point.
	 */
	@Override
	public Point getDestination(final Point point, final Direction direction) {
		final MapDimensions dims = getMapDimensions();
		final int maxColumn = dims.getColumns() - 1;
		final int maxRow = dims.getRows() - 1;
		final int row = point.getRow();
		final int column = point.getColumn();
		switch (direction) {
		case East:
			return new Point(row, increment(column, maxColumn));
		case North:
			return new Point(decrement(row, maxRow), column);
		case Northeast:
			return new Point(decrement(row, maxRow), increment(column, maxColumn));
		case Northwest:
			return new Point(decrement(row, maxRow), decrement(column, maxColumn));
		case South:
			return new Point(increment(row, maxRow), column);
		case Southeast:
			return new Point(increment(row, maxRow), increment(column, maxColumn));
		case Southwest:
			return new Point(increment(row, maxRow), decrement(column, maxColumn));
		case West:
			return new Point(row, decrement(column, maxColumn));
		case Nowhere:
			return point;
		default:
			throw new IllegalStateException("Exhaustive switch wasn't");
		}
	}

	private void fixMovedUnits(final Point base) {
		final BiFunction<IMapNG, TileFixture, Iterable<Pair<Point, TileFixture>>> localFind =
			(mapParam, target) -> mapParam.streamLocations()
				.flatMap(l -> mapParam.getFixtures(l).stream().map(f -> Pair.with(l, f)))
				.filter(p -> target.equals(p.getValue1())) // TODO: Filter should come earlier
				.collect(Collectors.toList());
		// TODO: Unit vision range
		final Iterable<Point> points = new SurroundingPointIterable(base, getMap().getDimensions(), 2);
		for (final IMutableMapNG submap : getRestrictedSubordinateMaps()) { // TODO: Can we limit use of mutability to a narrower critical section?
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
	 * @throws TraversalImpossibleException if movement in the specified direction is impossible
	 * @param direction The direction to move
	 * @param speed How hastily the explorer is moving
	 */
	@Override
	public int move(final Direction direction, final Speed speed) throws TraversalImpossibleException {
		final Pair<Point, @Nullable IUnit> local = selection;
		final Point point = local.getValue0();
		final IUnit unit = local.getValue1();
		if (unit == null) {
			throw new IllegalStateException("No mover selected");
		}
		final Point dest = getDestination(point, direction);
		final TileType terrain = getMap().getBaseTerrain(dest);
		final TileType startingTerrain = getMap().getBaseTerrain(point);
		if (terrain != null && startingTerrain != null &&
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
			final int retval = (int) (Math.ceil((base * speed.getMpMultiplier()) + 0.1));
			removeImpl(getRestrictedMap(), point, unit);
			getRestrictedMap().addFixture(dest, unit);
			setMapModified(true);
			for (final IMutableMapNG subMap : getRestrictedSubordinateMaps()) { // FIXME: Use copyToSubMaps()
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
			if (getMap().getBaseTerrain(point) == null) {
				LOGGER.fine("Started outside explored territory in main map");
			} else if (getMap().getBaseTerrain(dest) == null) {
				LOGGER.fine("Main map doesn't have terrain for destination");
			} else {
				if (SimpleMovementModel.landMovementPossible(terrain) &&
						    TileType.Ocean == startingTerrain) {
					LOGGER.fine("Starting in ocean, trying to get to " + terrain);
				} else if (TileType.Ocean == startingTerrain &&
						                                                                                              TileType.Ocean != terrain) {
					LOGGER.fine("Land movement not possible from ocean to " + terrain);
				} else if (TileType.Ocean != startingTerrain &&
						           TileType.Ocean == terrain) {
					LOGGER.fine(String.format("Starting in %s, trying to get to ocean",
						startingTerrain));
				} else {
					LOGGER.fine("Unknown reason for movement-impossible condition");
				}
			}
			for (final IMutableMapNG subMap : getRestrictedSubordinateMaps()) {
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
	public Point find(final TileFixture fixture) {
		for (final Point point : getMap().getLocations()) {
			if (doesLocationHaveFixture(getMap(), point, fixture)) {
				return point;
			}
		}
		return Point.INVALID_POINT;
	}

	private boolean mapsAgreeOnLocation(final IUnit unit) {
		if (unit instanceof ProxyUnit) {
			if (((ProxyUnit) unit).getProxied().isEmpty()) {
				return false;
			} else {
				return mapsAgreeOnLocation(((ProxyUnit) unit).getProxied().iterator().next());
			}
		}
		final Point mainLoc = find(unit);
		if (!mainLoc.isValid()) {
			return false;
		}
		for (final IMapNG subMap : getSubordinateMaps()) {
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
	public @Nullable IUnit getSelectedUnit() {
		return selection.getValue1();
	}

	/**
	 * Select the given unit.
	 */
	@Override
	public void setSelectedUnit(@Nullable final IUnit selectedUnit) {
		final Point oldLoc = selection.getValue0();
		final IUnit oldSelection = selection.getValue1();
		final Point loc;
		if (selectedUnit == null) {
			LOGGER.fine("Unsetting currently-selected-unit property");
			loc = Point.INVALID_POINT;
		} else {
			if (!mapsAgreeOnLocation(selectedUnit)) {
				LOGGER.warning("Maps containing that unit don't all agree on its location");
			}
			LOGGER.fine("Setting a newly selected unit");
			loc = find(selectedUnit);
			if (loc.isValid()) {
				LOGGER.fine("Found at " + loc);
			} else {
				LOGGER.fine("Not found using our 'find' method");
			}
		}
		selection = Pair.with(loc, selectedUnit);
		fireSelectionChange(oldLoc, loc);
		fireSelectedUnitChange(oldSelection, selectedUnit);
	}

	/**
	 * The location of the currently selected unit.
	 */
	@Override
	public Point getSelectedUnitLocation() {
		return selection.getValue0();
	}

	/**
	 * Add a selection-change listener.
	 */
	@Override
	public void addSelectionChangeListener(final SelectionChangeListener listener) {
		scListeners.add(listener);
	}

	/**
	 * Remove a selection-change listener.
	 */
	@Override
	public void removeSelectionChangeListener(final SelectionChangeListener listener) {
		scListeners.remove(listener);
	}

	/**
	 * Add a movement-cost listener.
	 */
	@Override
	public void addMovementCostListener(final MovementCostListener listener) {
		mcListeners.add(listener);
	}

	/**
	 * Remove a movement-cost listener.
	 */
	@Override
	public void removeMovementCostListener(final MovementCostListener listener) {
		mcListeners.remove(listener);
	}

	/**
	 * If there is a currently selected unit, make any independent villages
	 * at its location change to be owned by the owner of the currently
	 * selected unit. This costs MP.
	 */
	@Override
	public void swearVillages() {
		final Pair<Point, @Nullable IUnit> localSelection = selection;
		final Point currentPoint = localSelection.getValue0();
		final IUnit unit = localSelection.getValue1();
		if (unit != null) {
			final Player owner = unit.getOwner();
			final List<Village> villages = streamAllMaps().flatMap(m -> m.getFixtures(currentPoint).stream())
				.filter(Village.class::isInstance)
				.map(Village.class::cast)
				.filter(v -> v.getOwner().isIndependent())
				.collect(Collectors.toList());
			if (!villages.isEmpty()) {
				boolean subordinate = false;
				for (final Village village : villages) {
					village.setOwner(owner);
					for (final IMutableMapNG subMap : getRestrictedAllMaps()) {
						subMap.addFixture(currentPoint, village.copy(subordinate));
						subordinate = true;
						subMap.setModified(true);
					}
				}
				final IMapNG mainMap = getMap();
				final Iterable<Point> surroundingPoints =
					new SurroundingPointIterable(currentPoint, getMapDimensions(), 1);
				for (final Point point : surroundingPoints) {
					for (final IMutableMapNG subMap : getRestrictedSubordinateMaps()) {
						ensureTerrain(mainMap, subMap, point);
						final Optional<Forest> subForest = subMap.getFixtures(point)
							.stream().filter(Forest.class::isInstance)
							.map(Forest.class::cast).findFirst();
						final Optional<Forest> forest = getMap().getFixtures(point).stream()
							.filter(Forest.class::isInstance)
							.map(Forest.class::cast).findFirst();
						if (forest.isPresent() && !subForest.isPresent()) {
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
				for (final IMutableMapNG subMap : getRestrictedSubordinateMaps()) {
					vegetation.ifPresent(objects -> subMap.addFixture(objects.getValue0(),
							objects.getValue1().copy(true)));
					animal.ifPresent(objects -> subMap.addFixture(objects.getValue0(),
							objects.getValue1().copy(true)));
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
	public void dig() {
		final Point currentPoint = selection.getValue0();
		if (currentPoint.isValid()) {
			final IMutableMapNG mainMap = getRestrictedMap();
			final List<TileFixture> diggables = mainMap.getFixtures(currentPoint)
					.stream().filter(ExplorationModel::isDiggable).collect(Collectors.toList());
			if (diggables.isEmpty()) {
				return;
			}
			int i = 0;
			boolean first = true;
			while (first || (i < 4 && !(diggables.get(0) instanceof Ground))) {
				Collections.shuffle(diggables);
				first = false;
				i++;
			}
			final TileFixture oldFixture = diggables.get(0);
			final TileFixture newFixture = oldFixture.copy(false);
			if (newFixture instanceof Ground) { // TODO: Extract an interface for this field so we only have to do one test
				((Ground) newFixture).setExposed(true);
			} else if (newFixture instanceof MineralVein) {
				((MineralVein) newFixture).setExposed(true);
			}
			final BiConsumer<IMutableMapNG, Boolean> addToMap = (map, condition) -> {
				if (map.getFixtures(currentPoint).stream()
						.anyMatch(f -> areDiggablesEqual(oldFixture, f))) {
					map.replace(currentPoint, oldFixture, newFixture.copy(condition));
				} else {
					map.addFixture(currentPoint, newFixture.copy(condition));
				}
			};
			boolean subsequent = false;
			for (final IMutableMapNG subMap : getRestrictedAllMaps()) {
				addToMap.accept(subMap, subsequent);
				subsequent = true;
				subMap.setModified(true);
			}
			fireMovementCost(4);
		}
	}

	/**
	 * Add the given {@link unit} at the given {@link location}.
	 */
	@Override
	public void addUnitAtLocation(final IUnit unit, final Point location) { // TODO: If more than one map, return a proxy for the units; otherwise, return the unit
		for (final IMutableMapNG indivMap : getRestrictedAllMaps()) {
			indivMap.addFixture(location, unit); // FIXME: Check for existing matching unit there already
			indivMap.setModified(true);
		}
	}

	/**
	 * Copy the given fixture from the main map to subordinate maps. (It is
	 * found in the main map by ID, rather than trusting the input.) If it
	 * is a cache, remove it from the main map. If {@link zero}, remove
	 * sensitive information from the copies.
	 */
	@Override
	public boolean copyToSubMaps(final Point location, final TileFixture fixture, final boolean zero) {
		@Nullable final TileFixture matching;
		boolean retval = false;
		if (fixture instanceof FakeFixture) {
			// Skip it! It'll corrupt the output XML!
			return false;
		} else if (fixture instanceof AnimalTracks) {
			matching = fixture;
		} else {
			matching = getMap().getFixtures(location).stream()
				.filter(f -> f.getId() == fixture.getId()).findAny().orElse(null);
		}
		if (matching == null) {
			LOGGER.warning("Skipping because not in the main map");
		} else {
			for (final IMutableMapNG subMap : getRestrictedSubordinateMaps()) {
				retval = subMap.addFixture(location, matching.copy(zero)) || retval;
				// We do *not* use the return value because it returns false if an existing fixture was *replaced*
				subMap.setModified(true);
			}

			if (matching instanceof CacheFixture) {
				getRestrictedMap().removeFixture(location, matching); // TODO: make removeFixture() return Boolean, true if anything was removed
				retval = true;
				getRestrictedMap().setModified(true); // TODO: Here and elsewhere, don't bother to setModified() if the earlier mutator method already sets the flag
			}
		}
		return retval;
	}

	/**
	 * Copy any terrain, mountain, rivers, and roads from the main map to subordinate maps.
	 */
	@Override
	public void copyTerrainToSubMaps(final Point location) {
		for (final IMutableMapNG subMap : getRestrictedSubordinateMaps()) {
			if (getMap().isMountainous(location) && !subMap.isMountainous(location)) {
				subMap.setMountainous(location, true);
				subMap.setModified(true);
			}
			final TileType terrain = getMap().getBaseTerrain(location);
			if (terrain != null &&
					    terrain != subMap.getBaseTerrain(location)) {
				subMap.setBaseTerrain(location, terrain);
				subMap.setModified(true);
			}
			if (!getMap().getRivers(location).containsAll(subMap.getRivers(location))) {
				subMap.addRivers(location, getMap().getRivers(location).toArray(new River[0]));
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
	public void setSubMapTerrain(final Point location, @Nullable final TileType terrain) {
		for (final IMutableMapNG subMap : getRestrictedSubordinateMaps()) {
			subMap.setBaseTerrain(location, terrain);
			subMap.setModified(true);
		}
	}

	/**
	 * Copy the given rivers to sub-maps, if they are present in the main map.
	 */
	@Override
	public void copyRiversToSubMaps(final Point location, final River... rivers) {
		final Collection<River> actualRivers = EnumSet.copyOf(Stream.of(rivers)
			.collect(Collectors.toList()));
		actualRivers.retainAll(getMap().getRivers(location));
		for (final IMutableMapNG subMap : getRestrictedSubordinateMaps()) {
			subMap.addRivers(location, actualRivers.toArray(new River[0])); // TODO: Make it return Boolean if this was a change, and only set modified flag in that case
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
	public void removeRiversFromSubMaps(final Point location, final River... rivers) {
		for (final IMutableMapNG subMap : getRestrictedSubordinateMaps()) {
			subMap.removeRivers(location, rivers); // TODO: Make it return Boolean if this was a change, and only set modified flag in that case
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
	public void removeFixtureFromSubMaps(final Point location, final TileFixture fixture) {
		for (final IMutableMapNG subMap : getRestrictedSubordinateMaps()) {
			subMap.removeFixture(location, fixture); // TODO: Make it return Boolean if this was a change, and only set modified flag in that case
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
	public void setMountainousInSubMap(final Point location, final boolean mountainous) {
		for (final IMutableMapNG subMap : getRestrictedSubordinateMaps()) {
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
	public void moveMember(final UnitMember member, final IUnit old, final IUnit newOwner) {
		for (final IMutableMapNG map : getRestrictedSubordinateMaps()) {
			final Optional<IMutableUnit> matchingOld = map.streamAllFixtures()
				.flatMap(ExplorationModel::unflattenNonFortresses)
				.filter(IMutableUnit.class::isInstance).map(IMutableUnit.class::cast)
				.filter(u -> u.getOwner().equals(old.getOwner()) &&
					u.getKind().equals(old.getKind()) &&
					u.getName().equals(old.getName()) && u.getId() == old.getId())
				.findAny();
			final Optional<UnitMember> matchingMember =
				matchingOld.flatMap(u -> u.stream().filter(member::equals) // TODO: equals() isn't ideal for finding a matching member ...
					.findAny());
			final Optional<IMutableUnit> matchingNew = map.streamAllFixtures()
				.flatMap(ExplorationModel::unflattenNonFortresses)
				.filter(IMutableUnit.class::isInstance).map(IMutableUnit.class::cast)
				.filter(u -> u.getOwner().equals(newOwner.getOwner()) &&
					u.getKind().equals(newOwner.getKind()) &&
					u.getName().equals(newOwner.getName()) &&
					u.getId() == newOwner.getId())
				.findAny();
			if (matchingOld.isPresent() && matchingMember.isPresent() &&
					matchingNew.isPresent()) {
				matchingOld.get().removeMember(matchingMember.get());
				matchingNew.get().addMember(matchingMember.get());
				map.setModified(true);
			}
		}
	}

	private Predicate<Pair<Point, TileFixture>> unitMatching(final IUnit unit) {
		return (pair) -> {
			final Point location = pair.getValue0();
			final IFixture fixture = pair.getValue1();
			if (fixture instanceof IUnit && fixture.getId() == unit.getId() &&
					((IUnit) fixture).getOwner().equals(unit.getOwner())) {
				return true;
			} else {
				return false;
			}
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
	public boolean removeUnit(final IUnit unit) {
		LOGGER.fine("In ExplorationModel.removeUnit()");
		final List<Pair<IMutableMapNG, Pair<Point, IUnit>>> delenda = new ArrayList<>();
		for (final IMutableMapNG map : getRestrictedAllMaps()) {
			final Optional<Pair<Point, TileFixture>> pair = map.streamLocations()
					.flatMap(l -> map.getFixtures(l).stream().map(f -> Pair.with(l, f)))
					.filter(unitMatching(unit)).findAny();
			if (pair.isPresent()) {
				LOGGER.fine("Map has matching unit");
				final IUnit fixture = (IUnit) pair.get().getValue1();
				if (fixture.getKind().equals(unit.getKind()) &&
						fixture.getName().equals(unit.getName()) &&
						!fixture.iterator().hasNext()) {
					LOGGER.fine("Matching unit meets preconditions");
					delenda.add(Pair.with(map,
						Pair.with(pair.get().getValue0(), fixture)));
				} else {
					LOGGER.warning(String.format(
						"Matching unit in %s fails preconditions for removal",
						Optional.ofNullable(map.getFilename())
							.map(Object::toString)
							.filter(f -> !f.isEmpty())
							.orElse("an unsaved map")));
					return false;
				}
			}
		}
		if (delenda.isEmpty()) {
			LOGGER.fine("No matching units");
			return false;
		}
		for (final Pair<IMutableMapNG, Pair<Point, IUnit>> entry : delenda) {
			final IMutableMapNG map = entry.getValue0();
			final Point location = entry.getValue1().getValue0();
			final TileFixture fixture = entry.getValue1().getValue1();
			if (map.getFixtures(location).contains(fixture)) {
				map.removeFixture(location, fixture);
			} else {
				boolean any = false;
				for (final IMutableFortress fort : map.getFixtures(location).stream()
						.filter(IMutableFortress.class::isInstance)
						.map(IMutableFortress.class::cast)
						.collect(Collectors.toList())) {
					if (fort.stream().anyMatch(fixture::equals)) {
						fort.removeMember((FortressMember) fixture);
						any = true;
						break;
					}
				}
				if (!any) {
					LOGGER.warning("Failed to find unit to remove that we thought might be in a fortress");
				}
			}
		}
		LOGGER.fine("Finished removing matching unit(s) from map(s)");
		return true;
	}

	@Override
	public void addUnitMember(final IUnit unit, final UnitMember member) {
		for (final IMutableMapNG map : getRestrictedAllMaps()) {
			final Optional<IMutableUnit> matching = map.streamAllFixtures()
					.flatMap(ExplorationModel::unflattenNonFortresses)
					.filter(IMutableUnit.class::isInstance)
					.map(IMutableUnit.class::cast)
					.filter(u -> u.getOwner().equals(unit.getOwner()) &&
						u.getName().equals(unit.getName()) &&
						u.getKind().equals(unit.getKind()) &&
						u.getId() == unit.getId())
					.findAny();
			if (matching.isPresent()) {
				matching.get().addMember(member.copy(false));
				map.setModified(true);
				continue;
			}
		}
	}

	boolean matchingPlayer(final HasOwner fixture) {
		final Pair<Point, @Nullable IUnit> selection = this.selection; // TODO: Ceylon used "value", so may have been wrong here
		final IUnit unit = selection.getValue1();
		final Player currentPlayer = Optional.ofNullable(unit).map(IUnit::getOwner).orElse(null);
		if (currentPlayer == null) {
			return false;
		} else {
			return fixture.getOwner().equals(currentPlayer);
		}
	}

	@Override
	public boolean renameItem(final HasName item, final String newName) {
		boolean any = false;
		if (item instanceof IUnit) {
			for (final IMutableMapNG map : getRestrictedAllMaps()) {
				final Optional<HasMutableName> matching = map.streamAllFixtures()
						.flatMap(ExplorationModel::unflattenNonFortresses)
						.filter(IUnit.class::isInstance)
						.filter(HasMutableName.class::isInstance)
						.map(IUnit.class::cast)
						.filter(u -> u.getOwner().equals(((IUnit) item).getOwner()))
						.filter(u -> u.getName().equals(item.getName()))
						.filter(u -> u.getKind().equals(((IUnit) item).getKind()))
						.filter(u -> u.getId() == ((IUnit) item).getId())
						.map(HasMutableName.class::cast).findAny();
				if (matching.isPresent()) {
					any = true;
					matching.get().setName(newName);
					map.setModified(true);
				}
			}
			if (!any) {
				LOGGER.warning("Unable to find unit to rename");
			}
			return any;
		} else if (item instanceof UnitMember) {
			for (final IMutableMapNG map : getRestrictedAllMaps()) {
				final Optional<HasMutableName> matching = map.streamAllFixtures()
						.flatMap(ExplorationModel::unflattenNonFortresses)
						.filter(IUnit.class::isInstance).map(IUnit.class::cast)
						.filter(this::matchingPlayer).flatMap(FixtureIterable::stream)
						.filter(u -> u.getId() == ((UnitMember) item).getId())
						.filter(HasMutableName.class::isInstance)
						.map(HasMutableName.class::cast)
						.filter(u -> u.getName().equals(
							item.getName()))
						.findAny();
				if (matching.isPresent()) {
					any = true;
					matching.get().setName(newName);
					map.setModified(true);
				}
			}
			if (!any) {
				LOGGER.warning("Unable to find unit member to rename");
			}
			return any;
		} else {
			LOGGER.warning("Unable to find item to rename");
			return false;
		}
	}

	@Override
	public boolean changeKind(final HasKind item, final String newKind) {
		boolean any = false;
		if (item instanceof IUnit) {
			for (final IMutableMapNG map : getRestrictedAllMaps()) {
				final Optional<HasMutableKind> matching = map.streamAllFixtures()
						.flatMap(ExplorationModel::unflattenNonFortresses)
						.filter(IUnit.class::isInstance).map(IUnit.class::cast)
						.filter(u -> u.getOwner().equals(((IUnit) item).getOwner()))
						.filter(u -> u.getName().equals(((IUnit) item).getName()))
						.filter(u -> u.getKind().equals(item.getKind()))
						.filter(u -> u.getId() == ((IUnit) item).getId())
						.map(HasMutableKind.class::cast).findAny();
				if (matching.isPresent()) {
					any = true;
					matching.get().setKind(newKind);
					map.setModified(true);
				}
			}
			if (!any) {
				LOGGER.warning("Unable to find unit to change kind");
			}
			return any;
		} else if (item instanceof UnitMember) {
			for (final IMutableMapNG map : getRestrictedAllMaps()) {
				final Optional<HasMutableKind> matching = map.streamAllFixtures()
						.flatMap(ExplorationModel::unflattenNonFortresses)
						.filter(IUnit.class::isInstance).map(IUnit.class::cast)
						.filter(this::matchingPlayer)
						.flatMap(FixtureIterable::stream)
						.filter(HasMutableKind.class::isInstance)
						.map(HasMutableKind.class::cast)
						.filter(m -> m.getKind().equals(item.getKind()))
						.filter(m -> ((IFixture) m).getId() == ((IFixture) item).getId())
						.findAny();
				if (matching.isPresent()) {
					any = true;
					matching.get().setKind(newKind);
					map.setModified(true);
				}
			}
			if (!any) {
				LOGGER.warning("Unable to find unit member to change kind");
			}
			return any;
		} else {
			LOGGER.warning("Unable to find item to change kind");
			return false;
		}
	}

	// TODO: Keep a list of dismissed members
	@Override
	public void dismissUnitMember(final UnitMember member) {
		for (final IMutableMapNG map : getRestrictedAllMaps()) {
			for (final IMutableUnit unit : map.streamAllFixtures()
					.flatMap(ExplorationModel::unflattenNonFortresses)
					.filter(IMutableUnit.class::isInstance)
					.map(IMutableUnit.class::cast)
					.filter(this::matchingPlayer).collect(Collectors.toList())) {
				final Optional<UnitMember> matching = unit.stream().filter(member::equals).findAny();
				if (matching.isPresent()) { // FIXME: equals() will really not do here ...
					unit.removeMember(matching.get());
					map.setModified(true);
					break;
				}
			}
		}
	}

	@Override
	public boolean addSibling(final UnitMember existing, final UnitMember sibling) {
		boolean any = false;
		for (final IMutableMapNG map : getRestrictedAllMaps()) {
			for (final IMutableUnit unit : map.streamAllFixtures()
					.flatMap(ExplorationModel::unflattenNonFortresses)
					.filter(IMutableUnit.class::isInstance)
					.map(IMutableUnit.class::cast)
					.filter(this::matchingPlayer).collect(Collectors.toList())) {
				if (unit.stream().anyMatch(existing::equals)) { // TODO: look beyond equals() for matching-in-existing?
					unit.addMember(sibling.copy(false));
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
	public boolean changeOwner(final HasOwner item, final Player newOwner) {
		boolean any = false;
		for (final IMutableMapNG map : getRestrictedAllMaps()) {
			final Optional<HasMutableOwner> matching = map.streamAllFixtures()
					.flatMap(ExplorationModel::flattenIncluding).flatMap(ExplorationModel::flattenIncluding)
					.filter(HasMutableOwner.class::isInstance)
					.map(HasMutableOwner.class::cast)
					.filter(item::equals) // TODO: equals() is not the best way to find it ...
					.findAny();
			if (matching.isPresent()) {
				if (StreamSupport.stream(map.getPlayers().spliterator(), true).noneMatch(newOwner::equals)) { // FIXME: Add contains() method to IPlayerCollection and use that instead
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
	public boolean sortFixtureContents(final IUnit fixture) {
		boolean any = false;
		for (final IMutableMapNG map : getRestrictedAllMaps()) {
			final Optional<IMutableUnit> matching = map.streamAllFixtures()
					.flatMap(ExplorationModel::unflattenNonFortresses)
					.filter(IMutableUnit.class::isInstance)
					.map(IMutableUnit.class::cast)
					.filter(this::matchingPlayer)
					.filter(u -> u.getName().equals(fixture.getName()))
					.filter(u -> u.getKind().equals(fixture.getKind()))
					.filter(u -> u.getId() == fixture.getId())
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
	public void addUnit(final IUnit unit) {
		Point hqLoc = Point.INVALID_POINT;
		for (final Point location : getMap().getLocations()) {
			final Optional<IFortress> fortress = getMap().getFixtures(location).stream()
				.filter(IFortress.class::isInstance).map(IFortress.class::cast)
				.filter(f -> f.getOwner().equals(unit.getOwner())).findAny();
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
