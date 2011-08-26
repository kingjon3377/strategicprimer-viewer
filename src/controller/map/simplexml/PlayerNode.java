package controller.map.simplexml;

import model.viewer.Player;

import org.apache.commons.lang.NotImplementedException;
/**
 * A Node to represent a Player.
 * @author Jonathan Lovelace
 *
 */
public class PlayerNode extends AbstractChildNode<Player> {
	/**
	 * Produce the equivalent Player.
	 * @return the equivalent Player.
	 * @throws SPFormatException if we contain invalid data.
	 */
	@Override
	public Player produce() throws SPFormatException {
		throw new NotImplementedException("Player production not yet implemented.");
	}
	/**
	 * Check whether we contain invalid data.
	 * @throws SPFormatException if we do.
	 */
	@Override
	public void checkNode() throws SPFormatException {
		throw new NotImplementedException("Player data-checking not yet implemented.");
	}

}
