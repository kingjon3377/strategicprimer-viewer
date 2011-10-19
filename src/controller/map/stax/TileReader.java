package controller.map.stax;

import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import model.viewer.Fortress;
import model.viewer.PlayerCollection;
import model.viewer.River;
import model.viewer.Tile;
import model.viewer.TileType;
import model.viewer.Unit;
import model.viewer.events.AbstractEvent;
import model.viewer.events.BattlefieldEvent;
import model.viewer.events.CaveEvent;
import model.viewer.events.CityEvent;
import model.viewer.events.EventKind;
import model.viewer.events.FortificationEvent;
import model.viewer.events.MineralEvent;
import model.viewer.events.MineralKind;
import model.viewer.events.StoneEvent;
import model.viewer.events.StoneKind;
import model.viewer.events.TownEvent;
import model.viewer.events.TownSize;
import model.viewer.events.TownStatus;

/**
 * A helper class to read tiles and their contents from file. MapReader had too
 * many methods.
 * 
 * @author Jonathan Lovelace
 * 
 */
@Deprecated
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
	 * The helper for managing XML difficulties.
	 */
	private final XMLHelper helper;
	/**
	 * Constructor.
	 * @param xmlHelper a helper for some XML difficulties
	 */
	public TileReader(final XMLHelper xmlHelper) {
		helper = xmlHelper;
	}
	/**
	 * Parse a tile.
	 * 
	 * @param element
	 *            the tile tag itself
	 * @param reader
	 *            the stream of elements we're reading from
	 * @param players
	 *            the map's collection of players
	 * @return the tile in question.
	 */
	Tile parseTileAndContents(final StartElement element, // NOPMD
			final Iterable<XMLEvent> reader, final PlayerCollection players) {
		final Tile tile = parseTile(element);
		for (XMLEvent event : reader) {
			if (event.isStartElement()) {
				parseTileContents(event.asStartElement(), reader, tile, players);
			} else if (event.isCharacters()) {
				tile.setTileText(tile.getTileText()
						+ event.asCharacters().getData());
			} else if (event.isEndElement()) {
				tile.setTileText(tile.getTileText().trim());
				break;
			}
		}
		return tile;
	}
	/**
	 * @param elem the element we're parsing
	 * @param reader the stream of elements we're reading from
	 * @param tile the tile we're in the middle of
	 * @param players
	 *            the map's collection of players
	 */
	private void parseTileContents(final StartElement elem,
			final Iterable<XMLEvent> reader, final Tile tile,
			final PlayerCollection players) {
		switch (XMLHelper.getTagType(elem)) {
		case River:
		case Lake:
			tile.addRiver(parseRiver(elem, reader));
			break;
		case Fortress:
			tile.addFixture(parseFortress(elem, reader, players));
			break;
		case Unit:
			tile.addFixture(parseUnit(elem, reader, players));
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
	}

	/**
	 * @param elem
	 *            an XML element representing an event on that tile
	 * @param reader
	 *            the XML stream we're reading from
	 * @return the event
	 */
	private AbstractEvent parseEvent(final StartElement elem,
			final Iterable<XMLEvent> reader) {
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
		switch (XMLHelper.getTagType(elem)) {
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
	 */
	public River parseRiver(final StartElement elem,
			final Iterable<XMLEvent> reader) {
		// ESCA-JAVA0177:
		final River river = Tag.Lake.equals(XMLHelper.getTagType(elem)) ? River.Lake
				: River.getRiver(helper.getAttribute(elem, "direction"));
		helper.spinUntilEnd("<river>", reader);
		return river;
	}

	/**
	 * Parse a fortress.
	 * 
	 * @param elem
	 *            the fortress tag itself
	 * @param reader
	 *            the stream of elements we're reading from
	 * @param players
	 *            the map's collection of players
	 * @return the fortress in question.
	 */
	private Fortress parseFortress(final StartElement elem,
			final Iterable<XMLEvent> reader, final PlayerCollection players) {
		final Fortress fort = new Fortress(players.getPlayer(Integer
				.parseInt(helper.getAttributeWithDefault(elem, OWNER_ATTRIBUTE,
						"-1"))), helper.getAttributeWithDefault(elem,
				NAME_ATTRIBUTE, ""));
		for (XMLEvent event : reader) {
			if (event.isStartElement()) {
				parseFortContents(fort, event.asStartElement(), reader, players);
			} else if (event.isEndElement()) {
				break;
			}
		}
		return fort;
	}
	/**
	 * @param fort the fortress we're in the middle of
	 * @param element the current XML element
	 * @param reader the stream of XML elements we're reading from
	 * @param players
	 *            the map's collection of players
	 */
	private void parseFortContents(final Fortress fort, final StartElement element,
			final Iterable<XMLEvent> reader, final PlayerCollection players) {
		if (Tag.Unit.equals(XMLHelper.getTagType(element))) {
			fort.addUnit(parseUnit(element, reader, players));
		} else {
			throw new IllegalStateException(UNEXPECTED_TAG
					+ element.getName().getLocalPart()
					+ ": a fortress can only contain units");
		}
	}

	/**
	 * Parse a unit TODO: Soon there'll be tags nested inside units; we should
	 * handle them. And enforce the no-nested-units rule.
	 * 
	 * @param elem
	 *            the unit tag
	 * @param reader
	 *            the stream of elements we're reading from
	 * @param players
	 *            the map's collection of players
	 * @return the fortress in question.
	 * 
	 */
	private Unit parseUnit(final StartElement elem,
			final Iterable<XMLEvent> reader, final PlayerCollection players) {
		final Unit unit = new Unit(players.getPlayer(Integer.parseInt(helper
				.getAttributeWithDefault(elem, OWNER_ATTRIBUTE, "-1"))),
				helper.getAttributeWithDefault(elem, "type", ""),
				helper.getAttributeWithDefault(elem, NAME_ATTRIBUTE, ""));
		helper.spinUntilEnd("<unit>", reader);
		return unit;
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "TileReader";
	}
}
