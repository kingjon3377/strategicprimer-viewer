package model.exploration;

import java.util.List;

import model.map.Player;
import model.map.fixtures.mobile.Unit;
import model.misc.IMultiMapModel;
/**
 * A model for exploration drivers.
 * @author Jonathan Lovelace
 *
 */
public interface IExplorationModel extends IMultiMapModel {
	/**
	 * @return all the players that are shared by all the maps
	 */
	List<Player> getPlayerChoices();
	/**
	 * @param player a player
	 * @return all that player's units in the master map
	 */
	List<Unit> getUnits(final Player player);
}
