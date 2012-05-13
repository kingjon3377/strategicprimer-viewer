package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.events.StoneEvent;
import model.map.events.StoneKind;
import util.EqualsAny;
import util.Warning;
import controller.map.SPFormatException;
import controller.map.misc.IDFactory;

/**
 * A Node to represent a StoneEvent.
 * 
 * @author Jonathan Lovelace
 * 
 */
@Deprecated
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
		final StoneEvent fix = new StoneEvent(
				StoneKind.parseStoneKind(getProperty(STONE_PROPERTY)),
				Integer.parseInt(getProperty(DC_PROPERTY)),
				Long.parseLong(getProperty("id")));
		if (hasProperty("file")) {
			fix.setFile(getProperty("file"));
		}
		return fix;
	}

	/**
	 * Check whether the node is valid. A Stone Node is valid if it has no
	 * children and "kind", "dc", and "stone" properties.
	 * 
	 * @param warner
	 *            a Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate new ones as needed
	 * @throws SPFormatException
	 *             if the data is invalid.
	 */
	@Override
	public void checkNode(final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		forbidChildren(TAG);
		handleDeprecatedProperty(TAG, STONE_PROPERTY, OLD_STONE_PROP, warner, true, false);
		demandProperty(TAG, DC_PROPERTY, warner, false, false);
		registerOrCreateID(TAG, idFactory, warner);
	}
	/**
	 * @param property the name of a property
	 * @return whether this kind of node can use the property
	 */
	@Override
	public boolean canUse(final String property) {
		return EqualsAny.equalsAny(property, STONE_PROPERTY, DC_PROPERTY,
				OLD_STONE_PROP, "id");
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
