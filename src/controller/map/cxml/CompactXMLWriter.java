package controller.map.cxml;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import model.map.IMap;
import controller.map.iointerfaces.SPWriter;

/**
 * CompactXML's Writer implementation.
 *
 * @author Jonathan Lovelace
 *
 */
public class CompactXMLWriter implements SPWriter {
	/**
	 * Write a map to file.
	 *
	 * @param filename The file to write to
	 * @param map the map to write
	 * @throws IOException on I/O error
	 */
	@Override
	public void write(final String filename, final IMap map) throws IOException {
		writeObject(filename, map);
	}

	/**
	 * Write a map to a stream.
	 *
	 * @param ostream the stream to write to
	 * @param map the map to write
	 * @throws IOException on I/O error
	 */
	@Override
	public void write(final Writer ostream, final IMap map) throws IOException {
		writeObject(ostream, map);
	}

	/**
	 * Write an object to file.
	 *
	 * @param filename the file to write to
	 * @param obj the object to write
	 * @throws IOException on I/O error
	 */
	// ESCA-JAVA0173: The filename parameter is *too* used.
	public static void writeObject(final String filename, final Object obj)
			throws IOException {
		try (final Writer writer = new FileWriter(filename)) {
			writeObject(writer, obj);
		}
	}

	/**
	 * Write an object to a stream.
	 *
	 * @param ostream the stream to write to
	 * @param obj the object to write
	 * @throws IOException on I/O error
	 */
	public static void writeObject(final Writer ostream, final Object obj)
			throws IOException {
		CompactReaderAdapter.write(ostream, obj, 0);
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "CompactXMLWriter";
	}
}
