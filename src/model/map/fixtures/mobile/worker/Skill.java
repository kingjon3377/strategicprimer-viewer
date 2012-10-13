package model.map.fixtures.mobile.worker;

import model.map.XMLWritableImpl;

/**
 * A skill a worker has experience or training in.
 * @author Jonathan Lovelace
 *
 */
public class Skill extends XMLWritableImpl {
	/**
	 * Constructor.
	 * @param skillName the name of the skill
	 * @param skillLevel how many levels the worker has in the skill
	 * @param time how many hours of training or experience the worker has gained since last gaining a level.
	 */
	public Skill(final String skillName, final int skillLevel, final int time) {
		super();
		name = skillName;
		level = skillLevel;
		hours = time;
	}
	/**
	 * The name of the skill.
	 */
	private final String name;
	/**
	 * How many levels the worker has in the skill.
	 */
	private final int level;
	/**
	 * How many hours the worker has gained since leveling up last.
	 */
	private final int hours;
	/**
	 * @return the name of the skill
	 */
	public String getName() {
		return name;
	}
	/**
	 * @return how many levels the worker has in the skill
	 */
	public int getLevel() {
		return level;
	}
	/**
	 * @return how many hours the worker has accumulated since leveling up last
	 */
	public int getHours() {
		return hours;
	}
	/**
	 * @param obj an object
	 * @return whether it's the same as this one
	 */
	@Override
	public boolean equals(final Object obj) {
		return this == obj
				|| (obj instanceof Skill && name.equals(((Skill) obj).name)
						&& level == ((Skill) obj).level && hours == ((Skill) obj).hours);
	}
	/**
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return name.hashCode();
	}
}
