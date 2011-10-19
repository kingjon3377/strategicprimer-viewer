package controller.map.stax;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.map.Player;
import model.map.SPMap;
import util.IteratorWrapper;
import controller.map.IMapReader;
import controller.map.MapVersionException;

/**
 * A StAX implementation of a map parser. The annoyance of extending the SAX
 * version outweighs the difficulty of writing a new parser.
 * 
 * @author Jonathan Lovelace
 * 
 */
@Deprecated
public class MapReader implements IMapReader {
	/**
	 * Error message for unexpected tag.
	 */
	private static final String UNEXPECTED_TAG = "Unexpected tag ";
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(MapReader.class
			.getName());
	/**
	 * A helper to read tiles and their contents.
	 */
	private final TileReader tileReader;
	/**
	 * A helper for methods we need to share with TileReader and similar
	 * classes.
	 */
	private final XMLHelper helper;

	/**
	 * Constructor.
	 */
	public MapReader() {
		helper = new XMLHelper();
		tileReader = new TileReader(helper);
	}

	/**
	 * @param file
	 *            the file to read from
	 * @return the map contained in that file
	 * @throws XMLStreamException
	 *             on badly-formed XML or other processing error
	 * @throws IOException
	 *             if file not found or on other I/O error, e.g. while closing
	 *             the stream.
	 * @throws MapVersionException
	 *             if the map is too old a version
	 */
	@Override
	public SPMap readMap(final String file) throws XMLStreamException,
			IOException, MapVersionException {
		final FileInputStream istream = new FileInputStream(file);
		try {
			return readMap(istream);
		} finally {
			istream.close();
		}
	}

	/**
	 * @param istream
	 *            the stream to read from
	 * @return the map contained in the data in the stream
	 * @throws XMLStreamException
	 *             on badly-formed XML or other processing error
	 * @throws MapVersionException
	 *             if the map is too old a version
	 */
	@Override
	public SPMap readMap(final InputStream istream) throws XMLStreamException,
			MapVersionException {
		try {
			return readMapImpl(istream);
		} finally {
			try {
				istream.close();
			} catch (final IOException e) {
				LOGGER.log(Level.WARNING, "I/O error closing the input stream",
						e);
			}
		}
	}

	/**
	 * @param istream the stream to read from
	 * @return the map
	 * @throws XMLStreamException on XML error
	 * @throws MapVersionException if the map version is one we can't handle
	 */
	private SPMap readMapImpl(final InputStream istream)
			throws XMLStreamException, MapVersionException {
		LOGGER.info("Started reading XML");
		LOGGER.info(Long.toString(System.currentTimeMillis()));
		SPMap map = null;
		@SuppressWarnings("unchecked")
		final IteratorWrapper<XMLEvent> eventReader = new IteratorWrapper<XMLEvent>(
				XMLInputFactory.newInstance().createXMLEventReader(istream));
		for (XMLEvent event : eventReader) {
			if (event.isStartElement()) {
				final StartElement startElement = event.asStartElement();
				if (map == null) {
					map = firstTag(startElement);
					continue;
				} 
					switch (XMLHelper.getTagType(startElement)) {
					case Player:
						map.addPlayer(parsePlayer(startElement, eventReader));
						break;
					case Row:
						// Deliberately ignore
						continue;
					case Tile:
						map.addTile(tileReader.parseTileAndContents(
								startElement, eventReader, map.getPlayers()));
						break;
					default:
					throw new IllegalStateException(
							UNEXPECTED_TAG
									+ startElement.getName().getLocalPart()
									+ ": players, rows, and tiles are the only accepted top-level tags");
					}
			}
		}
		LOGGER.info("Finished reading XML");
		LOGGER.info(Long.toString(System.currentTimeMillis()));
		return map;
	}

	/**
	 * Handle the first tag, which should be the map tag.
	 * 
	 * @param startElement
	 *            The current element.
	 * @return The map.
	 * @throws MapVersionException
	 *             If the map version is too old.
	 */
	private SPMap firstTag(final StartElement startElement)
			throws MapVersionException {
		if (Tag.Map.equals(XMLHelper.getTagType(startElement))) {
			if (Integer.parseInt(helper.getAttributeWithDefault(startElement,
					"version", "-1")) >= SPMap.VERSION) {
				return new SPMap(Integer.parseInt(helper.getAttribute(// NOPMD
						startElement, "rows")), Integer.parseInt(helper
						.getAttribute(startElement, "columns"))); // NOPMD
			} else {
				throw new MapVersionException("Map is too old a version.");
			}
		} else {
			throw new IllegalStateException("Has to start with a map tag");
		}
	}

	/**
	 * Parse a player from a player tag.
	 * 
	 * @param element
	 *            The tag.
	 * @param reader
	 *            the stream of elements we're reading from
	 * @return The player it encapsulates.
	 */
	private static Player parsePlayer(final StartElement element,
			final Iterable<XMLEvent> reader) {
		for (XMLEvent event : reader) {
			if (event.isStartElement()) {
				throw new IllegalStateException(UNEXPECTED_TAG
						+ event.asStartElement().getName().getLocalPart()
						+ ": a player can't contain anything yet");
			} else if (event.isEndElement()) {
				break;
			}
		}
		return new Player(Integer.parseInt(element.getAttributeByName(
				new QName("number")).getValue()), element.getAttributeByName(
				new QName("code_name")).getValue());
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "MapReader";
	}

}
