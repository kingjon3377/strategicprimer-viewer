package controller.map.readerng;

import java.io.IOException;
import java.io.Reader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import model.map.MapView;
import model.map.PlayerCollection;
import model.map.SPMap;
import model.map.XMLWritable;
import util.IteratorWrapper;
import util.Warning;
import controller.map.IMapReader;
import controller.map.ISPReader;
import controller.map.MapVersionException;
import controller.map.SPFormatException;
import controller.map.misc.FileOpener;
import controller.map.misc.IDFactory;
import controller.map.misc.IncludingIterator;

/**
 * An XML-map reader that calls a tree of per-node XML readers, similar to the
 * SimpleXMLReader but allowing for more complex types that don't map 1:1 to the
 * XML.
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
	public MapView readMap(final String file, final Warning warner) throws IOException, XMLStreamException,
			SPFormatException, MapVersionException {
		final Reader istream = new FileOpener().createReader(file);
		try {
			return readMap(file, istream, warner);
		} finally {
			istream.close();
		}
	}
	/**
	 * 
	 * @param file the name of the file being read from
	 * @param istream a reader from which to read the XML
	 * @param warner the Warning instance to use for warnings.
	 * @return the map contained in that stream
	 * @throws XMLStreamException if the XML isn't well-formed
	 * @throws SPFormatException if the data is invalid
	 * @throws MapVersionException if the map version isn't one we support
	 */
	@Override
	public MapView readMap(final String file, final Reader istream, final Warning warner) throws XMLStreamException,
			SPFormatException, MapVersionException {
		return readXML(file, istream, MapView.class, warner);
	}
	/**
	 * @param <T> The type of the object the XML represents
	 * @param file the name of the file from which we're reading
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
	public <T> T readXML(final String file, final Reader istream, final Class<T> type, final Warning warner)
			throws XMLStreamException, SPFormatException {
		final IteratorWrapper<XMLEvent> eventReader = new IteratorWrapper<XMLEvent>(
				new IncludingIterator(file, XMLInputFactory.newInstance().createXMLEventReader(istream)));
		for (XMLEvent event : eventReader) {
			if (event.isStartElement()) {
				final XMLWritable retval = new ReaderAdapter().parse(//NOPMD
						event.asStartElement(), eventReader,
						new PlayerCollection(), warner, new IDFactory()); // NOPMD
				return checkType(retval, type);
			}
		}
		throw new XMLStreamException("XML stream didn't contain a start element");
	}
	
	/**
	 * Helper method: check that an XMLWritable is in fact assignable to the
	 * specified type. Return it if so; if not throw IllegalArgumentException.
	 * 
	 * @param <T>
	 *            the type to check the object against.
	 * @param obj
	 *            the object to check
	 * @param type
	 *            the type to check it against.
	 * @return the object, if it matches the desired type.
	 */
	@SuppressWarnings("unchecked")
	private static <T> T checkType(final XMLWritable obj, final Class<T> type) {
		if (type.isAssignableFrom(obj.getClass())) {
			return (T) obj; // NOPMD
		} else if (type.equals(MapView.class) && obj instanceof SPMap) {
			return (T) new MapView((SPMap) obj, ((SPMap) obj).getPlayers()
					.getCurrentPlayer().getPlayerId(), 0);
		} else {
			throw new IllegalArgumentException(
					"We want a node producing " + type.getSimpleName()
							+ ", not "
							+ obj.getClass().getSimpleName()
							+ ", as the top-level tag");
		}
	}
	
	/**
	 * @param <T> The type of the object the XML represents
	 * @param file the name of the file being read from
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
	public <T> T readXML(final String file, final Reader istream, final Class<T> type, final boolean reflection,
			final Warning warner) throws XMLStreamException, SPFormatException {
		return readXML(file, istream, type, warner);
	}
}
