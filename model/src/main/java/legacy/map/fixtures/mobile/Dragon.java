package legacy.map.fixtures.mobile;

import legacy.map.IFixture;
import legacy.map.HasMutableImage;
import legacy.map.HasKind;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * A dragon.
 */
public final class Dragon implements Immortal, HasMutableImage, HasKind {
	public Dragon(final String kind, final int id) {
		this.kind = kind;
		this.id = id;
	}

	/**
	 * What kind of dragon.
	 */
	private final String kind;

	/**
	 * What kind of dragon.
	 */
	@Override
	public String getKind() {
		return kind;
	}

	/**
	 * ID number.
	 */
	private final int id;

	/**
	 * ID number.
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
	public @NotNull Dragon copy(final CopyBehavior zero) {
		final Dragon retval = new Dragon(kind, id);
		retval.setImage(image);
		return retval;
	}

	@Override
	public String getShortDescription() {
		return (kind.isEmpty()) ? "dragon" : kind + " dragon";
	}

	@Override
	public String toString() {
		return getShortDescription();
	}

	/**
	 * @author https://openclipart.org/detail/166560/fire-dragon-by-olku
	 */
	@Override
	public String getDefaultImage() {
		return "dragon.png";
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof final Dragon d) {
			return d.getKind().equals(kind) && d.getId() == id;
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
		if (fixture instanceof final Dragon d) {
			return d.getKind().equals(kind);
		} else {
			return false;
		}
	}

	@Override
	public boolean isSubset(final IFixture obj, final Consumer<String> report) {
		if (obj.getId() == id) {
			if (obj instanceof final Dragon d) {
				if (d.getKind().equals(kind)) {
					return true;
				} else {
					report.accept("\tDifferent kinds of dragon for ID #" + id);
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
		return "Dragons";
	}

	@SuppressWarnings("MagicNumber")
	@Override
	public int getDC() {
		return 20;
	}
}
