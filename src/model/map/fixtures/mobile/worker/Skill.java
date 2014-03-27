package model.map.fixtures.mobile.worker;

import model.map.HasName;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A skill a worker has experience or training in.
 *
 * @author Jonathan Lovelace
 *
 */
public class Skill implements HasName {
	/**
	 * The name of the skill.
	 */
	private String name;
	/**
	 * How many levels the worker has in the skill.
	 */
	private int level;
	/**
	 * How many hours the worker has gained since leveling up last.
	 */
	private int hours;

	/**
	 * Constructor.
	 *
	 * @param skillName the name of the skill
	 * @param skillLevel how many levels the worker has in the skill
	 * @param time how many hours of training or experience the worker has
	 *        gained since last gaining a level.
	 */
	public Skill(final String skillName, final int skillLevel, final int time) {
		super();
		name = skillName;
		level = skillLevel;
		hours = time;
	}

	/**
	 * @return the name of the skill
	 */
	@Override
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
	public boolean equals(@Nullable final Object obj) {
		return this == obj || obj instanceof Skill
				&& name.equals(((Skill) obj).name)
				&& level == ((Skill) obj).level && hours == ((Skill) obj).hours;
	}

	/**
	 * @return a hash value for the object
	 */
	@Override
	public int hashCode() {
		return name.hashCode();
	}

	/**
	 * Add hours of training or experience.
	 *
	 * @param hrs the number of hours to add
	 * @param condition If less than or equal to the number of hours after the
	 *        addition, level up and zero the hours instead.
	 */
	public void addHours(final int hrs, final int condition) {
		hours += hrs;
		if (condition <= hours) {
			level++;
			hours = 0;
		}
	}

	/**
	 * @return a string representation of the skill
	 */
	@Override
	public String toString() {
		return name + " (" + Integer.toString(level) + ')';
	}

	/**
	 * @param nomen the skill's new name
	 */
	@Override
	public final void setName(final String nomen) {
		name = nomen;
	}
}
