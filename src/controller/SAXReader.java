package controller;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import model.Fortress;
import model.Player;
import model.SPMap;
import model.Tile;
import model.Unit;
import model.Tile.TileType;

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
public final class SAXReader extends DefaultHandler implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6927535476104522093L;

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
	private transient boolean safeToReturn;
	/**
	 * The tile we're currently parsing. (For Units and Fortresses.)
	 */
	private transient Tile currentTile;
	/**
	 * The fortress we're currently parsing. (For Units in a fortress.)
	 */
	private transient Fortress currentFortress;
	/**
	 * The map we're currently parsing.
	 */
	private transient SPMap currentMap;
	/**
	 * The unit we're currently parsing.
	 */
	private transient Unit currentUnit;
	/**
	 * The player we're currently parsing.
	 */
	private transient model.Player currentPlayer;

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
				currentTile.addUnit(currentUnit);
			} else {
				currentFortress.addUnit(currentUnit);
			}
			currentUnit = null; // NOPMD
		} else if ("fortress".equals(localName)) {
			currentTile.addFort(currentFortress);
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
				throw new SAXException(new IllegalStateException(
						"Must start with a map tag!"));
			}
		} else {
			if ("map".equals(localName)) {
				throw new SAXException(new IllegalStateException(
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
					Integer.parseInt(atts.getValue("column")), getTileType(atts
							.getValue("type")));
		} else {
			throw new SAXException(new IllegalStateException(
					"Cannot (at present) have one tile inside another"));
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
				throw new SAXException(new IllegalStateException(
						"Cannot have a fortress not in a tile"));
			}
			currentFortress = new Fortress(currentTile, Integer.parseInt(atts
					.getValue("owner")));
		} else {
			throw new SAXException(new IllegalStateException(
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
				throw new SAXException(new IllegalStateException(
						"Cannot have a unit not in a tile"));
			}
			currentUnit = new Unit(currentTile, Integer.parseInt(atts
					.getValue("owner")), atts.getValue("type"));
		} else {
			throw new SAXException(new IllegalStateException(
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
			currentPlayer = new Player(Integer
					.parseInt(atts.getValue("number")), atts
					.getValue("code_name"));
		} else {
			throw new SAXException(new IllegalStateException(
					"Cannot have a player inside a player"));
		}
	}

	/**
	 * The mapping from descriptive strings to tile types. Used to make
	 * multiple-return-points warnings go away.
	 */
	private static final Map<String, TileType> TILE_TYPE_MAP = new HashMap<String,TileType>();

	static {
		TILE_TYPE_MAP.put("tundra", TileType.Tundra);
		TILE_TYPE_MAP.put("temperate_forest", TileType.TemperateForest);
		TILE_TYPE_MAP.put("boreal_forest", TileType.BorealForest);
		TILE_TYPE_MAP.put("ocean", TileType.Ocean);
		TILE_TYPE_MAP.put("desert", TileType.Desert);
		TILE_TYPE_MAP.put("plains", TileType.Plains);
		TILE_TYPE_MAP.put("jungle", TileType.Jungle);
		TILE_TYPE_MAP.put("mountain", TileType.Mountain);
	}

	/**
	 * Parse a tile terrain type.
	 * 
	 * @param string
	 *            A string describing the terrain
	 * @return the terrain type
	 */
	private static TileType getTileType(final String string) {
		if (TILE_TYPE_MAP.containsKey(string)) {
			return TILE_TYPE_MAP.get(string);
		} // else
		throw new IllegalArgumentException("Unrecognized terrain type string");
	}
}
