package controller.map.simplexml.node;

import java.util.LinkedList;
import java.util.List;

import model.viewer.PlayerCollection;
import model.viewer.SPMap;
import controller.map.simplexml.AbstractChildNode;
import controller.map.simplexml.AbstractXMLNode;
import controller.map.simplexml.SPFormatException;

/**
 * A node generated from the <map> tag.
 * @author Jonathan Lovelace
 *
 */
public class MapNode extends AbstractChildNode<SPMap> {
	/**
	 * Check the node. A Map is valid iff every child is either a Player or a
	 * Tile and it includes version (greater than or equal to 1, for this
	 * version of the reader), rows, and columns properties.
	 * 
	 * @throws SPFormatException
	 *             if all is not well.
	 */
	@Override
	public void checkNode() throws SPFormatException {
		for (AbstractXMLNode node : this) {
			if (node instanceof TileNode || node instanceof PlayerNode) {
				node.checkNode();
			} else {
				throw new SPFormatException("Map should only directly contain Tiles and Players.", getLine());
			}
		}
		if (!hasProperty("version") || Integer.parseInt(getProperty("version")) < SPMap.VERSION) {
			throw new SPFormatException(
					"This reader only accepts maps with a \"version\" property greater than or equal to " + SPMap.VERSION,
					getLine());
		} else if (!hasProperty("rows") || !hasProperty("columns")) {
			throw new SPFormatException(
					"Map must specify number of rows and columns.", getLine());
		}
	}
	/**
	 * @return the map the XML represented
	 * @param players will be null, and is ignored
	 * @throws SPFormatException if something's wrong with the format.
	 */
	@Override
	public SPMap produce(final PlayerCollection players) throws SPFormatException {
		final SPMap map = new SPMap(Integer.parseInt(getProperty("rows")),
				Integer.parseInt(getProperty("columns")));
		final List<TileNode> tiles = new LinkedList<TileNode>();
		for (AbstractXMLNode node : this) {
			if (node instanceof PlayerNode) {
				map.addPlayer(((PlayerNode) node).produce(null));
			} else if (node instanceof TileNode) {
				tiles.add((TileNode) node);
			} else {
				throw new SPFormatException("Unsupported direct child of <map>", node.getLine());
			}
		}
		for (TileNode node : tiles) {
			map.addTile(node.produce(map.getPlayers()));
		}
		if (hasProperty("current_player")) {
			map.getPlayers().getPlayer(Integer.parseInt(getProperty("current_player"))).setCurrent(true);
		}
		return map;
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "MapNode";
	}
}
