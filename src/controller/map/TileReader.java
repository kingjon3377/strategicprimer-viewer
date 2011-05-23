package controller.map;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;

import model.viewer.Fortress;
import model.viewer.River;
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
import controller.map.MapReader.Tag;

/**
 * A helper class to read tiles and their contents from file. MapReader had too many methods.
 * @author Jonathan Lovelace
 *
 */
public class TileReader {
	/**
	 * The "name" attribute.
	 */
	private static final String NAME_ATTRIBUTE = "name";
	/**
	 * The "owner" attribute.
	 */
	private static final String OWNER_ATTRIBUTE = "owner";
	/**
	 * Error message for unexpected tag.
	 */
	private static final String UNEXPECTED_TAG = "Unexpected tag ";
	/**
	 * Our reference to the main reader, which has a couple of methods that do belong there that we need to use.
	 */
	private final MapReader mainReader;
	/**
	 * The helper for managing XML difficulties.
	 */
	private final XMLHelper helper;
	/**
	 * Constructor.
	 * @param reader the main reader
	 * @param xmlHelper a helper for some XML difficulties
	 */
	public TileReader(final MapReader reader, final XMLHelper xmlHelper) {
		mainReader = reader;
		helper = xmlHelper;
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
	Tile parseTileContents(final StartElement element, // NOPMD
			final XMLEventReader reader) throws XMLStreamException {
		final Tile tile = parseTile(element);
		while (reader.hasNext()) {
			if (reader.peek().isStartElement()) {
				final StartElement elem = reader.nextEvent().asStartElement();
				switch (mainReader.getTagType(elem)) {
				case River:
				case Lake:
					tile.addRiver(parseRiver(elem, reader));
					break;
				case Fortress:
					tile.addFixture(parseFortress(tile, elem, reader));
					break;
				case Unit:
					tile.addFixture(parseUnit(tile, elem, reader));
					break;
				case Event:
				case Battlefield:
				case Cave:
				case City:
				case Fortification:
				case Mineral:
				case Stone:
				case Town:
					tile.addFixture(parseEvent(elem, reader));
					break;
				default:
					throw new IllegalStateException(
							"Unexpected "
									+ elem.getName().getLocalPart()
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
	 * @param elem
	 *            an XML element representing an event on that tile
	 * @param reader
	 *            the XML stream we're reading from
	 * @return the event
	 * @throws XMLStreamException
	 *             on error while trying to find our end tag
	 */
	private AbstractEvent parseEvent(final StartElement elem,
			final XMLEventReader reader) throws XMLStreamException {
		// ESCA-JAVA0177:
		AbstractEvent retval;
		switch (getEventType(elem)) {
		case Battlefield:
			retval = new BattlefieldEvent(Integer.parseInt(helper.getAttribute(elem,
					"dc")));
			break;
		case Caves:
			retval = new CaveEvent(Integer.parseInt(helper.getAttribute(elem, "dc")));
			break;
		case City:
			retval = new CityEvent(TownStatus.parseTownStatus(helper.getAttribute(
					elem, "status")), TownSize.parseTownSize(helper.getAttribute(elem,
					"size")), Integer.parseInt(helper.getAttribute(elem, "dc")));
			break;
		case Fortification:
			retval = new FortificationEvent(
					TownStatus.parseTownStatus(helper.getAttribute(elem, "status")),
					TownSize.parseTownSize(helper.getAttribute(elem, "size")),
					Integer.parseInt(helper.getAttribute(elem, "dc")));
			break;
		case Mineral:
			retval = new MineralEvent(
					MineralKind.parseMineralKind(helper.getAttribute(elem, "mineral")),
					Boolean.parseBoolean(helper.getAttribute(elem, "exposed")),
					Integer.parseInt(helper.getAttribute(elem, "dc")));
			break;
		case Stone:
			retval = new StoneEvent(StoneKind.parseStoneKind(helper.getAttribute(elem,
					"stone")), Integer.parseInt(helper.getAttribute(elem, "dc")));
			break;
		case Town:
			retval = new TownEvent(TownStatus.parseTownStatus(helper.getAttribute(
					elem, "status")), TownSize.parseTownSize(helper.getAttribute(elem,
					"size")), Integer.parseInt(helper.getAttribute(elem, "dc")));
			break;
		default:
			throw new IllegalArgumentException("Unknown event type");
		}
		helper.spinUntilEnd("<event>", reader);
		return retval;
	}

	/**
	 * @param elem
	 *            an XML tag representing an event
	 * @return what kind of event it represents
	 */
	private EventKind getEventType(final StartElement elem) {
		switch (mainReader.getTagType(elem)) {
		case Battlefield:
			return EventKind.Battlefield; // NOPMD
		case Cave:
			return EventKind.Caves; // NOPMD
		case City:
			return EventKind.City; // NOPMD
		case Event:
			return EventKind.parseEventKind(helper.getAttribute(elem, "kind")); // NOPMD
		case Fortification:
			return EventKind.Fortification; // NOPMD
		case Mineral:
			return EventKind.Mineral; // NOPMD
		case Stone:
			return EventKind.Stone; // NOPMD
		case Town:
			return EventKind.Town; // NOPMD
		default:
			throw new IllegalStateException("Not a tag representing an Event");
		}
	}

	/**
	 * Parse the tile itself.
	 * 
	 * @param element
	 *            The tile XML tag
	 * @return the tile it represents.
	 */
	private Tile parseTile(final StartElement element) {
		return new Tile(Integer.parseInt(helper.getAttribute(element, "row")),
				Integer.parseInt(helper.getAttribute(element, "column")),
				TileType.getTileType(helper.getAttribute(element, "type")));
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
	public River parseRiver(final StartElement elem,
			final XMLEventReader reader) throws XMLStreamException {
		// ESCA-JAVA0177:
		final River river = Tag.Lake.equals(mainReader.getTagType(elem)) ? River.Lake
				: River.getRiver(helper.getAttribute(elem, "direction"));
		helper.spinUntilEnd("<river>", reader);
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
	private Fortress parseFortress(final Tile tile,
			final StartElement elem, final XMLEventReader reader)
			throws XMLStreamException {
		final Fortress fort = new Fortress(tile,
				Integer.parseInt(helper.getAttributeWithDefault(elem, OWNER_ATTRIBUTE,
						"-1")), helper.getAttributeWithDefault(elem, NAME_ATTRIBUTE,
						""));
		while (reader.hasNext()) {
			if (reader.peek().isStartElement()) {
				final StartElement element = reader.nextEvent()
						.asStartElement();
				if (Tag.Unit.equals(mainReader.getTagType(element))) {
					fort.addUnit(parseUnit(tile, elem, reader));
				} else {
					throw new IllegalStateException(UNEXPECTED_TAG
							+ element.getName().getLocalPart()
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
	private Unit parseUnit(final Tile tile, final StartElement elem,
			final XMLEventReader reader) throws XMLStreamException {
		final Unit unit = new Unit(tile,
				Integer.parseInt(helper.getAttributeWithDefault(elem, OWNER_ATTRIBUTE,
						"-1")), helper.getAttributeWithDefault(elem, "type", ""),
				helper.getAttributeWithDefault(elem, NAME_ATTRIBUTE, ""));
		helper.spinUntilEnd("<unit>", reader);
		return unit;
	}


}
