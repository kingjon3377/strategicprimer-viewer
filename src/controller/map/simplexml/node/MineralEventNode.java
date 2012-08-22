package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.fixtures.resources.MineralEvent;
import util.EqualsAny;
import util.Warning;
import controller.map.SPFormatException;
import controller.map.misc.IDFactory;

/**
 * A Node to represent a MineralEvent.
 *
 * @author Jonathan Lovelace
 * @deprecated Replaced by ReaderNG.
 */
@Deprecated
public class MineralEventNode extends AbstractFixtureNode<MineralEvent> {
	/**
	 * The name of the property saying whether the deposit is exposed.
	 */
	private static final String EXPOSED_PROPERTY = "exposed";
	/**
	 * The old, deprecated name for what is now MINERAL_PROPERTY.
	 */
	private static final String OLD_MINERAL_PROP = "mineral";
	/**
	 * The tag.
	 */
	private static final String TAG = "mineral";

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
	 * @param players the players on the map
	 * @param warner a Warning instance to use for warnings
	 * @return the equivalent event
	 * @throws SPFormatException if this includes invalid data
	 */
	@Override
	public MineralEvent produce(final PlayerCollection players,
			final Warning warner) throws SPFormatException {
		final MineralEvent fix = new MineralEvent(
				getProperty(MINERAL_PROPERTY),
				Boolean.parseBoolean(getProperty(EXPOSED_PROPERTY)),
				Integer.parseInt(getProperty(DC_PROPERTY)),
				Integer.parseInt(getProperty("id")));
		if (hasProperty("file")) {
			fix.setFile(getProperty("file"));
		}
		return fix;
	}

	/**
	 * @param property the name of a property
	 * @return whether this kind of node can use the property
	 */
	@Override
	public boolean canUse(final String property) {
		return EqualsAny.equalsAny(property, OLD_MINERAL_PROP,
				EXPOSED_PROPERTY, DC_PROPERTY, MINERAL_PROPERTY, "id");
	}

	/**
	 * Check whether the Node's data is valid. A MineralNode is valid if it has
	 * no children and "kind", "dc", and "exposed" properties.
	 *
	 * @param warner a Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate
	 *        new ones as needed
	 * @throws SPFormatException if the data is invalid.
	 */
	@Override
	public void checkNode(final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		forbidChildren(TAG);
		handleDeprecatedProperty(TAG, MINERAL_PROPERTY, OLD_MINERAL_PROP,
				warner, true, false);
		demandProperty(TAG, DC_PROPERTY, warner, false, false);
		demandProperty(TAG, EXPOSED_PROPERTY, warner, false, false);
		registerOrCreateID(TAG, idFactory, warner);
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
