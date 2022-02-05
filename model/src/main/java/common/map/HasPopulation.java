package common.map;

/**
 * An interface for fixtures with an integer population (or quantity reported as such).
 *
 * @see HasExtent
 */
public interface HasPopulation<Self extends HasPopulation<Self>> extends IFixture, Subsettable<IFixture> {
	/**
	 * The population.
	 */
	int getPopulation();

	/**
	 * Return a copy of this object, except with its population the
	 * specified value instead of its current value, and with the specified
	 * ID.
	 */
	Self reduced(int newPopulation, int newId);

	/**
	 * Return a copy of this object, except with its population the
	 * specified value instead of its current value.
	 */
	default Self reduced(final int newPopulation) {
		return reduced(newPopulation, getId());
	}

	/**
	 * Return a copy of this object, except with its population increased
	 * to its current population plus that of the {@link addend}, which
	 * must be of the same type.
	 */
	Self combined(Self addend);
}
