package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.events.BattlefieldEvent;
import util.EqualsAny;
import util.Warning;
import controller.map.SPFormatException;
import controller.map.misc.IDFactory;

/**
 * A Node representing a BattlefieldEvent.
 * 
 * @author Jonathan Lovelace
 * 
 */
@Deprecated
public class BattlefieldEventNode extends AbstractFixtureNode<BattlefieldEvent> {
	/**
	 * Constructor.
	 */
	public BattlefieldEventNode() {
		super(BattlefieldEvent.class);
	}
	/**
	 * The property of an event saying how difficult it is to find it.
	 */
	private static final String DC_PROPERTY = "dc";
	/**
	 * @param players
	 *            the players on the map
	 * @param warner a Warning instance to use for warnings
	 * @return the equivalent event
	 * @throws SPFormatException
	 *             if this includes invalid data
	 */
	@Override
	public BattlefieldEvent produce(final PlayerCollection players, final Warning warner)
			throws SPFormatException {
		final BattlefieldEvent fix = new BattlefieldEvent(Integer.parseInt(getProperty(DC_PROPERTY)),
				Long.parseLong(getProperty("id")));
		if (hasProperty("file")) {
			fix.setFile(getProperty("file"));
		}
		return fix;
	}

	/**
	 * Check whether this Node has valid data or not. A battlefield must have
	 * "kind" and "dc" properties (any others are ignored, for forward
	 * compatibility) and no children.
	 * 
	 * 
	 * @param warner
	 *            a Warning instance to use for warnings
	 * @param idFactory the factory to use to register ID numbers and generate new ones as needed
	 * @throws SPFormatException
	 *             if the data is invalid
	 */
	@Override
	public void checkNode(final Warning warner, final IDFactory idFactory)
			throws SPFormatException {
		forbidChildren("battlefield");
		demandProperty("battlefield", DC_PROPERTY, warner, false, false);
		registerOrCreateID("battlefield", idFactory, warner);
	}
	/**
	 * @param property the name of a property
	 * @return whether this kind of node can use the property
	 */
	@Override
	public boolean canUse(final String property) {
		return EqualsAny.equalsAny(property, DC_PROPERTY, "id");
	}

	/**
	 * 
	 * @return a String representation of this object.
	 */
	@Override
	public String toString() {
		return "BattlefieldEventNode";
	}
}
