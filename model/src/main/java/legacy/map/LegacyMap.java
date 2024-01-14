package legacy.map;

import legacy.map.fixtures.mobile.IUnit;

import java.util.Arrays;

import lovelace.util.LovelaceLogger;
import org.javatuples.Pair;

import java.nio.file.Path;

import java.util.Objects;
import java.util.Set;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import java.util.stream.Stream;

import java.util.function.Consumer;
import java.util.function.BiPredicate;

import legacy.map.fixtures.towns.AbstractTown;

import org.jetbrains.annotations.Nullable;

/**
 * A class to represent a game-world map and its contents.
 */
public class LegacyMap implements IMutableLegacyMap {
	/**
	 * Whether the given fixture should be zeroed out if the map is for the given player.
	 */
	private static boolean shouldZero(final TileFixture fixture, final @Nullable Player player) {
		if (!Objects.isNull(player) && fixture instanceof final HasOwner owned) {
			return player.equals(owned.owner());
		} else {
			return true;
		}
	}

	/**
	 * If either of the provided fixtures is a subset of the other, return
	 * true; otherwise return false.
	 */
	private static boolean subsetCheck(final TileFixture one, final TileFixture two) {
		if (one instanceof final SubsettableFixture sf && sf.isSubset(two, x -> {
		})) {
			return true;
		} else {
			return two instanceof final SubsettableFixture sf && sf.isSubset(one, x -> {
			});
		}
	}

	/**
	 * The file from which the map was loaded, or to which it should be saved, if known
	 */
	private @Nullable Path filename = null; // TODO: Require as constructor parameter?

	/**
	 * The file from which the map was loaded, or to which it should be saved, if known
	 */
	@Override
	public @Nullable Path getFilename() {
		return filename;
	}

	/**
	 * Set the file from which the map was loaded, or to which it should be saved, if known
	 */
	@Override
	public void setFilename(final @Nullable Path filename) {
		this.filename = filename;
	}

	/**
	 * Whether the map has been modified since it was last saved.
	 */
	private boolean modified = false; // FIXME: Make all mutating methods set this

	/**
	 * Whether the map has been modified since it was last saved.
	 */
	@Override
	public boolean isModified() {
		return modified;
	}

	/**
	 * Set whether the map has been modified since it was last saved.
	 */
	@Override
	public void setModified(final boolean modified) {
		this.modified = modified;
	}

	/**
	 * The set of mountainous places.
	 */
	private final Set<Point> mountains;

	/**
	 * The base terrain at points in the map.
	 */
	private final Map<Point, TileType> terrain;

	/**
	 * The players in the map.
	 */
	private final IMutableLegacyPlayerCollection playerCollection;

	/**
	 * Fixtures at various points, other than the main ground and forest.
	 */
	// TODO: Use Guava multimap?
	private final Map<Point, List<TileFixture>> fixturesMap;

	/**
	 * The version and dimensions of the map.
	 */
	private final MapDimensions mapDimensions;

	/**
	 * The rivers in the map.
	 */
	private final Map<Point, Set<River>> riversMap;

	/**
	 * Roads in the map.
	 */
	private final Map<Point, Map<Direction, Integer>> roadsMap;

	/**
	 * The current turn.
	 */
	private int currentTurn;

	/**
	 * The current turn.
	 */
	@Override
	public int getCurrentTurn() {
		return currentTurn;
	}

	/**
	 * The current turn.
	 */
	@Override
	public void setCurrentTurn(final int currentTurn) {
		this.currentTurn = currentTurn;
	}

	/**
	 * The collection of bookmarks.
	 */
	private final Map<Point, Set<Player>> bookmarksImpl;

	private static int reduceLarge(final int num) {
		if (num < 16) {
			return 16;
		}
		final int retval = num >> 1;
		if (retval < 20) {
			return num;
		} else {
			return retval;
		}
	}

