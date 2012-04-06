package controller.map.readerng;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import model.map.SPMap;
import util.IteratorWrapper;
import controller.map.IMapReader;
import controller.map.MapVersionException;
import controller.map.SPFormatException;

/**
 * An XML-map reader that calls a tree of per-node XML readers, similar to the
 * SimpleXMLReader but allowing for more complex types that don't map 1:1 to the
 * XML.
 *
 * TODO: tests
 * 
 * @author Jonathan Lovelace
 */
public class MapReaderNG implements IMapReader {
	/**
	 * @param file the name of a file
	 * @return the map contained in that file
	 * @throws IOException on I/O error
	 * @throws XMLStreamException if the XML isn't well-formed
	 * @throws SPFormatException if the data is invalid
	 * @throws MapVersionException if the format isn't one we support
	 */
	@Override
	public SPMap readMap(final String file) throws IOException, XMLStreamException,
			SPFormatException, MapVersionException {
		final FileReader istream = new FileReader(file);
		try {
			return readMap(istream);
		} finally {
			istream.close();
		}
	}
	/**
	 * 
	 * @param istream a reader from which to read the XML
	 * @return the map contained in that stream
	 * @throws XMLStreamException if the XML isn't well-formed
	 * @throws SPFormatException if the data is invalid
	 * @throws MapVersionException if the map version isn't one we support
	 */
	@Override
	public SPMap readMap(final Reader istream) throws XMLStreamException,
			SPFormatException, MapVersionException {
		final IteratorWrapper<XMLEvent> eventReader = new IteratorWrapper<XMLEvent>(
				XMLInputFactory.newInstance().createXMLEventReader(istream));
		for (XMLEvent event : eventReader) {
			if (event.isStartElement()) {
				return ReaderFactory.createReader(SPMap.class).parse(event.asStartElement(), eventReader);
			}
		}
		throw new XMLStreamException("XML stream didn't contain a start element");
	}
}
