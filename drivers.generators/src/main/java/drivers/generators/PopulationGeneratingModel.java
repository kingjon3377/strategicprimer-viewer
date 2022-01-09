package drivers.generators;

import drivers.common.IDriverModel;
import drivers.common.SimpleMultiMapModel;

import common.map.HasOwner;
import common.map.IFixture;
import common.map.IMapNG;
import common.map.IMutableMapNG;
import common.map.Player;
import common.map.Point;

import common.map.fixtures.mobile.Animal;
import common.map.fixtures.mobile.IMutableUnit;
import common.map.fixtures.mobile.IMutableWorker;
import common.map.fixtures.mobile.IUnit;
import common.map.fixtures.mobile.IWorker;

import common.map.fixtures.terrain.Forest;

import common.map.fixtures.resources.Grove;
import common.map.fixtures.resources.Meadow;
import common.map.fixtures.resources.Shrub;

import common.map.fixtures.towns.AbstractTown;
import common.map.fixtures.towns.CommunityStats;
import common.map.fixtures.towns.IFortress;
import common.map.fixtures.towns.ITownFixture;
import common.map.fixtures.towns.Village;

import common.map.fixtures.mobile.worker.IJob;
import common.map.fixtures.mobile.worker.IMutableJob;
import common.map.fixtures.mobile.worker.Job;

import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.stream.Collectors;

public class PopulationGeneratingModel extends SimpleMultiMapModel { // TODO: Extract interface
	/**
	 * The intersection of two sets; here so it can be passed as a method
	 * reference rather than a lambda in [[playerChoices]].
	 *
	 * TODO: Move * to lovelace.util? Or is there some equivalent
	 * method-reference logic with curry() or uncurry() or some such?
	 */
	private static <T> Set<T> intersection(Set<T> one, Set<T> two) {
		Set<T> retval = new HashSet<T>(one);
		retval.retainAll(two);
		return retval;
	}

	public PopulationGeneratingModel(IMutableMapNG map) {
		super(map);
	}

	// TODO: Make a static copyConstructor() method delegating to a private constructor method instead?
	public PopulationGeneratingModel(IDriverModel model) {
		super(model.getRestrictedMap());
	}

	/**
	 * Set the population of {@link kind} animals (talking or not per
	 * {@link talking}) at {@link location} to {@link population}. Returns
	 * true if a population was in fact set (i.e. if there was a matching
	 * object there in any of the maps), false otherwise.
	 */
	public boolean setAnimalPopulation(Point location, boolean talking, String kind, int population) {
		boolean retval = false;
		for (IMutableMapNG map : getRestrictedAllMaps()) { // TODO: Should submaps really all get this information?
			Optional<Animal> animal = map.getFixtures(location).stream()
				.filter(Animal.class::isInstance).map(Animal.class::cast)
				.filter(a -> a.isTalking() == talking)
				.filter(a -> kind.equals(a.getKind()))
				.findAny(); // TODO: Only match those without an existing population?
			if (animal.isPresent()) {
				Animal replacement = animal.get().reduced(population);
				map.replace(location, animal.get(), replacement);
				retval = true;
			}
		}
		return retval;
	}

	/**
	 * Set the population of {@link kind} trees (in a grove or orchard) at
	 * {@link location} to {@link population}. Returns true if a population
	 * was in fact set (i.e. if there was a matching grove or orchard there
	 * in any of the maps), false otherwise.
	 */
	public boolean setGrovePopulation(Point location, String kind, int population) {
		boolean retval = false;
		for (IMutableMapNG map : getRestrictedAllMaps()) { // TODO: Should submaps really all get this information?
			Optional<Grove> grove = map.getFixtures(location).stream()
				.filter(Grove.class::isInstance).map(Grove.class::cast)
				.filter(g -> kind.equals(g.getKind()))
				.findAny(); // TODO: Only match those without an existing population?
			if (grove.isPresent()) {
				Grove replacement = grove.get().reduced(population);
				map.replace(location, grove.get(), replacement);
				retval = true;
			}
		}
		return retval;
	}

	/**
	 * Set the population of {@link kind} shrubs at {@link location} to
	 * {@link population}.  Returns true if a population was in fact set
	 * (i.e. there were in fact matching shrubs there in any of the maps),
	 * false otherwise.
	 */
	public boolean setShrubPopulation(Point location, String kind, int population) {
		boolean retval = false;
		for (IMutableMapNG map : getRestrictedAllMaps()) { // TODO: Should submaps really all get this information?
			Optional<Shrub> shrub = map.getFixtures(location).stream()
				.filter(Shrub.class::isInstance).map(Shrub.class::cast)
				.filter(s -> kind.equals(s.getKind()))
				.findAny(); // TODO: Only match without an existing count?
			if (shrub.isPresent()) {
				Shrub replacement = shrub.get().reduced(population);
				map.replace(location, shrub.get(), replacement);
				retval = true;
			}
		}
		return retval;
	}

