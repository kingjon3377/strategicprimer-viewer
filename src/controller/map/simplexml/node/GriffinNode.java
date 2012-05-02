package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.fixtures.Griffin;
import util.Warning;
import controller.map.MissingParameterException;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;
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
		return new Griffin(Long.parseLong(getProperty("id")));
	}
	/**
	 * Check the node for invalid data. A Griffin is valid if it has no children.
	 * @param warner a Warning instance to use for warnings
	 * @throws SPFormatException if the node contains invalid data
	 */
	@Override
	public void checkNode(final Warning warner) throws SPFormatException {
		if (iterator().hasNext()) {
			throw new UnwantedChildException("griffin", iterator().next()
					.toString(), getLine());
		} else if (hasProperty("id")) {
			IDFactory.FACTORY.register(Long.parseLong(getProperty("id")));
		} else {
			warner.warn(new MissingParameterException("griffin", "id", getLine()));
			addProperty("id", Long.toString(IDFactory.FACTORY.getID()), warner);
		}
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
