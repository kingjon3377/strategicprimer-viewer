package model.map.fixtures.mobile.worker;

import model.map.HasName;
/**
 * An interface for Skills.
 * @author Jonathan Lovelace
 *
 */
public interface ISkill extends HasName {
	/**
	 * @return how many levels the worker has in the skill
	 */
	int getLevel();

	/**
	 * @return how many hours the worker has accumulated since leveling up last
	 */
	int getHours();

	/**
	 * Add hours of training or experience.
	 *
	 * @param hrs the number of hours to add
	 * @param condition If less than or equal to the number of hours after the
	 *        addition, level up and zero the hours instead.
	 */
	void addHours(int hrs, int condition);
}