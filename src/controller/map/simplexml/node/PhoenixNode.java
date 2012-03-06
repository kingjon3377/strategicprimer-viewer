package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.fixtures.Phoenix;
import controller.map.SPFormatException;

/**
 * A Node to represent a phoenix.
 * @author Jonathan Lovelace
 *
 */
public class PhoenixNode extends AbstractFixtureNode<Phoenix> {
	/**
	 * Constructor.
	 */
	public PhoenixNode() {
		super(Phoenix.class);
	}
	/**
	 * @param players ignored
	 * @return the phoenix this represents
	 * @throws SPFormatException never
	 */
	@Override
	public Phoenix produce(final PlayerCollection players) throws SPFormatException {
		return new Phoenix();
	}
	/**
	 * Check the node for invalid data. A Phoenix is valid if it has no children.
	 * @throws SPFormatException if the node contains invalid data
	 */
	@Override
	public void checkNode() throws SPFormatException {
		if (iterator().hasNext()) {
			throw new SPFormatException("Phoenix shouldn't have children", getLine());
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
		return "PhoenixNode";
	}
}
