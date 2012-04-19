package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.fixtures.Unit;
import util.EqualsAny;
import util.Warning;
import controller.map.DeprecatedPropertyException;
import controller.map.MissingParameterException;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;

/**
 * A Node to represent a Unit.
 * 
 * @author kingjon
 * 
 */
@Deprecated
public class UnitNode extends AbstractFixtureNode<Unit> {
	/**
	 * The old, deprecated name for what is now TYPE_ATTR.
	 */
	private static final String OLD_TYPE_ATTR = "type";
	/**
	 * The tag.
	 */
	private static final String TAG = "unit";

	/**
	 * Constructor.
	 */
	public UnitNode() {
		super(Unit.class);
	}

	/**
	 * The "name" attribute.
	 */
	private static final String NAME_ATTR = "name";
	/**
	 * The "type" attribute.
	 */
	private static final String TYPE_ATTR = "kind";
	/**
	 * The "owner" attribute.
	 */
	private static final String OWNER_ATTR = "owner";

	/**
	 * Produce the equivalent Unit.
	 * 
	 * @param players
	 *            the players in the map
	 * @param warner
	 *            a Warning instance to use for warnings
	 * @return the equivalent Unit.
	 * @throws SPFormatException
	 *             if we contain invalid data.
	 */
	@Override
	public Unit produce(final PlayerCollection players, final Warning warner)
			throws SPFormatException {
		return new Unit(players.getPlayer((hasProperty(OWNER_ATTR) && !""
				.equals(getProperty(OWNER_ATTR))) ? Integer
				.parseInt(getProperty(OWNER_ATTR)) : -1),
				hasProperty(TYPE_ATTR) ? getProperty(TYPE_ATTR) : "",
				hasProperty(NAME_ATTR) ? getProperty(NAME_ATTR) : "");
	}

	/**
	 * Check whether we contain any invalid data. At present, this merely means
	 * that the unit can't have any children, as neither of the properties we
	 * recognize ("owner" and "kind") do we require, and for forward
	 * compatibility we don't object to properties we don't recognize. But if at
	 * some point we should start requiring properties, that condition should be
	 * checked here.
	 * 
	 * 
	 * @param warner
	 *            a Warning instance to use for warnings
	 * @throws SPFormatException
	 *             if contain invalid data.
	 */
	@Override
	public void checkNode(final Warning warner) throws SPFormatException {
		if (iterator().hasNext()) {
			throw new UnwantedChildException(TAG, iterator().next()
					.toString(), getLine());
		} else if (!hasProperty(OWNER_ATTR) || "".equals(getProperty(OWNER_ATTR))) {
			warner.warn(new MissingParameterException(TAG, OWNER_ATTR,
					getLine()));
		}
		if (!hasProperty(TYPE_ATTR) || "".equals(getProperty(TYPE_ATTR))) {
			if (hasProperty(OLD_TYPE_ATTR)) {
				addProperty(TYPE_ATTR, getProperty(OLD_TYPE_ATTR), warner);
				warner.warn(new DeprecatedPropertyException(TAG, OLD_TYPE_ATTR, TYPE_ATTR,
						getLine()));
			} else {
				warner.warn(new MissingParameterException(TAG, TYPE_ATTR,
						getLine()));
			}
		}
		if (!hasProperty(NAME_ATTR) || "".equals(getProperty(NAME_ATTR))) {
			warner.warn(new MissingParameterException(TAG, NAME_ATTR,
					getLine()));
		}
	}

	/**
	 * @param property
	 *            the name of a property
	 * @return whether this kind of node can use the property
	 */
	@Override
	public boolean canUse(final String property) {
		return EqualsAny.equalsAny(property, OWNER_ATTR, TYPE_ATTR, NAME_ATTR, OLD_TYPE_ATTR);
	}

	/**
	 * 
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "UnitNode";
	}
}
