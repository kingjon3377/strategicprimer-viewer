package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.fixtures.Giant;
import controller.map.SPFormatException;

/**
 * A Node to represent a giant.
 * @author Jonathan Lovelace
 *
 */
public class GiantNode extends AbstractFixtureNode<Giant> {
	/**
	 * @param players ignored
	 * @return the giant this represents
	 * @throws SPFormatException if missing a required attribute
	 */
	@Override
	public Giant produce(final PlayerCollection players) throws SPFormatException {
		return new Giant(getProperty("kind"));
	}
	/**
	 * Check the node for invalid data. A Giant is valid if it has no children and has a "kind" property.
	 * @throws SPFormatException if the node contains invalid data
	 */
	@Override
	public void checkNode() throws SPFormatException {
		if (iterator().hasNext()) {
			throw new SPFormatException("Giant shouldn't have children", getLine());
		} else if (!hasProperty("kind")) {
			throw new SPFormatException("Giant must have \"kind\" property", getLine());
		}
	}
	/**
	 * @param property the name of a property
	 * @return whether this kind of node can use the property
	 */
	@Override
	public boolean canUse(final String property) {
		return "kind".equals(property);
	}
	/**
	 * @return a String representation of the node.
	 */
	@Override
	public String toString() {
		return "GiantNode";
	}
}
