package legacy.map.fixtures.mobile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
import java.util.stream.Collectors;

import static java.util.Map.Entry;

import legacy.map.IFixture;
import legacy.map.TileFixture;
import legacy.map.Player;
import legacy.map.PlayerImpl;
import legacy.map.fixtures.UnitMember;
import legacy.map.fixtures.mobile.worker.ProxyWorker;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import lovelace.util.LovelaceLogger;
import org.jetbrains.annotations.Nullable;

/**
 * A proxy for units in multiple maps, or all a player's units of one kind.
 *
 * @deprecated We're trying to get rid of the notion of 'proxies' in favor of
 * driver model methods.
 */
@Deprecated
public class ProxyUnit implements IUnit, ProxyFor<IUnit> {
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

	private static String mergeHelper(final String earlier, final String later) {
		return Objects.equals(earlier, later) ? earlier : "";
	}

	NavigableMap<Integer, String> mergeMaps(final Function<IUnit, NavigableMap<Integer, String>> method) {
		return proxiedList.stream().map(method).map(NavigableMap::entrySet).flatMap(Set::stream)
			.collect(Collectors.toMap(Entry::getKey, Entry::getValue, ProxyUnit::mergeHelper,
				TreeMap::new));
	}

	/**
	 * If we're proxying parallel units, they all share this ID. If not, this is null.
	 */
	private final @Nullable Integer commonID;

	/**
	 * If we're proxying all units of a given kind, it's this kind; if
	 * we're proxying parallel units, this is null.
	 */
	private final @Nullable String commonKind;

	/**
	 * Constructor for units from parallel maps.
	 */
	public ProxyUnit(final int idNum) {
		commonID = idNum;
		parallel = true;
		commonKind = null;
	}

