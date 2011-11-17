package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.fixtures.Oasis;
import controller.map.SPFormatException;

/**
 * A Node to produce an Oasis.
 * @author Jonathan Lovelace
 *
 */
public class OasisNode extends AbstractFixtureNode<Oasis> {
	/**
	 * @param players ignored
	 * @return the Oasis this represents
	 * @throws SPFormatException never
	 */
	@Override
	public Oasis produce(final PlayerCollection players) throws SPFormatException {
		return new Oasis();
	}
	/**
	 * Check that the noe is valid. An Oasis is valid if it has no children. TODO: should it have attributes?
	 * @throws SPFormatException if the node isn't valid
	 */
	@Override
	public void checkNode() throws SPFormatException {
		if (iterator().hasNext()) {
			throw new SPFormatException("Oasis shouldn't have children", getLine());
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
	 * @return a String representation of the node
	 */
	@Override
	public String toString() {
		return "OasisNode";
	}
}
