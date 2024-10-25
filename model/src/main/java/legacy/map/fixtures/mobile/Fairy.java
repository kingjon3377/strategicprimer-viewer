package legacy.map.fixtures.mobile;

import legacy.map.IFixture;
import legacy.map.HasMutableImage;
import legacy.map.HasKind;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * A fairy.
 */
public class Fairy implements Immortal, HasMutableImage, HasKind {
	public Fairy(final String kind, final int id) {
		this.kind = kind;
		this.id = id;
	}

	/**
	 * The kind of fairy.
	 */
	private final String kind;

	/**
	 * The kind of fairy.
	 */
	@Override
	public String getKind() {
		return kind;
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
	 * @param image The filename of an image to use as an icon for this instance.
	 */
	@Override
	public void setImage(final String image) {
		this.image = image;
	}

	@Override
	public @NotNull Fairy copy(final CopyBehavior zero) {
		final Fairy retval = new Fairy(kind, id);
		retval.setImage(image);
		return retval;
	}

	@Override
	public String getShortDescription() {
		return kind + " fairy";
	}

	@Override
	public String toString() {
		return getShortDescription();
	}

	@Override
	public String getDefaultImage() {
		return "fairy.png";
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof final Fairy f) {
			return f.getKind().equals(kind) && f.getId() == id;
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
		if (fixture instanceof final Fairy f) {
			return f.getKind().equals(kind);
		} else {
			return false;
		}
	}

	@Override
	public boolean isSubset(final IFixture obj, final Consumer<String> report) {
		if (obj.getId() == id) {
			if (obj instanceof final Fairy f) {
				if (f.getKind().equals(kind)) {
					return true;
				} else {
					report.accept("\tDifferent kinds of fairy for ID #" + id);
					return false;
				}
			} else {
				report.accept("\tFor ID #%d, different kinds of members".formatted(id));
				return false;
			}
		} else {
			report.accept("\tCalled with different IDs, #%d and %d".formatted(
					id, obj.getId()));
			return false;
		}
	}

	@Override
	public String getPlural() {
		return "Fairies";
	}

	/**
	 * The required Perception check result to find the fairy.
	 *
	 * TODO: Should vary, either defined in XML or computed from kind
	 */
	@SuppressWarnings("MagicNumber")
	@Override
	public int getDC() {
		return 28;
	}
}
