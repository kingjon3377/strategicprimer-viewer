
"The next turn, to use when creating
 [[TextFixture|strategicprimer.model.map.fixtures::TextFixture]]s."
Integer nextTurn = 15;
"The factor by which to expand the map on each axis."
Integer expansionFactor = 4;
"The maximum number of iterations per tile."
Integer maxIterations = 100;
shared String maxIterationsWarning = "FIXME: A fixture here was force-added after ``
maxIterations`` iterations";
"Probability of turning a watered desert to plains."
Float desertToPlains = 0.4;
"Probability of adding a forest to a tile."
Float addForestProbability = 0.1;
