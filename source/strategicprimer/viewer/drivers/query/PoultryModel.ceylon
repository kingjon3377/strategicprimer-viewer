import strategicprimer.model.map.fixtures {
	Quantity
}
"Models of (game statistics for) herding poultry." // TODO: convet to constructors?
abstract class PoultryModel(production, poundsCoefficient, dailyTimePerHead,
	extraChoresInterval) of chickens | turkeys | pigeons satisfies HerdModel {
	"The number of eggs produced per head per turn."
	Float production;
	"The amount produced per head per turn."
	shared actual Quantity productionPerHead = Quantity(production, "eggs");
	"The coefficient to turn production into pounds."
	shared actual Float poundsCoefficient;
	"How much time, per head, in minutes, must be spent to gather eggs."
	shared actual Integer dailyTimePerHead;
	"""How many turns, at most, should elapse between "extra chores" days."""
	shared Integer extraChoresInterval;
	"""How much time, in minutes, must be spent per head on "extra chores" days."""
	shared Integer extraTimePerHead = 30;
	"How much time, in minutes, herders must spend on a flock with this many animals per
	 herder."
	shared actual Integer dailyTime(Integer heads) => heads * dailyTimePerHead;
	"""How much time, in minutes, herders must spend on a flock with this many animals per
	   head on "extra chores" days."""
	shared Integer dailyExtraTime(Integer heads) => heads * extraTimePerHead;
}
"The model for chickens."
object chickens extends PoultryModel(0.75, 0.125, 2, 2) { }
"The model for turkeys."
object turkeys extends PoultryModel(0.75, 0.25, 2, 2) { }
"The model for pigeons."
object pigeons extends PoultryModel(0.5, 0.035, 1, 4) { }
