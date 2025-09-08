package legacy.map.fixtures.mobile;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Collections;
import java.util.Iterator;
import java.util.NavigableMap;
import java.util.OptionalInt;
import java.util.TreeMap;

import lovelace.util.ArraySet;
import legacy.map.IFixture;
import legacy.map.TileFixture;
import legacy.map.Player;
import legacy.map.fixtures.Implement;
import legacy.map.fixtures.IResourcePile;
import legacy.map.fixtures.UnitMember;

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
	 * affect equality or hashing, and is not printed in {@link #toString}.
	 */
	private final NavigableMap<Integer, String> orders = new TreeMap<>();

	/**
	 * The unit's results. This is serialized to and from XML, but does not
	 * affect equality or hashing, and is not printed in {@link #toString}.
	 */
	private final NavigableMap<Integer, String> results = new TreeMap<>();

	/**
	 * The members of the unit.
	 *
	 * We'd prefer to use LinkedHashSet, but we need the sort() operation.
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
	public Player owner() {
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
		if (obj instanceof final IUnit that) {
			return that.owner().getPlayerId() == owner.getPlayerId() &&
					kind.equals(that.getKind()) &&
					name.equals(that.getName()) &&
					that.getId() == id &&
					members.containsAll(that.stream().toList()) &&
					that.stream().collect(Collectors.toSet()).containsAll(members);
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
			return "Independent unit of type %s, named %s".formatted(kind, name);
		} else {
			return "Unit of type %s, belonging to %s, named %s".formatted(
					kind, owner, name);
		}
	}

	@Override
	public String getVerbose() {
		return "%s, consisting of:%n%s".formatted(this,
				String.join(System.lineSeparator(),
						members.stream().map(Object::toString).toArray(String[]::new)));
	}

	/**
	 * An icon to represent units by default.
	 *
	 * @author <a href="https://openclipart.org/detail/28731/sword-and-shield-icon">purzen</a>
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
		if (fixture instanceof final IUnit that &&
				that.owner().getPlayerId() == owner.getPlayerId() &&
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
			return "a(n) %s unit belonging to you".formatted(kind);
		} else if (owner.isIndependent()) {
			return name + ", an independent unit";
		} else {
			return "a(n) %s unit belonging to %s".formatted(kind, owner.getName());
		}
	}

	/**
	 * The required Perception check result for an explorer to notice the unit.
	 */
	@SuppressWarnings("MagicNumber")
	@Override
	public int getDC() {
		return IntStream.concat(members.stream().filter(TileFixture.class::isInstance)
						.map(TileFixture.class::cast).mapToInt(TileFixture::getDC),
				IntStream.of(25 - members.size())).min().orElse(25 - members.size());
	}

	// FIXME: Extract this to a more general place, probably lovelace-util
	private record TestingComparator<T>(Class<T> cls, Comparator<? super T> wrapped)
			implements Comparator<T>, Serializable {
		@SuppressWarnings("QuestionableName")
		@Override
		public int compare(final T one, final T two) {
			return wrapped.compare(one, two);
		}

		@SuppressWarnings("QuestionableName")
		public OptionalInt maybeCompare(final Object one, final Object two) {
			if (cls.isInstance(one) && cls.isInstance(two)) {
				return OptionalInt.of(compare(cls.cast(one), cls.cast(two)));
			} else {
				return OptionalInt.empty();
			}
		}
	}

	@SuppressWarnings({"MethodWithMultipleReturnPoints", "QuestionableName"})
	private static OptionalInt simpleComparison(final Object one, final Object two,
	                                            final TestingComparator<?>... comparators) {
		for (final TestingComparator<?> comparator : comparators) {
			final OptionalInt result = comparator.maybeCompare(one, two);
			if (result.isPresent()) {
				return result;
			} else if (comparator.cls().isInstance(one)) {
				return OptionalInt.of(-1);
			} else if (comparator.cls().isInstance(two)) {
				return OptionalInt.of(1);
			}
		}
		return OptionalInt.empty();
	}

	@SuppressWarnings("QuestionableName")
	private static int memberComparison(final UnitMember one, final UnitMember two) {
		final OptionalInt initialComparison = simpleComparison(one, two,
				new TestingComparator<>(IWorker.class, Comparator.comparing(IWorker::getName)),
				new TestingComparator<>(Immortal.class, Comparator.comparing(Object::toString)),
				new TestingComparator<>(Animal.class, Comparator.comparing(Animal::getKind)
						.thenComparing(Comparator.comparingInt(Animal::getPopulation).reversed())),
				new TestingComparator<>(Implement.class, Comparator.comparing(Implement::getKind)
						.thenComparing(Comparator.comparingInt(Implement::getPopulation).reversed())),
				new TestingComparator<>(IResourcePile.class, Comparator.comparing(IResourcePile::getKind)
						.thenComparing(IResourcePile::getContents)
						.thenComparing(Comparator.comparing(IResourcePile::getQuantity).reversed())));
		if (initialComparison.isPresent()) {
			return initialComparison.getAsInt();
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
