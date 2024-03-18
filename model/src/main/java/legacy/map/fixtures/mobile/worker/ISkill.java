package legacy.map.fixtures.mobile.worker;

import legacy.map.Subsettable;
import common.map.HasName;

import java.util.function.Consumer;

/**
 * An interface for Skills.
 */
public interface ISkill extends HasName, Subsettable<ISkill> {
	/**
	 * How many levels the worker has in the skill.
	 */
	int getLevel();

	/**
	 * How many hours of experience the worker has accumulated since the
	 * skill level last increased.
	 */
	int getHours();

	/**
	 * Clone the skill.
	 */
	ISkill copy();

	/**
	 * A skill is "empty" if the worker has no levels in it and no hours of
	 * experience in it.
	 */
	default boolean isEmpty() {
		return getLevel() == 0 && getHours() == 0;
	}

	/**
	 * A skill is a subset if it has the same name, equal or lower level,
	 * and if equal level equal or lower hours.
	 */
	@Override
	default boolean isSubset(final ISkill obj, final Consumer<String> report) {
		if (obj.getName().equals(getName())) {
			if (obj.getLevel() > getLevel()) {
				report.accept("Extra level(s) in " + getName());
				return false;
			} else if (obj.getLevel() == getLevel() && obj.getHours() > getHours()) {
				report.accept("Extra hours in " + getName());
				return false;
			} else {
				return true;
			}
		} else {
			report.accept("Called with non-corresponding skill, %s (this is %s)".formatted(
					obj.getName(), getName()));
			return false;
		}
	}
}