	/**
	 * Set the extent of the given {@link field field or meadow} at {@link
	 * location} to {@link acres} in maps where that field or meadow is
	 * known. Returns true if any of the maps had it at that location, false otherwise.
	 */
	public boolean setFieldExtent(Point location, Meadow field, double acres) {
		boolean retval = false;
		for (IMutableMapNG map : getRestrictedAllMaps()) { // TODO: Should submaps really all get this information?
			Optional<Meadow> existing = map.getFixtures(location).stream()
				.filter(Meadow.class::isInstance).map(Meadow.class::cast)
				.filter(m -> field.getKind().equals(m.getKind()))
				.filter(m -> field.isField() == m.isField())
				.filter(m -> field.isCultivated() == m.isCultivated())
				.filter(m -> field.getStatus().equals(m.getStatus()))
				.filter(m -> field.getId() == m.getId())
				.findAny(); // TODO: only match without an existing extent?
			if (existing.isPresent()) {
				Meadow replacement = new Meadow(field.getKind(), field.isField(),
					field.isCultivated(), field.getId(), field.getStatus(), acres);
				map.replace(location, existing.get(), replacement);
				retval = true;
			}
		}
		return retval;
	}

	/**
	 * Set the extent of the given {@link forest} at {@link location} to
	 * {@link acres} in maps where that forest is known. Returns true if
	 * any of the maps had it at that location, false otherwise.
	 */
	public boolean setForestExtent(Point location, Forest forest, Number acres) {
		boolean retval = false;
		for (IMutableMapNG map : getRestrictedAllMaps()) { // TODO: Should submaps really all get this information?
			Optional<Forest> existing = map.getFixtures(location).stream()
				.filter(Forest.class::isInstance).map(Forest.class::cast)
				.filter(f -> forest.getKind().equals(f.getKind()))
				.filter(f -> forest.isRows() == f.isRows())
				.filter(f -> forest.getId() == f.getId())
				.findAny();
			if (existing.isPresent()) {
				Forest replacement = new Forest(forest.getKind(), forest.isRows(),
					forest.getId(), acres);
				getRestrictedMap().replace(location, existing.get(), replacement);
				retval = true;
			}
		}
		return retval;
	}

	/**
	 * Assign the given {@link stats population details object} to the town
	 * identified by the given {@link townId ID number} and {@link name} at
	 * the given {@link location} in each map. Returns true if such a town
	 * was in any of the maps, false otherwise.
	 *
	 * TODO: Should use a copy of the stats object, not it itself
	 */
	public boolean assignTownStats(Point location, int townId, String name, CommunityStats stats) {
		boolean retval = false;
		for (IMutableMapNG map : getRestrictedAllMaps()) { // TODO: Should submaps really all get this information?
			Optional<ITownFixture> town = map.getFixtures(location).stream()
				.filter(f -> f instanceof AbstractTown || f instanceof Village) // TODO: extract IMutableTown interface
				.map(ITownFixture.class::cast)
				.filter(f -> f.getId() == townId)
				.filter(f -> name.equals(f.getName()))
				.findAny();
			if (town.isPresent()) {
				if (town.get() instanceof AbstractTown) {
					((AbstractTown) (town.get())).setPopulation(stats);
				} else {
					((Village) (town.get())).setPopulation(stats);
				}
				retval = true;
				map.setModified(true);
			}
		}
		return retval;
	}

	/**
	 * If the fixture is a fortress, return a stream of its contents;
	 * otherwise return a stream containing only the fixture. This flattens
	 * a fortress's contents into a stream of tile fixtures.
	 */
	private static Stream<IFixture> flattenFortresses(IFixture fixture) {
		if (fixture instanceof IFortress) {
			return StreamSupport.stream(((IFortress) fixture).spliterator(), true)
				.map(IFixture.class::cast);
		} else {
			return Stream.of(fixture);
		}
	}

