package controller.map.misc;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;

/**
 * A (stateless, with singleton) class to open files for the XML readers. It exists
 * because we also want a special extension for testing the "include" tag: filenames
 * beginning "string:" (with the first tag following immediately) are, after that prefix
 * is stripped, turned into a StringReader instead of a FileReader as usual.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2012-2013 Jonathan Lovelace
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
public final class FileOpener {
	/**
	 * Do not instantiate; only static methods.
	 */
	private FileOpener() {
		// Do nothing.
	}

	/**
	 * If filename begins "string:", with the colon followed immediately by the
	 * angle-bracket to begin the first XML tag, it is not treated as a filename;
	 * instead,
	 * a StringReader is created from the string (with the "string:" prefix removed) and
	 * returned.
	 *
	 * @param filename a filename
	 * @return a Reader reading the file it contains (but see method summary)
	 * @throws FileNotFoundException if file not found.
	 */
	public static Reader createReader(final String filename)
			throws FileNotFoundException {
		if (filename.contains("string:<")) {
			return new BufferedReader(new StringReader(filename.substring(7)));
		} else {
			return new BufferedReader(new FileReader(filename));
		}
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "FileOpener";
	}
}
