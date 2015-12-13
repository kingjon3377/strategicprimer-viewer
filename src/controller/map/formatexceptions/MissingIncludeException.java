package controller.map.formatexceptions;

/**
 * An exception to throw when an "include" tag references a nonexistent file. We
 * need it because we can't throw FileNotFound from tag-processing functions,
 * only SPFormatExceptions.
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
public final class MissingIncludeException extends SPFormatException {
	/**
	 * Constructor.
	 *
	 * @param file the missing file
	 * @param cause the exception that caused this one to be thrown.
	 * @param line the line the "include" tag was on.
	 */
	public MissingIncludeException(final String file, final Throwable cause,
			final int line) {
		super("File " + file + ", referenced by <include> tag on line " + line
				+ ", does not exist", line, cause);
	}
}
