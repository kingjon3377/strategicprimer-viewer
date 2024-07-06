package utility;

import legacy.map.SubsettableFixture;

import java.util.Collection;
import java.util.Objects;
import java.util.Random;
import java.util.function.Predicate;

import lovelace.util.LovelaceLogger;
import lovelace.util.SingletonRandom;
import org.jetbrains.annotations.Nullable;

import legacy.map.TileType;

import java.util.stream.Collectors;
import java.util.Optional;

import legacy.map.River;
import legacy.map.fixtures.FixtureIterable;
import legacy.map.fixtures.UnitMember;
import legacy.map.fixtures.FortressMember;

import java.util.Collections;
import java.util.LinkedList;
import java.util.function.Consumer;

import org.javatuples.Quartet;

import java.nio.file.Path;
import java.util.Map;

import legacy.map.Direction;
import legacy.map.HasExtent;
import legacy.map.HasOwner;
import legacy.map.HasPopulation;
import legacy.map.IFixture;
import legacy.map.ILegacyMap;
import legacy.map.IMutableLegacyMap;
import legacy.map.Player;
import legacy.map.Point;
import legacy.map.TileFixture;

import drivers.common.SimpleMultiMapModel;
import drivers.common.IDriverModel;

import java.util.List;
import java.util.ArrayList;

import legacy.map.fixtures.Ground;

import legacy.map.fixtures.resources.CacheFixture;

import legacy.map.fixtures.mobile.Animal;
import legacy.map.fixtures.mobile.IMutableUnit;
import legacy.map.fixtures.mobile.IUnit;

import legacy.map.fixtures.terrain.Forest;

import legacy.map.fixtures.towns.IMutableFortress;
import legacy.map.fixtures.towns.ITownFixture;

import exploration.common.Speed;
import exploration.common.SurroundingPointIterable;
import exploration.common.SimpleMovementModel;

/**
 * A driver model for the various utility drivers.
 */
public class UtilityDriverModel extends SimpleMultiMapModel {
	private static <Desired, Provided> Consumer<Provided> ifApplicable(final Consumer<Desired> func,
	                                                                   final Class<Desired> cls) {
		return (item) -> {
			if (cls.isInstance(item)) {
				func.accept(cls.cast(item));
			}
		};
	}

	private static boolean isSubset(final IFixture one, final IFixture two) {
		if (one instanceof final SubsettableFixture sf) {
			return sf.isSubset(two, s -> {
			});
		} else {
			return one.equals(two);
		}
	}

	private record Mock(Player owner) implements HasOwner {
	}

	public UtilityDriverModel(final IMutableLegacyMap map) {
		super(map);
	}

	// TODO: Make protected/private and provide static copyConstructor() instead?
	public UtilityDriverModel(final IDriverModel model) {
		super(model);
	}

	/**
	 * Copy rivers at the given location missing from subordinate
	 * maps, where they have other terrain information, from the main map.
	 */
	public void copyRiversAt(final Point location) {
		final ILegacyMap map = getMap();
		for (final IMutableLegacyMap subordinateMap : getRestrictedSubordinateMaps()) {
			final TileType mainTerrain = map.getBaseTerrain(location);
			final TileType subTerrain = subordinateMap.getBaseTerrain(location);
			if (!Objects.isNull(mainTerrain) && !Objects.isNull(subTerrain) && mainTerrain == subTerrain &&
					!map.getRivers(location).isEmpty() &&
					subordinateMap.getRivers(location).isEmpty()) {
				subordinateMap.addRivers(location,
						map.getRivers(location).toArray(River[]::new));
				subordinateMap.setModified(true);
			}
		}
	}

