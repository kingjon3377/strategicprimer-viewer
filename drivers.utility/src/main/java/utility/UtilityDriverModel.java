package utility;

import common.map.SubsettableFixture;
import java.util.Collection;
import java.util.Random;
import java.util.function.Predicate;
import java.util.logging.Logger;
import lovelace.util.SingletonRandom;
import org.jetbrains.annotations.Nullable;

import common.map.TileType;
import java.util.stream.Collectors;
import java.util.Optional;
import common.map.River;
import common.map.fixtures.FixtureIterable;
import common.map.fixtures.UnitMember;
import common.map.fixtures.FortressMember;
import java.util.Collections;
import java.util.LinkedList;
import java.util.function.Consumer;
import org.javatuples.Quartet;
import java.nio.file.Path;
import java.util.Map;
import common.map.Direction;
import common.map.HasExtent;
import common.map.HasOwner;
import common.map.HasPopulation;
import common.map.IFixture;
import common.map.IMapNG;
import common.map.IMutableMapNG;
import common.map.Player;
import common.map.Point;
import common.map.TileFixture;

import drivers.common.SimpleMultiMapModel;
import drivers.common.IDriverModel;

import java.util.List;
import java.util.ArrayList;

import common.map.fixtures.Ground;

import common.map.fixtures.resources.CacheFixture;

import common.map.fixtures.mobile.Animal;
import common.map.fixtures.mobile.IMutableUnit;
import common.map.fixtures.mobile.IUnit;

import common.map.fixtures.terrain.Forest;

import common.map.fixtures.towns.IMutableFortress;
import common.map.fixtures.towns.ITownFixture;

import exploration.common.Speed;
import exploration.common.SurroundingPointIterable;
import exploration.common.SimpleMovementModel;

/**
 * A driver model for the various utility drivers.
 */
public class UtilityDriverModel extends SimpleMultiMapModel {
	private static final Logger LOGGER = Logger.getLogger(UtilityDriverModel.class.getName());

	private static <Desired, Provided> Consumer<Provided> ifApplicable(final Consumer<Desired> func,
	                                                                   final Class<Desired> cls) {
		return (item) -> {
			if (cls.isInstance(item)) {
				func.accept((Desired) item);
			}
		};
	}

	private static boolean isSubset(final IFixture one, final IFixture two) {
		if (one instanceof SubsettableFixture) {
			return ((SubsettableFixture) one).isSubset(two, s -> {});
		} else {
			return one.equals(two);
		}
	}

	private static class Mock implements HasOwner {
		public Mock(final Player owner) {
			this.owner = owner;
		}

		private final Player owner;

		@Override
		public Player getOwner() {
			return owner;
		}
	}

	public UtilityDriverModel(final IMutableMapNG map) {
		super(map);
	}

	public UtilityDriverModel(final IDriverModel model) { // TODO: Make protected/private and provide static copyConstructor() instead?
		super(model);
	}

	/**
	 * Copy rivers at the given {@link location} missing from subordinate
	 * maps, where they have other terrain information, from the main map.
	 */
	public void copyRiversAt(final Point location) {
		final IMapNG map = getMap();
		for (final IMutableMapNG subordinateMap : getRestrictedSubordinateMaps()) {
			final TileType mainTerrain = map.getBaseTerrain(location);
			final TileType subTerrain = subordinateMap.getBaseTerrain(location);
			if (mainTerrain != null && subTerrain != null && mainTerrain == subTerrain &&
					!map.getRivers(location).isEmpty() &&
					subordinateMap.getRivers(location).isEmpty()) {
				subordinateMap.addRivers(location,
						map.getRivers(location).toArray(new River[0]));
				subordinateMap.setModified(true);
			}
		}
	}

