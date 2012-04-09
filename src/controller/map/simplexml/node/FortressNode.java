package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.fixtures.Fortress;
import util.EqualsAny;
import util.Warning;
import controller.map.SPFormatException;

/**
 * A node to produce a Fortress.
 * 
 * @author Jonathan Lovelace
 */
public class FortressNode extends AbstractFixtureNode<Fortress> {
	/**
	 * Constructor.
	 */
	public FortressNode() {
		super(Fortress.class);
	}
	/**
	 * The (name of) the "name" property.
	 */
	private static final String NAME_PROP = "name";
	/**
	 * The (name of) the "owner" property.
	 */
	private static final String OWNER_PROP = "owner";

	/**
	 * Produce the equivalent fortress.
	 * 
	 * @param players
	 *            the players in the map
	 * @param warner
	 *            a Warning instance to use for warnings
	 * @return the equivalent fortress.
	 * @throws SPFormatException
	 *             if this node contains invalid data.
	 */
	@Override
	public Fortress produce(final PlayerCollection players, final Warning warner)
			throws SPFormatException {
		final Fortress fort = new Fortress(
				players.getPlayer(hasProperty(OWNER_PROP) ? Integer
						.parseInt(getProperty(OWNER_PROP)) : -1),
				hasProperty(NAME_PROP) ? getProperty(NAME_PROP) : "");
		for (final AbstractXMLNode node : this) {
			if (node instanceof UnitNode) {
				fort.addUnit(((UnitNode) node).produce(players, warner));
			} else {
				throw new SPFormatException(
						"Fortress should contain only units", node.getLine());
			}
		}
		return fort;
	}
	/**
	 * @param property the name of a property
	 * @return whether this kind of node can use the property
	 */
	@Override
	public boolean canUse(final String property) {
		return EqualsAny.equalsAny(property, OWNER_PROP, NAME_PROP);
	}

	/**
	 * Check whether we contain valid data. A Fortress contains valid data iff
	 * every child is a Unit. At present we don't require it to have any
	 * properties, not even "owner" or "name"; if or when that changes, this
	 * should change to check those conditions.
	 * 
	 * 
	 * @param warner
	 *            a Warning instance to use for warnings
	 * @throws SPFormatException
	 *             if we don't.
	 */
	@Override
	public void checkNode(final Warning warner) throws SPFormatException {
		for (final AbstractXMLNode node : this) {
			if (node instanceof UnitNode) {
				node.checkNode(warner);
			} else {
				throw new SPFormatException(
						"Fortress should contain only units", getLine());
			}
		}
		if (!hasProperty(OWNER_PROP)) {
			warner.warn(new SPFormatException("Fortress should have an owner",
					getLine()));
		}
		if (!hasProperty(NAME_PROP)) {
			warner.warn(new SPFormatException("Fortress should have a name",
					getLine()));
		}
	}

	/**
	 * 
	 * @return a String representation of the object
	 */
	@Override
	public String toString() {
		return "FortressNode";
	}
}
