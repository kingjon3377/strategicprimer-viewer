package controller.map.simplexml;

import model.viewer.Tile;

import org.apache.commons.lang.NotImplementedException;
/**
 * A Node to represent a Tile.
 * @author Jonathan Lovelace
 *
 */
public class TileNode extends AbstractChildNode<Tile> {
	/**
	 * Produce the equivalent Tile.
	 * @return the equivalent Tile.
	 * @throws SPFormatException if we contain invalid data.
	 */
	@Override
	public Tile produce() throws SPFormatException {
		throw new NotImplementedException("Tile production not yet implemented.");
	}
	/**
	 * Check whether we contain invalid data. A Tile is valid if it has row, column, 
	 * and type properties and contains only valid units, fortresses, rivers, and events. 
	 * For forward compatibility, we do not object to properties we ignore. (But TODO: 
	 * should we object to "event" tags, since those *used* to be valid?)
	 * @throws SPFormatException if contain invalid data.
	 */
	@Override
	public void checkNode() throws SPFormatException {
		if (hasProperty("row") && hasProperty("column") && hasProperty("type")) {
			for (AbstractXMLNode node : this) {
				if (node instanceof UnitNode || node instanceof FortressNode
						|| node instanceof EventNode
						|| node instanceof RiverNode) {
					node.checkNode();
				} else {
					throw new SPFormatException("Unexpected child in tile.", getLine());
				}
			}
		} else {
			throw new SPFormatException(
					"Tile must contain \"row\", \"column\", and \"type\" properties.",
					getLine());
		}
	}

}
