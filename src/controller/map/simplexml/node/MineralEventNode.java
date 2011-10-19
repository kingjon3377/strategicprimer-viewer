package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.events.MineralEvent;
import model.map.events.MineralKind;
import controller.map.SPFormatException;

/**
 * A Node to represent a MineralEvent.
 * 
 * @author Jonathan Lovelace
 */
public class MineralEventNode extends AbstractChildNode<MineralEvent> {
	/**
	 * The property of an event saying how difficult it is to find it.
	 */
	private static final String DC_PROPERTY = "dc";
	/**
	 * The property of an Event saying what kind of event it is.
	 */
	private static final String KIND_PROPERTY = "kind";

	/**
	 * @param players
	 *            the players on the map
	 * @return the equivalent event
	 * @throws SPFormatException
	 *             if this includes invalid data
	 */
	@Override
	public MineralEvent produce(final PlayerCollection players)
			throws SPFormatException {
		return new MineralEvent(
				MineralKind.parseMineralKind(getProperty("mineral")),
				Boolean.parseBoolean(getProperty("exposed")),
				Integer.parseInt(getProperty(DC_PROPERTY)));
	}
	
	/**
	 * Check whether the Node's data is valid. A MineralNode is valid if it has
	 * no children and "kind", "dc", "mineral", and "exposed" properties.
	 * 
	 * @throws SPFormatException if the data is invalid.
	 */
	@Override
	public void checkNode() throws SPFormatException {
		if (iterator().hasNext()) {
			throw new SPFormatException("Event shouldn't have children",
					getLine());
		} else if (hasProperty(KIND_PROPERTY) && hasProperty(DC_PROPERTY)) {
			if (!hasProperty("mineral") || !hasProperty("exposed")) {
				throw new SPFormatException(
						"Mineral events must have \"mineral\" and \"exposed\" properties.",
						getLine());
			}
		} else {
			throw new SPFormatException(
					"Event must have \"kind\" and \"dc\" properties", getLine());
		}
	}
	/**
	 * @return a String representation of the object.
	 */
	@Override
	public String toString() {
		return "MineralEventNode";
	}
}
