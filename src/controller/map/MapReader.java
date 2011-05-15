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
import model.viewer.events.AbstractEvent;
import model.viewer.events.AbstractEvent.EventKind;
import model.viewer.events.AbstractEvent.TownSize;
import model.viewer.events.AbstractEvent.TownStatus;
import model.viewer.events.BattlefieldEvent;
import model.viewer.events.CaveEvent;
import model.viewer.events.CityEvent;
import model.viewer.events.FortificationEvent;
import model.viewer.events.MineralEvent;
import model.viewer.events.MineralEvent.MineralKind;
import model.viewer.events.StoneEvent;
import model.viewer.events.StoneEvent.StoneKind;
import model.viewer.events.TownEvent;

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
	 * @param file
	 *            the file to read from
	 * @return the map contained in that file
	 * @throws XMLStreamException
	 *             on badly-formed XML or other processing error
	 * @throws IOException
	 *             if file not found or on other I/O error, e.g. while closing
	 *             the stream.
	 */
	public SPMap readMap(final String file) throws XMLStreamException,
			IOException {
		final FileInputStream istream = new FileInputStream(file);
		final SPMap retval = readMap(istream);
		istream.close();
		return retval;
	}

	/**
	 * An enumerated type for the tags we know about.
	 */
	private enum Tag {
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
						map = new SPMap(Integer.parseInt(getAttribute(// NOPMD
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
					map.addTile(parseTileContents(startElement, eventReader));
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
		} catch (final IOException e) {
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
	private static Tile parseTileContents(final StartElement element,
			final XMLEventReader reader) throws XMLStreamException {
		final Tile tile = parseTile(element);
		while (reader.hasNext()) {
			if (reader.peek().isStartElement()) {
				final StartElement elem = reader.nextEvent().asStartElement();
				final Tag tag = getTagType(elem);
				if (Tag.Fortress.equals(tag)) {
					tile.addFixture(parseFortress(tile, elem, reader));
				} else if (Tag.Unit.equals(tag)) {
					tile.addFixture(parseUnit(tile, elem, reader));
				} else if (Tag.River.equals(tag) || Tag.Lake.equals(tag)) {
					tile.addRiver(parseRiver(elem, reader));
				} else if (Tag.Event.equals(tag) || Tag.Battlefield.equals(tag)
						|| Tag.Cave.equals(tag) || Tag.City.equals(tag)
						|| Tag.Fortification.equals(tag)
						|| Tag.Mineral.equals(tag) || Tag.Stone.equals(tag)
						|| Tag.Town.equals(tag)) {
					tile.addFixture(parseEvent(tile, elem, reader));
				} else {
					throw new IllegalStateException(
							"Unexpected "
									+ tag.getText()
									+ " tag: only fortresses, units, and rivers can be inside a tile");
				}
			} else if (reader.peek().isCharacters()) {
				tile.setTileText(tile.getTileText()
						+ reader.nextEvent().asCharacters().getData());
			} else if (reader.nextEvent().isEndElement()) {
				tile.setTileText(tile.getTileText().trim());
				break;
			}
		}
		return tile;
	}

	/**
	 * @param tile
	 *            the tile
	 * @param elem
	 *            an XML element representing an event on that tile
	 * @param reader
	 *            the XML stream we're reading from
	 * @return the event
	 * @throws XMLStreamException on error while trying to find our end tag
	 */
	private static AbstractEvent parseEvent(final Tile tile,
			final StartElement elem, final XMLEventReader reader) throws XMLStreamException {
		AbstractEvent retval;
		switch (getEventType(elem)) {
		case Battlefield:
			retval = new BattlefieldEvent(Integer.parseInt(getAttribute(elem,
					"dc")));
			break;
		case Caves:
			retval = new CaveEvent(Integer.parseInt(getAttribute(elem, "dc")));
			break;
		case City:
			retval = new CityEvent(TownStatus.parseTownStatus(getAttribute(
					elem, "status")), TownSize.parseTownSize(getAttribute(elem,
					"size")), Integer.parseInt(getAttribute(elem, "dc")));
			break;
		case Fortification:
			retval = new FortificationEvent(
					TownStatus.parseTownStatus(getAttribute(elem, "status")),
					TownSize.parseTownSize(getAttribute(elem, "size")),
					Integer.parseInt(getAttribute(elem, "dc")));
			break;
		case Mineral:
			retval = new MineralEvent(
					MineralKind.parseMineralKind(getAttribute(elem, "mineral")),
					Boolean.parseBoolean(getAttribute(elem, "exposed")),
					Integer.parseInt(getAttribute(elem, "dc")));
			break;
		case Stone:
			retval = new StoneEvent(StoneKind.parseStoneKind(getAttribute(elem,
					"stone")), Integer.parseInt(getAttribute(elem, "dc")));
			break;
		case Town:
			retval = new TownEvent(TownStatus.parseTownStatus(getAttribute(
					elem, "status")), TownSize.parseTownSize(getAttribute(elem,
					"size")), Integer.parseInt(getAttribute(elem, "dc")));
			break;
		default:
			throw new IllegalArgumentException("Unknown event type");
		}
		while (reader.hasNext()) {
			if (reader.peek().isStartElement()) {
				final StartElement element = reader.nextEvent()
						.asStartElement();
				throw new IllegalStateException(UNEXPECTED_TAG
						+ getTagType(element).getText()
						+ ": an event can't contain anything yet");
			} else if (reader.nextEvent().isEndElement()) {
				break;
			}
		}
		return retval;
	}

	/**
	 * @param elem
	 *            an XML tag representing an event
	 * @return what kind of event it represents
	 */
	private static EventKind getEventType(final StartElement elem) {
		EventKind kind;
		switch (getTagType(elem)) {
		case Battlefield:
			kind = EventKind.Battlefield;
			break;
		case Cave:
			kind = EventKind.Caves;
			break;
		case City:
			kind = EventKind.City;
			break;
		case Event:
			kind = EventKind.parseEventKind(getAttribute(elem, "kind"));
			break;
		case Fortification:
			kind = EventKind.Fortification;
			break;
		case Mineral:
			kind = EventKind.Mineral;
			break;
		case Stone:
			kind = EventKind.Stone;
			break;
		case Town:
			kind = EventKind.Town;
			break;
		default:
			throw new IllegalStateException("Not a tag representing an Event");
		}
		return kind;
	}

	/**
	 * Parse the tile itself.
	 * 
	 * @param element
	 *            The tile XML tag
	 * @return the tile it represents.
	 */
	private static Tile parseTile(final StartElement element) {
		return (hasAttribute(element, "event")) ? new Tile(
				Integer.parseInt(getAttribute(element, "row")),
				Integer.parseInt(getAttribute(element, "column")),
				TileType.getTileType(getAttribute(element, "type")))
				: new Tile(Integer.parseInt(getAttribute(element, "row")),
						Integer.parseInt(getAttribute(element, "column")),
						TileType.getTileType(getAttribute(element, "type")),
						Integer.parseInt(getAttribute(element, "event")));
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
		final River river = Tag.Lake.equals(getTagType(elem)) ? River.Lake
				: River.getRiver(getAttribute(elem, "direction"));
		while (reader.hasNext()) {
			if (reader.peek().isStartElement()) {
				final StartElement element = reader.nextEvent()
						.asStartElement();
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
	 * Parse a fortress.
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
		if (attr == null) {
			throw new IllegalArgumentException(
					"Element doesn't contain that attribute");
		}
		return attr.getValue();
	}

	/**
	 * @param startElement
	 *            a tag
	 * @param attribute
	 *            the attribute we want
	 * @return whether the tag has that attribute
	 */
	private static boolean hasAttribute(final StartElement startElement,
			final String attribute) {
		return startElement.getAttributeByName(new QName(attribute)) == null;
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
			} catch (final NullPointerException e) { // NOPMD
				LOGGER.log(Level.SEVERE, "Null pointer in " + arg, e);
			} catch (final IllegalStateException e) {
				LOGGER.log(Level.SEVERE, "Unexpected state in " + arg, e);
			}
		}
		out.close();
	}
}
