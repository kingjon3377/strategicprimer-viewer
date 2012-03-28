package controller.map.simplexml.node;

import util.EqualsAny;
import model.map.PlayerCollection;
import model.map.events.BattlefieldEvent;
import controller.map.SPFormatException;

/**
 * A Node representing a BattlefieldEvent.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class BattlefieldEventNode extends AbstractFixtureNode<BattlefieldEvent> {
	/**
	 * Constructor.
	 */
	public BattlefieldEventNode() {
		super(BattlefieldEvent.class);
	}
	/**
	 * The property of an event saying how difficult it is to find it.
	 */
	private static final String DC_PROPERTY = "dc";
	/**
	 * @param players
	 *            the players on the map
	 * @return the equivalent event
	 * @throws SPFormatException
	 *             if this includes invalid data
	 */
	@Override
	public BattlefieldEvent produce(final PlayerCollection players)
			throws SPFormatException {
		return new BattlefieldEvent(Integer.parseInt(getProperty(DC_PROPERTY)));
	}

	/**
	 * Check whether this Node has valid data or not. A battlefield must have
	 * "kind" and "dc" properties (any others are ignored, for forward
	 * compatibility) and no children.
	 * 
	 * 
	 * @throws SPFormatException
	 *             if the data is invalid
	 */
	@Override
	public void checkNode() throws SPFormatException {
		if (iterator().hasNext()) {
			throw new SPFormatException("Event shouldn't have children",
					getLine());
		} else if (!hasProperty(DC_PROPERTY)) {
			throw new SPFormatException(
					"Battlefield must have \"dc\" property", getLine());
		}
	}
	/**
	 * @param property the name of a property
	 * @return whether this kind of node can use the property
	 */
	@Override
	public boolean canUse(final String property) {
		return EqualsAny.equalsAny(property, DC_PROPERTY);
	}

	/**
	 * 
	 * @return a String representation of this object.
	 */
	@Override
	public String toString() {
		return "BattlefieldEventNode";
	}
}
