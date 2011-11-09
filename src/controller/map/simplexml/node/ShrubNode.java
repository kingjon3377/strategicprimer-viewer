package controller.map.simplexml.node;

import util.EqualsAny;
import model.map.PlayerCollection;
import model.map.fixtures.Shrub;
import controller.map.SPFormatException;

/**
 * A Node to represent shrubs (or the aquatic equivalent) on the tile.
 * @author Jonathan Lovelace
 *
 */
public class ShrubNode extends AbstractFixtureNode<Shrub> {
	/**
	 * @param players ignored
	 * @return the Shrub this node represents
	 * @throws SPFormatException if it doesn't have a "shrub" property.
	 */
	@Override
	public Shrub produce(final PlayerCollection players) throws SPFormatException {
		return new Shrub(getProperty("shrub"));
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
		} else if (!hasProperty("shrub")) {
			throw new SPFormatException("Shrub must have \"shrub\" property", getLine());
		}
	}
	/**
	 * @param property the name of a property
	 * @return whether this kind of node can use the property
	 */
	@Override
	public boolean canUse(final String property) {
		return EqualsAny.equalsAny(property, "shrub");
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "ShrubNode";
	}
}
