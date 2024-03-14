package legacy.map.fixtures.mobile;

import legacy.map.HasKind;
import legacy.map.HasMutableImage;
import legacy.map.IFixture;
import org.jetbrains.annotations.NotNull;

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
		return switch (kind) {
			case "snowbird" -> Snowbird::new;
			case "thunderbird" -> Thunderbird::new;
			case "pegasus" -> Pegasus::new;
			case "unicorn" -> Unicorn::new;
			case "kraken" -> Kraken::new;
			default -> throw new IllegalArgumentException("Unknown immortal-animal kind " + kind);
		};
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
	public abstract @NotNull ImmortalAnimal copy(CopyBehavior zero);

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
		if (obj instanceof final ImmortalAnimal i) {
			return i.getId() == id &&
					kind.equals(i.getKind());
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
		if (fixture instanceof final ImmortalAnimal i) {
			return i.getKind().equals(kind);
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
			if (obj instanceof final ImmortalAnimal i &&
					i.getKind().equals(kind)) {
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
