package model.workermgmt;

import java.util.List;

import model.map.Player;
import model.map.fixtures.mobile.Unit;
import model.misc.IDriverModel;
/**
 * An interface for a model to underlie the advancement GUI, etc.
 * @author Jonathan Lovelace
 *
 */
public interface IWorkerModel extends IDriverModel {
	/**
	 * @param player a player in the map
	 * @return a list of the units in the map belonging to the player
	 */
	List<Unit> getUnits(final Player player);
}
