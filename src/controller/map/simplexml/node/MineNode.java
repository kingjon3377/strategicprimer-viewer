package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.fixtures.resources.Mine;
import model.map.fixtures.towns.TownStatus;
import util.EqualsAny;
import util.Warning;
import controller.map.SPFormatException;
import controller.map.misc.IDFactory;

/**
 * A Node that will produce a Mine.
 *
 * @author Jonathan Lovelace
 * @deprecated Replaced by ReaderNG.
 */
@Deprecated
public class MineNode extends AbstractFixtureNode<Mine> {
	/**
	 * The old, deprecated name for what is now KIND_PROPERTY.
	 */
	private static final String OLD_KIND_PROPERTY = "product";
	/**
	 * The tag.
	 */
	private static final String TAG = "mine";
	/**
	 * The name of the property giving the mine's status.
	 */
	private static final String STATUS_PROPERTY = "status";
	/**
	 * The name of the property saying what kind of thing is mined in this mine.
	 */
	private static final String KIND_PROPERTY = "kind";

	/**
	 * Constructor.
	 */
	public MineNode() {
		super(Mine.class);
	}

	/**
	 * @param players ignored
	 * @param warner a Warning instance to use for warnings
	 * @return the Mine this node represents
	 * @throws SPFormatException if missing required properties
	 */
	@Override
	public Mine produce(final PlayerCollection players, final Warning warner)
			throws SPFormatException {
		final Mine fix = new Mine(getProperty(KIND_PROPERTY),
				TownStatus.parseTownStatus(getProperty(STATUS_PROPERTY)),
				Integer.parseInt(getProperty("id")), getProperty("file"));
		return fix;
	}

	/**
	 * Check the data for validity. A Mine is valid if it has no children and
	 * "product" and "status" properties.
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
		handleDeprecatedProperty(TAG, KIND_PROPERTY, OLD_KIND_PROPERTY, warner,
				true, false);
		demandProperty(TAG, STATUS_PROPERTY, warner, false, false);
		registerOrCreateID(TAG, idFactory, warner);
	}

	/**
	 * @param property the name of a property
	 * @return whether this kind of node can use the property
	 */
	@Override
	public boolean canUse(final String property) {
		return EqualsAny.equalsAny(property, KIND_PROPERTY, OLD_KIND_PROPERTY,
				STATUS_PROPERTY, "id");
	}

	/**
	 * @return a String representation of the node
	 */
	@Override
	public String toString() {
		return "MineNode";
	}
}
