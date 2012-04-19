package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.events.StoneEvent;
import model.map.events.StoneKind;
import util.EqualsAny;
import util.Warning;
import controller.map.DeprecatedPropertyException;
import controller.map.MissingParameterException;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;

/**
 * A Node to represent a StoneEvent.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class StoneEventNode extends AbstractFixtureNode<StoneEvent> {
	/**
	 * The current tag.
	 */
	private static final String TAG = "stone";
	/**
	 * The old, deprecated name for what is now STONE_PROPERTY.
	 */
	private static final String OLD_STONE_PROP = "stone";
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
			throw new UnwantedChildException(TAG, iterator().next().toString(),
					getLine());
		} else if (hasProperty(STONE_PROPERTY)) {
			if (!hasProperty(DC_PROPERTY)) {
				throw new MissingParameterException(TAG, DC_PROPERTY, getLine());
			}
		} else {
			if (hasProperty("stone")) {
				warner.warn(new DeprecatedPropertyException(TAG, OLD_STONE_PROP,
						STONE_PROPERTY, getLine()));
				addProperty(STONE_PROPERTY, getProperty(OLD_STONE_PROP), warner);
			} else {
				throw new MissingParameterException(TAG, STONE_PROPERTY, getLine());
			}
		}
	}
	/**
	 * @param property the name of a property
	 * @return whether this kind of node can use the property
	 */
	@Override
	public boolean canUse(final String property) {
		return EqualsAny.equalsAny(property, STONE_PROPERTY, DC_PROPERTY, OLD_STONE_PROP);
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
