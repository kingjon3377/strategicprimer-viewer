package drivers.generators;

import legacy.map.TileFixture;
import legacy.map.fixtures.towns.HasMutablePopulation;
import lovelace.util.LovelaceLogger;
import drivers.common.IDriverModel;
import drivers.common.SimpleMultiMapModel;

import legacy.map.IFixture;
import legacy.map.ILegacyMap;
import legacy.map.IMutableLegacyMap;
import legacy.map.Player;
import legacy.map.Point;

import legacy.map.fixtures.mobile.Animal;
import legacy.map.fixtures.mobile.IMutableUnit;
import legacy.map.fixtures.mobile.IMutableWorker;
import legacy.map.fixtures.mobile.IUnit;
import legacy.map.fixtures.mobile.IWorker;

import legacy.map.fixtures.terrain.Forest;

import legacy.map.fixtures.resources.Grove;
import legacy.map.fixtures.resources.Meadow;
import legacy.map.fixtures.resources.Shrub;

import legacy.map.fixtures.towns.CommunityStats;
import legacy.map.fixtures.towns.IFortress;
import legacy.map.fixtures.towns.ITownFixture;

import legacy.map.fixtures.mobile.worker.IJob;
import legacy.map.fixtures.mobile.worker.IMutableJob;
import legacy.map.fixtures.mobile.worker.Job;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.stream.Collectors;

import static lovelace.util.MatchingValue.matchingValue;
import static lovelace.util.MatchingValue.matchingValues;

public final class PopulationGeneratingModel extends SimpleMultiMapModel { // TODO: Extract interface
	/**
	 * The intersection of two sets; here so it can be passed as a method
	 * reference rather than a lambda in {@link #getPlayerChoices}.
	 *
	 * TODO: Move to lovelace.util? Or is there some equivalent
	 * method-reference logic with curry() or uncurry() or some such?
	 */
	private static <T> Set<T> intersection(final Set<T> one, final Collection<T> two) {
		final Set<T> retval = new HashSet<>(one);
		retval.retainAll(two);
		return retval;
	}

	public PopulationGeneratingModel(final IMutableLegacyMap map) {
		super(map);
	}

	// TODO: Make a static copyConstructor() method delegating to a private constructor method instead?
	public PopulationGeneratingModel(final IDriverModel model) {
		super(model);
	}

	/**
	 * Set the population of "kind" animals (talking or not per
	 * "talking") at "location" to "population". Returns
	 * true if a population was in fact set (i.e. if there was a matching
	 * object there in any of the maps), false otherwise.
	 */
	public boolean setAnimalPopulation(final Point location, final boolean talking, final String kind,
									   final int population) {
		boolean retval = false;
		final Predicate<Object> isAnimal = Animal.class::isInstance;
		final Function<Object, Animal> animalCast = Animal.class::cast;
		final Predicate<Animal> equalKind = a -> kind.equals(a.getKind());
		final Predicate<Animal> sameTalking = a -> a.isTalking() == talking;
		// TODO: Should submaps really all get this information?
		for (final IMutableLegacyMap map : getRestrictedAllMaps()) {
			final Optional<Animal> animal = map.streamFixtures(location)
					.filter(isAnimal).map(animalCast)
					.filter(sameTalking)
					.filter(equalKind)
					.findAny(); // TODO: Only match those without an existing population?
			if (animal.isPresent()) {
				final Animal replacement = animal.get().reduced(population);
				map.replace(location, animal.get(), replacement);
				retval = true;
			}
		}
		return retval;
	}

