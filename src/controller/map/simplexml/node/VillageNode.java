package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.fixtures.towns.TownStatus;
import model.map.fixtures.towns.Village;
import util.EqualsAny;
import util.Warning;
import controller.map.SPFormatException;
import controller.map.misc.IDFactory;

/**
 * A Node to produce a Village.
 *
 * @author Jonathan Lovelace
 * @deprecated Replaced by ReaderNG.
 */
@Deprecated
public class VillageNode extends AbstractFixtureNode<Village> {
	/**
	 * The tag.
	 */
	private static final String TAG = "village";
	/**
	 * The name of the property giving the village status.
	 */
	private static final String STATUS_PROPERTY = "status";
	/**
	 * The "name" property.
	 */
	private static final String NAME_PROPERTY = "name";

	/**
	 * Constructor.
	 */
	public VillageNode() {
		super(Village.class);
	}

	/**
	 * @param players ignored
	 * @param warner a Warning instance to use for warnings
	 * @return the Village this Node represents
	 * @throws SPFormatException if missing required attribute.
	 */
	@Override
	public Village produce(final PlayerCollection players, final Warning warner)
			throws SPFormatException {
		final Village fix = new Village(
				TownStatus.parseTownStatus(getProperty(STATUS_PROPERTY)),
				hasProperty(NAME_PROPERTY) ? getProperty(NAME_PROPERTY) : "",
				Integer.parseInt(getProperty("id")), getProperty("file"));
		return fix;
	}

	/**
	 * Check the node for invalid data. A Village is valid if it has no children
	 * and a "status" field.
	 *
	 * @param warner a Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate
	 *        new ones as needed
	 * @throws SPFormatException if the node is invalid.
	 */
	@Override
	public void checkNode(final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		forbidChildren(TAG);
		demandProperty(TAG, STATUS_PROPERTY, warner, false, false);
		demandProperty(TAG, NAME_PROPERTY, warner, true, false);
		registerOrCreateID(TAG, idFactory, warner);
	}

	/**
	 * @param property the name of a property
	 * @return whether this kind of node can use the property
	 */
	@Override
	public boolean canUse(final String property) {
		return EqualsAny.equalsAny(property, STATUS_PROPERTY, NAME_PROPERTY,
				"id");
	}

	/**
	 * @return a String representation of the node
	 */
	@Override
	public String toString() {
		return "VillageNode";
	}
}
