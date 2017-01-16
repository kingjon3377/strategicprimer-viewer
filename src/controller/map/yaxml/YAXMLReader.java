package controller.map.yaxml;

import controller.map.formatexceptions.SPFormatException;
import controller.map.iointerfaces.IMapReader;
import controller.map.iointerfaces.ISPReader;
import controller.map.misc.IDFactory;
import controller.map.misc.IDRegistrar;
import controller.map.misc.IncludingIterator;
import controller.map.misc.TypesafeXMLEventReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.stream.StreamSupport;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import model.map.IMutableMapNG;
import model.map.SPMapNG;
import org.eclipse.jdt.annotation.NonNull;
import util.IteratorWrapper;
import util.Warning;

/**
 * Sixth-generation SP XML reader.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2012-2016 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of version 3 of the GNU General Public License as published by the Free Software
 * Foundation; see COPYING or
 * <a href="http://www.gnu.org/licenses/">http://www.gnu.org/licenses/</a>.
 *
 * @author Jonathan Lovelace
 */
public final class YAXMLReader implements IMapReader, ISPReader {
	/**
	 * Read an object from XML.
	 * @param <T>     A supertype of the object the XML represents
	 * @param file    the file we're reading from
	 * @param istream the stream to read from
	 * @param type    the type of the object the caller wants
	 * @param warner  the Warning instance to use for warnings
	 * @return the wanted object
	 * @throws XMLStreamException if the XML isn't well-formed
	 * @throws SPFormatException  on SP XML format error
	 */
	@Override
	public <@NonNull T> T readXML(final Path file, final Reader istream,
								  final Class<T> type, final Warning warner)
			throws XMLStreamException, SPFormatException {
		final Iterator<XMLEvent> reader = new TypesafeXMLEventReader(istream);
		final Iterable<XMLEvent> eventReader =
				new IteratorWrapper<>(new IncludingIterator(file, reader));
		final IDRegistrar idFactory = new IDFactory();
		final StartElement event = StreamSupport.stream(eventReader.spliterator(), false)
										   .filter(XMLEvent::isStartElement).findFirst()
										   .map(XMLEvent::asStartElement).orElseThrow(
						() -> new XMLStreamException(
								"XML stream didn't contain a start element"));
		final Object retval = new YAReaderAdapter(warner, idFactory).parse(
				event.asStartElement(), new QName("root"), eventReader);
		if (type.isAssignableFrom(retval.getClass())) {
			//noinspection unchecked
			return (T) retval;
		} else {
			throw new IllegalStateException("Reader produced different type than" +
													" we expected");
		}
	}

	/**
	 * Read a map from XML.
	 * @param file   the file to read from
	 * @param warner a Warning instance to use for warnings
	 * @return the map contained in the file
	 * @throws IOException        on I/O error
	 * @throws XMLStreamException on badly-formed XML
	 * @throws SPFormatException  on SP format problems
	 */
	@Override
	public IMutableMapNG readMap(final Path file, final Warning warner)
			throws IOException, XMLStreamException, SPFormatException {
		try (final Reader istream = Files.newBufferedReader(file)) {
			return readMap(file, istream, warner);
		}
	}

	/**
	 * Read a map from a stream.
	 * @param file    the file we're reading from
	 * @param istream the stream to read from
	 * @param warner  a Warning instance to use for warnings
	 * @return the map contained in the file
	 * @throws XMLStreamException on badly-formed XML
	 * @throws SPFormatException  on SP format problems
	 */
	@Override
	public IMutableMapNG readMap(final Path file, final Reader istream,
								 final Warning warner)
			throws XMLStreamException, SPFormatException {
		return readXML(file, istream, SPMapNG.class, warner);
	}
}
