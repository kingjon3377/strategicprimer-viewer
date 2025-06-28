package drivers.advancement;

import legacy.map.fixtures.mobile.IUnit;

/**
 * An interface for the helper class that contains much of the logic of the advancement CLI.
 *
 * @author Jonathan Lovelace
 */
public interface AdvancementCLIHelper extends LevelGainSource {
	enum ExperienceConfig {
		SelfTeaching, ExpertMentoring
	}
	/**
	 * Let the user add experience to a worker or workers in a unit.
	 */
	void advanceWorkersInUnit(IUnit unit, ExperienceConfig experienceConfig);
}
