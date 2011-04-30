package controller.map;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.viewer.Fortress;
import model.viewer.Player;
import model.viewer.River;
import model.viewer.SPMap;
import model.viewer.Tile;
import model.viewer.TileType;
import model.viewer.Unit;

import org.xml.sax.SAXException;

/**
 * A StAX implementation of a map parser. The annoyance of extending the SAX
 * version outweighs the difficulty of writing a new parser.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class MapReader {
	private static final String UNEXPECTED_TAG = "Unexpected tag ";
	private static final Logger LOGGER = Logger.getLogger(MapReader.class.getName());
	/**
	 * @param file
	 *            the file to read from
	 * @return the map contained in that file
	 * @throws FileNotFoundException
	 *             if file not found
	 * @throws XMLStreamException
	 *             on badly-formed XML or other processing error
	 */
	public SPMap readMap(final String file) throws FileNotFoundException,
			XMLStreamException {
		return readMap(new FileInputStream(file));
	}

	/**
	 * An enumerated type for the tags we know about.
	 */
	private enum Tag {
		/**
		 * The main map tag
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
		 * Lakes: internally a special case of rivers, but we want a simpler XML tile for them.
		 */
		Lake("lake"),
		/**
		 * Anything not enumerated.
		 */
		Unknown("unknown");
		/**
		 * The text version of the tag.
		 */
		private String text;

		/**
		 * Constructor
		 * 
		 * @param _text
		 *            The string to associate with the tag.
		 */
		Tag(final String _text) {
			text = _text;
		}

		/**
		 * @return the string associated with the tag.
		 * @return
		 */
		public String getText() {
			return text;
		}

		/**
		 * @param _text
		 *            a string
		 * @return the tag that represents that string, if any, or Unknown if
		 *         none.
		 */
		public static Tag fromString(final String _text) {
			Tag retval = Unknown;
			if (_text != null) {
				for (Tag tag : Tag.values()) {
					if (_text.equalsIgnoreCase(tag.text)) {
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
	 */
	public SPMap readMap(final InputStream istream) throws XMLStreamException {
		LOGGER.info("Started reading XML");
		LOGGER.info(Long.toString(System.currentTimeMillis()));
		SPMap map = null;
		final XMLEventReader eventReader = XMLInputFactory.newInstance()
				.createXMLEventReader(istream);
		while (eventReader.hasNext()) {
			final XMLEvent event = eventReader.nextEvent();
			if (event.isStartElement()) {
				final StartElement startElement = event.asStartElement();
				final Tag tag = getTagType(startElement);
				if (map == null) {
					if (Tag.Map.equals(tag)) {
						map = new SPMap(Integer.parseInt(getAttribute( // NOPMD
								startElement, "rows")),
								Integer.parseInt(getAttribute(startElement,
										"columns"))); // NOPMD
					} else {
						throw new IllegalStateException(
								"Has to start with a map tag");
					}
				} else if (Tag.Player.equals(tag)) {
					map.addPlayer(parsePlayer(startElement, eventReader));
				} else if (Tag.Row.equals(tag)) {
					// Deliberately ignore
					continue;
				} else if (Tag.Tile.equals(tag)) {
					map.addTile(parseTile(startElement, eventReader));
				} else {
					throw new IllegalStateException(
							UNEXPECTED_TAG
									+ tag.getText()
									+ ": players, rows, and tiles are the only accepted top-level tags");
				}
			}
		}
		LOGGER.info("Finished reading XML");
		LOGGER.info(Long.toString(System.currentTimeMillis()));
		eventReader.close();
		try {
			istream.close();
		} catch (IOException e) {
			LOGGER.log(Level.WARNING, "I/O error closing the input stream", e);
		}
		return map;
	}

	/**
	 * Parse a tile.
	 * 
	 * @param element
	 *            the tile tag itself
	 * @param reader
	 *            the stream of elements we're reading from
	 * @return the tile in question.
	 * @throws XMLStreamException
	 *             on badly-formed XML or other processing error
	 */
	private static Tile parseTile(final StartElement element,
			final XMLEventReader reader) throws XMLStreamException {
		final Tile tile = getAttribute(element, "event") == null ? new Tile(
				Integer.parseInt(getAttribute(element, "row")),
				Integer.parseInt(getAttribute(element, "column")),
				TileType.getTileType(getAttribute(element, "type")))
				: new Tile(Integer.parseInt(getAttribute(element, "row")),
						Integer.parseInt(getAttribute(element, "column")),
						TileType.getTileType(getAttribute(element, "type")),
						Integer.parseInt(getAttribute(element, "event")));
		while (reader.hasNext()) {
			if (reader.peek().isStartElement()) {
				final StartElement elem = reader.nextEvent().asStartElement();
				final Tag tag = getTagType(elem);
				if (Tag.Fortress.equals(tag)) {
					tile.addFort(parseFortress(tile, elem, reader));
				} else if (Tag.Unit.equals(tag)) {
					tile.addUnit(parseUnit(tile, elem, reader));
				} else if (Tag.River.equals(tag) || Tag.Lake.equals(tag)) {
					tile.addRiver(parseRiver(elem, reader));
				} else {
					throw new IllegalStateException(
							"Unexpected "
									+ tag.getText()
									+ " tag: only fortresses, units, and rivers can be inside a tile");
				}
			} else if (reader.nextEvent().isEndElement()) {
				break;
			}
		}
		return tile;
	}

	/**
	 * Parse a river. This is its own method so we can deal with the closing
	 * tag, and for extensibility.
	 * 
	 * @param elem
	 *            the river tag itself
	 * @param reader
	 *            the stream of elements we're reading from
	 * @return the river in question.
	 * @throws XMLStreamException
	 *             on badly-formed XML or other processing error
	 */
	public static River parseRiver(final StartElement elem,
			final XMLEventReader reader) throws XMLStreamException {
		// ESCA-JAVA0177:
		final River river; // NOPMD
		if (Tag.Lake.equals(getTagType(elem))) {
			river = River.Lake;
		} else {
			river = River.getRiver(getAttribute(elem, "direction"));
		}
		while (reader.hasNext()) {
			if (reader.peek().isStartElement()) {
				final StartElement element = reader.nextEvent().asStartElement();
				throw new IllegalStateException(UNEXPECTED_TAG
						+ getTagType(element).getText()
						+ ": a river can't contain anything yet");
			} else if (reader.nextEvent().isEndElement()) {
				break;
			}
		}
		return river;
	}

	/**
	 * Parse a fortress
	 * 
	 * @param tile
	 *            the tile the fortress is on
	 * @param elem
	 *            the fortress tag itself
	 * @param reader
	 *            the stream of elements we're reading from
	 * @return the fortress in question.
	 * @throws XMLStreamException
	 *             on badly-formed XML or other processing error
	 */
	private static Fortress parseFortress(final Tile tile,
			final StartElement elem, final XMLEventReader reader)
			throws XMLStreamException {
		final Fortress fort = new Fortress(tile, Integer.parseInt(getAttribute(
				elem, "owner")), getAttribute(elem, "name"));
		while (reader.hasNext()) {
			if (reader.peek().isStartElement()) {
				final StartElement element = reader.nextEvent()
						.asStartElement();
				if (Tag.Unit.equals(getTagType(element))) {
					fort.addUnit(parseUnit(tile, elem, reader));
				} else {
					throw new IllegalStateException(UNEXPECTED_TAG
							+ getTagType(element).getText()
							+ ": a fortress can only contain units");
				}
			} else if (reader.nextEvent().isEndElement()) {
				break;
			}
		}
		return fort;
	}

	/**
	 * Parse a unit TODO: Soon there'll be tags nested inside units; we should
	 * handle them. And enforce the no-nested-units rule.
	 * 
	 * @param tile
	 *            the tile the unit is on
	 * @param elem
	 *            the unit tag
	 * @param reader
	 *            the stream of elements we're reading from
	 * @return the fortress in question.
	 * @throws XMLStreamException
	 *             on ill-formed XML or other processing problem
	 * 
	 */
	private static Unit parseUnit(final Tile tile, final StartElement elem,
			final XMLEventReader reader) throws XMLStreamException {
		final Unit unit = new Unit(tile, Integer.parseInt(getAttribute(elem,
				"owner")), getAttribute(elem, "type"), getAttribute(elem,
				"name"));
		while (reader.hasNext()) {
			if (reader.peek().isStartElement()) {
				final StartElement element = reader.nextEvent()
						.asStartElement();
				throw new IllegalStateException(UNEXPECTED_TAG
						+ getTagType(element).getText()
						+ ": a unit can't contain anything yet");
			} else if (reader.nextEvent().isEndElement()) {
				break;
			}
		}
		return unit;
	}

	/**
	 * @param startElement
	 *            a tag
	 * @param attribute
	 *            the attribute we want
	 * @return the value of that attribute.
	 */
	private static String getAttribute(final StartElement startElement,
			final String attribute) {
		final Attribute attr = startElement.getAttributeByName(new QName(
				attribute));
		return attr == null ? null : attr.getValue();
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
				final StartElement elem = reader.nextEvent().asStartElement();
				throw new IllegalStateException(UNEXPECTED_TAG
						+ getTagType(elem).getText()
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
	private static Tag getTagType(final StartElement startElement) {
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
		final PrintWriter out = new PrintWriter(new OutputStreamWriter(System.out));
		for (String arg : args) {
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
			} catch (SAXException e) {
				LOGGER.log(Level.SEVERE, "SAX exception when parsing " + arg, e);
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, "I/O error while parsing " + arg, e);
			} catch (XMLStreamException e) {
				LOGGER.log(Level.SEVERE,
						"XMLStreamException (probably badly formed input) in "
								+ arg, e);
			} catch (NumberFormatException e) {
				LOGGER.log(Level.SEVERE,
						"Non-numeric when numeric data expected in " + arg, e);
			} catch (NullPointerException e) { // NOPMD
				LOGGER.log(Level.SEVERE, "Null pointer in " + arg, e);
			} catch (IllegalStateException e) {
				LOGGER.log(Level.SEVERE, "Unexpected state in " + arg, e);
			}
		}
	}
}
