package controller.map.misc;

import java.util.BitSet;
import util.Warning;

/**
 * A class to register IDs with and produce not-yet-used IDs. Performance is likely to be
 * poor, but we don't want to go to random IDs because we want them to be as low as
 * possible.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2012-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class IDFactory implements IDRegistrar {
	/**
	 * The set of IDs used already.
	 */
	private final BitSet usedIDs = new BitSet();

	/**
	 * Whether the given ID is unused.
	 * @param idNum the ID number to check
	 * @return whether it's unused
	 */
	@Override
	public boolean isIDUnused(final int idNum) {
		return (idNum >= 0) && !usedIDs.get(idNum);
	}

	/**
	 * Register an ID.
	 *
	 * @param idNum the ID to register.
	 * @return the id, so this can be used functionally.
	 */
	@Override
	public int register(final int idNum) {
		return register(Warning.DEFAULT, idNum);
	}

	/**
	 * Register an ID.
	 *
	 * @param warning the Warning instance to use to report if the ID has already been
	 *                registered
	 * @param idNum   the ID to register.
	 * @return the id, so this can be used functionally.
	 */
	@Override
	public int register(final Warning warning, final int idNum) {
		if (idNum >= 0) {
			if (usedIDs.get(idNum)) {
				warning.warn(new DuplicateIDException(idNum));
			}
			usedIDs.set(idNum);
		}
		return idNum;
	}

	/**
	 * Generate and register an id that hasn't been previously registered.
	 *
	 * @return the generated id
	 */
	@Override
	public int createID() {
		synchronized (usedIDs) {
			if (usedIDs.cardinality() < Integer.MAX_VALUE) {
				return register(usedIDs.nextClearBit(0));
			} else {
				throw new IllegalStateException("Exhausted all possible integers ...");
			}
		}
	}

	/**
	 * Create a copy of this factory for testing purposes. (So that we don't "register"
	 * IDs that don't end up getting used.)
	 *
	 * TODO: Tests should cover this method
	 *
	 * @return a copy of this factory
	 */
	public IDRegistrar copy() {
		final IDFactory retval = new IDFactory();
		retval.usedIDs.or(usedIDs);
		return retval;
	}

	/**
	 * A trivial toString().
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "IDFactory";
	}
}
