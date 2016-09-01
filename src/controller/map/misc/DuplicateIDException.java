package controller.map.misc;

/**
 * An exception to warn about duplicate IDs.
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
public final class DuplicateIDException extends Exception {
	/**
	 * @param idNum the duplicate ID.
	 */
	public DuplicateIDException(final int idNum) {
		super("Duplicate ID #" + idNum);
	}
}
