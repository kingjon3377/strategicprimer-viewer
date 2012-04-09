package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.fixtures.Minotaur;
import util.Warning;
import controller.map.SPFormatException;

/**
 * A Node to represent a minotaur or group of minotaurs.
 * @author Jonathan Lovelace
 *
 */
public class MinotaurNode extends AbstractFixtureNode<Minotaur> {
	/**
	 * Constructor.
	 */
	public MinotaurNode() {
		super(Minotaur.class);
	}
	/**
	 * @param players ignored
	 * @return the minotaur this represents
	 * @throws SPFormatException never
	 */
	@Override
	public Minotaur produce(final PlayerCollection players) throws SPFormatException {
		return new Minotaur();
	}
	/**
	 * Check the node for invalid data. A Minotaur is valid if it has no children.
	 * @param warner a Warning instance to use for warnings
	 * @throws SPFormatException if the node contains invalid data
	 */
	@Override
	public void checkNode(final Warning warner) throws SPFormatException {
		if (iterator().hasNext()) {
			throw new SPFormatException("Minotaur shouldn't have children", getLine());
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
		return "MinotaurNode";
	}
}
