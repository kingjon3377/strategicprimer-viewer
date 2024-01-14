package drivers.turnrunning;

import legacy.map.HasExtent;
import legacy.map.HasOwner;
import legacy.map.HasPopulation;
import legacy.map.IFixture;
import legacy.map.IMutableLegacyMap;
import legacy.map.Player;
import legacy.map.Point;
import legacy.map.TileFixture;
import legacy.map.fixtures.FixtureIterable;
import legacy.map.fixtures.FortressMember;
import legacy.map.fixtures.IMutableResourcePile;
import legacy.map.fixtures.IResourcePile;
import legacy.map.fixtures.LegacyQuantity;
import legacy.map.fixtures.ResourcePileImpl;
import legacy.map.fixtures.UnitMember;
import legacy.map.fixtures.mobile.Animal;
import legacy.map.fixtures.mobile.AnimalImpl;
import legacy.map.fixtures.mobile.IMutableUnit;
import legacy.map.fixtures.mobile.IMutableWorker;
import legacy.map.fixtures.mobile.IUnit;
import legacy.map.fixtures.mobile.IWorker;
import legacy.map.fixtures.mobile.worker.IJob;
import legacy.map.fixtures.mobile.worker.IMutableJob;
import legacy.map.fixtures.mobile.worker.IMutableSkill;
import legacy.map.fixtures.mobile.worker.Job;
import legacy.map.fixtures.mobile.worker.ISkill;
import legacy.map.fixtures.mobile.worker.Skill;
import legacy.map.fixtures.towns.IFortress;
import legacy.map.fixtures.towns.IMutableFortress;
import drivers.common.IDriverModel;
import exploration.common.ExplorationModel;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import lovelace.util.LovelaceLogger;
import org.jetbrains.annotations.Nullable;

import static lovelace.util.Decimalize.decimalize;

public class TurnRunningModel extends ExplorationModel implements ITurnRunningModel {
	/**
	 * If "fixture" is a {@link IFortress fortress}, return a stream
	 * of its contents; otherwise, return stream containing only it. This
	 * is intended to be used in {@link Stream#flatMap}.
	 */
	private static Stream<IFixture> unflattenNonFortresses(final TileFixture fixture) {
		if (fixture instanceof final IFortress f) {
			return f.stream().map(IFixture.class::cast);
		} else {
			return Stream.of(fixture);
		}
	}

	/**
	 * If "fixture" is a {@link IFortress fortress}, return a stream
	 * of it and its contents; otherwise, return a stream of only it. This
	 * is intended to be used in {@link Stream#flatMap}.
	 */
	private static Stream<IFixture> partiallyFlattenFortresses(final TileFixture fixture) {
		if (fixture instanceof final IFortress f) {
			return Stream.concat(Stream.of(fixture), f.stream());
		} else {
			return Stream.of(fixture);
		}
	}

	public TurnRunningModel(final IMutableLegacyMap map) {
		super(map);
	}

	// TODO: provide copyConstructor static factory?
	public TurnRunningModel(final IDriverModel model) {
		super(model);
	}

	/**
	 * Add a copy of the given fixture to all submaps at the given location
	 * iff no fixture with the same ID is already there.
	 */
	@Override
	public void addToSubMaps(final Point point, final TileFixture fixture, final IFixture.CopyBehavior zero) {
		final IntPredicate matching = i -> fixture.getId() == i;
		for (final IMutableLegacyMap map : getRestrictedSubordinateMaps()) {
			if (map.getFixtures(point).stream().mapToInt(TileFixture::getId)
				.noneMatch(matching)) {
				map.addFixture(point, fixture.copy(zero));
			}
		}
	}

	/**
	 * Reduce the population of a group of plants, animals, etc., and copy
	 * the reduced form into all subordinate maps.
	 */
	@Override
	public <T extends HasPopulation<? extends TileFixture> & TileFixture>
	void reducePopulation(final Point location, final T fixture, final IFixture.CopyBehavior zero, final int reduction) {
		if (reduction > 0) {
			boolean first = false;
			boolean all = false;
			final Predicate<Object> isInstance = fixture.getClass()::isInstance;
			final Function<Object, ? extends HasPopulation> cast = fixture.getClass()::cast;
			for (final IMutableLegacyMap map : getRestrictedAllMaps()) {
				final T matching = (T) map.getFixtures(location).stream()
					.filter(isInstance).map(cast)
					.filter(f -> fixture.isSubset(f, x -> {
					})) // n.b. can't extract, non-denotable type
					.findAny().orElse(null);
				final IFixture.CopyBehavior cb;
				if (first) { // TODO: This should probably be if NOT first ...
					cb = IFixture.CopyBehavior.ZERO;
				} else {
					cb = zero;
				}
				if (!Objects.isNull(matching)) {
					if (all) {
						map.removeFixture(location, matching);
					} else if (matching.getPopulation() > 0) {
						final int remaining = matching.getPopulation() - reduction;
						if (remaining > 0) {
							final T addend = (T) matching.reduced(remaining);
							map.replace(location, matching,
								addend.copy(cb));
							first = false;
							continue;
						} else if (first) {
							all = true;
						}
						map.removeFixture(location, matching);
					} else if (first) {
						break;
					} else {
						map.addFixture(location, fixture.copy(zero));
					}
				} else if (first) {
					break;
				}
				first = false;
			}
		}
	}