	private static int reduceMore(final int num) {
		if (num < 16) {
			return 16;
		}
		final int retval = num >> 2;
		if (retval < 20) {
			return num;
		} else {
			return retval;
		}
	}

	public LegacyMap(final MapDimensions dimensions, final IMutableLegacyPlayerCollection players, final int turn) {
		final int size = dimensions.rows() * dimensions.columns();
		terrain = new HashMap<>(reduceLarge(size), 0.5f);
		fixturesMap = new HashMap<>(reduceLarge(size), 0.5f);
		riversMap = new HashMap<>(reduceMore(size), 0.5f);
		roadsMap = new HashMap<>(reduceMore(size), 0.5f);
		bookmarksImpl = new HashMap<>(reduceMore(size), 0.5f);
		mountains = new HashSet<>(reduceLarge(size), 0.5f);
		mapDimensions = dimensions;
		playerCollection = players;
		currentTurn = turn;
	}

	/**
	 * The dimensions of the map.
	 */
	@Override
	public MapDimensions getDimensions() {
		return mapDimensions;
	}

	/**
	 * A stream of the players known in the map
	 */
	@Override
	public ILegacyPlayerCollection getPlayers() {
		return playerCollection;
	}

	/**
	 * The locations in the map.
	 *
	 * In Ceylon this only gave valid locations, but the serialization code
	 * could get invalid-but-used points from {@code fixtures.keys};
	 * we include the invalid-but-apparently-used points here in Java
	 * because we don't expose fixtures as a Map now.
	 */
	@Override
	public Iterable<Point> getLocations() {
		return Stream.concat(
			StreamSupport.stream(new PointIterable(getDimensions(), true, true).spliterator(),
				false),
			fixturesMap.keySet().stream()).distinct().collect(Collectors.toList());
	}

	/**
	 * Stream the locations in the map.
	 */
	@Override
	public Stream<Point> streamLocations() {
		// TODO: Add stream() to PointIterable
		return Stream.concat(
			StreamSupport.stream(new PointIterable(getDimensions(), true, true).spliterator(),
				false),
			fixturesMap.keySet().stream()).distinct();
	}

	/**
	 * The base terrain at the given location.
	 */
	@Override
	public @Nullable TileType getBaseTerrain(final Point key) {
		return terrain.get(key);
	}

	/**
	 * Set the base terrain at the given location.
	 */
	@Override
	public @Nullable TileType setBaseTerrain(final Point key, final @Nullable TileType item) {
		modified = true; // TODO: Only if this is a change
		final @Nullable TileType retval = getBaseTerrain(key);
		if (Objects.isNull(item)) {
			terrain.remove(key);
		} else {
			terrain.put(key, item);
		}
		return retval;
	}

	/**
	 * Whether the given location is mountainous.
	 */
	@Override
	public boolean isMountainous(final Point key) {
		return mountains.contains(key);
	}

	@Override
	public boolean setMountainous(final Point key, final boolean item) {
		modified = true; // TODO: Only if this is a change
		final boolean retval = isMountainous(key);
		if (item) {
			mountains.add(key);
		} else {
			mountains.remove(key);
		}
		return retval;
	}

	/**
	 * The rivers, if any, at the given location.
	 */
	@Override
	public Collection<River> getRivers(final Point location) {
		final Set<River> retval = riversMap.get(location);
		if (Objects.isNull(retval)) {
			return Collections.emptySet();
		} else {
			return Collections.unmodifiableSet(retval);
		}
	}

	/**
	 * The directions and quality levels of roads at various locations.
	 */
	@Override
	public Map<Direction, Integer> getRoads(final Point location) {
		final Map<Direction, Integer> retval = roadsMap.get(location);
		if (Objects.isNull(retval)) {
			return Collections.emptyMap();
		} else {
			return Collections.unmodifiableMap(retval);
		}
	}

