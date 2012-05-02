package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.fixtures.Minotaur;
import util.Warning;
import controller.map.MissingParameterException;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;
import controller.map.misc.IDFactory;

/**
 * A Node to represent a minotaur or group of minotaurs.
 * @author Jonathan Lovelace
 *
 */
@Deprecated
public class MinotaurNode extends AbstractFixtureNode<Minotaur> {
	/**
	 * Constructor.
	 */
	public MinotaurNode() {
		super(Minotaur.class);
	}
	/**
	 * @param players ignored
	 * @param warner a Warning instance to use for warnings
	 * @return the minotaur this represents
	 * @throws SPFormatException never
	 */
	@Override
	public Minotaur produce(final PlayerCollection players, final Warning warner) throws SPFormatException {
		return new Minotaur(Long.parseLong(getProperty("id")));
	}
	/**
	 * Check the node for invalid data. A Minotaur is valid if it has no children.
	 * @param warner a Warning instance to use for warnings
	 * @throws SPFormatException if the node contains invalid data
	 */
	@Override
	public void checkNode(final Warning warner) throws SPFormatException {
		if (iterator().hasNext()) {
			throw new UnwantedChildException("minotaur", iterator().next()
					.toString(), getLine());
		} else if (hasProperty("id")) {
			IDFactory.FACTORY.register(Long.parseLong(getProperty("id")));
		} else {
			warner.warn(new MissingParameterException("minotaur", "id", getLine()));
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
		return "MinotaurNode";
	}
}