	/**
	 * Conditionally remove duplicate fixtures. Returns a list of fixtures
	 * that would be removed and a callback to do the removal; the initial
	 * caller of this method asks the user for approval.
	 */
	public Iterable<Quartet<Consumer<TileFixture>, @Nullable Path, TileFixture, Iterable<? extends TileFixture>>>
	conditionallyRemoveDuplicates(final Point location) {
		final List<Quartet<Consumer<TileFixture>, @Nullable Path, TileFixture, Iterable<? extends TileFixture>>>
				duplicatesList = new ArrayList<>();
		final List<TileFixture> checked = new ArrayList<>();
		final Predicate<TileFixture> noneMatch =
				item -> checked.stream().noneMatch(inner -> item == inner);
		for (final IMutableLegacyMap map : getRestrictedAllMaps()) {
			for (final TileFixture fixture : map.getFixtures(location)) {
				checked.add(fixture);
				switch (fixture) {
					case final IUnit u when u.getKind().contains("TODO") -> {
						continue;
					}
					case final CacheFixture cacheFixture -> {
						continue;
					}
					case final HasPopulation<?> hp when hp.getPopulation() > 0 -> {
						continue;
					}
					case final HasExtent<?> he when he.getAcres().doubleValue() > 0.0 -> {
						continue;
					}
					default -> {
					}
				}
				final List<TileFixture> matching = map.getFixtures(location).stream()
						.filter(noneMatch)
						.filter(fixture::equalsIgnoringID)
						.collect(Collectors.toList());
				if (!matching.isEmpty()) {
					duplicatesList.add(Quartet.with(
							item -> map.removeFixture(location, item),
							map.getFilename(), fixture, matching));
				}
			}
		}
		return duplicatesList;
	}

	private static List<Quartet<Runnable, String, String, Collection<? extends IFixture>>> coalesceImpl(
			final String context, final Iterable<? extends IFixture> stream, final Consumer<IFixture> add,
			final Consumer<IFixture> remove, final Runnable setModFlag,
			final Map<Class<? extends IFixture>, CoalescedHolder<? extends IFixture, ?>> handlers) {
		final List<Quartet<Runnable, String, String, Collection<? extends IFixture>>> retval =
				new ArrayList<>();
		for (final IFixture fixture : stream) {
			switch (fixture) {
				case final FixtureIterable<?> fixtureIterable -> {
					final String shortDesc = (fixture instanceof final TileFixture tf) ?
							tf.getShortDescription() : fixture.toString();
					switch (fixtureIterable) {
						case final IMutableUnit u -> retval.addAll(coalesceImpl(
								"%sIn %s: ".formatted(context, shortDesc),
								u,
								ifApplicable(u::addMember, UnitMember.class),
								ifApplicable(u::removeMember, UnitMember.class),
								setModFlag, handlers));
						case final IMutableFortress f -> retval.addAll(coalesceImpl(
								"%sIn %s: ".formatted(context, shortDesc), f,
								ifApplicable(f::addMember, FortressMember.class),
								ifApplicable(f::removeMember, FortressMember.class),
								setModFlag, handlers));
						default -> LovelaceLogger.warning("Unhandled fixture in coalesceImpl()");
					}
				}
				case final Animal a when !a.isTalking() && handlers.containsKey(Animal.class) ->
						handlers.get(Animal.class).addIfType(fixture);
				case final HasPopulation<?> hp when 0 > hp.getPopulation() -> {
				}
				case final HasExtent<?> he when 0.0 >= he.getAcres().doubleValue() -> {
				}
				default -> {
					if (handlers.containsKey(fixture.getClass())) {
						handlers.get(fixture.getClass()).addIfType(fixture);
					}
				}
			}
		}

		for (final CoalescedHolder<? extends IFixture, ?> handler : handlers.values()) {
			for (final List<? extends IFixture> list : handler) {
				if (list.isEmpty() || list.size() == 1) {
					continue;
				}
				retval.add(Quartet.with(() -> {
					final IFixture combined = handler.combineRaw(
							list.toArray(IFixture[]::new));
					list.forEach(remove);
					add.accept(combined);
					setModFlag.run();
				}, context, handler.getPlural().toLowerCase(), list));
			}
		}
		return retval;
	}

