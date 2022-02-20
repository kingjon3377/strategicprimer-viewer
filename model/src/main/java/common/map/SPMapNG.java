package common.map;

import common.map.fixtures.mobile.IUnit;

import java.util.Arrays;
import org.javatuples.Pair;

import java.nio.file.Path;

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

import java.util.logging.Logger;
import java.util.logging.Level;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import java.util.stream.Stream;

import java.util.function.Consumer;
import java.util.function.BiPredicate;

import common.map.fixtures.towns.AbstractTown;

import org.jetbrains.annotations.Nullable;

/**
 * A class to represent a game-world map and its contents.
 */
public class SPMapNG implements IMutableMapNG {
	private static final Logger LOGGER = Logger.getLogger(SPMapNG.class.getName());
	/**
	 * Whether the given fixture should be zeroed out if the map is for the given player.
	 */
	private static boolean shouldZero(final TileFixture fixture, final @Nullable Player player) {
		if (player != null && fixture instanceof HasOwner) {
			return player.equals(((HasOwner) fixture).getOwner());
		} else {
			return true;
		}
	}

	/**
	 * If either of the provided fixtures is a subset of the other, return
	 * true; otherwise return false.
	 */
	private static boolean subsetCheck(final TileFixture one, final TileFixture two) {
		if (one instanceof SubsettableFixture && ((SubsettableFixture) one).isSubset(two, x -> {})) {
			return true;
		} else {
			return two instanceof SubsettableFixture && ((SubsettableFixture) two).isSubset(one, x -> {});
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
	private final Set<Point> mountains = new HashSet<>();

	/**
	 * The base terrain at points in the map.
	 */
	private final Map<Point, TileType> terrain = new HashMap<>();

	/**
	 * The players in the map.
	 */
	private final IMutablePlayerCollection playerCollection;

	/**
	 * Fixtures at various points, other than the main ground and forest.
	 */
	// TODO: Use Guava multimap?
	private final Map<Point, List<TileFixture>> fixturesMap = new HashMap<>();

	/**
	 * The version and dimensions of the map.
	 */
	private final MapDimensions mapDimensions;

	/**
	 * The rivers in the map.
	 */
	private final Map<Point, Set<River>> riversMap = new HashMap<>();

	/**
	 * Roads in the map.
	 */
	private final Map<Point, Map<Direction, Integer>> roadsMap = new HashMap<>();

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
	private final Map<Point, Set<Player>> bookmarksImpl = new HashMap<>();

	public SPMapNG(final MapDimensions dimensions, final IMutablePlayerCollection players, final int turn) {
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
	public IPlayerCollection getPlayers() {
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
		if (item == null) {
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
		if (riversMap.containsKey(location)) {
			return Collections.unmodifiableSet(riversMap.get(location));
		} else {
			return Collections.emptySet();
		}
	}

	/**
	 * The directions and quality levels of roads at various locations.
	 */
	@Override
	public Map<Direction, Integer> getRoads(final Point location) {
		if (roadsMap.containsKey(location)) {
			return Collections.unmodifiableMap(roadsMap.get(location));
		} else {
			return Collections.emptyMap();
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
		if (roadsMap.containsKey(point)) {
			roadsMap.get(point).put(direction, quality);
		} else {
			final Map<Direction, Integer> roadsAtPoint = new EnumMap<>(Direction.class);
			roadsAtPoint.put(direction, quality);
			roadsMap.put(point, roadsAtPoint);
		}
	}

	/**
	 * The tile fixtures (other than rivers and mountains) at the given location.
	 */
	@Override
	public Collection<TileFixture> getFixtures(final Point location) {
		if (fixturesMap.containsKey(location)) {
			return Collections.unmodifiableCollection(fixturesMap.get(location));
		} else {
			return Collections.emptyList();
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
		if (bookmarksImpl.containsKey(location)) {
			return Collections.unmodifiableCollection(bookmarksImpl.get(location));
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public void addBookmark(final Point point, final Player player) {
		modified = true; // TODO: Only if this is a change
		final Set<Player> marks;
		if (bookmarksImpl.containsKey(point)) {
			marks = bookmarksImpl.get(point);
		} else {
			marks = new HashSet<>();
		}
		marks.add(player);
		bookmarksImpl.put(point, marks);
	}

	@Override
	public void removeBookmark(final Point point, final Player player) {
		modified = true; // TODO: Only if this is a change
		if (bookmarksImpl.containsKey(point)) {
			final Set<Player> marks = bookmarksImpl.get(point);
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
		final Set<River> set;
		if (riversMap.containsKey(location)) {
			set = riversMap.get(location);
		} else {
			set = EnumSet.noneOf(River.class); // TODO: Use EnumSet.of() rather than adding one by one below
		}
		set.addAll(Arrays.asList(addedRivers));
		riversMap.put(location, set);
	}

	/**
	 * Remove rivers from the given location.
	 */
	@Override
	public void removeRivers(final Point location, final River... removedRivers) {
		modified = true; // TODO: Only if this is a change
		if (riversMap.containsKey(location)) {
			final Set<River> set = riversMap.get(location);
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
			LOGGER.severe("Fake fixture passed to SPMapNG.addFixture()");
			LOGGER.log(Level.FINE, "Stack trace for fake fixture in SPMapNG.addFixture()",
				new Exception());
			return false;
		}
		modified = true; // TODO: Only if this is a change
		final List<TileFixture> local;
		if (fixturesMap.containsKey(location)) {
			local = fixturesMap.get(location);
		} else {
			local = new ArrayList<>();
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
				LOGGER.warning("Inserted duplicate-ID fixture at " + location);
				LOGGER.log(Level.INFO, "Stack trace of this location: ", new Exception());
				LOGGER.info("Existing fixture was: " + existing.get().getShortDescription());
				LOGGER.info("Added: " + fixture.getShortDescription());
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
		if (fixturesMap.containsKey(location)) {
			final List<TileFixture> local = fixturesMap.get(location);
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
		if (obj instanceof IMapNG) {
			if (getDimensions().equals(((IMapNG) obj).getDimensions()) &&
					getPlayers().containsAll(((IMapNG) obj).getPlayers()) &&
					((IMapNG) obj).getPlayers().containsAll(getPlayers()) &&
					currentTurn == ((IMapNG) obj).getCurrentTurn() &&
					getCurrentPlayer().equals(((IMapNG) obj).getCurrentPlayer())) {
				for (final Point point : getLocations()) {
					if (getBaseTerrain(point) != ((IMapNG) obj).getBaseTerrain(point) ||
							isMountainous(point) !=
								((IMapNG) obj).isMountainous(point) ||
							!getRivers(point).equals(
								((IMapNG) obj).getRivers(point)) ||
							!getFixtures(point).containsAll(
								((IMapNG) obj).getFixtures(point)) ||
							!((IMapNG) obj).getFixtures(point).containsAll(
								getFixtures(point)) ||
							!getRoads(point).equals(
								((IMapNG) obj).getRoads(point))) {
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
			.append("Map version: ").append(getDimensions().getVersion())
			.append(System.lineSeparator())
			.append("Rows: ").append(getDimensions().getRows()).append(System.lineSeparator())
			.append("Columns: ").append(getDimensions().getColumns())
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
			if (terrain.containsKey(location)) {
				builder.append("terrain: ").append(terrain.get(location)).append(", ");
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
			if (fixturesMap.containsKey(location)) {
				builder.append("fixtures: ").append(System.lineSeparator());
				builder.append(fixturesMap.get(location).stream().map(Object::toString)
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
			} else if (match.isSubset(desideratum, x -> {})) {
				unmatched = false;
				break;
			} else {
				unmatched = true;
			}
		}
		boolean retval = true;
		if (exactly || count == 1) {
			if (!matchPoint.equals(location)) {
				final String idStr;
				if (match instanceof IUnit && ((IUnit) match).getOwner().isIndependent()) {
					idStr = " (ID #" + ((IUnit) match).getId();
				} else {
					idStr = "";
				}
				ostream.accept(String.format("%s%s apparently moved from our %s to %s",
					match.toString(), idStr, matchPoint.toString(), location.toString()));
				retval = false;
			}
			retval = match.isSubset(desideratum, ostream) && retval;
		} else if (desideratum instanceof TileFixture &&
				movedFrom.test(location, (TileFixture) desideratum)) {
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
	public boolean isSubset(final IMapNG obj, final Consumer<String> report) {
		if (getDimensions().equals(obj.getDimensions())) {
			boolean retval = playerCollection.isSubset(obj.getPlayers(), report);
			// Declared here to avoid object allocations in the loop.
			final List<TileFixture> ourFixtures = new ArrayList<>();
			// TODO: Use Guava Multimap for this
			final Map<Integer, List<Pair<Subsettable<IFixture>, Point>>> ourSubsettables =
				new HashMap<>();
			final Map<TileFixture, Point> ourLocations = fixturesMap.entrySet().stream()
				.flatMap(e -> e.getValue().stream().map(f -> Pair.with(f, e.getKey())))
				.collect(Collectors.toMap(Pair::getValue0, Pair::getValue1));
			// IUnit is Subsettable<IUnit> and thus incompatible with SubsettableFixture // FIXME: No longer true
			final Map<Integer, List<Pair<IUnit, Point>>> ourUnits = new HashMap<>();
			// AbstractTown is Subsettable<AbstractTown>
			final Map<Integer, List<Pair<AbstractTown, Point>>> ourTowns = new HashMap<>();

			for (final Map.Entry<TileFixture, Point> entry : ourLocations.entrySet()) {
				final Point point = entry.getValue();
				final TileFixture fixture = entry.getKey();
				if (fixture instanceof IUnit) {
					final List<Pair<IUnit, Point>> list;
					if (ourUnits.containsKey(fixture.getId())) {
						list = ourUnits.get(fixture.getId());
					} else {
						list = new ArrayList<>();
					}
					list.add(Pair.with((IUnit) fixture, point));
					ourUnits.put(fixture.getId(), list);
				} else if (fixture instanceof AbstractTown) {
					final List<Pair<AbstractTown, Point>> list;
					if (ourTowns.containsKey(fixture.getId())) {
						list = ourTowns.get(fixture.getId());
					} else {
						list = new ArrayList<>();
					}
					list.add(Pair.with((AbstractTown) fixture, point));
					ourTowns.put(fixture.getId(), list);
				} else if (fixture instanceof Subsettable) {
					final List<Pair<Subsettable<IFixture>, Point>> list;
					if (ourSubsettables.containsKey(fixture.getId())) {
						list = ourSubsettables.get(fixture.getId());
					} else {
						list = new ArrayList<>();
					}
					list.add(Pair.with((Subsettable<IFixture>) fixture, point));
					ourSubsettables.put(fixture.getId(), list);
				}
			}

			// TODO: Convert to member function
			final BiPredicate<Point, TileFixture> movedFrom = (point, fixture) -> {
				if (ourLocations.containsKey(fixture) &&
						!ourLocations.get(fixture).equals(point)) {
					report.accept(String.format("%s moved from our %s to %s",
						fixture.toString(), ourLocations.get(fixture).toString(),
						point.toString()));
					return true;
				} else {
					return false;
				}
			};

			for (final Point point : getLocations()) {
				final Consumer<String> localReport =
					str -> report.accept(String.format(
						"At %s:\t%s", point.toString(), str));
				if (obj.getBaseTerrain(point) != null) {
					if (terrain.containsKey(point)) {
						if (getBaseTerrain(point) != obj.getBaseTerrain(point)) {
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
					if (ourFixtures.contains(fixture) || fixture.subsetShouldSkip()) {
						continue;
					} else if (fixture instanceof IUnit &&
							ourUnits.containsKey(fixture.getId())) {
						retval = testAgainstList(fixture, point,
							ourUnits.get(fixture.getId()), localReport, movedFrom) && retval;
					} else if (fixture instanceof AbstractTown &&
							ourTowns.containsKey(fixture.getId())) {
						retval = testAgainstList((AbstractTown) fixture, point,
								ourTowns.get(fixture.getId()), localReport, movedFrom)
							&& retval;
					} else if (fixture instanceof Subsettable &&
							ourSubsettables.containsKey(fixture.getId())) {
						retval = testAgainstList(fixture, point,
							ourSubsettables.get(fixture.getId()),
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
					if (ourRoads.containsKey(entry.getKey()) &&
							ourRoads.get(entry.getKey()) >= entry.getValue()) {
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
	public IMapNG copy(final IFixture.CopyBehavior zero, final @Nullable Player player) {
		final IMutableMapNG retval = new SPMapNG(getDimensions(), playerCollection.copy(),
			currentTurn);
		for (final Point point : getLocations()) {
			if (terrain.containsKey(point)) {
				retval.setBaseTerrain(point, terrain.get(point));
			}
			retval.setMountainous(point, isMountainous(point));
			retval.addRivers(point, getRivers(point).toArray(new River[0]));
			// TODO: what other fixtures should we zero, or skip?
			for (final TileFixture fixture : getFixtures(point)) {
				IFixture.CopyBehavior cb;
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