	/**
	 * Add a (copy of a) worker to a unit in all maps where that unit exists.
	 */
	public void addWorkerToUnit(IUnit unit, IWorker worker) {
		String existingNote;
		if (!worker.getNote(unit.getOwner()).isEmpty()) { // TODO: invert
			existingNote = String.format("%s ", worker.getNote(unit.getOwner()));
		} else {
			existingNote = "";
		}
		for (IMutableMapNG map : getRestrictedAllMaps()) {
			Optional<IMutableUnit> localUnit =
				StreamSupport.stream(map.getLocations().spliterator(), true)
					.flatMap(l -> map.getFixtures(l).stream())
					.flatMap(PopulationGeneratingModel::flattenFortresses)
					.filter(IMutableUnit.class::isInstance)
					.map(IMutableUnit.class::cast)
					.filter(u -> unit.getOwner().equals(u.getOwner()))
					.filter(u -> unit.getKind().equals(u.getKind()))
					.filter(u -> unit.getName().equals(u.getName()))
					.filter(u -> u.getId() == unit.getId())
					.findAny();
			if (localUnit.isPresent()) {
				int turn = map.getCurrentTurn();
				IWorker addend = worker.copy(false);
				if (turn >= 0) {
					addend.setNote(localUnit.get().getOwner(),
						String.format("%sNewcomer in turn #%d", existingNote, turn));
				}
				localUnit.get().addMember(addend);
				if (localUnit.get().getOrders(turn).isEmpty()) {
					localUnit.get().setOrders(turn, "TODO: assign");
				}
				map.setModified(true);
			}
		}
	}

	/**
	 * All the units in the main map belonging to the specified player.
	 */
	public Iterable<IUnit> getUnits(Player player) {
		return StreamSupport.stream(getMap().getLocations().spliterator(), true)
			.flatMap(l -> getMap().getFixtures(l).stream())
			.flatMap(PopulationGeneratingModel::flattenFortresses)
			.filter(IUnit.class::isInstance).map(IUnit.class::cast)
			.filter(u -> u.getOwner().equals(player)).collect(Collectors.toList());
	}

	/**
	 * All the players shared by all the maps.
	 *
	 * TODO: Move to IMultiMapModel?
	 */
	public Iterable<Player> getPlayerChoices() {
		Set<Player> set = null;
		for (IMapNG map : getAllMaps()) {
			if (set == null) {
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
	 * Add the given {@link unit} at the given {@link location}.
	 *
	 * TODO: If more than one map, return a proxy for the units; otherwise, return the unit
	 */
	public void addUnitAtLocation(IUnit unit, Point location) {
		for (IMutableMapNG indivMap : getRestrictedAllMaps()) {
			indivMap.addFixture(location, unit); // FIXME: Check for existing matching unit there already
			indivMap.setModified(true);
		}
	}

	/**
	 * Add a level in a {@link IJob job} to a {@link IWorker worker}.
	 * Returns true if a matching mutable Job in a matching worker was
	 * found, or created in a matching mutable worker, in at least one map,
	 * false otherwise.
	 */
	public boolean addJobLevel(IUnit unit, IWorker worker, String jobName) {
		boolean any = false;
		for (IMutableMapNG map : getRestrictedAllMaps()) {
			for (IUnit container : StreamSupport.stream(map.getLocations().spliterator(), true)
					.flatMap(l -> map.getFixtures(l).stream())
					.flatMap(PopulationGeneratingModel::flattenFortresses)
					.filter(IUnit.class::isInstance).map(IUnit.class::cast)
					.filter(u -> u.getOwner().equals(unit.getOwner()))
					.collect(Collectors.toList())) {
				Optional<IWorker> matching = 
					StreamSupport.stream(container.spliterator(), true)
						.filter(IWorker.class::isInstance).map(IWorker.class::cast)
						.filter(w -> worker.getRace().equals(w.getRace()))
						.filter(w -> worker.getName().equals(w.getName()))
						.filter(w -> worker.getId() == w.getId())
						.findAny();
				if (matching.isPresent()) { // FIXME: 'worker' below should be 'matching', right?
					Optional<IMutableJob> streamedJob =
						StreamSupport.stream(worker.spliterator(), true)
							.filter(IMutableJob.class::isInstance)
							.map(IMutableJob.class::cast)
							.filter(j -> jobName.equals(j.getName()))
							.findAny();
					IJob queriedJob = worker.getJob(jobName);
					if (streamedJob.isPresent()) {
						streamedJob.get().setLevel(
							streamedJob.get().getLevel() + 1);
					} else if (queriedJob instanceof IMutableJob) {
						((IMutableJob) queriedJob)
							.setLevel(queriedJob.getLevel() + 1);
					} else if (matching.get() instanceof IMutableWorker &&
							!StreamSupport.stream(
									matching.get().spliterator(), true)
								.anyMatch(j -> j.getName()
									.equals(jobName))) {
						((IMutableWorker) matching.get())
							.addJob(new Job(jobName, 1));
					} else {
						continue;
					}
					any = true;
					map.setModified(true);
					break;
				}
			}
		}
		return any;
	}
}