	/**
	 * Conditionally coalesce like resources in a location. Returns a list
	 * of resources that would be combined and a callback to do the
	 * operation; the initial caller of this ask the user for approval.
	 */
	public Collection<Quartet<Runnable, String, String, Collection<? extends IFixture>>>
	conditionallyCoalesceResources(final Point location,
	                               final Map<Class<? extends IFixture>,
			                               CoalescedHolder<? extends IFixture, ?>> handlers) {
		final List<Quartet<Runnable, String, String, Collection<? extends IFixture>>> retval = new ArrayList<>();
		for (final IMutableLegacyMap map : getRestrictedAllMaps()) {
			retval.addAll(coalesceImpl(
					"In %s: At %s: ".formatted(
							Optional.ofNullable(map.getFilename())
									.map(Path::toString).orElse("a new file"),
							location),
					map.getFixtures(location),
					ifApplicable(fix -> map.addFixture(location, fix), TileFixture.class),
					ifApplicable(fix -> map.removeFixture(location, fix), TileFixture.class),
					() -> map.setModified(true), handlers));
		}
		return retval;
	}

	/**
	 * Remove information in the main map from subordinate maps.
	 */
	public void subtractAtPoint(final Point location) {
		final ILegacyMap map = getMap();
		for (final IMutableLegacyMap subMap : getRestrictedSubordinateMaps()) {
			subMap.setModified(true);
			final TileType terrain = map.getBaseTerrain(location);
			final TileType ours = subMap.getBaseTerrain(location);
			if (!Objects.isNull(terrain) && !Objects.isNull(ours) && terrain == ours) {
				subMap.setBaseTerrain(location, null);
			}
			subMap.removeRivers(location,
					map.getRivers(location).toArray(River[]::new));
			final Map<Direction, Integer> mainRoads = map.getRoads(location);
			final Map<Direction, Integer> knownRoads = subMap.getRoads(location);
			for (final Map.Entry<Direction, Integer> entry : knownRoads.entrySet()) {
				final Direction direction = entry.getKey();
				final int road = entry.getValue();
				if (mainRoads.getOrDefault(direction, 0) >= road) {
					subMap.setRoadLevel(location, direction, 0);
				}
			}
			if (map.isMountainous(location)) {
				subMap.setMountainous(location, false);
			}
			final List<TileFixture> toRemove = new ArrayList<>();
			for (final TileFixture fixture : subMap.getFixtures(location)) {
				if (map.getFixtures(location).stream()
						.anyMatch(item -> isSubset(item, fixture))) {
					toRemove.add(fixture);
				}
			}
			for (final TileFixture fixture : toRemove) {
				subMap.removeFixture(location, fixture);
			}
		}
	}

	private static List<Forest> extractForests(final ILegacyMap map, final Point location) {
		return map.getFixtures(location).stream().filter(Forest.class::isInstance)
				.map(Forest.class::cast).collect(Collectors.toList());
	}

	private static List<Ground> extractGround(final ILegacyMap map, final Point location) {
		return map.getFixtures(location).stream().filter(Ground.class::isInstance)
				.map(Ground.class::cast).collect(Collectors.toList());
	}

	public void fixForestsAndGround(final Consumer<String> ostream) {
		for (final ILegacyMap map : getSubordinateMaps()) {
			ostream.accept("Starting %s".formatted(Optional.ofNullable(map.getFilename())
					.map(Path::toString).orElse("a map with no associated path")));

			for (final Point location : map.getLocations()) {
				final List<Forest> mainForests = extractForests(getMap(), location);
				final List<Forest> subForests = extractForests(map, location);
				for (final Forest forest : subForests) {
					if (mainForests.contains(forest)) {
						continue;
					}
					final Forest matching = mainForests.stream()
							.filter(forest::equalsIgnoringID).findAny().orElse(null);
					if (Objects.isNull(matching)) {
						ostream.accept("Unmatched forest in %s: %s".formatted(
								location, forest));
						getRestrictedMap().addFixture(location, forest.copy(IFixture.CopyBehavior.KEEP));
						setMapModified(true);
					} else {
						forest.setId(matching.getId());
						setMapModified(true);
					}
				}

				final List<Ground> mainGround = extractGround(getMap(), location);
				final List<Ground> subGround = extractGround(map, location);
				for (final Ground ground : subGround) {
					if (mainGround.contains(ground)) {
						continue;
					}
					final Ground matching = mainGround.stream()
							.filter(ground::equalsIgnoringID).findAny().orElse(null);
					if (Objects.isNull(matching)) {
						ostream.accept("Unmatched ground in %s: %s".formatted(
								location, ground));
						getRestrictedMap().addFixture(location, ground.copy(IFixture.CopyBehavior.KEEP));
						setMapModified(true);
					} else {
						ground.setId(matching.getId());
						setMapModified(true);
					}
				}
			}
		}
	}

