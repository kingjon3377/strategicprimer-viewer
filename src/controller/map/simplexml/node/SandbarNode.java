package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.fixtures.Sandbar;
import controller.map.SPFormatException;
/**
 * A Node to produce a Sandbar.
 * @author Jonathan Lovelace
 *
 */
public class SandbarNode extends AbstractFixtureNode<Sandbar> {
	/**
	 * Constructor.
	 */
	public SandbarNode() {
		super(Sandbar.class);
	}
	/**
	 * @param players ignored
	 * @return the Sandbar this Node represents
	 * @throws SPFormatException never
	 */
	@Override
	public Sandbar produce(final PlayerCollection players) throws SPFormatException {
		return new Sandbar();
	}
	/**
	 * Check this node for validity. A Sandbar is valid if it has no children.
	 * @throws SPFormatException if the node is invalid.
	 */
	@Override
	public void checkNode() throws SPFormatException {
		if (iterator().hasNext()) {
			throw new SPFormatException("Sandbar shouldn't have children", getLine());
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
		return "SandbarNode";
	}
}
