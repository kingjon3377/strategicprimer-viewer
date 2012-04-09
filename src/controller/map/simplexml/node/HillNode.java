package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.fixtures.Hill;
import util.Warning;
import controller.map.SPFormatException;

/**
 * A Node to produce a Hill.
 * @author Jonathan Lovelace
 *
 */
public class HillNode extends AbstractFixtureNode<Hill> {
	/**
	 * Constructor.
	 */
	public HillNode() {
		super(Hill.class);
	}
	/**
	 * @param players ignored
	 * @return the Hill this represents
	 * @throws SPFormatException never
	 */
	@Override
	public Hill produce(final PlayerCollection players) throws SPFormatException {
		return new Hill();
	}
	/**
	 * check that the node is valid. A Hill is valid if it has no children. TODO: should it have attributes?
	 * @param warner a Warning instance to use for warnings
	 * @throws SPFormatException if the node is invalid
	 */
	@Override
	public void checkNode(final Warning warner) throws SPFormatException {
		if (iterator().hasNext()) {
			throw new SPFormatException("Hill shouldn't have children", getLine());
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
		return "HillNode";
	}
}
