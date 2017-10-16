import strategicprimer.model.map.fixtures {
	Quantity
}
"Models of (game statistics for) herding mammals."
abstract class MammalModel(production, dailyTimePerHead) of dairyCattle | largeMammals |
		smallMammals satisfies HerdModel {
	"The amount produced per head per turn, in gallons"
	Float production;
	"How much time, per head, in minutes, must be spent to milk, or otherwise collect the
	 food produced by the animals."
	shared actual Integer dailyTimePerHead;
	"The amount produced per head per turn."
	shared actual Quantity productionPerHead = Quantity(production, "gallons");
	"The number of pounds per gallon."
	shared actual Float poundsCoefficient = 8.6;
	"How much time, in minutes, must be spent on the entire herd or flock each turn,
	 regardless of its size, in addition to each herder's time with individual animals."
	shared Integer dailyTimeFloor = 60;
	"How much time, in minutes, herders must spend on a flock with this many animals per
	 herder."
	shared actual Integer dailyTime(Integer heads) =>
			heads * dailyTimePerHead + dailyTimeFloor;
	"How much time, in minutes, an expert herder must spend on a flock with this many
	 animals per herder."
	shared Integer dailyExpertTime(Integer heads) =>
			heads * (dailyTimePerHead - 10) + dailyTimeFloor;
}
"The model for dairy cattle."
object dairyCattle extends MammalModel(4.0, 40) { }
"The model for other roughly-cattle-sized mammals. (Not for anything as large as
 elephants.)"
object largeMammals extends MammalModel(3.0, 40) { }
"The model for roughly-goat-sized mammals."
object smallMammals extends MammalModel(1.5, 30) { }
