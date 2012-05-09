package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.fixtures.Giant;
import util.EqualsAny;
import util.Warning;
import controller.map.MissingParameterException;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;
import controller.map.misc.IDFactory;

/**
 * A Node to represent a giant.
 * @author Jonathan Lovelace
 *
 */
@Deprecated
public class GiantNode extends AbstractFixtureNode<Giant> {
	/**
	 * The name of the property saying what kind of giant this is.
	 */
	private static final String KIND_PROPERTY = "kind";
	/**
	 * Constructor.
	 */
	public GiantNode() {
		super(Giant.class);
	}
	/**
	 * @param players ignored
	 * @param warner a Warning instance to use for warnings
	 * @return the giant this represents
	 * @throws SPFormatException if missing a required attribute
	 */
	@Override
	public Giant produce(final PlayerCollection players, final Warning warner) throws SPFormatException {
		return new Giant(getProperty(KIND_PROPERTY), Long.parseLong(getProperty("id")));
	}
	/**
	 * Check the node for invalid data. A Giant is valid if it has no children and has a "kind" property.
	 * @param warner a Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate new ones as needed
	 * @throws SPFormatException if the node contains invalid data
	 */
	@Override
	public void checkNode(final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		if (iterator().hasNext()) {
			throw new UnwantedChildException("giant", iterator().next()
					.toString(), getLine());
		} else if (hasProperty(KIND_PROPERTY)) {
			if (hasProperty("id")) {
				idFactory.register(Long.parseLong(getProperty("id")));
			} else {
				addProperty("id", Long.toString(idFactory.getID()), warner);
				warner.warn(new MissingParameterException("giant", "id", getLine()));
			}
		} else {
			throw new MissingParameterException("giant", KIND_PROPERTY, getLine());
		}
	}
	/**
	 * @param property the name of a property
	 * @return whether this kind of node can use the property
	 */
	@Override
	public boolean canUse(final String property) {
		return EqualsAny.equalsAny(property, KIND_PROPERTY, "id");
	}
	/**
	 * @return a String representation of the node.
	 */
	@Override
	public String toString() {
		return "GiantNode";
	}
}
