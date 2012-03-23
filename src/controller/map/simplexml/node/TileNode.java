package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.Tile;
import model.map.TileFixture;
import model.map.TileType;
import model.map.fixtures.TextFixture;
import util.EqualsAny;
import util.Warning;
import controller.map.SPFormatException;
import controller.map.simplexml.ITextNode;

/**
 * A Node to represent a Tile.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class TileNode extends AbstractChildNode<Tile> implements ITextNode {
	/**
	 * The name of the terrain-type property.
	 */
	private static final String TERRAIN_PROPERTY = "kind";

	/**
	 * Constructor.
	 */
	public TileNode() {
		super(Tile.class);
	}
	/**
	 * Produce the equivalent Tile.
	 * 
	 * @param players
	 *            the players in the map
	 * @return the equivalent Tile.
	 * @throws SPFormatException
	 *             if we contain invalid data.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Tile produce(final PlayerCollection players)
			throws SPFormatException {
		final Tile tile = new Tile(Integer.parseInt(getProperty("row")),
				Integer.parseInt(getProperty("column")),
				TileType.getTileType(getProperty(TERRAIN_PROPERTY)));
		for (final AbstractXMLNode node : this) {
			if (node instanceof RiverNode) {
				tile.addRiver(((RiverNode) node).produce(players));
			} else if (node instanceof AbstractFixtureNode) {
				tile.addFixture(((AbstractFixtureNode<? extends TileFixture>) node)
						.produce(players));
			} else {
				Warning.warn(new SPFormatException(//NOPMD
						"Unexpected TileNode child of type " + node.toString(),
						getLine()));
			}
		}
		tile.addFixture(new TextFixture(sbuild.toString().trim(), -1));
		return tile;
	}

	/**
	 * Check whether we contain invalid data. A Tile is valid if it has row,
	 * column, and kind properties and contains only valid fixtures (units, fortresses,
	 * rivers, events, etc.). For forward compatibility, we do not object to
	 * properties we ignore. (But TODO: should we object to "event" tags, since
	 * those *used* to be valid?)
	 * 
	 * 
	 * @throws SPFormatException
	 *             if contain invalid data.
	 */
	@Override
	public void checkNode() throws SPFormatException {
		if (hasProperty("row") && hasProperty("column")) {
			if (!hasProperty(TERRAIN_PROPERTY) && hasProperty("type")) {
				Warning.warn(new SPFormatException(
						"Designating tile's terrain-type by \"type\" property is deprecated; use \"kind\" instead.",
						getLine()));
				addProperty(TERRAIN_PROPERTY, getProperty("type"));
			} else if (hasProperty(TERRAIN_PROPERTY)) {
				for (final AbstractXMLNode node : this) {
					if (node instanceof AbstractFixtureNode
							|| node instanceof RiverNode) {
						node.checkNode();
					} else {
						throw new SPFormatException("Unexpected child in tile.",
								getLine());
					}
				}
			} else {
				throw new SPFormatException(
						"Tile must contain \"row\", \"column\", and \"kind\" properties.",
						getLine());
			}
		} else {
			throw new SPFormatException(
					"Tile must contain \"row\", \"column\", and \"kind\" properties.",
					getLine());
		}
	}
	/**
	 * @param property the name of a property
	 * @return whether this kind of node can use the property
	 */
	@Override
	public boolean canUse(final String property) {
		return EqualsAny.equalsAny(property, "row", "column", TERRAIN_PROPERTY, "type");
	}
	/**
	 * The text associated with the tile.
	 */
	private final StringBuilder sbuild = new StringBuilder("");

	/**
	 * Add text to the tile.
	 * 
	 * @param text
	 *            the text to add
	 */
	@Override
	public void addText(final String text) {
		sbuild.append(text);
	}

	/**
	 * 
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "TileNode";
	}

}