	@Override
	public void setRoadLevel(final Point point, final Direction direction, final int quality) {
		if (direction == Direction.Nowhere) {
			return;
		} else if (quality < 0) {
			throw new IllegalArgumentException("Road quality must be nonnegative");
		}
		modified = true; // TODO: Only if this is a change
		final Map<Direction, Integer> temp = roadsMap.get(point);
		final Map<Direction, Integer> roadsAtPoint;
		if (Objects.isNull(temp)) {
			roadsAtPoint = new EnumMap<>(Direction.class);
		} else {
			roadsAtPoint = temp;
		}
		roadsAtPoint.put(direction, quality);
		roadsMap.put(point, roadsAtPoint);
	}

	/**
	 * The tile fixtures (other than rivers and mountains) at the given location.
	 */
	@Override
	public Collection<TileFixture> getFixtures(final Point location) {
		final Collection<TileFixture> retval = fixturesMap.get(location);
		if (Objects.isNull(retval)) {
			return Collections.emptyList();
		} else {
			return Collections.unmodifiableCollection(retval);
		}
	}

	/**
	 * The current player.
	 */
	@Override
	public Player getCurrentPlayer() {
		return playerCollection.getCurrentPlayer();
	}

	@Override
	public void setCurrentPlayer(final Player currentPlayer) {
		// FIXME: Should set 'modified' flag
		playerCollection.setCurrentPlayer(currentPlayer);
	}

	@Override
	public Set<Point> getBookmarksFor(final Player player) {
		return bookmarksImpl.entrySet().stream().filter(e -> e.getValue().contains(player))
			.map(Map.Entry::getKey).collect(Collectors.toSet());
	}

	@Override
	public Set<Point> getBookmarks() {
		return getBookmarksFor(getCurrentPlayer());
	}

	@Override
	public Collection<Player> getAllBookmarks(final Point location) {
		final Collection<Player> retval = bookmarksImpl.get(location);
		if (Objects.isNull(retval)) {
			return Collections.emptyList();
		} else {
			return Collections.unmodifiableCollection(retval);
		}
	}

	@Override
	public void addBookmark(final Point point, final Player player) {
		modified = true; // TODO: Only if this is a change
		final Set<Player> temp = bookmarksImpl.get(point);
		final Set<Player> marks;
		if (Objects.isNull(temp)) {
			marks = new HashSet<>();
		} else {
			marks = temp;
		}
		marks.add(player);
		bookmarksImpl.put(point, marks);
	}

	@Override
	public void removeBookmark(final Point point, final Player player) {
		modified = true; // TODO: Only if this is a change
		final Set<Player> marks = bookmarksImpl.get(point);
		if (!Objects.isNull(marks)) {
			marks.remove(player);
			if (marks.isEmpty()) {
				bookmarksImpl.remove(point);
			} else {
				bookmarksImpl.put(point, marks);
			}
		}
	}

	/**
	 * Add a player.
	 */
	@Override
	public void addPlayer(final Player player) {
		modified = true; // TODO: Only if this is a change
		playerCollection.add(player);
	}

	/**
	 * Add rivers at a location.
	 */
	@Override
	public void addRivers(final Point location, final River... addedRivers) {
		modified = true; // TODO: Only if this is a change
		final Set<River> set = riversMap.get(location);
		if (Objects.isNull(set)) {
			if (addedRivers.length > 0) {
				riversMap.put(location, EnumSet.of(addedRivers[0], addedRivers));
			}
		} else {
			set.addAll(Arrays.asList(addedRivers));
			riversMap.put(location, set);
		}
	}

	/**
	 * Remove rivers from the given location.
	 */
	@Override
	public void removeRivers(final Point location, final River... removedRivers) {
		modified = true; // TODO: Only if this is a change
		final Set<River> set = riversMap.get(location);
		if (!Objects.isNull(set)) {
			for (final River river : removedRivers) {
				set.remove(river);
			}
			if (set.isEmpty()) {
				riversMap.remove(location);
			} else {
				riversMap.put(location, set);
			}
		}
	}

