package util;

/**
 * A centralized place for the system-line-separator constant.
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
public final class LineEnd {
	/**
	 * The system line separator.
	 */
	public static final String LINE_SEP = System.lineSeparator();

	/**
	 * Do not instantiate.
	 */
	private LineEnd() {
		// Never called.
	}
}
