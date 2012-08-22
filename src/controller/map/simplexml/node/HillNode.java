package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.fixtures.terrain.Hill;
import util.Warning;
import controller.map.SPFormatException;
import controller.map.misc.IDFactory;

/**
 * A Node to produce a Hill.
 *
 * @author Jonathan Lovelace
 * @deprecated Replaced by ReaderNG.
 */
@Deprecated
public class HillNode extends AbstractFixtureNode<Hill> {
	/**
	 * Constructor.
	 */
	public HillNode() {
		super(Hill.class);
	}

	/**
	 * @param players ignored
	 * @param warner a Warning instance to use for warnings
	 * @return the Hill this represents
	 * @throws SPFormatException never
	 */
	@Override
	public Hill produce(final PlayerCollection players, final Warning warner)
			throws SPFormatException {
		return new Hill(Integer.parseInt(getProperty("id")),
				getProperty("file"));
	}

	/**
	 * check that the node is valid. A Hill is valid if it has no children.
	 * TODO: should it have attributes?
	 *
	 * @param warner a Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate
	 *        new ones as needed
	 * @throws SPFormatException if the node is invalid
	 */
	@Override
	public void checkNode(final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		forbidChildren("hill");
		registerOrCreateID("hill", idFactory, warner);
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
		return "HillNode";
	}
}
