package controller.map.misc;

import java.util.HashSet;
import java.util.Set;

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
	 * The next ID to generate.
	 */
	private int next = 1;
	/**
	 * The set of IDs used already.
	 */
	private final Set<Integer> usedIDs = new HashSet<Integer>(); 
	/**
	 * Register an ID. 
	 * @param id the ID to register.
	 * @return the id, so this can be used functionally.
	 */
	public int register(final int id) { // NOPMD
		usedIDs.add(Integer.valueOf(id));
		if (id >= next) {
			next = id + 1;
		}
		return id;
	}
	/**
	 * Generate and register an id that hasn't been previously registered.
	 * @return the generated id 
	 */
	public int createID() {
		if (next < Integer.MAX_VALUE) {
			next++;
			return next - 1; // NOPMD
		} else {
			for (int i = 0; i < Integer.MAX_VALUE; i++) {
				if (!usedIDs.contains(Integer.valueOf(i))) {
					return register(i);
				}
			}
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
		retval.usedIDs.addAll(usedIDs);
		retval.next = next;
		return retval;
	}
}
