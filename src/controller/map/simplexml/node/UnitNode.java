package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.fixtures.Unit;
import util.EqualsAny;
import util.Warning;
import controller.map.SPFormatException;

/**
 * A Node to represent a Unit.
 * 
 * @author kingjon
 * 
 */
public class UnitNode extends AbstractFixtureNode<Unit> {
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
	 * @return the equivalent Unit.
	 * @throws SPFormatException
	 *             if we contain invalid data.
	 */
	@Override
	public Unit produce(final PlayerCollection players)
			throws SPFormatException {
		return new Unit(players.getPlayer(hasProperty(OWNER_ATTR) ? Integer
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
	 * @throws SPFormatException
	 *             if contain invalid data.
	 */
	@Override
	public void checkNode() throws SPFormatException {
		if (iterator().hasNext()) {
			throw new SPFormatException("Unit should't contain anything",
					getLine());
		}
		if (!hasProperty(OWNER_ATTR) || "".equals(getProperty(OWNER_ATTR))) {
			Warning.INSTANCE.warn(new SPFormatException("Unit should have an owner",
					getLine()));
		}
		if (!hasProperty(TYPE_ATTR) || "".equals(getProperty(TYPE_ATTR))) {
			if (hasProperty("type")) {
				addProperty(TYPE_ATTR, getProperty("type"));
				Warning.INSTANCE.warn(new SPFormatException(
						"Use of property \"type\" to designate kind of unit is deprecated; use \"kind\" instead",
						getLine()));
			} else {
				Warning.INSTANCE.warn(new SPFormatException("Unit should have a kind",
						getLine()));
			}
		}
		if (!hasProperty(NAME_ATTR) || "".equals(getProperty(NAME_ATTR))) {
			Warning.INSTANCE.warn(new SPFormatException("Unit should have a name",
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
		return EqualsAny.equalsAny(property, OWNER_ATTR, TYPE_ATTR, NAME_ATTR, "type");
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
