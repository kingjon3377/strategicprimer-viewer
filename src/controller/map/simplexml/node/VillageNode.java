package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.events.TownStatus;
import model.map.fixtures.Village;
import util.EqualsAny;
import util.Warning;
import controller.map.SPFormatException;

/**
 * A Node to produce a Village.
 * @author Jonathan Lovelace
 *
 */
public class VillageNode extends AbstractFixtureNode<Village> {
	/**
	 * The "name" property.
	 */
	private static final String NAME_PROPERTY = "name";
	/**
	 * Constructor.
	 */
	public VillageNode() {
		super(Village.class);
	}
	/**
	 * @param players ignored
	 * @param warner a Warning instance to use for warnings
	 * @return the Village this Node represents
	 * @throws SPFormatException if missing required attribute.
	 */
	@Override
	public Village produce(final PlayerCollection players, final Warning warner) throws SPFormatException {
		return new Village(TownStatus.parseTownStatus(getProperty("status")),
				hasProperty(NAME_PROPERTY) ? getProperty(NAME_PROPERTY) : "");
	}
	
	/**
	 * Check the node for invalid data. A Village is valid if it has no children
	 * and a "status" field.
	 * 
	 * @param warner a Warning instance to use for warnings
	 * @throws SPFormatException if the node is invalid.
	 */
	@Override
	public void checkNode(final Warning warner) throws SPFormatException {
		if (iterator().hasNext()) {
			throw new SPFormatException("Village shouldn't have children", getLine());
		} else if (hasProperty("status")) {
			if (!hasProperty(NAME_PROPERTY)) {
				warner.warn(new SPFormatException(
						"Village should have \"name\" property", getLine()));
			}
		} else {
			throw new SPFormatException("Village must have \"status\" property", getLine());
		}
	}
	/**
	 * @param property the name of a property
	 * @return whether this kind of node can use the property
	 */
	@Override
	public boolean canUse(final String property) {
		return EqualsAny.equalsAny(property, "status", NAME_PROPERTY);
	}
	/**
	 * @return a String representation of the node
	 */
	@Override
	public String toString() {
		return "VillageNode";
	}
}
