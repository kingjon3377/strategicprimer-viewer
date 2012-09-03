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
	 * @param inclusion whether to use inclusion
	 * @throws IOException on I/O error
	 */
	@Override
	public void write(final String filename, final IMap map, final boolean inclusion)
			throws IOException {
		writeObject(filename, map, inclusion);
	}
	/**
	 * Write a map to a stream.
	 * @param out the stream to write to
	 * @param map the map to write
	 * @param inclusion whether to use inclusion
	 * @throws IOException on I/O error
	 */
	@Override
	public void write(final Writer out, final IMap map, final boolean inclusion)
			throws IOException {
		writeObject(out, map, inclusion);
	}
	/**
	 * Write an object to file.
	 * @param filename the file to write to
	 * @param obj the object to write
	 * @param inclusion whether to write to other files if sub-objects came from 'include' tags
	 * @throws IOException on I/O error
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
	 * Write an object to a stream.
	 *
	 * @param out the stream to write to
	 * @param obj the object to write
	 * @param inclusion whether to write to other files if sub-objects came from
	 *        'include' tags.
	 * @throws IOException on I/O error
	 */
	public void writeObject(final Writer out, final XMLWritable obj, final boolean inclusion) throws IOException {
		CompactReaderAdapter.ADAPTER.write(out, obj, obj.getFile(), inclusion, 0);
	}
}