	/**
	 * Reduce the acreage of a fixture, and copy the reduced form into all subordinate maps.
	 *
	 * FIXME: Add tests of this and {@link #reducePopulation}, to cover all
	 * possible cases (present, not present, larger, smaller, exactly equal
	 * population/extent, etc., in main and sub-maps in various
	 * combinations ...) In porting I'm not 100% confident the logic here is right.
	 */
	@Override
	public <T extends HasExtent<? extends TileFixture> & TileFixture>
	void reduceExtent(final Point location, final T fixture, final IFixture.CopyBehavior zero, final BigDecimal reduction) {
		if (reduction.signum() > 0) {
			boolean first = false;
			boolean all = false;
			final Predicate<Object> isInstance = fixture.getClass()::isInstance;
			final Function<Object, ? extends HasExtent> cast = fixture.getClass()::cast;
			for (final IMutableLegacyMap map : getRestrictedAllMaps()) {
				final T matching = (T) map.getFixtures(location).stream()
					.filter(isInstance).map(cast).filter(f -> fixture.isSubset(f, x -> {
					}))
					.findAny().orElse(null);
				final IFixture.CopyBehavior cb;
				if (first) { // TODO: Should be NOT first, right?
					cb = IFixture.CopyBehavior.ZERO;
				} else {
					cb = zero;
				}
				if (!Objects.isNull(matching)) {
					if (all) {
						map.removeFixture(location, matching);
					} else if (matching.getAcres().doubleValue() > 0.0) {
						// Precision isn't essential here
						if (matching.getAcres().doubleValue() > reduction.doubleValue()) {
							final T addend = (T)
								matching.reduced(reduction).copy(cb);
							map.replace(location, matching, addend);
							first = false;
							continue;
						} else if (first) {
							all = true;
						}
						map.removeFixture(location, matching);
					} else if (first) {
						break;
					} else {
						map.addFixture(location, fixture.copy(zero));
					}
				} else if (first) {
					break;
				}
				first = false;
			}
		}
	}

	/**
	 * Add a Job to the matching worker in all maps. Returns true if a
	 * matching worker was found in at least one map, false otherwise.  If
	 * an existing Job by that name already existed, it is left alone.
	 */
	@Override
	public boolean addJobToWorker(final IWorker worker, final String jobName) {
		boolean any = false;
		final Predicate<Object> isUnit = IUnit.class::isInstance;
		final Predicate<Object> isWorker = IMutableWorker.class::isInstance;
		final Function<Object, IUnit> unitCast = IUnit.class::cast;
		final Function<Object, IMutableWorker> workerCast = IMutableWorker.class::cast;
		final Predicate<IMutableWorker> matchingRace = w -> w.getRace().equals(worker.getRace());
		final Predicate<IMutableWorker> matchingName = w -> w.getName().equals(worker.getName());
		final Predicate<IMutableWorker> matchingId = w -> w.getId() == worker.getId();
		final Predicate<IJob> matchingJob = j -> jobName.equals(j.getName());
		for (final IMutableLegacyMap map : getRestrictedAllMaps()) {
			final IMutableWorker matching = map.streamAllFixtures()
				.flatMap(TurnRunningModel::unflattenNonFortresses)
				.filter(isUnit).map(unitCast)
				.flatMap(FixtureIterable::stream)
				.filter(isWorker).map(workerCast)
				.filter(matchingRace)
				.filter(matchingName)
				.filter(matchingId).findAny().orElse(null);
			if (!Objects.isNull(matching)) {
				if (StreamSupport.stream(matching.spliterator(), true)
					.noneMatch(matchingJob)) {
					map.setModified(true);
					matching.addJob(new Job(jobName, 0));
				}
				any = true;
			}
		}
		return any;
	}

	/**
	 * Add a skill, without any hours in it, to the specified worker in the
	 * specified Job in all maps. Returns true if a matching worker was
	 * found in at least one map, false otherwise. If no existing Job by
	 * that name already exists, a zero-level Job with that name is added
	 * first. If a Skill by that name already exists in the corresponding
	 * Job, it is left alone.
	 */
	@Override
	public boolean addSkillToWorker(final IWorker worker, final String jobName, final String skillName) {
		boolean any = false;
		final Predicate<Object> isUnit = IUnit.class::isInstance;
		final Predicate<Object> isWorker = IMutableWorker.class::isInstance;
		final Function<Object, IUnit> unitCast = IUnit.class::cast;
		final Function<Object, IMutableWorker> workerCast = IMutableWorker.class::cast;
		final Predicate<IMutableWorker> matchingRace = w -> w.getRace().equals(worker.getRace());
		final Predicate<IMutableWorker> matchingName = w -> w.getName().equals(worker.getName());
		final Predicate<IMutableWorker> matchingId = w -> w.getId() == worker.getId();
		final Predicate<IJob> isMutableJob = IMutableJob.class::isInstance;
		final Function<IJob, IMutableJob> mutableJobCast = IMutableJob.class::cast;
		final Predicate<IJob> matchingJob = j -> jobName.equals(j.getName());
		for (final IMutableLegacyMap map : getRestrictedAllMaps()) {
			final IMutableWorker matching =
				map.streamAllFixtures().flatMap(TurnRunningModel::unflattenNonFortresses)
					.filter(isUnit).map(unitCast)
					.flatMap(IUnit::stream).filter(isWorker).map(workerCast)
					.filter(matchingRace).filter(matchingName)
					.filter(matchingId).findAny().orElse(null);
			if (!Objects.isNull(matching)) {
				final IMutableJob job = StreamSupport.stream(matching.spliterator(), false)
					.filter(isMutableJob).map(mutableJobCast)
					.filter(matchingJob).findAny().orElse(null);
				if (Objects.isNull(job)) {
					map.setModified(true);
					final Job newJob = new Job(jobName, 0);
					newJob.addSkill(new Skill(skillName, 0, 0));
					matching.addJob(newJob);
				} else if (StreamSupport.stream(job.spliterator(), false).map(ISkill::getName).noneMatch(Predicate.isEqual(skillName))) {
					map.setModified(true);
					job.addSkill(new Skill(skillName, 0, 0));
				}
				any = true;
			}
		}
		return any;
	}

