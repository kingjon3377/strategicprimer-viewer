package controller.map.simplexml.node;

import util.EqualsAny;
import util.Warning;
import model.map.PlayerCollection;
import model.map.events.TownStatus;
import model.map.fixtures.Mine;
import controller.map.SPFormatException;

/**
 * A Node that will produce a Mine.
 * @author Jonathan Lovelace
 */
public class MineNode extends AbstractFixtureNode<Mine> {
	/**
	 * The name of the property saying what kind of thing is mined in this mine.
	 */
	private static final String KIND_PROPERTY = "kind";
	/**
	 * Constructor.
	 */
	public MineNode() {
		super(Mine.class);
	}
	/**
	 * @param players ignored
	 * @return the Mine this node represents
	 * @throws SPFormatException if missing required properties
	 */
	@Override
	public Mine produce(final PlayerCollection players) throws SPFormatException {
		return new Mine(getProperty(KIND_PROPERTY), TownStatus.parseTownStatus(getProperty("status")));
	}
	/**
	 * Check the data for validity. A Mine is valid if it has no children and "product" and "status" properties.
	 * @throws SPFormatException if the node is invalid.
	 */
	@Override
	public void checkNode() throws SPFormatException {
		if (iterator().hasNext()) {
			throw new SPFormatException("Mine should not have children", getLine());
		} else if (hasProperty(KIND_PROPERTY)) {
			if (!hasProperty("status")) {
				throw new SPFormatException("Mine should have \"kind\" and \"status\" properties", getLine());
			}
		} else {
			if (hasProperty("product")) {
				Warning.warn(new SPFormatException(
						"Use of property \"product\" to designate mine product is deprecated; use \"kind\" instead",
						getLine()));
				addProperty(KIND_PROPERTY, getProperty("product"));
			} else {
				throw new SPFormatException("Mine should have \"kind\" and \"status\" properties", getLine());
			}
		}
	}
	/**
	 * @param property the name of a property
	 * @return whether this kind of node can use the property
	 */
	@Override
	public boolean canUse(final String property) {
		return EqualsAny.equalsAny(property, KIND_PROPERTY, "product", "status");
	}
	/**
	 * @return a String representation of the node
	 */
	@Override
	public String toString() {
		return "MineNode";
	}
}
