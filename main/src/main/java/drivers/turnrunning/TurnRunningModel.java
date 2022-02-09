package drivers.turnrunning;

import common.map.HasExtent;
import common.map.HasOwner;
import common.map.HasPopulation;
import common.map.IFixture;
import common.map.IMutableMapNG;
import common.map.Player;
import common.map.Point;
import common.map.TileFixture;
import common.map.fixtures.FixtureIterable;
import common.map.fixtures.FortressMember;
import common.map.fixtures.IMutableResourcePile;
import common.map.fixtures.IResourcePile;
import common.map.fixtures.Quantity;
import common.map.fixtures.ResourcePileImpl;
import common.map.fixtures.mobile.Animal;
import common.map.fixtures.mobile.AnimalImpl;
import common.map.fixtures.mobile.IMutableUnit;
import common.map.fixtures.mobile.IMutableWorker;
import common.map.fixtures.mobile.IUnit;
import common.map.fixtures.mobile.IWorker;
import common.map.fixtures.mobile.worker.IMutableJob;
import common.map.fixtures.mobile.worker.IMutableSkill;
import common.map.fixtures.mobile.worker.ISkill;
import common.map.fixtures.mobile.worker.Job;
import common.map.fixtures.mobile.worker.Skill;
import common.map.fixtures.towns.IFortress;
import common.map.fixtures.towns.IMutableFortress;
import drivers.common.IDriverModel;
import exploration.common.ExplorationModel;
import java.math.BigDecimal;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static lovelace.util.Decimalize.decimalize;

public class TurnRunningModel extends ExplorationModel implements ITurnRunningModel {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(TurnRunningModel.class.getName());

	/**
	 * If {@link fixture} is a {@link IFortress fortress}, return a stream
	 * of its contents; otherwise, return stream containing only it. This
	 * is intended to be used in {@link Stream#flatMap}.
	 */
	private static Stream<IFixture> unflattenNonFortresses(final TileFixture fixture) {
		if (fixture instanceof IFortress) {
			return ((IFortress) fixture).stream().map(IFixture.class::cast);
		} else {
			return Stream.of(fixture);
		}
	}

	/**
	 * If {@link fixture} is a {@link IFortress fortress}, return a stream
	 * of it and its contents; otherwise, return a stream of only it. This
	 * is intended to be used in {@link Stream#flatMap}.
	 */
	private static Stream<IFixture> partiallyFlattenFortresses(final TileFixture fixture) {
		if (fixture instanceof IFortress) {
			return Stream.concat(Stream.of(fixture), ((IFortress) fixture).stream());
		} else {
			return Stream.of(fixture);
		}
	}

