package controller.map.simplexml.node;

import model.viewer.PlayerCollection;
import model.viewer.Unit;
import controller.map.simplexml.AbstractChildNode;
import controller.map.simplexml.SPFormatException;
/**
 * A Node to represent a Unit.
 * @author kingjon
 *
 */
public class UnitNode extends AbstractChildNode<Unit> {
	/**
	 * Produce the equivalent Unit.
	 * @param players the players in the map
	 * @return the equivalent Unit.
	 * @throws SPFormatException if we contain invalid data.
	 */
	@Override
	public Unit produce(final PlayerCollection players) throws SPFormatException {
		return new Unit(players.getPlayer(hasProperty("owner") ? Integer
				.parseInt(getProperty("owner")) : -1),
				hasProperty("type") ? getProperty("type") : "",
				hasProperty("name") ? getProperty("name") : "");
	}
	/**
	 * Check whether we contain any invalid data. At present, this merely means that 
	 * the unit can't have any children, as neither of the properties we recognize 
	 * ("owner" and "type") do we require, and for forward compatibility we don't 
	 * object to properties we don't recognize. But if at some point we should start 
	 * requiring properties, that condition should be checked here. 
	 * @throws SPFormatException if contain invalid data.
	 */
	@Override
	public void checkNode() throws SPFormatException {
		if (iterator().hasNext()) {
			throw new SPFormatException("Unit should't contain anything", getLine());
		}
	}

}
