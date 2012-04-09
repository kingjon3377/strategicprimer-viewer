package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.fixtures.Griffin;
import util.Warning;
import controller.map.SPFormatException;

/**
 * A Node to represent a griffin or group of griffins.
 * @author Jonathan Lovelace
 *
 */
public class GriffinNode extends AbstractFixtureNode<Griffin> {
	/**
	 * Constructor.
	 */
	public GriffinNode() {
		super(Griffin.class);
	}
	/**
	 * @param players ignored
	 * @param warner a Warning instance to use for warnings
	 * @return the griffin this represents
	 * @throws SPFormatException never
	 */
	@Override
	public Griffin produce(final PlayerCollection players, final Warning warner) throws SPFormatException {
		return new Griffin();
	}
	/**
	 * Check the node for invalid data. A Griffin is valid if it has no children.
	 * @param warner a Warning instance to use for warnings
	 * @throws SPFormatException if the node contains invalid data
	 */
	@Override
	public void checkNode(final Warning warner) throws SPFormatException {
		if (iterator().hasNext()) {
			throw new SPFormatException("Griffin shouldn't have children", getLine());
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
		return "GriffinNode";
	}
}
