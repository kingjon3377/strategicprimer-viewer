package lovelace.util;

import java.io.Reader;
import java.io.Closeable;
import java.io.IOException;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.XMLEvent;
import java.util.Objects;
import java.util.Queue;
import java.util.LinkedList;

import java.nio.charset.MalformedInputException;

/**
 * A wrapper around {@link XMLEventReader} that hides its "raw type" from callers.
 * Also contains factory methods so callers don't need to deal *at all* with
 * the object this wraps. If the provided reader is {@link Closeable}, we call
 * its {@code close} method before flipping {@link #hasNext} to false.
 * Callers can also pass in additional methods to call at that point.
 *
 * N.B. in Java we don't take and implement {@link AutoCloseable} because that would require us to declare 'throws exception'.
 */
public class TypesafeXMLEventReader implements Iterator<XMLEvent>, Closeable {
    private final XMLEventReader wrapped;
    private final Queue<Closeable> closeHandles = new LinkedList<>();
    private boolean closed = false;

    public TypesafeXMLEventReader(final XMLEventReader reader, final Closeable... closeMethods) {
        wrapped = reader;
        if (reader instanceof final Closeable c) {
            closeHandles.add(c);
        }
        closeHandles.addAll(Arrays.asList(closeMethods));
    }

    public TypesafeXMLEventReader(final Reader reader, final Closeable... closeMethods)
            throws XMLStreamException {
        final XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
        factory.setProperty("javax.xml.stream.isSupportingExternalEntities",
                Boolean.FALSE);
        wrapped = factory.createXMLEventReader(reader);
        closeHandles.add(reader);
        closeHandles.addAll(Arrays.asList(closeMethods));
    }

    /**
     * Close the wrapped stream and prevent reading any further data.
     */
    @Override
    public void close() throws IOException {
        try {
            wrapped.close();
        } catch (final XMLStreamException except) {
            throw new IOException(except);
        }
        for (final Closeable handle : closeHandles) {
            handle.close();
        }
        closed = true;
    }

    /**
     * @throws XMLStreamException on malformed XML
     */
    @Override
    public XMLEvent next() throws NoSuchElementException {
        if (closed) {
            throw new NoSuchElementException("Reader is closed");
        } else {
            try {
                if (wrapped.hasNext()) {
                    final XMLEvent retval = wrapped.nextEvent();
	                if (Objects.isNull(retval)) {
                        close();
                        throw new NoSuchElementException("End of input");
                    } else {
                        if (!wrapped.hasNext()) {
                            close();
                        }
                        return retval;
                    }
                } else {
                    close();
                    throw new NoSuchElementException("End of input");
                }
            } catch (final XMLStreamException exception) {
                final Throwable cause = exception.getCause();
                if (cause instanceof MalformedInputException) {
                    throw new RuntimeException("Invalid character in map file, probably a different encoding.",
                            cause);
                } else {
                    throw new RuntimeException(exception);
                }
            } catch (final IOException except) {
                throw new RuntimeException(except);
            }
        }
    }

    @Override
    public boolean hasNext() {
        if (closed) {
            return false;
        } else {
            return wrapped.hasNext();
        }
    }
}
