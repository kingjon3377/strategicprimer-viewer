package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.fixtures.Fairy;
import controller.map.SPFormatException;

/**
 * A Node to represent a fairy or group of fairies.
 * @author Jonathan Lovelace
 *
 */
public class FairyNode extends AbstractFixtureNode<Fairy> {
	/**
	 * @param players ignored
	 * @return the fairy this represents
	 * @throws SPFormatException if missing a required attribute
	 */
	@Override
	public Fairy produce(final PlayerCollection players) throws SPFormatException {
		return new Fairy(getProperty("kind"));
	}
	/**
	 * Check the node for invalid data. A Fairy is valid if it has no children and has a "kind" property.
	 * @throws SPFormatException if the node contains invalid data
	 */
	@Override
	public void checkNode() throws SPFormatException {
		if (iterator().hasNext()) {
			throw new SPFormatException("Fairy shouldn't have children", getLine());
		} else if (!hasProperty("kind")) {
			throw new SPFormatException("Fairy must have \"kind\" property", getLine());
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
		return "FairyNode";
	}
}
