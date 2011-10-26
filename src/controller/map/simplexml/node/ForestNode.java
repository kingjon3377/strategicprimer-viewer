package controller.map.simplexml.node;

import controller.map.SPFormatException;
import model.map.PlayerCollection;
import model.map.events.Forest;
/**
 * A Node that will produce a Forest.
 * @author Jonathan Lovelace
 *
 */
public class ForestNode extends AbstractFixtureNode<Forest> {
	/**
	 * @param players ignored
	 * @return the Forest this node represents.
	 * @throws SPFormatException if the forest doesn't have a "kind" property.
	 */
	@Override
	public Forest produce(final PlayerCollection players) throws SPFormatException {
		return new Forest(getProperty("kind"));
	}
	
	/**
	 * Check whether the node is valid. A forest is valid if it has a "kind"
	 * property and no children. TODO: add further properties.
	 * 
	 * @throws SPFormatException
	 *             if any required properties are missing.
	 */
	@Override
	public void checkNode() throws SPFormatException {
		if (iterator().hasNext()) {
			throw new SPFormatException("Forest shouldn't have children", getLine());
		} else if (!hasProperty("kind")) {
			throw new SPFormatException("Forest must have \"kind\" property", getLine());
		}
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "ForestNode";
	}
}
