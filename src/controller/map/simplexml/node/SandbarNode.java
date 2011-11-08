package controller.map.simplexml.node;

import controller.map.SPFormatException;
import model.map.PlayerCollection;
import model.map.fixtures.Sandbar;
/**
 * A Node to produce a Sandbar.
 * @author Jonathan Lovelace
 *
 */
public class SandbarNode extends AbstractFixtureNode<Sandbar> {
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
}
