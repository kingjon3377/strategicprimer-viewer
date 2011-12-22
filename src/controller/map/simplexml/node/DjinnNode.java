package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.fixtures.Djinn;
import controller.map.SPFormatException;

/**
 * A Node to represent a djinn or group of djinni.
 * @author Jonathan Lovelace
 *
 */
public class DjinnNode extends AbstractFixtureNode<Djinn> {
	/**
	 * @param players ignored
	 * @return the djinn this represents
	 * @throws SPFormatException never
	 */
	@Override
	public Djinn produce(final PlayerCollection players) throws SPFormatException {
		return new Djinn();
	}
	/**
	 * Check the node for invalid data. A Djinn is valid i it has no children.
	 * @throws SPFormatException if the node contains invalid data
	 */
	@Override
	public void checkNode() throws SPFormatException {
		if (iterator().hasNext()) {
			throw new SPFormatException("Djinn shouldn't have children", getLine());
		}
	}
	/**
	 * @param property the name of a property
	 * @return whether this kind of node can use the property
	 */
	@Override
	public boolean canUse(final String property) {
		return false;
	}
	/**
	 * @return a String representation of the node.
	 */
	@Override
	public String toString() {
		return "DjinnNode";
	}
}
