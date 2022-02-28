package lovelace.util;

import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Basically a {@link FileInputStream}, but the file could be on disk or in the classpath.
 *
 * TODO: In Ceylon the file could be a resource associated with any of a list
 * of modules; there's nothing like that in Java (at least Java 8), but we'd
 * like to support files under a path prefix (at least on the classpath).
 */
public class ResourceInputStream extends InputStream {
	private final InputStream wrapped;
	public ResourceInputStream(final String filename) throws FileNotFoundException {
		this(filename, ResourceInputStream.class);
	}

	public ResourceInputStream(final String filename, final Class<?> sourceClass) throws FileNotFoundException {
		InputStream temp;
		try {
			temp = new BufferedInputStream(new FileInputStream(filename));
		} catch (final FileNotFoundException except) {
			temp = sourceClass.getResourceAsStream("/" + filename);
			if (temp == null) {
				throw except;
			}
		}
		wrapped = temp;
	}

	/**
	 * Read a single byte from the wrapped stream.
	 */
	@Override
	public int read() throws IOException {
		return wrapped.read();
	}

	/**
	 * Read from the wrapped stream into a provided buffer.
	 */
	@Override
	public int read(final byte[] buf) throws IOException {
		return wrapped.read(buf);
	}

	/**
	 * Read from the wrapped stream into a provided buffer at some offset.
	 */
	@Override
	public int read(final byte[] buf, final int off, final int len) throws IOException {
		return wrapped.read(buf, off, len);
	}

	/**
	 * Skip some bytes in the wrapped stream.
	 */
	@Override
	public long skip(final long num) throws IOException {
		return wrapped.skip(num);
	}

	/**
	 * How many bytes are estimated to be available in the wrapped stream.
	 */
	@Override
	public int available() throws IOException {
		return wrapped.available();
	}

	/**
	 * Close the wrapped stream.
	 */
	@Override
	public void close() throws IOException {
		wrapped.close();
	}
}
