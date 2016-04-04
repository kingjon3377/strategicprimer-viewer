package view.util;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Originally a class to get around Eclipse's insistence that System.out might be null,
 * now extends it to ensure that stdout cannot be closed.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2011-2016 Jonathan Lovelace
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
public final class SystemOut extends FilterOutputStream {
	/**
	 * The singleton object.
	 */
	@SuppressWarnings("resource")
	public static final PrintStream SYS_OUT = new PrintStream(new SystemOut());

	/**
	 * Constructor.
	 */
	private SystemOut() {
		super(System.out);
	}
	/**
	 * Do *not* close; this is stdout. However, flush the stream.
	 */
	@Override
	public void close() throws IOException {
		flush();
	}

	/**
	 * @return a String representation of the object.
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "SystemOut";
	}
}
