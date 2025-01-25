package legacy.map.fixtures.resources;

import legacy.map.IFixture;
import legacy.map.HasPopulation;

import java.text.ParseException;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * An orchard (fruit trees) or grove (other trees) on the map.
 */
public final class Grove implements HarvestableFixture, HasPopulation<Grove> {
	public enum GroveType {
		/**
		 * Non-fruit trees.
		 */
		GROVE,
		/**
		 * Fruit trees.
		 */
		ORCHARD;

		@Override
		public String toString() {
			return name().toLowerCase();
		}

		public static GroveType parse(final String str) throws ParseException {
			return Stream.of(values()).filter(v -> v.toString().equals(str)).findAny()
					.orElseThrow(() -> new ParseException("Unexpected grove type %s".formatted(str), 0));
		}
	}
	public Grove(final GroveType type, final CultivationStatus cultivation, final String kind, final int id,
				 final int population) {
		this.type = type;
		this.cultivation = cultivation;
		this.kind = kind;
		this.id = id;
		this.population = population;
	}

	public Grove(final GroveType type, final CultivationStatus cultivation, final String kind, final int id) {
		this(type, cultivation, kind, id, -1);
	}

	/**
	 * What (general) type of trees are here.
	 */
	private final GroveType type;

	/**
	 * If true, this is a fruit orchard; if false, a non-fruit grove.
	 */
	public GroveType getType() {
		return type;
	}

	/**
	 * If true, this is a cultivated grove or orchard; if false, wild or abandoned.
	 */
	private final CultivationStatus cultivation;

	/**
	 * If true, this is a cultivated grove or orchard; if false, wild or abandoned.
	 */
	public CultivationStatus getCultivation() {
		return cultivation;
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
		final Grove retval = new Grove(type, cultivation, kind, id,
				(zero == CopyBehavior.ZERO) ? -1 : population);
		retval.setImage(image);
		return retval;
	}

	@Override
	public Grove reduced(final int newPopulation, final int newId) {
		return new Grove(type, cultivation, kind, newId, newPopulation);
	}

	@Override
	public Grove combined(final Grove addend) {
		return new Grove(type, cultivation, kind, id,
				Math.max(population, 0) + Math.max(addend.getPopulation(), 0));
	}

	@Override
	public String getDefaultImage() {
		return switch (type) {
			case GROVE -> "tree.png";
			case ORCHARD -> "orchard.png";
		};
	}

	@Override
	public String getShortDescription() {
		final String type = this.type.name().toLowerCase();
		final String cultivation = this.cultivation.capitalized();
		if (population < 0) {
			return "%s %s %s".formatted(cultivation, kind, type);
		} else {
			return "%s %s %s of %d trees".formatted(cultivation, kind, type, population);
		}
	}

	@Override
	public String toString() {
		return getShortDescription();
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof final Grove that) {
			return kind.equals(that.getKind()) &&
					type == that.getType() &&
					cultivation == that.getCultivation() &&
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
		if (fixture instanceof final Grove that) {
			return kind.equals(that.getKind()) &&
					type == that.getType() &&
					cultivation == that.getCultivation() &&
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
		} else if (other instanceof final Grove it) {
			boolean retval = true;
			final Consumer<String> localReport = s -> report.accept("In %s with ID #%d:\t%s".formatted(type, id, s));
			if (!kind.equals(it.getKind())) {
				localReport.accept("Kinds differ");
				retval = false;
			}
			if (type != it.getType()) {
				localReport.accept("Grove vs. orchard differs");
				retval = false;
			}
			if (cultivation != it.getCultivation()) { // TODO: Should non-cultivated be a subset of cultivated?
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

	@SuppressWarnings("MagicNumber")
	@Override
	public int getDC() {
		return 18;
	}
}