	/**
	 * Add a skill, without any hours in it, to all workers in the
	 * specified Job in all maps. Returns true if at least one matching
	 * worker was found in at least one map, false otherwise. If a worker
	 * is in a different unit in some map, the Skill is still added to it.
	 * If no existing Job by that name already exists, a zero-level Job
	 * with that name is added first. If a Skill by that name already
	 * exists in the corresponding Job, it is left alone.
	 */
	@Override
	public boolean addSkillToAllWorkers(final IUnit unit, final String jobName, final String skillName) {
		boolean any = false;
		for (final IWorker worker : unit.stream().filter(IWorker.class::isInstance).map(IWorker.class::cast).toList()) {
			if (addSkillToWorker(worker, jobName, skillName)) {
				any = true;
			}
		}
		return any;
	}

	/**
	 * Add hours to a Skill to the specified Job in all workers in the
	 * given unit in all maps. (If a worker is in a different unit in some
	 * maps, that worker will still receive the hours.) Returns true if at
	 * least one worker received hours, false otherwise. If a worker
	 * doesn't have that skill in that Job, it is added first; if it
	 * doesn't have that Job, it is added first as in {@link
	 * #addJobToWorker}, then the skill is added to it. The "contextValue" is
	 * used to calculate a new value passed to {@link
	 * IMutableSkill#addHours} for each
	 * worker.
	 *
	 * TODO: Take a level-up listener?
	 */
	@Override
	public boolean addHoursToSkillInAll(final IUnit unit, final String jobName, final String skillName,
										final int hours, final int contextValue) {
		boolean any = false;
		final Random rng = new Random(contextValue);
		for (final UnitMember member : unit) {
			if (member instanceof final IWorker w && addHoursToSkill(w, jobName, skillName, hours,
				rng.nextInt(100))) {
				any = true;
			}
		}
		return any;
	}

	/**
	 * Add hours to a Skill to the specified Job in the matching worker in
	 * all maps.  Returns true if a matching worker was found in at least
	 * one map, false otherwise. If the worker doesn't have that Skill in
	 * that Job, it is added first; if the worker doesn't have that Job, it
	 * is added first as in {@link #addJobToWorker}, then the skill is added
	 * to it. The "contextValue" is passed to {@link
	 * IMutableSkill#addHours}; it should
	 * be a random number between 0 and 99.
	 */
	@Override
	public boolean addHoursToSkill(final IWorker worker, final String jobName, final String skillName, final int hours, final int contextValue) {
		boolean any = false;
		final Predicate<Object> isUnit = IUnit.class::isInstance;
		final Predicate<Object> isWorker = IMutableWorker.class::isInstance;
		final Function<Object, IUnit> unitCast = IUnit.class::cast;
		final Function<Object, IMutableWorker> workerCast = IMutableWorker.class::cast;
		final Predicate<IMutableWorker> matchingRace = w -> w.getRace().equals(worker.getRace());
		final Predicate<IMutableWorker> matchingName = w -> w.getName().equals(worker.getName());
		final Predicate<IMutableWorker> matchingId = w -> w.getId() == worker.getId();
		final Predicate<IJob> isMutableJob = IMutableJob.class::isInstance;
		final Function<IJob, IMutableJob> mutableJobCast = IMutableJob.class::cast;
		final Predicate<IJob> matchingJob = j -> jobName.equals(j.getName());
		final Predicate<Object> isMutableSkill = IMutableSkill.class::isInstance;
		final Function<Object, IMutableSkill> mutableSkillCast = IMutableSkill.class::cast;
		final Predicate<IMutableSkill> matchingSkill = s -> skillName.equals(s.getName());
		for (final IMutableLegacyMap map : getRestrictedAllMaps()) {
			final IMutableWorker matching = map.streamAllFixtures()
				.flatMap(TurnRunningModel::unflattenNonFortresses)
				.filter(isUnit).map(unitCast).flatMap(FixtureIterable::stream)
				.filter(isWorker).map(workerCast)
				.filter(matchingRace)
				.filter(matchingName)
				.filter(matchingId).findAny().orElse(null);
			if (!Objects.isNull(matching)) {
				map.setModified(true);
				any = true;
				final IMutableJob job;
				final IMutableJob temp = StreamSupport.stream(matching.spliterator(), false)
					.filter(isMutableJob).map(mutableJobCast)
					.filter(matchingJob).findAny().orElse(null);
				if (Objects.isNull(temp)) {
					job = new Job(jobName, 0);
					matching.addJob(job); // FIXME: addJob() is documented to not guarantee to reuse the object
				} else {
					job = temp;
				}
				final IMutableSkill skill;
				final IMutableSkill tSkill = StreamSupport.stream(job.spliterator(), false)
					.filter(isMutableSkill).map(mutableSkillCast)
					.filter(matchingSkill).findAny().orElse(null);
				if (Objects.isNull(tSkill)) {
					skill = new Skill(skillName, 0, 0);
					job.addSkill(skill); // FIXME: IIRC addSkill() is documented to not guarantee to reuse the object
				} else {
					skill = tSkill;
				}
				skill.addHours(hours, contextValue);
			}
		}
		return any;
	}

