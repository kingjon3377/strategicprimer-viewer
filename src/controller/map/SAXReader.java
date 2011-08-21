package controller.map;

import model.viewer.Fortress;
import model.viewer.Player;
import model.viewer.SPMap;
import model.viewer.Tile;
import model.viewer.TileType;
import model.viewer.Unit;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A class to parse XML more functionally (hopefully incurring fewer PMD
 * warnings!).
 * 
 * @author Jonathan Lovelace
 * 
 */
@Deprecated
public final class SAXReader extends DefaultHandler {
	/**
	 * Error message for the outer exception when we get an unexpected tag.
	 */
	private static final String NON_COMPLIANT_XML = "Non-compliant XML";
	/**
	 * Constructor.
	 */
	public SAXReader() {
		super();
		safeToReturn = false;
	}

	/**
	 * @return the map, if we've finished parsing it.
	 */
	public SPMap getMap() {
		if (safeToReturn) {
			return currentMap;
		} // else
		throw new IllegalStateException("We haven't finished parsing yet.");
	}

	/**
	 * Throw an exception when we're asked for the map unless this is true.
	 */
	private boolean safeToReturn;
	/**
	 * The tile we're currently parsing. (For Units and Fortresses.)
	 */
	private Tile currentTile;
	/**
	 * The fortress we're currently parsing. (For Units in a fortress.)
	 */
	private Fortress currentFortress;
	/**
	 * The map we're currently parsing.
	 */
	private SPMap currentMap;
	/**
	 * The unit we're currently parsing.
	 */
	private Unit currentUnit;
	/**
	 * The player we're currently parsing.
	 */
	private Player currentPlayer;

	/**
	 * Called when we're done parsing.
	 * 
	 * @throws SAXException
	 *             required by spec
	 */
	@Override
	public void endDocument() throws SAXException {
		safeToReturn = true;
	}

	/**
	 * Called when we've finished parsing an element.
	 * 
	 * @param namespaceURI
	 *            the Namespace URI
	 * @param localName
	 *            the local name
	 * @param qualifiedName
	 *            the qualified XML name
	 * @throws SAXException
	 *             required by spec
	 */
	@Override
	public void endElement(final String namespaceURI, final String localName,
			final String qualifiedName) throws SAXException {
		if ("unit".equals(localName)) {
			if (currentFortress == null) {
				currentTile.addFixture(currentUnit);
			} else {
				currentFortress.addUnit(currentUnit);
			}
			currentUnit = null; // NOPMD
		} else if ("fortress".equals(localName)) {
			currentTile.addFixture(currentFortress);
			currentFortress = null; // NOPMD
		} else if ("tile".equals(localName)) {
			currentMap.addTile(currentTile);
			currentTile = null; // NOPMD
		} else if ("player".equals(localName)) {
			currentMap.addPlayer(currentPlayer);
			currentPlayer = null; // NOPMD
		}
	}

	/**
	 * Start parsing an element.
	 * 
	 * @param namespaceURI
	 *            the namespace URI of the element
	 * @param localName
	 *            the local name of the element
	 * @param qualifiedName
	 *            the qualified name of the element
	 * @param atts
	 *            attributes of the element
	 * @throws SAXException
	 *             required by spec
	 */
	@Override
	public void startElement(final String namespaceURI, final String localName,
			final String qualifiedName, final Attributes atts)
			throws SAXException {
		if (currentMap == null) {
			if ("map".equals(localName)) {
				currentMap = new SPMap(Integer.parseInt(atts.getValue("rows")),
						Integer.parseInt(atts.getValue("columns")));
			} else {
				throw new SAXException(NON_COMPLIANT_XML, new IllegalStateException(
						"Must start with a map tag!"));
			}
		} else {
			if ("map".equals(localName)) {
				throw new SAXException(NON_COMPLIANT_XML, new IllegalStateException(
						"Shouldn't have multiple map tags"));
			} else if ("tile".equals(localName)) {
				parseTile(atts);
			} else if ("fortress".equals(localName)) {
				parseFort(atts);
			} else if ("unit".equals(localName)) {
				parseUnit(atts);
			} else if ("player".equals(localName)) {
				// 4:38 PM
				parsePlayer(atts);
			}
		}
	}

	/**
	 * Parse a tile.
	 * 
	 * @param atts
	 *            The XML tag's attributes.
	 * @throws SAXException
	 *             when one tile is inside another (wraps IllegalStateException)
	 */
	private void parseTile(final Attributes atts) throws SAXException {
		if (currentTile == null) {
				currentTile = new Tile(Integer.parseInt(atts.getValue("row")),
						Integer.parseInt(atts.getValue("column")),
						TileType.getTileType(atts.getValue("type")));
		} else {
			throw new SAXException(NON_COMPLIANT_XML, new IllegalStateException(
					"Cannot (at present) have one tile inside another: row "
							+ atts.getValue("row") + ", col "
							+ atts.getValue("column")));
		}
	}

	/**
	 * Parse a fortress.
	 * 
	 * @param atts
	 *            the XML tag's attributes
	 * @throws SAXException
	 *             (wrapper around IllegalStateException) When we can't have a
	 *             fortress.
	 */
	public void parseFort(final Attributes atts) throws SAXException {
		if (currentFortress == null) {
			if (currentTile == null) {
				throw new SAXException(NON_COMPLIANT_XML, new IllegalStateException(
						"Cannot have a fortress not in a tile"));
			}
			currentFortress = new Fortress(currentTile, currentMap.getPlayers().getPlayer(Integer.parseInt(atts
					.getValue("owner"))), atts.getValue("name"));
		} else {
			throw new SAXException(NON_COMPLIANT_XML, new IllegalStateException(
					"Cannot have a fortress in a fortress"));
		}
	}

	/**
	 * Parse a Unit.
	 * 
	 * @param atts
	 *            the XML tag's attributes
	 * @throws SAXException
	 *             (wrapping IllegalStateException) when we can't have a Unit
	 *             here.
	 */
	public void parseUnit(final Attributes atts) throws SAXException {
		if (currentUnit == null) {
			if (currentTile == null) {
				throw new SAXException(NON_COMPLIANT_XML, new IllegalStateException(
						"Cannot have a unit not in a tile"));
			}
			currentUnit = new Unit(currentTile, currentMap.getPlayers().getPlayer(Integer.parseInt(atts
					.getValue("owner"))), atts.getValue("type"),
					atts.getValue("name"));
		} else {
			throw new SAXException(NON_COMPLIANT_XML, new IllegalStateException(
					"Cannot (currently) have a unit inside a unit"));
		}
	}

	/**
	 * Parse a player.
	 * 
	 * @param atts
	 *            the XML tag's attributes
	 * @throws SAXException
	 *             (wrapping IllegalStateException) when we're in the middle of
	 *             a Player tag already.
	 */
	public void parsePlayer(final Attributes atts) throws SAXException {
		if (currentPlayer == null) {
			currentPlayer = new Player(
					Integer.parseInt(atts.getValue("number")),
					atts.getValue("code_name"));
		} else {
			throw new SAXException(NON_COMPLIANT_XML, new IllegalStateException(
					"Cannot have a player inside a player"));
		}
	}

}
