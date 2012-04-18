package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.fixtures.Oasis;
import util.Warning;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;

/**
 * A Node to produce an Oasis.
 * @author Jonathan Lovelace
 *
 */
public class OasisNode extends AbstractFixtureNode<Oasis> {
	/**
	 * Constructor.
	 */
	public OasisNode() {
		super(Oasis.class);
	}
	/**
	 * @param players ignored
	 * @param warner a Warning instance to use for warnings
	 * @return the Oasis this represents
	 * @throws SPFormatException never
	 */
	@Override
	public Oasis produce(final PlayerCollection players, final Warning warner) throws SPFormatException {
		return new Oasis();
	}
	/**
	 * Check that the noe is valid. An Oasis is valid if it has no children. TODO: should it have attributes?
	 * @param warner a Warning instance to use for warnings
	 * @throws SPFormatException if the node isn't valid
	 */
	@Override
	public void checkNode(final Warning warner) throws SPFormatException {
		if (iterator().hasNext()) {
			throw new UnwantedChildException("oasis", iterator().next()
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
	 * @return a String representation of the node
	 */
	@Override
	public String toString() {
		return "OasisNode";
	}
}
