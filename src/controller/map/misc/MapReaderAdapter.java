package controller.map.misc;

import java.io.File;
import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import controller.map.cxml.CompactXMLReader;
import controller.map.cxml.CompactXMLWriter;
import controller.map.formatexceptions.SPFormatException;
import controller.map.iointerfaces.IMapReader;
import controller.map.iointerfaces.SPWriter;
import model.map.IMapNG;
import model.map.IMutableMapNG;
import util.Warning;

/**
 * An adapter, so that classes using map readers and writers don't have to
 * change whenever the map reader or writer is replaced.
 *
 * This is part of the Strategic Primer assistive programs suite developed by
 * Jonathan Lovelace.
 *
 * Copyright (C) 2011-2015 Jonathan Lovelace
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of version 3 of the GNU General Public License as published by the
 * Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Jonathan Lovelace
 *
 */
public class MapReaderAdapter {
	/**
	 * The implementation we use under the hood.
	 */
	private final IMapReader reader;
	/**
	 * The map writer implementation we use under the hood.
	 */
	private final SPWriter writer;

	/**
	 * Constructor.
	 */
	public MapReaderAdapter() {
		reader = new CompactXMLReader();
		writer = new CompactXMLWriter();
	}

	/**
	 * @param file
	 *            the file to open
	 * @param warner
	 *            the Warning instance to use for warnings.
	 * @return the map it contains
	 * @throws IOException
	 *             on I/O error opening the file
	 * @throws XMLStreamException
	 *             if the XML is badly formed
	 * @throws SPFormatException
	 *             if the reader can't handle this map version or there are map
	 *             format errors
	 */
	public IMutableMapNG readMap(final File file, final Warning warner)
			throws IOException, XMLStreamException, SPFormatException {
		return reader.readMap(file, warner);
	}

	/**
	 * Write a map.
	 * @param file the file to write to
	 * @param map the map to write
	 * @throws IOException on error opening the file
	 */
	public void write(final File file, final IMapNG map) throws IOException {
		writer.write(file, map);
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "MapReaderAdapter";
	}
}
