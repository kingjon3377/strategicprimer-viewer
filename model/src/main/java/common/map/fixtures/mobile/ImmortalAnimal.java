package common.map.fixtures.mobile;

import common.map.HasKind;
import common.map.HasMutableImage;
import common.map.IFixture;

import java.util.function.Consumer;
import java.util.function.IntFunction;

/**
 * An immortal that used to be represented as an {@link Animal animal} in the
 * past.  This class is provided, instead of making them additional subclasses
 * of {@link SimpleImmortal}, to ease the handling of old map files by
 * XML-reading code.
 *
 * TODO: Merge with/make subclass of SimpleImmortal?
 */
public /* sealed */ abstract class ImmortalAnimal
		/* of Snowbird|Thunderbird|Pegasus|Unicorn|Kraken */
		implements Immortal, HasMutableImage, HasKind {
	/**
	 * Get an immortal constructor for the given kind.
	 */
	public static IntFunction<ImmortalAnimal> parse(final String kind) {
		switch (kind) {
			case "snowbird":
				return Snowbird::new;
			case "thunderbird":
				return Thunderbird::new;
			case "pegasus":
				return Pegasus::new;
			case "unicorn":
				return Unicorn::new;
			case "kraken":
				return Kraken::new;
			default:
				throw new IllegalArgumentException("Unknown immortal-animal kind " + kind);
		}
	}

	protected ImmortalAnimal(final String kind, final String plural, final int dc, final int id) {
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
	 * A short description of the fixture. Expected to be overridden by
	 * subclasses which take "an" instead of "a".
	 */
	@Override
	public String getShortDescription() {
		return "a " + kind;
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
	public abstract ImmortalAnimal copy(CopyBehavior zero);

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
		if (obj instanceof ImmortalAnimal) {
			return ((ImmortalAnimal) obj).getId() == id &&
				kind.equals(((ImmortalAnimal) obj).getKind());
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
		if (fixture instanceof ImmortalAnimal) {
			return ((ImmortalAnimal) fixture).getKind().equals(kind);
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
			if (obj instanceof ImmortalAnimal &&
					((ImmortalAnimal) obj).getKind().equals(kind)) {
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
