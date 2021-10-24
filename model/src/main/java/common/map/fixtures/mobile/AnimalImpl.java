package common.map.fixtures.mobile;

import common.map.HasMutableImage;

/**
 * An animal or population of animals in the map.
 */
public class AnimalImpl implements Animal, HasMutableImage {
	public AnimalImpl(String kind, boolean talking, String status, int id, int born, int population) {
		this.kind = kind;
		this.talking = talking;
		this.status = status;
		this.id = id;
		this.born = born;
		this.population = population;
	}

	public AnimalImpl(String kind, boolean talking, String status, int id, int born) {
		this(kind, talking, status, id, born, 1);
	}

	public AnimalImpl(String kind, boolean talking, String status, int id) {
		this(kind, talking, status, id, -1, 1);
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
	 * Whether this is a talking animal.
	 */
	private final boolean talking;

	/**
	 * Whether this is a talking animal.
	 */
	@Override
	public boolean isTalking() {
		return talking;
	}

	/**
	 * The domestication status of the animal.
	 */
	private String status;

	/**
	 * The domestication status of the animal.
	 */
	@Override
	public String getStatus() {
		return status;
	}

	/**
	 * Set the domestication status of the animal.
	 */
	public void setStatus(String status) {
		this.status = status;
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
	 * The filename of an image to use as an icon for this instance.
	 */
	@Override
	public void setImage(String image) {
		this.image = image;
	}

	/**
	 * What kind of animal this is
	 */
	private final String kind;

	/**
	 * What kind of animal this is
	 */
	@Override
	public String getKind() {
		return kind;
	}

	/**
	 * The turn the animal was born, or -1 if it is an adult (or if this is traces ...)
	 */
	private int born;

	/**
	 * The turn the animal was born, or -1 if it is an adult (or if this is traces ...)
	 */
	@Override
	public int getBorn() {
		return born;
	}

	/**
	 * Set the turn the animal was born, or -1 if it is an adult
	 */
	public void setBorn(int born) {
		this.born = born;
	}

	/**
	 * How many individual animals are in the population this represents.
	 */
	private final int population;

	/**
	 * How many individual animals are in the population this represents.
	 */
	@Override
	public int getPopulation() {
		return population;
	}

	/**
	 * Clone the animal.
	 *
	 * TODO: change, here and elsewhere, so that "unknown" is -1 population
	 */
	@Override
	public Animal copy(boolean zero) {
		AnimalImpl retval = new AnimalImpl(kind, talking, status, id,
			(zero) ? -1 : born, (zero) ? 1 : population);
		retval.setImage(image);
		return retval;
	}

	@Override
	public Animal reduced(int newPopulation, int newId) {
		return new AnimalImpl(kind, talking, status, newId, born, newPopulation);
	}

	@Override
	public Animal combined(Animal addend) {
		return new AnimalImpl(kind, talking, status, id, born,
			Math.max(0, population) + Math.max(0, addend.getPopulation()));
	}

	/**
	 * Required Perception check result to find the animal.
	 *
	 * TODO: Should be based on population size as well as animal kind
	 */
	@Override
	public int getDC() {
		int retval = AnimalDiscoveryDCs.get(kind);
		return (retval >= 0) ?  retval : 22;
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public String toString() {
		return getShortDescription();
	}

	/**
	 * An object is equal if it is an animal with equal kind, either both
	 * or neither are talking, and their IDs are equal.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj instanceof Animal) {
			return ((Animal) obj).getId() == id && equalsIgnoringID((Animal) obj);
		} else {
			return false;
		}
	}
}
