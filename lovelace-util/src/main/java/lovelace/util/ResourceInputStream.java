package lovelace.util;

import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.Objects;

/**
 * Basically a {@link FileInputStream}, but the file could be on disk or in the classpath.
 *
 * TODO: In Ceylon the file could be a resource associated with any of a list
 * of modules; there's nothing like that in Java (at least Java 8), but we'd
 * like to support files under a path prefix (at least on the classpath).
 */
public class ResourceInputStream extends InputStream {
	private final InputStream wrapped;
	private final String filename;

	public ResourceInputStream(final String filename) throws NoSuchFileException {
		this(filename, ResourceInputStream.class);
	}

	public ResourceInputStream(final String filename, final Class<?> sourceClass) throws NoSuchFileException {
		this.filename = filename;
		InputStream temp;
		//noinspection RedundantSuppression The warning appears if not suppressed, but is 'redundant' when suppressed?
		try {
			// N.B. we don't use Files::newInputStream because we'd have to either catch or throw IOException.
			temp = new BufferedInputStream(new FileInputStream(filename));
		} catch (final FileNotFoundException except) {
			//noinspection HardcodedFileSeparator getResourceAsStream() takes a '/'-delimited path.
			temp = sourceClass.getResourceAsStream("/" + filename);
			if (Objects.isNull(temp)) {
				final NoSuchFileException wrappedException = new NoSuchFileException(filename);
				wrappedException.initCause(except);
				throw wrappedException;
			}
		}
		wrapped = temp;
	}

	/**
	 * Read a single byte from the wrapped stream.
	 */
	@Override
	public final int read() throws IOException {
		return wrapped.read();
	}

	/**
	 * Read from the wrapped stream into a provided buffer.
	 */
	@Override
	public final int read(final byte[] buf) throws IOException {
		return wrapped.read(buf);
	}

	/**
	 * Read from the wrapped stream into a provided buffer at some offset.
	 */
	@Override
	public final int read(final byte[] buf, final int off, final int len) throws IOException {
		return wrapped.read(buf, off, len);
	}

	/**
	 * Skip some bytes in the wrapped stream.
	 */
	@Override
	public final long skip(final long num) throws IOException {
		return wrapped.skip(num);
	}

	/**
	 * How many bytes are estimated to be available in the wrapped stream.
	 */
	@Override
	public final int available() throws IOException {
		return wrapped.available();
	}

	/**
	 * Close the wrapped stream.
	 */
	@Override
	public final void close() throws IOException {
		wrapped.close();
	}

	@Override
	public final String toString() {
		return "ResourceInputStream for: " + filename;
	}
}
