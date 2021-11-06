package common.map.fixtures.resources;

import lovelace.util.NumberComparator;
import common.map.IFixture;
import common.map.HasExtent;

import java.util.function.Consumer;

/**
 * A field or meadow. If in forest, should increase a unit's vision slightly when the unit is on it.
 *
 * TODO: Implement that effect
 */
public class Meadow implements HarvestableFixture, HasExtent<Meadow> {
	public Meadow(String kind, boolean field, boolean cultivated, int id, FieldStatus status, Number acres) {
		this.kind = kind;
		this.field = field;
		this.cultivated = cultivated;
		this.id = id;
		this.status = status;
		this.acres = acres;
	}

	public Meadow(String kind, boolean field, boolean cultivated, int id, FieldStatus status) {
		this(kind, field, cultivated, id, status, -1);
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
	 * If true, this is a field; if false, a meadow.
	 */
	private final boolean field;

	/**
	 * If true, this is a field; if false, a meadow.
	 *
	 * TODO: Use constructors instead of exposing this as a field?
	 */
	public boolean isField() {
		return field;
	}

	/**
	 * Whether this field or meadow is under cultivation.
	 */
	private final boolean cultivated;

	/**
	 * Whether this field or meadow is under cultivation.
	 */
	public boolean isCultivated() {
		return cultivated;
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
	public void setImage(String image) {
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
	public Meadow copy(boolean zero) {
		Meadow retval = new Meadow(kind, field, cultivated, id, status,
			(zero) ? -1 : acres);
		retval.setImage(image);
		return retval;
	}

	/**
	 * The name of an image to use as an icon by default.
	 *
	 * TODO: Make more granular based on {@link kind}
	 */
	@Override
	public String getDefaultImage() {
		return (field) ? "field.png" : "meadow.png";
	}

	@Override
	public String getShortDescription() {
		String acreage;
		if (acres.doubleValue() <= 0.0) {
			acreage = "";
		} else {
			acreage = acres + "-acre ";
		}
		if (field) {
			return (cultivated) ? String.format("%s%s field", acreage, kind) :
				String.format("Wild or abandoned %s%s field", acreage, kind);
		} else {
			return String.format("%s%s meadow", acreage, kind);
		}
	}

	@Override
	public String toString() {
		return getShortDescription();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Meadow) {
			// TODO: Make NumberComparator.compare() static first, with the dynamic one delegating?
			return kind.equals(((Meadow) obj).getKind()) &&
				field == ((Meadow) obj).isField() &&
				status.equals(((Meadow) obj).getStatus()) &&
				cultivated == ((Meadow) obj).isCultivated() &&
				id == ((Meadow) obj).getId() &&
				new NumberComparator().compare(acres, ((Meadow) obj).getAcres()) == 0;
		} else {
			return false;
		}
	}

	@Override
	public boolean equalsIgnoringID(IFixture fixture) {
		if (fixture instanceof Meadow) {
			return kind.equals(((Meadow) fixture).getKind()) &&
				field == ((Meadow) fixture).isField() &&
				status.equals(((Meadow) fixture).getStatus()) &&
				cultivated == ((Meadow) fixture).isCultivated() &&
				new NumberComparator().compare(acres, ((Meadow) fixture).getAcres()) == 0;
		} else {
			return false;
		}
	}

	@Override
	public boolean isSubset(IFixture other, Consumer<String> report) {
		if (other.getId() != id) {
			report.accept("IDs differ");
			return false;
		} else if (other instanceof Meadow) {
			if (field != (((Meadow) other).isField())) {
				report.accept("One field, one meadow for ID #" + id);
				return false;
			} else if (!kind.equals(((Meadow) other).getKind())) {
				String fieldString = (field) ? "field" : "meadow";
				report.accept(String.format("In %s with ID #%d:\tKinds differ",
					fieldString, id));
				return false;
			}
			Consumer<String> localReport;
			if (field) {
				localReport = s -> report.accept(String.format("In %s field (ID #%d):\t%s",
					kind, id, s));
			} else {
				localReport = s -> report.accept(String.format("In %s meadow (ID #%d):\t%s",
					kind, id, s));
			}
			boolean retval = true;
			if (!status.equals(((Meadow) other).getStatus())) {
				localReport.accept("Field status differs");
				retval = false;
			}
			if (cultivated != ((Meadow) other).isCultivated()) {
				localReport.accept("Cultivation status differs");
				retval = false;
			}
			if (new NumberComparator().compare(acres, ((Meadow) other).getAcres()) < 0) {
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
		return (int) (20 - 3.0 / 40.0 * acres.doubleValue());
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public Meadow combined(Meadow other) {
		return new Meadow(kind, field, cultivated, id, status, HasExtent.sum(acres, other.getAcres()));
	}

	@Override
	public Meadow reduced(Number subtrahend) {
		return new Meadow(kind, field, cultivated, id, status, HasExtent.sum(acres, HasExtent.negate(subtrahend)));
	}
}
