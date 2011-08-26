package controller.map.simplexml;

import model.viewer.SPMap;

import org.apache.commons.lang.NotImplementedException;

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
		if (!hasProperty("version") || Integer.parseInt(getProperty("version")) < 1) {
			throw new SPFormatException(
					"This reader only accepts maps with a \"version\" property greater than or equal to 1.",
					getLine());
		} else if (!hasProperty("rows") || !hasProperty("columns")) {
			throw new SPFormatException(
					"Map must specify number of rows and columns.", getLine());
		}
	}
	/**
	 * @return the map the XML represented
	 * @throws SPFormatException if something's wrong with the format.
	 */
	@Override
	public SPMap produce() throws SPFormatException {
		throw new NotImplementedException("Placeholder");
	}

}
