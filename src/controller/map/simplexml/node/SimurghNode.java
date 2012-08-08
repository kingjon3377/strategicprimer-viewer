package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.fixtures.Simurgh;
import util.Warning;
import controller.map.SPFormatException;
import controller.map.misc.IDFactory;

/**
 * A Node to represent a simurgh.
 *
 * @author Jonathan Lovelace
 * @deprecated Replaced by ReaderNG.
 */
@Deprecated
public class SimurghNode extends AbstractFixtureNode<Simurgh> {
	/**
	 * Constructor.
	 */
	public SimurghNode() {
		super(Simurgh.class);
	}

	/**
	 * @param players ignored
	 * @param warner a Warning instance to use for warnings
	 * @return the simurgh this represents
	 * @throws SPFormatException never
	 */
	@Override
	public Simurgh produce(final PlayerCollection players, final Warning warner)
			throws SPFormatException {
		return new Simurgh(Integer.parseInt(getProperty("id")),
				getProperty("file"));
	}

	/**
	 * Check the node for invalid data. A Simurgh is valid if it has no
	 * children.
	 *
	 * @param warner a Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate
	 *        new ones as needed
	 * @throws SPFormatException if the node contains invalid data
	 */
	@Override
	public void checkNode(final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		forbidChildren("simurgh");
		registerOrCreateID("simurgh", idFactory, warner);
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
		return "SimurghNode";
	}
}
