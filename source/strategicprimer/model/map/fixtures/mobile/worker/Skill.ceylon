import lovelace.util.common {
    todo
}

"A skill a worker has experience or training in."
shared class Skill(name, skillLevel, time) satisfies ISkill {
	"The name of the skill."
	shared actual String name;
	"How many levels the worker has in the skill."
	variable Integer skillLevel;
	"How many levels the worker has in the skill."
	shared actual Integer level => skillLevel;
	"How many hours of experience the worker has gained since last leveling up."
	variable Integer time;
	"How many hours of experience the worker has gained since last leveling up."
	shared actual Integer hours => time;
	"Clone the object."
	shared actual Skill copy() => Skill(name, level, hours);
	"An object is equal iff it is a Skill with the same name, level, and number of hours."
	todo("Specify interface instead of concrete class?")
	shared actual Boolean equals(Object obj) {
		if (is Skill obj) {
			return name == obj.name && level == obj.level && hours == obj.hours;
		} else {
			return false;
		}
	}
	shared actual Integer hash => name.hash;
	"Add hours of training or experience."
	shared actual void addHours(Integer hours, Integer condition) {
		time += hours;
		if (condition <= time) {
			skillLevel++;
			time = 0;
		}
	}
	shared actual String string => "``name`` (``level``)";
}