	/**
	 * Replace one skill, "delenda" with another, "replacement",
	 * in the specified job in the specified worker in all maps. Unlike
	 * {@link #addHoursToSkill}, if a map does not have an <em>equal</em>
	 * Skill in the matching Job in the matching worker, that map is
	 * completely skipped.  If the replacement is already present, just
	 * remove the first skill. Returns true if the operation was carried
	 * out in any of the maps, false otherwise.
	 */
	@Override
	public boolean replaceSkillInJob(final IWorker worker, final String jobName, final ISkill delenda, final ISkill replacement) {
		boolean any = false;
		final Predicate<Object> isUnit = IUnit.class::isInstance;
		final Predicate<Object> isWorker = IMutableWorker.class::isInstance;
		final Function<Object, IUnit> unitCast = IUnit.class::cast;
		final Function<Object, IMutableWorker> workerCast = IMutableWorker.class::cast;
		final Predicate<IMutableWorker> matchingRace = w -> w.getRace().equals(worker.getRace());
		final Predicate<IMutableWorker> matchingName = w -> w.getName().equals(worker.getName());
		final Predicate<IMutableWorker> matchingId = w -> w.getId() == worker.getId();
		final Predicate<IJob> isMutableJob = IMutableJob.class::isInstance;
		final Function<IJob, IMutableJob> mutableJobCast = IMutableJob.class::cast;
		final Predicate<IJob> matchingJobName = j -> jobName.equals(j.getName());
		final Predicate<Object> isMutableSkill = IMutableSkill.class::isInstance;
		final Function<Object, IMutableSkill> mutableSkillCast = IMutableSkill.class::cast;
		for (final IMutableLegacyMap map : getRestrictedAllMaps()) {
			final IMutableWorker matching = map.streamAllFixtures()
				.flatMap(TurnRunningModel::unflattenNonFortresses)
				.filter(isUnit).map(unitCast).flatMap(FixtureIterable::stream)
				.filter(isWorker).map(workerCast)
				.filter(matchingRace)
				.filter(matchingName)
				.filter(matchingId).findAny().orElse(null);
			if (!Objects.isNull(matching)) {
				final IMutableJob matchingJob = StreamSupport.stream(matching.spliterator(), true)
					.filter(isMutableJob).map(mutableJobCast)
					.filter(matchingJobName).findAny().orElse(null);
				if (!Objects.isNull(matchingJob)) {
					final ISkill matchingSkill = StreamSupport.stream(matchingJob.spliterator(), true)
						.filter(Predicate.isEqual(delenda)).findAny().orElse(null);
					if (!Objects.isNull(matchingSkill)) {
						map.setModified(true);
						any = true;
						matchingJob.removeSkill(matchingSkill);
						matchingJob.addSkill(replacement.copy());
						continue;
					}
				}
				LovelaceLogger.warning("No matching skill in matching worker");
			}
		}
		return any;
	}

	/**
	 * Reduce the matching {@link IResourcePile resource}, in a {@link
	 * legacy.map.fixtures.mobile.IUnit unit} or {@link IFortress fortress}
	 * owned by the specified player, by the
	 * specified amount. Returns true if any (mutable) resource piles
	 * matched in any of the maps, false otherwise.
	 */
	@Override
	public boolean reduceResourceBy(final IResourcePile resource, final BigDecimal amount, final Player owner) {
		boolean any = false;
		for (final IMutableLegacyMap map : getRestrictedAllMaps()) {
			for (final FixtureIterable<?> container : map.streamAllFixtures()
				.flatMap(TurnRunningModel::partiallyFlattenFortresses)
				.filter(HasOwner.class::isInstance).map(HasOwner.class::cast)
				.filter(f -> f.owner().equals(owner))
				.filter(FixtureIterable.class::isInstance).map(FixtureIterable.class::cast).toList()) {
				boolean found = false;
				for (final IMutableResourcePile item : container.stream()
					.filter(IMutableResourcePile.class::isInstance)
					.map(IMutableResourcePile.class::cast).toList()) {
					if (resource.isSubset(item, x -> {
					}) || // TODO: is that the right way around?
						(resource.getKind().equals(item.getKind()) &&
							resource.getContents().equals(item.getContents()) && resource.getId() == item.getId())) {
						final BigDecimal qty = decimalize(item.getQuantity().number());
						if (qty.compareTo(amount) <= 0) {
							if (container instanceof final IMutableUnit unit) {
								unit.removeMember(item);
							} else if (container instanceof final IMutableFortress fort) {
								fort.removeMember(item);
							} else {
								throw new IllegalStateException(
									"Unexpected fixture container type");
							}
						} else {
							item.setQuantity(new LegacyQuantity(qty.subtract(amount),
								resource.getQuantity().units()));
						}
						map.setModified(true);
						any = true;
						found = true;
						break;
					}
				}
				if (found) {
					break;
				}
			}
		}
		return any;
	}

