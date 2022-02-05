package worker.common;

import java.util.function.BiPredicate;
import java.util.Collections;
import java.util.Optional;
import common.map.fixtures.FixtureIterable;
import java.util.stream.Stream;
import org.javatuples.Pair;
import java.util.function.Predicate;
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

import common.map.HasKind;
import common.map.HasMutableKind;
import common.map.HasMutableName;
import common.map.HasMutableOwner;
import common.map.IFixture;
import common.map.IMapNG;
import common.map.IMutableMapNG;
import common.map.SPMapNG;
import common.map.Point;
import common.map.MapDimensionsImpl;
import common.map.Player;
import common.map.PlayerImpl;
import common.map.TileFixture;
import common.map.PlayerCollection;

import common.map.fixtures.UnitMember;

import common.map.fixtures.mobile.ProxyFor;
import common.map.fixtures.mobile.IMutableUnit;
import common.map.fixtures.mobile.IMutableWorker;
import common.map.fixtures.mobile.IUnit;
import common.map.fixtures.mobile.IWorker;
import common.map.fixtures.mobile.Unit;
import common.map.fixtures.mobile.ProxyUnit;
import common.map.fixtures.mobile.AnimalImpl;
import common.map.fixtures.terrain.Oasis;
import common.map.fixtures.terrain.Hill;
import common.map.fixtures.terrain.Forest;
import common.map.fixtures.towns.IFortress;
import common.map.fixtures.towns.IMutableFortress;
import common.map.fixtures.towns.TownSize;
import common.map.fixtures.towns.FortressImpl;
import drivers.common.SimpleMultiMapModel;
import drivers.common.IDriverModel;
import drivers.common.IWorkerModel;
import java.util.logging.Logger;

import common.map.fixtures.mobile.worker.IJob;
import common.map.fixtures.mobile.worker.IMutableJob;
import common.map.fixtures.mobile.worker.IMutableSkill;
import common.map.fixtures.mobile.worker.ISkill;
import common.map.fixtures.mobile.worker.Job;
import common.map.fixtures.mobile.worker.Skill;

/**
 * A model to underlie the advancement GUI, etc.
 */
public class WorkerModel extends SimpleMultiMapModel implements IWorkerModel {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(WorkerModel.class.getName());