	/**
	 * Constructor for units sharing a kind.
	 */
	public ProxyUnit(final String unitKind) {
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
	public IUnit copy(final CopyBehavior zero) {
		final ProxyUnit retval;
		if (parallel) {
			assert (commonID != null);
			retval = new ProxyUnit(commonID);
		} else {
			assert (commonKind != null);
			retval = new ProxyUnit(commonKind);
		}
		for (final IUnit unit : proxiedList) {
			retval.addProxied(unit.copy(zero));
		}
		return retval;
	}

	private final Player defaultPlayer = new PlayerImpl(-1, "proxied");

	@Override
	public String getKind() {
		if (parallel) {
			final String retval = getConsensus(IUnit::getKind);
			return retval == null ? "proxied" : retval;
		} else {
			return Objects.requireNonNullElse(commonKind, "proxied");
		}
	}

	@Override
	public String getShortDescription() {
		if (parallel || proxiedList.size() == 1) {
			if (owner().isCurrent()) {
				return String.format("a(n) %s unit belonging to you", getKind());
			} else if (owner().isIndependent()) {
				return String.format("an independent %s unit", getKind());
			} else {
				return String.format("a(n) %s unit belonging to %s", getKind(),
					owner());
			}
		} else {
			return "Multiple units of kind " + getKind();
		}
	}

	@Override
	public int getId() {
		if (parallel) {
			return Objects.requireNonNullElse(commonID, -1);
		} else {
			return -1;
		}
	}

	@Override
	public boolean equalsIgnoringID(final IFixture fixture) {
		LovelaceLogger.error("ProxyUnit.equalsIgnoringID called");
		if (fixture instanceof final ProxyUnit pu) {
			return proxiedList.stream().allMatch(m -> (pu.proxiedList.stream().anyMatch(m::equals))); // TODO: Should check the converse as well
		} else {
			return false;
		}
	}

	@Override
	public int compareTo(final TileFixture fixture) {
		LovelaceLogger.warning("ProxyUnit.compare called");
		return IUnit.super.compareTo(fixture);
	}

	@Override
	public String getDefaultImage() {
		if (proxiedList.isEmpty()) {
			return "";
		}
		final String img = getConsensus(IUnit::getDefaultImage);
		return img == null ? "unit.png" : img;
	}

	@Override
	public String getImage() {
		final String img = getConsensus(IUnit::getImage);
		return img == null ? "" : img;
	}

	@Override
	public Iterator<UnitMember> iterator() {
		if (!parallel || proxiedList.isEmpty()) {
			return Collections.emptyIterator();
		} else if (cachedIterable.iterator().hasNext()) {
			return cachedIterable.iterator();
		} else {
			final Map<Integer, UnitMemberProxy<? extends UnitMember>> map =
				new LinkedHashMap<>();
			for (final IUnit unit : proxiedList) {
				for (final UnitMember member : unit) {
					final UnitMemberProxy<? extends UnitMember> proxy;
					final int memberID = member.getId();
					if (map.containsKey(memberID)) {
						proxy = map.get(memberID);
						if (proxy instanceof final WorkerProxy wp) {
							if (member instanceof final IWorker w) {
								wp.addProxied(w);
							} else {
								LovelaceLogger.warning("ProxyWorker matched non-worker");
							}
						} else if (proxy instanceof final AnimalProxy ap) {
							if (member instanceof final Animal a) {
								ap.addProxied(a);
							} else {
								LovelaceLogger.warning("ProxyAnimal matched non-animal");
							}
						} else {
							((UnitMemberProxy<UnitMember>) proxy).addProxied(member);
						}
					} else {
						if (member instanceof final IWorker w) {
							proxy = new ProxyWorker(w);
						} else if (member instanceof final Animal a) {
							proxy = new ProxyAnimal(a);
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
		final String retval = getConsensus(IUnit::getName);
		return retval == null ? "proxied" : retval;
	}

	@Override
	public String getPortrait() {
		final String retval = getConsensus(IUnit::getPortrait);
		return retval == null ? "" : retval;
	}

	@Override
	public Player owner() {
		final Player retval = getConsensus(IUnit::owner);
		return retval == null ? defaultPlayer : retval;
	}

	@Override
	public boolean isSubset(final IFixture obj, final Consumer<String> report) {
		report.accept("Called ProxyUnit.isSubset()");
		return IUnit.super.isSubset(obj, s -> report.accept("In proxy unit:\t" + s));
	}

	@Override
	public String getOrders(final int turn) {
		final String retval = getConsensus(u -> u.getOrders(turn));
		return retval == null ? "" : retval;
	}

	@Override
	public String getResults(final int turn) {
		final String retval = getConsensus(u -> u.getResults(turn));
		return retval == null ? "" : retval;
	}

	@Override
	public String getVerbose() {
		if (parallel) {
			final Iterator<IUnit> iterator = proxiedList.iterator();
			if (iterator.hasNext()) {
				return "A proxy for units in several maps, such as the following:" + System.lineSeparator() +
					iterator.next().getVerbose();
			} else {
				return "A proxy for units in several maps, but no units yet.";
			}
		} else {
			return "A proxy for units of kind " + commonKind;
		}
	}

	@Override
	public Collection<IUnit> getProxied() {
		return new ArrayList<>(proxiedList);
	}

	@Override
	public String toString() {
		return (parallel) ? "ProxyUnit for ID #" + commonID :
			"ProxyUnit for units of kind " + commonKind;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof final ProxyUnit pu) {
			return parallel == pu.parallel &&
				Objects.equals(commonID, pu.commonID) &&
				Objects.equals(commonKind, pu.commonKind) &&
				proxiedList.equals(pu.proxiedList);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		final IUnit[] proxied = new IUnit[proxiedList.size()];
		return Arrays.hashCode(proxiedList.toArray(proxied));
	}

	@Override
	public int getDC() {
		final Integer retval = getConsensus(IUnit::getDC);
		return retval == null ? 10 : retval;
	}

	/**
	 * Proxy an additonal unit.
	 */
	@Override
	public void addProxied(final IUnit item) {
		if (item == this) {
			return;
		} else if (parallel && !Objects.equals(item.getId(), commonID)) {
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
