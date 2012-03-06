package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.fixtures.Simurgh;
import controller.map.SPFormatException;

/**
 * A Node to represent a simurgh.
 * @author Jonathan Lovelace
 *
 */
public class SimurghNode extends AbstractFixtureNode<Simurgh> {
	/**
	 * Constructor.
	 */
	public SimurghNode() {
		super(Simurgh.class);
	}
	/**
	 * @param players ignored
	 * @return the simurgh this represents
	 * @throws SPFormatException never
	 */
	@Override
	public Simurgh produce(final PlayerCollection players) throws SPFormatException {
		return new Simurgh();
	}
	/**
	 * Check the node for invalid data. A Simurgh is valid if it has no children.
	 * @throws SPFormatException if the node contains invalid data
	 */
	@Override
	public void checkNode() throws SPFormatException {
		if (iterator().hasNext()) {
			throw new SPFormatException("Simurgh shouldn't have children", getLine());
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
		return "SimurghNode";
	}
}
