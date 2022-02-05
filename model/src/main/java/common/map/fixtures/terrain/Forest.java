package common.map.fixtures.terrain;

import lovelace.util.NumberComparator;
import common.map.fixtures.TerrainFixture;
import common.map.IFixture;
import common.map.HasExtent;
import common.map.HasMutableImage;
import common.map.HasKind;

import java.util.function.Consumer;

/**
 * A forest on a tile.
 */
public class Forest implements TerrainFixture, HasMutableImage, HasKind, HasExtent<Forest> {
	public Forest(final String kind, final boolean rows, final int id, final Number acres) {
		this.kind = kind;
		this.rows = rows;
		this.id = id;
		this.acres = acres;
	}

	public Forest(final String kind, final boolean rows, final int id) {
		this(kind, rows, id, -1);
	}

	/**
	 * What kind of trees dominate this forest.
	 */
	private final String kind;

	/**
	 * What kind of trees dominate this forest.
	 */
	@Override
	public String getKind() {
		return kind;
	}

	/**
	 * Whether this is "rows of" trees.
	 */
	private final boolean rows;

	/**
	 * Whether this is "rows of" trees.
	 */
	public boolean isRows() {
		return rows;
	}

	/**
	 * Unique identifying number for this instance.
	 */
	private int id;

	/**
	 * Unique identifying number for this instance.
	 */
	@Override
	public int getId() {
		return id;
	}

	public void setId(final int id) {
		this.id = id;
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
	 * The size of the forest, in acres. (Or a negative number if unknown.)
	 */
	private final Number acres;

	/**
	 * The size of the forest, in acres. (Or a negative number if unknown.)
	 */
	@Override
	public Number getAcres() {
		return acres;
	}

	/**
	 * Clone the forest.
	 */
	@Override
	public Forest copy(final boolean zero) {
		final Forest retval = new Forest(kind, rows, id, (zero) ? -1 : acres);
		retval.setImage(image);
		return retval;
	}

	/**
	 * The filename of an image to represent forests by default.
	 *
	 * TODO: Should differ based on kind of tree.
	 */
	@Override
	public String getDefaultImage() {
		return "trees.png";
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof Forest) {
			return ((Forest) obj).getId() == id &&
				kind.equals(((Forest) obj).getKind()) &&
				rows == ((Forest) obj).isRows() &&
				acres.equals(((Forest) obj).getAcres());
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
		if (fixture instanceof Forest) {
			return ((Forest) fixture).getKind().equals(kind) &&
				((Forest) fixture).isRows() == rows &&
				((Forest) fixture).getAcres().equals(acres);
		} else {
			return false;
		}
	}

	@Override
	public String getPlural() {
		return "Forests";
	}

	@Override
	public String getShortDescription() {
		if (HasExtent.isPositive(acres)) {
			if (rows) {
				return String.format("Rows of %s trees for %s acres", kind, acres.toString());
			} else {
				return String.format("A %s-acre %s forest", acres.toString(), kind);
			}
		} else {
			if (rows) {
				return String.format("Rows of %s trees", kind);
			} else {
				return String.format("A %s forest", kind);
			}
		}
	}

	@Override
	public String toString() {
		return getShortDescription();
	}

	@Override
	public boolean isSubset(final IFixture other, final Consumer<String> report) {
		if (id != other.getId()) {
			report.accept("Different IDs");
			return false;
		} else if (other instanceof Forest) {
			if (!((Forest) other).getKind().equals(kind)) {
				report.accept(String.format("In forest with ID #%d: Kinds differ", id));
				return false;
			}
			boolean retval = true;
			Consumer<String> localReport = s -> report.accept(
				String.format("In %s forest (ID #%d):\t%s", kind, id, s));
			if (((Forest) other).isRows() && !rows) {
				localReport.accept("In rows when we aren't");
				retval = false;
			}
			if (new NumberComparator().compare(((Forest) other).acres, acres) > 0) {
				localReport.accept("Has larger extent than we do");
				retval = false;
			}
			return retval;
		} else {
			report.accept("Different types for ID #``id``");
			return false;
		}
	}

	@Override
	public int getDC() {
		return 5;
	}

	@Override
	public Forest combined(final Forest other) {
		return new Forest(kind, rows, id, HasExtent.sum(acres, other.getAcres()));
	}

	@Override
	public Forest reduced(final Number subtrahend) {
		return new Forest(kind, rows, id, HasExtent.sum(acres, HasExtent.negate(subtrahend)));
	}
}
