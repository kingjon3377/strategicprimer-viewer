package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.fixtures.Fortress;
import util.EqualsAny;
import util.Warning;
import controller.map.SPFormatException;
import controller.map.UnwantedChildException;
import controller.map.misc.IDFactory;

/**
 * A node to produce a Fortress.
 * 
 * @author Jonathan Lovelace
 */
@Deprecated
public class FortressNode extends AbstractFixtureNode<Fortress> {
	/**
	 * The tag.
	 */
	private static final String TAG = "fortress";

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
	 * @param players the players in the map
	 * @param warner a Warning instance to use for warnings
	 * @return the equivalent fortress.
	 * @throws SPFormatException if this node contains invalid data.
	 */
	@Override
	public Fortress produce(final PlayerCollection players, final Warning warner)
			throws SPFormatException {
		final Fortress fort = new Fortress(
				players.getPlayer(hasProperty(OWNER_PROP) ? Integer
						.parseInt(getProperty(OWNER_PROP)) : -1),
				hasProperty(NAME_PROP) ? getProperty(NAME_PROP) : "",
				Integer.parseInt(getProperty("id")), getProperty("file"));
		for (final AbstractXMLNode node : this) {
			if (node instanceof UnitNode) {
				fort.addUnit(((UnitNode) node).produce(players, warner));
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
		return EqualsAny.equalsAny(property, OWNER_PROP, NAME_PROP, "id");
	}

	/**
	 * Check whether we contain valid data. A Fortress contains valid data iff
	 * every child is a Unit. At present we don't require it to have any
	 * properties, not even "owner" or "name"; if or when that changes, this
	 * should change to check those conditions.
	 * 
	 * 
	 * @param warner a Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate
	 *        new ones as needed
	 * @throws SPFormatException if we don't.
	 */
	@Override
	public void checkNode(final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		for (final AbstractXMLNode node : this) {
			if (node instanceof UnitNode) {
				node.checkNode(warner, idFactory);
			} else {
				throw new UnwantedChildException(TAG, node.toString(),
						getLine());
			}
		}
		demandProperty(TAG, OWNER_PROP, warner, true, false);
		demandProperty(TAG, NAME_PROP, warner, true, false);
		registerOrCreateID(TAG, idFactory, warner);
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
