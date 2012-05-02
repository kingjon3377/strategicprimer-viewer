package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.events.CaveEvent;
import util.EqualsAny;
import util.Warning;
import controller.map.MissingParameterException;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;
import controller.map.misc.IDFactory;

/**
 * A Node representing a CaveEvent.
 * 
 * @author kingjon
 * 
 */
@Deprecated
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
	 * @param warner
	 *            a Warning instance to use for warnings
	 * @return the equivalent event
	 * @throws SPFormatException
	 *             if this includes invalid data
	 */
	@Override
	public CaveEvent produce(final PlayerCollection players, final Warning warner)
			throws SPFormatException {
		return new CaveEvent(Integer.parseInt(getProperty(DC_PROPERTY)),
				Long.parseLong(getProperty("id")));
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
			throw new UnwantedChildException("cave", iterator().next().toString(),
					getLine());
		} else if (hasProperty(DC_PROPERTY)) {
			if (hasProperty("id")) {
				IDFactory.FACTORY.register(Long.parseLong(getProperty("id")));
			} else {
				warner.warn(new MissingParameterException("cave", "id", getLine()));
				addProperty("id", Long.toString(IDFactory.FACTORY.getID()), warner);
			}
		} else {
			throw new MissingParameterException("cave", "dc", getLine());
		}
	}
	/**
	 * @param property the name of a property
	 * @return whether this kind of node can use the property
	 */
	@Override
	public boolean canUse(final String property) {
		return EqualsAny.equalsAny(property, DC_PROPERTY, "id");
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
