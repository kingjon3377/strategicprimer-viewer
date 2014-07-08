package util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Basically a FileInputStream, but the file could be on disk or in the
 * classpath.
 *
 * @author Jonathan Lovelace
 *
 */
public class ResourceInputStream extends InputStream {
	/**
	 * The stream we wrap.
	 */
	private final InputStream wrapped;

	/**
	 * Constructor.
	 *
	 * @param filename the name of the file to read
	 * @throws FileNotFoundException if it's not found on disk or in the
	 *         classpath
	 */
	@SuppressWarnings("resource")
	// The resource is *not* leaked; it's closed when this is.
	public ResourceInputStream(final String filename)
			throws FileNotFoundException {
		// ESCA-JAVA0177:
		InputStream temp;
		try {
			temp = new FileInputStream(filename);
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
	 *
	 * @see java.io.InputStream#read()
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
	public int read(@Nullable final byte[] buf) throws IOException {
		return wrapped.read(buf);
	}

	/**
	 * @param buf the buffer into which to read the data
	 * @param off the offset in the array
	 * @param len the maximum number of bytes to read
	 * @return the total number of bytes read into the buffer, or 1 if EOF
	 * @throws IOException when thrown by wrapped implementation
	 * @see InputStream#read(byte[], int, int)
	 */
	@Override
	public int read(@Nullable final byte[] buf, final int off, final int len)
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
	 * @return an estimate of the number of bytes that can be read or skipped
	 *         over in the stream without blocking
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
