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
import controller.map.formatexceptions.MapVersionException;
import controller.map.formatexceptions.SPFormatException;
import controller.map.iointerfaces.IMapReader;
import controller.map.iointerfaces.ISPReader;
import controller.map.misc.FileOpener;
import controller.map.misc.IDFactory;
import controller.map.misc.IncludingIterator;

/**
 * An XML-map reader that calls a tree of per-node XML readers, similar to the
 * SimpleXMLReader but allowing for more complex types that don't map 1:1 to the
 * XML.
 *
 * @author Jonathan Lovelace
 * @deprecated ReaderNG is deprecated
 */
@Deprecated
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
	public MapView readMap(final String file, final Warning warner)
			throws IOException, XMLStreamException, SPFormatException,
			MapVersionException {
		final Reader istream = FileOpener.createReader(file);
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
	 * @throws SPFormatException if the data is invalid, including if the map
	 *         version isn't one we support
	 */
	@Override
	public MapView readMap(final String file, final Reader istream,
			final Warning warner) throws XMLStreamException, SPFormatException {
		return readXML(file, istream, MapView.class, warner);
	}

	/**
	 * @param <T> A supertype of the object the XML represents
	 * @param file the name of the file from which we're reading
	 * @param istream a reader from which to read the XML
	 * @param type The type of the object the XML represents
	 * @param warner a Warning instance to use for warnings
	 * @return the object contained in that stream
	 * @throws XMLStreamException if XML isn't well-formed.
	 * @throws SPFormatException if the data is invalid.
	 */
	@Override
	public <T extends XMLWritable> T readXML(final String file, final Reader istream,
			final Class<T> type, final Warning warner)
			throws XMLStreamException, SPFormatException {
		final IteratorWrapper<XMLEvent> eventReader = new IteratorWrapper<>(
				new IncludingIterator(file, XMLInputFactory.newInstance()
						.createXMLEventReader(istream)));
		for (final XMLEvent event : eventReader) {
			if (event.isStartElement()) {
				final XMLWritable retval = ReaderAdapter.ADAPTER.parse(
						// NOPMD
						event.asStartElement(), eventReader,
						new PlayerCollection(), warner, new IDFactory()); // NOPMD
				// This is a hack to make it compile under the new twoparameter system ...
				return checkType(retval, type);
			}
		}
		throw new XMLStreamException(
				"XML stream didn't contain a start element");
	}

	/**
	 * Helper method: check that an XMLWritable is in fact assignable to the
	 * specified type. Return it if so; if not throw IllegalArgumentException.
	 *
	 * @param <T> the type to check the object against.
	 * @param obj the object to check
	 * @param type the type to check it against.
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
			throw new IllegalArgumentException("We want a node producing "
					+ type.getSimpleName() + ", not "
					+ obj.getClass().getSimpleName() + ", as the top-level tag");
		}
	}

	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "MapReaderNG";
	}
}