	/**
	 * Add a fixture at a location, and return whether the "all fixtures at
	 * this point" set has an additional member as a result of this.
	 */
	@Override
	public boolean addFixture(final Point location, final TileFixture fixture) {
		if (fixture instanceof FakeFixture) {
			LovelaceLogger.error("Fake fixture passed to SPMapNG.addFixture()");
			LovelaceLogger.debug(new Exception("Fake fixture"), "Stack trace for fake fixture in SPMapNG.addFixture()");
			return false;
		}
		modified = true; // TODO: Only if this is a change
		final List<TileFixture> local;
		final List<TileFixture> temp = fixturesMap.get(location);
		if (Objects.isNull(temp)) {
			local = new ArrayList<>();
		} else {
			local = temp;
		}
		final Optional<TileFixture> existing = local.stream()
			.filter(f -> f.getId() == fixture.getId()).findAny();
		if (fixture.getId() >= 0 && existing.isPresent()) {
			if (existing.get().equals(fixture) || subsetCheck(existing.get(), fixture)) {
				local.remove(existing.get());
				local.add(fixture);
				fixturesMap.put(location, local);
				// The return value is primarily used by {@link
				// FixtureListModel}, which won't care about
				// differences, but would end up with double
				// entries if we returned true here.
				return false;
			} else {
				local.add(fixture);
				fixturesMap.put(location, local);
				LovelaceLogger.warning("Inserted duplicate-ID fixture at %s", location);
				LovelaceLogger.debug(new Exception("Duplicate ID"), "Stack trace of this location: ");
				LovelaceLogger.info("Existing fixture was: %s", existing.get().getShortDescription());
				LovelaceLogger.info("Added: %s", fixture.getShortDescription());
				return true;
			}
		} else {
			final int oldSize = local.size();
			local.add(fixture);
			fixturesMap.put(location, local);
			return oldSize < fixturesMap.get(location).size();
		}
	}

	/**
	 * Remove a fixture from a location.
	 */
	@Override
	public void removeFixture(final Point location, final TileFixture fixture) {
		modified = true; // TODO: Only if this is a change
		final List<TileFixture> local = fixturesMap.get(location);
		if (!Objects.isNull(local)) {
			local.remove(fixture);
			if (local.isEmpty()) {
				fixturesMap.remove(location);
			} else {
				fixturesMap.put(location, local);
			}
		}
	}

