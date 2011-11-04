package controller.map.simplexml.node;

import model.map.PlayerCollection;
import model.map.events.TownStatus;
import model.map.fixtures.Mine;
import controller.map.SPFormatException;

/**
 * A Node that will produce a Mine.
 * @author Jonathan Lovelace
 */
public class MineNode extends AbstractFixtureNode<Mine> {
	/**
	 * @param players ignored
	 * @return the Mine this node represents
	 * @throws SPFormatException if missing required properties
	 */
	@Override
	public Mine produce(final PlayerCollection players) throws SPFormatException {
		return new Mine(getProperty("product"), TownStatus.parseTownStatus(getProperty("status")));
	}
	/**
	 * Check the data for validity. A Mine is valid if it has no children and "product" and "status" properties.
	 * @throws SPFormatException if the node is invalid.
	 */
	@Override
	public void checkNode() throws SPFormatException {
		if (iterator().hasNext()) {
			throw new SPFormatException("Mine should not have children", getLine());
		} else if (!hasProperty("product") || !hasProperty("status")) {
			throw new SPFormatException("Mine should have \"product\" and \"status\" properties", getLine());
		}
	}
}
