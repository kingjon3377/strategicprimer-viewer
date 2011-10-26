package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.Tile;
import model.map.TileFixture;
import model.map.TileType;
import controller.map.SPFormatException;

/**
 * A Node to represent a Tile.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class TileNode extends AbstractChildNode<Tile> {
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
				TileType.getTileType(getProperty("type")));
		for (final AbstractXMLNode node : this) {
			if (node instanceof RiverNode) {
				tile.addRiver(((RiverNode) node).produce(players));
			} else if (node instanceof AbstractFixtureNode) {
				tile.addFixture(((AbstractFixtureNode<? extends TileFixture>) node)
						.produce(players));
			}
		}
		tile.setTileText(sbuild.toString().trim());
		return tile;
	}

	/**
	 * Check whether we contain invalid data. A Tile is valid if it has row,
	 * column, and type properties and contains only valid units, fortresses,
	 * rivers, and events. For forward compatibility, we do not object to
	 * properties we ignore. (But TODO: should we object to "event" tags, since
	 * those *used* to be valid?)
	 * 
	 * 
	 * @throws SPFormatException
	 *             if contain invalid data.
	 */
	@Override
	public void checkNode() throws SPFormatException {
		if (hasProperty("row") && hasProperty("column") && hasProperty("type")) {
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
					"Tile must contain \"row\", \"column\", and \"type\" properties.",
					getLine());
		}
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
