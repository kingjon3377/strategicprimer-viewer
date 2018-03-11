object oneToTwoConfig {
	"The next turn, to use when creating
	 [[TextFixture|strategicprimer.model.map.fixtures::TextFixture]]s."
	shared Integer nextTurn = 15;
	"The factor by which to expand the map on each axis."
	shared Integer expansionFactor = 4;
	"The maximum number of iterations per tile."
	shared Integer maxIterations = 100;
	shared String maxIterationsWarning =
			"FIXME: A fixture here was force-added after ``maxIterations`` iterations";
	"Probability of turning a watered desert to plains."
	shared Float desertToPlains = 0.4;
	"Probability of adding a forest to a tile."
	shared Float addForestProbability = 0.1;
}