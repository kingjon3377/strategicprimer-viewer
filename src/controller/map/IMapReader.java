package controller.map;

import java.io.IOException;
import java.io.Reader;

import javax.xml.stream.XMLStreamException;

import controller.map.formatexceptions.MapVersionException;
import controller.map.formatexceptions.SPFormatException;

import model.map.IMap;
import model.map.MapView;
import util.Warning;

/**
 * An interface for map readers.
 * 
 * @author Jonathan Lovelace
 * 
 */
public interface IMapReader {
	/**
	 * Read the map view contained in a file.
	 * 
	 * @param file the name of the file to read
	 * @param warner the Warning instance to use for warnings.
	 * @return the map view it contains
	 * @throws IOException if there are other I/O errors, i.e. opening the file
	 * @throws XMLStreamException if there are XML errors
	 * @throws SPFormatException if the reader doesn't recognize the map format
	 * @throws MapVersionException if the reader can't handle this map version
	 */
	MapView readMap(final String file, final Warning warner)
			throws IOException, XMLStreamException, SPFormatException,
			MapVersionException;

	/**
	 * Read the map contained in a reader.
	 * 
	 * @param file the name of the file the stream represents
	 * @param istream the reader to read from
	 * @param warner the Warning instance to use for warnings.
	 * @return the map it contains
	 * @throws XMLStreamException if there are XML errors
	 * @throws SPFormatException if the reader doesn't recognize the map format
	 * @throws MapVersionException if the reader can't handle this map version
	 */
	IMap readMap(final String file, final Reader istream, final Warning warner)
			throws XMLStreamException, SPFormatException, MapVersionException;
}