	/**
	 * Conditionally remove duplicate fixtures. Returns a list of fixtures
	 * that would be removed and a callback to do the removal; the initial
	 * caller of this asks the user for approval.
	 */
	public Iterable<Quartet<Consumer<TileFixture>, @Nullable Path, TileFixture, Iterable<? extends TileFixture>>>
			conditionallyRemoveDuplicates(final Point location) {
		final List<Quartet<Consumer<TileFixture>, @Nullable Path, TileFixture, Iterable<? extends TileFixture>>>
			duplicatesList = new ArrayList<>();
		final List<TileFixture> checked = new ArrayList<>();
		for (final IMutableMapNG map : getRestrictedAllMaps()) {
			for (final TileFixture fixture : map.getFixtures(location)) {
				checked.add(fixture);
				if (fixture instanceof IUnit &&
						((IUnit) fixture).getKind().contains("TODO")) {
					continue;
				} else if (fixture instanceof CacheFixture) {
					continue;
				} else if (fixture instanceof HasPopulation &&
						((HasPopulation<?>) fixture).getPopulation() > 0) {
					continue;
				} else if (fixture instanceof HasExtent &&
						((HasExtent<?>) fixture).getAcres().doubleValue() > 0.0) {
					continue;
				}
				final List<TileFixture> matching = map.getFixtures(location).stream()
					.filter(item -> checked.stream().noneMatch(inner -> item == inner))
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
			if (fixture instanceof FixtureIterable) {
				final String shortDesc = (fixture instanceof TileFixture) ?
					((TileFixture) fixture).getShortDescription() : fixture.toString();
				if (fixture instanceof IMutableUnit) {
					retval.addAll(coalesceImpl(
						String.format("%sIn %s: ", context, shortDesc),
						(IMutableUnit) fixture,
						ifApplicable(((IMutableUnit) fixture)::addMember,
							UnitMember.class),
						ifApplicable(((IMutableUnit) fixture)::removeMember,
							UnitMember.class),
						setModFlag, handlers));
				} else if (fixture instanceof IMutableFortress) {
					retval.addAll(coalesceImpl(
						String.format("%sIn %s: ", context, shortDesc),
						(IMutableFortress) fixture,
						ifApplicable(((IMutableFortress) fixture)::addMember,
							FortressMember.class),
						ifApplicable(((IMutableFortress) fixture)::removeMember,
							FortressMember.class),
						setModFlag, handlers));
				} else {
					LOGGER.warning("Unhandled fixture in coalesceImpl()");
				}
			} else if (fixture instanceof Animal) {
				if (((Animal) fixture).isTalking()) {
					continue;
				}
				if (handlers.containsKey(Animal.class)) {
					handlers.get(Animal.class).addIfType(fixture);
				}
			} else if (fixture instanceof HasPopulation &&
					((HasPopulation<?>) fixture).getPopulation() < 0) {
				continue;
			} else if (fixture instanceof HasExtent &&
					((HasExtent<?>) fixture).getAcres().doubleValue() <= 0.0) {
				continue;
			} else if (handlers.containsKey(fixture.getClass())) {
				handlers.get(fixture.getClass()).addIfType(fixture);
			}
		}

		for (final CoalescedHolder<? extends IFixture, ?> handler : handlers.values()) {
			for (final List<? extends IFixture> list : handler) {
				if (list.isEmpty() || list.size() == 1) {
					continue;
				}
				retval.add(Quartet.with(() -> {
						final IFixture combined = handler.combineRaw(
								list.toArray(new IFixture[0]));
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
		for (final IMutableMapNG map : getRestrictedAllMaps()) {
			retval.addAll(coalesceImpl(
				String.format("In %s: At %s: ",
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
		final IMapNG map = getMap();
		for (final IMutableMapNG subMap : getRestrictedSubordinateMaps()) {
			subMap.setModified(true);
			final TileType terrain = map.getBaseTerrain(location);
			final TileType ours = subMap.getBaseTerrain(location);
			if (terrain != null && ours != null && terrain == ours) {
				subMap.setBaseTerrain(location, null);
			}
			subMap.removeRivers(location,
					map.getRivers(location).toArray(new River[0]));
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
			List<TileFixture> toRemove = new ArrayList<>();
			for (final TileFixture fixture : subMap.getFixtures(location)) {
				if (map.getFixtures(location).stream()
						.anyMatch(item -> isSubset(item, fixture))) {
					toRemove.add(fixture);
				}
			}
			for (TileFixture fixture : toRemove) {
				subMap.removeFixture(location, fixture);
			}
		}
	}

	private static List<Forest> extractForests(final IMapNG map, final Point location) {
		return map.getFixtures(location).stream().filter(Forest.class::isInstance)
			.map(Forest.class::cast).collect(Collectors.toList());
	}

	private static List<Ground> extractGround(final IMapNG map, final Point location) {
		return map.getFixtures(location).stream().filter(Ground.class::isInstance)
			.map(Ground.class::cast).collect(Collectors.toList());
	}

	public void fixForestsAndGround(final Consumer<String> ostream) {
		for (final IMapNG map : getSubordinateMaps()) {
			ostream.accept(String.format("Starting %s",
				Optional.ofNullable(map.getFilename())
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
					if (matching == null) {
						ostream.accept(String.format("Unmatched forest in %s: %s",
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
					if (matching == null) {
						ostream.accept(String.format("Unmatched ground in %s: %s",
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

	private static void safeAdd(final IMutableMapNG map, final Player currentPlayer, final Point point, final TileFixture fixture) {
		final Predicate<TileFixture> equality = fixture::equals;
		final Consumer<String> noop = x -> {};
		final Predicate<TileFixture> newSubsetOfOld =
				f -> f instanceof SubsettableFixture && ((SubsettableFixture) f).isSubset(fixture, noop);
		if (map.getFixtures(point).stream().anyMatch(equality.or(newSubsetOfOld))) {
			return;
		}
		final IFixture.CopyBehavior cb;
		if (fixture instanceof HasOwner && !(fixture instanceof ITownFixture) &&
				    ((HasOwner) fixture).getOwner().equals(currentPlayer)) {
			cb = IFixture.CopyBehavior.KEEP;
		} else {
			cb = IFixture.CopyBehavior.ZERO;
		}
		final TileFixture zeroed = fixture.copy(cb);
		final Predicate<TileFixture> zeroedEquals = zeroed::equals;
		final Predicate<TileFixture> zeroedSubsetOfOld =
				f -> f instanceof SubsettableFixture && ((SubsettableFixture) f).isSubset(zeroed, noop);
		final Predicate<TileFixture> oldSubsetOfZeroed =
				f -> zeroed instanceof SubsettableFixture && ((SubsettableFixture) zeroed).isSubset(f, noop);
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
		final IMapNG map = getMap();
		final long seed = SingletonRandom.SINGLETON_RANDOM.nextLong();
		for (final IMutableMapNG subMap : getRestrictedSubordinateMaps()) {
			if (!subMap.getCurrentPlayer().equals(currentPlayer)) {
				continue;
			}

			final Random rng = new Random(seed);

			for (final Point neighbor : new SurroundingPointIterable(center, subMap.getDimensions())) {
				if (subMap.getBaseTerrain(neighbor) == null) {
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
