package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.fixtures.Mountain;
import controller.map.SPFormatException;
/**
 * A Node to produce a Mountain.
 * @author Jonathan Lovelace
 *
 */
public class MountainNode extends AbstractFixtureNode<Mountain> {
	/**
	 * @param players ignored
	 * @return the Mountain this represents
	 * @throws SPFormatException never
	 */
	@Override
	public Mountain produce(final PlayerCollection players) throws SPFormatException {
		return new Mountain();
	}
	
	/**
	 * Check that the node is valid. A Mountain is valid if it has no children.
	 * TODO: should it have attributes?
	 * 
	 * @throws SPFormatException if the node isn't valid.
	 */
	@Override
	public void checkNode() throws SPFormatException {
		if (iterator().hasNext()) {
			throw new SPFormatException("Mountain shouldn't have children", getLine());
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
}