	/**
	 * Remove the given {@link IResourcePile resource} from a {@link
	 * legacy.map.fixtures.mobile.IUnit unit} or {@link
	 * IFortress fortress} owned by
	 * the specified player in all maps. Returns true if any matched in
	 * any of the maps, false otherwise.
	 *
	 * @deprecated Use {@link #reduceResourceBy} when possible instead.
	 */
	@Deprecated
	@Override
	public boolean removeResource(final IResourcePile resource, final Player owner) {
		boolean any = false;
		for (final IMutableLegacyMap map : getRestrictedAllMaps()) {
			for (final FixtureIterable<?> container : map.streamAllFixtures()
				.flatMap(TurnRunningModel::partiallyFlattenFortresses)
				.filter(HasOwner.class::isInstance).map(HasOwner.class::cast)
				.filter(f -> f.owner().equals(owner))
				.filter(FixtureIterable.class::isInstance).map(FixtureIterable.class::cast).toList()) {
				boolean found = false;
				for (final IMutableResourcePile item : container.stream()
					.filter(IMutableResourcePile.class::isInstance)
					.map(IMutableResourcePile.class::cast).toList()) {
					if (resource.isSubset(item, x -> {
					})) { // TODO: is that the right way around?
						if (container instanceof final IMutableUnit unit) {
							unit.removeMember(item);
						} else if (container instanceof final IMutableFortress fort) {
							fort.removeMember(item);
						} else {
							throw new IllegalStateException(
								"Unexpected fixture container type");
						}
						map.setModified(true);
						any = true;
						found = true;
						break;
					}
				}
				if (found) {
					break;
				}
			}
		}
		return any;
	}

	/**
	 * Set the given unit's orders for the given turn to the given text.
	 * Returns true if a matching (and mutable) unit was found in at least
	 * one map, false otherwise.
	 */
	@Override
	public boolean setUnitOrders(final IUnit unit, final int turn, final String results) {
		boolean any = false;
		final Predicate<Object> isUnit = IMutableUnit.class::isInstance;
		final Function<Object, IMutableUnit> unitCast = IMutableUnit.class::cast;
		final Predicate<IMutableUnit> matchingOwner = u -> u.owner().equals(unit.owner());
		final Predicate<IMutableUnit> matchingKind = u -> u.getKind().equals(unit.getKind());
		final Predicate<IMutableUnit> matchingName = u -> u.getName().equals(unit.getName());
		final Predicate<IMutableUnit> matchingId = u -> u.getId() == unit.getId();
		for (final IMutableLegacyMap map : getRestrictedAllMaps()) {
			final IMutableUnit matching = map.streamAllFixtures()
				.flatMap(TurnRunningModel::unflattenNonFortresses)
				.filter(isUnit).map(unitCast)
				.filter(matchingOwner)
				.filter(matchingKind)
				.filter(matchingName)
				.filter(matchingId).findAny().orElse(null);
			if (!Objects.isNull(matching)) {
				matching.setOrders(turn, results);
				map.setModified(true);
				any = true;
			}
		}
		return any;
	}

	/**
	 * Set the given unit's results for the given turn to the given text.
	 * Returns true if a matching (and mutable) unit was found in at least
	 * one map, false otherwise.
	 */
	@Override
	public boolean setUnitResults(final IUnit unit, final int turn, final String results) {
		boolean any = false;
		final Predicate<Object> isUnit = IMutableUnit.class::isInstance;
		final Function<Object, IMutableUnit> unitCast = IMutableUnit.class::cast;
		final Predicate<IMutableUnit> matchingOwner = u -> u.owner().equals(unit.owner());
		final Predicate<IMutableUnit> matchingKind = u -> u.getKind().equals(unit.getKind());
		final Predicate<IMutableUnit> matchingName = u -> u.getName().equals(unit.getName());
		final Predicate<IMutableUnit> matchingId = u -> u.getId() == unit.getId();
		for (final IMutableLegacyMap map : getRestrictedAllMaps()) {
			final IMutableUnit matching = map.streamAllFixtures()
				.flatMap(TurnRunningModel::unflattenNonFortresses)
				.filter(isUnit).map(unitCast)
				.filter(matchingOwner)
				.filter(matchingKind)
				.filter(matchingName)
				.filter(matchingId).findAny().orElse(null);
			if (!Objects.isNull(matching)) {
				matching.setResults(turn, results);
				map.setModified(true);
				any = true;
			}
		}
		return any;
	}

	/**
	 * Add a resource with the given ID, kind, contents, and quantity in
	 * the given unit in all maps.  Returns true if a matching (and
	 * mutable) unit was found in at least one map, false otherwise.
	 */
	@Override
	public boolean addResource(final IUnit container, final int id, final String kind, final String contents, final LegacyQuantity quantity) {
		boolean any = false;
		final IMutableResourcePile resource = new ResourcePileImpl(id, kind, contents, quantity);
		final Predicate<Object> isUnit = IMutableUnit.class::isInstance;
		final Function<Object, IMutableUnit> unitCast = IMutableUnit.class::cast;
//		final Predicate<IMutableUnit> matchingOwner = u -> u.owner().equals(container.owner());
//		final Predicate<IMutableUnit> matchingKind = u -> u.getKind().equals(container.getKind());
		final Predicate<IMutableUnit> matchingName = u -> u.getName().equals(container.getName());
		final Predicate<IMutableUnit> matchingId = u -> u.getId() == container.getId();
		final Consumer<IMutableUnit> addLambda =
			matching -> matching.addMember(resource.copy(IFixture.CopyBehavior.KEEP));
		for (final IMutableLegacyMap map : getRestrictedAllMaps()) {
			// TODO: Match the unit on owner and kind as well as name and ID?
			map.streamAllFixtures()
				.flatMap(TurnRunningModel::unflattenNonFortresses)
				.filter(isUnit).map(unitCast)
//				.filter(matchingOwner)
//				.filter(matchingKind)
				.filter(matchingName)
				.filter(matchingId).findAny()
				.ifPresent(addLambda);
			map.setModified(true);
			any = true;
		}
		return any;
	}