	/**
	 * Set the population of "kind" trees (in a grove or orchard) at
	 * "location" to "population". Returns true if a population
	 * was in fact set (i.e. if there was a matching grove or orchard there
	 * in any of the maps), false otherwise.
	 */
	public boolean setGrovePopulation(final Point location, final String kind, final int population) {
		boolean retval = false;
		final Predicate<Object> isGrove = Grove.class::isInstance;
		final Function<Object, Grove> groveCast = Grove.class::cast;
		final Predicate<Grove> sameKind = g -> kind.equals(g.getKind());
		// TODO: Should submaps really all get this information?
		for (final IMutableLegacyMap map : getRestrictedAllMaps()) {
			final Optional<Grove> grove = map.streamFixtures(location)
					.filter(isGrove).map(groveCast)
					.filter(sameKind)
					.findAny(); // TODO: Only match those without an existing population?
			if (grove.isPresent()) {
				final Grove replacement = grove.get().reduced(population);
				map.replace(location, grove.get(), replacement);
				retval = true;
			}
		}
		return retval;
	}

	/**
	 * Set the population of "kind" shrubs at "location" to
	 * "population".  Returns true if a population was in fact set
	 * (i.e. there were in fact matching shrubs there in any of the maps),
	 * false otherwise.
	 */
	public boolean setShrubPopulation(final Point location, final String kind, final int population) {
		boolean retval = false;
		final Predicate<Object> isShrub = Shrub.class::isInstance;
		final Function<Object, Shrub> shrubCast = Shrub.class::cast;
		final Predicate<Shrub> sameKind = s -> kind.equals(s.getKind());
		// TODO: Should submaps really all get this information?
		for (final IMutableLegacyMap map : getRestrictedAllMaps()) {
			final Optional<Shrub> shrub = map.streamFixtures(location)
					.filter(isShrub).map(shrubCast)
					.filter(sameKind)
					.findAny(); // TODO: Only match without an existing count?
			if (shrub.isPresent()) {
				final Shrub replacement = shrub.get().reduced(population);
				map.replace(location, shrub.get(), replacement);
				retval = true;
			}
		}
		return retval;
	}

	/**
	 * Set the extent of the given field or meadow at
	 * "location" to "acres" in maps where that field or meadow is
	 * known. Returns true if any of the maps had it at that location, false otherwise.
	 */
	public boolean setFieldExtent(final Point location, final Meadow field, final double acres) {
		boolean retval = false;
		final Predicate<Object> isMeadow = Meadow.class::isInstance;
		final Function<Object, Meadow> meadowCast = Meadow.class::cast;
		final Predicate<Meadow> matchingProperties = matchingValues(field, Meadow::getKind, Meadow::getType,
				Meadow::getCultivation, Meadow::getStatus, Meadow::getId);
		// TODO: Should submaps really all get this information?
		for (final IMutableLegacyMap map : getRestrictedAllMaps()) {
			final Optional<Meadow> existing = map.streamFixtures(location)
					.filter(isMeadow).map(meadowCast)
					.filter(matchingProperties)
					.findAny(); // TODO: only match without an existing extent?
			if (existing.isPresent()) {
				final TileFixture replacement = new Meadow(field.getKind(), field.getType(),
						field.getCultivation(), field.getId(), field.getStatus(), acres);
				map.replace(location, existing.get(), replacement);
				retval = true;
			}
		}
		return retval;
	}

	/**
	 * Set the extent of the given forest at "location" to
	 * "acres" in maps where that forest is known. Returns true if
	 * any of the maps had it at that location, false otherwise.
	 */
	public boolean setForestExtent(final Point location, final Forest forest, final Number acres) {
		boolean retval = false;
		final Predicate<Object> isForest = Forest.class::isInstance;
		final Function<Object, Forest> forestCast = Forest.class::cast;
		final Predicate<Forest> matchingFields = matchingValues(forest,
				Forest::getKind, Forest::isRows, Forest::getId);
		// TODO: Should submaps really all get this information?
		for (final IMutableLegacyMap map : getRestrictedAllMaps()) {
			final Optional<Forest> existing = map.streamFixtures(location)
					.filter(isForest).map(forestCast)
					.filter(matchingFields)
					.findAny();
			if (existing.isPresent()) {
				final TileFixture replacement = new Forest(forest.getKind(), forest.isRows(),
						forest.getId(), acres);
				getRestrictedMap().replace(location, existing.get(), replacement);
				retval = true;
			}
		}
		return retval;
	}

