package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.fixtures.Ground;
import util.EqualsAny;
import util.Warning;
import controller.map.DeprecatedPropertyException;
import controller.map.MissingParameterException;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;
/**
 * A Node to produce a Ground fixture.
 * @author Jonathan Lovelace
 *
 */
public class GroundNode extends AbstractFixtureNode<Ground> {
	/**
	 * The name of the property for what kind of ground.
	 */
	private static final String KIND_PROPERTY = "kind";
	/**
	 * Constructor.
	 */
	public GroundNode() {
		super(Ground.class);
	}
	/**
	 * @param players ignored
	 * @param warner a Warning instance to use for warnings
	 * @return the Ground this node represents.
	 * @throws SPFormatException if the element was missing any required properties
	 */
	@Override
	public Ground produce(final PlayerCollection players, final Warning warner) throws SPFormatException {
		return new Ground(getProperty(KIND_PROPERTY), Boolean.parseBoolean(getProperty("exposed")));
	}
	/**
	 * @param property the name of a property
	 * @return whether this kind of node can use the property
	 */
	@Override
	public boolean canUse(final String property) {
		return EqualsAny.equalsAny(property, KIND_PROPERTY, "exposed", "ground");
	}
	/**
	 * Check whether the node is valid. A Ground is valid if it has "ground" and "exposed"
	 * properties and no children. TODO: add further properties.
	 * 
	 * @param warner a Warning instance to use for warnings
	 * @throws SPFormatException
	 *             if any required properties are missing.
	 */
	@Override
	public void checkNode(final Warning warner) throws SPFormatException {
		if (iterator().hasNext()) {
			throw new UnwantedChildException("ground", iterator().next()
					.toString(), getLine());
		} else if (hasProperty(KIND_PROPERTY)) {
			if (!hasProperty("exposed")) {
				throw new MissingParameterException("ground", "exposed",
						getLine());
			}
		} else {
			if (hasProperty("ground")) {
				warner.warn(new DeprecatedPropertyException("ground", "ground",
						KIND_PROPERTY, getLine()));
				addProperty(KIND_PROPERTY, getProperty("ground"), warner);
			} else {
				throw new MissingParameterException("ground", KIND_PROPERTY,
						getLine());
			}
		}
	}
	/**
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "GroundNode";
	}
}
