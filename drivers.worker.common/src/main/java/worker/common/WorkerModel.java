package worker.common;

import common.map.HasName;
import legacy.map.HasOwner;

import java.util.Collection;
import java.util.Objects;
import java.util.Random;
import java.util.function.BiPredicate;
import java.util.Collections;
import java.util.Optional;

import legacy.map.fixtures.FixtureIterable;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import legacy.map.fixtures.mobile.worker.IJob;
import lovelace.util.LovelaceLogger;
import org.javatuples.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.stream.StreamSupport;
import java.util.stream.Collectors;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.Deque;

import legacy.map.HasKind;
import legacy.map.HasMutableKind;
import legacy.map.HasMutableName;
import legacy.map.HasMutableOwner;
import legacy.map.IFixture;
import legacy.map.ILegacyMap;
import legacy.map.IMutableLegacyMap;
import legacy.map.Point;
import legacy.map.Player;

import legacy.map.fixtures.UnitMember;

import legacy.map.fixtures.mobile.ProxyFor;
import legacy.map.fixtures.mobile.IMutableUnit;
import legacy.map.fixtures.mobile.IMutableWorker;
import legacy.map.fixtures.mobile.IUnit;
import legacy.map.fixtures.mobile.IWorker;
import legacy.map.fixtures.mobile.ProxyUnit;
import legacy.map.fixtures.towns.IFortress;
import legacy.map.fixtures.towns.IMutableFortress;
import drivers.common.SimpleMultiMapModel;
import drivers.common.IDriverModel;
import drivers.common.IWorkerModel;

import legacy.map.fixtures.mobile.worker.IMutableJob;
import legacy.map.fixtures.mobile.worker.IMutableSkill;
import legacy.map.fixtures.mobile.worker.ISkill;
import legacy.map.fixtures.mobile.worker.Job;
import legacy.map.fixtures.mobile.worker.Skill;

/**
 * A model to underlie the advancement GUI, etc.
 */
public class WorkerModel extends SimpleMultiMapModel implements IWorkerModel {
	/**
	 * If the argument is a {@link IFortress fortress},
	 * return a stream of its members; otherwise, return a stream
	 * containing only the argument. This allows callers to get a flattened
	 * stream of units, including those in fortresses.
	 */
	private static Stream<?> flatten(final Object fixture) {
		if (fixture instanceof final IFortress f) {
			return f.stream();
		} else {
			return Stream.of(fixture);
		}
	}

	/**
	 * If the item in the entry is a {@link IFortress fortress}, return a
	 * stream of its contents paired with its location; otherwise, return a
	 * stream of just it.
	 */
	private static Stream<Pair<Point, IFixture>> flattenEntries(final Point point,
																final IFixture fixture) {
		if (fixture instanceof final IFortress f) {
			return f.stream().map(m -> Pair.with(point, m));
		} else {
			return Stream.of(Pair.with(point, fixture));
		}
	}

	/**
	 * Add the given unit at the given location in the given map.
	 */
	private static void addUnitAtLocationImpl(final IUnit unit, final Point location, final IMutableLegacyMap map) {
		final IMutableFortress fortress = map.getFixtures(location).stream()
				.filter(IMutableFortress.class::isInstance).map(IMutableFortress.class::cast)
				.filter(f -> f.owner().equals(unit.owner())).findAny().orElse(null);
		if (Objects.isNull(fortress)) {
			map.addFixture(location, unit.copy(IFixture.CopyBehavior.KEEP));
		} else {
			fortress.addMember(unit.copy(IFixture.CopyBehavior.KEEP));
		}
	}

	/**
	 * The current player, subject to change by user action.
	 */
	private @Nullable Player currentPlayerImpl = null;

	private final List<UnitMember> dismissedMembers = new ArrayList<>();

	public WorkerModel(final IMutableLegacyMap map) {
		super(map);
	}

	// TODO: Provide copyConstructor() static factory method?
	public WorkerModel(final IDriverModel model) {
		super(model);
	}

	/**
	 * The current player, subject to change by user action.
	 */
	@Override
	public Player getCurrentPlayer() {
		if (Objects.isNull(currentPlayerImpl)) {
			for (final ILegacyMap localMap : getAllMaps()) {
				final Player temp = localMap.getCurrentPlayer();
				if (!getUnits(temp).isEmpty()) {
					currentPlayerImpl = temp;
					return temp;
				}
			}
			currentPlayerImpl = getMap().getCurrentPlayer();
			return currentPlayerImpl;
		} else {
			return currentPlayerImpl;
		}
	}

	/**
	 * Set the current player for the GUI. Note we <em>deliberately</em> do
	 * not pass this change through to the maps; this is a read-only
	 * operation as far as the map <em>files</em> are concerned.
	 */
	@Override
	public void setCurrentPlayer(final Player currentPlayer) {
		currentPlayerImpl = currentPlayer;
	}

