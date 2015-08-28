package util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * The streams equivalent of /dev/null.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2013-2014 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of version 3 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Jonathan Lovelace
 *
 */
public class NullStream extends OutputStream {
	/**
	 * A bit-bucket to send subset output to.
	 */
	public static final PrintWriter DEV_NULL = new PrintWriter(
			new OutputStreamWriter(new NullStream()));
	/**
	 * Do nothing when anything is written.
	 *
	 * @param byt ignored
	 * @throws IOException never
	 */
	@Override
	public void write(final int byt) throws IOException {
		// Do nothing.
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "NullStream";
	}
}
