import strategicprimer.model.map.fixtures {
	Quantity
}
"Models of (game statistics for) herding."
interface HerdModel of PoultryModel | MammalModel {
	"How much is produced per head per turn, in some model-specified unit."
	shared formal Quantity productionPerHead;
	"The coefficient to turn production into pounds."
	shared formal Float poundsCoefficient;
	"How much time, per head, in minutes, must be spent to milk, gather eggs, or otherwise
	 collect the food produced by the animals. Callers may adjust this downward somewhat
	 if the herders in question are experts."
	shared formal Integer dailyTimePerHead;
	"""How much time is spent for a flock (per herder) of the given size, possibly
	   including any time "floor"."""
	shared formal Integer dailyTime(Integer heads);
	"How much is produced by a flock of the given size."
	shared Quantity scaledProduction(Integer heads) => Quantity(
		productionPerHead.floatNumber * heads, productionPerHead.units);
	"How many pounds are produced by a flock of the given size."
	shared Float scaledPoundsProduction(Integer heads) =>
			productionPerHead.floatNumber * heads * poundsCoefficient;
}
