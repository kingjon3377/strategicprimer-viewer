package legacy.map.fixtures.mobile;

import java.util.function.Consumer;

import legacy.map.IFixture;
import legacy.map.HasMutableImage;
import org.jetbrains.annotations.NotNull;

/**
 * Animal tracks or other traces.
 *
 * TODO: We'd prefer this to not be MobileFixture, but changing that would
 * require serious refactoring of XML I/O code.
 */
public final class AnimalTracks implements HasMutableImage, MobileFixture,
		AnimalOrTracks {
	public AnimalTracks(final String kind) {
		this.kind = kind;
	}

	/**
	 * The kind of animal of which this is tracks or traces.
	 */
	private final String kind;

	/**
	 * The kind of animal of which this is tracks or traces.
	 */
	@Override
	public String getKind() {
		return kind;
	}

	@Override
	public String getShortDescription() {
		return "traces of " + kind;
	}

	/**
	 * TODO: Should perhaps depend on the kind of animal
	 */
	@Override
	public String getDefaultImage() {
		return "tracks.png";
	}

	@Override
	public int hashCode() {
		return kind.hashCode();
	}

	@Override
	public String toString() {
		return getShortDescription();
	}

	@Override
	public String getPlural() {
		return "Animal tracks";
	}

	@Override
	public boolean equals(final Object other) {
		if (this == other) {
			return true;
		} else if (other instanceof final AnimalTracks at) {
			return kind.equals(at.getKind());
		} else {
			return false;
		}
	}

	@Override
	public boolean equalsIgnoringID(final IFixture other) {
		return equals(other);
	}

	/**
	 * TODO: Allow user to customize via XML?
	 */
	@SuppressWarnings("MagicNumber")
	@Override
	public int getDC() {
		return 12;
	}

	@Override
	public int getId() {
		return -1;
	}

	@Override
	public @NotNull AnimalTracks copy(final CopyBehavior zero) {
		return new AnimalTracks(kind);
	}

	private String image = "";

	@Override
	public String getImage() {
		return image;
	}

	@Override
	public void setImage(final String image) {
		this.image = image;
	}

	@Override
	public boolean isSubset(final IFixture fixture, final Consumer<String> report) {
		switch (fixture) {
			case final AnimalTracks at when at.getKind().equals(kind) -> {
				return true;
			}
			case final AnimalTracks at -> {
				report.accept("Comparing tracks from different kinds of animals: %s and %s".formatted(
						at.getKind(), kind));
				return false;
			}
			case final Animal a when a.getKind().equals(kind) -> {
				report.accept("Has full %s animal where we have only tracks".formatted(kind));
				return false;
			}
			default -> {
				report.accept("Different kind of fixture");
				return false;
			}
		}
	}

	/**
	 * Animal tracks are a representation of evidence for players of the
	 * presence of animals that are in the main map, and so should not be
	 * in the main map themselves.
	 */
	@Override
	public boolean subsetShouldSkip() {
		return true;
	}
}
