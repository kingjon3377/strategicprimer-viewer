package drivers.advancement;

import legacy.map.fixtures.mobile.IUnit;

/**
 * TODO: explain this class
 *
 * @author Jonathan Lovelace
 */
public interface AdvancementCLIHelper extends LevelGainSource {
	/**
	 * Let the user add experience to a worker or workers in a unit.
	 */
	void advanceWorkersInUnit(IUnit unit, boolean allowExpertMentoring);
}
