package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.fixtures.Sphinx;
import controller.map.SPFormatException;

/**
 * A Node to represent a Sphinx.
 * @author Jonathan Lovelace
 *
 */
public class SphinxNode extends AbstractFixtureNode<Sphinx> {
	/**
	 * Constructor.
	 */
	public SphinxNode() {
		super(Sphinx.class);
	}
	/**
	 * @param players ignored
	 * @return the Sphinx this represents
	 * @throws SPFormatException never
	 */
	@Override
	public Sphinx produce(final PlayerCollection players) throws SPFormatException {
		return new Sphinx();
	}
	/**
	 * Check the node for invalid data. A Sphinx is valid if it has no children.
	 * @throws SPFormatException if the node contains invalid data
	 */
	@Override
	public void checkNode() throws SPFormatException {
		if (iterator().hasNext()) {
			throw new SPFormatException("Sphinx shouldn't have children", getLine());
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
		return "SphinxNode";
	}
}
