package strategicprimer.viewer.xmlio;

import java.util.NoSuchElementException;

/**
 * A NoSuchElementException that takes a custom cause, unlike its superclass. This remains
 * implemented in Java because there is no initCause() method in ceylon.lang.Exception,
 * which java.lang.Exception gets erased to (and vice versa).
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2012-2017 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public class NoSuchElementBecauseException extends NoSuchElementException {
	/**
	 * Constructor.
	 *
	 * @param message the message
	 * @param cause   the cause
	 */
	public NoSuchElementBecauseException(final String message, final Throwable cause) {
		super(message);
		initCause(cause);
	}
}
