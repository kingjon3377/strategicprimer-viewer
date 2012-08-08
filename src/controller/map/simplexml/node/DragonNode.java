package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.fixtures.Dragon;
import util.EqualsAny;
import util.Warning;
import controller.map.SPFormatException;
import controller.map.misc.IDFactory;

/**
 * A Node to represent a dragon.
 *
 * @author Jonathan Lovelace
 * @deprecated Replaced by ReaderNG.
 */
@Deprecated
public class DragonNode extends AbstractFixtureNode<Dragon> {
	/**
	 * The name of the property saying what kind of dragon.
	 */
	private static final String KIND_PROPERTY = "kind";

	/**
	 * Constructor.
	 */
	public DragonNode() {
		super(Dragon.class);
	}

	/**
	 * @param players ignored
	 * @param warner a Warning instance to use for warnings
	 * @return the dragon this represents
	 * @throws SPFormatException if missing a required attribute
	 */
	@Override
	public Dragon produce(final PlayerCollection players, final Warning warner)
			throws SPFormatException {
		return new Dragon(getProperty(KIND_PROPERTY),
				Integer.parseInt(getProperty("id")), getProperty("file"));
	}

	/**
	 * Check the node for invalid data. A Dragon is valid if it has no children
	 * and has a "kind" property.
	 *
	 * @param warner a Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate
	 *        new ones as needed
	 * @throws SPFormatException if the node contains invalid data
	 */
	@Override
	public void checkNode(final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		forbidChildren("dragon");
		demandProperty("dragon", KIND_PROPERTY, warner, false, true);
		registerOrCreateID("dragon", idFactory, warner);
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
		return "DragonNode";
	}
}
