package controller.map.readerng;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import model.map.IMap;
import model.map.XMLWritable;
import controller.map.SPWriter;

/**
 * Entry point for the new map writing framework.
 *
 * @author Jonathan Lovelace
 *
 */
public class MapWriterNG implements SPWriter {
	/**
	 * Write a map.
	 *
	 * @param filename the file to write to
	 * @param map the map to write.
	 * @param inclusion whether to write to other files if sub-objects came from
	 *        'include' tags.
	 * @throws IOException on error opening the file
	 */
	@Override
	public void write(final String filename, final IMap map,
			final boolean inclusion) throws IOException {
		writeObject(filename, map, inclusion);
	}

	/**
	 * Write a map.
	 *
	 * @param out the Writer to write to
	 * @param map the map to write.
	 * @param inclusion whether to write to other files if sub-objects came from
	 *        'include' tags.
	 * @throws IOException on I/O error in writing
	 */
	@Override
	public void write(final Writer out, final IMap map, final boolean inclusion)
			throws IOException {
		writeObject(out, map, inclusion);
	}

	/**
	 * Write a SP object.
	 *
	 * @param filename the file to write to
	 * @param obj the object to write.
	 * @param inclusion whether to write to other files if sub-objects came from
	 *        'include' tags.
	 * @throws IOException on error opening the file
	 */
	public void writeObject(final String filename, final XMLWritable obj,
			final boolean inclusion) throws IOException {
		final Writer writer = new FileWriter(filename);
		try {
			writeObject(writer, obj, inclusion);
		} finally {
			writer.close();
		}
	}

	/**
	 * Write a SP object.
	 *
	 * @param out the Writer to write to
	 * @param obj the object to write.
	 * @param inclusion whether to write to other files if sub-objects came from
	 *        'include' tags.
	 * @throws IOException on I/O error in writing
	 */
	public void writeObject(final Writer out, final XMLWritable obj,
			final boolean inclusion) throws IOException {
		ReaderAdapter.ADAPTER.write(obj).write(out, inclusion, 0);
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "MapWriterNG";
	}
}
