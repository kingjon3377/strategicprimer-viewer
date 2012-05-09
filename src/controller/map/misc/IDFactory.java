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
	 * The set of IDs used already.
	 */
	private final Set<Long> usedIDs = new HashSet<Long>(); 
	/**
	 * Register an ID. 
	 * @param id the ID to register.
	 * @return the id, so this can be used functionally.
	 */
	public long register(final long id) { // NOPMD
		usedIDs.add(Long.valueOf(id));
		return id;
	}
	/**
	 * Generate and register an id that hasn't been previously registered.
	 * @return the generated id 
	 */
	public long getID() {
		for (long i = 0; i < Long.MAX_VALUE; i++) {
			if (!usedIDs.contains(Long.valueOf(i))) {
				return register(i);
			}
		}
		throw new IllegalStateException("Exhausted all longs ...");
	}
}
