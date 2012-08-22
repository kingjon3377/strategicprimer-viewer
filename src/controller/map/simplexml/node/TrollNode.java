package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.fixtures.mobile.Troll;
import util.Warning;
import controller.map.SPFormatException;
import controller.map.misc.IDFactory;

/**
 * A Node to represent a Troll.
 *
 * @author Jonathan Lovelace
 * @deprecated Replaced by ReaderNG.
 */
@Deprecated
public class TrollNode extends AbstractFixtureNode<Troll> {
	/**
	 * Constructor.
	 */
	public TrollNode() {
		super(Troll.class);
	}

	/**
	 * @param players ignored
	 * @param warner a Warning instance to use for warnings
	 * @return the Troll this represents
	 * @throws SPFormatException never
	 */
	@Override
	public Troll produce(final PlayerCollection players, final Warning warner)
			throws SPFormatException {
		return new Troll(Integer.parseInt(getProperty("id")),
				getProperty("file"));
	}

	/**
	 * Check the node for invalid data. A Troll is valid if it has no children.
	 *
	 * @param warner a Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate
	 *        new ones as needed
	 * @throws SPFormatException if the node contains invalid data
	 */
	@Override
	public void checkNode(final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		forbidChildren("troll");
		registerOrCreateID("troll", idFactory, warner);
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
	 * @return a String representation of the node.
	 */
	@Override
	public String toString() {
		return "TrollNode";
	}
}