	/**
	 * Add a resource with the given ID, kind, contents, and quantity in
	 * the given fortress in all maps.  Returns true if a matching (and
	 * mutable) fortress was found in at least one map, false otherwise.
	 */
	@Override
	public boolean addResource(final IFortress container, final int id, final String kind, final String contents, final LegacyQuantity quantity) {
		boolean any = false;
		final IMutableResourcePile resource = new ResourcePileImpl(id, kind, contents, quantity);
		final Predicate<Object> isFortress = IMutableFortress.class::isInstance;
		final Function<Object, IMutableFortress> fortressCast = IMutableFortress.class::cast;
		final Predicate<IMutableFortress> matchingName = f -> f.getName().equals(container.getName());
		final Predicate<IMutableFortress> matchingId = f -> f.getId() == container.getId();
		final Consumer<IMutableFortress> addLambda =
			matching -> matching.addMember(resource.copy(IFixture.CopyBehavior.KEEP));
		for (final IMutableLegacyMap map : getRestrictedAllMaps()) {
			// TODO: Match the fortress on owner as well as name and ID?
			map.streamAllFixtures()
				.filter(isFortress).map(fortressCast)
				.filter(matchingName)
				.filter(matchingId).findAny()
				.ifPresent(addLambda);
			map.setModified(true);
			any = true;
		}
		return any;
	}

	/**
	 * Add a resource with the given ID, kind, contents, quantity, and
	 * created date in the given unit in all maps.  Returns true if a
	 * matching (and mutable) unit was found in at least one map, false
	 * otherwise.
	 */
	@Override
	public boolean addResource(final IUnit container, final int id, final String kind, final String contents, final LegacyQuantity quantity,
							   final int createdDate) {
		boolean any = false;
		final IMutableResourcePile resource = new ResourcePileImpl(id, kind, contents, quantity);
		resource.setCreated(createdDate);
		final Predicate<Object> isUnit = IMutableUnit.class::isInstance;
		final Function<Object, IMutableUnit> unitCast = IMutableUnit.class::cast;
//		final Predicate<IMutableUnit> matchingOwner = u -> u.owner().equals(container.owner());
//		final Predicate<IMutableUnit> matchingKind = u -> u.getKind().equals(container.getKind());
		final Predicate<IMutableUnit> matchingName = u -> u.getName().equals(container.getName());
		final Predicate<IMutableUnit> matchingId = u -> u.getId() == container.getId();
		final Consumer<IMutableUnit> addLambda =
			matching -> matching.addMember(resource.copy(IFixture.CopyBehavior.KEEP));
		for (final IMutableLegacyMap map : getRestrictedAllMaps()) {
			// TODO: Match the unit on owner and kind as well as name and ID?
			map.streamAllFixtures()
				.flatMap(TurnRunningModel::unflattenNonFortresses)
				.filter(isUnit).map(unitCast)
//				.filter(matchingOwner)
//				.filter(matchingKind)
				.filter(matchingName)
				.filter(matchingId).findAny()
				.ifPresent(addLambda);
			map.setModified(true);
			any = true;
		}
		return any;
	}

	/**
	 * Add a resource with the given ID, kind, contents, quantity, and
	 * created date in the given fortress in all maps.  Returns true if a
	 * matching (and mutable) fortress was found in at least one map, false
	 * otherwise.
	 */
	@Override
	public boolean addResource(final IFortress container, final int id, final String kind, final String contents, final LegacyQuantity quantity,
							   final int createdDate) {
		boolean any = false;
		final IMutableResourcePile resource = new ResourcePileImpl(id, kind, contents, quantity);
		resource.setCreated(createdDate);
		final Predicate<Object> isFortress = IMutableFortress.class::isInstance;
		final Function<Object, IMutableFortress> fortressCast = IMutableFortress.class::cast;
		final Predicate<IMutableFortress> matchingName = f -> f.getName().equals(container.getName());
		final Predicate<IMutableFortress> matchingId = f -> f.getId() == container.getId();
		final Consumer<IMutableFortress> addLambda = matching ->
			matching.addMember(resource.copy(IFixture.CopyBehavior.KEEP));
		for (final IMutableLegacyMap map : getRestrictedAllMaps()) {
			// TODO: Match the fortress on owner as well as name and ID?
			map.streamAllFixtures()
				.filter(isFortress).map(fortressCast)
				.filter(matchingName)
				.filter(matchingId).findAny()
				.ifPresent(addLambda);
			map.setModified(true);
			any = true;
		}
		return any;
	}

