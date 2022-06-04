package common.map.fixtures.towns;

import common.map.IFixture;
import common.map.Subsettable;
import common.map.HasMutableImage;
import common.map.Player;

import java.util.Objects;

import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

/**
 * An abstract superclass for towns etc.
 *
 * // FIXME: Probably Needs to implement SubsettableFixture instead of {@code Subsettable<AbstractTown>}
 */
public abstract class AbstractTown implements HasMutableImage,
		IMutableTownFixture, Subsettable<AbstractTown> {
	// TODO: Should probably take ID as a constructor parameter too?
	protected AbstractTown(final TownStatus status, final TownSize townSize, final String name, final Player owner, final int dc) {
		this.status = status;
		this.townSize = townSize;
		this.name = name;
		this.owner = owner;
		this.dc = dc;
	}

	/**
	 * The status of the town, fortification, or city
	 */
	private final TownStatus status;

	/**
	 * The status of the town, fortification, or city
	 */
	@Override
	public final TownStatus getStatus() {
		return status;
	}

	/**
	 * The size of the town, fortification, or city
	 */
	private final TownSize townSize;

	/**
	 * The size of the town, fortification, or city
	 */
	@Override
	public final TownSize getTownSize() {
		return townSize;
	}

	/**
	 * The name of the town, fortification, or city
	 */
	private final String name;

	/**
	 * The name of the town, fortification, or city
	 */
	@Override
	public final String getName() {
		return name;
	}

	/**
	 * The player that owns the town, fortification, or city
	 */
	private Player owner;

	/**
	 * The player that owns the town, fortification, or city
	 */
	@Override
	public final Player owner() {
		return owner;
	}

	/**
	 * The player that owns the town, fortification, or city
	 */
	@Override
	public final void setOwner(final Player owner) {
		this.owner = owner;
	}

	/**
	 * The DC to discover the town, fortification, or city
	 *
	 * TODO: Provide reasonable default, depending on {@link #population}
	 * members as well as {@link #status} and {@link #townSize}")
	 */
	private final int dc;

	/**
	 * The DC to discover the town, fortification, or city
	 *
	 * TODO: Provide reasonable default, depending on {@link #population}
	 * members as well as {@link #status} and {@link #townSize}")
	 */
	@Override
	public final int getDC() {
		return dc;
	}

	/**
	 * The filename of an image to use as an icon for this instance.
	 */
	private String image = "";

	/**
	 * The filename of an image to use as an icon for this instance.
	 */
	@Override
	public final String getImage() {
		return image;
	}

	/**
	 * Set the filename of an image to use as an icon for this instance.
	 */
	@Override
	public final void setImage(final String image) {
		this.image = image;
	}

	/**
	 * A filename of an image to use as a portrait.
	 */
	private String portrait = "";

	/**
	 * A filename of an image to use as a portrait.
	 */
	@Override
	public final String getPortrait() {
		return portrait;
	}

	/**
	 * A filename of an image to use as a portrait.
	 */
	@Override
	public final void setPortrait(final String portrait) {
		this.portrait = portrait;
	}

	/**
	 * The contents of the town.
	 */
	private @Nullable CommunityStats population = null;

	/**
	 * The contents of the town.
	 */
	@Override
	public final @Nullable CommunityStats getPopulation() {
		return population;
	}

	/**
	 * The contents of the town.
	 */
	public final void setPopulation(final @Nullable CommunityStats population) {
		this.population = population;
	}

	@Override
	public final boolean isSubset(final AbstractTown other, final Consumer<String> report) {
		if (getId() != other.getId()) {
			report.accept("Fixtures' ID #s differ");
			return false;
		} else if (!name.equals(other.getName()) && !"unknown".equals(other.getName())) {
			report.accept("Town name differs");
			return false;
		} else if (!getKind().equals(other.getKind())) {
			report.accept(String.format("In %s, ID #%d:\tTown kind differs", name, getId()));
			return false;
		}
		final Consumer<String> localReport = s -> report.accept(String.format(
			"In %s %s, ID #%d:\t%s", getKind(), name, getId(), s));
		boolean retval = true;
		if (status != other.getStatus()) {
			localReport.accept("Town status differs");
			retval = false;
		}
		if (townSize != other.getTownSize()) {
			localReport.accept("Town size differs");
			retval = false;
		}
		if (other.getPopulation() != null && population == null) {
			localReport.accept("Has contents details we don't");
			retval = false;
		}
		if (population != null && !population.isSubset(other.getPopulation(), localReport)) {
			// TODO: Don't really need to report after
			// passinglocalReport to the population isSubset call, right?
			localReport.accept("Has different population details");
			retval = false;
		}
		if (!owner.equals(other.owner()) && !other.owner().isIndependent()) {
			localReport.accept("Has different owner");
			retval = false;
		}
		return retval;
	}

	/**
	 * A helper method for equals() that checks everything except the type of the object.
	 */
	protected final boolean equalsContents(final AbstractTown fixture) {
		return fixture.getTownSize() == townSize &&
			fixture.getName().equals(name) && fixture.getStatus() == status &&
			fixture.owner().equals(owner) &&
			Objects.equals(population, fixture.getPopulation());
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof AbstractTown it) {
			return getId() == it.getId() && equalsContents(it);
		} else {
			return false;
		}
	}

	// TODO: Make this final? Or make this final and merge it with equalsContents()?
	@Override
	public boolean equalsIgnoringID(final IFixture fixture) {
		if (fixture instanceof AbstractTown it) {
			return equalsContents(it);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return getId();
	}

	@Override
	public String toString() {
		if (owner.isIndependent()) {
			// TODO: Split into if/else rather than having the conditional inside format() call
			return String.format("An independent %s %s %s of DC %s %s",
					townSize, status, getKind(), dc,
				name.isEmpty() ? "with no name" : "named " + name);
		} else {
			// TODO: Split into if/else rather than having the conditional inside format() call
			return String.format("A %s %s %s of DC %d %s, owned by %s",
					townSize, status, getKind(), dc,
				name.isEmpty() ? "with no name" : "named " + name,
				owner.isCurrent() ? "you" : owner.getName());
		}
	}

	@Override
	public String getShortDescription() {
		if (owner.isIndependent()) {
			// TODO: Split into if/else rather than having the conditional inside format() call
			return String.format("An independent %s %s %s %s", townSize,
					status, getKind(),
				name.isEmpty() ? "with no name" : "named " + name);
		} else {
			// TODO: Split into if/else rather than having the conditional inside format() call
			return String.format("A %s %s %s %s, owned by %s", townSize,
					status, getKind(),
				name.isEmpty() ? "with no name" : "named " + name,
				owner.isCurrent() ? "you" : owner.getName());
		}
	}
}
