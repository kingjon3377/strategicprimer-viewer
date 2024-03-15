package legacy.map.fixtures.mobile;

import java.util.function.Consumer;

import legacy.map.HasPopulation;
import legacy.map.IFixture;
import legacy.map.HasImage;
import org.jetbrains.annotations.NotNull;

/**
 * An animal or group of animals.
 */
public interface Animal extends AnimalOrTracks, MobileFixture, HasImage,
		HasPopulation<Animal> {
	/**
	 * Whether this is a talking animal.
	 *
	 * TODO: Convert 'talking animals' to a separate type
	 */
	boolean isTalking();

	/**
	 * The domestication status of the animal.
	 *
	 * TODO: Should this be an enumerated type?
	 */
	String getStatus();

	/**
	 * The turn the animal was born, or -1 if it is an adult (or if this is traces ...)
	 */
	int getBorn();

	@Override
	default String getShortDescription() {
		if (isTalking()) {
			return "talking " + getKind();
		} else {
			final String popString = (getPopulation() == 1) ? "" : getPopulation() + " ";
			if (getBorn() >= 0) {
				return String.format("%s%s (born %d)", popString, getKind(), getBorn());
			} else {
				return String.format("%s%s %s", popString, getStatus(), getKind());
			}
		}
	}

	/**
	 * Default image filename
	 *
	 * TODO: Should depend on the kind of animal
	 */
	@Override
	default String getDefaultImage() {
		return "animal.png";
	}

	@Override
	default String getPlural() {
		return "Animals";
	}

	/**
	 * Whether another animal is equal except its ID and population count.
	 */
	default boolean equalExceptPopulation(final Animal other) {
		return getKind().equals(other.getKind()) && isTalking() == other.isTalking() &&
				getStatus().equals(other.getStatus()) && getBorn() == other.getBorn();
	}

	@Override
	default boolean equalsIgnoringID(final IFixture fixture) {
		return fixture instanceof final Animal a && equalExceptPopulation(a) &&
				getPopulation() == (a).getPopulation();
	}

	@Override
	Animal reduced(int newPopulation, int newId);

	@Override
	@NotNull
	Animal copy(CopyBehavior zero);

	@Override
	Animal combined(Animal addend);

	@Override
	default boolean isSubset(final IFixture obj, final Consumer<String> report) {
		if (obj.getId() == getId()) {
			switch (obj) {
				case final Animal a when !getKind().equals(a.getKind()) -> {
					report.accept("Different kinds of animal for ID #" + getId());
					return false;
				}
				case final Animal a when !isTalking() && a.isTalking() -> {
					report.accept(String.format("In animal ID #%d:\tSubmap's is talking and master's isn't", getId()));
					return false;
				}
				case final Animal a when !getStatus().equals(a.getStatus()) -> {
					report.accept("Animal domestication status differs at ID #" + getId());
					return false;
				}
				case final Animal a when a.getPopulation() > getPopulation() -> {
					report.accept(String.format("In animal #%d: Submap has greater population than master", getId()));
					return false;
				}
				case final Animal a when a.getBorn() < getBorn() -> {
					report.accept(String.format("In animal #%d: Submap has greater age than master", getId()));
					return false;
				}
				case final Animal ignored -> {
					return true;
				}
				case final AnimalTracks at when getKind().equals(at.getKind()) -> {
					return true;
				}
				default -> {
					report.accept(String.format("For ID #%d, different kinds of members", getId()));
					return false;
				}
			}
		} else {
			report.accept(String.format("Called with different IDs, #%d and #%d", getId(), obj.getId()));
			return false;
		}
	}
}
