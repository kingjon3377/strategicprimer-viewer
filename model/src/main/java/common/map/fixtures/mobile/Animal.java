package common.map.fixtures.mobile;

import java.util.function.Consumer;
import common.map.HasPopulation;
import common.map.IFixture;
import common.map.HasKind;
import common.map.HasImage;
import common.map.fixtures.UnitMember;

/**
 * An animal or group of animals.
 */
public interface Animal extends AnimalOrTracks, MobileFixture, HasImage,
		HasKind, UnitMember, HasPopulation<Animal> {
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
			String popString = (getPopulation() == 1) ? "" : Integer.toString(getPopulation()) + " ";
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
	default boolean equalExceptPopulation(Animal other) {
		return getKind().equals(other.getKind()) && isTalking() == other.isTalking() &&
			getStatus().equals(other.getStatus()) && getBorn() == other.getBorn();
	}

	@Override
	default boolean equalsIgnoringID(IFixture fixture) {
		return fixture instanceof Animal && equalExceptPopulation((Animal) fixture) &&
			getPopulation() == ((Animal) fixture).getPopulation();
	}

	@Override
	Animal reduced(int newPopulation, int newId);

	@Override
	Animal copy(boolean zero);

	@Override
	Animal combined(Animal addend);

	@Override
	default boolean isSubset(IFixture obj, Consumer<String> report) {
		if (obj.getId() == getId()) {
			if (obj instanceof Animal) {
				if (!getKind().equals(((Animal) obj).getKind())) {
					report.accept("Different kinds of animal for ID #" + getId());
					return false;
				} else if (!isTalking() && ((Animal) obj).isTalking()) {
					report.accept(String.format("In animal ID #%d:\tSubmap's is talking and master's isn't", getId()));
					return false;
				} else if (!getStatus().equals(((Animal) obj).getStatus())) {
					report.accept("Animal domestication status differs at ID #" + getId());
					return false;
				} else if (((Animal) obj).getPopulation() > getPopulation()) {
					report.accept(String.format("In animal #%d: Submap has greater population than master", getId()));
					return false;
				} else if (((Animal) obj).getBorn() < getBorn()) {
					report.accept(String.format("In animal #%d: Submap has greater age than master", getId()));
					return false;
				} else {
					return true;
				}
			} else if (obj instanceof AnimalTracks && getKind().equals(((AnimalTracks) obj).getKind())) {
				return true;
			} else {
				report.accept(String.format("For ID #%d, different kinds of members", getId()));
				return false;
			}
		} else {
			report.accept(String.format("Called with different IDs, #%d and #%d", getId(), obj.getId()));
			return false;
		}
	}
}