	/**
	 * Add a non-talking animal population to the given unit in all maps.
	 * Returns true if the input makes sense and a matching (and mutable)
	 * unit was found in at least one map, false otherwise.
	 *
	 * Note the last two parameters are <em>reversed</em> from the {@link
	 * legacy.map.fixtures.mobile.AnimalImpl} constructor, to better fit the needs of <em>our</em> callers.
	 */
	@Override
	public boolean addAnimal(final IUnit container, final String kind, final String status, final int id, final int population, final int born) {
		if (population <= 0) {
			return false;
		}
		final Animal animal = new AnimalImpl(kind, false, status, id, born, population);
		boolean any = false;
		final Predicate<Object> isUnit = IMutableUnit.class::isInstance;
		final Function<Object, IMutableUnit> unitCast = IMutableUnit.class::cast;
		final Predicate<IMutableUnit> matchingOwner = u -> u.owner().equals(container.owner());
		final Predicate<IMutableUnit> matchingKind = u -> u.getKind().equals(container.getKind());
		final Predicate<IMutableUnit> matchingName = u -> u.getName().equals(container.getName());
		final Predicate<IMutableUnit> matchingId = u -> u.getId() == container.getId();
		for (final IMutableLegacyMap map : getRestrictedAllMaps()) {
			final IMutableUnit matching = map.streamAllFixtures()
				.flatMap(TurnRunningModel::unflattenNonFortresses)
				.filter(isUnit).map(unitCast)
				.filter(matchingOwner)
				.filter(matchingKind)
				.filter(matchingName)
				.filter(matchingId).findAny().orElse(null);
			if (!Objects.isNull(matching)) {
				matching.addMember(animal.copy(IFixture.CopyBehavior.KEEP));
				any = true;
				map.setModified(true);
			}
		}
		return any;
	}

	/** Transfer "quantity" units from a resource to (if
	 * not all of it) another resource in the given unit in
	 * all maps. If this leaves any behind in any map, "idFactory"
	 * will be called exactly once to generate the ID number for the
	 * resource in the destination in maps where that is the case. Returns
	 * true if a matching (mutable) resource and destination are found (and
	 * the transfer occurs) in any map, false otherwise.
	 */
	@Override
	public boolean transferResource(final IResourcePile from, final IUnit to, final BigDecimal quantity, final IntSupplier idFactory) {
		boolean any = false;
		final IntSupplier id = new GenerateOnce(idFactory);
		final Predicate<Object> isResource = IMutableResourcePile.class::isInstance;
		final Function<Object, IMutableResourcePile> mrpCast = IMutableResourcePile.class::cast;
		final Predicate<IMutableResourcePile> matchingPileKind = r -> r.getKind().equals(from.getKind());
		final Predicate<IMutableResourcePile> matchingPileContents = r -> r.getContents().equals(from.getContents());
		final Predicate<IMutableResourcePile> matchingPileAge = r -> r.getCreated() == from.getCreated();
		final Predicate<IMutableResourcePile> matchingPileUnits = r -> r.getQuantity().units().equals(from.getQuantity().units());
		final Predicate<IMutableResourcePile> matchingPileId = r -> r.getId() == from.getId();
		final Predicate<Object> isUnit = IMutableUnit.class::isInstance;
		final Function<Object, IMutableUnit> unitCast = IMutableUnit.class::cast;
		final Predicate<IMutableUnit> matchingUnitName = u -> u.getName().equals(to.getName());
		final Predicate<IMutableUnit> matchingUnitId = u -> u.getId() == to.getId();

		for (final IMutableLegacyMap map : getRestrictedAllMaps()) {
			for (final FixtureIterable<?> container : map.streamAllFixtures()
				.flatMap(TurnRunningModel::partiallyFlattenFortresses)
				.filter(FixtureIterable.class::isInstance)
				.filter(HasOwner.class::isInstance)
				.map(HasOwner.class::cast)
				.filter(f -> f.owner().equals(to.owner()))
				.map(FixtureIterable.class::cast).toList()) {
				final IMutableResourcePile matching = container.stream()
					.filter(isResource).map(mrpCast).filter(matchingPileKind)
					.filter(matchingPileContents).filter(matchingPileAge).filter(matchingPileUnits)
					.filter(matchingPileId).findAny().orElse(null);
				// TODO: Match destination by owner and kind, not just name and ID?
				final IMutableUnit destination = map.streamAllFixtures()
					.flatMap(TurnRunningModel::partiallyFlattenFortresses)
					.filter(isUnit).map(unitCast)
					.filter(matchingUnitName)
					.filter(matchingUnitId).findAny().orElse(null);
				if (!Objects.isNull(matching) && !Objects.isNull(destination)) {
					map.setModified(true);
					if (quantity.doubleValue() >= matching.getQuantity().number().doubleValue()) {
						if (container instanceof final IMutableFortress fort) { // TODO: Combine with other block when a supertype is added for this method
							fort.removeMember(matching);
						} else if (container instanceof final IMutableUnit unit) {
							unit.removeMember(matching);
						} else {
							throw new IllegalStateException("Unexpected fixture-container type");
						}
						destination.addMember(matching);
					} else {
						final IMutableResourcePile split = new ResourcePileImpl(id.getAsInt(),
							matching.getKind(), matching.getContents(),
							new LegacyQuantity(quantity, matching.getQuantity().units()));
						split.setCreated(matching.getCreated());
						matching.setQuantity(new LegacyQuantity(decimalize(matching.getQuantity()
							.number()).subtract(quantity), matching.getQuantity().units()));
					}
					any = true;
					break;
				}
			}
		}
		return any;
	}

