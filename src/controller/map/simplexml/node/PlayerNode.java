package controller.map.simplexml.node;

import model.map.Player;
import model.map.PlayerCollection;
import controller.map.SPFormatException;
/**
 * A Node to represent a Player.
 * @author Jonathan Lovelace
 *
 */
public class PlayerNode extends AbstractChildNode<Player> {
	/**
	 * Produce the equivalent Player.
	 * @param players ignored
	 * @return the equivalent Player.
	 * @throws SPFormatException if we contain invalid data.
	 */
	@Override
	public Player produce(final PlayerCollection players) throws SPFormatException {
		return new Player(Integer.parseInt(getProperty("number")),
				getProperty("code_name"));
	}
	/**
	 * Check whether we contain invalid data. A Player is valid iff 
	 * it has no children and contains number and code_name properties. 
	 * For forward compatibility, we do not object to properties we don't 
	 * check.
	 * @throws SPFormatException if we contain invalid data.
	 */
	@Override
	public void checkNode() throws SPFormatException {
		if (iterator().hasNext()) {
			throw new SPFormatException(
					"A Player shouldn't contain other tags.", getLine());
		} else if (!hasProperty("number") || !hasProperty("code_name")) {
			throw new SPFormatException(
					"A Player must specify \"number\" and \"code_name\" properties.",
					getLine());
		}
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "PlayerNode";
	}
}