	private static void safeAdd(final IMutableLegacyMap map, final Player currentPlayer, final Point point,
	                            final TileFixture fixture) {
		final Predicate<TileFixture> equality = fixture::equals;
		final Consumer<String> noop = x -> {
		};
		final Predicate<TileFixture> newSubsetOfOld =
				f -> f instanceof final SubsettableFixture sf && sf.isSubset(fixture, noop);
		if (map.getFixtures(point).stream().anyMatch(equality.or(newSubsetOfOld))) {
			return;
		}
		final IFixture.CopyBehavior cb;
		if (fixture instanceof final HasOwner owned && !(fixture instanceof ITownFixture) &&
				owned.owner().equals(currentPlayer)) {
			cb = IFixture.CopyBehavior.KEEP;
		} else {
			cb = IFixture.CopyBehavior.ZERO;
		}
		final TileFixture zeroed = fixture.copy(cb);
		final Predicate<TileFixture> zeroedEquals = zeroed::equals;
		final Predicate<TileFixture> zeroedSubsetOfOld =
				f -> f instanceof final SubsettableFixture sf && sf.isSubset(zeroed, noop);
		final Predicate<TileFixture> oldSubsetOfZeroed =
				f -> zeroed instanceof final SubsettableFixture sf && sf.isSubset(f, noop);
		if (map.getFixtures(point).stream().noneMatch(zeroedEquals.or(zeroedSubsetOfOld))) {
			final Optional<TileFixture> matching = map.getFixtures(point).stream().filter(oldSubsetOfZeroed).findAny();
			if (matching.isPresent()) {
				map.replace(point, matching.get(), fixture.copy(cb));
			} else {
				map.addFixture(point, fixture.copy(cb));
			}
		}
	}

	public void expandAroundPoint(final Point center, final Player currentPlayer) {
		final Mock mock = new Mock(currentPlayer);
		final ILegacyMap map = getMap();
		final long seed = SingletonRandom.SINGLETON_RANDOM.nextLong();
		for (final IMutableLegacyMap subMap : getRestrictedSubordinateMaps()) {
			if (!subMap.getCurrentPlayer().equals(currentPlayer)) {
				continue;
			}

			final Random rng = new Random(seed);

			for (final Point neighbor : new SurroundingPointIterable(center, subMap.getDimensions())) {
				if (Objects.isNull(subMap.getBaseTerrain(neighbor))) {
					subMap.setBaseTerrain(neighbor, map.getBaseTerrain(neighbor));
					if (map.isMountainous(neighbor)) {
						subMap.setMountainous(neighbor, true);
					}
				}
				final LinkedList<TileFixture> possibilities = new LinkedList<>();
				for (final TileFixture fixture : map.getFixtures(neighbor)) {
					if (fixture instanceof CacheFixture ||
							subMap.getFixtures(neighbor).contains(fixture)) {
						continue;
					} else if (SimpleMovementModel.shouldAlwaysNotice(mock, fixture)) {
						safeAdd(subMap, currentPlayer, neighbor, fixture);
					} else if (SimpleMovementModel.shouldSometimesNotice(mock,
							Speed.Careful, fixture)) {
						possibilities.add(fixture);
					}
				}
				Collections.shuffle(possibilities, rng);
				if (!possibilities.isEmpty()) {
					final TileFixture first = possibilities.removeFirst();
					safeAdd(subMap, currentPlayer, neighbor, first);
				}
			}
			subMap.setModified(true);
		}
	}
}
