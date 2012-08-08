package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.fixtures.Sandbar;
import util.Warning;
import controller.map.SPFormatException;
import controller.map.misc.IDFactory;

/**
 * A Node to produce a Sandbar.
 *
 * @author Jonathan Lovelace
 * @deprecated Replaced by ReaderNG.
 */
@Deprecated
public class SandbarNode extends AbstractFixtureNode<Sandbar> {
	/**
	 * Constructor.
	 */
	public SandbarNode() {
		super(Sandbar.class);
	}

	/**
	 * @param players ignored
	 * @param warner a Warning instance to use for warnings
	 * @return the Sandbar this Node represents
	 * @throws SPFormatException never
	 */
	@Override
	public Sandbar produce(final PlayerCollection players, final Warning warner)
			throws SPFormatException {
		return new Sandbar(Integer.parseInt(getProperty("id")),
				getProperty("file"));
	}

	/**
	 * Check this node for validity. A Sandbar is valid if it has no children.
	 *
	 * @param warner a Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate
	 *        new ones as needed
	 * @throws SPFormatException if the node is invalid.
	 */
	@Override
	public void checkNode(final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		forbidChildren("sandbar");
		registerOrCreateID("sandbar", idFactory, warner);
	}

	/**
	 * @param property the name of a property
	 * @return whether this kind of node can use the property
	 */
	@Override
	public boolean canUse(final String property) {
		return "id".equals(property);
	}

	/**
	 * @return a String representation of the node
	 */
	@Override
	public String toString() {
		return "SandbarNode";
	}
}
