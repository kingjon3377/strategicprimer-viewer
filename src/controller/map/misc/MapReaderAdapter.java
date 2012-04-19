package controller.map.misc;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import model.map.SPMap;
import util.Warning;
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
	 * @param warner the Warning instance to use for warnings.
	 * @return the map it contains
	 * @throws IOException
	 *             on I/O error opening the file
	 * @throws XMLStreamException
	 *             if the XML is badly formed
	 * @throws SPFormatException
	 *             if there are map format errors
	 * @throws MapVersionException
	 *             if the reader can't handle this map version
	 */
	public SPMap readMap(final String filename, final Warning warner) throws IOException,
			XMLStreamException, SPFormatException, MapVersionException {
		return reader.readMap(filename, warner);
	}
}
