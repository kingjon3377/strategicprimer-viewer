package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.fixtures.Shrub;
import util.EqualsAny;
import util.Warning;
import controller.map.DeprecatedPropertyException;
import controller.map.MissingParameterException;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;

/**
 * A Node to represent shrubs (or the aquatic equivalent) on the tile.
 * @author Jonathan Lovelace
 *
 */
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
		return new Shrub(getProperty(KIND_PROPERTY));
	}
	/**
	 * Check whether the node is valid. A Shrub is valid if it has a "shrub"
	 * property and no children.
	 * @param warner a Warning instance to use for warnings
	 * @throws SPFormatException if the required property is missing
	 */
	@Override
	public void checkNode(final Warning warner) throws SPFormatException {
		if (iterator().hasNext()) {
			throw new UnwantedChildException(TAG, iterator().next()
					.toString(), getLine());
		} else if (hasProperty("shrub")) {
			warner.warn(new DeprecatedPropertyException(TAG, OLD_KIND_PROPERTY,
					KIND_PROPERTY, getLine()));
			addProperty(KIND_PROPERTY, getProperty(OLD_KIND_PROPERTY), warner);
		} else if (!hasProperty(KIND_PROPERTY)) {
			throw new MissingParameterException(TAG, KIND_PROPERTY, getLine());
		}
	}
	/**
	 * @param property the name of a property
	 * @return whether this kind of node can use the property
	 */
	@Override
	public boolean canUse(final String property) {
		return EqualsAny.equalsAny(property, KIND_PROPERTY, OLD_KIND_PROPERTY);
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "ShrubNode";
	}
}
