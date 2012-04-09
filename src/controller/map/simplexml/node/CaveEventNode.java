package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.events.CaveEvent;
import util.EqualsAny;
import util.Warning;
import controller.map.SPFormatException;

/**
 * A Node representing a CaveEvent.
 * 
 * @author kingjon
 * 
 */
public class CaveEventNode extends AbstractFixtureNode<CaveEvent> {
	/**
	 * Constructor.
	 */
	public CaveEventNode() {
		super(CaveEvent.class);
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
	public CaveEvent produce(final PlayerCollection players)
			throws SPFormatException {
		return new CaveEvent(Integer.parseInt(getProperty(DC_PROPERTY)));
	}

	/**
	 * Check whether this Node has valid data or not. A Cave is valid if it has
	 * "dc" and "kind" properties and no children.
	 * 
	 * 
	 * @param warner
	 *            a Warning instance to use for warnings
	 * @throws SPFormatException
	 *             if it isn't valid.
	 */
	@Override
	public void checkNode(final Warning warner) throws SPFormatException {
		if (iterator().hasNext()) {
			throw new SPFormatException("Event shouldn't have children",
					getLine());
		} else if (!hasProperty(DC_PROPERTY)) {
			throw new SPFormatException(
					"Cave must have \"dc\" property", getLine());
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
	 * @return a String representation of this object
	 */
	@Override
	public String toString() {
		return "CaveEventNode";
	}
}
