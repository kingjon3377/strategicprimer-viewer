package controller.map.simplexml.node;

import model.map.Fortress;
import model.map.PlayerCollection;
import util.Warning;
import controller.map.SPFormatException;

/**
 * A node to produce a Fortress.
 * 
 * @author Jonathan Lovelace
 */
public class FortressNode extends AbstractFixtureNode<Fortress> {
	/**
	 * Produce the equivalent fortress.
	 * 
	 * @param players
	 *            the players in the map
	 * @return the equivalent fortress.
	 * @throws SPFormatException
	 *             if this node contains invalid data.
	 */
	@Override
	public Fortress produce(final PlayerCollection players)
			throws SPFormatException {
		final Fortress fort = new Fortress(
				players.getPlayer(hasProperty("owner") ? Integer
						.parseInt(getProperty("owner")) : -1),
				hasProperty("name") ? getProperty("name") : "");
		for (final AbstractXMLNode node : this) {
			if (node instanceof UnitNode) {
				fort.addUnit(((UnitNode) node).produce(players));
			} else {
				throw new SPFormatException(
						"Fortress should contain only units", node.getLine());
			}
		}
		return fort;
	}

	/**
	 * Check whether we contain valid data. A Fortress contains valid data iff
	 * every child is a Unit. At present we don't require it to have any
	 * properties, not even "owner" or "name"; if or when that changes, this
	 * should change to check those conditions.
	 * 
	 * 
	 * @throws SPFormatException
	 *             if we don't.
	 */
	@Override
	public void checkNode() throws SPFormatException {
		for (final AbstractXMLNode node : this) {
			if (node instanceof UnitNode) {
				node.checkNode();
			} else {
				throw new SPFormatException(
						"Fortress should contain only units", getLine());
			}
		}
		if (!hasProperty("owner")) {
			Warning.warn(new SPFormatException("Fortress should have an owner",
					getLine()));
		}
		if (!hasProperty("name")) {
			Warning.warn(new SPFormatException("Fortress should have a name",
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
