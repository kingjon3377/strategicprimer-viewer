package util;

import java.io.OutputStream;
import java.util.Formatter;

/**
 * The streams equivalent of /dev/null.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2013-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
@SuppressWarnings("resource")
public final class NullStream extends OutputStream {
	/**
	 * A bit-bucket to send subset output to.
	 */
	@SuppressWarnings("IOResourceOpenedButNotSafelyClosed")
	public static final Formatter DEV_NULL =
			new Formatter(new NullStream());

	/**
	 * Do nothing when anything is written.
	 *
	 * @param byt ignored
	 */
	@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
	@Override
	public void write(final int byt) {
		// Do nothing.
	}

	/**
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "NullStream";
	}
}
