package common.map.fixtures.mobile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.util.Map.Entry;

import common.map.IFixture;
import common.map.TileFixture;
import common.map.Player;
import common.map.PlayerImpl;
import common.map.fixtures.UnitMember;
import common.map.fixtures.mobile.ProxyFor;
import common.map.fixtures.mobile.IUnit;
import common.map.fixtures.mobile.ProxyMember;
import common.map.fixtures.mobile.IWorker;
import common.map.fixtures.mobile.worker.ProxyWorker;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A proxy for units in multiple maps, or all a player's units of one kind.
 */
public class ProxyUnit implements IUnit, ProxyFor<IUnit> {
	private static final Logger LOGGER = Logger.getLogger(ProxyUnit.class.getName());
	/**
	 * If true, we are proxying parallel units in different maps; if false,
	 * multiple units of the same kind owned by one player.
	 */
	private final boolean parallel;

	/**
	 * If true, we are proxying parallel units in different maps; if false,
	 * multiple units of the same kind owned by one player.
	 */
	@Override
	public boolean isParallel() {
		return parallel;
	}

	/**
	 * The units we are a proxy for.
	 */
	private final List<IUnit> proxiedList = new ArrayList<>();

	private Iterable<UnitMember> cachedIterable = Collections.emptyList();

	private static String mergeHelper(String earlier, String later) {
		return Objects.equals(earlier, later) ? earlier : "";
	}

	NavigableMap<Integer, String> mergeMaps(Function<IUnit, NavigableMap<Integer, String>> method) {
		return proxiedList.stream().map(method).map(NavigableMap::entrySet).flatMap(Set::stream)
			.collect(Collectors.toMap(Entry::getKey, Entry::getValue, ProxyUnit::mergeHelper,
				TreeMap::new));
	}

	/**
	 * If we're proxying parallel units, they all share this ID. If not, this is null.
	 */
	private Integer commonID;

	/**
	 * If we're proxying all units of a given kind, it's this kind; if
	 * we're proxying parallel units, this is null.
	 */
	private String commonKind;

	/**
	 * Constructor for units from parallel maps.
	 */
	public ProxyUnit(int idNum) {
		commonID = idNum;
		parallel = true;
		commonKind = null;
	}

	/**
	 * Constructor for units sharing a kind.
	 */
	public ProxyUnit(String unitKind) {
		commonID = null;
		commonKind = unitKind;
		parallel = false;
	}

	/**
	 * All orders shared by all the proxied units.
	 */
	@Override
	public NavigableMap<Integer, String> getAllOrders() {
		return mergeMaps(IUnit::getAllOrders);
	}

	/**
	 * All results shared by all the proxied units.
	 */
	@Override
	public NavigableMap<Integer, String> getAllResults() {
		return mergeMaps(IUnit::getAllResults);
	}

	@Override
	public IUnit copy(boolean zero) {
		final ProxyUnit retval;
		if (parallel) {
			retval = new ProxyUnit(commonID);
		} else {
			retval = new ProxyUnit(commonKind);
		}
		for (IUnit unit : proxiedList) {
			retval.addProxied(unit.copy(zero));
		}
		return retval;
	}

	private final Player defaultPlayer = new PlayerImpl(-1, "proxied");

	@Override
	public String getKind() {
		if (parallel) {
			String retval = getConsensus(IUnit::getKind);
			return retval == null ? "proxied" : retval;
		} else {
			return commonKind;
		}
	}

	@Override
	public String getShortDescription() {
		if (parallel || proxiedList.size() == 1) {
			if (getOwner().isCurrent()) {
				return String.format("a(n) %s unit belonging to you", getKind());
			} else if (getOwner().isIndependent()) {
				return String.format("an independent %s unit", getKind());
			} else {
				return String.format("a(n) %s unit belonging to %s", getKind(),
					getOwner().toString());
			}
		} else {
			return "Multiple units of kind " + getKind();
		}
	}

	@Override
	public int getId() {
		if (parallel) {
			return commonID;
		} else {
			return -1;
		}
	}

	@Override
	public boolean equalsIgnoringID(IFixture fixture) {
		LOGGER.severe("ProxyUnit.equalsIgnoringID called");
		if (fixture instanceof ProxyUnit) {
			return proxiedList.stream().allMatch(m -> (((ProxyUnit) fixture).proxiedList.stream().anyMatch(m::equals))); // TODO: Should check the converse as well
		} else {
			return false;
		}
	}

	@Override
	public int compareTo(TileFixture fixture) {
		LOGGER.warning("ProxyUnit.compare called");
		return IUnit.super.compareTo(fixture);
	}

	@Override
	public String getDefaultImage() {
		if (proxiedList.isEmpty()) {
			return "";
		}
		String img = getConsensus(IUnit::getDefaultImage);
		return img == null ? "unit.png" : img;
	}

