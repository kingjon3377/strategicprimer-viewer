import lovelace.util.common {
    todo
}

import model.map {
    HasName,
    Subsettable
}
import java.util {
    Formatter
}
"An interface for Skills."
todo("Split mutators into a separate interface?")
shared interface ISkill satisfies HasName&Subsettable<ISkill> {
	"How many levels the worker has in the skill."
	shared formal Integer level;
	"How many hours of experience the worker has accumulated since the skill level last
	 increased."
	shared formal Integer hours;
	"Add hours of training or experience."
	shared formal void addHours(
			"The number of hours to add."
			Integer hours,
			"If less than or equal to the total number of hours after the addition, level
			 up and zero the hours instead."
			Integer condition);
	"Clone the skill."
	shared formal ISkill copy();
	"""A skill is "empty" if the worker has no levels in it and no hours of experience in
	   it."""
	todo("Move to a default method here?")
	shared formal Boolean empty;
	"A skill is a subset if it has the same name, equal or lower level, and if equal level
	 equal or lower hours."
	shared actual default Boolean isSubset(ISkill obj, Formatter ostream, String context) {
		if (obj.name == name) {
			if (obj.level > level) {
				ostream.format("%s\tExtra level(s) in %s%n", context, obj.name);
				return false;
			} else if (obj.level == level, obj.hours > hours) {
				ostream.format("%s\tExtra hours in %s%n", context, obj.name);
				return false;
			} else {
				return true;
			}
		} else {
			ostream.format("%s\tCalled with non-corresponding skill, %s (this is %s)%n",
				context, obj.name, name);
			return false;
		}
	}
}