	/**
	 * Flatten and filter the stream to include only units, and only those owned by the given player.
	 */
	private static List<IUnit> getUnitsImpl(final Iterable<?> iter, final Player player) {
		return StreamSupport.stream(iter.spliterator(), false).flatMap(WorkerModel::flatten)
				.filter(IUnit.class::isInstance).map(IUnit.class::cast)
				.filter(u -> u.owner().getPlayerId() == player.getPlayerId())
				.collect(Collectors.toList());
	}

	@Override
	public Iterable<IFortress> getFortresses(final Player player) {
		return getMap().streamAllFixtures()
				.filter(IFortress.class::isInstance).map(IFortress.class::cast)
				.filter(f -> f.owner().equals(player))
				.collect(Collectors.toList());
	}

	/**
	 * All the players in all the maps.
	 */
	@Override
	public Iterable<Player> getPlayers() {
		return streamAllMaps()
				.flatMap(m -> StreamSupport.stream(m.getPlayers().spliterator(), false)).distinct()
				.collect(Collectors.toList());
	}

	/**
	 * Get all the given player's units, or only those of a specified kind.
	 */
	@Override
	public Collection<IUnit> getUnits(final Player player, final String kind) {
		return getUnits(player).stream().filter(u -> kind.equals(u.getKind())).collect(Collectors.toList());
	}

	/**
	 * Get all the given player's units, or only those of a specified kind.
	 */
	@Override
	public Collection<IUnit> getUnits(final Player player) {
		if (getSubordinateMaps().iterator().hasNext()) {
			final Iterable<IUnit> temp = streamAllMaps()
					.flatMap((indivMap) -> getUnitsImpl(indivMap.streamAllFixtures()
							.collect(Collectors.toList()), player).stream())
					.collect(Collectors.toList());
			final Map<Integer, ProxyUnit> tempMap = new TreeMap<>();
			for (final IUnit unit : temp) {
				final int key = unit.getId();
				final ProxyUnit proxy;
				if (tempMap.containsKey(key)) {
					proxy = tempMap.get(key);
				} else {
					final ProxyUnit newProxy = new ProxyUnit(key);
					tempMap.put(key, newProxy);
					proxy = newProxy;
				}
				proxy.addProxied(unit);
			}
			return tempMap.values().stream().sorted(Comparator.comparing(IUnit::getName,
					String.CASE_INSENSITIVE_ORDER)).collect(Collectors.toList());
		} else {
			// Just in case I missed something in the proxy implementation, make sure
			// things work correctly when there's only one map.
			return getUnitsImpl(getMap().streamAllFixtures()
					.collect(Collectors.toList()), player)
					.stream().sorted(Comparator.comparing(IUnit::getName,
							String.CASE_INSENSITIVE_ORDER))
					.collect(Collectors.toList());
		}
	}

	/**
	 * All the "kinds" of units the given player has.
	 */
	@Override
	public Iterable<String> getUnitKinds(final Player player) {
		return getUnits(player).stream().map(IUnit::getKind).distinct().sorted(String.CASE_INSENSITIVE_ORDER)
				.collect(Collectors.toList());
	}

	/**
	 * Add the given unit at the given location in all maps.
	 *
	 * FIXME: Should copy into subordinate maps, and return either the unit (in one-map case) or a proxy
	 */
	private void addUnitAtLocation(final IUnit unit, final Point location) {
		if (getSubordinateMaps().iterator().hasNext()) {
			for (final IMutableLegacyMap eachMap : getRestrictedAllMaps()) {
				addUnitAtLocationImpl(unit, location, eachMap);
				eachMap.setModified(true);
			}
		} else {
			addUnitAtLocationImpl(unit, location, getRestrictedMap());
			setMapModified(true);
		}
	}

	/**
	 * Add a unit to all the maps, at the location of its owner's HQ in the main map.
	 */
	@Override
	public void addUnit(final IUnit unit) {
		Pair<IMutableFortress, Point> temp = null;
		for (final Pair<Point, IMutableFortress> pair : getMap().streamLocations()
				.flatMap(l -> getMap().getFixtures(l).stream()
						.filter(IMutableFortress.class::isInstance)
						.map(IMutableFortress.class::cast)
						.filter(f -> f.owner().getPlayerId() ==
								unit.owner().getPlayerId())
						.map(f -> Pair.with(l, f))).toList()) {
			final Point point = pair.getValue0();
			final IMutableFortress fixture = pair.getValue1();
			if ("HQ".equals(fixture.getName())) {
				addUnitAtLocation(unit, point);
				return;
			} else if (Objects.isNull(temp)) {
				temp = Pair.with(fixture, point);
			}
		}
		if (!Objects.isNull(temp)) {
			final IMutableFortress fortress = temp.getValue0();
			final Point loc = temp.getValue1();
			LovelaceLogger.info("Added unit at fortress %s, not HQ", fortress.getName());
			addUnitAtLocation(unit, loc);
			return;
		} else if (!unit.owner().isIndependent()) {
			LovelaceLogger.warning("No suitable location found for unit %s, owned by %s",
					unit.getName(), unit.owner());
		}
	}

