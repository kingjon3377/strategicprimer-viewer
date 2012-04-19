package controller.map.readerng;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import model.map.PlayerCollection;
import model.map.SPMap;
import util.IteratorWrapper;
import util.Warning;
import controller.map.IMapReader;
import controller.map.MapVersionException;
import controller.map.SPFormatException;
import controller.map.simplexml.ISPReader;

/**
 * An XML-map reader that calls a tree of per-node XML readers, similar to the
 * SimpleXMLReader but allowing for more complex types that don't map 1:1 to the
 * XML.
 *
 * TODO: tests
 * 
 * @author Jonathan Lovelace
 */
public class MapReaderNG implements IMapReader, ISPReader {
	/**
	 * @param file the name of a file
	 * @param warner the Warning instance to use for warnings.
	 * @return the map contained in that file
	 * @throws IOException on I/O error
	 * @throws XMLStreamException if the XML isn't well-formed
	 * @throws SPFormatException if the data is invalid
	 * @throws MapVersionException if the format isn't one we support
	 */
	@Override
	public SPMap readMap(final String file, final Warning warner) throws IOException, XMLStreamException,
			SPFormatException, MapVersionException {
		final FileReader istream = new FileReader(file);
		try {
			return readMap(istream, warner);
		} finally {
			istream.close();
		}
	}
	/**
	 * 
	 * @param istream a reader from which to read the XML
	 * @param warner the Warning instance to use for warnings.
	 * @return the map contained in that stream
	 * @throws XMLStreamException if the XML isn't well-formed
	 * @throws SPFormatException if the data is invalid
	 * @throws MapVersionException if the map version isn't one we support
	 */
	@Override
	public SPMap readMap(final Reader istream, final Warning warner) throws XMLStreamException,
			SPFormatException, MapVersionException {
		return readXML(istream, SPMap.class, warner);
	}
	/**
	 * @param <T> The type of the object the XML represents
	 * @param istream
	 *            a reader from which to read the XML
	 * @param type The type of the object the XML represents
	 * @param warner a Warning instance to use for warnings
	 * @return the object contained in that stream
	 * @throws XMLStreamException
	 *             if XML isn't well-formed.
	 * @throws SPFormatException
	 *             if the data is invalid.
	 */
	@Override
	public <T> T readXML(final Reader istream, final Class<T> type, final Warning warner)
			throws XMLStreamException, SPFormatException {
		final IteratorWrapper<XMLEvent> eventReader = new IteratorWrapper<XMLEvent>(
				XMLInputFactory.newInstance().createXMLEventReader(istream));
		final PlayerCollection dummyPlayers = new PlayerCollection();
		for (XMLEvent event : eventReader) {
			if (event.isStartElement()) {
				return ReaderFactory.createReader(type).parse(
						event.asStartElement(), eventReader,
						dummyPlayers, warner);
			}
		}
		throw new XMLStreamException("XML stream didn't contain a start element");
	}
	/**
	 * @param <T> The type of the object the XML represents
	 * @param istream
	 *            a reader from which to read the XML
	 * @param type The type of the object the XML represents
	 * @param warner a Warning instance to use for warnings
	 * @param reflection ignored
	 * @return the object contained in that stream
	 * @throws XMLStreamException
	 *             if XML isn't well-formed.
	 * @throws SPFormatException
	 *             if the data is invalid.
	 */
	@Override
	public <T> T readXML(final Reader istream, final Class<T> type, final boolean reflection,
			final Warning warner) throws XMLStreamException, SPFormatException {
		return readXML(istream, type, warner);
	}
}
