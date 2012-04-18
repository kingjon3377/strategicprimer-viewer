package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.events.MineralEvent;
import util.EqualsAny;
import util.Warning;
import controller.map.DeprecatedPropertyException;
import controller.map.MissingParameterException;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;

/**
 * A Node to represent a MineralEvent.
 * 
 * @author Jonathan Lovelace
 */
public class MineralEventNode extends AbstractFixtureNode<MineralEvent> {
	/**
	 * Constructor.
	 */
	public MineralEventNode() {
		super(MineralEvent.class);
	}
	/**
	 * The property of an event saying how difficult it is to find it.
	 */
	private static final String DC_PROPERTY = "dc";
	/**
	 * The property saying what kind of mineral this is.
	 */
	private static final String MINERAL_PROPERTY = "kind";

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
	public MineralEvent produce(final PlayerCollection players, final Warning warner)
			throws SPFormatException {
		return new MineralEvent(
				getProperty(MINERAL_PROPERTY),
				Boolean.parseBoolean(getProperty("exposed")),
				Integer.parseInt(getProperty(DC_PROPERTY)));
	}
	/**
	 * @param property the name of a property
	 * @return whether this kind of node can use the property
	 */
	@Override
	public boolean canUse(final String property) {
		return EqualsAny.equalsAny(property, "mineral", "exposed", DC_PROPERTY, MINERAL_PROPERTY);
	}

	/**
	 * Check whether the Node's data is valid. A MineralNode is valid if it has
	 * no children and "kind", "dc", and "exposed" properties.
	 * 
	 * @param warner
	 *            a Warning instance to use for warnings
	 * @throws SPFormatException
	 *             if the data is invalid.
	 */
	@Override
	public void checkNode(final Warning warner) throws SPFormatException {
		if (iterator().hasNext()) {
			throw new UnwantedChildException("mineral", iterator().next()
					.toString(), getLine());
		} else if (hasProperty(MINERAL_PROPERTY)) {
			if (hasProperty(DC_PROPERTY)) {
				if (!hasProperty("exposed")) {
					throw new MissingParameterException("mineral", "exposed",
							getLine());
				}
			} else {
				throw new MissingParameterException("mineral", "dc", getLine());
			}
		} else {
			if (hasProperty("mineral")) {
				warner.warn(new DeprecatedPropertyException("mineral",
						"mineral", MINERAL_PROPERTY, getLine()));
				addProperty(MINERAL_PROPERTY, getProperty("mineral"), warner);
			} else {
				throw new MissingParameterException("mineral", "kind",
						getLine());
			}
		}
	}

	/**
	 * 
	 * @return a String representation of the object.
	 */
	@Override
	public String toString() {
		return "MineralEventNode";
	}
}
