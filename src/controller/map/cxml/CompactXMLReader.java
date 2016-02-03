package controller.map.cxml;

import controller.map.formatexceptions.SPFormatException;
import controller.map.iointerfaces.IMapReader;
import controller.map.iointerfaces.ISPReader;
import controller.map.misc.IDFactory;
import controller.map.misc.IncludingIterator;
import controller.map.misc.TypesafeXMLEventReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import model.map.IMutableMapNG;
import model.map.IMutablePlayerCollection;
import model.map.PlayerCollection;
import model.map.SPMapNG;
import org.eclipse.jdt.annotation.NonNull;
import util.IteratorWrapper;
import util.Warning;

/**
 * Fourth-generation SP XML reader.
 *
 * This is part of the Strategic Primer assistive programs suite developed by Jonathan
 * Lovelace.
 *
 * Copyright (C) 2012-2015 Jonathan Lovelace
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
public final class CompactXMLReader implements IMapReader, ISPReader {
	/**
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
	public <@NonNull T> T readXML(final File file, final Reader istream,
	                              final Class<T> type, final Warning warner)
			throws XMLStreamException, SPFormatException {
		final TypesafeXMLEventReader reader = new TypesafeXMLEventReader(
				                                                                istream);
		final IteratorWrapper<XMLEvent> eventReader =
				new IteratorWrapper<>(new IncludingIterator(file, reader));
		final IMutablePlayerCollection players = new PlayerCollection();
		final IDFactory idFactory = new IDFactory();
		for (final XMLEvent event : eventReader) {
			if (event.isStartElement()) {
				final Object retval = CompactReaderAdapter
						                      .parse(event.asStartElement(), eventReader,
								                      players, warner, idFactory);
				if (type.isAssignableFrom(retval.getClass())) {
					//noinspection unchecked
					return (T) retval;
				} else {
					throw new IllegalStateException("Reader produced different type than we expected");
				}
			}
		}
		throw new XMLStreamException(
				                            "XML stream didn't contain a start element");
	}

	/**
	 * @param file   the file to read from
	 * @param warner a Warning instance to use for warnings
	 * @return the map contained in the file
	 * @throws IOException        on I/O error
	 * @throws XMLStreamException on badly-formed XML
	 * @throws SPFormatException  on SP format problems
	 */
	@Override
	public IMutableMapNG readMap(final File file, final Warning warner)
			throws IOException, XMLStreamException, SPFormatException {
		try (final Reader istream = new FileReader(file)) {
			return readMap(file, istream, warner);
		}
	}

	/**
	 * @param file    the file we're reading from
	 * @param istream the stream to read from
	 * @param warner  a Warning instance to use for warnings
	 * @return the map contained in the file
	 * @throws XMLStreamException on badly-formed XML
	 * @throws SPFormatException  on SP format problems
	 */
	@Override
	public IMutableMapNG readMap(final File file, final Reader istream,
	                             final Warning warner)
			throws XMLStreamException, SPFormatException {
		return readXML(file, istream, SPMapNG.class, warner);
	}

	/**
	 * @return a String representation of the object
	 */
	@SuppressWarnings("MethodReturnAlwaysConstant")
	@Override
	public String toString() {
		return "CompactXMLReader";
	}
}
