package legacy.map.fixtures.mobile;

import legacy.map.HasKind;
import legacy.map.HasMutableImage;
import legacy.map.IFixture;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * An abstract base class for immortals that don't have any state other than
 * their ID, so their further implementation can be trivial.
 */
public abstract /* sealed */ class SimpleImmortal
		/* of Sphinx|Djinn|Griffin| Minotaur|Ogre|Phoenix|Simurgh|Troll */
		implements Immortal, HasMutableImage, HasKind {
	protected SimpleImmortal(final String kind, final String plural, final int dc, final int id) {
		this.kind = kind;
		this.plural = plural;
		this.dc = dc;
		this.id = id;
	}

	/**
	 * An ID number for the fixture.
	 */
	private final int id;

	/**
	 * An ID number for the fixture.
	 */
	@Override
	public final int getId() {
		return id;
	}

	/**
	 * What kind of immortal this is, as a string.
	 */
	private final String kind;

	/**
	 * What kind of immortal this is, as a string.
	 */
	@Override
	public final String getKind() {
		return kind;
	}

	/**
	 * The required Perception check result to find the immortal.
	 */
	private final int dc;

	/**
	 * The required Perception check result to find the immortal.
	 */
	@Override
	public final int getDC() {
		return dc;
	}

	/**
	 * A short description of the fixture. Expected to be overridden by
	 * subclasses which take "an" instead of "a".
	 */
	@Override
	public String getShortDescription() {
		return "a " + kind;
	}

	/**
	 * The plural of the immortal's kind.
	 */
	private final String plural;

	/**
	 * The plural of the immortal's kind.
	 */
	@Override
	public final String getPlural() {
		return plural;
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
	 * Clone the object.
	 */
	@Override
	public abstract @NotNull SimpleImmortal copy(CopyBehavior zero);

	@Override
	public final String toString() {
		return kind;
	}

	/**
	 * The default icon filename.
	 */
	@Override
	public final String getDefaultImage() {
		return kind + ".png";
	}

	@Override
	public final boolean equals(final Object obj) {
		if (obj instanceof final SimpleImmortal si) {
			return si.getId() == id &&
					kind.equals(si.getKind());
		} else {
			return false;
		}
	}

	@Override
	public final int hashCode() {
		return id;
	}

	/**
	 * If we ignore ID, all simple immortals of a given kind are equal.
	 */
	@Override
	public final boolean equalsIgnoringID(final IFixture fixture) {
		if (fixture instanceof final SimpleImmortal si) {
			return si.getKind().equals(kind);
		} else {
			return false;
		}
	}

	/**
	 * A fixture is a subset iff it is equal.
	 */
	@Override
	public final boolean isSubset(final IFixture obj, final Consumer<String> report) {
		if (obj.getId() == id) {
			if (obj instanceof final SimpleImmortal si &&
					si.getKind().equals(kind)) {
				return true;
			} else {
				report.accept(String.format("For ID #%d, different kinds of members", id));
				return false;
			}
		} else {
			report.accept(String.format("Called with different IDs, #%d and %d", id,
					obj.getId()));
			return false;
		}
	}
}
