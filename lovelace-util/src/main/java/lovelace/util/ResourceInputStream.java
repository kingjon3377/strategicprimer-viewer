package lovelace.util;

import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.NoSuchFileException;

/**
 * Basically a {@link FileInputStream}, but the file could be on disk or in the classpath.
 *
 * TODO: In Ceylon the file could be a resource associated with any of a list
 * of modules; there's nothing like that in Java (at least Java 8), but we'd
 * like to support files under a path prefix (at least on the classpath).
 */
public class ResourceInputStream extends InputStream {
    private final InputStream wrapped;

    public ResourceInputStream(final String filename) throws NoSuchFileException {
        this(filename, ResourceInputStream.class);
    }

    public ResourceInputStream(final String filename, final Class<?> sourceClass) throws NoSuchFileException {
        InputStream temp;
        try {
            // N.B. we don't use Files::newInputStream because we'd have to either catch or throw IOException.
            temp = new BufferedInputStream(new FileInputStream(filename));
        } catch (final FileNotFoundException except) {
            temp = sourceClass.getResourceAsStream("/" + filename);
            if (temp == null) {
                final NoSuchFileException wrapped = new NoSuchFileException(filename);
                wrapped.initCause(except);
                throw wrapped;
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