	/**
	 * Transfer "quantity" units from a resource to (if
	 * not all of it) another resource in the specified fortress in all
	 * maps. If this leaves any behind in any map, "idFactory" will
	 * be called exactly once to generate the ID number for the resource in
	 * the destination in maps where that is the case. Returns true if a
	 * matching (mutable) resource and destination are found (and the
	 * transfer occurs) in any map, false otherwise.
	 */
	@Override
	public boolean transferResource(final IResourcePile from, final IFortress to, final BigDecimal quantity, final IntSupplier idFactory) {
		boolean any = false;
		final IntSupplier id = new GenerateOnce(idFactory);
		final Predicate<Object> isResource = IMutableResourcePile.class::isInstance;
		final Function<Object, IMutableResourcePile> mrpCast = IMutableResourcePile.class::cast;
		final Predicate<IMutableResourcePile> matchingPileKind = r -> r.getKind().equals(from.getKind());
		final Predicate<IMutableResourcePile> matchingPileContents = r -> r.getContents().equals(from.getContents());
		final Predicate<IMutableResourcePile> matchingPileAge = r -> r.getCreated() == from.getCreated();
		final Predicate<IMutableResourcePile> matchingPileUnits = r -> r.getQuantity().units().equals(from.getQuantity().units());
		final Predicate<IMutableResourcePile> matchingPileId = r -> r.getId() == from.getId();
		final Predicate<Object> isFortress = IMutableFortress.class::isInstance;
		final Function<Object, IMutableFortress> fortressCast = IMutableFortress.class::cast;
		final Predicate<IMutableFortress> matchingFortName = f -> f.getName().equals(to.getName());
		final Predicate<IMutableFortress> matchingFortId = f -> f.getId() == to.getId();

		for (final IMutableLegacyMap map : getRestrictedAllMaps()) {
			for (final FixtureIterable<?> container : map.streamAllFixtures()
				.flatMap(TurnRunningModel::partiallyFlattenFortresses)
				.filter(FixtureIterable.class::isInstance)
				.filter(HasOwner.class::isInstance)
				.map(HasOwner.class::cast)
				.filter(f -> f.owner().equals(to.owner()))
				.map(FixtureIterable.class::cast).toList()) {
				final IMutableResourcePile matching = container.stream()
					.filter(isResource).map(mrpCast)
					.filter(matchingPileKind)
					.filter(matchingPileContents)
					.filter(matchingPileAge)
					.filter(matchingPileUnits)
					.filter(matchingPileId).findAny().orElse(null);
				// TODO: Match destination by owner and kind, not just name and ID?
				final IMutableFortress destination = map.streamAllFixtures()
					.filter(isFortress).map(fortressCast)
					.filter(matchingFortName)
					.filter(matchingFortId).findAny().orElse(null);
				if (!Objects.isNull(matching) && !Objects.isNull(destination)) {
					map.setModified(true);
					if (quantity.doubleValue() >= matching.getQuantity().number().doubleValue()) {
						if (container instanceof final IMutableFortress fort) { // TODO: Combine with other block when a supertype is added for this method
							fort.removeMember(matching);
						} else if (container instanceof final IMutableUnit unit) {
							unit.removeMember(matching);
						} else {
							throw new IllegalStateException("Unexpected fixture-container type");
						}
						destination.addMember(matching);
					} else {
						final IMutableResourcePile split = new ResourcePileImpl(id.getAsInt(),
							matching.getKind(), matching.getContents(),
							new LegacyQuantity(quantity, matching.getQuantity().units()));
						split.setCreated(matching.getCreated());
						matching.setQuantity(new LegacyQuantity(decimalize(matching.getQuantity()
							.number()).subtract(quantity), matching.getQuantity().units()));
					}
					any = true;
					break;
				}
			}
		}
		return any;
	}

	/**
	 * Add (a copy of) an existing resource to the fortress belonging to
	 * the given player with the given name, or failing that to any
	 * fortress belonging to the given player, in all maps. Returns true if
	 * a matching (and mutable) fortress ws found in at least one map,
	 * false otherwise.
	 */
	// TODO: Make a way to add to units
	@Override
	public boolean addExistingResource(final FortressMember resource, final Player owner, final String fortName) {
		boolean any = false;
		final Function<IMutableLegacyMap, Stream<IMutableFortress>> supp =
			map -> map.streamAllFixtures()
				.filter(IMutableFortress.class::isInstance).map(IMutableFortress.class::cast)
				.filter(f -> f.owner().equals(owner));
		final Predicate<IMutableFortress> matchingName = f -> fortName.equals(f.getName());

		for (final IMutableLegacyMap map : getRestrictedAllMaps()) {
			final IMutableFortress result = supp.apply(map).filter(matchingName).findAny()
				.orElseGet(() -> supp.apply(map).findAny().orElse(null));
			if (Objects.isNull(result)) {
				continue;
			}
			any = true;
			map.setModified(true);
			result.addMember(resource.copy(IFixture.CopyBehavior.KEEP));
		}
		return any;
	}

	private static class GenerateOnce implements IntSupplier {
		private final IntSupplier idFactory;
		private @Nullable Integer generatedId;

		public GenerateOnce(final IntSupplier idFactory) {
			this.idFactory = idFactory;
			generatedId = null;
		}

		@Override
		public int getAsInt() {
			if (Objects.isNull(generatedId)) {
				final int temp = idFactory.getAsInt();
				generatedId = temp;
				return temp;
			} else {
				return generatedId;
			}
		}
	}
}
