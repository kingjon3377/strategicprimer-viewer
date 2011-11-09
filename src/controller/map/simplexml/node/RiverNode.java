package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.River;
import controller.map.SPFormatException;

/**
 * A node representing a river XML tag.
 * 
 * @author Jonathan Lovelace
 * 
 */
public class RiverNode extends AbstractChildNode<River> {
	/**
	 * We don't *promise* to throw an exception here if there are unexpected
	 * children---still need to call checkNode().
	 * 
	 * @param players
	 *            ignored
	 * @return a River equivalent to this.
	 * @throws SPFormatException
	 *             if this has unexpected children or doesn't have needed data
	 */
	@Override
	public River produce(final PlayerCollection players)
			throws SPFormatException {
		return River.getRiver(getProperty("direction"));
	}

	/**
	 * Check the validity of the node. (TODO: eventually we may want to allow
	 * units or even fortresses, etc., in rivers.) A river is valid iff it has
	 * no children and has a direction.
	 * 
	 * @throws SPFormatException
	 *             if the River contains anythig.
	 */
	@Override
	public void checkNode() throws SPFormatException {
		if (iterator().hasNext()) {
			throw new SPFormatException("River has sub-tags", getLine());
		} else if (!hasProperty("direction")) {
			throw new SPFormatException("River should have a direction",
					getLine());
		}
	}

	/**
	 * 
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "RiverNode";
	}
	/**
	 * @param property the name of a property
	 * @return whether this kind of node can use the property
	 */
	@Override
	public boolean canUse(final String property) {
		return "direction".equals(property);
	}

}
