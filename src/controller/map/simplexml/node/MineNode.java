package controller.map.simplexml.node;

import util.EqualsAny;
import util.Warning;
import model.map.PlayerCollection;
import model.map.events.TownStatus;
import model.map.fixtures.Mine;
import controller.map.DeprecatedPropertyException;
import controller.map.MissingParameterException;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;
import controller.map.misc.IDFactory;

/**
 * A Node that will produce a Mine.
 * @author Jonathan Lovelace
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
	public Mine produce(final PlayerCollection players, final Warning warner) throws SPFormatException {
		return new Mine(getProperty(KIND_PROPERTY),
				TownStatus.parseTownStatus(getProperty(STATUS_PROPERTY)),
				Long.parseLong(getProperty("id")));
	}
	/**
	 * Check the data for validity. A Mine is valid if it has no children and "product" and "status" properties.
	 * @param warner a Warning instance to use for warnings
	 * @throws SPFormatException if the node is invalid.
	 */
	@Override
	public void checkNode(final Warning warner) throws SPFormatException {
		if (iterator().hasNext()) {
			throw new UnwantedChildException(TAG, iterator().next()
					.toString(), getLine());
		} else if (hasProperty(KIND_PROPERTY)) {
			if (!hasProperty(STATUS_PROPERTY)) {
				throw new MissingParameterException(TAG, STATUS_PROPERTY, getLine());
			}
		} else {
			if (hasProperty(OLD_KIND_PROPERTY)) {
				warner.warn(new DeprecatedPropertyException(TAG, OLD_KIND_PROPERTY,
						KIND_PROPERTY, getLine()));
				addProperty(KIND_PROPERTY, getProperty(OLD_KIND_PROPERTY), warner);
			} else {
				throw new MissingParameterException(TAG, KIND_PROPERTY, getLine());
			}
		}
		if (hasProperty("id")) {
			IDFactory.FACTORY.register(Long.parseLong(getProperty("id")));
		} else {
			warner.warn(new MissingParameterException(TAG, "id", getLine()));
			addProperty("id", Long.toString(IDFactory.FACTORY.getID()), warner);
		}
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
