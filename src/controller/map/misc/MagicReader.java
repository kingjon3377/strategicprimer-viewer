package controller.map.misc;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A Reader that delegates to FileReader unless the filename begins "string:<", in which
 * case the "string:" prefix is stripped and we delegate to a StringReader.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2016 Jonathan Lovelace
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
public class MagicReader extends Reader {
	/**
	 * The Reader we delegate to.
	 */
	private final Reader delegate;
	/**
	 * The name of the file we're reading, or "a string" if reading from a string.
	 */
	private final String fname;
	/**
	 * If filename begins "string:", with the colon followed immediately by the
	 * angle-bracket to begin the first XML tag, it is not treated as a filename;
	 * instead, we read from the contents of the string after that prefix.
	 *
	 * @param filename a filename
	 * @throws FileNotFoundException if file not found.
	 */
	public MagicReader(final String filename) throws FileNotFoundException {
		if (filename.contains("string:<")) {
			delegate = new StringReader(filename.substring(7));
			fname = "a string";
		} else {
			delegate = new FileReader(filename);
			fname = filename;
		}
	}

	/**
	 * Read into a buffer.
	 * @param buffer the buffer to read into
	 * @param offset the offset at which to begin reading
	 * @param length how much to read
	 * @return how much was read
	 * @throws IOException on I/O error reading
	 */
	@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
	@Override
	public int read(final char @Nullable [] buffer, final int offset, final int length)
			throws IOException {
		return delegate.read(buffer, offset, length);
	}

	/**
	 * Close the reader.
	 * @throws IOException on I/O error doing so
	 */
	@Override
	public void close() throws IOException {
		delegate.close();
	}

	/**
	 * Read a single character.
	 * @return the character read, or -1 on EOF
	 * @throws IOException on I/O error
	 */
	@Override
	public int read() throws IOException {
		return delegate.read();
	}

	/**
	 * @return whether the stream supports mark()
	 */
	@Override
	public boolean markSupported() {
		return delegate.markSupported();
	}

	/**
	 * Marks the present position in the stream, if the reader we delegate to supports it.
	 * @param readAheadLimit Limit on the number of characters that may be
	 *                         read while still preserving the mark.
	 * @throws IOException on I/O error
	 */
	@Override
	public void mark(final int readAheadLimit) throws IOException {
		delegate.mark(readAheadLimit);
	}

	/**
	 * Reset the stream if the delegate supports it.
	 * @throws IOException on I/O error
	 */
	@Override
	public void reset() throws IOException {
		delegate.reset();
	}
	/**
	 * @return a diagnostic String.
	 */
	@Override
	public String toString() {
		return "MagicReader reading " + fname;
	}
}
