package controller.map.misc;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.stream.XMLStreamException;

import model.map.IMap;
import model.map.MapView;
import model.map.XMLWritable;
import util.Warning;
import controller.map.cxml.CompactXMLReader;
import controller.map.cxml.CompactXMLWriter;
import controller.map.formatexceptions.MapVersionException;
import controller.map.formatexceptions.SPFormatException;
import controller.map.iointerfaces.IMapReader;
import controller.map.iointerfaces.ISPReader;
import controller.map.iointerfaces.SPWriter;

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
	 * The SP-reader implementation.
	 */
	private final ISPReader spReader;
	/**
	 * The map writer implementation we use under the hood.
	 */
	private final SPWriter writer;

	/**
	 * Constructor.
	 */
	public MapReaderAdapter() {
		final CompactXMLReader impl = new CompactXMLReader();
		reader = impl;
		spReader = impl;
		writer = new CompactXMLWriter();
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
		writer.write(filename, map);
	}
	/**
	 * Read an arbitrary model object from a string.
	 * @param string the string to read from
	 * @return the model object contained in that string
	 * @throws SPFormatException on SP format error
	 * @throws XMLStreamException on XML error
	 */
	public XMLWritable readModelObject(final String string) throws XMLStreamException, SPFormatException {
		return spReader.readXML("string", new StringReader(string),
				XMLWritable.class, new Warning(Warning.Action.Ignore));
	}

	/**
	 * Write a model object to a string. FIXME: Add writeObject to some
	 * interface so we don't have to cast to a particular implementation.
	 *
	 * @param obj the object to write
	 * @return a string containing it.
	 * @throws IOException on I/O error in writing
	 */
	public String writeModelObject(final XMLWritable obj) throws IOException {
		final StringWriter out = new StringWriter();
		((CompactXMLWriter) writer).writeObject(out, obj);
		return out.toString();
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "MapReaderAdapter";
	}
}
