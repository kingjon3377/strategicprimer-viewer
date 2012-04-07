package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.fixtures.Ground;
import util.EqualsAny;
import util.Warning;
import controller.map.SPFormatException;
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
	 * @return the Ground this node represents.
	 * @throws SPFormatException if the element was missing any required properties
	 */
	@Override
	public Ground produce(final PlayerCollection players) throws SPFormatException {
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
	 * @throws SPFormatException
	 *             if any required properties are missing.
	 */
	@Override
	public void checkNode() throws SPFormatException {
		if (iterator().hasNext()) {
			throw new SPFormatException("Ground shouldn't have children", getLine());
		} else if (hasProperty(KIND_PROPERTY)) {
			if (!hasProperty("exposed")) {
				throw new SPFormatException("Ground must have \"kind\" and \"exposed\" properties", getLine());
			}
		} else {
			if (hasProperty("ground")) {
				Warning.INSTANCE.warn(new SPFormatException(
						"Use of \"ground\" property to designate kind of ground is deprecated; use \"kind\" instead",
						getLine()));
				addProperty(KIND_PROPERTY, getProperty("ground"));
			} else {
				throw new SPFormatException(
						"Ground must have \"kind\" and \"exposed\" properties",
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