	@Override
	public int hashCode() {
		return getDimensions().hashCode() + currentTurn << 3 + getCurrentPlayer().hashCode() << 5;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof final ILegacyMap that) {
			if (getDimensions().equals(that.getDimensions()) &&
				getPlayers().containsAll(that.getPlayers()) &&
				that.getPlayers().containsAll(getPlayers()) &&
				currentTurn == that.getCurrentTurn() &&
				getCurrentPlayer().equals(that.getCurrentPlayer())) {
				for (final Point point : getLocations()) {
					if (getBaseTerrain(point) != that.getBaseTerrain(point) ||
						isMountainous(point) != that.isMountainous(point) ||
						!getRivers(point).equals(
							that.getRivers(point)) ||
						!getFixtures(point).containsAll(that.getFixtures(point)) ||
						!that.getFixtures(point).containsAll(getFixtures(point)) ||
						!getRoads(point).equals(that.getRoads(point))) {
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("SPMapNG:").append(System.lineSeparator())
			.append("Map version: ").append(getDimensions().version())
			.append(System.lineSeparator())
			.append("Rows: ").append(getDimensions().rows()).append(System.lineSeparator())
			.append("Columns: ").append(getDimensions().columns())
			.append(System.lineSeparator())
			.append("Current Turn: ").append(currentTurn).append(System.lineSeparator())
			.append("Players:").append(System.lineSeparator());
		for (final Player player : getPlayers()) {
			builder.append(player.toString());
			if (player.isCurrent()) {
				builder.append(" (current)");
			}
			builder.append(System.lineSeparator());
		}
		builder.append(System.lineSeparator());
		builder.append("Contents:").append(System.lineSeparator());
		for (final Point location : getLocations()) {
			if (isLocationEmpty(location)) {
				continue;
			}
			builder.append("At ").append(location);
			final TileType tileTerrain = terrain.get(location);
			if (!Objects.isNull(tileTerrain)) {
				builder.append("terrain: ").append(tileTerrain).append(", ");
			}
			if (isMountainous(location)) {
				builder.append("mountains, ");
			}
			if (!getRivers(location).isEmpty()) {
				builder.append("rivers: ");
				builder.append(getRivers(location).stream().map(Object::toString)
					.collect(Collectors.joining(" ")));
				builder.append(", ");
			}
			final Collection<TileFixture> localFixtures =
				fixturesMap.get(location);
			if (!Objects.isNull(localFixtures)) {
				builder.append("fixtures: ").append(System.lineSeparator());
				builder.append(localFixtures.stream().map(Object::toString)
					.collect(Collectors.joining(System.lineSeparator())));
			}
			builder.append(System.lineSeparator());
		}
		return builder.toString();
	}

	// FIXME: Inline this into all callers, or figure out what's missing
	// FIXME: Remove 'movedFrom' once that's converted to a member function
	private static <Target extends IFixture, SubsetType extends Subsettable<Target>>
	boolean testAgainstList(final Target desideratum, final Point location,
							final Collection<Pair<SubsetType, Point>> list, final Consumer<String> ostream,
							final BiPredicate<Point, TileFixture> movedFrom) {
		int count = 0;
		boolean unmatched = true;
		@Nullable SubsetType match = null;
		@Nullable Point matchPoint = null;
		boolean exactly = false;
		for (final Pair<SubsetType, Point> pair : list) {
			count++;
			match = pair.getValue0();
			matchPoint = pair.getValue1();
			if (match.equals(desideratum)) {
				exactly = true;
				break;
			} else if (match.isSubset(desideratum, x -> {
			})) {
				unmatched = false;
				break;
			}
		}
		boolean retval = true;
		if (exactly || count == 1) {
			if (!matchPoint.equals(location)) {
				final String idStr;
				if (match instanceof final IUnit unit && unit.owner().isIndependent()) {
					idStr = " (ID #" + unit.getId();
				} else {
					idStr = "";
				}
				ostream.accept(String.format("%s%s apparently moved from our %s to %s",
					match, idStr, matchPoint, location));
				retval = false;
			}
			retval = match.isSubset(desideratum, ostream) && retval;
		} else if (desideratum instanceof final TileFixture tf && movedFrom.test(location, tf)) {
			retval = false;
		} else if (count == 0) {
			retval = false;
			ostream.accept("Extra fixture:\t" + desideratum);
		} else if (unmatched) {
			ostream.accept(String.format(
				"Fixture with ID #%d didn't match any of the subsettable fixtures sharing that ID",
				desideratum.getId()));
			retval = false;
		}
		return retval;
	}

	/**
	 * Returns true if the other map is a "strict subset" of this one,
	 * except for those cases we deliberately ignore.
	 */
	@Override
	public boolean isSubset(final ILegacyMap obj, final Consumer<String> report) {
		if (getDimensions().equals(obj.getDimensions())) {
			boolean retval = playerCollection.isSubset(obj.getPlayers(), report);
			// Declared here to avoid object allocations in the loop.
			final List<TileFixture> ourFixtures = new ArrayList<>();
			// TODO: Use Guava Multimap for this
			final Map<Integer, List<Pair<Subsettable<IFixture>, Point>>> ourSubsettables =
				new HashMap<>(50, 0.4f);
			final Map<TileFixture, Point> ourLocations = fixturesMap.entrySet().stream()
				.flatMap(e -> e.getValue().stream().map(f -> Pair.with(f, e.getKey())))
				.collect(Collectors.toMap(Pair::getValue0, Pair::getValue1));
			// IUnit is Subsettable<IUnit> and thus incompatible with SubsettableFixture // FIXME: No longer true
			final Map<Integer, List<Pair<IUnit, Point>>> ourUnits = new HashMap<>(50, 0.4f);
			// AbstractTown is Subsettable<AbstractTown>
			final Map<Integer, List<Pair<AbstractTown, Point>>> ourTowns = new HashMap<>(50, 0.4f);

			for (final Map.Entry<TileFixture, Point> entry : ourLocations.entrySet()) {
				final Point point = entry.getValue();
				final TileFixture fixture = entry.getKey();
				if (fixture instanceof final IUnit unit) {
					final List<Pair<IUnit, Point>> list;
					final List<Pair<IUnit, Point>> temp =
						ourUnits.get(fixture.getId());
					if (Objects.isNull(temp)) {
						list = new ArrayList<>();
					} else {
						list = temp;
					}
					list.add(Pair.with(unit, point));
					ourUnits.put(fixture.getId(), list);
				} else if (fixture instanceof final AbstractTown town) {
					final List<Pair<AbstractTown, Point>> list;
					final List<Pair<AbstractTown, Point>> temp =
						ourTowns.get(fixture.getId());
					if (Objects.isNull(temp)) {
						list = new ArrayList<>();
					} else {
						list = temp;
					}
					list.add(Pair.with(town, point));
					ourTowns.put(fixture.getId(), list);
				} else if (fixture instanceof Subsettable) {
					final List<Pair<Subsettable<IFixture>, Point>> list;
					final List<Pair<Subsettable<IFixture>, Point>> temp =
						ourSubsettables.get(fixture.getId());
					if (Objects.isNull(temp)) {
						list = new ArrayList<>();
					} else {
						list = temp;
					}
					list.add(Pair.with((Subsettable<IFixture>) fixture, point));
					ourSubsettables.put(fixture.getId(), list);
				}
			}

			// TODO: Convert to member function
			final BiPredicate<Point, TileFixture> movedFrom = (point, fixture) -> {
				final Point tPoint = ourLocations.get(fixture);
				if (!Objects.isNull(tPoint) && !tPoint.equals(point)) {
					report.accept(String.format("%s moved from our %s to %s",
						fixture, tPoint,
						point));
					return true;
				} else {
					return false;
				}
			};

			for (final Point point : getLocations()) {
				final Consumer<String> localReport =
					str -> report.accept(String.format(
						"At %s:\t%s", point.toString(), str));
				final TileType theirTerrain = obj.getBaseTerrain(point);
				final TileType ourTerrain = terrain.get(point);
				if (!Objects.isNull(theirTerrain)) {
					if (!Objects.isNull(ourTerrain)) {
						if (ourTerrain != theirTerrain) {
							localReport.accept("Base terrain differs");
							retval = false;
							continue;
						} else if (!getRivers(point).isEmpty() &&
							obj.getRivers(point).isEmpty()) {
							localReport.accept("Has terrain but not our rivers");
						}
					} else {
						localReport.accept("Has terrain information we don't");
						retval = false;
						continue;
					}
				}
				if (obj.isMountainous(point) && !isMountainous(point)) {
					localReport.accept("Has mountains we don't");
					retval = false; // return false;
				}
				ourFixtures.clear();
				for (final TileFixture fixture : getFixtures(point)) {
					final int idNum = fixture.getId();
					// FIXME: Should add to ourUnits, ourTowns, etc, if of the right type and not in those, right?
					if (fixture instanceof IUnit && ourUnits.containsKey(idNum)) {
						continue;
					} else if (fixture instanceof AbstractTown &&
						ourTowns.containsKey(idNum)) {
						continue;
					} else { // FIXME: Also check ourSubsettables, right?
						ourFixtures.add(fixture);
					}
				}
				final Collection<TileFixture> theirFixtures = obj.getFixtures(point);
				for (final TileFixture fixture : theirFixtures) {
					final List<Pair<IUnit, Point>> unitLocs = ourUnits.get(fixture.getId());
					final List<Pair<AbstractTown, Point>> townLocs = ourTowns.get(fixture.getId());
					final List<Pair<Subsettable<IFixture>, Point>> subsetLocs = ourSubsettables.get(fixture.getId());
					if (ourFixtures.contains(fixture) || fixture.subsetShouldSkip()) {
						continue;
					} else if (fixture instanceof IUnit &&
						!Objects.isNull(unitLocs)) {
						retval = testAgainstList(fixture, point,
							unitLocs, localReport, movedFrom) && retval;
					} else if (fixture instanceof final AbstractTown town &&
						!Objects.isNull(townLocs)) {
						retval = testAgainstList(town, point,
							townLocs, localReport, movedFrom)
							&& retval;
					} else if (fixture instanceof Subsettable &&
						!Objects.isNull(subsetLocs)) {
						retval = testAgainstList(fixture, point,
							subsetLocs,
							localReport, movedFrom) && retval;
					} else if (movedFrom.test(point, fixture)) {
						retval = false; // return false;
					} else {
						localReport.accept("Extra fixture:\t" + fixture);
						retval = false; // return false;
					}
				}
				if (!getRivers(point).containsAll(obj.getRivers(point))) {
					localReport.accept("Extra river(s)");
					retval = false; // return false;
					break;
				}
				final Map<Direction, Integer> theirRoads = obj.getRoads(point);
				final Map<Direction, Integer> ourRoads = getRoads(point);
				// TODO: Extract road-subset method
				for (final Map.Entry<Direction, Integer> entry : theirRoads.entrySet()) {
					if (ourRoads.getOrDefault(entry.getKey(), 0) >= entry.getValue()) {
						continue;
					} else {
						localReport.accept("Has road information we don't");
						retval = false;
						break;
					}
				}
			}
			return retval;
		} else {
			report.accept("Dimension mismatch");
			return false;
		}
	}

	/**
	 * Clone a map, possibly for a specific player, who shouldn't see other players' details.
	 *
	 * TODO: What about filename and modified flag?
	 *
	 * FIXME: (In the interface) split with-player and without-player into separate signatures
	 */
	@Override
	public ILegacyMap copy(final IFixture.CopyBehavior zero, final @Nullable Player player) {
		// FIXME: Should declare as SPMapNG and use collection bulk-add methods
		final IMutableLegacyMap retval = new LegacyMap(getDimensions(), playerCollection.copy(),
			currentTurn);
		for (final Point point : getLocations()) {
			final TileType tileType = terrain.get(point);
			if (!Objects.isNull(tileType)) {
				retval.setBaseTerrain(point, tileType);
			}
			retval.setMountainous(point, isMountainous(point));
			retval.addRivers(point, getRivers(point).toArray(River[]::new));
			// TODO: what other fixtures should we zero, or skip?
			for (final TileFixture fixture : getFixtures(point)) {
				final IFixture.CopyBehavior cb;
				if (zero == IFixture.CopyBehavior.ZERO) {
					cb = IFixture.CopyBehavior.ZERO;
				} else if (shouldZero(fixture, player)) {
					cb = IFixture.CopyBehavior.ZERO;
				} else {
					cb = IFixture.CopyBehavior.KEEP;
				}
				retval.addFixture(point, fixture.copy(cb));
			}
		}
		return retval;
	}

	@Override
	public void replace(final Point location, final TileFixture original, final TileFixture replacement) {
		modified = true; // TODO: Only if this is a change
		if (getFixtures(location).contains(replacement) && !original.equals(replacement)) {
			removeFixture(location, original);
		} else {
			final List<TileFixture> existing = new ArrayList<>(getFixtures(location));
			int index = 0;
			boolean replaced = false;
			for (final TileFixture item : existing) {
				if (original.equals(item)) {
					fixturesMap.get(location).set(index, replacement);
					replaced = true;
					break;
				}
				index++;
			}
			if (!replaced) {
				addFixture(location, replacement);
			}
		}
	}
}
