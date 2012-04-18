package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.fixtures.Fairy;
import util.Warning;
import controller.map.MissingParameterException;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;

/**
 * A Node to represent a fairy or group of fairies.
 * @author Jonathan Lovelace
 *
 */
public class FairyNode extends AbstractFixtureNode<Fairy> {
	/**
	 * The name of the property saying what kind of fairy.
	 */
	private static final String KIND_PROPERTY = "kind";
	/**
	 * Constructor.
	 */
	public FairyNode() {
		super(Fairy.class);
	}
	/**
	 * @param players ignored
	 * @param warner a Warning instance to use for warnings
	 * @return the fairy this represents
	 * @throws SPFormatException if missing a required attribute
	 */
	@Override
	public Fairy produce(final PlayerCollection players, final Warning warner) throws SPFormatException {
		return new Fairy(getProperty(KIND_PROPERTY));
	}
	/**
	 * Check the node for invalid data. A Fairy is valid if it has no children and has a "kind" property.
	 * @param warner a Warning instance to use for warnings
	 * @throws SPFormatException if the node contains invalid data
	 */
	@Override
	public void checkNode(final Warning warner) throws SPFormatException {
		if (iterator().hasNext()) {
			throw new UnwantedChildException("fairy", iterator().next()
					.toString(), getLine());
		} else if (!hasProperty(KIND_PROPERTY)) {
			throw new MissingParameterException("fairy", KIND_PROPERTY, getLine());
		}
	}
	/**
	 * @param property the name of a property
	 * @return whether this kind of node can use the property
	 */
	@Override
	public boolean canUse(final String property) {
		return KIND_PROPERTY.equals(property);
	}
	/**
	 * @return a String representation of the node.
	 */
	@Override
	public String toString() {
		return "FairyNode";
	}
}
