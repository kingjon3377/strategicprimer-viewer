package common.map.fixtures.mobile.worker;

/**
 * A skill a worker has experience or training in.
 */
public final class Skill implements IMutableSkill {
	public Skill(final String name, final int skillLevel, final int time) {
		this.name = name;
		this.skillLevel = skillLevel;
		this.time = time;
	}

	/**
	 * The name of the skill.
	 */
	private final String name;

	/**
	 * The name of the skill.
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * How many levels the worker has in the skill.
	 */
	private int skillLevel;

	/**
	 * How many levels the worker has in the skill.
	 */
	@Override
	public int getLevel() {
		return skillLevel;
	}

	/**
	 * How many hours of experience the worker has gained since last leveling up.
	 */
	private int time;

	/**
	 * How many hours of experience the worker has gained since last leveling up.
	 */
	@Override
	public int getHours() {
		return time;
	}

	/**
	 * Clone the object.
	 */
	@Override
	public Skill copy() {
		return new Skill(name, skillLevel, time);
	}

	/**
	 * An object is equal iff it is a Skill with the same name, level, and number of hours.
	 */
	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof ISkill that) {
			return name.equals(that.getName()) &&
				skillLevel == that.getLevel() &&
				time == that.getHours();
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	/**
	 * Add hours of training or experience.
	 */
	@Override
	public void addHours(final int hours, final int condition) {
		time += hours;
		if (condition <= time) {
			skillLevel++;
			time = 0;
		}
	}

	@Override
	public String toString() {
		return String.format("%s (%d)", name, skillLevel);
	}
}
