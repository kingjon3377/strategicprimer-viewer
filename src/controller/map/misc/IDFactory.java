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
 * Copyright (C) 2012-2014 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class IDFactory {
	/**
	 * The set of IDs used already.
	 */
	private final BitSet usedIDs = new BitSet();

	/**
	 * This should probably only be called from the IDFactoryFiller.
	 *
	 * @param idNum an ID number.
	 * @return whether it's used.
	 */
	public boolean used(final int idNum) {
		return (idNum < 0) || usedIDs.get(idNum);
	}

	/**
	 * Register an ID.
	 *
	 * @param idNum the ID to register.
	 * @return the id, so this can be used functionally.
	 */
	public int register(final int idNum) {
		if (idNum >= 0) {
			if (usedIDs.get(idNum)) {
				Warning.INSTANCE.warn(new DuplicateIDException(idNum));
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
	public int createID() {
		if (usedIDs.cardinality() < Integer.MAX_VALUE) {
			return register(usedIDs.nextClearBit(0));
		} else {
			throw new IllegalStateException("Exhausted all ints ...");
		}
	}

	/**
	 * Create a copy of this factory for testing purposes. (So that we don't "register"
	 * IDs that don't end up getting used.)
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
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "IDFactory";
	}
}
