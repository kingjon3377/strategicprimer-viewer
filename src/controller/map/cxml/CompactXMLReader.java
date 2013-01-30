package controller.map.cxml;

import java.io.IOException;
import java.io.Reader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import model.map.IMap;
import model.map.MapView;
import model.map.PlayerCollection;
import model.map.SPMap;
import model.map.XMLWritable;
import util.IteratorWrapper;
import util.Warning;
import controller.map.formatexceptions.SPFormatException;
import controller.map.io_interfaces.IMapReader;
import controller.map.io_interfaces.ISPReader;
import controller.map.misc.FileOpener;
import controller.map.misc.IDFactory;
import controller.map.misc.IncludingIterator;
/**
 * Fourth-generation SP XML reader.
 * @author Jonathan Lovelace
 *
 */
public class CompactXMLReader implements IMapReader, ISPReader {
	/**
	 * @param <T> A supertype of the object the XML represents
	 * @param file the file we're reading from
	 * @param istream the stream to read from
	 * @param type the type of the object the caller wants
	 * @param warner the Warning instance to use for warnings
	 * @return the wanted object
	 * @throws XMLStreamException if the XML isn't well-formed
	 * @throws SPFormatException on SP XML format error
	 */
	@Override
	public <T extends XMLWritable> T readXML(final String file, final Reader istream,
			final Class<T> type, final Warning warner) throws XMLStreamException,
			SPFormatException {
		final IteratorWrapper<XMLEvent> eventReader = new IteratorWrapper<XMLEvent>(
				new IncludingIterator(file, XMLInputFactory.newInstance()
						.createXMLEventReader(istream)));
		final PlayerCollection players = new PlayerCollection();
		final IDFactory idFactory = new IDFactory();
		for (final XMLEvent event : eventReader) {
			if (event.isStartElement()) {
				final T retval = CompactReaderAdapter.ADAPTER.parse(type,
					event.asStartElement(), eventReader,
					players, warner, idFactory);
				return retval;
			}
		}
		throw new XMLStreamException(
				"XML stream didn't contain a start element");
	}

	/**
	 * @param file the file to read from
	 * @param warner a Warning instance to use for warnings
	 * @return the map contained in the file
	 * @throws IOException on I/O error
	 * @throws XMLStreamException on badly-formed XML
	 * @throws SPFormatException on SP format problems
	 */
	@Override
	public MapView readMap(final String file, final Warning warner) throws IOException,
			XMLStreamException, SPFormatException {
		final Reader istream = new FileOpener().createReader(file);
		try {
			return readMap(file, istream, warner);
		} finally {
			istream.close();
		}
	}

	/**
	 * @param file the file we're reading from
	 * @param istream the stream to read from
	 * @param warner a Warning instance to use for warnings
	 * @return the map contained in the file
	 * @throws XMLStreamException on badly-formed XML
	 * @throws SPFormatException on SP format problems
	 */
	@Override
	public MapView readMap(final String file, final Reader istream, final Warning warner)
			throws XMLStreamException, SPFormatException {
		final IMap retval = readXML(file, istream, MapView.class, warner);
		return retval instanceof SPMap ? new MapView((SPMap) retval, retval.getPlayers()
				.getCurrentPlayer().getPlayerId(), 0) : (MapView) retval;
	}

}
