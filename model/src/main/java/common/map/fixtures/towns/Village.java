package common.map.fixtures.towns;

import common.map.IFixture;
import common.map.HasMutableImage;
import common.map.Subsettable;
import common.map.Player;

import java.util.Objects;

import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

/**
 * A village in the map.
 *
 * TODO: We'd like to be able to have {@link common.map.fixtures.mobile.Worker}
 * members (directly or in {@link CommunityStats}) to represent villagers that
 * players have been informed about by name.
 */
public class Village implements IMutableTownFixture, HasMutableImage, IFixture,
		Subsettable<IFixture> {
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
	public Player getOwner() {
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
	@Nullable
	private CommunityStats population = null;

	/**
	 * The contents of the village.
	 */
	@Override
	@Nullable
	public CommunityStats getPopulation() {
		return population;
	}

	/**
	 * The contents of the village.
	 */
	public void setPopulation(@Nullable final CommunityStats population) {
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
		builder.append(status.toString()).append(" village");
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
		if (obj instanceof Village && status == ((Village) obj).getStatus() &&
				name.equals(((Village) obj).getName()) && id == ((Village) obj).getId() &&
				owner.equals(((Village) obj).getOwner()) &&
				race.equals(((Village) obj).getRace())) {
			return Objects.equals(population, ((Village) obj).getPopulation());
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
		if (fixture instanceof Village && status == ((Village) fixture).getStatus() &&
				name.equals(((Village) fixture).getName()) &&
				owner.equals(((Village) fixture).getOwner())) {
			return Objects.equals(population, ((Village) fixture).getPopulation());
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
		if (obj instanceof Village) {
			if (id != ((Village) obj).getId()) {
				report.accept("IDs differ");
			} else if (status != ((Village) obj).getStatus()) {
				report.accept(String.format("In village (ID #%d):\tVillage status differs",
					id));
			} else if (name.equals(((Village) obj).getName())) {
				report.accept(String.format("In village (ID #%d):\tVillage name differs",
					id));
			} else if (race.equals(((Village) obj).getRace())) {
				report.accept(String.format("In village %s (ID #%d):\tDominant race differs",
					name, id));
			} else if (owner.getPlayerId() != ((Village) obj).getOwner().getPlayerId() &&
					!((Village) obj).getOwner().isIndependent()) {
				report.accept(String.format("In village %s (ID #%d):\tOwners differ",
					name, id));
			} else if (population != null) {
				return population.isSubset(((Village) obj).getPopulation(),
					(st) -> report.accept(String.format("In village %s (ID #%d):\t%s",
						name, id, st)));
			} else if (((Village) obj).getPopulation() != null) {
				report.accept(String.format(
					"In village %s (ID #%d):\tHas extra population details", name, id));
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
	@Override
	public int getDC() {
		if (TownStatus.Active == status) {
			if (population == null) {
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
	public Village copy(final boolean zero) {
		final Village retval = new Village(status, name, id, owner, race);
		retval.setImage(image);
		if (!zero) {
			retval.setPortrait(portrait);
			retval.setPopulation(population);
		}
		return retval;
	}
}