	/**
	 * If {@link fixture the argument} is a {@link IFortress fortress},
	 * return a stream of its members; otherwise, return a stream
	 * containing only the argument. This allows callers to get a flattened
	 * stream of units, including those in fortresses.
	 */
	private static Stream<?> flatten(final Object fixture) {
		if (fixture instanceof IFortress) {
			return ((IFortress) fixture).stream();
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
		if (fixture instanceof IFortress) {
			return ((IFortress) fixture).stream().map(m -> Pair.with(point, m));
		} else {
			return Stream.of(Pair.with(point, fixture));
		}
	}

	/**
	 * Add the given unit at the given location in the given map.
	 */
	private static void addUnitAtLocationImpl(final IUnit unit, final Point location, final IMutableMapNG map) {
		IMutableFortress fortress = map.getFixtures(location).stream()
			.filter(IMutableFortress.class::isInstance).map(IMutableFortress.class::cast)
			.filter(f -> f.getOwner().equals(unit.getOwner())).findAny().orElse(null);
		if (fortress != null) {
			fortress.addMember(unit.copy(false));
		} else {
			map.addFixture(location, unit.copy(false));
		}
	}

	/**
	 * The current player, subject to change by user action.
	 */
	@Nullable
	private Player currentPlayerImpl = null;

	private final List<UnitMember> dismissedMembers = new ArrayList<>();

	public WorkerModel(final IMutableMapNG map) {
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
		if (currentPlayerImpl != null) { // TODO: invert
			return currentPlayerImpl;
		} else {
			for (IMapNG localMap : getAllMaps()) {
				Player temp = localMap.getCurrentPlayer();
				if (getUnits(temp).iterator().hasNext()) {
					currentPlayerImpl = temp;
					return temp;
				}
			}
			currentPlayerImpl = getMap().getCurrentPlayer();
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
	private List<IUnit> getUnitsImpl(final Iterable<?> iter, final Player player) {
		return StreamSupport.stream(iter.spliterator(), false).flatMap(WorkerModel::flatten)
			.filter(IUnit.class::isInstance).map(IUnit.class::cast)
			.filter(u -> u.getOwner().getPlayerId() == player.getPlayerId())
			.collect(Collectors.toList());
	}

	@Override
	public Iterable<IFortress> getFortresses(final Player player) {
		return getMap().streamLocations()
			.flatMap(l -> getMap().getFixtures(l).stream())
			.filter(IFortress.class::isInstance).map(IFortress.class::cast)
			.filter(f -> f.getOwner().equals(player))
			.collect(Collectors.toList());
	}

	/**
	 * All the players in all the maps.
	 */
	@Override
	public Iterable<Player> getPlayers() {
		return StreamSupport.stream(getAllMaps().spliterator(), false)
			.flatMap(m -> StreamSupport.stream(m.getPlayers().spliterator(), false)).distinct()
			.collect(Collectors.toList());
	}

	/**
	 * Get all the given player's units, or only those of a specified kind.
	 */
	@Override
	public Iterable<IUnit> getUnits(final Player player, final String kind) {
		return StreamSupport.stream(getUnits(player).spliterator(), false)
			.filter(u -> kind.equals(u.getKind())).collect(Collectors.toList());
	}

	/**
	 * Get all the given player's units, or only those of a specified kind.
	 */
	@Override
	public Iterable<IUnit> getUnits(final Player player) {
		if (!getSubordinateMaps().iterator().hasNext()) {
			// Just in case I missed something in the proxy implementation, make sure
			// things work correctly when there's only one map.
			return getUnitsImpl(getMap().streamLocations()
					.flatMap(l -> getMap().getFixtures(l).stream())
					.collect(Collectors.toList()), player)
				.stream().sorted(Comparator.comparing(IUnit::getName,
					String.CASE_INSENSITIVE_ORDER))
				.collect(Collectors.toList());
		} else {
			Iterable<IUnit> temp = StreamSupport.stream(getAllMaps().spliterator(), false)
				.flatMap((indivMap) -> getUnitsImpl(indivMap.streamLocations()
					.flatMap(l -> indivMap.getFixtures(l).stream())
						.collect(Collectors.toList()), player).stream())
				.collect(Collectors.toList());
			Map<Integer, ProxyUnit> tempMap = new TreeMap<>();
			for (IUnit unit : temp) {
				int key = unit.getId();
				ProxyUnit proxy;
				if (tempMap.containsKey(key)) {
					proxy = tempMap.get(key);
				} else {
					ProxyUnit newProxy = new ProxyUnit(key);
					tempMap.put(key, newProxy);
					proxy = newProxy;
				}
				proxy.addProxied(unit);
			}
			return tempMap.values().stream().sorted(Comparator.comparing(IUnit::getName,
				String.CASE_INSENSITIVE_ORDER)).collect(Collectors.toList());
		}
	}

	/**
	 * All the "kinds" of units the given player has.
	 */
	@Override
	public Iterable<String> getUnitKinds(final Player player) {
		return StreamSupport.stream(getUnits(player).spliterator(), false)
			.map(IUnit::getKind).distinct().sorted(String.CASE_INSENSITIVE_ORDER)
			.collect(Collectors.toList());
	}

	/**
	 * Add the given unit at the given location in all maps.
	 *
	 * FIXME: Should copy into subordinate maps, and return either the unit (in one-map case) or a proxy
	 */
	private void addUnitAtLocation(final IUnit unit, final Point location) {
		if (!getSubordinateMaps().iterator().hasNext()) {
			addUnitAtLocationImpl(unit, location, getRestrictedMap());
			setMapModified(true);
		} else {
			for (IMutableMapNG eachMap : getRestrictedAllMaps()) {
				addUnitAtLocationImpl(unit, location, eachMap);
				eachMap.setModified(true);
			}
		}
	}

	/**
	 * Add a unit to all the maps, at the location of its owner's HQ in the main map.
	 */
	@Override
	public void addUnit(final IUnit unit) {
		Pair<IMutableFortress, Point> temp = null;
		for (Pair<Point, IMutableFortress> pair : getMap().streamLocations()
				.flatMap(l -> getMap().getFixtures(l).stream()
					.filter(IMutableFortress.class::isInstance)
					.map(IMutableFortress.class::cast)
					.filter(f -> f.getOwner().getPlayerId() ==
						unit.getOwner().getPlayerId())
					.map(f -> Pair.with(l, f)))
				.collect(Collectors.toList())) {
			Point point = pair.getValue0();
			IMutableFortress fixture = pair.getValue1();
			if ("HQ".equals(fixture.getName())) {
				addUnitAtLocation(unit, point);
				return;
			} else if (temp == null) {
				temp = Pair.with(fixture, point);
			}
		}
		if (temp != null) {
			IMutableFortress fortress = temp.getValue0();
			Point loc = temp.getValue1();
			LOGGER.info(String.format("Added unit at fortress %s, not HQ", fortress.getName()));
			addUnitAtLocation(unit, loc);
			return;
		} else if (!unit.getOwner().isIndependent()) {
			LOGGER.warning(String.format("No suitable location found for unit %s, owned by %s",
				unit.getName(), unit.getOwner()));
		}
	}

	/**
	 * Get a unit by its owner and ID.
	 */
	@Override
	@Nullable
	public IUnit getUnitByID(final Player owner, final int id) {
		return StreamSupport.stream(getUnits(owner).spliterator(), true)
			.filter(u -> u.getId() == id).findAny().orElse(null);
	}

	private BiPredicate<Point, IFixture> unitMatching(final IUnit unit) {
		return (point, fixture) ->
			fixture instanceof IUnit && fixture.getId() == unit.getId() &&
				((IUnit) fixture).getOwner().equals(unit.getOwner());
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
		LOGGER.fine("In WorkerModel.removeUnit()");
		List<Pair<IMutableMapNG, Pair<Point, IUnit>>> delenda = new ArrayList<>();
		for (IMutableMapNG map : getRestrictedAllMaps()) {
			Pair<Point, IFixture> pair = map.streamLocations()
					.flatMap(l -> map.getFixtures(l).stream()
					.map(f -> Pair.with(l, f)))
					.flatMap(p -> flattenEntries(p.getValue0(), p.getValue1()))
					.filter(p -> unitMatching(unit).test(p.getValue0(),
						p.getValue1()))
					.findAny().orElse(null);
			if (pair != null) {
				LOGGER.fine("Map has matching unit");
				Point location = pair.getValue0();
				IUnit fixture = (IUnit) pair.getValue1();
				if (fixture.getKind().equals(unit.getKind()) &&
						fixture.getName().equals(unit.getName()) &&
						!fixture.iterator().hasNext()) {
					LOGGER.fine("Matching unit meets preconditions");
					delenda.add(Pair.with(map, Pair.with(location, fixture)));
				} else {
					LOGGER.warning(String.format(
						"Matching unit in %s fails preconditions for removal",
						Optional.ofNullable(map.getFilename())
							.map(Object::toString).orElse("an unsaved map")));
					return false;
				}
			}
		}
		if (delenda.isEmpty()) {
			LOGGER.fine("No matching units");
			return false;
		}
		for (Pair<IMutableMapNG, Pair<Point, IUnit>> pair : delenda) {
			IMutableMapNG map = pair.getValue0();
			Point location = pair.getValue1().getValue0();
			IUnit fixture = pair.getValue1().getValue1();
			if (map.getFixtures(location).contains(fixture)) {
				map.removeFixture(location, fixture);
			} else {
				boolean any = false;
				for (IMutableFortress fort : map.getFixtures(location).stream()
						.filter(IMutableFortress.class::isInstance)
						.map(IMutableFortress.class::cast)
						.collect(Collectors.toList())) {
					if (fort.stream().anyMatch(fixture::equals)) {
						any = true;
						fort.removeMember(fixture);
						break;
					}
				}
				if (!any) {
					LOGGER.warning(
						"Failed to find unit to remove that we thought might be in a fortress");
				}
			}
		}
		LOGGER.fine("Finished removing matching unit(s) from map(s)");
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
		if (iterableSize(old.getProxied()) == iterableSize(newOwner.getProxied()) &&
				iterableSize(old.getProxied()) == iterableSize(member.getProxied())) {
			LinkedList<UnitMember> memberProxied = new LinkedList<UnitMember>(
				StreamSupport.stream(member.getProxied().spliterator(), false)
					.collect(Collectors.toList()));
			LinkedList<IUnit> oldProxied = new LinkedList<IUnit>(StreamSupport.stream(
				old.getProxied().spliterator(), false).collect(Collectors.toList()));
			LinkedList<IUnit> newProxied = new LinkedList<IUnit>(StreamSupport.stream(
				newOwner.getProxied().spliterator(), false).collect(Collectors.toList()));
			Deque<UnitMember> members = new LinkedList<>();
			Deque<IMutableUnit> newList = new LinkedList<>();
			while (!memberProxied.isEmpty() && !oldProxied.isEmpty() && !newProxied.isEmpty()) {
				UnitMember item = memberProxied.removeFirst();
				IUnit innerOld = oldProxied.removeFirst();
				IUnit innerNew = newProxied.removeFirst();
				if (innerOld instanceof IMutableUnit && innerNew instanceof IMutableUnit) {
					((IMutableUnit) innerOld).removeMember(item);
					members.addLast(item);
					newList.addLast((IMutableUnit) innerNew);
				} else {
					LOGGER.warning("Immutable unit in moveProxied()");
					return false;
				}
			}
			while (!newList.isEmpty() && !members.isEmpty()) {
				IMutableUnit unit = newList.removeFirst();
				UnitMember innerMember = members.removeFirst();
				unit.addMember(innerMember);
			}
			for (IMutableMapNG map : getRestrictedAllMaps()) {
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
	 * which was extracted as {@link moveProxied}.
	 */
	@Override
	public void moveMember(final UnitMember member, final IUnit old, final IUnit newOwner) {
		if (member instanceof ProxyFor && old instanceof ProxyUnit &&
				newOwner instanceof ProxyUnit &&
				moveProxied((ProxyFor<? extends UnitMember>) member, (ProxyUnit) old,
					(ProxyUnit) newOwner)) {
			return;
		}
		for (IMutableMapNG map : getRestrictedAllMaps()) {
			// TODO: Add streamAllFixtures() to IMapNG.
			IMutableUnit matchingOld = getUnitsImpl(map.streamLocations()
						.flatMap(l -> map.getFixtures(l).stream())
						.collect(Collectors.toList()), old.getOwner()).stream()
				.filter(IMutableUnit.class::isInstance).map(IMutableUnit.class::cast)
				.filter(u -> u.getKind().equals(old.getKind()))
				.filter(u -> u.getName().equals(old.getName()))
				.filter(u -> u.getId() == old.getId())
				.findAny().orElse(null);
			if (matchingOld != null) {
				UnitMember matchingMember = matchingOld.stream().filter(member::equals) // TODO: equals() isn't ideal for finding a matching member ...
					.findAny().orElse(null);
				IMutableUnit matchingNew = getUnitsImpl(map.streamLocations()
							.flatMap(l -> map.getFixtures(l).stream())
							.collect(Collectors.toList()), newOwner.getOwner())
						.stream()
						.filter(IMutableUnit.class::isInstance)
						.map(IMutableUnit.class::cast)
						.filter(u -> u.getKind().equals(newOwner.getKind()))
						.filter(u -> u.getName().equals(newOwner.getName()))
						.filter(u -> u.getId() == newOwner.getId())
						.findAny().orElse(null);
				if (matchingMember != null && matchingNew != null) {
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
		for (IMutableMapNG map : getRestrictedAllMaps()) {
			for (IMutableUnit unit : getUnitsImpl(map.streamLocations()
							.flatMap(l -> map.getFixtures(l).stream())
							.collect(Collectors.toList()),
						getCurrentPlayer()).stream()
					.filter(IMutableUnit.class::isInstance)
					.map(IMutableUnit.class::cast).collect(Collectors.toList())) {
				UnitMember matching = unit.stream().filter(member::equals) // FIXME: equals() will really not do here ...
					.findAny().orElse(null);
				if (matching != null) {
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
		for (IMutableMapNG map : getRestrictedAllMaps()) {
			IMutableUnit matching = getUnitsImpl(map.streamLocations()
						.flatMap(l -> map.getFixtures(l).stream())
						.collect(Collectors.toList()), unit.getOwner()).stream()
				.filter(IMutableUnit.class::isInstance)
				.map(IMutableUnit.class::cast)
				.filter(u -> u.getName().equals(unit.getName()))
				.filter(u -> u.getKind().equals(unit.getKind()))
				.filter(u -> u.getId() == unit.getId())
				.findAny().orElse(null);
			if (matching != null) {
				matching.addMember(member.copy(false));
				map.setModified(true);
				continue;
			}
		}
	}

	@Override
	public boolean renameItem(final HasMutableName item, final String newName) {
		boolean any = false;
		if (item instanceof IUnit) {
			for (IMutableMapNG map : getRestrictedAllMaps()) {
				IUnit matching =
					getUnitsImpl(map.streamLocations()
							.flatMap(l -> map.getFixtures(l).stream())
							.collect(Collectors.toList()),
						((IUnit) item).getOwner()).stream()
					.filter(u -> u.getName().equals(((IUnit) item).getName()))
					.filter(u -> u.getKind().equals(((IUnit) item).getKind()))
					.filter(u -> u.getId() == ((IUnit) item).getId())
					.findAny().orElse(null);
				if (matching instanceof HasMutableName) {
					any = true;
					((HasMutableName) matching).setName(newName);
					map.setModified(true);
				}
			}
			if (!any) {
				LOGGER.warning("Unable to find unit to rename");
			}
			return any;
		} else if (item instanceof UnitMember) {
			for (IMutableMapNG map : getRestrictedAllMaps()) {
				UnitMember matching =
					getUnitsImpl(map.streamLocations()
							.flatMap(l -> map.getFixtures(l).stream())
							.collect(Collectors.toList()), getCurrentPlayer())
						.stream().flatMap(u -> u.stream())
						.filter(HasMutableName.class::isInstance)
						.filter(m -> m.getId() == ((UnitMember) item).getId())
						.filter(m -> ((HasMutableName) m).getName()
							.equals(item.getName()))
						.findAny().orElse(null); // FIXME: We should have a firmer identification than just name and ID
				if (matching != null) {
					any = true;
					((HasMutableName) matching).setName(newName);
					map.setModified(true);
				}
			}
			if (!any) {
				LOGGER.warning("Unable to find unit member to rename");
			}
			return any;
		} else {
			LOGGER.warning("Unable to find item to rename");
			return false;
		}
	}

	@Override
	public boolean changeKind(final HasKind item, final String newKind) {
		boolean any = false;
		if (item instanceof IUnit) {
			for (IMutableMapNG map : getRestrictedAllMaps()) {
				IUnit matching = getUnitsImpl(map.streamLocations()
							.flatMap(l -> map.getFixtures(l).stream())
							.collect(Collectors.toList()),
						((IUnit) item).getOwner()).stream()
					.filter(u -> u.getName().equals(((IUnit) item).getName()))
					.filter(u -> u.getKind().equals(((IUnit) item).getKind()))
					.filter(u -> u.getId() == ((IUnit) item).getId())
					.findAny().orElse(null);
				if (matching instanceof HasMutableKind) {
					any = true;
					((HasMutableKind) matching).setKind(newKind);
					map.setModified(true);
				}
			}
			if (!any) {
				LOGGER.warning("Unable to find unit to change kind");
			}
			return any;
		} else if (item instanceof UnitMember) {
			for (IMutableMapNG map : getRestrictedAllMaps()) {
				HasMutableKind matching = getUnitsImpl(map.streamLocations()
							.flatMap(l -> map.getFixtures(l).stream())
							.collect(Collectors.toList()), getCurrentPlayer())
					.stream().flatMap(u -> u.stream())
					.filter(m -> m.getId() == ((UnitMember) item).getId())
					.filter(HasMutableKind.class::isInstance)
					.map(HasMutableKind.class::cast)
					.filter(m -> m.getKind().equals(item.getKind()))
					.findAny().orElse(null); // FIXME: We should have a firmer identification than just kind and ID
				if (matching != null) {
					any = true;
					matching.setKind(newKind);
					map.setModified(true);
				}
			}
			if (!any) {
				LOGGER.warning("Unable to find unit member to change kind");
			}
			return any;
		} else {
			LOGGER.warning("Unable to find item to change kind");
			return false;
		}
	}

	@Override
	public boolean addSibling(final UnitMember existing, final UnitMember sibling) {
		boolean any = false;
		for (IMutableMapNG map : getRestrictedAllMaps()) {
			for (IMutableUnit unit : getUnitsImpl(map.streamLocations()
						.flatMap(l -> map.getFixtures(l).stream())
						.collect(Collectors.toList()), getCurrentPlayer())
					.stream().filter(IMutableUnit.class::isInstance)
					.map(IMutableUnit.class::cast).collect(Collectors.toList())) {
				if (unit.stream().anyMatch(existing::equals)) {
					// TODO: look beyond equals() for matching-in-existing?
					unit.addMember(sibling.copy(false));
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
	public boolean changeOwner(final HasMutableOwner item, final Player newOwner) {
		boolean any = false;
		for (IMutableMapNG map : getRestrictedAllMaps()) {
			HasMutableOwner matching = map.streamLocations()
				.flatMap(l -> map.getFixtures(l).stream()).flatMap(WorkerModel::flattenIncluding)
				.flatMap(WorkerModel::flattenIncluding).filter(HasMutableOwner.class::isInstance)
				.map(HasMutableOwner.class::cast)
				.filter(item::equals) // TODO: equals() is not the best way to find it ...
				.findAny().orElse(null);
			if (matching != null) {
				if (StreamSupport.stream(map.getPlayers().spliterator(), true)
						.noneMatch(newOwner::equals)) {
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
		for (IMutableMapNG map : getRestrictedAllMaps()) {
			IMutableUnit matching = getUnitsImpl(map.streamLocations()
					.flatMap(l -> map.getFixtures(l).stream())
					.collect(Collectors.toList()), getCurrentPlayer()).stream()
				.filter(IMutableUnit.class::isInstance).map(IMutableUnit.class::cast)
				.filter(u -> u.getName().equals(fixture.getName()))
				.filter(u -> u.getKind().equals(fixture.getKind()))
				.filter(u -> u.getId() == fixture.getId())
				.findAny().orElse(null);
			if (matching != null) {
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
		for (IMutableMapNG map : getRestrictedAllMaps()) {
			IMutableWorker matching = getUnitsImpl(map.streamLocations()
					.flatMap(l -> map.getFixtures(l).stream())
					.collect(Collectors.toList()), getCurrentPlayer()).stream()
				.flatMap(u -> u.stream()).filter(IMutableWorker.class::isInstance)
				.map(IMutableWorker.class::cast)
				.filter(w -> w.getRace().equals(worker.getRace()))
				.filter(w -> w.getName().equals(worker.getName()))
				.filter(w -> w.getId() == worker.getId())
				.findAny().orElse(null);
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
	public boolean addHoursToSkill(final IWorker worker, final String jobName, final String skillName, final int hours,
	                               final int contextValue) {
		boolean any = false;
		for (IMutableMapNG map : getRestrictedAllMaps()) {
			IMutableWorker matching = getUnitsImpl(map.streamLocations()
					.flatMap(l -> map.getFixtures(l).stream())
					.collect(Collectors.toList()), getCurrentPlayer()).stream()
				.flatMap(u -> u.stream()).filter(IMutableWorker.class::isInstance)
				.map(IMutableWorker.class::cast)
				.filter(w -> w.getRace().equals(worker.getRace()))
				.filter(w -> w.getName().equals(worker.getName()))
				.filter(w -> w.getId() == worker.getId())
				.findAny().orElse(null);
			if (matching != null) {
				map.setModified(true);
				any = true;
				IMutableJob job;
				IMutableJob temp = StreamSupport.stream(matching.spliterator(), true)
					.filter(IMutableJob.class::isInstance).map(IMutableJob.class::cast)
					.filter(j -> jobName.equals(j.getName())).findAny().orElse(null);
				if (temp == null) {
					job = new Job(jobName, 0);
					matching.addJob(job); // FIXME: The IWorker API doc explicitly says the Job object can't be assumed to have been preserved
				} else {
					job = temp;
				}
				IMutableSkill skill;
				IMutableSkill tempSkill = StreamSupport.stream(job.spliterator(), true)
					.filter(IMutableSkill.class::isInstance)
					.map(IMutableSkill.class::cast)
					.filter(s -> skillName.equals(s.getName()))
					.findAny().orElse(null);
				if (tempSkill == null) {
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
	 * Replace {@link delenda one skill|delenda} with {@link replacement
	 * another} in the specified job in the specified worker in all maps.
	 * Unlike {@link addHoursToSkill}, if a map does not have an
	 * <em>equal</em> Job in the matching worker, that map is completely
	 * skipped.  If the replacement is already present, just remove the
	 * first skill. Returns true if the operation was carried out in any of
	 * the maps, false otherwise.
	 */
	@Override
	public boolean replaceSkillInJob(final IWorker worker, final String jobName, final ISkill delenda,
	                                 final ISkill replacement) {
		boolean any = false;
		for (IMutableMapNG map : getRestrictedAllMaps()) {
			IMutableWorker matchingWorker = getUnitsImpl(map.streamLocations()
					.flatMap(l -> map.getFixtures(l).stream())
					.collect(Collectors.toList()), getCurrentPlayer()).stream()
				.flatMap(u -> u.stream())
				.filter(IMutableWorker.class::isInstance)
				.map(IMutableWorker.class::cast)
				.filter(w -> w.getRace().equals(worker.getRace()))
				.filter(w -> w.getName().equals(worker.getName()))
				.filter(w -> w.getId() == worker.getId())
				.findAny().orElse(null);
			if (matchingWorker != null) {
				IMutableJob matchingJob = StreamSupport.stream(
						matchingWorker.spliterator(), true)
					.filter(IMutableJob.class::isInstance).map(IMutableJob.class::cast)
					.filter(j -> jobName.equals(j.getName())).findAny().orElse(null);
				if (matchingJob != null) {
					ISkill matchingSkill = StreamSupport.stream(
							matchingJob.spliterator(), true)
						.filter(delenda::equals).findAny().orElse(null);
					if (matchingSkill != null) {
						map.setModified(true);
						any = true;
						matchingJob.removeSkill(matchingSkill);
						matchingJob.addSkill(replacement.copy());
					} else {
						LOGGER.warning("No matching skill in matching worker");
					}
				} else {
					LOGGER.warning("No matching skill in matching worker");
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
		for (IMutableMapNG map : getRestrictedAllMaps()) {
			// TODO: Why not use getUnitsImpl?
			IMutableUnit matching = map.streamLocations()
				.flatMap(l -> map.getFixtures(l).stream()).flatMap(WorkerModel::flatten)
				.filter(IMutableUnit.class::isInstance).map(IMutableUnit.class::cast)
				.filter(u -> u.getOwner().equals(unit.getOwner()))
				.filter(u -> u.getKind().equals(unit.getKind()))
				.filter(u -> u.getName().equals(unit.getName()))
				.filter(u -> u.getId() == unit.getId())
				.findAny().orElse(null);
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
		for (IMutableMapNG map : getRestrictedAllMaps()) {
			// TODO: Why not use getUnitsImpl?
			IMutableUnit matching = map.streamLocations()
				.flatMap(l -> map.getFixtures(l).stream()).flatMap(WorkerModel::flatten)
				.filter(IMutableUnit.class::isInstance).map(IMutableUnit.class::cast)
				.filter(u -> u.getOwner().equals(unit.getOwner()))
				.filter(u -> u.getKind().equals(unit.getKind()))
				.filter(u -> u.getName().equals(unit.getName()))
				.filter(u -> u.getId() == unit.getId())
				.findAny().orElse(null);
			if (matching != null) {
				matching.setResults(turn, results);
				map.setModified(true);
				any = true;
			}
		}
		return any;
	}
}

