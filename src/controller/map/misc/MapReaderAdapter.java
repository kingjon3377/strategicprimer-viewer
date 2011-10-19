package controller.map.misc;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import model.map.SPMap;
import controller.map.IMapReader;
import controller.map.MapVersionException;
import controller.map.SPFormatException;
import controller.map.simplexml.SimpleXMLReader;

/**
 * An adapter, so that classes using map readers don't have to change whenever
 * the map reader is replaced.
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
	 * Constructor.
	 */
	public MapReaderAdapter() {
		reader = new SimpleXMLReader();
	}

	/**
	 * @param filename
	 *            the file to open
	 * @return the map it contains
	 * @throws MapVersionException
	 *             if the reader can't handle this map version
	 * @throws SPFormatException
	 *             if there are map format errors
	 * @throws XMLStreamException
	 *             if the XML is badly formed
	 * @throws IOException
	 *             on I/O error opening the file
	 */
	public SPMap readMap(final String filename) throws IOException,
			XMLStreamException, SPFormatException, MapVersionException {
		return reader.readMap(filename);
	}
}