	/**
	 * Assign the given population details object to the town
	 * identified by the given ID number and name at
	 * the given location in each map. Returns true if such a town
	 * was in any of the maps, false otherwise.
	 */
	public boolean assignTownStats(final Point location, final int townId, final String name,
								   final CommunityStats stats) {
		boolean retval = false;
		final Function<Object, ITownFixture> townCast = ITownFixture.class::cast;
		final Predicate<Object> hmpFilter = HasMutablePopulation.class::isInstance;
		final Function<Object, HasMutablePopulation> hmpCast = HasMutablePopulation.class::cast;
		final Predicate<ITownFixture> sameId = f -> f.getId() == townId;
		final Predicate<ITownFixture> sameName = f -> name.equals(f.getName());
		// TODO: Should submaps really all get this information?
		for (final IMutableLegacyMap map : getRestrictedAllMaps()) {
			final Optional<HasMutablePopulation> town = map.streamFixtures(location)
					.filter(hmpFilter)
					.map(townCast)
					.filter(sameId)
					.filter(sameName)
					.map(hmpCast)
					.findAny();
			if (town.isPresent()) {
				town.get().setPopulation(stats.copy());
				retval = true;
				map.setStatus(ILegacyMap.ModificationStatus.Modified);
			}
		}
		return retval;
	}

	/**
	 * If the fixture is a fortress, return a stream of its contents;
	 * otherwise return a stream containing only the fixture. This flattens
	 * a fortress's contents into a stream of tile fixtures.
	 */
	private static Stream<IFixture> flattenFortresses(final IFixture fixture) {
		if (fixture instanceof final IFortress fort) {
			return fort.stream().map(IFixture.class::cast);
		} else {
			return Stream.of(fixture);
		}
	}

	/**
	 * Add a (copy of a) worker to a unit in all maps where that unit exists.
	 */
	public void addWorkerToUnit(final IUnit unit, final IWorker worker) {
		final String existingNote;
		if (worker.getNote(unit.owner()).isEmpty()) {
			existingNote = "";
		} else {
			existingNote = "%s ".formatted(worker.getNote(unit.owner()));
		}
		final Predicate<IFixture> isUnit = IMutableUnit.class::isInstance;
		final Function<IFixture, IMutableUnit> unitCast = IMutableUnit.class::cast;
		final Predicate<IUnit> matchingFields = matchingValues(unit, IUnit::owner, IUnit::getKind,
				IUnit::getName, IUnit::getId);
		for (final IMutableLegacyMap map : getRestrictedAllMaps()) {
			final Optional<IMutableUnit> localUnit = map.streamAllFixtures()
					.flatMap(PopulationGeneratingModel::flattenFortresses)
					.filter(isUnit)
					.map(unitCast)
					.filter(matchingFields)
					.findAny();
			if (localUnit.isPresent()) {
				final int turn = map.getCurrentTurn();
				final IWorker addend = worker.copy(IFixture.CopyBehavior.KEEP);
				if (turn >= 0) {
					addend.setNote(localUnit.get().owner(),
							"%sNewcomer in turn #%d".formatted(existingNote, turn));
				}
				localUnit.get().addMember(addend);
				if (localUnit.get().getOrders(turn).isEmpty()) {
					localUnit.get().setOrders(turn, "TODO: assign");
				}
				map.setStatus(ILegacyMap.ModificationStatus.Modified);
			} else {
				LovelaceLogger.debug("Unit not found in %s",
						Optional.ofNullable(map.getFilename()).map(Object::toString).orElse("unsaved map"));
			}
		}
	}

	/**
	 * All the units in the main map belonging to the specified player.
	 */
	public List<IUnit> getUnits(final Player player) {
		return getMap().streamAllFixtures()
				.flatMap(PopulationGeneratingModel::flattenFortresses)
				.filter(IUnit.class::isInstance).map(IUnit.class::cast)
				.filter(u -> u.owner().equals(player)).collect(Collectors.toList());
	}