	/**
	 * Get a unit by its owner and ID.
	 */
	@Override
	public @Nullable IUnit getUnitByID(final Player owner, final int id) {
		return getUnits(owner).parallelStream()
				.filter(u -> u.getId() == id).findAny().orElse(null);
	}

	private static BiPredicate<Point, IFixture> unitMatching(final IUnit unit) {
		return (point, fixture) ->
				fixture instanceof final IUnit u && fixture.getId() == unit.getId() &&
						u.owner().equals(unit.owner());
	}

	/**
	 * Remove the given unit from the map. It must be empty, and may be
	 * required to be owned by the current player. The operation will also
	 * fail if "matching" units differ in name or kind from the provided
	 * unit.  Returns true if the preconditions were met and the unit was
	 * removed, and false otherwise. To make an edge case explicit, if
	 * there are no matching units in any map the method returns false.
	 */
	@Override
	public boolean removeUnit(final IUnit unit) {
		LovelaceLogger.debug("In WorkerModel.removeUnit()");
		final List<Pair<IMutableLegacyMap, Pair<Point, IUnit>>> delenda = new ArrayList<>();
		final Predicate<Pair<Point, IFixture>> testPair =
				p -> unitMatching(unit).test(p.getValue0(), p.getValue1());
		for (final IMutableLegacyMap map : getRestrictedAllMaps()) {
			final Pair<Point, IFixture> pair = map.streamLocations()
					.flatMap(l -> map.getFixtures(l).stream()
							.map(f -> Pair.with(l, f)))
					.flatMap(p -> flattenEntries(p.getValue0(), p.getValue1()))
					.filter(testPair)
					.findAny().orElse(null);
			if (!Objects.isNull(pair)) {
				LovelaceLogger.debug("Map has matching unit");
				final Point location = pair.getValue0();
				final IUnit fixture = (IUnit) pair.getValue1();
				if (fixture.getKind().equals(unit.getKind()) &&
						fixture.getName().equals(unit.getName()) &&
						!fixture.iterator().hasNext()) {
					LovelaceLogger.debug("Matching unit meets preconditions");
					delenda.add(Pair.with(map, Pair.with(location, fixture)));
				} else {
					LovelaceLogger.warning(
							"Matching unit in %s fails preconditions for removal",
							Optional.ofNullable(map.getFilename())
									.map(Object::toString).orElse("an unsaved map"));
					return false;
				}
			}
		}
		if (delenda.isEmpty()) {
			LovelaceLogger.debug("No matching units");
			return false;
		}
		for (final Pair<IMutableLegacyMap, Pair<Point, IUnit>> pair : delenda) {
			final IMutableLegacyMap map = pair.getValue0();
			final Point location = pair.getValue1().getValue0();
			final IUnit fixture = pair.getValue1().getValue1();
			if (map.getFixtures(location).contains(fixture)) {
				map.removeFixture(location, fixture);
			} else {
				boolean any = false;
				for (final IMutableFortress fort : map.getFixtures(location).stream()
						.filter(IMutableFortress.class::isInstance)
						.map(IMutableFortress.class::cast).toList()) {
					if (fort.stream().anyMatch(Predicate.isEqual(fixture))) {
						any = true;
						fort.removeMember(fixture);
						break;
					}
				}
				if (!any) {
					LovelaceLogger.warning(
							"Failed to find unit to remove that we thought might be in a fortress");
				}
			}
		}
		LovelaceLogger.debug("Finished removing matching unit(s) from map(s)");
		return true;
	}

	private static int iterableSize(final Iterable<?> iter) {
		return (int) StreamSupport.stream(iter.spliterator(), true).count();
	}

