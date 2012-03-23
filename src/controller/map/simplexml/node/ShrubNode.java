package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.fixtures.Shrub;
import util.EqualsAny;
import util.Warning;
import controller.map.SPFormatException;

/**
 * A Node to represent shrubs (or the aquatic equivalent) on the tile.
 * @author Jonathan Lovelace
 *
 */
public class ShrubNode extends AbstractFixtureNode<Shrub> {
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
	 * @return the Shrub this node represents
	 * @throws SPFormatException if it doesn't have a "kind" property.
	 */
	@Override
	public Shrub produce(final PlayerCollection players) throws SPFormatException {
		return new Shrub(getProperty(KIND_PROPERTY));
	}
	/**
	 * Check whether the node is valid. A Shrub is valid if it has a "shrub"
	 * property and no children.
	 * @throws SPFormatException if the required property is missing
	 */
	@Override
	public void checkNode() throws SPFormatException {
		if (iterator().hasNext()) {
			throw new SPFormatException("Shrub shouldn't have children", getLine());
		} else if (hasProperty("shrub")) {
			Warning.warn(new SPFormatException(
					"Use of property \"shrub\" to give kind of shrub is deprecated; use \"kind\" instead",
					getLine()));
			addProperty(KIND_PROPERTY, getProperty("shrub"));
		} else if (!hasProperty(KIND_PROPERTY)) {
			throw new SPFormatException("Shrub must have \"shrub\" property", getLine());
		}
	}
	/**
	 * @param property the name of a property
	 * @return whether this kind of node can use the property
	 */
	@Override
	public boolean canUse(final String property) {
		return EqualsAny.equalsAny(property, KIND_PROPERTY, "shrub");
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "ShrubNode";
	}
}
