package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.fixtures.Oasis;
import util.Warning;
import controller.map.MissingParameterException;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;
import controller.map.misc.IDFactory;

/**
 * A Node to produce an Oasis.
 * @author Jonathan Lovelace
 *
 */
@Deprecated
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
		return new Oasis(Long.parseLong(getProperty("id")));
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
		} else if (hasProperty("id")) {
			IDFactory.FACTORY.register(Long.parseLong(getProperty("id")));
		} else {
			warner.warn(new MissingParameterException("oasis", "id", getLine()));
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
	 * @return a String representation of the node
	 */
	@Override
	public String toString() {
		return "OasisNode";
	}
}
