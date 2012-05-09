package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.fixtures.Shrub;
import util.EqualsAny;
import util.Warning;
import controller.map.DeprecatedPropertyException;
import controller.map.MissingParameterException;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;
import controller.map.misc.IDFactory;

/**
 * A Node to represent shrubs (or the aquatic equivalent) on the tile.
 * @author Jonathan Lovelace
 *
 */
@Deprecated
public class ShrubNode extends AbstractFixtureNode<Shrub> {
	/**
	 * The current tag.
	 */
	private static final String TAG = "srub";
	/**
	 * The old, deprecated name for what is now KIND_PROPERTY.
	 */
	private static final String OLD_KIND_PROPERTY = "shrub";
	/**
	 * The name of the property telling what kind of shrub.
	 */
	private static final String KIND_PROPERTY = "kind";
	/**
	 * Constructor.
	 */
	public ShrubNode() {
		super(Shrub.class);
	}
	/**
	 * @param players ignored
	 * @param warner a Warning instance to use for warnings
	 * @return the Shrub this node represents
	 * @throws SPFormatException if it doesn't have a "kind" property.
	 */
	@Override
	public Shrub produce(final PlayerCollection players, final Warning warner) throws SPFormatException {
		return new Shrub(getProperty(KIND_PROPERTY), Long.parseLong(getProperty("id")));
	}
	/**
	 * Check whether the node is valid. A Shrub is valid if it has a "shrub"
	 * property and no children.
	 * @param warner a Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate new ones as needed
	 * @throws SPFormatException if the required property is missing
	 */
	@Override
	public void checkNode(final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		if (iterator().hasNext()) {
			throw new UnwantedChildException(TAG, iterator().next()
					.toString(), getLine());
		} else if (hasProperty(OLD_KIND_PROPERTY)) {
			// FIXME: This should go below the "kind" property check ...
			warner.warn(new DeprecatedPropertyException(TAG, OLD_KIND_PROPERTY,
					KIND_PROPERTY, getLine()));
			addProperty(KIND_PROPERTY, getProperty(OLD_KIND_PROPERTY), warner);
		} else if (hasProperty(KIND_PROPERTY)) {
			if (hasProperty("id")) {
				idFactory.register(Long.parseLong(getProperty("id")));
			} else {
				warner.warn(new MissingParameterException("shrub", "id", getLine()));
				addProperty("id", Long.toString(idFactory.getID()), warner);
			}
		} else {
			throw new MissingParameterException(TAG, KIND_PROPERTY, getLine());
		}
	}
	/**
	 * @param property the name of a property
	 * @return whether this kind of node can use the property
	 */
	@Override
	public boolean canUse(final String property) {
		return EqualsAny.equalsAny(property, KIND_PROPERTY, OLD_KIND_PROPERTY, "id");
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "ShrubNode";
	}
}