	/**
	 * Move a unit-member from one unit to another in the presence of
	 * proxies, that is, when each unit and unit-member represents
	 * corresponding units and unit members in multiple maps and the same
	 * operations must be applied to all of them.
	 *
	 * The proxy code is some of the most difficult and delicate code in
	 * the entire suite, and I'm <em>pretty</em> sure the algorithm this
	 * method implements is correct ...
	 *
	 * Returns true if our preconditions were met and so we did the move,
	 * and false when preconditions were not met and the caller should fall
	 * back to the non-proxy algorithm.
	 *
	 * TODO: Add a test of this method.
	 */
	private boolean moveProxied(/*UnitMember&*/final ProxyFor<? extends UnitMember> member, final ProxyUnit old,
											   final ProxyUnit newOwner) {
		if (old.getProxied().size() == newOwner.getProxied().size() &&
				old.getProxied().size() == member.getProxied().size()) {
			final LinkedList<UnitMember> memberProxied = new LinkedList<>(member.getProxied());
			final LinkedList<IUnit> oldProxied = new LinkedList<>(old.getProxied());
			final LinkedList<IUnit> newProxied = new LinkedList<>(newOwner.getProxied());
			final Deque<UnitMember> members = new LinkedList<>();
			final Deque<IMutableUnit> newList = new LinkedList<>();
			while (!memberProxied.isEmpty() && !oldProxied.isEmpty() && !newProxied.isEmpty()) {
				final UnitMember item = memberProxied.removeFirst();
				final IUnit innerOld = oldProxied.removeFirst();
				final IUnit innerNew = newProxied.removeFirst();
				if (innerOld instanceof final IMutableUnit oldUnit && innerNew instanceof final IMutableUnit newUnit) {
					oldUnit.removeMember(item);
					members.addLast(item);
					newList.addLast(newUnit);
				} else {
					LovelaceLogger.warning("Immutable unit in moveProxied()");
					return false;
				}
			}
			while (!newList.isEmpty() && !members.isEmpty()) {
				final IMutableUnit unit = newList.removeFirst();
				final UnitMember innerMember = members.removeFirst();
				unit.addMember(innerMember);
			}
			for (final IMutableLegacyMap map : getRestrictedAllMaps()) {
				map.setModified(true);
			}
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Move a unit-member from one unit to another. If all three objects
	 * are proxies, we use a special algorithm that unwraps the proxies,
	 * which was extracted as {@link #moveProxied}.
	 */
	@Override
	public void moveMember(final UnitMember member, final IUnit old, final IUnit newOwner) {
		// Adding either <?> or <? extends UnitMember> to the first 'ProxyFor' will not compile;
		// the compiler insists that 'UnitMember' and 'ProxyFor<UnitMember>' are entirely disjoint,
		// despite proof to the contrary.
		//noinspection rawtypes,unchecked
		if (member instanceof final ProxyFor proxyMember && old instanceof final ProxyUnit proxyOld &&
				newOwner instanceof final ProxyUnit proxyNew && moveProxied(proxyMember, proxyOld, proxyNew)) {
			return;
		}
		final Predicate<Object> isUnit = IMutableUnit.class::isInstance;
		final Function<Object, IMutableUnit> unitCast = IMutableUnit.class::cast;
		final Predicate<IUnit> matchingOldKind = u -> u.getKind().equals(old.getKind());
		final Predicate<IUnit> matchingOldName = u -> u.getName().equals(old.getName());
		final Predicate<IUnit> matchingOldId = u -> u.getId() == old.getId();
		final Predicate<IUnit> matchingNewKind = u -> u.getKind().equals(newOwner.getKind());
		final Predicate<IUnit> matchingNewName = u -> u.getName().equals(newOwner.getName());
		final Predicate<IUnit> matchingNewId = u -> u.getId() == newOwner.getId();
		for (final IMutableLegacyMap map : getRestrictedAllMaps()) {
			final IMutableUnit matchingOld = getUnitsImpl(map.streamAllFixtures()
					.collect(Collectors.toList()), old.owner()).stream()
					.filter(isUnit).map(unitCast).filter(matchingOldKind)
					.filter(matchingOldName).filter(matchingOldId)
					.findAny().orElse(null);
			if (!Objects.isNull(matchingOld)) {
				// TODO: equals() isn't ideal for finding a matching member ...
				final UnitMember matchingMember = matchingOld.stream().filter(Predicate.isEqual(member))
						.findAny().orElse(null);
				final IMutableUnit matchingNew = getUnitsImpl(map.streamAllFixtures()
						.collect(Collectors.toList()), newOwner.owner())
						.stream()
						.filter(isUnit).map(unitCast).filter(matchingNewKind)
						.filter(matchingNewName).filter(matchingNewId)
						.findAny().orElse(null);
				if (!Objects.isNull(matchingMember) && !Objects.isNull(matchingNew)) {
					matchingOld.removeMember(matchingMember);
					matchingNew.addMember(matchingMember);
					map.setModified(true);
				}
			}
		}
	}

	@Override
	public void dismissUnitMember(final UnitMember member) {
		boolean any = false;
		// TODO: Handle proxies specially?
		for (final IMutableLegacyMap map : getRestrictedAllMaps()) {
			for (final IMutableUnit unit : getUnitsImpl(map.streamAllFixtures()
							.collect(Collectors.toList()),
					getCurrentPlayer()).stream()
					.filter(IMutableUnit.class::isInstance)
					.map(IMutableUnit.class::cast).toList()) {
				// FIXME: matching by equals() will really not do here ...
				final UnitMember matching = unit.stream().filter(Predicate.isEqual(member))
						.findAny().orElse(null);
				if (!Objects.isNull(matching)) {
					any = true;
					unit.removeMember(matching);
					map.setModified(true);
					break;
				}
			}
		}
		if (any) {
			dismissedMembers.add(member);
		}
	}

	@Override
	public Iterable<UnitMember> getDismissed() {
		return Collections.unmodifiableList(dismissedMembers);
	}

	// TODO: Notification events should come from the map, instead of here
	// (as we might add one to this method), so UI could just call this and
	// the tree model could listen to the map---so the worker-mgmt UI would
	// update if a unit were added through the map-viewer UI.
	@Override
	public void addUnitMember(final IUnit unit, final UnitMember member) {
		final Predicate<Object> isUnit = IMutableUnit.class::isInstance;
		final Function<Object, IMutableUnit> unitCast = IMutableUnit.class::cast;
		final Predicate<IUnit> matchingName = u -> u.getName().equals(unit.getName());
		final Predicate<IUnit> matchingKind = u -> u.getKind().equals(unit.getKind());
		final Predicate<IUnit> matchingId = u -> u.getId() == unit.getId();
		for (final IMutableLegacyMap map : getRestrictedAllMaps()) {
			final IMutableUnit matching = getUnitsImpl(map.streamAllFixtures()
					.collect(Collectors.toList()), unit.owner()).stream()
					.filter(isUnit)
					.map(unitCast)
					.filter(matchingName)
					.filter(matchingKind)
					.filter(matchingId)
					.findAny().orElse(null);
			if (!Objects.isNull(matching)) {
				matching.addMember(member.copy(IFixture.CopyBehavior.KEEP));
				map.setModified(true);
			}
		}
	}

	@Override
	public boolean renameItem(final HasName item, final String newName) {
		boolean any = false;
		switch (item) {
			case final IUnit unit -> {
				final Predicate<IUnit> matchingName = u -> u.getName().equals(item.getName());
				final Predicate<IUnit> matchingKind = u -> u.getKind().equals(unit.getKind());
				final Predicate<IUnit> matchingId = u -> u.getId() == unit.getId();
				for (final IMutableLegacyMap map : getRestrictedAllMaps()) {
					final IUnit matching =
							getUnitsImpl(map.streamAllFixtures()
											.collect(Collectors.toList()),
									unit.owner()).stream()
									.filter(matchingName)
									.filter(matchingKind)
									.filter(matchingId)
									.findAny().orElse(null);
					if (matching instanceof final HasMutableName matchNamed) {
						any = true;
						matchNamed.setName(newName);
						map.setModified(true);
					}
				}
				if (!any) {
					LovelaceLogger.warning("Unable to find unit to rename");
				}
				return any;
			}
			case final UnitMember memberItem -> {
				final Predicate<Object> isNamed = HasMutableName.class::isInstance;
				final Predicate<UnitMember> matchingId = m -> m.getId() == memberItem.getId();
				final Predicate<UnitMember> matchingName =
						m -> ((HasMutableName) m).getName().equals(item.getName());
				for (final IMutableLegacyMap map : getRestrictedAllMaps()) {
					// FIXME: We should have a firmer identification than just name and ID
					final UnitMember matching =
							getUnitsImpl(map.streamAllFixtures()
									.collect(Collectors.toList()), getCurrentPlayer())
									.stream().flatMap(FixtureIterable::stream)
									.filter(isNamed)
									.filter(matchingId)
									.filter(matchingName)
									.findAny().orElse(null);
					if (!Objects.isNull(matching)) {
						any = true;
						((HasMutableName) matching).setName(newName);
						map.setModified(true);
					}
				}
				if (!any) {
					LovelaceLogger.warning("Unable to find unit member to rename");
				}
				return any;
			}
			default -> {
				LovelaceLogger.warning("Unable to find item to rename");
				return false;
			}
		}
	}

	@Override
	public boolean changeKind(final HasKind item, final String newKind) {
		boolean any = false;
		switch (item) {
			case final IUnit unit -> {
				final Predicate<IUnit> matchingName = u -> u.getName().equals(unit.getName());
				final Predicate<IUnit> matchingKind = u -> u.getKind().equals(item.getKind());
				final Predicate<IUnit> matchingId = u -> u.getId() == unit.getId();
				for (final IMutableLegacyMap map : getRestrictedAllMaps()) {
					final IUnit matching = getUnitsImpl(map.streamAllFixtures()
									.collect(Collectors.toList()),
							unit.owner()).stream()
							.filter(matchingName)
							.filter(matchingKind)
							.filter(matchingId)
							.findAny().orElse(null);
					if (matching instanceof final HasMutableKind kinded) {
						any = true;
						kinded.setKind(newKind);
						map.setModified(true);
					}
				}
				if (!any) {
					LovelaceLogger.warning("Unable to find unit to change kind");
				}
				return any;
			}
			case final UnitMember member -> {
				final Predicate<UnitMember> matchingId = m -> m.getId() == member.getId();
				final Predicate<UnitMember> hasMutableKind = HasMutableKind.class::isInstance;
				final Function<Object, HasMutableKind> hmkCast = HasMutableKind.class::cast;
				final Predicate<HasMutableKind> matchingKind = m -> m.getKind().equals(item.getKind());
				for (final IMutableLegacyMap map : getRestrictedAllMaps()) {
					// FIXME: We should have a firmer identification than just kind and ID
					final HasMutableKind matching = getUnitsImpl(map.streamAllFixtures()
							.collect(Collectors.toList()), getCurrentPlayer())
							.stream().flatMap(FixtureIterable::stream)
							.filter(matchingId)
							.filter(hasMutableKind)
							.map(hmkCast)
							.filter(matchingKind)
							.findAny().orElse(null);
					if (!Objects.isNull(matching)) {
						any = true;
						matching.setKind(newKind);
						map.setModified(true);
					}
				}
				if (!any) {
					LovelaceLogger.warning("Unable to find unit member to change kind");
				}
				return any;
			}
			default -> {
				LovelaceLogger.warning("Unable to find item to change kind");
				return false;
			}
		}
	}

	@Override
	public boolean addSibling(final UnitMember existing, final UnitMember sibling) {
		boolean any = false;
		for (final IMutableLegacyMap map : getRestrictedAllMaps()) {
			for (final IMutableUnit unit : getUnitsImpl(map.streamAllFixtures()
					.collect(Collectors.toList()), getCurrentPlayer())
					.stream().filter(IMutableUnit.class::isInstance)
					.map(IMutableUnit.class::cast).toList()) {
				if (unit.stream().anyMatch(Predicate.isEqual(existing))) {
					// TODO: look beyond equals() for matching-in-existing?
					unit.addMember(sibling.copy(IFixture.CopyBehavior.KEEP));
					any = true;
					map.setModified(true);
					break;
				}
			}
		}
		return any;
	}

	private static Stream<IFixture> flattenIncluding(final IFixture fixture) {
		if (fixture instanceof FixtureIterable) {
			return Stream.concat(Stream.of(fixture), ((FixtureIterable<?>) fixture).stream());
		} else {
			return Stream.of(fixture);
		}
	}

	/**
	 * Change the owner of the given item in all maps. Returns true if this
	 * succeeded in any map, false otherwise.
	 */
	@Override
	public boolean changeOwner(final HasOwner item, final Player newOwner) {
		boolean any = false;
		final Predicate<Object> isOwned = HasMutableOwner.class::isInstance;
		final Function<Object, HasMutableOwner> hmoCast = HasMutableOwner.class::cast;
		for (final IMutableLegacyMap map : getRestrictedAllMaps()) {
			final HasMutableOwner matching = map.streamAllFixtures()
					.flatMap(WorkerModel::flattenIncluding)
					.flatMap(WorkerModel::flattenIncluding).filter(isOwned)
					.map(hmoCast)
					.filter(Predicate.isEqual(item)) // TODO: equals() is not the best way to find it ...
					.findAny().orElse(null);
			if (!Objects.isNull(matching)) {
				if (StreamSupport.stream(map.getPlayers().spliterator(), true)
						.noneMatch(Predicate.isEqual(newOwner))) {
					map.addPlayer(newOwner);
				}
				matching.setOwner(map.getPlayers().getPlayer(newOwner.getPlayerId()));
				map.setModified(true);
				any = true;
			}
		}
		return any;
	}

	@Override
	public boolean sortFixtureContents(final IUnit fixture) {
		boolean any = false;
		final Predicate<Object> isUnit = IMutableUnit.class::isInstance;
		final Function<Object, IMutableUnit> unitCast = IMutableUnit.class::cast;
		final Predicate<IMutableUnit> matchingName = u -> u.getName().equals(fixture.getName());
		final Predicate<IMutableUnit> matchingKind = u -> u.getKind().equals(fixture.getKind());
		final Predicate<IMutableUnit> matchingId = u -> u.getId() == fixture.getId();
		for (final IMutableLegacyMap map : getRestrictedAllMaps()) {
			final IMutableUnit matching = getUnitsImpl(map.streamAllFixtures()
					.collect(Collectors.toList()), getCurrentPlayer()).stream()
					.filter(isUnit).map(unitCast)
					.filter(matchingName)
					.filter(matchingKind)
					.filter(matchingId)
					.findAny().orElse(null);
			if (!Objects.isNull(matching)) {
				matching.sortMembers();
				map.setModified(true);
				any = true;
			}
		}
		return any;
	}

	/**
	 * Add a Job to the matching worker in all maps. Returns true if a
	 * matching worker was found in at least one map, false otherwise.
	 * If an existing Job by that name already existed, it is left alone.
	 */
	@Override
	public boolean addJobToWorker(final IWorker worker, final String jobName) {
		boolean any = false;
		final Predicate<Object> isWorker = IMutableWorker.class::isInstance;
		final Function<Object, IMutableWorker> workerCast = IMutableWorker.class::cast;
		final Predicate<IMutableWorker> matchingRace = w -> w.getRace().equals(worker.getRace());
		final Predicate<IMutableWorker> matchingName = w -> w.getName().equals(worker.getName());
		final Predicate<IMutableWorker> matchingId = w -> w.getId() == worker.getId();
		final Predicate<IJob> matchingJob = j -> jobName.equals(j.getName());
		for (final IMutableLegacyMap map : getRestrictedAllMaps()) {
			final IMutableWorker matching = getUnitsImpl(map.streamAllFixtures()
					.collect(Collectors.toList()), getCurrentPlayer()).stream()
					.flatMap(FixtureIterable::stream).filter(isWorker)
					.map(workerCast)
					.filter(matchingRace)
					.filter(matchingName)
					.filter(matchingId)
					.findAny().orElse(null);
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
		final Predicate<Object> isWorker = IMutableWorker.class::isInstance;
		final Function<Object, IMutableWorker> workerCast = IMutableWorker.class::cast;
		final Predicate<IMutableWorker> matchingRace = w -> w.getRace().equals(worker.getRace());
		final Predicate<IMutableWorker> matchingName = w -> w.getName().equals(worker.getName());
		final Predicate<IMutableWorker> matchingId = w -> w.getId() == worker.getId();
		final Predicate<IJob> isMutableJob = IMutableJob.class::isInstance;
		final Function<Object, IMutableJob> mjCast = IMutableJob.class::cast;
		final Predicate<IJob> matchingJob = j -> jobName.equals(j.getName());
		for (final IMutableLegacyMap map : getRestrictedAllMaps()) {
			final IMutableWorker matching =
					getUnitsImpl(map.streamAllFixtures().collect(Collectors.toList()), getCurrentPlayer()).stream()
							.flatMap(IUnit::stream).filter(isWorker).map(workerCast)
							.filter(matchingRace).filter(matchingName)
							.filter(matchingId).findAny().orElse(null);
			if (!Objects.isNull(matching)) {
				final IMutableJob job = StreamSupport.stream(matching.spliterator(), false)
						.filter(isMutableJob).map(mjCast)
						.filter(matchingJob).findAny().orElse(null);
				if (Objects.isNull(job)) {
					map.setModified(true);
					final Job newJob = new Job(jobName, 0);
					newJob.addSkill(new Skill(skillName, 0, 0));
					matching.addJob(newJob);
				} else if (StreamSupport.stream(job.spliterator(), false).map(ISkill::getName)
						.noneMatch(Predicate.isEqual(skillName))) {
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
	public boolean addHoursToSkill(final IWorker worker, final String jobName, final String skillName, final int hours,
								   final int contextValue) {
		boolean any = false;
		final Predicate<Object> isWorker = IMutableWorker.class::isInstance;
		final Function<Object, IMutableWorker> workerCast = IMutableWorker.class::cast;
		final Predicate<IMutableWorker> matchingRace = w -> w.getRace().equals(worker.getRace());
		final Predicate<IMutableWorker> matchingName = w -> w.getName().equals(worker.getName());
		final Predicate<IMutableWorker> matchingId = w -> w.getId() == worker.getId();
		final Predicate<IJob> isMutableJob = IMutableJob.class::isInstance;
		final Function<Object, IMutableJob> mjCast = IMutableJob.class::cast;
		final Predicate<IJob> matchingJob = j -> jobName.equals(j.getName());
		final Predicate<ISkill> isMutableSkill = IMutableSkill.class::isInstance;
		final Function<Object, IMutableSkill> msCast = IMutableSkill.class::cast;
		final Predicate<ISkill> matchingSkill = s -> skillName.equals(s.getName());
		for (final IMutableLegacyMap map : getRestrictedAllMaps()) {
			final IMutableWorker matching = getUnitsImpl(map.streamAllFixtures()
					.collect(Collectors.toList()), getCurrentPlayer()).stream()
					.flatMap(FixtureIterable::stream).filter(isWorker)
					.map(workerCast)
					.filter(matchingRace)
					.filter(matchingName)
					.filter(matchingId)
					.findAny().orElse(null);
			if (!Objects.isNull(matching)) {
				map.setModified(true);
				any = true;
				final IMutableJob job;
				final IMutableJob temp = StreamSupport.stream(matching.spliterator(), true)
						.filter(isMutableJob).map(mjCast)
						.filter(matchingJob).findAny().orElse(null);
				if (Objects.isNull(temp)) {
					job = new Job(jobName, 0);
					// FIXME: The addJob() API doc explicitly says the Job can't be assumed to have been preserved
					matching.addJob(job);
				} else {
					job = temp;
				}
				final IMutableSkill skill;
				final IMutableSkill tempSkill = StreamSupport.stream(job.spliterator(), true)
						.filter(isMutableSkill)
						.map(msCast)
						.filter(matchingSkill)
						.findAny().orElse(null);
				if (Objects.isNull(tempSkill)) {
					skill = new Skill(skillName, 0, 0);
					job.addSkill(skill); // FIXME: Similarly, assumes behavior the API doc explicitly warns against
				} else {
					skill = tempSkill;
				}
				skill.addHours(hours, contextValue);
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
	 * #addJobToWorker}, then the skill is added to it. The
	 * "contextValue" is used to calculate a new value passed to {@link
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
	 * Replace one skill, "delenda", with
	 * another, "replacement", in the specified job in the specified worker in all maps.
	 * Unlike {@link #addHoursToSkill}, if a map does not have an
	 * <em>equal</em> Job in the matching worker, that map is completely
	 * skipped.  If the replacement is already present, just remove the
	 * first skill. Returns true if the operation was carried out in any of
	 * the maps, false otherwise.
	 */
	@Override
	public boolean replaceSkillInJob(final IWorker worker, final String jobName, final ISkill delenda,
									 final ISkill replacement) {
		boolean any = false;
		final Predicate<Object> isWorker = IMutableWorker.class::isInstance;
		final Function<Object, IMutableWorker> workerCast = IMutableWorker.class::cast;
		final Predicate<IMutableWorker> matchingRace = w -> w.getRace().equals(worker.getRace());
		final Predicate<IMutableWorker> matchingName = w -> w.getName().equals(worker.getName());
		final Predicate<IMutableWorker> matchingId = w -> w.getId() == worker.getId();
		final Predicate<IJob> isMutableJob = IMutableJob.class::isInstance;
		final Function<Object, IMutableJob> mjCast = IMutableJob.class::cast;
		final Predicate<IJob> matchingJobName = j -> jobName.equals(j.getName());
		for (final IMutableLegacyMap map : getRestrictedAllMaps()) {
			final IMutableWorker matchingWorker = getUnitsImpl(map.streamAllFixtures()
					.collect(Collectors.toList()), getCurrentPlayer()).stream()
					.flatMap(FixtureIterable::stream)
					.filter(isWorker)
					.map(workerCast)
					.filter(matchingRace)
					.filter(matchingName)
					.filter(matchingId)
					.findAny().orElse(null);
			if (!Objects.isNull(matchingWorker)) {
				final IMutableJob matchingJob = StreamSupport.stream(
								matchingWorker.spliterator(), true)
						.filter(isMutableJob).map(mjCast)
						.filter(matchingJobName).findAny().orElse(null);
				if (Objects.isNull(matchingJob)) {
					LovelaceLogger.warning("No matching skill in matching worker");
				} else {
					final ISkill matchingSkill = StreamSupport.stream(
									matchingJob.spliterator(), true)
							.filter(Predicate.isEqual(delenda)).findAny().orElse(null);
					if (Objects.isNull(matchingSkill)) {
						LovelaceLogger.warning("No matching skill in matching worker");
					} else {
						map.setModified(true);
						any = true;
						matchingJob.removeSkill(matchingSkill);
						matchingJob.addSkill(replacement.copy());
					}
				}
			}
		}
		return any;
	}

	/**
	 * Set the given unit's orders for the given turn to the given text.
	 * Returns true if a matching (and mutable) unit was found in at
	 * least one map, false otherwise.
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
			// TODO: Why not use getUnitsImpl?
			final IMutableUnit matching = map.streamAllFixtures()
					.flatMap(WorkerModel::flatten)
					.filter(isUnit).map(unitCast)
					.filter(matchingOwner)
					.filter(matchingKind)
					.filter(matchingName)
					.filter(matchingId)
					.findAny().orElse(null);
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
			// TODO: Why not use getUnitsImpl?
			final IMutableUnit matching = map.streamAllFixtures()
					.flatMap(WorkerModel::flatten)
					.filter(isUnit).map(unitCast)
					.filter(matchingOwner)
					.filter(matchingKind)
					.filter(matchingName)
					.filter(matchingId)
					.findAny().orElse(null);
			if (!Objects.isNull(matching)) {
				matching.setResults(turn, results);
				map.setModified(true);
				any = true;
			}
		}
		return any;
	}
}

