package legacy.map.fixtures.resources;

import common.map.fixtures.resources.FieldStatus;
import lovelace.util.EnumParser;
import lovelace.util.NumberComparator;
import legacy.map.IFixture;
import legacy.map.HasExtent;
import lovelace.util.ThrowingFunction;

import java.util.function.Consumer;

/**
 * A field or meadow. If in forest, should increase a unit's vision slightly when the unit is on it.
 *
 * TODO: Implement that effect
 */
public final class Meadow implements HarvestableFixture, HasExtent<Meadow> {
	private static final double BASE_DC = 20;
	private static final double DC_MOD_PER_ACRE = 3.0 / 40.0;
	public enum MeadowType {
		/**
		 * Grain
		 */
		FIELD,
		/**
		 * Grasses/pasture/forage
		 */
		MEADOW;

		@Override
		public String toString() {
			return name().toLowerCase();
		}

		private static final ThrowingFunction<String, MeadowType, IllegalArgumentException> PARSER =
				new EnumParser<>(MeadowType.class, values().length);
		public static MeadowType parse(final String str) throws IllegalArgumentException {
			return PARSER.apply(str);
		}

	}

	public Meadow(final String kind, final MeadowType type, final CultivationStatus cultivation, final int id,
	              final FieldStatus status, final Number acres) {
		this.kind = kind;
		this.type = type;
		this.cultivation = cultivation;
		this.id = id;
		this.status = status;
		this.acres = acres;
	}

	public Meadow(final String kind, final MeadowType type, final CultivationStatus cultivation, final int id,
				  final FieldStatus status) {
		this(kind, type, cultivation, id, status, -1);
	}

	/**
	 * The kind of grain or grass growing in this field or meadow.
	 */
	private final String kind;

	/**
	 * The kind of grain or grass growing in this field or meadow.
	 */
	@Override
	public String getKind() {
		return kind;
	}

	/**
	 * Whether this is a field or a meadow.
	 */
	private final MeadowType type;

	/**
	 * Whether this is a field or a meadow.
	 *
	 * TODO: Use constructors instead of exposing this as a field?
	 */
	public MeadowType getType() {
		return type;
	}

	/**
	 * Whether this field or meadow is under cultivation.
	 */
	private final CultivationStatus cultivation;

	/**
	 * Whether this field or meadow is under cultivation.
	 */
	public CultivationStatus getCultivation() {
		return cultivation;
	}

	/**
	 * An ID number to identify the field or meadow.
	 */
	private final int id;

	/**
	 * An ID number to identify the field or meadow.
	 */
	@Override
	public int getId() {
		return id;
	}

	/**
	 * Which season the field is in.
	 *
	 * TODO: Make mutable?
	 */
	private final FieldStatus status;

	/**
	 * Which season the field is in.
	 *
	 * TODO: Make mutable?
	 */
	public FieldStatus getStatus() {
		return status;
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
	 * The filename of an image to use as an icon for this instance.
	 */
	@Override
	public void setImage(final String image) {
		this.image = image;
	}

	/**
	 * The size of the field or meadow, in acres. (Or a negative number if unknown.)
	 */
	private final Number acres;

	/**
	 * The size of the field or meadow, in acres. (Or a negative number if unknown.)
	 */
	@Override
	public Number getAcres() {
		return acres;
	}

	@Override
	public Meadow copy(final CopyBehavior zero) {
		final Meadow retval = new Meadow(kind, type, cultivation, id, status,
				(zero == CopyBehavior.ZERO) ? -1 : acres);
		retval.setImage(image);
		return retval;
	}

	/**
	 * The name of an image to use as an icon by default.
	 *
	 * TODO: Make more granular based on {@link #kind}
	 */
	@Override
	public String getDefaultImage() {
		return type + ".png";
	}

	@SuppressWarnings("NestedSwitchStatement") // Unavoidable given enhanced-switch restrictions
	@Override
	public String getShortDescription() {
		final String acreage;
		if (acres.doubleValue() <= 0.0) {
			acreage = "";
		} else {
			acreage = acres + "-acre ";
		}
		return switch (type) {
			case FIELD -> switch (cultivation) {
				case CULTIVATED -> "%s%s field".formatted(acreage, kind);
				case WILD -> "Wild or abandoned %s%s field".formatted(acreage, kind);
			};
			case MEADOW -> "%s%s meadow".formatted(acreage, kind);
		};
	}

	@Override
	public String toString() {
		return getShortDescription();
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof final Meadow it) {
			return kind.equals(it.getKind()) &&
					type == it.getType() &&
					status == it.getStatus() &&
					cultivation == it.getCultivation() &&
					id == it.getId() &&
					NumberComparator.compareNumbers(acres, it.getAcres()) == 0;
		} else {
			return false;
		}
	}

	@Override
	public boolean equalsIgnoringID(final IFixture fixture) {
		if (fixture instanceof final Meadow it) {
			return kind.equals(it.getKind()) &&
					type == it.getType() &&
					status == it.getStatus() &&
					cultivation == it.getCultivation() &&
					NumberComparator.compareNumbers(acres, it.getAcres()) == 0;
		} else {
			return false;
		}
	}

	@Override
	public boolean isSubset(final IFixture other, final Consumer<String> report) {
		if (other.getId() != id) {
			report.accept("IDs differ");
			return false;
		} else if (other instanceof final Meadow it) {
			if (type != it.getType()) {
				report.accept("One field, one meadow for ID #" + id);
				return false;
			} else if (!kind.equals(it.getKind())) {
				report.accept("In %s with ID #%d:\tKinds differ".formatted(
						type, id));
				return false;
			}
			final Consumer<String> localReport =
					s -> report.accept("In %s %s (ID #%d):\t%s".formatted(kind, type, id, s));
			boolean retval = true;
			if (status != it.getStatus()) {
				localReport.accept("Field status differs");
				retval = false;
			}
			if (cultivation != it.getCultivation()) { // TODO: Should non-cultivated be subset of cultivated?
				localReport.accept("Cultivation status differs");
				retval = false;
			}
			if (NumberComparator.compareNumbers(acres, it.getAcres()) < 0) {
				localReport.accept("Has larger extent");
				retval = false;
			}
			return retval;
		} else {
			report.accept("Different kinds of fixtures for ID #" + id);
			return false;
		}
	}

	@Override
	public String getPlural() {
		return "Fields and meadows";
	}

	/**
	 * The required Perception check to find the fixture.
	 */
	@Override
	public int getDC() {
		return (int) (BASE_DC - DC_MOD_PER_ACRE * acres.doubleValue());
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public Meadow combined(final Meadow other) {
		return new Meadow(kind, type, cultivation, id, status, HasExtent.sum(acres, other.getAcres()));
	}

	@Override
	public Meadow reduced(final Number subtrahend) {
		return new Meadow(kind, type, cultivation, id, status, HasExtent.sum(acres, HasExtent.negate(subtrahend)));
	}
}