	@Override
	public String getImage() {
		String img = getConsensus(IUnit::getImage);
		return img == null ? "" : img;
	}

	@Override
	public Iterator<UnitMember> iterator() {
		if (!parallel || proxiedList.isEmpty()) {
			return Collections.emptyIterator();
		} else if (!cachedIterable.iterator().hasNext()) {
			return cachedIterable.iterator();
		} else {
			Map<Integer, UnitMemberProxy<? extends UnitMember>> map =
				new LinkedHashMap<>();
			for (IUnit unit : proxiedList) {
				for (UnitMember member : unit) {
					UnitMemberProxy<? extends UnitMember> proxy;
					int memberID = member.getId();
					if (map.containsKey(memberID)) {
						proxy = map.get(memberID);
						if (proxy instanceof WorkerProxy) {
							if (member instanceof IWorker) {
								((WorkerProxy) proxy).addProxied((IWorker) member);
							} else {
								LOGGER.warning("ProxyWorker matched non-worker");
							}
						} else if (proxy instanceof AnimalProxy) {
							if (member instanceof Animal) {
								((AnimalProxy) proxy).addProxied((Animal) member);
							} else {
								LOGGER.warning("ProxyAnimal matched non-animal");
							}
						} else {
							((UnitMemberProxy<UnitMember>) proxy).addProxied(member);
						}
					} else {
						if (member instanceof IWorker) {
							proxy = new ProxyWorker((IWorker) member);
						} else if (member instanceof Animal) {
							proxy = new ProxyAnimal((Animal) member);
						} else {
							proxy = new ProxyMember(member);
						}
						map.put(memberID, proxy);
					}
				}
			}
			// FIXME: Make sure this doesn't result in ClassCastExceptions
			cachedIterable = ((Map<Integer, UnitMember>) ((Map<Integer, ? extends UnitMember>) map)).values();
			return cachedIterable.iterator();
		}
	}

	@Override
	public Stream<UnitMember> stream() {
		return StreamSupport.stream(spliterator(), false);
	}

	@Override
	public String getName() {
		String retval = getConsensus(IUnit::getName);
		return retval == null ? "proxied" : retval;
	}

	@Override
	public String getPortrait() {
		String retval = getConsensus(IUnit::getPortrait);
		return retval == null ? "" : retval;
	}

	@Override
	public Player getOwner() {
		Player retval = getConsensus(IUnit::getOwner);
		return retval == null ? defaultPlayer : retval;
	}

	@Override
	public boolean isSubset(IFixture obj, Consumer<String> report) {
		report.accept("Called ProxyUnit.isSubset()");
		return IUnit.super.isSubset(obj, s -> report.accept("In proxy unit:\t" + s));
	}

	@Override
	public String getOrders(int turn) {
		String retval = getConsensus(u -> u.getOrders(turn));
		return retval == null ? "" : retval;
	}

	@Override
	public String getResults(int turn) {
		String retval = getConsensus(u -> u.getResults(turn));
		return retval == null ? "" : retval;
	}

	@Override
	public String getVerbose() {
		if (parallel) {
			Iterator<IUnit> iterator = proxiedList.iterator();
			if (iterator.hasNext()) {
				return "A proxy for units in several maps, such as the following:\n" +
					iterator.next().getVerbose();
			} else {
				return "A proxy for units in several maps, but no units yet.";
			}
		} else {
			return "A proxy for units of kind " + commonKind;
		}
	}

	@Override
	public Iterable<IUnit> getProxied() {
		return new ArrayList<>(proxiedList);
	}

	@Override
	public String toString() {
		return (parallel) ? "ProxyUnit for ID #" + commonID :
			"ProxyUnit for units of kind " + commonKind;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ProxyUnit) {
			return parallel == ((ProxyUnit) obj).parallel &&
				Objects.equals(commonID, ((ProxyUnit) obj).commonID) &&
				Objects.equals(commonKind, ((ProxyUnit) obj).commonKind) &&
				proxiedList.equals(((ProxyUnit) obj).proxiedList);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		IUnit[] proxied = new IUnit[proxiedList.size()];
		return Arrays.hashCode(proxiedList.toArray(proxied));
	}

	@Override
	public int getDC() {
		Integer retval = getConsensus(IUnit::getDC);
		return retval == null ? 10 : retval;
	}

	/**
	 * Proxy an additonal unit.
	 */
	@Override
	public void addProxied(IUnit item) {
		if (item == this) {
			return;
		} else if (parallel && item.getId() != commonID) {
			throw new IllegalArgumentException("Unit must have ID #" + commonID);
		} else if (!parallel && !Objects.equals(commonKind, item.getKind())) {
			throw new IllegalArgumentException("Unit must have kind " + commonKind);
		}
		cachedIterable = Collections.emptyList();
		proxiedList.add(item);
	}

	@Override
	public boolean isEmpty() {
		return !iterator().hasNext();
	}
}
