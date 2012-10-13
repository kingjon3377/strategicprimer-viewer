package controller.map.cxml;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import model.map.IMap;
import model.map.XMLWritable;
import controller.map.SPWriter;
/**
 * CompactXML's Writer implementation.
 * @author Jonathan Lovelace
 *
 */
public class CompactXMLWriter implements SPWriter {
	/**
	 * Write a map to file.
	 * @param filename The file to write to
	 * @param map the map to write
	 * @throws IOException on I/O error
	 */
	@Override
	public void write(final String filename, final IMap map)
			throws IOException {
		writeObject(filename, map);
	}
	/**
	 * Write a map to a stream.
	 * @param out the stream to write to
	 * @param map the map to write
	 * @throws IOException on I/O error
	 */
	@Override
	public void write(final Writer out, final IMap map)
			throws IOException {
		writeObject(out, map);
	}
	/**
	 * Write an object to file.
	 * @param filename the file to write to
	 * @param obj the object to write
	 * @throws IOException on I/O error
	 */
	public void writeObject(final String filename, final XMLWritable obj) throws IOException {
		final Writer writer = new FileWriter(filename);
		try {
			writeObject(writer, obj);
		} finally {
			writer.close();
		}
	}
	/**
	 * Write an object to a stream.
	 *
	 * @param out the stream to write to
	 * @param obj the object to write
	 * @throws IOException on I/O error
	 */
	public void writeObject(final Writer out, final XMLWritable obj) throws IOException {
		CompactReaderAdapter.ADAPTER.write(out, obj, 0);
	}
}
