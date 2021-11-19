package lovelace.util;

import java.io.Reader;
import java.io.Closeable;
import java.io.IOException;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.XMLEvent;
import java.util.Queue;
import java.util.LinkedList;
import lovelace.util.MalformedXMLException;

import java.nio.charset.MalformedInputException;

/**
 * A wrapper around {@link XMLEventReader} that hides its "raw type" from callers.
 * Also contains factory methods so callers don't need to deal *at all* with
 * the object this wraps. If the provided reader is {@link Closeable}, we call
 * its <code>close</code> method before flipping {@link hasNext} to false.
 * Callers can also pass in additional methods to call at that point.
 */
public class TypesafeXMLEventReader implements Iterator<XMLEvent>, Closeable {
	private final XMLEventReader wrapped;
	private final Queue<Closeable> closeHandles = new LinkedList<>();
	private boolean closed = false;

	public TypesafeXMLEventReader (XMLEventReader reader, Closeable... closeMethods) {
		wrapped = reader;
		if (reader instanceof Closeable) { // TODO: AutoCloseable instead?
			closeHandles.add((Closeable) reader);
		}
		for (Closeable method : closeMethods) {
			closeHandles.add(method);
		}
	}

	public TypesafeXMLEventReader(Reader reader, Closeable... closeMethods)
			throws MalformedXMLException {
		try {
			XMLInputFactory factory = XMLInputFactory.newInstance();
			factory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
			factory.setProperty("javax.xml.stream.isSupportingExternalEntities",
				Boolean.FALSE);
			wrapped = factory.createXMLEventReader(reader);
		} catch (XMLStreamException except) {
			throw new MalformedXMLException(except);
		}
		closeHandles.add(reader);
		for (Closeable method : closeMethods) {
			closeHandles.add(method);
		}
	}

	/**
	 * Close the wrapped stream and prevent reading any further data.
	 */
	@Override
	public void close() throws IOException {
		try {
			wrapped.close();
		} catch (XMLStreamException except) {
			throw new IOException(except);
		}
		for (Closeable handle : closeHandles) {
			handle.close();
		}
		closed = true;
	}

	/**
	 * @throws MalformedXMLException on malformed XML
	 */
	@Override
	public XMLEvent next() {
		if (closed) {
			throw new NoSuchElementException();
		} else {
			try {
				if (wrapped.hasNext()) {
					XMLEvent retval = wrapped.nextEvent();
					if (retval != null) {
						if (!wrapped.hasNext()) {
							close();
						}
						return retval;
					} else {
						close();
						throw new NoSuchElementException();
					}
				} else {
					close();
					throw new NoSuchElementException();
				}
			} catch (XMLStreamException exception) {
				Throwable cause = exception.getCause();
				if (cause instanceof MalformedInputException) {
					throw new RuntimeException(new MalformedXMLException(cause,
						"Invalid character in map file, probably a different encoding."));
				} else {
					throw new RuntimeException(new MalformedXMLException(exception));
				}
			} catch (IOException except) {
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