	public TurnRunningModel(final IMutableMapNG map) {
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
	public void addToSubMaps(final Point point, final TileFixture fixture, final boolean zero) {
		for (final IMutableMapNG map : getRestrictedSubordinateMaps()) {
			if (map.getFixtures(point).stream().mapToInt(TileFixture::getId)
					.noneMatch(i -> fixture.getId() == i)) {
				map.addFixture(point, fixture.copy(zero));
			}
		}
	}

	/**
	 * Reduce the population of a group of plants, animals, etc., and copy
	 * the reduced form into all subordinate maps.
	 */
	@Override
	public <T extends HasPopulation<? extends TileFixture>&TileFixture>
			void reducePopulation(final Point location, final T fixture, final boolean zero, final int reduction) {
		if (reduction > 0) {
			boolean first = false;
			boolean all = false;
			for (final IMutableMapNG map : getRestrictedAllMaps()) {
				final T matching = (T) map.getFixtures(location).stream()
					.filter(fixture.getClass()::isInstance).map(fixture.getClass()::cast)
						.filter(f -> fixture.isSubset(f, x -> {}))
					.findAny().orElse(null);
				if (matching != null) {
					if (all) {
						map.removeFixture(location, matching);
					} else if (((HasPopulation<? extends TileFixture>) matching).getPopulation() > 0) {
						final int remaining = matching.getPopulation() - reduction;
						if (remaining > 0) {
							final T addend = (T) matching.reduced(remaining);
							map.replace(location, matching,
								(TileFixture) addend.copy(first || zero));
							// FIXME: first = false here, right?
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
	 * FIXME: Add tests of this and {@link reducePopulation}, to cover all
	 * possible cases (present, not present, larger, smaller, exactly equal
	 * population/extent, etc., in main and sub-maps in various
	 * combinations ...) In porting I'm not 100% confident the logic here is right.
	 */
	@Override
	public <T extends HasExtent<? extends TileFixture>&TileFixture>
		void reduceExtent(final Point location, final T fixture, final boolean zero, final BigDecimal reduction) {
		if (reduction.compareTo(BigDecimal.ZERO) > 0) {
			boolean first = false;
			boolean all = false;
			for (final IMutableMapNG map : getRestrictedAllMaps()) {
				final T matching = (T) map.getFixtures(location).stream()
					.filter(fixture.getClass()::isInstance).map(fixture.getClass()::cast).filter(f -> fixture.isSubset(f, x -> {}))
					.findAny().orElse(null);
				if (matching != null) {
					if (all) {
						map.removeFixture(location, matching);
					} else if (matching.getAcres().doubleValue() > 0.0) {
						// Precision isn't essential here
						if (matching.getAcres().doubleValue() > reduction.doubleValue()) {
							final T addend = (T)
								matching.reduced(reduction).copy(first || zero);
							map.replace(location, matching, (TileFixture) addend);
							// FIXME: first = false here, right?
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
		for (final IMutableMapNG map : getRestrictedAllMaps()) {
			final IMutableWorker matching = map.streamAllFixtures()
				.flatMap(TurnRunningModel::unflattenNonFortresses)
				.filter(IUnit.class::isInstance).map(IUnit.class::cast)
				.filter(u -> u.getOwner().equals(map.getCurrentPlayer()))
				.flatMap(FixtureIterable::stream)
				.filter(IMutableWorker.class::isInstance).map(IMutableWorker.class::cast)
				.filter(w -> w.getRace().equals(worker.getRace()))
				.filter(w -> w.getName().equals(worker.getName()))
				.filter(w -> w.getId() == worker.getId()).findAny().orElse(null);
			if (matching != null) {
				if (StreamSupport.stream(matching.spliterator(), true)
						.noneMatch(j -> jobName.equals(j.getName()))) {
					map.setModified(true);
					matching.addJob(new Job(jobName, 0));
				}
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
	 * is added first as in {@link addJobToWorker}, then the skill is added
	 * to it. The {@link contextValue} is passed to {@link
	 * common.map.fixtures.mobile.worker.IMutableSkill#addHours}; it should
	 * be a random number between 0 and 99.
	 */
	@Override
	public boolean addHoursToSkill(final IWorker worker, final String jobName, final String skillName, final int hours, final int contextValue) {
		boolean any = false;
		for (final IMutableMapNG map : getRestrictedAllMaps()) {
			final IMutableWorker matching = map.streamAllFixtures()
				.flatMap(TurnRunningModel::unflattenNonFortresses)
				.filter(IUnit.class::isInstance).map(IUnit.class::cast)
				.filter(u -> u.getOwner().equals(map.getCurrentPlayer())).flatMap(FixtureIterable::stream)
				.filter(IMutableWorker.class::isInstance).map(IMutableWorker.class::cast)
				.filter(w -> w.getRace().equals(worker.getRace()))
				.filter(w -> w.getName().equals(worker.getName()))
				.filter(w -> w.getId() == worker.getId()).findAny().orElse(null);
			if (matching != null) {
				map.setModified(true);
				any = true;
				final IMutableJob job;
				final IMutableJob temp = StreamSupport.stream(matching.spliterator(), false)
						.filter(IMutableJob.class::isInstance).map(IMutableJob.class::cast)
						.filter(j -> jobName.equals(j.getName())).findAny().orElse(null);
				if (temp == null) {
					job = new Job(jobName, 0);
					matching.addJob(job); // FIXME: addJob() is documented to not guarantee to reuse the object
				} else {
					job = temp;
				}
				final IMutableSkill skill;
				final IMutableSkill tSkill = StreamSupport.stream(job.spliterator(), false)
					.filter(IMutableSkill.class::isInstance).map(IMutableSkill.class::cast)
					.filter(s -> skillName.equals(s.getName())).findAny().orElse(null);
				if (tSkill == null) {
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
	 * Replace {@link delenda one skill} with {@link replacement another}
	 * in the specified job in the specified worker in all maps. Unlike
	 * {@link addHoursToSkill}, if a map does not have an <em>equal</em>
	 * Skill in the matching Job in the matching worker, that map is
	 * completely skipped.  If the replacement is already present, just
	 * remove the first skill. Returns true if the operation was carried
	 * out in any of the maps, false otherwise.
	 */
	@Override
	public boolean replaceSkillInJob(final IWorker worker, final String jobName, final ISkill delenda, final ISkill replacement) {
		boolean any = false;
		for (final IMutableMapNG map : getRestrictedAllMaps()) {
			final IMutableWorker matching = map.streamAllFixtures()
				.flatMap(TurnRunningModel::unflattenNonFortresses)
				.filter(IUnit.class::isInstance).map(IUnit.class::cast)
				.filter(u -> u.getOwner().equals(map.getCurrentPlayer())).flatMap(FixtureIterable::stream)
				.filter(IMutableWorker.class::isInstance).map(IMutableWorker.class::cast)
				.filter(w -> w.getRace().equals(worker.getRace()))
				.filter(w -> w.getName().equals(worker.getName()))
				.filter(w -> w.getId() == worker.getId()).findAny().orElse(null);
			if (matching != null) {
				final IMutableJob matchingJob = StreamSupport.stream(matching.spliterator(), true)
					.filter(IMutableJob.class::isInstance).map(IMutableJob.class::cast)
					.filter(j -> jobName.equals(j.getName())).findAny().orElse(null);
				if (matchingJob != null) {
					final ISkill matchingSkill = StreamSupport.stream(matchingJob.spliterator(), true)
						.filter(delenda::equals).findAny().orElse(null);
					if (matchingSkill != null) {
						map.setModified(true);
						any = true;
						matchingJob.removeSkill(matchingSkill);
						matchingJob.addSkill(replacement.copy());
						continue;
					}
				}
				LOGGER.warning("No matching skill in matching worker");
			}
		}
		return any;
	}

	/**
	 * Reduce the matching {@link IResourcePile resource}, in a {@link
	 * common.map.fixtures.mobile.IUnit unit} or {@link IFortress fortress}
	 * owned by {@link owner the specified player}, by {@link amount the
	 * specified amount}. Returns true if any (mutable) resource piles
	 * matched in any of the maps, false otherwise.
	 */
	@Override
	public boolean reduceResourceBy(final IResourcePile resource, final BigDecimal amount, final Player owner) {
		boolean any = false;
		for (final IMutableMapNG map : getRestrictedAllMaps()) {
			for (final FixtureIterable<?> container : map.streamAllFixtures()
					.flatMap(TurnRunningModel::partiallyFlattenFortresses)
					.filter(HasOwner.class::isInstance).map(HasOwner.class::cast)
					.filter(f -> f.getOwner().equals(owner))
					.filter(FixtureIterable.class::isInstance).map(FixtureIterable.class::cast)
					.collect(Collectors.toList())) {
				boolean found = false;
				for (final IMutableResourcePile item : container.stream()
						.filter(IMutableResourcePile.class::isInstance)
						.map(IMutableResourcePile.class::cast)
						.collect(Collectors.toList())) {
					if (resource.isSubset(item, x -> {})) { // TODO: is that the right way around?
						final BigDecimal qty = decimalize(item.getQuantity().getNumber());
						if (qty.compareTo(amount) <= 0) {
							if (container instanceof IMutableUnit) {
								((IMutableUnit) container).removeMember(item);
							} else if (container instanceof IMutableFortress) {
								((IMutableFortress) container).removeMember(item);
							} else {
								throw new IllegalStateException(
									"Unexpected fixture container type");
							}
						} else {
							item.setQuantity(new Quantity(qty.subtract(amount),
								resource.getQuantity().getUnits()));
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
	 * common.map.fixtures.mobile.IUnit unit} or {@link
	 * common.map.fixtures.towns.IFortress fortress} owned by {@link owner
	 * the specified player} in all maps. Returns true if any matched in
	 * any of the maps, false otherwise.
	 *
	 * @deprecated Use {@link reduceResourceBy} when possible instead.
	 */
	@Deprecated
	@Override
	public boolean removeResource(final IResourcePile resource, final Player owner) {
		boolean any = false;
		for (final IMutableMapNG map : getRestrictedAllMaps()) {
			for (final FixtureIterable<?> container : map.streamAllFixtures()
					.flatMap(TurnRunningModel::partiallyFlattenFortresses)
					.filter(HasOwner.class::isInstance).map(HasOwner.class::cast)
					.filter(f -> f.getOwner().equals(owner))
					.filter(FixtureIterable.class::isInstance).map(FixtureIterable.class::cast)
					.collect(Collectors.toList())) {
				boolean found = false;
				for (final IMutableResourcePile item : container.stream()
						.filter(IMutableResourcePile.class::isInstance)
						.map(IMutableResourcePile.class::cast)
						.collect(Collectors.toList())) {
					if (resource.isSubset(item, x -> {})) { // TODO: is that the right way around?
						if (container instanceof IMutableUnit) {
							((IMutableUnit) container).removeMember(item);
						} else if (container instanceof IMutableFortress) {
							((IMutableFortress) container).removeMember(item);
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
		for (final IMutableMapNG map : getRestrictedAllMaps()) {
			final IMutableUnit matching = map.streamAllFixtures()
				.flatMap(TurnRunningModel::unflattenNonFortresses)
				.filter(IMutableUnit.class::isInstance).map(IMutableUnit.class::cast)
				.filter(u -> u.getOwner().equals(unit.getOwner()))
				.filter(u -> u.getKind().equals(unit.getKind()))
				.filter(u -> u.getName().equals(unit.getName()))
				.filter(u -> u.getId() == unit.getId()).findAny().orElse(null);
			if (matching != null) {
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
		for (final IMutableMapNG map : getRestrictedAllMaps()) {
			final IMutableUnit matching = map.streamAllFixtures()
				.flatMap(TurnRunningModel::unflattenNonFortresses)
				.filter(IMutableUnit.class::isInstance).map(IMutableUnit.class::cast)
				.filter(u -> u.getOwner().equals(unit.getOwner()))
				.filter(u -> u.getKind().equals(unit.getKind()))
				.filter(u -> u.getName().equals(unit.getName()))
				.filter(u -> u.getId() == unit.getId()).findAny().orElse(null);
			if (matching != null) {
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
	public boolean addResource(final IUnit container, final int id, final String kind, final String contents, final Quantity quantity) {
		boolean any = false;
		final IMutableResourcePile resource = new ResourcePileImpl(id, kind, contents, quantity);
		for (final IMutableMapNG map : getRestrictedAllMaps()) {
			// TODO: Match the unit on owner and kind as well as name and ID?
			map.streamAllFixtures()
					.flatMap(TurnRunningModel::unflattenNonFortresses)
					.filter(IMutableUnit.class::isInstance).map(IMutableUnit.class::cast)
//				.filter(u -> u.getOwner().equals(container.getOwner()))
//				.filter(u -> u.getKind().equals(container.getKind()))
					.filter(u -> u.getName().equals(container.getName()))
					.filter(u -> u.getId() == container.getId()).findAny()
					.ifPresent(matching -> matching.addMember(resource.copy(false)));
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
	public boolean addResource(final IFortress container, final int id, final String kind, final String contents, final Quantity quantity) {
		boolean any = false;
		final IMutableResourcePile resource = new ResourcePileImpl(id, kind, contents, quantity);
		for (final IMutableMapNG map : getRestrictedAllMaps()) {
			// TODO: Match the fortress on owner as well as name and ID?
			map.streamLocations()
					.filter(IMutableFortress.class::isInstance).map(IMutableFortress.class::cast)
					.filter(f -> f.getName().equals(container.getName()))
					.filter(f -> f.getId() == container.getId()).findAny()
					.ifPresent(matching -> matching.addMember(resource.copy(false)));
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
	public boolean addResource(final IUnit container, final int id, final String kind, final String contents, final Quantity quantity,
	                           final int createdDate) {
		boolean any = false;
		final IMutableResourcePile resource = new ResourcePileImpl(id, kind, contents, quantity);
		resource.setCreated(createdDate);
		for (final IMutableMapNG map : getRestrictedAllMaps()) {
			// TODO: Match the unit on owner and kind as well as name and ID?
			map.streamAllFixtures()
					.flatMap(TurnRunningModel::unflattenNonFortresses)
					.filter(IMutableUnit.class::isInstance).map(IMutableUnit.class::cast)
//				.filter(u -> u.getOwner().equals(container.getOwner()))
//				.filter(u -> u.getKind().equals(container.getKind()))
					.filter(u -> u.getName().equals(container.getName()))
					.filter(u -> u.getId() == container.getId()).findAny()
					.ifPresent(matching -> matching.addMember(resource.copy(false)));
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
	public boolean addResource(final IFortress container, final int id, final String kind, final String contents, final Quantity quantity,
	                           final int createdDate) {
		boolean any = false;
		final IMutableResourcePile resource = new ResourcePileImpl(id, kind, contents, quantity);
		resource.setCreated(createdDate);
		for (final IMutableMapNG map : getRestrictedAllMaps()) {
			// TODO: Match the fortress on owner as well as name and ID?
			map.streamLocations()
					.filter(IMutableFortress.class::isInstance).map(IMutableFortress.class::cast)
					.filter(f -> f.getName().equals(container.getName()))
					.filter(f -> f.getId() == container.getId()).findAny()
					.ifPresent(matching -> matching.addMember(resource.copy(false)));
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
	 * common.map.fixtures.mobile.AnimalImpl} constructor, to better fit the needs of <em>our</em> callers.
	 */
	@Override
	public boolean addAnimal(final IUnit container, final String kind, final String status, final int id, final int population, final int born) {
		if (population <= 0) {
			return false;
		}
		final Animal animal = new AnimalImpl(kind, false, status, id, born, population);
		boolean any = false;
		for (final IMutableMapNG map : getRestrictedAllMaps()) {
			final IMutableUnit matching = map.streamAllFixtures()
				.flatMap(TurnRunningModel::unflattenNonFortresses)
				.filter(IMutableUnit.class::isInstance).map(IMutableUnit.class::cast)
				.filter(u -> u.getOwner().equals(container.getOwner()))
				.filter(u -> u.getKind().equals(container.getKind()))
				.filter(u -> u.getName().equals(container.getName()))
				.filter(u -> u.getId() == container.getId()).findAny().orElse(null);
			if (matching != null) {
				matching.addMember(animal.copy(false));
				any = true;
				map.setModified(true);
			}
		}
		return any;
	}

	/** Transfer {@link quantity} units from {@link from a resource} to (if
	 * not all of it) another resource in {@link to a unit } in
	 * all maps. If this leaves any behind in any map, {@link idFactory}
	 * will be called exactly once to generate the ID number for the
	 * resource in the destination in maps where that is the case. Returns
	 * true if a matching (mutable) resource and destination are found (and
	 * the transfer occurs) in any map, false otherwise.
	 */
	@Override
	public boolean transferResource(final IResourcePile from, final IUnit to, final BigDecimal quantity, final IntSupplier idFactory) {
		boolean any = false;
		final IntSupplier id = new GenerateOnce(idFactory);

		for (final IMutableMapNG map : getRestrictedAllMaps()) {
			for (final FixtureIterable<?> container : map.streamAllFixtures()
					.flatMap(TurnRunningModel::partiallyFlattenFortresses)
					.filter(FixtureIterable.class::isInstance)
					.filter(HasOwner.class::isInstance)
					.map(HasOwner.class::cast)
					.filter(f -> f.getOwner().equals(to.getOwner()))
					.map(FixtureIterable.class::cast).collect(Collectors.toList())) {
				final IMutableResourcePile matching = container.stream()
					.filter(IMutableResourcePile.class::isInstance).map(IMutableResourcePile.class::cast)
					.filter(r -> r.getKind().equals(from.getKind()))
					.filter(r -> r.getContents().equals(from.getContents()))
					.filter(r -> r.getCreated() == from.getCreated())
					.filter(r -> r.getQuantity().getUnits().equals(from.getQuantity().getUnits()))
					.filter(r -> r.getId() == from.getId()).findAny().orElse(null);
				// TODO: Match destination by owner and kind, not just name and ID?
				final IMutableUnit destination = map.streamAllFixtures()
					.flatMap(TurnRunningModel::partiallyFlattenFortresses)
					.filter(IMutableUnit.class::isInstance).map(IMutableUnit.class::cast)
					.filter(u -> u.getName().equals(to.getName()))
					.filter(u -> u.getId() == to.getId()).findAny().orElse(null);
				if (matching != null && destination != null) {
					map.setModified(true);
					if (quantity.doubleValue() >= matching.getQuantity().getNumber().doubleValue()) {
						if (container instanceof IMutableFortress) { // TODO: Combine with other block when a supertype is added for this method
							((IMutableFortress) container).removeMember(matching);
						} else if (container instanceof IMutableUnit) {
							((IMutableUnit) container).removeMember(matching);
						} else {
							throw new IllegalStateException("Unexpected fixture-container type");
						}
						destination.addMember(matching);
					} else {
						final IMutableResourcePile split = new ResourcePileImpl(id.getAsInt(),
							matching.getKind(), matching.getContents(),
							new Quantity(quantity, matching.getQuantity().getUnits()));
						split.setCreated(matching.getCreated());
						matching.setQuantity(new Quantity(decimalize(matching.getQuantity()
							.getNumber()).subtract(quantity), matching.getQuantity().getUnits()));
					}
					any = true;
					break;
				}
			}
		}
		return any;
	}

	/**
	 * Transfer {@link quantity} units from {@link from a resource} to (if
	 * not all of it) another resource in {@link to a fortress} in all
	 * maps. If this leaves any behind in any map, {@link idFactory} will
	 * be called exactly once to generate the ID number for the resource in
	 * the destination in maps where that is the case. Returns true if a
	 * matching (mutable) resource and destination are found (and the
	 * transfer occurs) in any map, false otherwise.
	 */
	@Override
	public boolean transferResource(final IResourcePile from, final IFortress to, final BigDecimal quantity, final IntSupplier idFactory) {
		boolean any = false;
		final IntSupplier id = new GenerateOnce(idFactory);

		for (final IMutableMapNG map : getRestrictedAllMaps()) {
			for (final FixtureIterable<?> container : map.streamAllFixtures()
					.flatMap(TurnRunningModel::partiallyFlattenFortresses)
					.filter(FixtureIterable.class::isInstance)
					.filter(HasOwner.class::isInstance)
					.map(HasOwner.class::cast)
					.filter(f -> f.getOwner().equals(to.getOwner()))
					.map(FixtureIterable.class::cast).collect(Collectors.toList())) {
				final IMutableResourcePile matching = container.stream()
					.filter(IMutableResourcePile.class::isInstance).map(IMutableResourcePile.class::cast)
					.filter(r -> r.getKind().equals(from.getKind()))
					.filter(r -> r.getContents().equals(from.getContents()))
					.filter(r -> r.getCreated() == from.getCreated())
					.filter(r -> r.getQuantity().getUnits().equals(from.getQuantity().getUnits()))
					.filter(r -> r.getId() == from.getId()).findAny().orElse(null);
				// TODO: Match destination by owner and kind, not just name and ID?
				final IMutableFortress destination = map.streamAllFixtures()
					.filter(IMutableFortress.class::isInstance).map(IMutableFortress.class::cast)
					.filter(f -> f.getName().equals(to.getName()))
					.filter(f -> f.getId() == to.getId()).findAny().orElse(null);
				if (matching != null && destination != null) {
					map.setModified(true);
					if (quantity.doubleValue() >= matching.getQuantity().getNumber().doubleValue()) {
						if (container instanceof IMutableFortress) { // TODO: Combine with other block when a supertype is added for this method
							((IMutableFortress) container).removeMember(matching);
						} else if (container instanceof IMutableUnit) {
							((IMutableUnit) container).removeMember(matching);
						} else {
							throw new IllegalStateException("Unexpected fixture-container type");
						}
						destination.addMember(matching);
					} else {
						final IMutableResourcePile split = new ResourcePileImpl(id.getAsInt(),
							matching.getKind(), matching.getContents(),
							new Quantity(quantity, matching.getQuantity().getUnits()));
						split.setCreated(matching.getCreated());
						matching.setQuantity(new Quantity(decimalize(matching.getQuantity()
							.getNumber()).subtract(quantity), matching.getQuantity().getUnits()));
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
		for (final IMutableMapNG map : getRestrictedAllMaps()) {
			final Supplier<Stream<IMutableFortress>> supp =
				() -> map.streamAllFixtures()
					.filter(IMutableFortress.class::isInstance).map(IMutableFortress.class::cast)
					.filter(f -> f.getOwner().equals(owner));
			final IMutableFortress result = supp.get().filter(f -> fortName.equals(f.getName())).findAny()
				.orElseGet(() -> supp.get().findAny().orElse(null));
			if (result == null) {
				continue;
			}
			any = true;
			map.setModified(true);
			result.addMember(resource.copy(false));
		}
		return any;
	}

	private static class GenerateOnce implements IntSupplier {
		private final IntSupplier idFactory;
		Integer generatedId;

		public GenerateOnce(final IntSupplier idFactory) {
			this.idFactory = idFactory;
			generatedId = null;
		}

		@Override
		public int getAsInt() {
			if (generatedId != null) {
				return generatedId;
			} else {
				final int temp = idFactory.getAsInt();
				generatedId = temp;
				return temp;
			}
		}
	}
}
