package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.fixtures.Centaur;
import util.Warning;
import controller.map.MissingParameterException;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;

/**
 * A Node to represent a centaur or group of centaurs.
 * @author Jonathan Lovelace
 */
@Deprecated
public class CentaurNode extends AbstractFixtureNode<Centaur> {
	/**
	 * The name of the property saying what kind of centaur.
	 */
	private static final String KIND_PROPERTY = "kind";
	/**
	 * Constructor.
	 */
	public CentaurNode() {
		super(Centaur.class);
	}
	/**
	 * @param players ignored
	 * @param warner a Warning instance to use for warnings
	 * @return the centaur this represents
	 * @throws SPFormatException
	 *             if it's missing a required attribute
	 */
	@Override
	public Centaur produce(final PlayerCollection players, final Warning warner) throws SPFormatException {
		return new Centaur(getProperty(KIND_PROPERTY));
	}
	
	/**
	 * Check the node for invalid data. A Centaur is valid if it has no children
	 * and has a "kind" property.
	 * 
	 * @param warner
	 *            a Warning instance to use for warnings
	 * @throws SPFormatException
	 *             if the node contains invalid data.
	 */
	@Override
	public void checkNode(final Warning warner) throws SPFormatException {
		if (iterator().hasNext()) {
			throw new UnwantedChildException("centaur", iterator().next()
					.toString(), getLine());
		} else if (!hasProperty(KIND_PROPERTY)) {
			throw new MissingParameterException("centaur", KIND_PROPERTY, getLine());
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
		return "CentaurNode";
	}
}