	/**
	 * All the players shared by all the maps.
	 *
	 * TODO: Move to IMultiMapModel?
	 */
	public Collection<Player> getPlayerChoices() {
		Set<Player> set = null;
		for (final ILegacyMap map : getAllMaps()) {
			if (Objects.isNull(set)) {
				set = StreamSupport.stream(map.getPlayers().spliterator(), true)
						.collect(Collectors.toSet());
			} else {
				set = intersection(set,
						StreamSupport.stream(map.getPlayers().spliterator(), true)
								.collect(Collectors.toSet()));
			}
		}
		return Collections.unmodifiableSet(Optional.ofNullable(set).orElseGet(HashSet::new));
	}

	/**
	 * Add the given unit at the given location. If a unit with the same ID is already there, it is not updated.
	 *
	 * TODO: If more than one map, return a proxy for the units; otherwise, return the unit
	 */
	@SuppressWarnings("TypeMayBeWeakened") // Let's not change the public API
	public void addUnitAtLocation(final IUnit unit, final Point location) {
		for (final IMutableLegacyMap indivMap : getRestrictedAllMaps()) {
			if (indivMap.streamFixtures(location).filter(IUnit.class::isInstance)
					.noneMatch(matchingValue(unit, TileFixture::getId))) {
				// TODO: Maybe require more (owner?) to match to prevent adding
				indivMap.addFixture(location, unit);
				indivMap.setStatus(ILegacyMap.ModificationStatus.Modified);
			}
		}
	}

	/**
	 * Add a level in a {@link IJob job} to a {@link IWorker worker}.
	 * Returns true if a matching mutable Job in a matching worker was
	 * found, or created in a matching mutable worker, in at least one map,
	 * false otherwise.
	 */
	@SuppressWarnings("TypeMayBeWeakened") // weakening could only work as an artifact of our find-the-matching approach
	public boolean addJobLevel(final IUnit unit, final IWorker worker, final String jobName) {
		boolean any = false;
		final Predicate<Object> isWorker = IWorker.class::isInstance;
		final Function<Object, IWorker> workerCast = IWorker.class::cast;
		final Predicate<IWorker> matchingFields = matchingValues(worker, IWorker::getRace,
				IWorker::getName, IWorker::getId);
		for (final IMutableLegacyMap map : getRestrictedAllMaps()) {
			for (final IUnit container : map.streamAllFixtures()
					.flatMap(PopulationGeneratingModel::flattenFortresses)
					.filter(IUnit.class::isInstance).map(IUnit.class::cast)
					.filter(matchingValue(unit, IUnit::owner)).toList()) {
				final Optional<IWorker> matching =
						container.stream().filter(isWorker).map(workerCast)
								.filter(matchingFields)
								.findAny();
				if (matching.isPresent()) {
					final Optional<IMutableJob> streamedJob =
							StreamSupport.stream(matching.get().spliterator(), true)
									.filter(IMutableJob.class::isInstance)
									.map(IMutableJob.class::cast)
									.filter(j -> jobName.equals(j.getName()))
									.findAny();
					final IJob queriedJob = matching.get().getJob(jobName);
					if (streamedJob.isPresent()) {
						streamedJob.get().setLevel(
								streamedJob.get().getLevel() + 1);
					} else if (queriedJob instanceof final IMutableJob mj) {
						mj.setLevel(queriedJob.getLevel() + 1);
					} else if (matching.get() instanceof final IMutableWorker mw &&
							StreamSupport.stream(mw.spliterator(), true)
									.noneMatch(j -> j.getName().equals(jobName))) {
						mw.addJob(new Job(jobName, 1));
					} else {
						continue;
					}
					any = true;
					map.setStatus(ILegacyMap.ModificationStatus.Modified);
					break;
				}
			}
		}
		return any;
	}
}
