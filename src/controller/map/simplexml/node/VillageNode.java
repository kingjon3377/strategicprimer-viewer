package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.events.TownStatus;
import model.map.fixtures.Village;
import util.EqualsAny;
import util.Warning;
import controller.map.MissingParameterException;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;
import controller.map.misc.IDFactory;

/**
 * A Node to produce a Village.
 * @author Jonathan Lovelace
 *
 */
@Deprecated
public class VillageNode extends AbstractFixtureNode<Village> {
	/**
	 * The name of the property giving the village status.
	 */
	private static final String STATUS_PROPERTY = "status";
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
		return new Village(
				TownStatus.parseTownStatus(getProperty(STATUS_PROPERTY)),
				hasProperty(NAME_PROPERTY) ? getProperty(NAME_PROPERTY) : "",
				Long.parseLong(getProperty("id")));
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
			throw new UnwantedChildException("village", iterator().next()
					.toString(), getLine());
		} else if (hasProperty(STATUS_PROPERTY)) {
			if (!hasProperty(NAME_PROPERTY)) {
				warner.warn(new MissingParameterException("village",
						NAME_PROPERTY, getLine()));
			}
			if (hasProperty("id")) {
				IDFactory.FACTORY.register(Long.parseLong(getProperty("id")));
			} else {
				warner.warn(new MissingParameterException("village", "id", getLine()));
				addProperty("id", Long.toString(IDFactory.FACTORY.getID()), warner);
			}
		} else {
			throw new MissingParameterException("village", STATUS_PROPERTY, getLine());
		}
	}
	/**
	 * @param property the name of a property
	 * @return whether this kind of node can use the property
	 */
	@Override
	public boolean canUse(final String property) {
		return EqualsAny.equalsAny(property, STATUS_PROPERTY, NAME_PROPERTY, "id");
	}
	/**
	 * @return a String representation of the node
	 */
	@Override
	public String toString() {
		return "VillageNode";
	}
}
