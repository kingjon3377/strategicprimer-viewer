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
	 * Check whether we contain invalid data.
	 * @throws SPFormatException if we do.
	 */
	@Override
	public void checkNode() throws SPFormatException {
		throw new NotImplementedException("Tile validity-checking not yet implemented.");
	}

}
