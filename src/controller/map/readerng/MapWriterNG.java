package controller.map.readerng;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import model.map.IMap;
import controller.map.iointerfaces.SPWriter;

/**
 * Entry point for the new map writing framework.
 *
 * @author Jonathan Lovelace
 * @deprecated ReaderNG is deprecated
 */
@Deprecated
public class MapWriterNG implements SPWriter {
	/**
	 * Write a map.
	 *
	 * @param filename the file to write to
	 * @param map the map to write.
	 * @throws IOException on error opening the file
	 */
	@Override
	public void write(final String filename, final IMap map) throws IOException {
		writeObject(filename, map);
	}

	/**
	 * Write a map.
	 *
	 * @param out the Writer to write to
	 * @param map the map to write.
	 * @throws IOException on I/O error in writing
	 */
	@Override
	public void write(final Writer out, final IMap map) throws IOException {
		writeObject(out, map);
	}

	/**
	 * Write a SP object.
	 *
	 * @param filename the file to write to
	 * @param obj the object to write.
	 * @throws IOException on error opening the file
	 */
	// ESCA-JAVA0173: The filename parameter is *too* used.
	public static void writeObject(final String filename, final Object obj)
			throws IOException {
		try (final Writer writer = new FileWriter(filename)) {
			writeObject(writer, obj);
		}
	}

	/**
	 * Write a SP object.
	 *
	 * @param out the Writer to write to
	 * @param obj the object to write.
	 * @throws IOException on I/O error in writing
	 */
	public static void writeObject(final Writer out, final Object obj)
			throws IOException {
		ReaderAdapter.ADAPTER.write(obj).write(out, 0);
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "MapWriterNG";
	}
}
