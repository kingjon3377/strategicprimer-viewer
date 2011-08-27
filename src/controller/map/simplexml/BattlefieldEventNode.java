package controller.map.simplexml;

import model.viewer.PlayerCollection;
import model.viewer.events.BattlefieldEvent;

/**
 * A Node representing a BattlefieldEvent.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class BattlefieldEventNode extends AbstractChildNode<BattlefieldEvent> {
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
	public BattlefieldEvent produce(final PlayerCollection players)
			throws SPFormatException {
		return new BattlefieldEvent(Integer.parseInt(getProperty(DC_PROPERTY)));
	}

	@Override
	public void checkNode() throws SPFormatException {
		if (iterator().hasNext()) {
			throw new SPFormatException("Event shouldn't have children",
					getLine());
		} else if (!hasProperty(KIND_PROPERTY) || !hasProperty(DC_PROPERTY)) {
			throw new SPFormatException("Event must have \"kind\" and \"dc\" properties", getLine());
		} 
	}

}
