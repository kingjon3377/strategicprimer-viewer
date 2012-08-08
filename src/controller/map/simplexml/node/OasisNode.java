package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.fixtures.Oasis;
import util.Warning;
import controller.map.SPFormatException;
import controller.map.misc.IDFactory;

/**
 * A Node to produce an Oasis.
 *
 * @author Jonathan Lovelace
 * @deprecated Replaced by ReaderNG.
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
	public Oasis produce(final PlayerCollection players, final Warning warner)
			throws SPFormatException {
		return new Oasis(Integer.parseInt(getProperty("id")),
				getProperty("file"));
	}

	/**
	 * Check that the noe is valid. An Oasis is valid if it has no children.
	 * TODO: should it have attributes?
	 *
	 * @param warner a Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate
	 *        new ones as needed
	 * @throws SPFormatException if the node isn't valid
	 */
	@Override
	public void checkNode(final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		forbidChildren("oasis");
		registerOrCreateID("oasis", idFactory, warner);
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
