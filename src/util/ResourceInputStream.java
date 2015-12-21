package util;

import org.eclipse.jdt.annotation.Nullable;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Basically a FileInputStream, but the file could be on disk or in the classpath.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2012-2014 Jonathan Lovelace
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
public final class ResourceInputStream extends InputStream {
	/**
	 * The stream we wrap.
	 */
	private final InputStream wrapped;

	/**
	 * Constructor.
	 *
	 * @param filename the name of the file to read
	 * @throws FileNotFoundException if it's not found on disk or in the classpath
	 */
	@SuppressWarnings("resource")
	// The resource is *not* leaked; it's closed when this is.
	public ResourceInputStream(final String filename)
			throws FileNotFoundException {
		InputStream temp;
		try {
			temp = new BufferedInputStream(new FileInputStream(filename));
		} catch (final FileNotFoundException except) {
			temp = ResourceInputStream.class.getClassLoader()
					       .getResourceAsStream(filename);
			if (temp == null) {
				throw except;
			}
		}
		wrapped = temp;
	}

	/**
	 * @return the result of reading from the file
	 * @throws IOException when thrown by wrapped stream
	 */
	@Override
	public int read() throws IOException {
		return wrapped.read();
	}

	/**
	 * @param buf a buffer into which to read
	 * @return the number of bytes read
	 * @throws IOException when thrown by wrapped stream
	 */
	@Override
	public int read(final byte @Nullable [] buf) throws IOException {
		return wrapped.read(buf);
	}

	/**
	 * @param buf the buffer into which to read the data
	 * @param off the offset in the array
	 * @param len the maximum number of bytes to read
	 * @return the total number of bytes read into the buffer, or 1 if EOF
	 * @throws IOException when thrown by wrapped implementation
	 */
	@Override
	public int read(final byte @Nullable [] buf, final int off, final int len)
			throws IOException {
		return wrapped.read(buf, off, len);
	}

	/**
	 * @param num how many bytes to skip
	 * @return the actual number of bytes skipped
	 * @throws IOException if thrown by wrapped implementation.
	 */
	@Override
	public long skip(final long num) throws IOException {
		return wrapped.skip(num);
	}

	/**
	 * @return an estimate of the number of bytes that can be read or skipped over in the
	 * stream without blocking
	 * @throws IOException if thrown by wrapped implementation
	 */
	@Override
	public int available() throws IOException {
		return wrapped.available();
	}

	/**
	 * @throws IOException if thrown by wrapped implementation
	 */
	@Override
	public void close() throws IOException {
		wrapped.close();
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "ResourceInputStream";
	}
}
