package controller.map.simplexml.node;

import util.EqualsAny;
import util.Warning;
import model.map.PlayerCollection;
import model.map.events.TownStatus;
import model.map.fixtures.Mine;
import controller.map.DeprecatedPropertyException;
import controller.map.MissingParameterException;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;

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
	 * @param warner a Warning instance to use for warnings
	 * @return the Mine this node represents
	 * @throws SPFormatException if missing required properties
	 */
	@Override
	public Mine produce(final PlayerCollection players, final Warning warner) throws SPFormatException {
		return new Mine(getProperty(KIND_PROPERTY), TownStatus.parseTownStatus(getProperty("status")));
	}
	/**
	 * Check the data for validity. A Mine is valid if it has no children and "product" and "status" properties.
	 * @param warner a Warning instance to use for warnings
	 * @throws SPFormatException if the node is invalid.
	 */
	@Override
	public void checkNode(final Warning warner) throws SPFormatException {
		if (iterator().hasNext()) {
			throw new UnwantedChildException("mine", iterator().next()
					.toString(), getLine());
		} else if (hasProperty(KIND_PROPERTY)) {
			if (!hasProperty("status")) {
				throw new MissingParameterException("mine", "status", getLine());
			}
		} else {
			if (hasProperty("product")) {
				warner.warn(new DeprecatedPropertyException("mine", "product",
						KIND_PROPERTY, getLine()));
				addProperty(KIND_PROPERTY, getProperty("product"), warner);
			} else {
				throw new MissingParameterException("mine", KIND_PROPERTY, getLine());
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
