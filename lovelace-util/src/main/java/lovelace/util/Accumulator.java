package lovelace.util;

/**
 * An interface to hold a mutable value, accept modifications to it, and report its current value.
 */
public interface Accumulator<Type extends Number> {
	/**
	 * Add to the accumulation.
	 *
	 * @param addend The amount to add
	 */
	void add(Type addend);

	/**
	 * @return The current value of the accumulation.
	 */
	Type getSum();
}
