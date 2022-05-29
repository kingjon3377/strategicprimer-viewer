package common.map.fixtures.mobile;

import java.util.Comparator;
import java.util.Collections;
import java.util.Iterator;
import java.util.NavigableMap;
import java.util.TreeMap;

import lovelace.util.ArraySet;
import common.map.IFixture;
import common.map.TileFixture;
import common.map.Player;
import common.map.fixtures.Implement;
import common.map.fixtures.IResourcePile;
import common.map.fixtures.UnitMember;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lovelace.util.LovelaceLogger;

/**
 * A unit in the map.
 *
 * FIXME: we need more members: something about stats; what else?
 */
public final class Unit implements IMutableUnit {
	public Unit(final Player owner, final String kind, final String name, final int id) {
		this.owner = owner;
		this.kind = kind;
		this.name = name;
		this.id = id;
	}

	/**
	 * The unit's orders. This is serialized to and from XML, but does not
	 * affect equality or hashing, and is not printed in {@link toString}.
	 */
	private final NavigableMap<Integer, String> orders = new TreeMap<>();

	/**
	 * The unit's results. This is serialized to and from XML, but does not
	 * affect equality or hashing, and is not printed in {@link toString}.
	 */
	private final NavigableMap<Integer, String> results = new TreeMap<>();

	/**
	 * The members of the unit.
	 */
	private final ArraySet<UnitMember> members = new ArraySet<>();

	/**
	 * The ID number.
	 */
	private final int id;

	/**
	 * The ID number.
	 */
	@Override
	public int getId() {
		return id;
	}

	/**
	 * The filename of an image to use as an icon for this instance.
	 */
	private String image = "";

	/**
	 * The filename of an image to use as an icon for this instance.
	 */
	@Override
	public String getImage() {
		return image;
	}

	/**
	 * Set the filename of an image to use as an icon for this instance.
	 */
	@Override
	public void setImage(final String image) {
		this.image = image;
	}

	/**
	 * The player that owns the unit.
	 */
	private Player owner;

	/**
	 * The player that owns the unit.
	 */
	@Override
	public Player getOwner() {
		return owner;
	}

	/**
	 * Set the player that owns the unit.
	 */
	@Override
	public void setOwner(final Player owner) {
		this.owner = owner;
	}

	/**
	 * What kind of unit this is. For player-owned units this is usually
	 * their "category" (e.g. "agriculture"); for independent units it's
	 * more descriptive.
	 */
	private String kind;

	/**
	 * What kind of unit this is. For player-owned units this is usually
	 * their "category" (e.g. "agriculture"); for independent units it's
	 * more descriptive.
	 */
	@Override
	public String getKind() {
		return kind;
	}

	/**
	 * Set what kind of unit this is.
	 */
	@Override
	public void setKind(final String kind) {
		this.kind = kind;
	}

	/**
	 * The name of this unit. For independent this is often something like
	 * "party from the village".
	 */
	private String name;

	/**
	 * The name of this unit. For independent this is often something like
	 * "party from the village".
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * Set the name of this unit.
	 */
	@Override
	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * The filename of an image to use as a portrait for the unit.
	 */
	private String portrait = "";

	/**
	 * The filename of an image to use as a portrait for the unit.
	 */
	@Override
	public String getPortrait() {
		return portrait;
	}

	/**
	 * Set the filename of an image to use as a portrait for the unit.
	 */
	@Override
	public void setPortrait(final String portrait) {
		this.portrait = portrait;
	}

	/**
	 * The unit's orders for all turns.
	 */
	@Override
	public NavigableMap<Integer, String> getAllOrders() {
		return Collections.unmodifiableNavigableMap(orders);
	}

	/**
	 * The unit's results for all turns.
	 */
	@Override
	public NavigableMap<Integer, String> getAllResults() {
		return Collections.unmodifiableNavigableMap(results);
	}

	/**
	 * Clone the unit.
	 *
	 * TODO: There should be some way to convey the unit's *size* without
	 * the *details* of its contents. Or maybe we should give the contents
	 * but not *their* details?
	 */
	@Override
	public Unit copy(final CopyBehavior zero) {
		final Unit retval = new Unit(owner, kind, name, id);
		if (zero == CopyBehavior.KEEP) {
			retval.orders.putAll(orders);
			retval.results.putAll(results);
			for (final UnitMember member : members) {
				retval.addMember(member.copy(CopyBehavior.KEEP));
			}
		}
		retval.setImage(image);
		return retval;
	}

	/**
	 * Add a member.
	 */
	@Override
	public void addMember(final UnitMember member) {
		if (member instanceof ProxyFor) {
			LovelaceLogger.error(new IllegalStateException("Proxy member added to Unit"), "Proxy member added to Unit");
		}
		members.add(member);
	}

	/**
	 * Remove a member.
	 */
	@Override
	public void removeMember(final UnitMember member) {
		members.remove(member);
	}

	/**
	 * An iterator over the unit's members.
	 */
	@Override
	public Iterator<UnitMember> iterator() {
		return members.iterator();
	}

	@Override
	public Stream<UnitMember> stream() {
		return members.stream();
	}

