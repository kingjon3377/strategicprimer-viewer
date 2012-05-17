package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.fixtures.Griffin;
import util.Warning;
import controller.map.SPFormatException;
import controller.map.misc.IDFactory;

/**
 * A Node to represent a griffin or group of griffins.
 * @author Jonathan Lovelace
 *
 */
@Deprecated
public class GriffinNode extends AbstractFixtureNode<Griffin> {
	/**
	 * Constructor.
	 */
	public GriffinNode() {
		super(Griffin.class);
	}
	/**
	 * @param players ignored
	 * @param warner a Warning instance to use for warnings
	 * @return the griffin this represents
	 * @throws SPFormatException never
	 */
	@Override
	public Griffin produce(final PlayerCollection players, final Warning warner) throws SPFormatException {
		final Griffin fix = new Griffin(Integer.parseInt(getProperty("id")));
		if (hasProperty("file")) {
			fix.setFile(getProperty("file"));
		}
		return fix;
	}
	/**
	 * Check the node for invalid data. A Griffin is valid if it has no children.
	 * @param warner a Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate new ones as needed
	 * @throws SPFormatException if the node contains invalid data
	 */
	@Override
	public void checkNode(final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		forbidChildren("griffin");
		registerOrCreateID("griffin", idFactory, warner);
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
		return "GriffinNode";
	}
}
