package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.events.StoneEvent;
import model.map.events.StoneKind;
import util.EqualsAny;
import util.Warning;
import controller.map.SPFormatException;

/**
 * A Node to represent a StoneEvent.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class StoneEventNode extends AbstractFixtureNode<StoneEvent> {
	/**
	 * Constructor.
	 */
	public StoneEventNode() {
		super(StoneEvent.class);
	}
	/**
	 * The property of an event saying how difficult it is to find it.
	 */
	private static final String DC_PROPERTY = "dc";
	/**
	 * The property saying what kind of stone this is.
	 */
	private static final String STONE_PROPERTY = "kind";

	/**
	 * @param players
	 *            the players on the map
	 * @param warner a Warning instance to use for warnings
	 * @return the equivalent event
	 * @throws SPFormatException
	 *             if this includes invalid data
	 */
	@Override
	public StoneEvent produce(final PlayerCollection players, final Warning warner)
			throws SPFormatException {
		return new StoneEvent(StoneKind.parseStoneKind(getProperty(STONE_PROPERTY)),
				Integer.parseInt(getProperty(DC_PROPERTY)));
	}

	/**
	 * Check whether the node is valid. A Stone Node is valid if it has no
	 * children and "kind", "dc", and "stone" properties.
	 * 
	 * @param warner
	 *            a Warning instance to use for warnings
	 * @throws SPFormatException
	 *             if the data is invalid.
	 */
	@Override
	public void checkNode(final Warning warner) throws SPFormatException {
		if (iterator().hasNext()) {
			throw new SPFormatException("Event shouldn't have children",
					getLine());
		} else if (hasProperty(STONE_PROPERTY)) {
			if (!hasProperty(DC_PROPERTY)) {
				throw new SPFormatException(
						"Event must have \"kind\" and \"dc\" properties", getLine());
			}
		} else {
			if (hasProperty("stone")) {
				warner.warn(new SPFormatException(
						"Use of \"stone\" property to specify kind of stone is deprecated; use \"kind\" instead",
						getLine()));
				addProperty(STONE_PROPERTY, getProperty("stone"), warner);
			} else {
				throw new SPFormatException(
						"Event must have \"kind\" and \"dc\" properties",
						getLine());
			}
		}
	}
	/**
	 * @param property the name of a property
	 * @return whether this kind of node can use the property
	 */
	@Override
	public boolean canUse(final String property) {
		return EqualsAny.equalsAny(property, STONE_PROPERTY, DC_PROPERTY, "stone");
	}

	/**
	 * 
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "StoneEventNode";
	}
}
