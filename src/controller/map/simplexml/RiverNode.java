package controller.map.simplexml;

import org.apache.commons.lang.NotImplementedException;

import model.viewer.River;

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
	 * @return a River equivalent to this.
	 * @throws SPFormatException
	 *             if this has unexpected children or doesn't have needed data
	 */
	@Override
	public River produce() throws SPFormatException {
		throw new NotImplementedException(
				"River production not implemented yet.");
	}

	/**
	 * Check the validity of the node. (TODO: eventually we may want to allow
	 * units or even fortresses, etc., in rivers.) A river is valid iff it has
	 * no children and has a direction.
	 * 
	 * @throws SPFormatException
	 *             if the River contains anythig.
	 * 
	 * @see controller.map.simplexml.AbstractXMLNode#checkNode()
	 */
	@Override
	public void checkNode() throws SPFormatException {
		if (iterator().hasNext()) {
			throw new SPFormatException("River has sub-tags", getLine());
		} else if (!hasProperty("direction")) {
			throw new SPFormatException("River should have a direction", getLine());
		}
	}

}
