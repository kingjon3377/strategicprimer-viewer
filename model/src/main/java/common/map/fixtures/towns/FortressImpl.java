package common.map.fixtures.towns;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import common.map.IFixture;
import common.map.TileFixture;
import common.map.Player;
import common.map.fixtures.FortressMember;
import common.map.fixtures.mobile.IUnit;

import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * A fortress on the map. A player can only have one fortress per tile, but
 * multiple players may have fortresses on the same tile.
 *
 * FIXME: We need something about buildings yet
 */
public class FortressImpl implements IMutableFortress {
	public FortressImpl(final Player owner, final String name, final int id, final TownSize townSize) {
		this.owner = owner;
		this.name = name;
		this.id = id;
		this.townSize = townSize;
	}

	public FortressImpl(final Player owner, final String name, final int id) {
		this(owner, name, id, TownSize.Small);
	}

	/**
	 * The player who owns the fortress.
	 */
	private Player owner;

	/**
	 * The player who owns the fortress.
	 */
	@Override
	public Player getOwner() {
		return owner;
	}

	/**
	 * Set the player who owns the fortress.
	 */
	@Override
	public void setOwner(final Player owner) {
		this.owner = owner;
	}

	/**
	 * The name of the fortress.
	 */
	private String name;

	/**
	 * The name of the fortress.
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * Set the name of the fortress.
	 */
	@Override
	public void setName(final String name) {
		this.name = name;
	}

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
	 * The size of the fortress.
	 */
	private final TownSize townSize;

	/**
	 * The size of the fortress.
	 *
	 * TODO: Rename back to 'size', as it was in Java before porting to
	 * Ceylon where 'size' is a member of Iterable?
	 */
	@Override
	public TownSize getTownSize() {
		return townSize;
	}

	/**
	 * The members of the fortress.
	 *
	 * TODO: Should this perhaps be a Set?
	 */
	private final List<FortressMember> members = new ArrayList<>();

	/**
	 * A Fortress's contents aren't handled like those of other towns.
	 *
	 * TODO: OTOH, should they be?
	 */
	@Override
	@Nullable
	public CommunityStats getPopulation() {
		return null;
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
	 * The name of an image to use as a portrait.
	 */
	private String portrait = "";

	/**
	 * The name of an image to use as a portrait.
	 */
	@Override
	public String getPortrait() {
		return portrait;
	}

	/**
	 * Set the name of an image to use as a portrait.
	 */
	@Override
	public void setPortrait(final String portrait) {
		this.portrait = portrait;
	}

	/**
	 * Add a member to the fortress.
	 */
	@Override
	public void addMember(final FortressMember member) {
		members.add(member);
	}

	/**
	 * Remove a member from the fortress.
	 */
	@Override
	public void removeMember(final FortressMember member) {
		members.remove(member);
	}

	/**
	 * Clone the fortress.
	 */
	@Override
	public IMutableFortress copy(final boolean zero) {
		final IMutableFortress retval;
		if (zero) {
			retval = new FortressImpl(owner, "unknown", id, townSize);
		} else {
			retval = new FortressImpl(owner, name, id, townSize);
			for (FortressMember member : members) {
				retval.addMember(member.copy(false));
			}
		}
		retval.setImage(image);
		return retval;
	}

	/**
	 * An iterator over the members of the fortress.
	 */
	@Override
	public Iterator<FortressMember> iterator() {
		return members.iterator();
	}

	@Override
	public Stream<FortressMember> stream() {
		return members.stream();
	}

	@Override
	public boolean equalsIgnoringID(final IFixture fixture) {
		if (fixture instanceof IFortress) {
			Set<FortressMember> theirs =
				((IFortress) fixture).stream().collect(Collectors.toSet());
			return name.equals(((IFortress) fixture).getName()) &&
				owner.getPlayerId() == ((IFortress) fixture).getOwner().getPlayerId() &&
				members.containsAll(theirs) && theirs.containsAll(members);
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("Fortress ").append(name).append(", owned by player ")
			.append(owner.toString()).append(". Members:");
		int count = 0;
		final int size = members.size();
		for (FortressMember member : members) {
			builder.append(System.lineSeparator());
			builder.append("\t\t\t");
			if (member instanceof IUnit) {
				builder.append(((IUnit) member).getName());
				if (((IUnit) member).getOwner().equals(owner)) {
					builder.append(" (").append(((IUnit) member).getKind()).append(")");
				} else if (((IUnit) member).getOwner().isIndependent()) {
					builder.append(", an independent ")
						.append(((IUnit) member).getKind());
				} else {
					builder.append(" (").append(((IUnit) member).getKind())
						.append("), belonging to ")
						.append(((IUnit) member).getOwner());
				}
			} else {
				builder.append(member.toString());
			}
			count++;
			if (count < size - 1) {
				builder.append(";");
			}
		}
		return builder.toString();
	}

	/**
	 * A fixture is a subset if it is a Fortress with the same ID, owner,
	 * and name (or it has the name "unknown") and every member it has is
	 * equal to, or a subset of, one of our members.
	 */
	@Override
	public boolean isSubset(final IFixture obj, final Consumer<String> report) {
		if (!(obj instanceof IFortress)) {
			report.accept("Incompatible type to Fortress");
			return false;
		}
		final IFortress fort = (IFortress) obj;
		if (fort.getId() != id) {
			report.accept("ID mismatch between Fortresses");
			return false;
		}
		if (!townSize.equals(fort.getTownSize())) {
			report.accept("Size mismatch between Fortresses");
			return false;
		}
		if ((name.equals(fort.getName()) || "unknown".equals(fort.getName())) &&
				fort.getOwner().getPlayerId() == owner.getPlayerId()) {
			Map<Integer, FortressMember> ours = members.stream()
				.collect(Collectors.toMap(FortressMember::getId, m -> m));
			boolean retval = true;
			Consumer<String> localFormat = s -> report.accept(String.format(
				"In fortress %s (ID #%d):\t%s", name, id, s));
			for (FortressMember member : fort) {
				if (ours.containsKey(member.getId())) {
					if (!ours.get(member.getId()).isSubset(member, localFormat)) {
						retval = false;
					}
				} else {
					localFormat.accept(String.format("Extra member:\t%s, ID #%d",
						member.toString(), member.getId()));
					retval = false;
				}
			}
			return retval;
		} else {
			report.accept(String.format("In fortress (ID #%d): Names don't match", id));
			return false;
		}
	}

	/**
	 * The required Perception check for an explorer to find the fortress.
	 */
	@Override
	public int getDC() {
		return members.stream().filter(TileFixture.class::isInstance).map(TileFixture.class::cast)
				.mapToInt(TileFixture::getDC).min().orElse(20) - members.size() -
			townSize.ordinal() * 2;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof IFortress) {
			return equalsIgnoringID((IFortress) obj) && id == ((IFortress) obj).getId();
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return id;
	}
}
