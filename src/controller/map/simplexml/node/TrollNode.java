package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.fixtures.Troll;
import controller.map.SPFormatException;

/**
 * A Node to represent a Troll.
 * @author Jonathan Lovelace
 *
 */
public class TrollNode extends AbstractFixtureNode<Troll> {
	/**
	 * Constructor.
	 */
	public TrollNode() {
		super(Troll.class);
	}
	/**
	 * @param players ignored
	 * @return the Troll this represents
	 * @throws SPFormatException never
	 */
	@Override
	public Troll produce(final PlayerCollection players) throws SPFormatException {
		return new Troll();
	}
	/**
	 * Check the node for invalid data. A Troll is valid if it has no children.
	 * @throws SPFormatException if the node contains invalid data
	 */
	@Override
	public void checkNode() throws SPFormatException {
		if (iterator().hasNext()) {
			throw new SPFormatException("Troll shouldn't have children", getLine());
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
		return "TrollNode";
	}
}
