package common.map.fixtures.resources;

import common.map.IFixture;
import common.map.HasPopulation;

import java.util.function.Consumer;

/**
 * An orchard (fruit trees) or grove (other trees) on the map.
 *
 * TODO: Convert Boolean fields to enums.
 */
public class Grove implements HarvestableFixture, HasPopulation<Grove> {
	public Grove(final boolean orchard, final boolean cultivated, final String kind, final int id, final int population) {
		this.orchard = orchard;
		this.cultivated = cultivated;
		this.kind = kind;
		this.id = id;
		this.population = population;
	}

	public Grove(final boolean orchard, final boolean cultivated, final String kind, final int id) {
		this(orchard, cultivated, kind, id, -1);
	}

	/**
	 * If true, this is a fruit orchard; if false, a non-fruit grove.
	 */
	private final boolean orchard;

	/**
	 * If true, this is a fruit orchard; if false, a non-fruit grove.
	 */
	public boolean isOrchard() {
		return orchard;
	}

	/**
	 * If true, this is a cultivated grove or orchard; if false, wild or abandoned.
	 */
	private final boolean cultivated;

	/**
	 * If true, this is a cultivated grove or orchard; if false, wild or abandoned.
	 */
	public boolean isCultivated() {
		return cultivated;
	}

	/**
	 * What kind of tree is in this orchard or grove.
	 */
	private final String kind;

	/**
	 * What kind of tree is in this orchard or grove.
	 */
	@Override
	public String getKind() {
		return kind;
	}

	/**
	 * An ID number to identify this orchard or grove.
	 */
	private final int id;

	/**
	 * An ID number to identify this orchard or grove.
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
	 * How many individual trees are in this grove or orchard.
	 */
	private final int population;

	/**
	 * How many individual trees are in this grove or orchard.
	 */
	@Override
	public int getPopulation() {
		return population;
	}

	@Override
	public Grove copy(final CopyBehavior zero) {
		final Grove retval = new Grove(orchard, cultivated, kind, id,
			(zero == CopyBehavior.ZERO) ? -1 : population);
		retval.setImage(image);
		return retval;
	}

	@Override
	public Grove reduced(final int newPopulation, final int newId) {
		return new Grove(orchard, cultivated, kind, newId, newPopulation);
	}

	@Override
	public Grove combined(final Grove addend) {
		return new Grove(orchard, cultivated, kind, id,
			Math.max(population, 0) + Math.max(addend.getPopulation(), 0));
	}

	@Override
	public String getDefaultImage() {
		return (orchard) ? "orchard.png" : "tree.png";
	}

	@Override
	public String getShortDescription() {
		final String type;
		final String cultivation;
		String retval;
		if (cultivated) {
			cultivation = "Cultivated";
		} else {
			cultivation = "Wild";
		}
		if (orchard) {
			type = "orchard";
		} else {
			type = "grove";
		}
		if (population < 0) {
			return String.format("%s %s %s", cultivation, kind, type);
		} else {
			return String.format("%s %s %s of %d trees", cultivation, kind, type, population);
		}
	}

	@Override
	public String toString() {
		return getShortDescription();
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof Grove that) {
			return kind.equals(that.getKind()) &&
				orchard == that.isOrchard() &&
				cultivated == that.isCultivated() &&
				id == that.getId() &&
				population == that.getPopulation();
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public boolean equalsIgnoringID(final IFixture fixture) {
		if (fixture instanceof Grove that) {
			return kind.equals(that.getKind()) &&
				orchard == that.isOrchard() &&
				cultivated == that.isCultivated() &&
				population == that.getPopulation();
		} else {
			return false;
		}
	}

	@Override
	public boolean isSubset(final IFixture other, final Consumer<String> report) {
		if (other.getId() != id) {
			report.accept("Different IDs");
			return false;
		} else if (other instanceof Grove it) {
			boolean retval = true;
			final Consumer<String> localReport;
			if (orchard) {
				localReport = s -> report.accept(String.format(
					"In orchard with ID #%d:\t%s", id, s));
			} else {
				localReport = s -> report.accept(String.format(
					"In grove with ID #%d:\t%s", id, s));
			}
			if (!kind.equals(it.getKind())) {
				localReport.accept("Kinds differ");
				retval = false;
			}
			if (orchard != it.isOrchard()) {
				localReport.accept("Grove vs. orchard differs");
				retval = false;
			}
			if (cultivated != it.isCultivated()) { // TODO: Should a non-cultivated one be a subset of a cultivated one?
				localReport.accept("Cultivation status differs");
				retval = false;
			}
			if (population < it.getPopulation()) {
				localReport.accept("Has larger number of trees than we do");
				retval = false;
			}
			return retval;
		} else {
			report.accept("Different types for ID #" + id);
			return false;
		}
	}

	@Override
	public String getPlural() {
		return "Groves and orchards";
	}

	@Override
	public int getDC() {
		return 18;
	}
}
