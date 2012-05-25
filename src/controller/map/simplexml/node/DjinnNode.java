package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.fixtures.Djinn;
import util.Warning;
import controller.map.SPFormatException;
import controller.map.misc.IDFactory;

/**
 * A Node to represent a djinn or group of djinni.
 * @author Jonathan Lovelace
 *
 */
@Deprecated
public class DjinnNode extends AbstractFixtureNode<Djinn> {
	/**
	 * Constructor.
	 */
	public DjinnNode() {
		super(Djinn.class);
	}
	/**
	 * @param players ignored
	 * @param warner a Warning instance to use for warnings
	 * @return the djinn this represents
	 * @throws SPFormatException never
	 */
	@Override
	public Djinn produce(final PlayerCollection players, final Warning warner) throws SPFormatException {
		return new Djinn(Integer.parseInt(getProperty("id")), getProperty("file"));
	}
	/**
	 * Check the node for invalid data. A Djinn is valid i it has no children.
	 * @param warner a Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate new ones as needed
	 * @throws SPFormatException if the node contains invalid data
	 */
	@Override
	public void checkNode(final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		forbidChildren("djinn");
		registerOrCreateID("djinn", idFactory, warner);
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
		return "DjinnNode";
	}
}
