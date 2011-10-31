package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.fixtures.Ground;
import controller.map.SPFormatException;
/**
 * A Node to produce a Ground fixture.
 * @author Jonathan Lovelace
 *
 */
public class GroundNode extends AbstractFixtureNode<Ground> {
	/**
	 * @param players ignored
	 * @return the Forest this node represents.
	 * @throws SPFormatException if the forest doesn't have a "kind" property.
	 */
	@Override
	public Ground produce(final PlayerCollection players) throws SPFormatException {
		return new Ground(getProperty("ground"), Boolean.parseBoolean(getProperty("exposed")));
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
			throw new SPFormatException("Ground shouldn't have children", getLine());
		} else if (!hasProperty("ground") || !hasProperty("exposed")) {
			throw new SPFormatException("Ground must have \"ground\" and \"exposed\" properties", getLine());
		}
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "GroundNode";
	}
}
