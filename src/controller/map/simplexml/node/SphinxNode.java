package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.fixtures.Sphinx;
import util.Warning;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;

/**
 * A Node to represent a Sphinx.
 * @author Jonathan Lovelace
 *
 */
@Deprecated
public class SphinxNode extends AbstractFixtureNode<Sphinx> {
	/**
	 * Constructor.
	 */
	public SphinxNode() {
		super(Sphinx.class);
	}
	/**
	 * @param players ignored
	 * @param warner a Warning instance to use for warnings
	 * @return the Sphinx this represents
	 * @throws SPFormatException never
	 */
	@Override
	public Sphinx produce(final PlayerCollection players, final Warning warner) throws SPFormatException {
		return new Sphinx();
	}
	/**
	 * Check the node for invalid data. A Sphinx is valid if it has no children.
	 * @param warner a Warning instance to use for warnings
	 * @throws SPFormatException if the node contains invalid data
	 */
	@Override
	public void checkNode(final Warning warner) throws SPFormatException {
		if (iterator().hasNext()) {
			throw new UnwantedChildException("sphinx", iterator().next()
					.toString(), getLine());
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
