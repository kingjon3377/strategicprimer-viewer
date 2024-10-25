package legacy.map.fixtures.towns;

import common.map.fixtures.towns.TownSize;
import common.map.fixtures.towns.TownStatus;
import legacy.map.IFixture;
import legacy.map.HasMutableImage;
import legacy.map.Player;

import legacy.map.SubsettableFixture;

import java.util.Objects;

import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

/**
 * A village in the map.
 *
 * TODO: We'd like to be able to have {@link legacy.map.fixtures.mobile.Worker}
 * members (directly or in {@link CommunityStats}) to represent villagers that
 * players have been informed about by name.
 */
public class Village implements IMutableTownFixture, HasMutableImage,
		SubsettableFixture {
	public Village(final TownStatus status, final String name, final int id, final Player owner, final String race) {
		this.status = status;
		this.name = name;
		this.id = id;
		this.owner = owner;
		this.race = race;
	}

	/**
	 * The status of the village.
	 */
	private final TownStatus status;

	/**
	 * The status of the village.
	 */
	@Override
	public TownStatus getStatus() {
		return status;
	}

	/**
	 * The name of the village.
	 */
	private final String name;

	/**
	 * The name of the village.
	 */
	@Override
	public String getName() {
		return name;
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
	 * The player the village has pledged to serve and support, if any.
	 */
	private Player owner;

	/**
	 * The player the village has pledged to serve and support, if any.
	 */
	@Override
	public Player owner() {
		return owner;
	}

	/**
	 * Set the player the village has pledged to serve and support, if any.
	 */
	@Override
	public void setOwner(final Player owner) {
		this.owner = owner;
	}

	/**
	 * The dominant race of the village.
	 *
	 * TODO: Make a "copy-with-this-changed" method instead of making this
	 * mutable (or simply alter the few callers of the setter to do that
	 * manually)
	 */
	private String race;

	/**
	 * The dominant race of the village.
	 */
	public String getRace() {
		return race;
	}

	/**
	 * The dominant race of the village.
	 */
	public void setRace(final String race) {
		this.race = race;
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
	 * The default-icon filename.
	 */
	@Override
	public String getDefaultImage() {
		return "village.png";
	}

	/**
	 * A filename of an image to use as a portrait of the village.
	 */
	private String portrait = "";

	/**
	 * A filename of an image to use as a portrait of the village.
	 */
	@Override
	public String getPortrait() {
		return portrait;
	}

	/**
	 * A filename of an image to use as a portrait of the village.
	 */
	@Override
	public void setPortrait(final String portrait) {
		this.portrait = portrait;
	}

	/**
	 * The contents of the village.
	 */
	private @Nullable CommunityStats population = null;

	/**
	 * The contents of the village.
	 */
	@Override
	public @Nullable CommunityStats getPopulation() {
		return population;
	}

	/**
	 * The contents of the village.
	 */
	public void setPopulation(final @Nullable CommunityStats population) {
		this.population = population;
	}

	/**
	 * A short description of the village.
	 */
	@Override
	public String getShortDescription() {
		final StringBuilder builder = new StringBuilder();
		if (owner.isIndependent()) {
			builder.append("Independent ");
		}
		builder.append(status).append(" village");
		if (!name.isEmpty()) {
			builder.append(" named ").append(name);
		}
		if (owner.isCurrent()) {
			builder.append(", owned by you");
		} else if (!owner.isIndependent()) {
			builder.append(", owned by ").append(owner.getName());
		}
		return builder.toString();
	}

	@Override
	public String toString() {
		return getShortDescription();
	}

	/**
	 * An object is equal if it is a Village with the same status, ID, name, race, and owner.
	 */
	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof final Village it && status == it.getStatus() &&
				name.equals(it.getName()) && id == it.getId() &&
				owner.equals(it.owner()) &&
				race.equals(it.getRace())) {
			return Objects.equals(population, it.getPopulation());
		} else {
			return false;
		}
	}

	/**
	 * Use the ID number for hashing.
	 */
	@Override
	public int hashCode() {
		return id;
	}

	/**
	 * If we ignore ID, a fixture is equal iff it is a Village with the
	 * same status, owner, and race.
	 */
	@Override
	public boolean equalsIgnoringID(final IFixture fixture) {
		if (fixture instanceof final Village it && status == it.getStatus() &&
				name.equals(it.getName()) &&
				owner.equals(it.owner())) {
			return Objects.equals(population, it.getPopulation());
		} else {
			return false;
		}
	}

	/**
	 * All villages are small.
	 */
	@Override
	public TownSize getTownSize() {
		return TownSize.Small;
	}

	@Override
	public String getPlural() {
		return "Villages";
	}

	/**
	 * A village is a "subset" of another if they are identical, or if the
	 * only difference is that the "subset" is independent and the
	 * "superset" owes allegiance to some player. (Or if the "subset"
	 * village's population details are a "subset" of the other's.)
	 */
	@Override
	public boolean isSubset(final IFixture obj, final Consumer<String> report) {
		if (obj instanceof final Village it) {
			if (id != obj.getId()) {
				report.accept("IDs differ");
			} else if (status != it.getStatus()) {
				report.accept("In village (ID #%d):\tVillage status differs".formatted(id));
			} else if (!name.equals(it.getName())) {
				report.accept("In village (ID #%d):\tVillage name differs".formatted(id));
			} else if (!race.equals(it.getRace())) {
				report.accept("In village %s (ID #%d):\tDominant race differs".formatted(
						name, id));
			} else if (owner.getPlayerId() != it.owner().getPlayerId() &&
					!it.owner().isIndependent()) {
				report.accept("In village %s (ID #%d):\tOwners differ".formatted(
						name, id));
			} else if (!Objects.isNull(population)) {
				return population.isSubset(it.getPopulation(),
						(st) -> report.accept("In village %s (ID #%d):\t%s".formatted(
								name, id, st)));
			} else if (!Objects.isNull(it.getPopulation())) {
				report.accept("In village %s (ID #%d):\tHas extra population details".formatted(name, id));
			} else {
				return true;
			}
			return false;
		} else {
			report.accept("Incompatible type to Village");
			return false;
		}
	}

	@Override
	public String getKind() {
		return "village";
	}

	/**
	 * The required Perception check to find the village.
	 */
	@SuppressWarnings("MagicNumber")
	@Override
	public int getDC() {
		if (TownStatus.Active == status) {
			if (Objects.isNull(population)) {
				return 15;
			} else if (population.getPopulation() < 10) {
				return 20;
			} else if (population.getPopulation() < 15) {
				return 17;
			} else if (population.getPopulation() < 20) {
				return 15;
			} else if (population.getPopulation() < 50) {
				return 12;
			} else if (population.getPopulation() < 100) {
				return 10;
			} else {
				return 5;
			}
		} else {
			return 30;
		}
	}

	/**
	 * Clone the object.
	 */
	@Override
	public Village copy(final CopyBehavior zero) {
		final Village retval = new Village(status, name, id, owner, race);
		retval.setImage(image);
		if (zero == CopyBehavior.KEEP) {
			retval.setPortrait(portrait);
			retval.setPopulation(population);
		}
		return retval;
	}
}
