package common.map.fixtures.mobile;

import java.util.Comparator;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import lovelace.util.ArraySet;
import common.map.IFixture;
import common.map.TileFixture;
import common.map.Player;
import common.map.fixtures.Implement;
import common.map.fixtures.IResourcePile;
import common.map.fixtures.Quantity;
import common.map.fixtures.UnitMember;
import common.map.fixtures.mobile.ProxyFor;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A unit in the map.
 *
 * FIXME: we need more members: something about stats; what else?
 */
public final class Unit implements IMutableUnit {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(Unit.class.getName());

	public Unit(Player owner, String kind, String name, int id) {
		this.owner = owner;
		this.kind = kind;
		this.name = name;
		this.id = id;
	}

	/**
	 * The unit's orders. This is serialized to and from XML, but does not
	 * affect equality or hashing, and is not printed in {@link toString}.
	 */
	private final SortedMap<Integer, String> orders = new TreeMap<>();

	/**
	 * The unit's results. This is serialized to and from XML, but does not
	 * affect equality or hashing, and is not printed in {@link toString}.
	 */
	private final SortedMap<Integer, String> results = new TreeMap<>();

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
	public void setImage(String image) {
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
	public void setOwner(Player owner) {
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
	public void setKind(String kind) {
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
	public void setName(String name) {
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
	public void setPortrait(String portrait) {
		this.portrait = portrait;
	}

	/**
	 * The unit's orders for all turns.
	 */
	@Override
	public SortedMap<Integer, String> getAllOrders() {
		return Collections.unmodifiableSortedMap(orders);
	}

	/**
	 * The unit's results for all turns.
	 */
	@Override
	public SortedMap<Integer, String> getAllResults() {
		return Collections.unmodifiableSortedMap(results);
	}

	/**
	 * Clone the unit.
	 *
	 * TODO: There should be some way to convey the unit's *size* without
	 * the *details* of its contents. Or maybe we should give the contents
	 * but not *their* details?
	 */
	@Override
	public Unit copy(boolean zero) {
		Unit retval = new Unit(owner, kind, name, id);
		if (!zero) {
			retval.orders.putAll(orders);
			retval.results.putAll(results);
			for (UnitMember member : members) {
				retval.addMember(member.copy(false));
			}
		}
		retval.setImage(image);
		return retval;
	}

	/**
	 * Add a member.
	 */
	@Override
	public void addMember(UnitMember member) {
		if (member instanceof ProxyFor) {
			LOGGER.log(Level.SEVERE, "ProxyWorker added to Unit",
				new IllegalStateException("ProxyWorker added to Unit"));
		}
		members.add(member);
	}

	/**
	 * Remove a member.
	 */
	@Override
	public void removeMember(UnitMember member) {
		members.remove(member);
	}

	/**
	 * An iterator over the unit's members.
	 */
	@Override
	public Iterator<UnitMember> iterator() {
		return members.iterator();
	}

	/**
	 * An object is equal iff it is a IUnit owned by the same player, with
	 * the same kind, ID, and name, and with equal members.
	 *
	 * FIXME: Add stream() method to our interfaces that extend Iterable, to replace StreamSupport calls.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IUnit) {
			return ((IUnit) obj).getOwner().getPlayerId() == owner.getPlayerId() &&
				kind.equals(((IUnit) obj).getKind()) &&
				name.equals(((IUnit) obj).getName()) &&
				((IUnit) obj).getId() == id &&
				members.containsAll(StreamSupport.stream(((IUnit) obj).spliterator(), true)
					.collect(Collectors.toList())) &&
				StreamSupport.stream(((IUnit) obj).spliterator(), true)
					.collect(Collectors.toList()).containsAll(members);
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
				kind, owner.toString(), name);
		}
	}

	@Override
	public String getVerbose() {
		return String.format("%s, consisting of:%n%s", toString(),
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
	public boolean equalsIgnoringID(IFixture fixture) {
		if (fixture instanceof IUnit &&
				((IUnit) fixture).getOwner().getPlayerId() == owner.getPlayerId() &&
				((IUnit) fixture).getKind().equals(kind) &&
				((IUnit) fixture).getName().equals(name)) {
			for (UnitMember member : this) {
				if (!StreamSupport.stream(((IUnit) fixture).spliterator(), true)
						.anyMatch(member::equalsIgnoringID)) {
					return false;
				}
			}
			for (UnitMember member : (IUnit) fixture) {
				if (!members.stream().anyMatch(member::equalsIgnoringID)) {
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
	public void setOrders(int turn, String newOrders) {
		orders.put(turn, newOrders);
	}

	/**
	 * Get orders for a turn.
	 */
	@Override
	public String getOrders(int turn) {
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
	public void setResults(int turn, String newResults) {
		results.put(turn, newResults);
	}

	/**
	 * Get results for a turn.
	 */
	@Override
	public String getResults(int turn) {
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

	private static int memberComparison(UnitMember one, UnitMember two) {
		if (one instanceof IWorker) {
			if (two instanceof IWorker) {
				return ((IWorker) one).getName().compareTo(((IWorker) two).getName());
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
		} else if (one instanceof Animal) {
			if (two instanceof Animal) {
				return Comparator.comparing(Animal::getKind)
					.thenComparing(Comparator.comparingInt(Animal::getPopulation)
						.reversed())
					.compare((Animal) one, (Animal) two);
			} else {
				return -1;
			}
		} else if (two instanceof Animal) {
			return 1;
		} else if (one instanceof Implement) {
			if (two instanceof Implement) {
				return Comparator.comparing(Implement::getKind)
					.thenComparing(Comparator.comparingInt(Implement::getPopulation)
						.reversed())
					.compare((Implement) one, (Implement) two);
			} else {
				return -1;
			}
		} else if (two instanceof Implement) {
			return 1;
		} else if (one instanceof IResourcePile) {
			if (two instanceof IResourcePile) {
				return Comparator.comparing(IResourcePile::getKind)
					.thenComparing(IResourcePile::getContents)
					.thenComparing(Comparator.comparing(IResourcePile::getQuantity)
						.reversed())
					.compare((IResourcePile) one, (IResourcePile) two);
			} else {
				return -1;
			}
		} else if (two instanceof IResourcePile) {
			return 1;
		} else {
			LOGGER.severe("Unhandled unit-member in sorting");
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
