package util;

import java.io.PrintWriter;
import java.io.Writer;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A PrintWriter that prefixes every line printed via printLine() with a
 * specified string.
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
 * this program. If not, see <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 *
 */
public final class PrefixingPrintWriter extends PrintWriter {
	/**
	 * What the caller wanted each printLine prefixed with.
	 */
	private final String pref;

	/**
	 * @param outs the stream we wrap. Probably a StringWriter
	 * @param prefix what we want each line prefixed with.
	 */
	public PrefixingPrintWriter(final Writer outs, final String prefix) {
		super(outs);
		pref = prefix;
	}

	/**
	 * Write a line, prefixed with the specified prefix.
	 *
	 * @param str the line
	 */
	@Override
	public void println(@Nullable final String str) {
		print(pref);
		super.println(str);
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder(36 + pref.length());
		builder.append("PrefixingPrintWriter with prefix '");
		builder.append(pref);
		builder.append('\'');
		return NullCleaner.assertNotNull(builder.toString());
	}
}
