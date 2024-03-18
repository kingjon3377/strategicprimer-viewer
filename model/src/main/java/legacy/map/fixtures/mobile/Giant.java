package legacy.map.fixtures.mobile;

import legacy.map.IFixture;
import legacy.map.HasMutableImage;
import legacy.map.HasKind;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * A giant.
 */
public class Giant implements Immortal, HasMutableImage, HasKind {
	public Giant(final String kind, final int id) {
		this.kind = kind;
		this.id = id;
	}

	/**
	 * The kind of giant.
	 */
	private final String kind;

	/**
	 * The kind of giant.
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
	public @NotNull Giant copy(final CopyBehavior zero) {
		final Giant retval = new Giant(kind, id);
		retval.setImage(image);
		return retval;
	}

	@Override
	public String getShortDescription() {
		return (kind.isEmpty()) ? "giant" : kind + " giant";
	}

	@Override
	public String toString() {
		return getShortDescription();
	}

	@Override
	public String getDefaultImage() {
		return "giant.png";
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof final Giant g) {
			return g.getKind().equals(kind) && g.getId() == id;
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
		if (fixture instanceof final Giant g) {
			return g.getKind().equals(kind);
		} else {
			return false;
		}
	}

	@Override
	public boolean isSubset(final IFixture obj, final Consumer<String> report) {
		if (obj.getId() == id) {
			if (obj instanceof final Giant g) {
				if (g.getKind().equals(kind)) {
					return true;
				} else {
					report.accept("\tDifferent kinds of giant for ID #" + id);
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
		return "Giants";
	}

	/**
	 * The required Perception check result to find the fairy.
	 *
	 * TODO: Should this vary with kind?
	 */
	@Override
	public int getDC() {
		return 28;
	}
}
