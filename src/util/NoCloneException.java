package util;

/**
 * An exception to throw when a CloneNotSupportedException is called for but not
 * allowed because a superclass dropped it from its signature.
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
public class NoCloneException extends IllegalStateException {
	/**
	 * Constructor.
	 * @param message the message to use
	 */
	public NoCloneException(final String message) {
		super(message, new CloneNotSupportedException(message));
	}
}