	/**
	 * An object is equal iff it is a IUnit owned by the same player, with
	 * the same kind, ID, and name, and with equal members.
	 */
	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof IUnit that) {
			return that.getOwner().getPlayerId() == owner.getPlayerId() &&
				kind.equals(that.getKind()) &&
				name.equals(that.getName()) &&
				that.getId() == id &&
				members.containsAll(that.stream().collect(Collectors.toList())) &&
				that.stream().collect(Collectors.toList()).containsAll(members);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public String toString() {
		if (owner.isIndependent()) {
			return String.format("Independent unit of type %s, named %s", kind, name);
		} else {
			return String.format("Unit of type %s, belonging to %s, named %s",
				kind, owner, name);
		}
	}

	@Override
	public String getVerbose() {
		return String.format("%s, consisting of:%n%s", this,
			String.join(System.lineSeparator(),
				members.stream().map(Object::toString).toArray(String[]::new)));
	}

	/**
	 * An icon to represent units by default.
	 *
	 * @author {@link https://openclipart.org/detail/28731/sword-and-shield-icon purzen}
	 */
	@Override
	public String getDefaultImage() {
		return "unit.png";
	}

	/**
	 * If we ignore ID, a fixture is equal iff it is an IUnit owned by the
	 * same player with the same kind and name and neither has any members
	 * that are not equal-ignoring-ID to any member of the other.
	 */
	@Override
	public boolean equalsIgnoringID(final IFixture fixture) {
		if (fixture instanceof IUnit that &&
				that.getOwner().getPlayerId() == owner.getPlayerId() &&
				that.getKind().equals(kind) &&
				that.getName().equals(name)) {
			for (final UnitMember member : this) {
				if (that.stream().noneMatch(member::equalsIgnoringID)) {
					return false;
				}
			}
			for (final UnitMember member : that) {
				if (members.stream().noneMatch(member::equalsIgnoringID)) {
					return false;
				}
			}
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Set orders for a turn.
	 */
	@Override
	public void setOrders(final int turn, final String newOrders) {
		orders.put(turn, newOrders);
	}

	/**
	 * Get orders for a turn.
	 */
	@Override
	public String getOrders(final int turn) {
		if (orders.containsKey(turn)) {
			return orders.get(turn);
		} else if (turn < 0 && orders.containsKey(-1)) {
			return orders.get(-1);
		} else {
			return "";
		}
	}

	/**
	 * Set results for a turn.
	 */
	@Override
	public void setResults(final int turn, final String newResults) {
		results.put(turn, newResults);
	}

	/**
	 * Get results for a turn.
	 */
	@Override
	public String getResults(final int turn) {
		if (results.containsKey(turn)) {
			return results.get(turn);
		} else if (turn < 0 && results.containsKey(-1)) {
			return results.get(-1);
		} else {
			return "";
		}
	}

	/**
	 * A short description of the fixture, giving its kind and owner but not its name.
	 */
	@Override
	public String getShortDescription() {
		if (owner.isCurrent()) {
			return String.format("a(n) %s unit belonging to you", kind);
		} else if (owner.isIndependent()) {
			return name + ", an independent unit";
		} else {
			return String.format("a(n) %s unit belonging to %s", kind, owner.getName());
		}
	}

	/**
	 * The required Perception check result for an explorer to notice the unit.
	 */
	@Override
	public int getDC() {
		return IntStream.concat(members.stream().filter(TileFixture.class::isInstance)
				.map(TileFixture.class::cast).mapToInt(TileFixture::getDC),
			IntStream.of(25 - members.size())).min().orElse(25 - members.size());
	}

	private static int memberComparison(final UnitMember one, final UnitMember two) {
		if (one instanceof IWorker first) {
			if (two instanceof IWorker second) {
				return first.getName().compareTo(second.getName());
			} else {
				return -1;
			}
		} else if (two instanceof IWorker) {
			return 1;
		} else if (one instanceof Immortal) {
			if (two instanceof Immortal) {
				return one.toString().compareTo(two.toString());
			} else {
				return -1;
			}
		} else if (two instanceof Immortal) {
			return 1;
		} else if (one instanceof Animal first) {
			if (two instanceof Animal second) {
				return Comparator.comparing(Animal::getKind)
					.thenComparing(Comparator.comparingInt(Animal::getPopulation)
						.reversed())
					.compare(first, second);
			} else {
				return -1;
			}
		} else if (two instanceof Animal) {
			return 1;
		} else if (one instanceof Implement first) {
			if (two instanceof Implement second) {
				return Comparator.comparing(Implement::getKind)
					.thenComparing(Comparator.comparingInt(Implement::getPopulation)
						.reversed())
					.compare(first, second);
			} else {
				return -1;
			}
		} else if (two instanceof Implement) {
			return 1;
		} else if (one instanceof IResourcePile first) {
			if (two instanceof IResourcePile second) {
				return Comparator.comparing(IResourcePile::getKind)
					.thenComparing(IResourcePile::getContents)
					.thenComparing(Comparator.comparing(IResourcePile::getQuantity)
						.reversed())
					.compare(first, second);
			} else {
				return -1;
			}
		} else if (two instanceof IResourcePile) {
			return 1;
		} else {
			LovelaceLogger.error("Unhandled unit-member in sorting");
			return one.toString().compareTo(two.toString());
		}
	}

	@Override
	public void sortMembers() {
		members.sort(Unit::memberComparison);
	}

	@Override
	public boolean isEmpty() {
		return members.isEmpty();
	}
}
