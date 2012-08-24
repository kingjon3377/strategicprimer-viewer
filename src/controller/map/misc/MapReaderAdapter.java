package controller.map.misc;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import model.map.IMap;
import model.map.MapView;
import util.Warning;
import controller.map.IMapReader;
import controller.map.MapVersionException;
import controller.map.SPFormatException;
import controller.map.SPWriter;
import controller.map.readerng.MapReaderNG;
import controller.map.readerng.MapWriterNG;

/**
 * An adapter, so that classes using map readers and writers don't have to
 * change whenever the map reader or writer is replaced.
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
		reader = new MapReaderNG();
		writer = new MapWriterNG();
	}

	/**
	 * @param filename the file to open
	 * @param warner the Warning instance to use for warnings.
	 * @return the map it contains
	 * @throws IOException on I/O error opening the file
	 * @throws XMLStreamException if the XML is badly formed
	 * @throws SPFormatException if there are map format errors
	 * @throws MapVersionException if the reader can't handle this map version
	 */
	public MapView readMap(final String filename, final Warning warner)
			throws IOException, XMLStreamException, SPFormatException,
			MapVersionException {
		return reader.readMap(filename, warner);
	}

	/**
	 * Write a map.
	 *
	 * @param filename the file to write to
	 * @param map the map to write.
	 * @throws IOException on error opening the file
	 */
	public void write(final String filename, final IMap map) throws IOException {
		writer.write(filename, map, true);
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "MapReaderAdapter";
	}
}
