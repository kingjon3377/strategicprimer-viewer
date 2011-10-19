package controller.map;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.stream.XMLStreamException;

import model.map.SPMap;

/**
 * An interface for map readers.
 * 
 * @author Jonathan Lovelace
 * 
 */
public interface IMapReader {
	/**
	 * Read the map contained in a file.
	 * 
	 * @param file
	 *            the name of the file to read
	 * @return the map it contains
	 * @throws SPFormatException
	 *             if the reader doesn't recognize the map format
	 * @throws XMLStreamException
	 *             if there are XML errors
	 * @throws IOException
	 *             if there are other I/O errors, i.e. opening the file
	 * @throws MapVersionException
	 *             if the reader can't handle this map version
	 */
	SPMap readMap(final String file) throws IOException, XMLStreamException,
			SPFormatException, MapVersionException;

	/**
	 * Read the map contained in an input stream.
	 * 
	 * @param istream
	 *            the stream to read from
	 * @return the map it contains
	 * @throws SPFormatException
	 *             if the reader doesn't recognize the map format
	 * @throws XMLStreamException
	 *             if there are XML errors
	 * @throws MapVersionException
	 *             if the reader can't handle this map version
	 */
	SPMap readMap(final InputStream istream) throws XMLStreamException,
			SPFormatException, MapVersionException;
}
