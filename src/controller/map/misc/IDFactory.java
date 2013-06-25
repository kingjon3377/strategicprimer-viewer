package controller.map.misc;

import java.util.BitSet;

/**
 * A class to register IDs with and produce not-yet-used IDs. Performance is
 * likely to be poor, but we don't want to go to random IDs because we want them
 * to be as low as possible.
 *
 * @author Jonathan Lovelace
 *
 */
public final class IDFactory {
	/**
	 * The set of IDs used already.
	 */
	private final BitSet usedIDs = new BitSet();

	/**
	 * Register an ID.
	 *
	 * @param id the ID to register.
	 * @return the id, so this can be used functionally.
	 */
	public int register(final int id) { // NOPMD
		if (id >= 0) {
			usedIDs.set(id);
		}
		return id;
	}

	/**
	 * Generate and register an id that hasn't been previously registered.
	 *
	 * @return the generated id
	 */
	public int createID() {
		if (usedIDs.cardinality() < Integer.MAX_VALUE) {
			return register(usedIDs.nextClearBit(0));
		} else {
			throw new IllegalStateException("Exhausted all ints ...");
		}
	}

	/**
	 * Create a copy of this factory for testing purposes. (So that we don't
	 * "register" IDs that don't end up getting used.)
	 *
	 * @return a copy of this factory
	 */
	public IDFactory copy() {
		final IDFactory retval = new IDFactory();
		retval.usedIDs.or(usedIDs);
		return retval;
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "IDFactory";
	}
}
