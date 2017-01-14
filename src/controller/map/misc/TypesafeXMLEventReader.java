package controller.map.misc;

import java.io.Reader;
import java.util.Iterator;
import java.util.NoSuchElementException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import org.eclipse.jdt.annotation.NonNull;

/**
 * A wrapper around {@link XMLEventReader} that makes the Iterator declaration take a type
 * argument. Also contains factory methods so callers don't need to deal *at all* with the
 * object this wraps.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2014-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class TypesafeXMLEventReader implements Iterator<@NonNull XMLEvent> {
	/**
	 * The object we wrap.
	 */
	private final XMLEventReader wrapped;

	/**
	 * Constructor taking an XMLEventReader.
	 *
	 * @param reader the reader to wrap
	 */
	public TypesafeXMLEventReader(final XMLEventReader reader) {
		wrapped = reader;
	}

	/**
	 * Constructor.
	 * @param reader a source of (unparsed) XML data
	 * @throws XMLStreamException on malformed XML data
	 */
	public TypesafeXMLEventReader(final Reader reader)
			throws XMLStreamException {
		this(XMLInputFactory.newInstance().createXMLEventReader(reader));
	}

	/**
	 * Whether there's another event in the stream.
	 * @return whether there's another event in the stream
	 */
	@Override
	public boolean hasNext() {
		return wrapped.hasNext();
	}

	/**
	 * Get the next element.
	 * @return the next element in the stream.
	 */
	@Override
	public XMLEvent next() {
		try {
			// TODO: use Optional to condense
			final XMLEvent retval = wrapped.nextEvent();
			if (retval == null) {
				throw new NoSuchElementException("next event was null");
			} else {
				return retval;
			}
		} catch (final XMLStreamException except) {
			final NoSuchElementException nse =
					new NoSuchElementException("Malformed XML");
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

	/**
	 * A trivial toString().
	 * @return a string representation of this class
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "TypesafeXMLEventReader";
	}
}
