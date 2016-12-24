package controller.map.misc;

import util.Warning;

/**
 * An interface for a factory that XML-reading code can use to register IDs and produce
 * not-yet-used IDs.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public interface IDRegistrar {
	/**
	 * Whether the given ID number is unused.
	 * @param idNum the ID number to check
	 * @return whether it's unused
	 */
	boolean isIDUnused(int idNum);

	/**
	 * Register an ID.
	 *
	 * @param idNum the ID to register.
	 * @return the id, so this can be used functionally.
	 */
	int register(int idNum);

	/**
	 * Register an ID.
	 *
	 * @param warning the Warning instance to use to report if the ID has already been
	 *                registered
	 * @param idNum   the ID to register.
	 * @return the id, so this can be used functionally.
	 */
	int register(Warning warning, int idNum);

	/**
	 * Generate and register an id that hasn't been previously registered.
	 *
	 * @return the generated id
	 */
	int createID();
}
