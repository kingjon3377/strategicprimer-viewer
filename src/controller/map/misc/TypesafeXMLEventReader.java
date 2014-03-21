package controller.map.misc;

import java.io.Reader;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

/**
 * A wrapper around {@link XMLEventReader} that makes the Iterator declaration
 * take a type argument. Also contains factory methods so callers don't need to
 * deal *at all* with the object this wraps.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class TypesafeXMLEventReader implements Iterator<XMLEvent> {
	/**
	 * The object we wrap.
	 */
	private final XMLEventReader wrapped;
	/**
	 * Constructor taking an XMLEventReader.
	 * @param reader the reader to wrap
	 */
	public TypesafeXMLEventReader(final XMLEventReader reader) {
		wrapped = reader;
	}
	/**
	 * @param reader a source of (unparsed) XML data
	 * @throws XMLStreamException on malformed XML data 
	 */
	public TypesafeXMLEventReader(final Reader reader)
			throws XMLStreamException {
		final XMLEventReader temp = XMLInputFactory.newInstance()
				.createXMLEventReader(reader);
		if (temp == null) {
			throw new XMLStreamException(
					"Factory created a null XMLEventReader");
		} else {
			wrapped = temp;
		}
	}
	/**
	 * @return whether there's another event in the stream 
	 */
	@Override
	public boolean hasNext() {
		return wrapped.hasNext();
	}
	/**
	 * @return the next element in the stream.
	 */
	@Override
	public XMLEvent next() {
		try {
			final XMLEvent retval = wrapped.nextEvent();
			if (retval == null) {
				throw new NoSuchElementException("next event was null");
			} else {
				return retval;
			}
		} catch (XMLStreamException except) {
			final NoSuchElementException nse = new NoSuchElementException(
					"Malformed XML");
			nse.initCause(except);
			throw nse;
		}
	}
	/**
	 * Try to remove the current element.
	 */
	@Override
	public void remove() {
		wrapped.remove();
	}
}
