package drivers.generators;

import lovelace.util.LovelaceLogger;
import drivers.common.IDriverModel;
import drivers.common.SimpleMultiMapModel;

import legacy.map.IFixture;
import legacy.map.ILegacyMap;
import legacy.map.IMutableLegacyMap;
import common.map.Player;
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

import legacy.map.fixtures.towns.AbstractTown;
import legacy.map.fixtures.towns.CommunityStats;
import legacy.map.fixtures.towns.IFortress;
import legacy.map.fixtures.towns.ITownFixture;
import legacy.map.fixtures.towns.Village;

import legacy.map.fixtures.mobile.worker.IJob;
import legacy.map.fixtures.mobile.worker.IMutableJob;
import legacy.map.fixtures.mobile.worker.Job;

import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.stream.Collectors;

public class PopulationGeneratingModel extends SimpleMultiMapModel { // TODO: Extract interface
    /**
     * The intersection of two sets; here so it can be passed as a method
     * reference rather than a lambda in {@link #getPlayerChoices}.
     *
     * TODO: Move * to lovelace.util? Or is there some equivalent
     * method-reference logic with curry() or uncurry() or some such?
     */
    private static <T> Set<T> intersection(final Set<T> one, final Set<T> two) {
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
    public boolean setAnimalPopulation(final Point location, final boolean talking, final String kind, final int population) {
        boolean retval = false;
        final Predicate<Object> isAnimal = Animal.class::isInstance;
        final Function<Object, Animal> animalCast = Animal.class::cast;
        final Predicate<Animal> equalKind = a -> kind.equals(a.getKind());
        final Predicate<Animal> sameTalking = a -> a.isTalking() == talking;
        for (final IMutableLegacyMap map : getRestrictedAllMaps()) { // TODO: Should submaps really all get this information?
            final Optional<Animal> animal = map.getFixtures(location).stream()
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
        for (final IMutableLegacyMap map : getRestrictedAllMaps()) { // TODO: Should submaps really all get this information?
            final Optional<Grove> grove = map.getFixtures(location).stream()
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
        for (final IMutableLegacyMap map : getRestrictedAllMaps()) { // TODO: Should submaps really all get this information?
            final Optional<Shrub> shrub = map.getFixtures(location).stream()
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
        final Predicate<Meadow> sameKind = m -> field.getKind().equals(m.getKind());
        final Predicate<Meadow> sameField = m -> field.isField() == m.isField();
        final Predicate<Meadow> sameCultivated = m -> field.isCultivated() == m.isCultivated();
        final Predicate<Meadow> sameStatus = m -> field.getStatus() == m.getStatus();
        final Predicate<Meadow> sameId = m -> field.getId() == m.getId();
        for (final IMutableLegacyMap map : getRestrictedAllMaps()) { // TODO: Should submaps really all get this information?
            final Optional<Meadow> existing = map.getFixtures(location).stream()
                    .filter(isMeadow).map(meadowCast)
                    .filter(sameKind)
                    .filter(sameField)
                    .filter(sameCultivated)
                    .filter(sameStatus)
                    .filter(sameId)
                    .findAny(); // TODO: only match without an existing extent?
            if (existing.isPresent()) {
                final Meadow replacement = new Meadow(field.getKind(), field.isField(),
                        field.isCultivated(), field.getId(), field.getStatus(), acres);
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
        final Predicate<Forest> sameKind = f -> forest.getKind().equals(f.getKind());
        final Predicate<Forest> sameRows = f -> forest.isRows() == f.isRows();
        final Predicate<Forest> sameId = f -> forest.getId() == f.getId();
        for (final IMutableLegacyMap map : getRestrictedAllMaps()) { // TODO: Should submaps really all get this information?
            final Optional<Forest> existing = map.getFixtures(location).stream()
                    .filter(isForest).map(forestCast)
                    .filter(sameKind)
                    .filter(sameRows)
                    .filter(sameId)
                    .findAny();
            if (existing.isPresent()) {
                final Forest replacement = new Forest(forest.getKind(), forest.isRows(),
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
     *
     * TODO: Should use a copy of the stats object, not it itself
     */
    public boolean assignTownStats(final Point location, final int townId, final String name, final CommunityStats stats) {
        boolean retval = false;
        final Function<Object, ITownFixture> townCast = ITownFixture.class::cast;
        final Predicate<ITownFixture> sameId = f -> f.getId() == townId;
        final Predicate<ITownFixture> sameName = f -> name.equals(f.getName());
        for (final IMutableLegacyMap map : getRestrictedAllMaps()) { // TODO: Should submaps really all get this information?
            final Optional<ITownFixture> town = map.getFixtures(location).stream()
                    .filter(f -> f instanceof AbstractTown || f instanceof Village) // TODO: extract IMutableTown interface
                    .map(townCast)
                    .filter(sameId)
                    .filter(sameName)
                    .findAny();
            if (town.isPresent()) {
                if (town.get() instanceof final AbstractTown t) {
                    t.setPopulation(stats);
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
            existingNote = String.format("%s ", worker.getNote(unit.owner()));
        }
        final Predicate<IFixture> isUnit = IMutableUnit.class::isInstance;
        final Function<IFixture, IMutableUnit> unitCast = IMutableUnit.class::cast;
        final Predicate<IMutableUnit> sameOwner = u -> unit.owner().equals(u.owner());
        final Predicate<IMutableUnit> sameKind = u -> unit.getKind().equals(u.getKind());
        final Predicate<IMutableUnit> sameName = u -> unit.getName().equals(u.getName());
        final Predicate<IMutableUnit> sameId = u -> u.getId() == unit.getId();
        for (final IMutableLegacyMap map : getRestrictedAllMaps()) {
            final Optional<IMutableUnit> localUnit = map.streamAllFixtures()
                    .flatMap(PopulationGeneratingModel::flattenFortresses)
                    .filter(isUnit)
                    .map(unitCast)
                    .filter(sameOwner)
                    .filter(sameKind)
                    .filter(sameName)
                    .filter(sameId)
                    .findAny();
            if (localUnit.isPresent()) {
                final int turn = map.getCurrentTurn();
                final IWorker addend = worker.copy(IFixture.CopyBehavior.KEEP);
                if (turn >= 0) {
                    addend.setNote(localUnit.get().owner(),
                            String.format("%sNewcomer in turn #%d", existingNote, turn));
                }
                localUnit.get().addMember(addend);
                if (localUnit.get().getOrders(turn).isEmpty()) {
                    localUnit.get().setOrders(turn, "TODO: assign");
                }
                map.setModified(true);
            } else {
                LovelaceLogger.debug("Unit not found in %s",
                        Optional.ofNullable(map.getFilename()).map(Object::toString).orElse("unsaved map"));
            }
        }
    }

    /**
     * All the units in the main map belonging to the specified player.
     *
     * FIXME: Return List
     */
    public Iterable<IUnit> getUnits(final Player player) {
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
    public Iterable<Player> getPlayerChoices() {
        Set<Player> set = null;
        for (final ILegacyMap map : getAllMaps()) {
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
     * Add the given unit at the given location.
     *
     * TODO: If more than one map, return a proxy for the units; otherwise, return the unit
     */
    public void addUnitAtLocation(final IUnit unit, final Point location) {
        for (final IMutableLegacyMap indivMap : getRestrictedAllMaps()) {
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
    public boolean addJobLevel(final IUnit unit, final IWorker worker, final String jobName) {
        boolean any = false;
        final Predicate<Object> isWorker = IWorker.class::isInstance;
        final Function<Object, IWorker> workerCast = IWorker.class::cast;
        final Predicate<IWorker> sameRace = w -> worker.getRace().equals(w.getRace());
        final Predicate<IWorker> sameName = w -> worker.getName().equals(w.getName());
        final Predicate<IWorker> sameId = w -> worker.getId() == w.getId();
        for (final IMutableLegacyMap map : getRestrictedAllMaps()) {
            for (final IUnit container : map.streamAllFixtures()
                    .flatMap(PopulationGeneratingModel::flattenFortresses)
                    .filter(IUnit.class::isInstance).map(IUnit.class::cast)
                    .filter(u -> u.owner().equals(unit.owner())).toList()) {
                final Optional<IWorker> matching =
                        container.stream().filter(isWorker).map(workerCast)
                                .filter(sameRace)
                                .filter(sameName)
                                .filter(sameId)
                                .findAny();
                if (matching.isPresent()) { // FIXME: 'worker' below should be 'matching', right?
                    final Optional<IMutableJob> streamedJob =
                            StreamSupport.stream(worker.spliterator(), true)
                                    .filter(IMutableJob.class::isInstance)
                                    .map(IMutableJob.class::cast)
                                    .filter(j -> jobName.equals(j.getName()))
                                    .findAny();
                    final IJob queriedJob = worker.getJob(jobName);
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
                    map.setModified(true);
                    break;
                }
            }
        }
        return any;
    }
}
