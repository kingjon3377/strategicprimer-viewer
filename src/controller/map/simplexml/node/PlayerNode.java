package controller.map.simplexml.node;

import model.map.Player;
import model.map.PlayerCollection;
import util.EqualsAny;
import util.Warning;
import controller.map.SPFormatException;
import controller.map.misc.IDFactory;

/**
 * A Node to represent a Player.
 *
 * @author Jonathan Lovelace
 * @deprecated Replaced by ReaderNG.
 */
@Deprecated
public class PlayerNode extends AbstractChildNode<Player> {
	/**
	 * The name of the property giving the player's code name.
	 */
	private static final String NAME_PROPERTY = "code_name";
	/**
	 * The name of the property giving the player's number.
	 */
	private static final String NUMBER_PROPERTY = "number";

	/**
	 * Constructor.
	 */
	public PlayerNode() {
		super(Player.class);
	}

	/**
	 * Produce the equivalent Player.
	 *
	 * @param players ignored
	 * @param warner a Warning instance to use for warnings
	 * @return the equivalent Player.
	 * @throws SPFormatException if we contain invalid data.
	 */
	@Override
	public Player produce(final PlayerCollection players, final Warning warner)
			throws SPFormatException {
		final Player player = new Player(
				Integer.parseInt(getProperty(NUMBER_PROPERTY)),
				getProperty(NAME_PROPERTY), getProperty("file"));
		return player;
	}

	/**
	 * Check whether we contain invalid data. A Player is valid iff it has no
	 * children and contains number and code_name properties. For forward
	 * compatibility, we do not object to properties we don't check.
	 *
	 * @param warner a Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate
	 *        new ones as needed
	 * @throws SPFormatException if we contain invalid data.
	 */
	@Override
	public void checkNode(final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		forbidChildren("player");
		demandProperty("player", NUMBER_PROPERTY, warner, false, false);
		demandProperty("player", NAME_PROPERTY, warner, false, false);
	}

	/**
	 * @param property the name of a property
	 * @return whether this kind of node can use the property
	 */
	@Override
	public boolean canUse(final String property) {
		return EqualsAny.equalsAny(property, NUMBER_PROPERTY, NAME_PROPERTY);
	}

	/**
	 *
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "PlayerNode";
	}
}
