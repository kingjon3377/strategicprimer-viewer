package controller.map;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.viewer.Player;
import model.viewer.SPMap;

import org.xml.sax.SAXException;

/**
 * A StAX implementation of a map parser. The annoyance of extending the SAX
 * version outweighs the difficulty of writing a new parser.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class MapReader {
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
	 * A helper for methods we need to share with TileReader and similar classes.
	 */
	private final XMLHelper helper;
	/**
	 * Constructor.
	 */
	public MapReader() {
		helper = new XMLHelper();
		tileReader = new TileReader(this, helper);
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
	 * @throws MapVersionException if the map is too old a version
	 */
	public SPMap readMap(final String file) throws XMLStreamException,
			IOException, MapVersionException {
		final FileInputStream istream = new FileInputStream(file);
		final SPMap retval = readMap(istream);
		istream.close();
		return retval;
	}

	/**
	 * An enumerated type for the tags we know about.
	 */
	enum Tag {
		/**
		 * The main map tag.
		 */
		Map("map"),
		/**
		 * Rows: we actually ignore these tags.
		 */
		Row("row"),
		/**
		 * Tiles.
		 */
		Tile("tile"),
		/**
		 * Players.
		 */
		Player("player"),
		/**
		 * Fortresses.
		 */
		Fortress("fortress"),
		/**
		 * Units.
		 */
		Unit("unit"),
		/**
		 * Rivers.
		 */
		River("river"),
		/**
		 * Lakes: internally a special case of rivers, but we want a simpler XML
		 * tile for them.
		 */
		Lake("lake"),
		/**
		 * An Event. @see model.viewer.events.AbstractEvent
		 */
		Event("event"),
		/**
		 * A battlefield. @see model.viewer.events.BattlefieldEvent
		 */
		Battlefield("battlefield"),
		/**
		 * Cave. @see model.veiwer.events.CaveEvent
		 */
		Cave("cave"),
		/**
		 * City. @see model.viewer.events.CityEvent
		 */
		City("city"),
		/**
		 * Fortification: @see model.viewer.events.FortificationEvent FIXME: We
		 * want this to use the Fortress tag instead, eventually.
		 */
		Fortification("fortification"),
		/**
		 * Minerals. @see model.viewer.events.MineralEvent
		 */
		Mineral("mineral"),
		/**
		 * Stone. @see model.viewer.events.StoneEvent
		 */
		Stone("stone"),
		/**
		 * Town. @see model.viewer.events.TownEvent
		 */
		Town("town"),
		/**
		 * Anything not enumerated.
		 */
		Unknown("unknown");
		/**
		 * The text version of the tag.
		 */
		private final String text;

		/**
		 * Constructor.
		 * 
		 * @param tagText
		 *            The string to associate with the tag.
		 */
		Tag(final String tagText) {
			text = tagText;
		}

		/**
		 * @return the string associated with the tag.
		 */
		public String getText() {
			return text;
		}

		/**
		 * @param tagText
		 *            a string
		 * @return the tag that represents that string, if any, or Unknown if
		 *         none.
		 */
		public static Tag fromString(final String tagText) {
			Tag retval = Unknown;
			if (tagText != null) {
				for (final Tag tag : Tag.values()) {
					if (tagText.equalsIgnoreCase(tag.text)) {
						retval = tag;
						break;
					}
				}
			}
			return retval;
		}
	}

	/**
	 * @param istream
	 *            the stream to read from
	 * @return the map contained in the data in the stream
	 * @throws XMLStreamException
	 *             on badly-formed XML or other processing error
	 * @throws MapVersionException if the map is too old a version
	 */
	public SPMap readMap(final InputStream istream) throws XMLStreamException, MapVersionException {
		LOGGER.info("Started reading XML");
		LOGGER.info(Long.toString(System.currentTimeMillis()));
		SPMap map = null;
		final XMLEventReader eventReader = XMLInputFactory.newInstance()
				.createXMLEventReader(istream);
		while (eventReader.hasNext()) {
			final XMLEvent event = eventReader.nextEvent();
			if (event.isStartElement()) {
				final StartElement startElement = event.asStartElement();
				if (map == null) {
					map = firstTag(startElement);
				} else {
					switch (getTagType(startElement)) {
					case Player:
						map.addPlayer(parsePlayer(startElement, eventReader));
						break;
					case Row:
						// Deliberately ignore
						continue;
					case Tile:
						map.addTile(tileReader.parseTileContents(startElement, eventReader));
						break;
					default:
						throw new IllegalStateException(
								UNEXPECTED_TAG
										+ startElement.getName().getLocalPart()
										+ ": players, rows, and tiles are the only accepted top-level tags");
					}
				}
			}
		}
		LOGGER.info("Finished reading XML");
		LOGGER.info(Long.toString(System.currentTimeMillis()));
		eventReader.close();
		try {
			istream.close();
		} catch (final IOException e) {
			LOGGER.log(Level.WARNING, "I/O error closing the input stream", e);
		}
		return map;
	}

	/**
	 * Handle the first tag, which should be the map tag.
	 * @param startElement The current element.
	 * @return The map.
	 * @throws MapVersionException If the map version is too old.
	 */
	private SPMap firstTag(final StartElement startElement) throws MapVersionException {
		if (Tag.Map.equals(getTagType(startElement))) {
			if (Integer.parseInt(helper.getAttributeWithDefault(startElement,
					"version", "-1")) >= SPMap.VERSION) {
				return new SPMap(Integer.parseInt(helper.getAttribute(// NOPMD
						startElement, "rows")),
						Integer.parseInt(helper.getAttribute(startElement,
								"columns"))); // NOPMD
			} else {
				throw new MapVersionException(
						"Map is too old a version.");
			}
		} else {
			throw new IllegalStateException(
					"Has to start with a map tag");
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
	 * @throws XMLStreamException
	 *             on ill-formed XML or other processing problem
	 */
	private static Player parsePlayer(final StartElement element,
			final XMLEventReader reader) throws XMLStreamException {
		final Player player = new Player(Integer.parseInt(element
				.getAttributeByName(new QName("number")).getValue()), element
				.getAttributeByName(new QName("code_name")).getValue());
		while (reader.hasNext()) {
			if (reader.peek().isStartElement()) {
				throw new IllegalStateException(UNEXPECTED_TAG
						+ reader.nextEvent().asStartElement().getName().getLocalPart()
						+ ": a player can't contain anything yet");
			} else if (reader.nextEvent().isEndElement()) {
				break;
			}
		}
		return player;
	}

	/**
	 * Get the tag type of a tag.
	 * 
	 * @param startElement
	 *            the tag to identify
	 * @return the type of tag, in usable (enumerated) form.
	 */
	// ESCA-JAVA0130:
	Tag getTagType(final StartElement startElement) { // NOPMD
		return Tag.fromString(startElement.getName().getLocalPart());
	}

	/**
	 * Driver method to compare the results of this reader with those of the
	 * legacy reader.
	 * 
	 * @param args
	 *            The maps to test the two readers on.
	 */
	// ESCA-JAVA0266:
	public static void main(final String[] args) {
		final PrintWriter out = new PrintWriter(new OutputStreamWriter(
				System.out));
		for (final String arg : args) {
			try {
				final long startOne = System.nanoTime();
				final SPMap map1 = new XMLReader().getMap(arg); // NOPMD;
				final long endOne = System.nanoTime();
				final long startTwo = System.nanoTime();
				final SPMap map2 = new MapReader().readMap(arg); // NOPMD;
				final long endTwo = System.nanoTime();
				if (map1.equals(map2)) {
					out.println("Readers produce identical results");
				} else {
					out.println("Readers differ on " + arg);
				}
				out.println("Old method took " + (endOne - startOne)
						+ " time-units;");
				out.println("New method took " + (endTwo - startTwo)
						+ " time-units.");
			} catch (final SAXException e) {
				LOGGER.log(Level.SEVERE, "SAX exception when parsing " + arg, e);
			} catch (final IOException e) {
				LOGGER.log(Level.SEVERE, "I/O error while parsing " + arg, e);
			} catch (final XMLStreamException e) {
				LOGGER.log(Level.SEVERE,
						"XMLStreamException (probably badly formed input) in "
								+ arg, e);
			} catch (final NumberFormatException e) {
				LOGGER.log(Level.SEVERE,
						"Non-numeric when numeric data expected in " + arg, e);
			} catch (final IllegalStateException e) {
				LOGGER.log(Level.SEVERE, "Unexpected state in " + arg, e);
			} catch (MapVersionException e) {
				LOGGER.log(Level.SEVERE, "Map version too old for new-style reader");
			}
		}
		out.close();
	}
	/**
	 * An exception to throw when the map's version is too old.
	 */
	public static class MapVersionException extends Exception {
		/**
		 * Version UID for serialization.
		 */
		private static final long serialVersionUID = 7748965005262842245L;

		/**
		 * Constructor.
		 * @param message the message to show the user if this isn't caught.
		 */
		public MapVersionException(final String message) {
			super(message);
		}
	}
}
